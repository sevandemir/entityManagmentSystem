package org.eventhub.eventhub.service;

import org.eventhub.eventhub.dto.categories.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> getAllCategories();
}