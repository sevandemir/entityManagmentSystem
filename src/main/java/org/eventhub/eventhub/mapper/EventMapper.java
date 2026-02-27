package org.eventhub.eventhub.mapper;

import org.eventhub.eventhub.dto.event.*;
import org.eventhub.eventhub.entity.Event;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EventMapper {
    // CreateDto → Entity
    // categoryId ve organizer manuel set edilecek (ilişkisel alan)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "ticketTiers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "eventStatus", ignore = true)
    Event toEntity(EventCreateRequestDto dto);

    // Entity → ResponseDto
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "organizerName", expression = "java(event.getOrganizer().getDisplayName())")
    EventResponseDto toResponseDto(Event event);

    // Update — null alanları atla
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "ticketTiers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "eventStatus", ignore = true)
    void updateEntityFromDto(EventUpdateRequestDto dto, @MappingTarget Event event);

}
