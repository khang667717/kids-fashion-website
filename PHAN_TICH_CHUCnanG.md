# 📊 PHÂN TÍCH CHỨC NĂNG HỆ THỐNG QUÁN LÝ BÁN QUẦN ÁO TRẺ EM

---

## 1. Quản lý danh mục quần áo trẻ em (CRUD)

### 📌 Trạng thái
* ✅ **Đã triển khai đầy đủ**

### 📁 File liên quan
* **Controller**: `AdminController.java` (lines 154-200)
* **Service**: `CategoryService.java`
* **Repository**: `CategoryRepository.java`
* **Entity**: `Category.java`

### 🌐 API endpoint
* `GET /admin/categories` - Danh sách danh mục
* `GET /admin/categories/new` - Form tạo danh mục
* `POST /admin/categories/save` - Lưu danh mục (Create/Update)
* `GET /admin/categories/edit/{id}` - Form chỉnh sửa danh mục
* `GET /admin/categories/delete/{id}` - Xóa danh mục
* `GET /categories` - Xem danh sách danh mục (user)
* `GET /categories/{id}` - Xem chi tiết danh mục

### ⚙️ Mô tả logic hoạt động
Hệ thống cho phép admin quản lý các danh mục sản phẩm (T-shirt, Dress, Jacket, v.v.). Mỗi danh mục có tên (duy nhất), mô tả, ngày tạo, ngày cập nhật. Danh mục liên kết với nhiều sản phẩm thông qua quan hệ One-To-Many. Admin có thể:
- **Tạo mới**: Nhập tên, mô tả danh mục
- **Sửa**: Cập nhật thông tin danh mục
- **Xóa**: Xóa danh mục (nếu không có sản phẩm)
- **Xem**: Hiển thị danh sách danh mục cho người dùng

### 🔄 Luồng xử lý
```
AdminController → CategoryService → CategoryRepository → Database
```

#### Chi tiết luồng:
1. Admin truy cập `/admin/categories`
2. `AdminController.listCategories()` gọi `CategoryService.getAllCategories()`
3. `CategoryService` gọi repository và ánh xạ Entity → DTO
4. Dữ liệu hiển thị trên Thymeleaf template

### 🧾 Trích dẫn code

**Controller - Tạo danh mục**:
```java
@PostMapping("/categories/save")
public String saveCategory(@ModelAttribute("category") CategoryDTO categoryDTO,
                           RedirectAttributes redirectAttributes) {
    if (categoryDTO.getId() == null) {
        categoryService.createCategory(categoryDTO);
        redirectAttributes.addFlashAttribute("success", "Category created");
    } else {
        categoryService.updateCategory(categoryDTO.getId(), categoryDTO);
    }
    return "redirect:/admin/categories";
}
```

**Service - Tạo danh mục**:
```java
@Transactional
public CategoryDTO createCategory(CategoryDTO categoryDTO) {
    Category category = convertToEntity(categoryDTO);
    Category savedCategory = categoryRepository.save(category);
    return convertToDTO(savedCategory);
}
```

**Entity**:
```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;
}
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**Danh mục là các nhóm sản phẩm như T-shirt, Dress, v.v. Admin có thể Create, Read, Update, Delete danh mục. Mỗi sản phẩm phải thuộc một danh mục. Khi tạo danh mục, hệ thống lưu tên duy nhất, mô tả, và tự động record ngày tạo/sửa. Admin có thể sửa hoặc xóa danh mục từ trang quản lý.**

### 🎯 Vai trò phần code
* **Entity (Category)**: Định nghĩa bảng `categories` trong database
* **Repository (CategoryRepository)**: Giao tiếp với database (JpaRepository tự cung cấp CRUD)
* **Service (CategoryService)**: Xử lý logic, convert Entity ↔ DTO
* **Controller (AdminController)**: Nhận request HTTP, gọi Service, trả response

---

## 2. Quản lý mã giảm giá (Coupon)

### 📌 Trạng thái
* ✅ **Đã triển khai đầy đủ**

### 📁 File liên quan
* **Controller**: `AdminController.java` (coupon endpoints)
* **Service**: `CouponService.java`, `CartService.java`
* **Repository**: `CouponRepository.java`
* **Entity**: `Coupon.java`

### 🌐 API endpoint
* `GET /admin/coupons` - Danh sách mã giảm giá
* `GET /admin/coupons/new` - Form tạo coupon
* `POST /admin/coupons/save` - Lưu coupon
* `GET /admin/coupons/edit/{id}` - Form chỉnh sửa
* `GET /admin/coupons/delete/{id}` - Xóa coupon
* `POST /cart/apply-coupon` - Áp dụng coupon vào giỏ hàng

### ⚙️ Mô tả logic hoạt động
Hệ thống quản lý mã giảm giá với các thông tin: code (duy nhất), % giảm, ngày bắt đầu, ngày kết thúc. Admin có thể tạo, sửa, xóa coupon. Khi khách hàng áp dụng coupon vào giỏ hàng:
- Hệ thống kiểm tra coupon có hợp lệ (trong khoảng thời gian)
- Tính toán chiết khấu = Tổng × (% giảm / 100)
- Lưu coupon code vào session để dùng khi checkout

### 🔄 Luồng xử lý
```
User (View) → CartController.applyCoupon() 
→ CouponService.getCouponByCode() 
→ CartService.applyDiscount() 
→ Session updated
```

#### Chi tiết:
1. User nhập mã coupon, click "Apply Coupon"
2. `CartController` gọi `CouponService.getCouponByCode(code)`
3. Service kiểm tra coupon có tồn tại và còn hiệu lực (startDate <= today <= endDate)
4. `CartService.applyDiscount()` tính % giảm
5. Kết quả lưu vào session

### 🧾 Trích dẫn code

**Entity**:
```java
@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
```

**Service - Áp dụng giảm giá**:
```java
public BigDecimal applyDiscount(BigDecimal total, String couponCode) {
    CouponDTO coupon = getCouponByCode(couponCode);
    BigDecimal discount = total.multiply(coupon.getDiscountPercent().divide(new BigDecimal(100)));
    return total.subtract(discount);
}

public List<CouponDTO> getActiveCoupons() {
    LocalDate today = LocalDate.now();
    return couponRepository.findAll().stream()
            .filter(c -> !c.getStartDate().isAfter(today) && !c.getEndDate().isBefore(today))
            .map(this::convertToDTO)
            .collect(Collectors.toList());
}
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**Coupon là mã giảm giá với % discount, có ngày bắt đầu/kết thúc. Admin tạo coupon từ dashboard. User nhập code vào giỏ hàng, hệ thống kiểm tra coupon còn hiệu lực hay chưa (bằng cách so sánh LocalDate.now() với startDate/endDate). Nếu hợp lệ, tính giá trị discount và cập nhật tổng tiền.**

### 🎯 Vai trò phần code
* **Entity (Coupon)**: Lưu code, %, ngày hiệu lực
* **Repository (CouponRepository)**: Tìm coupon theo code và ngày
* **Service (CouponService)**: Logic validate, calculate discount
* **CartService**: Tích hợp coupon vào giỏ hàng

---

## 3. Quản lý người mua hàng (Customer Management)

### 📌 Trạng thái
* ✅ **Đã triển khai**

### 📁 File liên quan
* **Controller**: `RegisterController.java`, `ProfileController.java`, `AdminController.java`
* **Service**: `UserService.java`, `AddressService.java`
* **Repository**: `UserRepository.java`, `AddressRepository.java`
* **Entity**: `User.java`, `Address.java`

### 🌐 API endpoint
* `GET /register` - Form đăng ký
* `POST /register` - Tạo tài khoản mới
* `GET /login` - Form đăng nhập
* `GET /profile` - Xem hồ sơ cá nhân
* `POST /profile/update` - Cập nhật hồ sơ
* `POST /profile/change-password` - Đổi mật khẩu
* `GET /admin/users` - Danh sách user (admin)
* `GET /admin/users/{id}` - Chi tiết user (admin)

### ⚙️ Mô tả logic hoạt động
Hệ thống quản lý khách hàng với các thông tin:
- **Thông tin đăng nhập**: username (duy nhất), password (mã hóa), email (duy nhất), role (ROLE_ADMIN, ROLE_CUSTOMER)
- **Thông tin cá nhân**: full name, phone, gender, birthday, avatar
- **Địa chỉ**: Lưu one-to-many với User (một user nhiều địa chỉ)

User đăng ký → Controller gọi UserService → Mã hóa password (BCrypt) → Lưu DB. Khi login, Spring Security gọi UserDetailsService để load user, verify password.

### 🔄 Luồng xử lý
```
User Registration:
RegisterController → UserService.createUser() → PasswordEncoder.encode() 
→ UserRepository.save()

User Login:
Spring Security → UserService.loadUserByUsername() 
→ UserRepository.findByUsername() → Build UserDetails
```

### 🧾 Trích dẫn code

**Entity - User**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;  // Mã hóa BCrypt

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role;  // ROLE_ADMIN, ROLE_CUSTOMER

    // Profile info
    private String fullName;
    private String phone;
    private String gender;  // MALE, FEMALE, OTHER
    private LocalDate birthday;
    private String avatarUrl;
}
```

**Service - Tạo user**:
```java
public User createUser(User user) {
    // Mã hóa password trước khi lưu
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepository.save(user);
}

@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
    return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole().replace("ROLE_", ""))
            .build();
}
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**User có thông tin đăng nhập (username, password mã hóa, email, role) và thông tin cá nhân (tên, số điện thoại, giới tính, ngày sinh, ảnh đại diện). Khi đăng ký, password được mã hóa bằng BCrypt rồi lưu vào DB. Khi đăng nhập, Spring Security gọi UserService để load user, kiểm tra password. Admin có thể xem danh sách user.**

### 🎯 Vai trò phần code
* **Entity (User)**: Định nghĩa bảng users
* **Service (UserService)**: Password encoding, load user for Spring Security
* **Repository (UserRepository)**: Query tìm user theo username/email
* **Controller (RegisterController, ProfileController)**: Handle registration, profile update

---

## 4. Quản lý đơn đặt hàng (Order Management)

### 📌 Trạng thái
* ✅ **Đã triển khai đầy đủ**

### 📁 File liên quan
* **Controller**: `OrderController.java`, `AdminController.java`
* **Service**: `OrderService.java`
* **Repository**: `OrderRepository.java`
* **Entity**: `Order.java`, `OrderItem.java`

### 🌐 API endpoint
* `GET /checkout` - Trang checkout
* `POST /checkout/place-order` - Tạo đơn hàng
* `GET /orders` - Danh sách đơn hàng (user)
* `GET /order/{id}` - Chi tiết đơn hàng
* `GET /admin/orders` - Danh sách đơn hàng (admin)
* `GET /admin/orders/{id}` - Chi tiết đơn hàng (admin)
* `POST /admin/orders/update-status` - Cập nhật trạng thái đơn hàng

### ⚙️ Mô tả logic hoạt động
Đơn hàng được tạo từ giỏ hàng:
- **Thông tin đơn hàng**: User, tổng giá tiền, trạng thái (PENDING → PROCESSING → SHIPPED → DELIVERED → CANCELLED), coupon áp dụng, ngày tạo
- **Chi tiết đơn hàng (OrderItem)**: Sản phẩm, số lượng, giá, size
- **Quy trình**:
  1. User checkout
  2. System tính tổng tiền + áp dụng coupon
  3. Tạo Order + list OrderItem
  4. **Trừ stock** của sản phẩm
  5. Xóa giỏ hàng session
  6. Admin cập nhật status sau khi xử lý

### 🔄 Luồng xử lý
```
Cart (Session) → OrderController.placeOrder() 
→ OrderService.createOrder()
  → Create Order entity
  → Create OrderItems from CartItems
  → Update Product stock (stock -= quantity)
  → Save to DB
  → CartService.clearCart()
```

### 🧾 Trích dẫn code

**Entity - Order**:
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal totalPrice;

    private String status;  // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "applied_coupon")
    private String appliedCoupon;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

**Service - Tạo đơn hàng**:
```java
@Transactional
public Order createOrder(Long userId, HttpSession session) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    List<CartItem> cartItems = cartService.getCartItems(session);
    if (cartItems.isEmpty()) {
        throw new RuntimeException("Cart is empty");
    }

    Order order = new Order();
    order.setUser(user);
    order.setStatus("PENDING");

    // Tính tiền và áp dụng coupon
    BigDecimal total = cartService.getTotalPrice(session);
    String couponCode = cartService.getAppliedCoupon(session);
    if (couponCode != null) {
        total = couponService.applyDiscount(total, couponCode);
        order.setAppliedCoupon(couponCode);
    }
    order.setTotalPrice(total);

    // Chuyển CartItem → OrderItem, cập nhật stock
    List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow();

        if (product.getStock() < cartItem.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }
        product.setStock(product.getStock() - cartItem.getQuantity());
        productRepository.save(product);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getPrice());
        orderItem.setSize(cartItem.getSize());

        return orderItem;
    }).collect(Collectors.toList());

    order.setOrderItems(orderItems);
    Order savedOrder = orderRepository.save(order);

    // Xóa giỏ hàng
    cartService.clearCart(session);

    return savedOrder;
}
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**Đơn hàng tạo từ giỏ hàng, bao gồm user, danh sách sản phẩm (với số lượng, giá, size), tổng tiền, mã giảm giá áp dụng, trạng thái. Khi checkout, hệ thống trừ stock sản phẩm, xóa giỏ hàng. Admin có thể xem danh sách đơn hàng và cập nhật trạng thái (PENDING → PROCESSING → SHIPPED → DELIVERED).**

### 🎯 Vai trò phần code
* **Entity (Order, OrderItem)**: Định nghĩa structure đơn hàng
* **Repository (OrderRepository)**: Query orders by user, calculate revenue
* **Service (OrderService)**: Logic tạo order, cập nhật status, cọc thống kê
* **Controller (OrderController, AdminController)**: Handle checkout, view orders

---

## 5. Thống kê (Statistics / Dashboard)

### 📌 Trạng thái
* ✅ **Đã triển khai** (Mock data + real data)

### 📁 File liên quan
* **Controller**: `AdminController.java` (dashboard method)
* **Service**: `OrderService.java`, `ProductService.java`, `ReviewService.java`
* **Repository**: `OrderRepository.java`, `ProductRepository.java`

### 🌐 API endpoint
* `GET /admin/dashboard` - Dashboard với thống kê

### ⚙️ Mô tả logic hoạt động
Dashboard hiển thị các KPI:
- **Tổng sản phẩm**: `ProductService.countAll()` → `SELECT COUNT(*)`
- **Tổng đơn hàng**: `OrderService.countAll()`
- **Tổng doanh thu**: `OrderRepository.getTotalRevenue()` → Tính từ orders với status='DELIVERED'
- **Sản phẩm bán chạy nhất (Top 5)**: `ProductService.getTopSelling(5)` → Query với LEFT JOIN, GROUP BY, COUNT, ORDER BY
- **Đơn hàng gần nhất (Top 5)**: `OrderService.getLatestOrders(5)`
- **Doanh thu hàng tháng (Mock)**: 6 tháng gần nhất (mocked in service)
- **Số lượng đơn hàng hàng tháng (Mock)**: mocked
- **Tổng review**: Count từ Review table
- **Review gần nhất (Top 5)**

### 🔄 Luồng xử lý
```
AdminController.dashboard()
→ ProductService.countAll(), getTotalRevenue()
→ OrderService.countAll(), getTotalRevenue(), getLatestOrders()
→ ProductService.getTopSelling()
→ ReviewService.getAllReviews()
→ View received all data
```

### 🧾 Trích dẫn code

**Controller - Dashboard**:
```java
@GetMapping("/dashboard")
public String dashboard(Model model) {
    model.addAttribute("totalProducts", productService.countAll());
    model.addAttribute("totalOrders", orderService.countAll());
    model.addAttribute("totalRevenue", orderService.getTotalRevenue());
    model.addAttribute("topProducts", productService.getTopSelling(5));
    model.addAttribute("latestOrders", orderService.getLatestOrders(5));

    model.addAttribute("monthlyRevenue", orderService.getMonthlyRevenue());
    model.addAttribute("monthlyOrders", orderService.getMonthlyOrderCounts());
    model.addAttribute("months", List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"));

    List<ReviewDTO> allReviews = reviewService.getAllReviews();
    model.addAttribute("totalReviews", allReviews.size());
    model.addAttribute("recentReviews", allReviews.stream()
            .limit(5)
            .collect(Collectors.toList()));

    return "admin-dashboard";
}
```

**Service - Top Selling Products**:
```java
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

@Transactional(readOnly = true)
public List<Double> getMonthlyRevenue() {
    // Mock data
    return Arrays.asList(1200.0, 1900.0, 3000.0, 2500.0, 4200.0, 3800.0);
}
```

**Repository Query**:
```java
@Query("SELECT COUNT(o) FROM Order o")
long count();

@Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
Double getTotalRevenue();

@Query("SELECT p, COALESCE(SUM(oi.quantity), 0) as salesCount " +
       "FROM Product p " +
       "LEFT JOIN p.orderItems oi " +
       "GROUP BY p " +
       "ORDER BY salesCount DESC")
List<Object[]> findTopSellingProductsWithCount(Pageable pageable);
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**Dashboard hiển thị KPI như tổng sản phẩm, tổng đơn hàng, tổng doanh thu (tính từ orders có status='DELIVERED'). Top 5 sản phẩm bán chạy được lấy bằng cách join Product với OrderItem, group by product, count quantity và sort giảm. Còn có doanh thu/đơn hàng hàng tháng (mock data) và danh sách review gần nhất.**

### 🎯 Vai trò phần code
* **Repository (OrderRepository, ProductRepository)**: Viết JPQL/SQL query để tính toán
* **Service**: Convert query results thành DTO
* **Controller**: Collect all data, add to Model
* **View (Thymeleaf)**: Hiển thị charts/tables

---

## 6. Xem danh sách sản phẩm theo loại (By Category)

### 📌 Trạng thái
* ✅ **Đã triển khai**

### 📁 File liên quan
* **Controller**: `ProductController.java`
* **Service**: `ProductService.java`, `CategoryService.java`
* **Repository**: `ProductRepository.java`, `CategoryRepository.java`
* **Entity**: `Product.java`, `Category.java`

### 🌐 API endpoint
* `GET /products?category={categoryId}` - Danh sách sản phẩm theo category (paginated)
* `GET /category/{slug}` - Danh sách sản phẩm theo slug (custom route)

### ⚙️ Mô tả logic hoạt động
User xem danh sách sản phẩm theo từng loại:
1. Lấy danh sách categories từ `CategoryService`
2. Nếu chọn category → Filter sản phẩm thuộc category đó
3. Hỗ trợ sort (by name, price, date)
4. Hỗ trợ sort direction (asc / desc)
5. Hỗ trợ pagination (page, size)

### 🔄 Luồng xử lý
```
User click category → ProductController.listProductsByCategorySlug()
→ Convert slug to categoryId
→ ProductService.getProductsByCategory(categoryId, page, size, sortBy, direction)
→ ProductRepository.findByCategoryId(categoryId, pageable)
→ View with products, pagination info, categories list
```

### 🧾 Trích dẫn code

**Controller - List by Category**:
```java
@GetMapping("/products")
public String listProducts(@RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "12") int size,
        @RequestParam(value = "category", required = false) Long category,
        @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
        @RequestParam(value = "direction", defaultValue = "asc") String direction,
        Model model) {
    
    Page<ProductDTO> productPage;
    if (category != null) {
        productPage = productService.getProductsByCategory(category, page, size, sortBy, direction);
    } else {
        productPage = productService.getProductsPaginated(page, size, sortBy, direction);
    }
    
    List<CategoryDTO> categories = categoryService.getAllCategories();
    
    model.addAttribute("products", productPage.getContent());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", productPage.getTotalPages());
    model.addAttribute("selectedCategory", category);
    model.addAttribute("categories", categories);
    
    return "product-list";
}
```

**Service - Get by Category**:
```java
@Transactional(readOnly = true)
public Page<ProductDTO> getProductsByCategory(Long categoryId, int page, int size, String sortBy, String direction) {
    Sort sort = direction.equalsIgnoreCase("asc") 
        ? Sort.by(sortBy).ascending() 
        : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    
    return productRepository.findByCategoryId(categoryId, pageable)
            .map(this::convertToDTO);
}
```

**Repository**:
```java
Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**User click vào một category (ví dụ T-shirt), hệ thống lấy categoryId (hoặc slug), gọi ProductService.getProductsByCategory(). Service dùng Repository.findByCategoryId() để query sản phẩm. Hỗ trợ sort (by name, price, date) và direction (asc/desc), pagination (12 sản phẩm/trang).**

### 🎯 Vai trò phần code
* **Repository**: Query `findByCategoryId()` với Pageable for sorting
* **Service**: Convert query result, handle sorting logic
* **Controller**: Receive category param, call service, add to model
* **View**: Render products, pagination buttons, category filters

---

## 7. Hiển thị sản phẩm mới nhất / bán chạy nhất

### 📌 Trạng thái
* ✅ **Đã triển khai**

### 📁 File liên quan
* **Controller**: `ProductController.java`
* **Service**: `ProductService.java`
* **Repository**: `ProductRepository.java`
* **Entity**: `Product.java`, `OrderItem.java`

### 🌐 API endpoint
* `GET /` - Homepage (home method) - hiển thị sản phẩm mới nhất + bán chạy nhất
* Không có dedicated endpoint, nó là phần của homepage

### ⚙️ Mô tả logic hoạt động
Homepage hiển thị 2 danh sách:
1. **Sản phẩm mới nhất (8 items)**: Order by `createdAt DESC` (timestamp tự động khi tạo)
2. **Sản phẩm bán chạy nhất (8 items)**: Count `OrderItem.quantity` per product, order by count DESC

### 🔄 Luồng xử lý
```
User access / 
→ ProductController.home()
→ ProductService.getLatestProducts(8)  // Query: ORDER BY createdAt DESC LIMIT 8
→ ProductService.getBestSellingProducts(8)  // Query: GROUP BY product, COUNT, ORDER BY DESC
→ Add to Model
→ View renders "index" with 2 sections
```

### 🧾 Trích dẫn code

**Controller - Home**:
```java
@GetMapping("/")
public String home(Model model) {
    List<ProductDTO> latestProducts = productService.getLatestProducts(8);
    List<ProductDTO> bestSelling = productService.getBestSellingProducts(8);
    List<CategoryDTO> categories = categoryService.getAllCategories();
    
    model.addAttribute("latestProducts", latestProducts);
    model.addAttribute("bestSelling", bestSelling);
    model.addAttribute("categories", categories);
    
    return "index";
}
```

**Service - Latest & Best Selling**:
```java
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
```

**Repository Queries**:
```java
@Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
List<Product> findLatestProducts(Pageable pageable);

@Query("SELECT p FROM Product p ORDER BY SIZE(p.orderItems) DESC")
List<Product> findBestSellingProducts(Pageable pageable);
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**Sản phẩm mới nhất query Order by createdAt DESC (timestamp tự động). Sản phẩm bán chạy nhất sử dụng SIZE() function để count OrderItem links, order DESC. Cả 2 đều limit 8 để hiển thị trên homepage. Việc count orders không yêu cầu query riêng, repository tự động count via relationship.**

### 🎯 Vai trò phần code
* **Repository**: JPQL queries with ORDER BY, SIZE() function
* **Service**: Call repository, convert to DTO, limit results
* **Controller**: Display both on home page
* **Entity relationship**: Product → OrderItems (for counting best sellers)

---

## 8. Tìm kiếm sản phẩm theo tên

### 📌 Trạng thái
* ✅ **Đã triển khai**

### 📁 File liên quan
* **Controller**: `ProductController.java`
* **Service**: `ProductService.java`
* **Repository**: `ProductRepository.java`
* **Entity**: `Product.java`

### 🌐 API endpoint
* `GET /search?keyword={keyword}&page={page}&size={size}` - Tìm kiếm sản phẩm

### ⚙️ Mô tả logic hoạt động
User nhập keyword vào search box → search sản phẩm:
- Tìm trong **Product.name** hoặc **Product.description**
- Pattern: `... LIKE %keyword%` (case-insensitive)
- Hỗ trợ pagination

### 🔄 Luồng xử lý
```
User enter keyword → Form submit /search?keyword=...
→ ProductController.searchProducts(keyword, page, size)
→ ProductService.searchProducts(keyword, page, size)
→ ProductRepository.search(keyword, pageable)
→ SQL: "SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%"
→ View results with pagination
```

### 🧾 Trích dẫn code

**Controller - Search**:
```java
@GetMapping("/search")
public String searchProducts(@RequestParam("keyword") String keyword,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "12") int size,
        Model model) {
    
    Page<ProductDTO> productPage = productService.searchProducts(keyword, page, size);
    
    model.addAttribute("products", productPage.getContent());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", productPage.getTotalPages());
    model.addAttribute("totalItems", productPage.getTotalElements());
    model.addAttribute("keyword", keyword);
    
    return "product-list";
}
```

**Service - Search**:
```java
@Transactional(readOnly = true)
public Page<ProductDTO> searchProducts(String keyword, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return productRepository.search(keyword, pageable)
            .map(this::convertToDTO);
}
```

**Repository Query**:
```java
@Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
Page<Product> search(@Param("keyword") String keyword, Pageable pageable);
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**User nhập keyword vào search box, hệ thống dùng LIKE operator để tìm trong Product.name hoặc description. Query: `p.name LIKE %keyword% OR p.description LIKE %keyword%` (không phân biệt hoa thường). Kết quả được pagination, 12 sản phẩm/trang.**

### 🎯 Vai trò phần code
* **Repository**: JPQL query with LIKE operator
* **Service**: Call repo, convert result
* **Controller**: Parse keyword param, call service, add to model
* **View**: Display search results, pagination, show keyword

---

## 9. Xem chi tiết sản phẩm (Product Detail)

### 📌 Trạng thái
* ✅ **Đã triển khai**

### 📁 File liên quan
* **Controller**: `ProductController.java`, `ReviewController.java`
* **Service**: `ProductService.java`, `ReviewService.java`
* **Repository**: `ProductRepository.java`, `ReviewRepository.java`
* **Entity**: `Product.java`, `Review.java`

### 🌐 API endpoint
* `GET /product/{id}` - Xem chi tiết sản phẩm + reviews

### ⚙️ Mô tả logic hoạt động
Trang chi tiết sản phẩm hiển thị:
- **Thông tin sản phẩm**: Name, description, price, stock, image, category, sizes (XS, S, M, L, XL)
- **Danh sách reviews**: Hiển thị reviews đã duyệt (status = APPROVED)
- **Review permission check**: Kiểm tra user hiện tại có thể review không (đã mua + đơn hàng đã giao)

### 🔄 Luồng xử lý
```
User click product → ProductController.productDetail(id)
→ ProductService.getProductById(id)
→ ReviewService.getApprovedReviewsForProduct(id)
→ ReviewService.canReview(userId, productId)  // Check if delivered & not reviewed
→ View renders product info + reviews + "Add Review" form (if can review)
```

### 🧾 Trích dẫn code

**Controller - Product Detail**:
```java
@GetMapping("/product/{id}")
public String productDetail(@PathVariable("id") Long id, Model model,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    ProductDTO product = productService.getProductById(id);
    model.addAttribute("product", product);

    // Lấy danh sách review đã duyệt
    model.addAttribute("reviews", reviewService.getApprovedReviewsForProduct(id));

    // Kiểm tra user có thể review không
    boolean canReview = false;
    if (userDetails != null) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElse(null);
        if (user != null) {
            canReview = reviewService.canReview(user.getId(), id);
        }
    }
    model.addAttribute("canReview", canReview);

    return "product-detail";
}
```

**Service - Check Review Permission**:
```java
@Transactional(readOnly = true)
public boolean canReview(Long userId, Long productId) {
    // Kiểm tra: user đã mua sản phẩm này và đơn hàng đã giao?
    boolean hasPurchased = orderRepository.existsByUserIdAndProductIdAndStatus(
            userId, productId, "DELIVERED");

    if (!hasPurchased) return false;

    // Kiểm tra: chưa review?
    boolean alreadyReviewed = reviewRepository.existsByUserAndProduct(
            userRepository.getReferenceById(userId),
            productRepository.getReferenceById(productId));

    return !alreadyReviewed;
}

@Transactional(readOnly = true)
public List<ReviewDTO> getApprovedReviewsForProduct(Long productId) {
    Product product = productRepository.findById(productId)
            .orElseThrow();
    
    return reviewRepository.findByProductAndStatus(product, ReviewStatus.APPROVED).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
}
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**Trang chi tiết sản phẩm hiển thị name, description, price, stock, image, category, sizes. Nếu user đã login, hệ thống kiểm tra user có thể review không bằng cách: (1) check user đã mua sản phẩm và đơn hàng status='DELIVERED', (2) check user chưa review sản phẩm này. Nếu được, hiển thị form "Add Review", ngược lại ẩn form.**

### 🎯 Vai trò phần code
* **Entity (Product, Review)**: Define structure, relationships
* **Repository**: Query reviews by product & status, check purchase history
* **Service (ReviewService)**: Check permission (purchase + delivery + not reviewed)
* **Controller**: Load product, reviews, permission status
* **View**: Render product details, reviews, form (if can review)

---

## 10. Giỏ hàng (Shopping Cart) - Thêm / Xóa / Sửa

### 📌 Trạng thái
* ✅ **Đã triển khai đầy đủ** (Session-based cart)

### 📁 File liên quan
* **Controller**: `CartController.java`
* **Service**: `CartService.java`, `CouponService.java`
* **Entity**: `CartItem.java`
* **Storage**: `HttpSession` (server-side session)

### 🌐 API endpoint
* `GET /cart` - Xem giỏ hàng
* `POST /cart/add` - Thêm sản phẩm vào giỏ (AJAX)
* `POST /cart/update` - Cập nhật số lượng (AJAX)
* `POST /cart/remove` - Xóa sản phẩm (AJAX)
* `POST /cart/apply-coupon` - Áp dụng coupon (AJAX)
* `GET /cart/api/cart/size` - Lấy số lượng items (AJAX)

### ⚙️ Mô tả logic hoạt động
Giỏ hàng lưu trong **HttpSession** (server-side, không cookies):
- **Structure**: `List<CartItem>` trong session key "cart"
- **CartItem**: productId, productName, price, quantity, imageUrl, **size** (new field)
- **Thao tác**:
  1. **Thêm**: Check product exists, stock available, add to cart (nếu đã có size khác nhau, lưu riêng)
  2. **Sửa**: Update quantity (kiểm tra stock)
  3. **Xóa**: Remove item
  4. **Áp dụng coupon**: Validate coupon, calculate discount, save to session
  5. **Tính tổng**: sum(quantity × price) của tất cả items
  6. **Tính tổng sau giảm**: apply coupon discount

### 🔄 Luồng xử lý
```
User click "Add to Cart"
→ CartController.addToCart(productId, quantity, size)
→ CartService.addToCart(session, productId, quantity, size)
  → Validate: product exists, stock >= quantity
  → Validate: size not null & valid
  → Check cart for existing item (by productId + size)
  → If exists: increment quantity
  → Else: create new CartItem
  → Update session
→ Return JSON response (success/error)
→ Frontend update cart UI

User click "Apply Coupon"
→ CartController.applyCoupon(code)
→ CouponService.getCouponByCode(code)  // Check valid & date range
→ CartService.applyDiscount(total, code)  // Calculate discount
→ Save coupon code to session
→ Return updated total
```

### 🧾 Trích dẫn code

**Entity - CartItem**:
```java
public class CartItem {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private String size;  // ✅ NEW FIELD
}
```

**Service - Add to Cart**:
```java
@Transactional(readOnly = true)
public void addToCart(HttpSession session, Long productId, Integer quantity, String size) {
    // Validate quantity
    if (quantity == null || quantity <= 0) {
        throw new RuntimeException("Quantity must be > 0");
    }

    List<CartItem> cart = getCart(session);

    // Validate product exists
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

    // ✅ Validate size
    if (size == null || size.trim().isEmpty()) {
        throw new RuntimeException("Please select a size");
    }

    // Check stock
    if (product.getStock() < quantity) {
        throw new RuntimeException("Insufficient stock");
    }

    // Find or create item (by productId + size)
    Optional<CartItem> existingItem = cart.stream()
            .filter(item -> item.getProductId().equals(productId) &&
                    size.equals(item.getSize()))
            .findFirst();

    if (existingItem.isPresent()) {
        CartItem item = existingItem.get();
        item.setQuantity(item.getQuantity() + quantity);
    } else {
        CartItem newItem = new CartItem();
        newItem.setProductId(product.getId());
        newItem.setProductName(product.getName());
        newItem.setPrice(product.getPrice());
        newItem.setQuantity(quantity);
        newItem.setImageUrl(product.getImageUrl());
        newItem.setSize(size);  // ✅ Set size
        cart.add(newItem);
    }
    session.setAttribute(CART_SESSION_KEY, cart);
}

@Transactional(readOnly = true)
public void updateCartItem(HttpSession session, Long productId, Integer quantity) {
    CartItem item = getCart(session).stream()
            .filter(i -> i.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Item not found in cart"));

    Product product = productRepository.findById(productId)
            .orElseThrow();

    if (quantity > product.getStock()) {
        throw new RuntimeException("Insufficient stock");
    }

    item.setQuantity(quantity);
    session.setAttribute(CART_SESSION_KEY, getCart(session));
}

public void removeFromCart(HttpSession session, Long productId) {
    List<CartItem> cart = getCart(session);
    cart.removeIf(item -> item.getProductId().equals(productId));
    session.setAttribute(CART_SESSION_KEY, cart);
}

public BigDecimal getTotalPrice(HttpSession session) {
    List<CartItem> cart = getCart(session);
    return cart.stream()
            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**Controller - Add to Cart (AJAX)**:
```java
@PostMapping("/add")
@ResponseBody
public ResponseEntity<Map<String, Object>> addToCart(
        @RequestParam("productId") Long productId,
        @RequestParam(value = "quantity", defaultValue = "1") int quantity,
        @RequestParam("size") String size,  // ✅ NEW PARAM
        HttpSession session) {
    
    try {
        cartService.addToCart(session, productId, quantity, size);
        
        int cartSize = cartService.getCartSize(session);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Product added to cart");
        response.put("cartSize", cartSize);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

### 🎤 Cách trình bày khi vấn đáp (2–3 câu)
**Giỏ hàng là List<CartItem> lưu trong HttpSession (server-side). CartItem gồm productId, name, price, quantity, imageUrl, size. Khi thêm vào giỏ, hệ thống validate: product exists, size selected (XS/S/M/L/XL), stock available. Tìm item có cùng productId+size, nếu có thì increments quantity, nếu không thì add new item. Có thể sửa số lượng, xóa item, áp dụng coupon để tính discount.**

### 🎯 Vai trò phần code
* **Entity (CartItem)**: POJO lưu cart items info
* **Service (CartService)**: Validate, add/update/remove logic, calculate totals
* **Controller (CartController)**: Handle AJAX requests, return JSON responses
* **Session (HttpSession)**: Store cart data server-side
* **View (Thymeleaf + JavaScript)**: Display cart, handle AJAX calls

---

## 📊 TỔNG KẾT

### ✅ Hệ thống đã hoàn thành: **10/10 chức năng**

| # | Chức năng | Status | Độ hoàn chỉnh |
|---|-----------|--------|-----------------|
| 1 | Quản lý danh mục (CRUD) | ✅ Đã làm | 100% |
| 2 | Quản lý mã giảm giá | ✅ Đã làm | 100% |
| 3 | Quản lý người mua hàng | ✅ Đã làm | 100% |
| 4 | Quản lý đơn đặt hàng | ✅ Đã làm | 100% |
| 5 | Thống kê Dashboard | ✅ Đã làm | 90% (Mock data cho monthly) |
| 6 | Hiển thị sản phẩm theo loại | ✅ Đã làm | 100% |
| 7 | Sản phẩm mới nhất/bán chạy | ✅ Đã làm | 100% |
| 8 | Tìm kiếm sản phẩm theo tên | ✅ Đã làm | 100% |
| 9 | Xem chi tiết sản phẩm + Review | ✅ Đã làm | 100% |
| 10 | Giỏ hàng (Add/Update/Remove) | ✅ Đã làm | 100% |

### 🏆 Chức năng làm tốt nhất
1. **Giỏ hàng (Cart)**: Hỗ trợ size selection, coupon discount, real-time cart updates (AJAX), session-based storage
2. **Quản lý đơn hàng**: Automatic stock deduction, coupon application, order status tracking (PENDING→DELIVERED)
3. **Tìm kiếm sản phẩm**: LIKE operator trên name & description, pagination, flexible query

### ⚠️ Chức năng còn thiếu / Chưa hoàn chỉnh
1. **Thống kê**: Month ly revenue/orders là mock data (hardcoded Arrays), chưa query real data từ DB
2. **Review status**: Mặc dù có ReviewStatus enum, nhưng tất cả review auto-approved (không có manual approval panel cho admin)
3. **Stock management**: Chỉ trừ stock khi checkout, không có "Reserved stock" cho items đang trong cart (risk: overselling nếu 2 user checkout cùng lúc)
4. **User roles**: Chỉ có ROLE_CUSTOMER & ROLE_ADMIN, chưa phân quyền chi tiết (edit own product, view own order detail, v.v.)

### 💡 Gợi ý cải thiện
1. **Real-time monthly statistics**: Query actual data từ Order table (GROUP BY MONTH, SUM revenue) thay vì hardcoded mock
2. **Advanced search**: Support filter by price range, rating, size availability
3. **Wishlist / Favorite**: Add `FavoriteProduct` entity để user save sản phẩm yêu thích
4. **Product variations**: Tách size/color thành SKU riêng (ProductVariant entity) để quản lý stock chi tiết
5. **Inventory alerts**: Notify admin khi stock < threshold
6. **Order tracking**: Real-time status updates, estimated delivery date
7. **Payment integration**: Xây dựng payment gateway (VNPay, Paypal)
8. **Review moderation**: Admin panel để approve/reject/edit reviews
9. **User roles & permissions**: Implement Spring Security role-based access control
10. **Image optimization**: Compress images on upload, CDN for image serving

---

## ❓ 5 CÂU HỎI GIẢNG VIÊN CÓ THỂ HỎI

### Câu 1: Nêu chi tiết cách hệ thống xử lý khi 2 user cùng checkout sản phẩm giống nhau mà stock chỉ còn 1?

**Trả lời (dễ học thuộc)**:
> Khi user1 click "Place Order", OrderService.createOrder() sẽ duyệt CartItems, kiểm tra stock từ Product table, nếu đủ thì trừ stock và save. Khi user2 checkout cùng lúc, OrderService cũng kiểm tra stock (lúc này stock đã - do user1), nếu không đủ sẽ throw RuntimeException "Insufficient stock" và transaction rollback (không tạo order). Vấn đề: có race condition nếu 2 request xảy ra gần như cùng lúc. Giải pháp: dùng `@Transactional` + `SELECT ... FOR UPDATE` (pessimistic lock) hoặc optimistic lock (version field).

---

### Câu 2: Giải thích cách hệ thống phân biệt khi user thêm cùng sản phẩm nhưng size khác vào giỏ hàng?

**Trả lời (dễ học thuộc)**:
> Khi user click "Add to Cart", form gửi productId, quantity, size (mới thêm vào). CartService.addToCart() tìm item trong cart bằng: `cart.stream().filter(item -> item.getProductId().equals(productId) && item.getSize().equals(size))`. Nếu tìm thấy (cùng product & cùng size) thì increment quantity, nếu không thì create new CartItem với size khác. Ví dụ: thêm áo Nike XS → 1 item, sau đó thêm áo Nike M → 2 items riêng biệt trong giỏ theo size.

---

### Câu 3: Coupon có date range (startDate, endDate). Nêu cách hệ thống kiểm tra coupon còn hiệu lực?

**Trả lời (dễ học thuộc)**:
> CouponService.getActiveCoupons() dùng LocalDate.now() để lấy ngày hôm nay, sau đó filter các coupon thỏa: `!startDate.isAfter(today) && !endDate.isBefore(today)` (tức startDate <= today <= endDate). Khi user apply coupon, CouponService.getCouponByCode() cũng gọi repository query: `findByCodeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(code, today, today)` để tìm coupon validDate. Nếu không tìm thấy → throw "Invalid or expired coupon".

---

### Câu 4: ProductRepository có 2 query để tìm "bán chạy nhất": findBestSellingProducts() vs findTopSellingProductsWithCount(). Khác nhau thế nào?

**Trả lời (dễ học thuộc)**:
> `findBestSellingProducts()` dùng `SIZE(p.orderItems)` - đơn giản, đếm số OrderItem linked. `findTopSellingProductsWithCount()` dùng `LEFT JOIN p.orderItems oi` + `GROUP BY p` + `SUM(oi.quantity)` - chính xác hơn vì tính tổng quantity (khối lượng bán). Ví dụ: Product A có 1 order của 100 chiếc vs Product B có 10 orders của 5 chiếc mỗi cái (= 50 total). SIZE() cho kết quả A >B, nhưng actual A bán chạy hơn. Nên dùng findTopSellingProductsWithCount() để accurate.

---

### Câu 5: Khi user checkout, hệ thống thực hiện những gì? Liệt kê các bước transaction?

**Trả lời (dễ học thuộc)**:
> OrderService.createOrder() bọc trong @Transactional:
> 1. Lấy User từ DB
> 2. Lấy CartItems từ session
> 3. Lấy Address theo addressId user chọn, kiểm tra ownership, rồi copy snapshot (Tên, SĐT, Địa chỉ) vào entity Order
> 4. Tạo Order object, set user, shipping info, status="PENDING"
> 5. Tính tổng tiền (getTotalPrice)
> 6. Nếu có coupon, validate & apply discount
> 7. Duyệt CartItems, create OrderItems, **trừ Product.stock**
> 8. Save Order + OrderItems vào DB (cascade)
> 9. Clear session cart
> Nếu lỗi ở bất kỳ bước nào → rollback toàn bộ (không trừ stock, không xóa cart, không tạo order). Điều này đảm bảo data consistency.

---

## 🎓 KẾT LUẬN

Hệ thống **Kids Fashion Store** đã implement đầy đủ **10/10 chức năng** với Spring Boot 3.5 + JPA/Hibernate + Thymeleaf + Spring Security. 

### Điểm mạnh:
- ✅ Clean architecture (Controller → Service → Repository)
- ✅ Transaction management (@Transactional)
- ✅ Entity relationships (One-To-Many, Many-To-One)
- ✅ JPQL queries (custom queries in repositories)
- ✅ Session-based cart with coupon support
- ✅ Password encoding (BCrypt)
- ✅ Stock management on checkout

### Các điểm cần cải thiện:
- ⚠️ Race conditions (concurrent checkout)
- ⚠️ Mock data cho statistics
- ⚠️ Auto-approve reviews (no manual moderation)
- ⚠️ No payment gateway integration

---

**Ngày phân tích**: 2026-04-06  
**Phạm vi**: 10 required functionalities  
**Mã nguồn**: Spring Boot 3.5 + MySQL  
**Sinh viên trả lời**: [Tên sinh viên cần điền vào]
