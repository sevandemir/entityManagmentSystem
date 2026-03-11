package org.eventhub.eventhub.dto.ticketTier;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.eventhub.eventhub.enums.TicketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter

public class TicketTierCreateRequest {
    @NotNull(message = "Bilet türü zorunludur")
    private TicketType ticketType;

    @NotNull(message = "Fiyat zorunludur")
    @DecimalMin(value = "0.0", message = "Fiyat 0'dan küçük olamaz")
    private BigDecimal price;

    @NotNull(message = "Toplam adet zorunludur")
    @Min(value = 1, message = "En az 1 bilet olmalı")
    private Integer totalQuantity;

    @NotNull(message = "Satış başlangıç tarihi zorunludur")
    private LocalDateTime saleStartDate;

    @NotNull(message = "Satış bitiş tarihi zorunludur")
    private LocalDateTime saleEndDate;
}
