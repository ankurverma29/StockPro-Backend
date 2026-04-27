package com.stockpro.purchase.controller;

import com.stockpro.purchase.dto.ApiResponse;
import com.stockpro.purchase.dto.POLineItemRequest;
import com.stockpro.purchase.dto.PurchaseOrderRequest;
import com.stockpro.purchase.dto.PurchaseOrderResponse;
import com.stockpro.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@Tag(name = "Purchase Order Management", description = "Procurement lifecycle APIs for StockPro purchase orders.")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseResource {

    private final PurchaseService purchaseService;

    public PurchaseResource(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_OFFICER')")
    @Operation(summary = "Create a new purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> createPO(@RequestBody PurchaseOrderRequest request) {
        PurchaseOrderResponse response = purchaseService.createPO(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Purchase order created successfully"));
    }

    @GetMapping("/{poId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get purchase order by id")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getById(@PathVariable Long poId) {
        PurchaseOrderResponse response = purchaseService.getById(poId);
        return ResponseEntity.ok(ApiResponse.success(response, "Purchase order retrieved successfully"));
    }

    @PutMapping("/{poId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER')")
    @Operation(summary = "Approve a purchase order")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> approvePO(@PathVariable Long poId) {
        PurchaseOrderResponse response = purchaseService.approvePO(poId);
        return ResponseEntity.ok(ApiResponse.success(response, "Purchase order approved successfully"));
    }

    @PostMapping("/{poId}/receive")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Receive goods for a purchase order and update warehouse stock")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> receiveGoods(@PathVariable Long poId,
            @RequestBody List<POLineItemRequest> receivedItems) {
        PurchaseOrderResponse response = purchaseService.receiveGoods(poId, receivedItems);
        return ResponseEntity.ok(ApiResponse.success(response, "Goods received successfully"));
    }

    @PutMapping("/{poId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_OFFICER')")
    @Operation(summary = "Cancel a purchase order before goods are received")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> cancelPO(@PathVariable Long poId) {
        PurchaseOrderResponse response = purchaseService.cancelPO(poId);
        return ResponseEntity.ok(ApiResponse.success(response, "Purchase order cancelled successfully"));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get purchase orders by status")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getPOsByStatus(@PathVariable String status) {
        List<PurchaseOrderResponse> response = purchaseService.getPOsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(response, "Purchase orders retrieved successfully"));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get purchase orders within an order-date range")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getPOsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<PurchaseOrderResponse> response = purchaseService.getPOsByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success(response, "Purchase orders retrieved successfully"));
    }
}
