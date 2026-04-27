package com.stockpro.purchase.service.impl;

import com.stockpro.purchase.client.ProductClient;
import com.stockpro.purchase.client.SupplierClient;
import com.stockpro.purchase.client.WarehouseClient;
import com.stockpro.purchase.dto.*;
import com.stockpro.purchase.dto.event.PoPendingApprovalEvent;
import com.stockpro.purchase.entity.POLineItem;
import com.stockpro.purchase.entity.PurchaseOrder;
import com.stockpro.purchase.entity.PurchaseOrderStatus;
import com.stockpro.purchase.enums.AlertSeverity;
import com.stockpro.purchase.exception.BadRequestException;
import com.stockpro.purchase.exception.ConflictException;
import com.stockpro.purchase.exception.ExternalServiceException;
import com.stockpro.purchase.exception.ResourceNotFoundException;
import com.stockpro.purchase.mapper.PurchaseMapper;
import com.stockpro.purchase.publisher.AlertEventPublisher;
import com.stockpro.purchase.repository.PurchaseRepository;
import com.stockpro.purchase.security.JwtService;
import com.stockpro.purchase.service.PurchaseService;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductClient productClient;
    private final SupplierClient supplierClient;
    private final WarehouseClient warehouseClient;
    private final AlertEventPublisher alertEventPublisher;
    private final JwtService jwtService;
    private final PurchaseMapper purchaseMapper;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
            ProductClient productClient,
            SupplierClient supplierClient,
            WarehouseClient warehouseClient,
            AlertEventPublisher alertEventPublisher,
            JwtService jwtService,
            PurchaseMapper purchaseMapper) {
        this.purchaseRepository = purchaseRepository;
        this.productClient = productClient;
        this.supplierClient = supplierClient;
        this.warehouseClient = warehouseClient;
        this.alertEventPublisher = alertEventPublisher;
        this.jwtService = jwtService;
        this.purchaseMapper = purchaseMapper;
    }

    @Override
    public PurchaseOrderResponse createPO(PurchaseOrderRequest request) {
        if (request == null) {
            throw new BadRequestException("Purchase order payload is required.");
        }

        validateSupplierExists(request.getSupplierId());

        PurchaseOrder persistentPurchaseOrder = purchaseMapper.toEntity(request);
        persistentPurchaseOrder.setCreatedById(resolveCreatedById(null));
        
        // Re-calculate total amount and validate line items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (POLineItem lineItem : persistentPurchaseOrder.getLineItems()) {
            validateProductExists(lineItem.getProductId());
            totalAmount = totalAmount.add(lineItem.getTotalCost());
        }
        persistentPurchaseOrder.setTotalAmount(totalAmount);

        PurchaseOrder savedPurchaseOrder = purchaseRepository.save(persistentPurchaseOrder);
        publishPoPendingAlertAfterCommit(savedPurchaseOrder);
        return purchaseMapper.toResponse(savedPurchaseOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(Long poId) {
        return purchaseMapper.toResponse(getPurchaseOrder(poId));
    }

    @Override
    public PurchaseOrderResponse approvePO(Long poId) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(poId);

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new ConflictException("Cancelled purchase orders cannot be approved.");
        }

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.FULLY_RECEIVED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new ConflictException("Purchase orders with received goods cannot be re-approved.");
        }

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.APPROVED) {
            return purchaseMapper.toResponse(purchaseOrder);
        }

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT
                && purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new ConflictException("Only DRAFT or PENDING_APPROVAL purchase orders can be approved.");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        return purchaseMapper.toResponse(purchaseRepository.save(purchaseOrder));
    }

    @Override
    public PurchaseOrderResponse receiveGoods(Long poId, List<POLineItemRequest> receivedItems) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(poId);

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new ConflictException("Cancelled purchase orders cannot receive goods.");
        }

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.DRAFT
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new ConflictException("Purchase order must be approved before goods can be received.");
        }

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.FULLY_RECEIVED) {
            throw new ConflictException("All goods for this purchase order have already been received.");
        }

        if (receivedItems == null || receivedItems.isEmpty()) {
            throw new BadRequestException("At least one received item is required.");
        }

        Map<Long, POLineItem> lineItemsByProductId = new HashMap<>();
        for (POLineItem existingLineItem : purchaseOrder.getLineItems()) {
            lineItemsByProductId.put(existingLineItem.getProductId(), existingLineItem);
        }

        for (POLineItemRequest receivedItem : receivedItems) {
            POLineItem targetLineItem = lineItemsByProductId.get(receivedItem.getProductId());
            if (targetLineItem == null) {
                throw new ResourceNotFoundException("PO line item not found for productId: " + receivedItem.getProductId());
            }

            int currentReceived = targetLineItem.getReceivedQty() != null ? targetLineItem.getReceivedQty() : 0;
            int newReceivedQty = currentReceived + receivedItem.getQuantity();

            if (newReceivedQty > targetLineItem.getQuantity()) {
                throw new BadRequestException("Received quantity exceeds remaining quantity for productId " + targetLineItem.getProductId());
            }

            targetLineItem.setReceivedQty(newReceivedQty);
            
            // Update Warehouse Stock
            updateWarehouseStock(purchaseOrder.getWarehouseId(), targetLineItem.getProductId(), receivedItem.getQuantity(), poId);
        }

        // Update PO Status
        if (purchaseOrder.getLineItems().stream().allMatch(item -> item.getReceivedQty() >= item.getQuantity())) {
            purchaseOrder.setStatus(PurchaseOrderStatus.FULLY_RECEIVED);
        } else {
            purchaseOrder.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }

        purchaseOrder.setReceivedDate(LocalDate.now());
        return purchaseMapper.toResponse(purchaseRepository.save(purchaseOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPOsByStatus(String status) {
        return purchaseRepository.findByStatus(parseStatus(status)).stream()
                .map(purchaseMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPOsByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BadRequestException("Both start and end dates are required.");
        }
        return purchaseRepository.findByOrderDateBetween(start, end).stream()
                .map(purchaseMapper::toResponse)
                .toList();
    }

    @Override
    public PurchaseOrderResponse cancelPO(Long poId) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(poId);

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            return purchaseMapper.toResponse(purchaseOrder);
        }

        if (purchaseOrder.getStatus() == PurchaseOrderStatus.FULLY_RECEIVED) {
            throw new ConflictException("Fully received purchase orders cannot be cancelled.");
        }

        boolean hasAnyReceived = purchaseOrder.getLineItems().stream().anyMatch(item -> item.getReceivedQty() > 0);
        if (hasAnyReceived) {
            throw new ConflictException("Purchase orders with received goods cannot be cancelled.");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        return purchaseMapper.toResponse(purchaseRepository.save(purchaseOrder));
    }

    private PurchaseOrder getPurchaseOrder(Long poId) {
        return purchaseRepository.findByPoId(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + poId));
    }

    private void validateSupplierExists(Long supplierId) {
        try {
            supplierClient.getSupplierById(supplierId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Supplier not found with id: " + supplierId);
        }
    }

    private void validateProductExists(Long productId) {
        try {
            productClient.getProductById(productId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
    }

    private void updateWarehouseStock(Long warehouseId, Long productId, Integer quantity, Long poId) {
        warehouseClient.updateStock(WarehouseStockUpdateRequest.builder()
                .warehouseId(warehouseId)
                .productId(productId)
                .quantity(BigDecimal.valueOf(quantity))
                .referenceId(poId)
                .referenceType("PURCHASE_ORDER")
                .notes("Goods received against PO " + poId)
                .build());
    }

    private void publishPoPendingAlertAfterCommit(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.PENDING_APPROVAL) {
            runAfterCommit(() -> alertEventPublisher.publishPoPendingApprovalEvent(PoPendingApprovalEvent.builder()
                    .purchaseOrderId(purchaseOrder.getPoId())
                    .supplierId(purchaseOrder.getSupplierId())
                    .warehouseId(purchaseOrder.getWarehouseId())
                    .totalAmount(purchaseOrder.getTotalAmount())
                    .severity(AlertSeverity.INFO)
                    .eventTime(LocalDateTime.now())
                    .build()));
        }
    }

    private PurchaseOrderStatus parseStatus(String status) {
        try {
            return PurchaseOrderStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    private Long resolveCreatedById(Long requestedId) {
        if (requestedId != null) return requestedId;
        String token = extractCurrentToken();
        if (token != null) {
            return jwtService.extractUserId(token);
        }
        return 1L; // Fallback for testing
    }

    private String extractCurrentToken() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            String header = sra.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
        }
        return null;
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
