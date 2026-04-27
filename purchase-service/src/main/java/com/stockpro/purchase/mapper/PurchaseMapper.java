package com.stockpro.purchase.mapper;

import com.stockpro.purchase.dto.POLineItemRequest;
import com.stockpro.purchase.dto.POLineItemResponse;
import com.stockpro.purchase.dto.PurchaseOrderRequest;
import com.stockpro.purchase.dto.PurchaseOrderResponse;
import com.stockpro.purchase.entity.POLineItem;
import com.stockpro.purchase.entity.PurchaseOrder;
import com.stockpro.purchase.entity.PurchaseOrderStatus;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PurchaseMapper {

    public PurchaseOrder toEntity(PurchaseOrderRequest request) {
        if (request == null) return null;
        
        PurchaseOrder po = PurchaseOrder.builder()
                .supplierId(request.getSupplierId())
                .warehouseId(request.getWarehouseId())
                .orderDate(request.getOrderDate())
                .expectedDate(request.getExpectedDate())
                .notes(request.getNotes())
                .referenceNumber(request.getReferenceNumber())
                .status(PurchaseOrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .build();
        
        if (request.getLineItems() != null) {
            po.setLineItems(request.getLineItems().stream()
                    .map(item -> toEntity(item, po))
                    .collect(Collectors.toList()));
        }
        
        return po;
    }

    public POLineItem toEntity(POLineItemRequest request, PurchaseOrder po) {
        if (request == null) return null;
        return POLineItem.builder()
                .purchaseOrder(po)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .unitCost(request.getUnitCost())
                .receivedQty(0)
                .totalCost(request.getUnitCost().multiply(BigDecimal.valueOf(request.getQuantity())))
                .build();
    }

    public PurchaseOrderResponse toResponse(PurchaseOrder po) {
        if (po == null) return null;
        return PurchaseOrderResponse.builder()
                .poId(po.getPoId())
                .supplierId(po.getSupplierId())
                .warehouseId(po.getWarehouseId())
                .createdById(po.getCreatedById())
                .status(po.getStatus())
                .totalAmount(po.getTotalAmount())
                .orderDate(po.getOrderDate())
                .expectedDate(po.getExpectedDate())
                .receivedDate(po.getReceivedDate())
                .notes(po.getNotes())
                .referenceNumber(po.getReferenceNumber())
                .lineItems(po.getLineItems().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public POLineItemResponse toResponse(POLineItem item) {
        if (item == null) return null;
        return POLineItemResponse.builder()
                .lineItemId(item.getLineItemId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitCost(item.getUnitCost())
                .totalCost(item.getTotalCost())
                .receivedQty(item.getReceivedQty())
                .build();
    }
}
