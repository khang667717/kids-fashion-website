package com.example.kidsfashion.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSalesDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private Integer salesCount;  // Số lượng đã bán
}