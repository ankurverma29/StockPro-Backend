package com.stockpro.movement.controller;

import com.stockpro.movement.dto.request.MovementSearchRequest;
import com.stockpro.movement.dto.request.RecordMovementRequest;
import com.stockpro.movement.dto.response.ApiResponse;
import com.stockpro.movement.dto.response.MovementResponse;
import com.stockpro.movement.dto.response.MovementSummaryResponse;
import com.stockpro.movement.dto.response.StockInOutSummaryResponse;
import com.stockpro.movement.service.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/movements")
@Tag(name = "Stock Movement Management", description = "Stock in/out, transfers, and audit history APIs.")
@SecurityRequirement(name = "bearerAuth")
public class MovementController {

    private static final String RECORD_MOVEMENT_EXAMPLE = """
            {
              "warehouseId": 1,
              "productId": 501,
              "quantity": 25.0000,
              "unitCost": 149.5000,
              "movementType": "STOCK_IN",
              "referenceId": 9001,
              "referenceType": "PURCHASE_ORDER",
              "notes": "Initial stock receipt from PO-9001"
            }
            """;

    private static final String SEARCH_MOVEMENT_EXAMPLE = """
            {
              "warehouseId": 1,
              "productId": 501,
              "movementType": "STOCK_IN",
              "startDate": "2024-01-01",
              "endDate": "2024-03-31",
              "page": 0,
              "size": 20,
              "sortBy": "movementDate",
              "sortDir": "desc"
            }
            """;

    private final MovementService movementService;

    public MovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF')")
    @Operation(
            summary = "Record a new stock movement",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "RecordMovement", value = RECORD_MOVEMENT_EXAMPLE))))
    public ResponseEntity<ApiResponse<MovementResponse>> recordMovement(@Valid @RequestBody RecordMovementRequest request) {
        MovementResponse response = movementService.recordMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock movement recorded successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get movement by id")
    public ResponseEntity<ApiResponse<MovementResponse>> getById(@PathVariable Long id) {
        MovementResponse response = movementService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Movement retrieved successfully"));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get movements by product")
    public ResponseEntity<ApiResponse<Page<MovementResponse>>> getByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        Page<MovementResponse> response = movementService.getByProduct(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Movements for product " + productId + " retrieved successfully"));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get movements by warehouse")
    public ResponseEntity<ApiResponse<Page<MovementResponse>>> getByWarehouse(
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        Page<MovementResponse> response = movementService.getByWarehouse(warehouseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Movements for warehouse " + warehouseId + " retrieved successfully"));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(
            summary = "Search movements with filters",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "SearchMovements", value = SEARCH_MOVEMENT_EXAMPLE))))
    public ResponseEntity<ApiResponse<Page<MovementResponse>>> searchMovements(@Valid @RequestBody MovementSearchRequest request) {
        Page<MovementResponse> response = movementService.searchMovements(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Movement search results retrieved successfully"));
    }

    @GetMapping("/summary/stock-in-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get stock in/out summary for a product across warehouses")
    public ResponseEntity<ApiResponse<StockInOutSummaryResponse>> getStockInOutSummary(@RequestParam Long productId) {
        StockInOutSummaryResponse response = movementService.getStockInOutSummary(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock in/out summary retrieved successfully"));
    }

    @GetMapping("/summary/warehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get movement summary by warehouse")
    public ResponseEntity<ApiResponse<List<MovementSummaryResponse>>> getMovementSummaryByWarehouse() {
        List<MovementSummaryResponse> response = movementService.getMovementSummaryByWarehouse();
        return ResponseEntity.ok(ApiResponse.success(response, "Movement summary by warehouse retrieved successfully"));
    }

    @GetMapping("/warehouse/internal/movements")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Internal endpoint for report-service to fetch movements by date range")
    public ResponseEntity<ApiResponse<List<MovementResponse>>> getMovementsByDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        List<MovementResponse> response = movementService.getMovementsByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success(response, "Internal movements retrieved successfully"));
    }
}
