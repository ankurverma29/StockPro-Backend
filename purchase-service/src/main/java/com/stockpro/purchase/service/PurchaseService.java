package com.stockpro.purchase.service;

import com.stockpro.purchase.dto.POLineItemRequest;
import com.stockpro.purchase.dto.PurchaseOrderRequest;
import com.stockpro.purchase.dto.PurchaseOrderResponse;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseService {

    PurchaseOrderResponse createPO(PurchaseOrderRequest request);

    PurchaseOrderResponse getById(Long poId);

    PurchaseOrderResponse approvePO(Long poId);

    PurchaseOrderResponse receiveGoods(Long poId, List<POLineItemRequest> receivedItems);

    List<PurchaseOrderResponse> getPOsByStatus(String status);

    List<PurchaseOrderResponse> getPOsByDateRange(LocalDate start, LocalDate end);

    PurchaseOrderResponse cancelPO(Long poId);
}
