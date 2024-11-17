package com.shakhawat.springbatchprocess.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shakhawat.springbatchprocess.entity.Product;
import com.shakhawat.springbatchprocess.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<Object, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${product.discount.update.topic}")
    private String topicName;

    public String resetProducts() {
        productRepository.findAll().forEach(product -> {
            product.setOfferApplied(false);
            product.setPriceAfterDiscount(product.getPrice());
            product.setDiscountPercentage(0);
            productRepository.save(product);
        });
        return "Products reset successfully";
    }

    public void processProductIds(List<Long> productIds) {
        productIds.parallelStream().forEach(this::updateAndPublishProduct);
    }

    private void updateAndPublishProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new RuntimeException("Product not found")
        );

        // Update the product
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
        double discountPercentage = (price > 1000) ? 10 : (price > 800 ? 5 : 0);

        product.setOfferApplied(false);

        if (discountPercentage > 0) {
            double priceAfterDiscount = price - (price * discountPercentage / 100);
            product.setOfferApplied(true);
            product.setPriceAfterDiscount(priceAfterDiscount);
            product.setDiscountPercentage(discountPercentage);
        }
    }

    // Implement the logic to publish the product event to Kafka
    private void publishProductEvent(Product product) {
        try {
            String productJson = objectMapper.writeValueAsString(product);
            kafkaTemplate.send(topicName, productJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert product to JSON", e);
        }
    }

    public List<Long> getProductIds(){
        return productRepository.findAll()
                .stream()
                .map(Product::getId)
                .collect(Collectors.toList());
    }

}
