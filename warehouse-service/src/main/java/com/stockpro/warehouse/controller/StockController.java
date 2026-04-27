package com.stockpro.warehouse.controller;

import com.stockpro.warehouse.dto.request.ReleaseReservationRequest;
import com.stockpro.warehouse.dto.request.ReserveStockRequest;
import com.stockpro.warehouse.dto.request.StockSearchRequest;
import com.stockpro.warehouse.dto.request.TransferStockRequest;
import com.stockpro.warehouse.dto.request.UpdateStockRequest;
import com.stockpro.warehouse.dto.response.*;
import com.stockpro.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/stock")
@Tag(name = "Stock Management", description = "Stock levels, reservations, transfers and low stock lookup APIs.")
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private static final String UPDATE_STOCK_EXAMPLE = """
            {
              "warehouseId": 1,
              "productId": 501,
              "quantity": 25.0000,
              "unitCost": 149.5000,
              "referenceId": 9001,
              "referenceType": "PURCHASE_ORDER",
              "location": "A-01-RACK-03",
              "notes": "Goods received against PO-9001"
            }
            """;

    private static final String RESERVE_STOCK_EXAMPLE = """
            {
              "warehouseId": 1,
              "productId": 501,
              "quantity": 5.0000,
              "referenceId": 12001,
              "referenceType": "SALES_ORDER",
              "notes": "Reserved for SO-12001"
            }
            """;

    private static final String RELEASE_STOCK_EXAMPLE = """
            {
              "warehouseId": 1,
              "productId": 501,
              "quantity": 2.0000,
              "referenceId": 12001,
              "referenceType": "SALES_ORDER",
              "notes": "Released after order change"
            }
            """;

    private static final String TRANSFER_STOCK_EXAMPLE = """
            {
              "sourceWarehouseId": 1,
              "destinationWarehouseId": 2,
              "productId": 501,
              "quantity": 12.0000,
              "referenceId": 15001,
              "referenceType": "WAREHOUSE_TRANSFER",
              "unitCost": 149.5000,
              "notes": "Redistribution to south fulfillment center"
            }
            """;

    private static final String SEARCH_STOCK_EXAMPLE = """
            {
              "warehouseId": 1,
              "productId": 501,
              "location": "A-01",
              "lowStockOnly": false,
              "page": 0,
              "size": 20,
              "sortBy": "lastUpdated",
              "sortDir": "desc"
            }
            """;

    private final WarehouseService warehouseService;

    public StockController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/level")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get stock level by warehouse and product")
    public ResponseEntity<ApiResponse<StockLevelResponse>> getStockLevel(@RequestParam Long warehouseId, @RequestParam Long productId) {
        StockLevelResponse response = warehouseService.getStockLevel(warehouseId, productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock level retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get stock level by id")
    public ResponseEntity<ApiResponse<StockLevelResponse>> getById(@PathVariable Long id) {
        StockLevelResponse response = warehouseService.getStockById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock level retrieved successfully"));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(
            summary = "Adjust stock quantity",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "UpdateStock", value = UPDATE_STOCK_EXAMPLE))))
    public ResponseEntity<ApiResponse<StockLevelResponse>> updateStock(@Valid @RequestBody UpdateStockRequest request) {
        StockLevelResponse response = warehouseService.updateStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock updated successfully"));
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(
            summary = "Reserve available stock",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "ReserveStock", value = RESERVE_STOCK_EXAMPLE))))
    public ResponseEntity<ApiResponse<StockLevelResponse>> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        StockLevelResponse response = warehouseService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock reserved successfully"));
    }

    @PostMapping("/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(
            summary = "Release reserved stock",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "ReleaseReservation", value = RELEASE_STOCK_EXAMPLE))))
    public ResponseEntity<ApiResponse<StockLevelResponse>> releaseReservation(
            @Valid @RequestBody ReleaseReservationRequest request) {
        StockLevelResponse response = warehouseService.releaseReservation(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Reservation released successfully"));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(
            summary = "Transfer stock atomically between warehouses",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "TransferStock", value = TRANSFER_STOCK_EXAMPLE))))
    public ResponseEntity<ApiResponse<TransferStockResponse>> transferStock(@Valid @RequestBody TransferStockRequest request) {
        TransferStockResponse response = warehouseService.transferStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock transfer completed successfully"));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get stock by warehouse")
    public ResponseEntity<ApiResponse<Page<StockLevelResponse>>> getStockByWarehouse(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "lastUpdated") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<StockLevelResponse> response = warehouseService.getStockByWarehouse(warehouseId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock levels for warehouse " + warehouseId + " retrieved successfully"));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get stock by product")
    public ResponseEntity<ApiResponse<Page<StockLevelResponse>>> getStockByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "lastUpdated") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<StockLevelResponse> response = warehouseService.getStockByProduct(productId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock levels for product " + productId + " retrieved successfully"));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get low stock items for alerting and dashboards")
    public ResponseEntity<ApiResponse<Page<LowStockItemResponse>>> getLowStockItems(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        Page<LowStockItemResponse> response = warehouseService.getLowStockItems(page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Low stock items retrieved successfully"));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(
            summary = "Advanced stock search",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "SearchStock", value = SEARCH_STOCK_EXAMPLE))))
    public ResponseEntity<ApiResponse<Page<StockLevelResponse>>> searchStock(@Valid @RequestBody StockSearchRequest request) {
        Page<StockLevelResponse> response = warehouseService.searchStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock search results retrieved successfully"));
    }

    @GetMapping("/internal/low-stock")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Internal low stock endpoint used by alert-service scheduler")
    public ResponseEntity<ApiResponse<List<LowStockItemResponse>>> getInternalLowStockItems() {
        List<LowStockItemResponse> response = warehouseService.getLowStockItemsForAlert();
        return ResponseEntity.ok(ApiResponse.success(response, "Internal low stock items retrieved successfully"));
    }

    @PostMapping("/internal/stock-levels")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Internal live stock lookup endpoint used by product-service")
    public ResponseEntity<ApiResponse<List<StockLevelQuantityResponse>>> getStockLevels(@RequestBody List<Long> productIds) {
        List<StockLevelQuantityResponse> response = warehouseService.getStockLevels(productIds);
        return ResponseEntity.ok(ApiResponse.success(response, "Internal stock levels retrieved successfully"));
    }
}
