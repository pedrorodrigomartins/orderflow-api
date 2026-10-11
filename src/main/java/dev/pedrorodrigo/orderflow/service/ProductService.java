
package dev.pedrorodrigo.orderflow.service;

import dev.pedrorodrigo.orderflow.domain.entity.Category;
import dev.pedrorodrigo.orderflow.domain.entity.Product;
import dev.pedrorodrigo.orderflow.dto.CreateProductRequest;
import dev.pedrorodrigo.orderflow.dto.ProductResponse;
import dev.pedrorodrigo.orderflow.exception.CategoryNotFoundException;
import dev.pedrorodrigo.orderflow.exception.DuplicateSkuException;
import dev.pedrorodrigo.orderflow.exception.ProductNotFoundException;
import dev.pedrorodrigo.orderflow.repository.CategoryRepository;
import dev.pedrorodrigo.orderflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
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
        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with id '" + id + "' not found."
                ));

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with id '" + id + "' not found."
                ));

        if (!product.getSku().equals(request.sku())
                && productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(
                    "Product with SKU '" + request.sku() + "' already exists."
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category with id '" + request.categoryId() + "' not found."
                ));

        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setStockQuantity(request.stockQuantity());
        product.setPrice(request.price());
        product.setCategory(category);

        productRepository.flush();
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProductActiveStatus(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product with id '" + id + "' not found."
                ));

        product.changeActiveStatus(active);
        productRepository.flush();
        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getStockQuantity(),
                product.getPrice(),
                product.isActive(),
                product.getCategory().getId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
