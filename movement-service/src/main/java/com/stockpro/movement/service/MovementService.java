package com.stockpro.movement.service;

import com.stockpro.movement.dto.request.MovementSearchRequest;
import com.stockpro.movement.dto.request.RecordMovementRequest;
import com.stockpro.movement.dto.response.MovementResponse;
import com.stockpro.movement.dto.response.MovementSummaryResponse;
import com.stockpro.movement.dto.response.StockInOutSummaryResponse;
import com.stockpro.movement.enums.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;

public interface MovementService {

    MovementResponse recordMovement(RecordMovementRequest request);

    MovementResponse getById(Long id);

    Page<MovementResponse> getByProduct(Long productId, int page, int size);

    Page<MovementResponse> getByWarehouse(Long warehouseId, int page, int size);

    List<MovementResponse> getByType(MovementType movementType);

    List<MovementResponse> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<MovementResponse> getByReference(Long referenceId);

    List<MovementResponse> getMovementHistory(Long productId, Long warehouseId);

    BigDecimal getStockIn(Long productId);

    BigDecimal getStockOut(Long productId);

    StockInOutSummaryResponse getStockInOutSummary(Long productId);

    List<MovementSummaryResponse> getMovementSummaryByWarehouse();

    List<MovementResponse> getMovementsByDateRange(String start, String end);

    Page<MovementResponse> getAllMovements(int page, int size, String sortBy, String sortDir);

    Page<MovementResponse> searchMovements(MovementSearchRequest request);
}
