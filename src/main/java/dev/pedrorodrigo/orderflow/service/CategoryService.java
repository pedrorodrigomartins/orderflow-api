package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Category;
import dev.pedrorodrigo.orderflow.dto.CategoryResponse;
import dev.pedrorodrigo.orderflow.dto.CreateCategoryRequest;
import dev.pedrorodrigo.orderflow.exception.CategoryNotFoundException;
import dev.pedrorodrigo.orderflow.exception.ExistingCategoryException;
import dev.pedrorodrigo.orderflow.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ExistingCategoryException(
                    "Category with name '" + request.name() + "' already exists.");
        }

        Category category = new Category(
                request.name(),
                request.description()
        );

        Category savedCategory = categoryRepository.save(category);

        return new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getDescription(),
                savedCategory.getCreatedAt(),
                savedCategory.getUpdatedAt()
        );
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.getCreatedAt(),
                        category.getUpdatedAt()
                ))
                .toList();
    }

    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category with id '" + id + "' not found."
                ));

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

}
