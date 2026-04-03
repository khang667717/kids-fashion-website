package com.example.kidsfashion.service;

import com.example.kidsfashion.dto.AddressDTO;
import com.example.kidsfashion.entity.Address;
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    /**
     * Lấy tất cả địa chỉ của user (default lên đầu).
     */
    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
    }

    /**
     * Lấy địa chỉ theo ID (kiểm tra ownership).
     */
    public Address getAddressById(Long id, User user) {
        return addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Address not found or access denied"));
    }

    /**
     * Tạo địa chỉ mới. Nếu là địa chỉ đầu tiên → auto set default.
     */
    @Transactional
    public Address createAddress(User user, AddressDTO dto) {
        Address address = new Address();
        mapDtoToEntity(dto, address);
        address.setUser(user);

        // Nếu là địa chỉ đầu tiên → auto default
        long count = addressRepository.countByUser(user);
        if (count == 0) {
            address.setIsDefault(true);
        }

        // Nếu đặt làm default → bỏ default cái cũ
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefaultAddress(user);
        }

        return addressRepository.save(address);
    }

    /**
     * Cập nhật địa chỉ.
     */
    @Transactional
    public Address updateAddress(Long id, User user, AddressDTO dto) {
        Address address = getAddressById(id, user);
        mapDtoToEntity(dto, address);

        // Nếu đặt làm default → bỏ default cái cũ
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearDefaultAddress(user);
        }

        return addressRepository.save(address);
    }

    /**
     * Xoá địa chỉ.
     */
    @Transactional
    public void deleteAddress(Long id, User user) {
        Address address = getAddressById(id, user);

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        // Nếu xoá địa chỉ default → set cái đầu tiên còn lại làm default
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsDefault(true);
                addressRepository.save(remaining.get(0));
            }
        }
    }

    /**
     * Đặt địa chỉ làm mặc định.
     */
    @Transactional
    public void setDefaultAddress(Long id, User user) {
        Address address = getAddressById(id, user);
        clearDefaultAddress(user);
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    /**
     * Bỏ tất cả default của user.
     */
    private void clearDefaultAddress(User user) {
        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        for (Address addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }

    /**
     * Map DTO → Entity.
     */
    private void mapDtoToEntity(AddressDTO dto, Address address) {
        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setAddressLine(dto.getAddressLine());
        address.setWard(dto.getWard());
        address.setDistrict(dto.getDistrict());
        address.setCity(dto.getCity());
        address.setLabel(dto.getLabel() != null ? dto.getLabel() : "HOME");
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
    }
}
