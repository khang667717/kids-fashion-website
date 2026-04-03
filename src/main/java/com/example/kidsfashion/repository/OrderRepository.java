package com.example.kidsfashion.repository;

import com.example.kidsfashion.entity.Order;
import com.example.kidsfashion.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;


import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findLatestOrders(Pageable pageable);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenue();

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi WHERE o.user.id = :userId AND oi.product.id = :productId AND o.status = :status")
    boolean existsByUserIdAndProductIdAndStatus(@Param("userId") Long userId,
                                                @Param("productId") Long productId,
                                                @Param("status") String status);
}