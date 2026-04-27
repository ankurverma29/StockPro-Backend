package com.stockpro.purchase.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class POLineItemRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitCost;
}
