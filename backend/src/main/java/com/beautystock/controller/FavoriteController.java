package com.beautystock.controller;

import com.beautystock.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final ProductService productService;

    public FavoriteController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(@PathVariable Long productId) {
        productService.addFavorite(productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        productService.removeFavorite(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/is-favorite")
    public ResponseEntity<Map<String, Boolean>> isFavorite(@PathVariable Long productId) {
        return ResponseEntity.ok(Map.of("isFavorite", productService.isFavorite(productId)));
    }
}