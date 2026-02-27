package org.eventhub.eventhub.service.impl;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.categories.CategoryResponseDto;
import org.eventhub.eventhub.mapper.CategoryMapper;
import org.eventhub.eventhub.repo.CategoryRepository;
import org.eventhub.eventhub.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }
}
