package org.eventhub.eventhub.controller;

import lombok.RequiredArgsConstructor;
import org.eventhub.eventhub.dto.categories.CategoryResponseDto;
import org.eventhub.eventhub.repo.CategoryRepository;
import org.eventhub.eventhub.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
