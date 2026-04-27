package com.stockpro.purchase.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderRequest {
    private Long supplierId;
    private Long warehouseId;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private String notes;
    private String referenceNumber;
    private List<POLineItemRequest> lineItems;
}
