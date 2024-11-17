package com.shakhawat.springbatchprocess.controller;

import com.shakhawat.springbatchprocess.service.ProductBatchProcessService;
import com.shakhawat.springbatchprocess.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductBatchProcessService productBatchProcessService;

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getIds() {
        return ResponseEntity.ok(productService.getProductIds());
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetProductRecords() {
        String response = productBatchProcessService.resetProducts();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    public ResponseEntity<String> processProductIds(@RequestBody List<Long> productIds) {
        productService.processProductIds(productIds);
        return ResponseEntity.ok("Products processed and events published.");
    }

    @PostMapping("/batch-process")
    public ResponseEntity<String> batchProcessProductIds(@RequestBody List<Long> productIds) {
        productBatchProcessService.executeProductIds(productIds);
        return ResponseEntity.ok("Products processed and events published.");
    }


}
