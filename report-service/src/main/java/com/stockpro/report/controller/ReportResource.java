package com.stockpro.report.controller;

import com.stockpro.report.dto.response.*;
import com.stockpro.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/reports")
@Tag(name = "Inventory Reporting", description = "On-demand and scheduled inventory analysis and export APIs.")
@SecurityRequirement(name = "bearerAuth")
public class ReportResource {

    private final ReportService reportService;

    public ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory-snapshot")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get total stock valuation snapshot")
    public ResponseEntity<ApiResponse<TotalStockValueResponse>> getTotalStockValue() {
        TotalStockValueResponse response = reportService.getTotalStockValue();
        return ResponseEntity.ok(ApiResponse.success(response, "Total stock valuation retrieved successfully"));
    }

    @GetMapping("/inventory-snapshot/warehouse")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get stock valuation breakdown by warehouse")
    public ResponseEntity<ApiResponse<List<WarehouseStockValueResponse>>> getStockValueByWarehouse() {
        List<WarehouseStockValueResponse> response = reportService.getStockValueByWarehouse();
        return ResponseEntity.ok(ApiResponse.success(response, "Stock value by warehouse retrieved successfully"));
    }

    @GetMapping("/inventory-turnover")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get inventory turnover analysis")
    public ResponseEntity<ApiResponse<List<InventoryTurnoverResponse>>> getInventoryTurnover(@RequestParam int days) {
        List<InventoryTurnoverResponse> response = reportService.getInventoryTurnover(days);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory turnover analysis retrieved successfully"));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get current low stock items across all warehouses")
    public ResponseEntity<ApiResponse<List<LowStockReportResponse>>> getLowStockReport() {
        List<LowStockReportResponse> response = reportService.getLowStockReport();
        return ResponseEntity.ok(ApiResponse.success(response, "Low stock report retrieved successfully"));
    }

    @GetMapping("/top-moving")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get top moving products for a period")
    public ResponseEntity<ApiResponse<List<TopMovingProductResponse>>> getTopMovingProducts(@RequestParam int days, @RequestParam int limit) {
        List<TopMovingProductResponse> response = reportService.getTopMovingProducts(days, limit);
        return ResponseEntity.ok(ApiResponse.success(response, "Top moving products retrieved successfully"));
    }

    @GetMapping("/slow-moving")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get slow moving products")
    public ResponseEntity<ApiResponse<List<SlowMovingProductResponse>>> getSlowMovingProducts(@RequestParam int days) {
        List<SlowMovingProductResponse> response = reportService.getSlowMovingProducts(days);
        return ResponseEntity.ok(ApiResponse.success(response, "Slow moving products retrieved successfully"));
    }

    @GetMapping("/dead-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get dead stock (no movement) over a period")
    public ResponseEntity<ApiResponse<List<DeadStockResponse>>> getDeadStock(@RequestParam int days) {
        List<DeadStockResponse> response = reportService.getDeadStock(days);
        return ResponseEntity.ok(ApiResponse.success(response, "Dead stock retrieved successfully"));
    }

    @GetMapping("/po-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get purchase order summary by status")
    public ResponseEntity<ApiResponse<List<PurchaseOrderSummaryResponse>>> getPurchaseOrderSummary() {
        List<PurchaseOrderSummaryResponse> response = reportService.getPurchaseOrderSummary();
        return ResponseEntity.ok(ApiResponse.success(response, "Purchase order summary retrieved successfully"));
    }

    @GetMapping("/movement-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get daily movement summary counts")
    public ResponseEntity<ApiResponse<List<StockMovementSummaryResponse>>> getMovementSummary(@RequestParam int days) {
        List<StockMovementSummaryResponse> response = reportService.getMovementSummary(days);
        return ResponseEntity.ok(ApiResponse.success(response, "Movement summary retrieved successfully"));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Trigger a manual report export (PDF/Excel/CSV)")
    public ResponseEntity<ApiResponse<GeneratedReportResponse>> exportReport(
            @RequestParam String reportType,
            @RequestParam String format) {
        GeneratedReportResponse response = reportService.exportReport(reportType, format);
        return ResponseEntity.ok(ApiResponse.success(response, "Report export triggered successfully"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Get history of daily snapshots")
    public ResponseEntity<ApiResponse<List<InventorySnapshotResponse>>> getSnapshotHistory(@RequestParam int limit) {
        List<InventorySnapshotResponse> response = reportService.getSnapshotHistory(limit);
        return ResponseEntity.ok(ApiResponse.success(response, "Snapshot history retrieved successfully"));
    }
}
