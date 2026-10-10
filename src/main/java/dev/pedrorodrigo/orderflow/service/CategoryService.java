package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Category;
import dev.pedrorodrigo.orderflow.dto.CategoryResponse;
import dev.pedrorodrigo.orderflow.dto.CreateCategoryRequest;
import dev.pedrorodrigo.orderflow.exception.CategoryInUseException;
import dev.pedrorodrigo.orderflow.exception.CategoryNotFoundException;
import dev.pedrorodrigo.orderflow.exception.ExistingCategoryException;
import dev.pedrorodrigo.orderflow.repository.CategoryRepository;
import dev.pedrorodrigo.orderflow.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

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

    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category with id '" + id + "' not found."
                ));

        if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new ExistingCategoryException(
                    "Category with name '" + request.name() + "' already exists.");
        }

        category.setName(request.name());
        category.setDescription(request.description());

        Category updatedCategory = categoryRepository.save(category);

        return new CategoryResponse(
                updatedCategory.getId(),
                updatedCategory.getName(),
                updatedCategory.getDescription(),
                updatedCategory.getCreatedAt(),
                updatedCategory.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(
                    "Category with id '" + id + "' not found."
            );
        }

        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException(
                    "Category with id '" + id + "' has associated products."
            );
        }

        categoryRepository.deleteById(id);
    }
}
