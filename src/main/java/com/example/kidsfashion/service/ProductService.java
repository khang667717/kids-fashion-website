package com.example.kidsfashion.service;

import com.example.kidsfashion.dto.ProductDTO;
import com.example.kidsfashion.dto.ProductSalesDTO;
import com.example.kidsfashion.entity.Category;
import com.example.kidsfashion.entity.Product;
import com.example.kidsfashion.entity.SizeEnum;
import com.example.kidsfashion.repository.CategoryRepository;
import com.example.kidsfashion.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ReviewService reviewService;

    @Value("${upload.path}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsPaginated(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByCategoryId(categoryId, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.search(keyword, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        System.out.println("===== CREATE PRODUCT DEBUG =====");
        System.out.println("Product name: " + productDTO.getName());

        Product product = convertToEntity(productDTO);
        if (imageFile != null && !imageFile.isEmpty()) {
            validateImageFile(imageFile);
            String imageUrl = saveImage(imageFile, null); // Không có ảnh cũ để so sánh
            product.setImageUrl(imageUrl);
        }
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        System.out.println("Product created with ID: " + savedProduct.getId());
        System.out.println("=================================");

        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        System.out.println("===== UPDATE PRODUCT DEBUG =====");
        System.out.println("Product ID: " + id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        String oldImageUrl = existingProduct.getImageUrl();
        System.out.println("Old image URL from DB: " + oldImageUrl);

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());

        // Cập nhật sizes
        if (productDTO.getSizes() != null) {
            existingProduct.setSizes(productDTO.getSizes().stream()
                    .map(SizeEnum::valueOf)
                    .collect(Collectors.toSet()));
        }

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            existingProduct.setCategory(category);
        }

        // ✅ XỬ LÝ UPLOAD ẢNH MỚI - ĐÃ SỬA LỖI
        if (imageFile != null && !imageFile.isEmpty()) {
            System.out.println("New image uploaded: " + imageFile.getOriginalFilename());
            System.out.println("File size: " + imageFile.getSize() + " bytes");

            validateImageFile(imageFile);

            // Kiểm tra xem ảnh mới có giống ảnh cũ không (dựa vào checksum)
            boolean isSameImage = false;
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                isSameImage = isSameImageContent(imageFile, oldImageUrl);
                System.out.println("Is same image as old: " + isSameImage);
            }

            if (!isSameImage) {
                // Lưu ảnh mới TRƯỚC
                String newImageUrl = saveImage(imageFile, oldImageUrl);
                existingProduct.setImageUrl(newImageUrl);
                System.out.println("New image URL set to: " + newImageUrl);
            } else {
                System.out.println("Image is identical to old one, keeping old image");
                // Giữ nguyên ảnh cũ, không làm gì cả
            }
        } else {
            System.out.println("No new image uploaded, keeping old image");
        }

        Product updatedProduct = productRepository.save(existingProduct);
        System.out.println("Product saved, final image URL: " + updatedProduct.getImageUrl());
        System.out.println("=================================");

        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        System.out.println("===== DELETE PRODUCT DEBUG =====");
        System.out.println("Product ID: " + id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        String imageUrl = product.getImageUrl();

        // Xóa product trước
        productRepository.delete(product);
        System.out.println("Product deleted from database");

        // Sau đó xóa ảnh (nếu có)
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.contains("no-image") && !imageUrl.contains("placeholder")) {
            System.out.println("Deleting image: " + imageUrl);
            boolean deleted = deleteImageFile(imageUrl);
            System.out.println("Image deleted: " + deleted);
        }

        System.out.println("=================================");
    }

    /**
     * Kiểm tra xem file upload có giống với ảnh cũ không (dùng MD5 checksum)
     */
    private boolean isSameImageContent(MultipartFile newFile, String oldImageUrl) {
        try {
            // Tính checksum của file mới
            String newChecksum = calculateMD5(newFile.getBytes());

            // Đọc file cũ và tính checksum
            if (oldImageUrl != null && oldImageUrl.startsWith("/uploads/")) {
                String filename = oldImageUrl.substring("/uploads/".length());
                Path oldFilePath = Paths.get(uploadDir).resolve(filename);

                if (Files.exists(oldFilePath)) {
                    byte[] oldBytes = Files.readAllBytes(oldFilePath);
                    String oldChecksum = calculateMD5(oldBytes);

                    return newChecksum.equals(oldChecksum);
                }
            }
        } catch (Exception e) {
            System.err.println("Error comparing images: " + e.getMessage());
        }
        return false;
    }

    /**
     * Tính MD5 checksum của mảng byte
     */
    private String calculateMD5(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Xóa file ảnh vật lý
     */
    private boolean deleteImageFile(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                String filename = imageUrl.substring("/uploads/".length());
                Path uploadPath = Paths.get(uploadDir);
                Path filePath = uploadPath.resolve(filename).normalize();

                System.out.println("Looking for file at: " + filePath.toAbsolutePath());
                System.out.println("File exists: " + Files.exists(filePath));

                if (Files.exists(filePath)) {
                    boolean deleted = Files.deleteIfExists(filePath);
                    System.out.println("File deleted: " + deleted);
                    return deleted;
                } else {
                    System.out.println("File does not exist, skipping delete");
                    return false;
                }
            }
        } catch (IOException e) {
            System.err.println("Error deleting image file: " + e.getMessage());
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findLatestProducts(pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getBestSellingProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findBestSellingProducts(pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public List<ProductSalesDTO> getTopSelling(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = productRepository.findTopSellingProductsWithCount(pageable);
        List<ProductSalesDTO> topProducts = new ArrayList<>();

        for (Object[] result : results) {
            Product product = (Product) result[0];
            Long salesCount = ((Number) result[1]).longValue();

            ProductSalesDTO dto = new ProductSalesDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setImageUrl(product.getImageUrl());
            dto.setSalesCount(salesCount.intValue());

            topProducts.add(dto);
        }

        return topProducts;
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed. Received: " + contentType);
        }

        // Kiểm tra kích thước file (tối đa 5MB)
        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size too large. Maximum 5MB allowed.");
        }
    }

    private String saveImage(MultipartFile imageFile, String oldImageUrl) throws IOException {
        System.out.println("===== SAVE IMAGE DEBUG =====");
        System.out.println("uploadDir from properties: " + uploadDir);

        // Tạo thư mục upload nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        System.out.println("Absolute path: " + uploadPath);
        System.out.println("Directory exists: " + Files.exists(uploadPath));

        if (!Files.exists(uploadPath)) {
            System.out.println("Creating directory: " + uploadPath);
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file duy nhất nhưng có tổ chức
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Tạo tên file dựa trên thời gian và nội dung
        String baseName = "product_" + System.currentTimeMillis();
        String filename = baseName + fileExtension;
        Path filePath = uploadPath.resolve(filename);

        System.out.println("Saving to: " + filePath);

        // Kiểm tra nếu file đã tồn tại
        int counter = 1;
        while (Files.exists(filePath)) {
            filename = baseName + "_" + counter + fileExtension;
            filePath = uploadPath.resolve(filename);
            counter++;
        }

        // Copy file với tùy chọn REPLACE_EXISTING
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("File saved, exists: " + Files.exists(filePath));
        System.out.println("File size after save: " + Files.size(filePath) + " bytes");

        String imageUrl = "/uploads/" + filename;
        System.out.println("Generated URL: " + imageUrl);

        // Xóa ảnh cũ nếu có (và khác ảnh mới)
        if (oldImageUrl != null && !oldImageUrl.isEmpty() && !oldImageUrl.equals(imageUrl)) {
            deleteImageFile(oldImageUrl);
        }

        System.out.println("============================");

        return imageUrl;
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getSizes() != null) {
            dto.setSizes(product.getSizes().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet()));
        }
        // Thêm rating - xử lý an toàn
        try {
            if (reviewService != null) {
                Double avgRating = reviewService.getAverageRating(product.getId());
                dto.setAverageRating(avgRating != null ? avgRating : 0.0);

                Long count = reviewService.getReviewCount(product.getId());
                dto.setReviewCount(count != null ? count : 0L);
            } else {
                dto.setAverageRating(0.0);
                dto.setReviewCount(0L);
            }
        } catch (Exception e) {
            System.err.println("Error getting ratings for product " + product.getId() + ": " + e.getMessage());
            dto.setAverageRating(0.0);
            dto.setReviewCount(0L);
        }
        return dto;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = modelMapper.map(dto, Product.class);
        if (dto.getSizes() != null) {
            product.setSizes(dto.getSizes().stream()
                    .map(SizeEnum::valueOf)
                    .collect(Collectors.toSet()));
        }
        return product;
    }
}