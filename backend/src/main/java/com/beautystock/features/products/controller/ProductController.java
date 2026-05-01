package com.beautystock.features.products.controller;

import com.beautystock.features.products.dto.CreateProductDTO;
import com.beautystock.features.products.dto.ProductDTO;
import com.beautystock.features.products.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody CreateProductDTO request) {
        return ResponseEntity.ok(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody CreateProductDTO request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<ProductDTO> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productService.uploadImage(id, file));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<ProductDTO>> getExpiring() {
        return ResponseEntity.ok(productService.getExpiring());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(productService.getDashboard());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> search(@RequestParam("query") String query) {
        return ResponseEntity.ok(productService.search(query));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> filterByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.filterByCategory(category));
    }
}
