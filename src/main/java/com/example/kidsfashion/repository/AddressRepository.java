package com.example.kidsfashion.repository;

import com.example.kidsfashion.entity.Address;
import com.example.kidsfashion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Lấy tất cả địa chỉ của user, default lên đầu, sau đó theo thời gian tạo mới nhất.
     */
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);

    /**
     * Tìm địa chỉ theo ID và user (kiểm tra quyền sở hữu).
     */
    Optional<Address> findByIdAndUser(Long id, User user);

    /**
     * Đếm số địa chỉ của user.
     */
    long countByUser(User user);
}
