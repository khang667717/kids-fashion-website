package com.example.kidsfashion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Set<String> sizes;   // Thêm trường sizes

    public String getCategorySlug() {
        if (this.categoryName == null) return "";
        String temp = java.text.Normalizer.normalize(this.categoryName, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .toLowerCase()
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("-$", "");
    }
    private Double averageRating;   // điểm trung bình
    private Long reviewCount;       // số lượng đánh giá

    public Set<String> getSizes() {
        if (sizes == null) return new java.util.LinkedHashSet<>();
        java.util.List<String> order = java.util.Arrays.asList("S", "M", "L", "XL", "XXL");
        java.util.List<String> sorted = new java.util.ArrayList<>(sizes);
        sorted.sort(java.util.Comparator.comparingInt(size -> {
            int index = order.indexOf(size);
            return index == -1 ? 99 : index;
        }));
        return new java.util.LinkedHashSet<>(sorted);
    }
}