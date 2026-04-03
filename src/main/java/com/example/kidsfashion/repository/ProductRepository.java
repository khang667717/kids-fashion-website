package com.example.kidsfashion.repository;

import com.example.kidsfashion.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY SIZE(p.orderItems) DESC")
    List<Product> findBestSellingProducts(Pageable pageable);

    // THÊM: Query lấy top sản phẩm bán chạy kèm số lượng
    @Query("SELECT p, COALESCE(SUM(oi.quantity), 0) as salesCount " +
            "FROM Product p " +
            "LEFT JOIN p.orderItems oi " +
            "GROUP BY p " +
            "ORDER BY salesCount DESC")
    List<Object[]> findTopSellingProductsWithCount(Pageable pageable);
}