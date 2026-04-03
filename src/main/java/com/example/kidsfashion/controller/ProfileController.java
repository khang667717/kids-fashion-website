package com.example.kidsfashion.controller;

import com.example.kidsfashion.dto.AddressDTO;
import com.example.kidsfashion.dto.ChangePasswordDTO;
import com.example.kidsfashion.dto.ProfileDTO;
import com.example.kidsfashion.entity.Address;
import com.example.kidsfashion.entity.User;
import com.example.kidsfashion.service.AddressService;
import com.example.kidsfashion.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final AddressService addressService;

    /**
     * Helper: lấy User entity từ Authentication.
     */
    private User getCurrentUser(Authentication auth) {
        return userService.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ===========================
    //  TAB 1: HỒ SƠ CÁ NHÂN
    // ===========================

    @GetMapping
    public String profilePage(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "profile");
        model.addAttribute("view", "profile");
        return "layout";
    }

    @PostMapping("/update")
    public String updateProfile(Authentication auth,
                                @ModelAttribute ProfileDTO profileDTO,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);
        user.setFullName(profileDTO.getFullName());
        user.setEmail(profileDTO.getEmail());
        user.setPhone(profileDTO.getPhone());
        user.setGender(profileDTO.getGender());
        user.setBirthday(profileDTO.getBirthday());
        userService.updateProfile(user);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    // ===========================
    //  TAB 2: ĐỔI MẬT KHẨU
    // ===========================

    @GetMapping("/password")
    public String passwordPage(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", "password");
        model.addAttribute("view", "profile");
        return "layout";
    }

    @PostMapping("/password")
    public String changePassword(Authentication auth,
                                 @ModelAttribute ChangePasswordDTO dto,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        // Validate confirm password
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "New password and confirmation do not match!");
            return "redirect:/profile/password";
        }

        // Validate password length
        if (dto.getNewPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "New password must be at least 6 characters!");
            return "redirect:/profile/password";
        }

        boolean result = userService.changePassword(user, dto.getCurrentPassword(), dto.getNewPassword());
        if (result) {
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect!");
        }
        return "redirect:/profile/password";
    }

    // ===========================
    //  TAB 3: QUẢN LÝ ĐỊA CHỈ
    // ===========================

    @GetMapping("/addresses")
    public String addressesPage(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        List<Address> addresses = addressService.getAddressesByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("activeTab", "addresses");
        model.addAttribute("view", "profile");
        return "layout";
    }

    @GetMapping("/addresses/new")
    public String newAddressForm(Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("address", new AddressDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("view", "profile-address-form");
        return "layout";
    }

    @GetMapping("/addresses/edit/{id}")
    public String editAddressForm(@PathVariable("id") Long id,
                                  Authentication auth, Model model) {
        User user = getCurrentUser(auth);
        Address address = addressService.getAddressById(id, user);

        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setAddressLine(address.getAddressLine());
        dto.setWard(address.getWard());
        dto.setDistrict(address.getDistrict());
        dto.setCity(address.getCity());
        dto.setLabel(address.getLabel());
        dto.setIsDefault(address.getIsDefault());

        model.addAttribute("user", user);
        model.addAttribute("address", dto);
        model.addAttribute("isEdit", true);
        model.addAttribute("view", "profile-address-form");
        return "layout";
    }

    @PostMapping("/addresses/save")
    public String saveAddress(Authentication auth,
                              @ModelAttribute AddressDTO addressDTO,
                              RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);

        if (addressDTO.getId() != null) {
            addressService.updateAddress(addressDTO.getId(), user, addressDTO);
            redirectAttributes.addFlashAttribute("success", "Address updated successfully!");
        } else {
            addressService.createAddress(user, addressDTO);
            redirectAttributes.addFlashAttribute("success", "Address added successfully!");
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long id,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);
        addressService.deleteAddress(id, user);
        redirectAttributes.addFlashAttribute("success", "Address deleted successfully!");
        return "redirect:/profile/addresses";
    }

    @PostMapping("/addresses/set-default/{id}")
    public String setDefaultAddress(@PathVariable("id") Long id,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(auth);
        addressService.setDefaultAddress(id, user);
        redirectAttributes.addFlashAttribute("success", "Default address updated!");
        return "redirect:/profile/addresses";
    }
}
