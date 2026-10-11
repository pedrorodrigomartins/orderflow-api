package dev.pedrorodrigo.orderflow.controller;

import dev.pedrorodrigo.orderflow.dto.CreateProductRequest;
import dev.pedrorodrigo.orderflow.dto.ProductResponse;
import dev.pedrorodrigo.orderflow.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.createProduct(request);
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return  productService.getProductById(id);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> getProductByCategoryId(@PathVariable Long categoryId) {
        return productService.getProductByCategoryId(categoryId);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @PatchMapping("/{id}/activate")
    public ProductResponse activateProduct(@PathVariable Long id) {
        return productService.updateProductActiveStatus(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    public ProductResponse deactivateProduct(@PathVariable Long id) {
        return productService.updateProductActiveStatus(id, false);
    }
}
