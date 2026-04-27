package com.stockpro.report.service;

import com.stockpro.report.dto.request.GenerateReportRequest;
import com.stockpro.report.dto.request.ReportFilterRequest;
import com.stockpro.report.dto.request.TakeSnapshotRequest;
import com.stockpro.report.dto.response.DeadStockResponse;
import com.stockpro.report.dto.response.GeneratedReportResponse;
import com.stockpro.report.dto.response.InventorySnapshotResponse;
import com.stockpro.report.dto.response.InventoryTurnoverResponse;
import com.stockpro.report.dto.response.LowStockReportResponse;
import com.stockpro.report.dto.response.PurchaseOrderSummaryResponse;
import com.stockpro.report.dto.response.StockMovementSummaryResponse;
import com.stockpro.report.dto.response.SlowMovingProductResponse;
import com.stockpro.report.dto.response.TopMovingProductResponse;
import com.stockpro.report.dto.response.TotalStockValueResponse;
import com.stockpro.report.dto.response.WarehouseStockValueResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ReportService {

    InventorySnapshotResponse takeSnapshot(TakeSnapshotRequest request);

    void takeDailySnapshot();

    TotalStockValueResponse getTotalStockValue(LocalDate snapshotDate);

    TotalStockValueResponse getTotalStockValue();

    List<WarehouseStockValueResponse> getStockValueByWarehouse(LocalDate snapshotDate);

    List<WarehouseStockValueResponse> getStockValueByWarehouse();

    List<InventoryTurnoverResponse> getInventoryTurnover(ReportFilterRequest request);

    List<InventoryTurnoverResponse> getInventoryTurnover(int days);

    Page<LowStockReportResponse> getLowStockReport(ReportFilterRequest request);

    List<LowStockReportResponse> getLowStockReport();

    StockMovementSummaryResponse getStockMovementSummary(ReportFilterRequest request);

    List<StockMovementSummaryResponse> getMovementSummary(int days);

    Page<TopMovingProductResponse> getTopMovingProducts(ReportFilterRequest request);

    List<TopMovingProductResponse> getTopMovingProducts(int days, int limit);

    Page<SlowMovingProductResponse> getSlowMovingProducts(ReportFilterRequest request);

    List<SlowMovingProductResponse> getSlowMovingProducts(int days);

    Page<DeadStockResponse> getDeadStock(ReportFilterRequest request);

    List<DeadStockResponse> getDeadStock(int days);

    PurchaseOrderSummaryResponse getPOSummary(ReportFilterRequest request);

    List<PurchaseOrderSummaryResponse> getPurchaseOrderSummary();

    GeneratedReportResponse generateInventoryReport(GenerateReportRequest request);

    GeneratedReportResponse exportReport(String reportType, String format);

    List<InventorySnapshotResponse> getSnapshotHistory(int limit);
}
