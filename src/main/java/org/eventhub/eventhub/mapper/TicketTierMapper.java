package org.eventhub.eventhub.mapper;

import org.eventhub.eventhub.dto.ticketTier.TicketTierCreateRequest;
import org.eventhub.eventhub.dto.ticketTier.TicketTierResponse;
import org.eventhub.eventhub.entity.TicketTier;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TicketTierMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "soldCount", constant = "0")
    TicketTier toEntity(TicketTierCreateRequest dto);

    @Mapping(target = "availableCount",
            expression = "java(tier.getTotalQuantity() - tier.getSoldCount())")
    @Mapping(target = "saleActive",
            expression = "java(java.time.LocalDateTime.now().isAfter(tier.getSaleStartDate()) && java.time.LocalDateTime.now().isBefore(tier.getSaleEndDate()))")
    TicketTierResponse toResponse(TicketTier tier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    void updateEntityFromDto(TicketTierCreateRequest request, @MappingTarget TicketTier tier);
}
