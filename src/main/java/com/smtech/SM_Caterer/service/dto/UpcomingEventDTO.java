package com.smtech.SM_Caterer.service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for upcoming events display on dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNumber;
    private String customerName;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String eventTypeName;
    private String venueName;
    private Integer guestCount;
    private BigDecimal balance;
    private Long daysUntilEvent;
}
