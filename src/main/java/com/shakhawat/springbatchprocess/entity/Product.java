package com.shakhawat.springbatchprocess.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    private Long id;

    private String name;

    private String category;

    private double price;

    @Column(name = "is_offer_applied")
    private boolean isOfferApplied;

    @Column(name = "discount_percentage")
    private double discountPercentage;

    @Column(name = "price_after_discount")
    private double priceAfterDiscount;

}
