package com.shakhawat.springbatchprocess.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhawat.springbatchprocess.entity.Product;
import com.shakhawat.springbatchprocess.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductBatchProcessService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<Object, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;
    private final ExecutorService threadPool;

    public ProductBatchProcessService(
            ProductRepository productRepository,
            KafkaTemplate<Object, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${product.discount.update.topic}") String topicName
    ) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
        this.threadPool = Executors.newFixedThreadPool(6);
    }

    public String resetProducts() {
        try {

            List<Product> products = productRepository.findAll();

            // Reset each product
            List<Product> resetProducts = products.stream()
                    .peek(product -> {
                        product.setOfferApplied(false);
                        product.setPriceAfterDiscount(product.getPrice());
                        product.setDiscountPercentage(0);
                    })
                    .collect(Collectors.toList());

            // Save the reset products back to the repository
            productRepository.saveAll(resetProducts);

            // Optionally, send a message to a Kafka topic about the reset operation
            String message = objectMapper.writeValueAsString(resetProducts);
            kafkaTemplate.send(topicName, message);

            return "Products have been reset successfully.";
        } catch (JsonProcessingException e) {
            return "Failed to process product data.";
        } catch (Exception e) {
            return "An error occurred while resetting products.";
        }
    }

    public void executeProductIds(List<Long> productIds) {
        List<List<Long>> batches = divideProductIdsIntoBatches(productIds, 50);

        List<CompletableFuture<Void>> processingFutures = batches.stream()
                .map(this::processProductIdsAsync)
                .toList();

        CompletableFuture.allOf(processingFutures.toArray(new CompletableFuture[0])).join();
    }

    private List<List<Long>> divideProductIdsIntoBatches(List<Long> productIds, int batchSize) {
        int totalProductCount = productIds.size();
        int batchCount = (totalProductCount + batchSize - 1) / batchSize;

        List<List<Long>> productBatches = new ArrayList<>();

        for (int batchIndex = 0; batchIndex < batchCount; batchIndex++) {
            int startIndex = batchIndex * batchSize;
            int endIndex = Math.min(totalProductCount, (batchIndex + 1) * batchSize);
            productBatches.add(productIds.subList(startIndex, endIndex));
        }

        return productBatches;
    }

    private CompletableFuture<Void> processProductIdsAsync(List<Long> batch) {
        return CompletableFuture.runAsync(() -> processProductIds(batch), threadPool);
    }

    private void processProductIds(List<Long> batch) {
        log.info("Processing batch {} by thread {}", batch, Thread.currentThread().getName());
        batch.forEach(this::fetchUpdateAndPublish);
    }


    private void fetchUpdateAndPublish(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product ID does not exist in the system")
                );

        //update discount price
        updateDiscountedPrice(product);

        if(product.isOfferApplied()){
            //save to DB
            productRepository.save(product);

            //kafka events
            publishProductEvent(product);
        }
    }

    private void updateDiscountedPrice(Product product) {

        double price = product.getPrice();

        int discountPercentage = (price > 1000) ? 10 : (price > 800 ? 5 : 0);

        product.setOfferApplied(false);

        if (discountPercentage > 0) {
            double priceAfterDiscount = price - (price * discountPercentage / 100);
            product.setOfferApplied(true);
            product.setPriceAfterDiscount(priceAfterDiscount);
            product.setDiscountPercentage(discountPercentage);
        }
    }

    private void publishProductEvent(Product product) {
        try {
            String productJson = objectMapper.writeValueAsString(product);
            kafkaTemplate.send(topicName, productJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert product to JSON", e);
        }
    }

}
