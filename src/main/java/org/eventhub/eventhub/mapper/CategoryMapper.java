package org.eventhub.eventhub.mapper;

import org.eventhub.eventhub.dto.categories.CategoryResponseDto;
import org.eventhub.eventhub.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDto toResponseDto(Category category);
}