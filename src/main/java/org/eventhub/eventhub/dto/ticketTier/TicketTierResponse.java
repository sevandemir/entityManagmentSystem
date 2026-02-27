package org.eventhub.eventhub.dto.ticketTier;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter

public class TicketTierResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer soldCount;
    private Integer availableCount;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;
    private boolean saleActive;
}
