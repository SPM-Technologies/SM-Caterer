package com.smtech.SM_Caterer.service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for stock/inventory report data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long materialId;
    private String materialCode;
    private String materialName;
    private String groupName;
    private String unitName;
    private String unitSymbol;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private BigDecimal costPerUnit;
    private BigDecimal totalValue;
    private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    private Boolean isActive;
}
