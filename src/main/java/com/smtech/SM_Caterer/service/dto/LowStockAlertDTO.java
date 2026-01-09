package com.smtech.SM_Caterer.service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for low stock alerts on dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockAlertDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long materialId;
    private String materialCode;
    private String materialName;
    private String groupName;
    private String unitSymbol;
    private BigDecimal currentStock;
    private BigDecimal minimumStock;
    private BigDecimal shortfall;
    private String severity; // CRITICAL, WARNING
}
