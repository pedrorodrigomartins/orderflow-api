package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Category;
import dev.pedrorodrigo.orderflow.domain.entity.Product;
import dev.pedrorodrigo.orderflow.dto.CreateProductRequest;
import dev.pedrorodrigo.orderflow.dto.ProductResponse;
import dev.pedrorodrigo.orderflow.exception.CategoryNotFoundException;
import dev.pedrorodrigo.orderflow.exception.DuplicateSkuException;
import dev.pedrorodrigo.orderflow.repository.CategoryRepository;
import dev.pedrorodrigo.orderflow.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(
                    "Product with SKU '" + request.sku() + "' already exists."
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category with id '" + request.categoryId() + "' not found."
                ));

        Product product = new Product(
                request.name(),
                request.sku(),
                request.stockQuantity(),
                request.description(),
                request.price(),
                category
        );

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getSku(),
                savedProduct.getDescription(),
                savedProduct.getStockQuantity(),
                savedProduct.getPrice(),
                savedProduct.isActive(),
                savedProduct.getCategory().getId(),
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }
}
