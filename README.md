# Kids Fashion Store

> **Website thương mại điện tử bán quần áo thời trang trẻ em**, được xây dựng bằng Spring Boot với đầy đủ tính năng từ trang khách hàng đến bảng điều khiển quản trị.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen?style=flat-square&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=flat-square&logo=mysql)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=flat-square&logo=thymeleaf)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=flat-square&logo=apachemaven)

---

## 📋 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Tính năng](#tính-năng)
- [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Cài đặt và chạy](#cài-đặt-và-chạy)
- [Cấu trúc project](#cấu-trúc-project)
- [Database Schema](#database-schema)
- [API & Routes](#api--routes)
- [Tài khoản mặc định](#tài-khoản-mặc-định)

---

## 🎯 Giới thiệu

**Kids Fashion Store** là một ứng dụng web thương mại điện tử hoàn chỉnh dành cho cửa hàng quần áo trẻ em, được phát triển trong khuôn khổ môn học **Lập Trình Java Nâng Cao**. Ứng dụng cung cấp đầy đủ luồng mua sắm cho khách hàng và bộ công cụ quản trị mạnh mẽ cho admin.

---

## ✨ Tính năng

### 🛍️ Dành cho Khách hàng

| Tính năng | Mô tả |
|---|---|
| **Trang chủ** | Hiển thị sản phẩm mới nhất & bán chạy nhất |
| **Duyệt sản phẩm** | Lọc theo danh mục, sắp xếp, phân trang |
| **Tìm kiếm** | Tìm kiếm sản phẩm theo từ khóa |
| **Chi tiết sản phẩm** | Xem ảnh, mô tả, chọn size (S, M, L, XL), đánh giá |
| **Giỏ hàng** | Thêm/xóa/cập nhật số lượng sản phẩm theo từng size |
| **Mã giảm giá (Coupon)** | Áp dụng và xóa mã giảm giá |
| **Checkout** | Đặt hàng và trừ tồn kho tự động |
| **Lịch sử đơn hàng** | Xem và theo dõi trạng thái đơn hàng |
| **Đánh giá sản phẩm** | Viết review (chỉ với sản phẩm đã mua và nhận hàng) |
| **Hồ sơ cá nhân** | Cập nhật thông tin, đổi mật khẩu, quản lý địa chỉ |
| **Đăng ký / Đăng nhập** | Form đăng ký tài khoản và xác thực |

### ⚙️ Dành cho Admin

| Tính năng | Mô tả |
|---|---|
| **Dashboard** | Thống kê tổng quan: doanh thu, đơn hàng, sản phẩm, biểu đồ |
| **Quản lý Sản phẩm** | CRUD đầy đủ, upload ảnh, quản lý size & tồn kho |
| **Quản lý Danh mục** | Thêm, sửa, xóa danh mục sản phẩm |
| **Quản lý Đơn hàng** | Xem chi tiết & cập nhật trạng thái đơn hàng |
| **Quản lý Coupon** | Tạo và quản lý mã giảm giá có thời hạn |
| **Quản lý Người dùng** | Xem, tạo, sửa, xóa tài khoản |
| **Quản lý Đánh giá** | Duyệt, từ chối, xóa review của khách hàng |

---

## 🏗️ Kiến trúc hệ thống

Ứng dụng tuân theo mô hình **MVC (Model - View - Controller)** truyền thống của Spring Boot:

```
Browser / Client
      │
      ▼
┌─────────────────┐
│   Controller    │  ← Xử lý HTTP request, điều hướng luồng
│  (Spring MVC)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Service      │  ← Business logic, xử lý nghiệp vụ
│    Layer        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │  ← Truy vấn database qua JPA
│  (Spring Data)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│    Database     │  ← MySQL
│    (MySQL)      │
└─────────────────┘
         │ Data
         ▼
┌─────────────────┐
│  Thymeleaf      │  ← Server-side rendering → HTML
│  Templates      │
└─────────────────┘
```

### Các lớp chính

- **Entity**: Ánh xạ trực tiếp tới bảng Database (`@Entity`, JPA)
- **DTO**: Đối tượng truyền dữ liệu giữa Controller và View
- **Repository**: Interface kế thừa `JpaRepository`, truy vấn JPQL
- **Service**: Business logic, xử lý nghiệp vụ, ánh xạ Entity ↔ DTO
- **Controller**: Nhận request, gọi Service, trả dữ liệu về View
- **Config**: Cấu hình Security, ModelMapper, WebMvc

---

## 🛠️ Công nghệ sử dụng

### Backend
| Thư viện | Phiên bản | Mục đích |
|---|---|---|
| **Java** | 17 | Ngôn ngữ lập trình chính |
| **Spring Boot** | 3.5.11 | Framework chính |
| **Spring Security** | 6.x | Xác thực & phân quyền (BCrypt) |
| **Spring Data JPA** | 3.x | ORM, truy vấn database |
| **Thymeleaf** | 3.x | Template engine (server-side rendering) |
| **Thymeleaf Security** | 3.x | Tích hợp Spring Security vào template |
| **Lombok** | Latest | Giảm boilerplate code |
| **ModelMapper** | 3.2.0 | Ánh xạ Entity ↔ DTO tự động |
| **Spring Validation** | 3.x | Validation dữ liệu đầu vào |

### Database
| Thư viện | Mục đích |
|---|---|
| **MySQL 8.x** | RDBMS chính |
| **MySQL Connector/J** | Driver kết nối Java ↔ MySQL |

### Build & Dev Tools
| Công cụ | Mục đích |
|---|---|
| **Maven** | Build tool, quản lý dependency |
| **Spring Boot DevTools** | Hot reload khi phát triển |

---

## 🚀 Cài đặt và chạy

### Yêu cầu hệ thống

- **JDK 17+**
- **Maven 3.6+**
- **MySQL Server 8.x**

### Bước 1: Clone project

```bash
git clone <repository-url>
cd kids-fashion-store
```

### Bước 2: Tạo Database

Kết nối MySQL và tạo database:

```sql
CREATE DATABASE kids_fashion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 3: Cấu hình kết nối Database

Mở file `src/main/resources/application.properties` và chỉnh sửa thông tin:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/kids_fashion_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Bước 4: Cấu hình thư mục Upload ảnh

```properties
# Đường dẫn tuyệt đối đến thư mục lưu ảnh sản phẩm
upload.path=/path/to/your/uploads/
```

> Thư mục `uploads/` sẽ được tạo tự động nếu chưa tồn tại.

### Bước 5: Chạy ứng dụng

```bash
# Chạy với Maven Wrapper (khuyến nghị)
./mvnw spring-boot:run

# Hoặc với Maven đã cài sẵn
mvn spring-boot:run
```

Ứng dụng sẽ khởi động tại: **http://localhost:8080**

> **Lưu ý:** Khi chạy lần đầu, Spring Boot sẽ tự động tạo schema (Hibernate DDL) và dữ liệu mẫu từ `data.sql` được nạp vào database.

---

## 📁 Cấu trúc project

```
kids-fashion-store/
├── src/
│   ├── main/
│   │   ├── java/com/example/kidsfashion/
│   │   │   ├── KidsFashionApplication.java      # Entry point
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # Spring Security (xác thực, phân quyền)
│   │   │   │   ├── WebConfig.java               # Cấu hình resource handler (upload)
│   │   │   │   └── AppConfig.java               # ModelMapper bean
│   │   │   │
│   │   │   ├── entity/                          # JPA Entities (ánh xạ DB tables)
│   │   │   │   ├── User.java                    # Người dùng (Customer/Admin)
│   │   │   │   ├── Product.java                 # Sản phẩm
│   │   │   │   ├── Category.java                # Danh mục
│   │   │   │   ├── Order.java                   # Đơn hàng
│   │   │   │   ├── OrderItem.java               # Chi tiết đơn hàng
│   │   │   │   ├── CartItem.java                # Item trong giỏ hàng (session)
│   │   │   │   ├── Coupon.java                  # Mã giảm giá
│   │   │   │   ├── Review.java                  # Đánh giá sản phẩm
│   │   │   │   ├── ReviewStatus.java            # Enum: PENDING, APPROVED, REJECTED
│   │   │   │   ├── SizeEnum.java                # Enum: S, M, L, XL
│   │   │   │   └── Address.java                 # Địa chỉ giao hàng
│   │   │   │
│   │   │   ├── dto/                             # Data Transfer Objects
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── CategoryDTO.java
│   │   │   │   ├── OrderDTO.java
│   │   │   │   ├── OrderItemDTO.java
│   │   │   │   ├── CouponDTO.java
│   │   │   │   ├── ReviewDTO.java
│   │   │   │   ├── AddressDTO.java
│   │   │   │   ├── ProfileDTO.java
│   │   │   │   ├── ChangePasswordDTO.java
│   │   │   │   └── ProductSalesDTO.java
│   │   │   │
│   │   │   ├── repository/                      # Spring Data JPA Repositories
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── CouponRepository.java
│   │   │   │   ├── ReviewRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── AddressRepository.java
│   │   │   │
│   │   │   ├── service/                         # Business Logic Layer
│   │   │   │   ├── ProductService.java          # CRUD sản phẩm, upload ảnh, find/search
│   │   │   │   ├── CategoryService.java         # CRUD danh mục
│   │   │   │   ├── CartService.java             # Giỏ hàng (session-based)
│   │   │   │   ├── OrderService.java            # Tạo/quản lý đơn hàng
│   │   │   │   ├── CouponService.java           # Kiểm tra & áp dụng mã giảm giá
│   │   │   │   ├── ReviewService.java           # Đánh giá sản phẩm
│   │   │   │   ├── UserService.java             # Quản lý người dùng, xác thực
│   │   │   │   └── AddressService.java          # Quản lý địa chỉ giao hàng
│   │   │   │
│   │   │   ├── controller/                      # HTTP Controllers (MVC)
│   │   │   │   ├── ProductController.java       # Trang chủ, danh sách, tìm kiếm
│   │   │   │   ├── CartController.java          # Giỏ hàng (REST API + View)
│   │   │   │   ├── OrderController.java         # Checkout, lịch sử đơn hàng
│   │   │   │   ├── ProfileController.java       # Hồ sơ, mật khẩu, địa chỉ
│   │   │   │   ├── ReviewController.java        # Gửi đánh giá
│   │   │   │   ├── RegisterController.java      # Đăng ký tài khoản
│   │   │   │   ├── CategoryController.java      # Danh mục (public)
│   │   │   │   ├── AdminController.java         # Toàn bộ admin panel
│   │   │   │   ├── AdminReviewController.java   # Admin: duyệt review
│   │   │   │   └── LogoutController.java        # Xử lý đăng xuất
│   │   │   │
│   │   │   └── exception/                       # Exception handling
│   │   │
│   │   └── resources/
│   │       ├── application.properties           # Cấu hình ứng dụng
│   │       ├── data.sql                         # Dữ liệu mẫu ban đầu
│   │       ├── static/
│   │       │   ├── css/                         # Stylesheet CSS
│   │       │   ├── js/                          # JavaScript
│   │       │   └── images/                      # Ảnh tĩnh
│   │       └── templates/                       # Thymeleaf HTML Templates (26 files)
│   │           ├── layout.html                  # Layout chính (navigation, footer)
│   │           ├── index.html                   # Trang chủ
│   │           ├── product-list.html            # Danh sách sản phẩm
│   │           ├── product-detail.html          # Chi tiết sản phẩm
│   │           ├── cart.html                    # Giỏ hàng
│   │           ├── checkout.html                # Thanh toán
│   │           ├── orders.html                  # Lịch sử đơn hàng
│   │           ├── order-detail.html            # Chi tiết đơn hàng
│   │           ├── profile.html                 # Hồ sơ cá nhân
│   │           ├── register.html                # Đăng ký
│   │           ├── admin-dashboard.html         # Admin: Dashboard
│   │           ├── admin-products.html          # Admin: Quản lý sản phẩm
│   │           ├── admin-product-form.html      # Admin: Form sản phẩm
│   │           ├── admin-orders.html            # Admin: Quản lý đơn hàng
│   │           ├── admin-order-detail.html      # Admin: Chi tiết đơn hàng
│   │           ├── admin-categories.html        # Admin: Quản lý danh mục
│   │           ├── admin-coupons.html           # Admin: Quản lý coupon
│   │           ├── admin-users.html             # Admin: Quản lý người dùng
│   │           ├── admin-reviews.html           # Admin: Quản lý đánh giá
│   │           └── ...
│   │
│   └── test/                                    # Unit tests
│
├── uploads/                                     # Thư mục lưu ảnh upload (runtime)
├── pom.xml                                      # Maven dependencies
├── mvnw / mvnw.cmd                              # Maven Wrapper
└── README.md
```

---

## 🗄️ Database Schema

### Các bảng chính

```
┌──────────┐     ┌──────────┐     ┌──────────────┐
│  users   │─1:N─│  orders  │─1:N─│ order_items  │
│          │     │          │     │              │
│ id (PK)  │     │ id (PK)  │     │ id (PK)      │
│ username │     │ user_id  │     │ order_id     │
│ password │     │ total_   │     │ product_id   │
│ email    │     │ price    │     │ quantity     │
│ role     │     │ status   │     │ price        │
│ full_name│     │ applied_ │     │ size         │
│ phone    │     │ coupon   │     └──────────────┘
│ gender   │     └──────────┘
│ birthday │
└──────────┘
     │1:N
     ▼
┌──────────┐
│ addresses│
│          │
│ id (PK)  │
│ user_id  │
│ full_name│
│ phone    │
│ address_ │
│ line     │
│ city     │
│ is_      │
│ default  │
└──────────┘

┌──────────┐     ┌──────────────┐     ┌──────────┐
│ products │─1:N─│ product_sizes│     │categories│
│          │     │ (sizes enum) │     │          │
│ id (PK)  │     └──────────────┘     │ id (PK)  │
│ name     │                          │ name     │
│ desc     │◄────────────────────────│ slug     │
│ price    │ N:1 category_id          └──────────┘
│ stock    │
│ image_url│
└──────────┘
     │1:N
     ▼
┌──────────┐
│ reviews  │
│          │
│ id (PK)  │
│ user_id  │
│ product_ │
│ id       │
│ rating   │
│ comment  │
│ status   │
└──────────┘

┌──────────┐
│ coupons  │
│          │
│ id (PK)  │
│ code     │
│ discount_│
│ percent  │
│ start_   │
│ date     │
│ end_date │
└──────────┘
```

### Trạng thái đơn hàng (Order Status)

```
PENDING → PROCESSING → SHIPPED → DELIVERED
                              ↘ CANCELLED
```

---

## 🌐 API & Routes

### Public Routes (Không cần đăng nhập)

| Method | URL | Mô tả |
|---|---|---|
| `GET` | `/` | Trang chủ |
| `GET` | `/products` | Danh sách sản phẩm (có phân trang, lọc) |
| `GET` | `/product/{id}` | Chi tiết sản phẩm |
| `GET` | `/category/{slug}` | Sản phẩm theo danh mục |
| `GET` | `/search?keyword=...` | Tìm kiếm sản phẩm |
| `GET` | `/register` | Form đăng ký |
| `POST` | `/register` | Xử lý đăng ký |
| `POST` | `/cart/add` | Thêm vào giỏ hàng (AJAX) |
| `GET` | `/cart/summary` | Lấy tổng quan giỏ hàng (AJAX) |

### Routes cần Đăng nhập (ROLE_CUSTOMER)

| Method | URL | Mô tả |
|---|---|---|
| `GET` | `/cart` | Xem giỏ hàng |
| `POST` | `/cart/update` | Cập nhật số lượng (AJAX) |
| `POST` | `/cart/remove` | Xóa sản phẩm khỏi giỏ (AJAX) |
| `POST` | `/cart/apply-coupon` | Áp dụng mã giảm giá (AJAX) |
| `POST` | `/cart/remove-coupon` | Xóa mã giảm giá (AJAX) |
| `GET` | `/checkout` | Trang thanh toán |
| `POST` | `/checkout/place-order` | Đặt hàng |
| `GET` | `/orders` | Danh sách đơn hàng |
| `GET` | `/order/{id}` | Chi tiết đơn hàng |
| `GET` | `/profile` | Hồ sơ cá nhân |
| `POST` | `/profile/update` | Cập nhật hồ sơ |
| `GET` | `/profile/password` | Đổi mật khẩu |
| `POST` | `/profile/password` | Xử lý đổi mật khẩu |
| `GET` | `/profile/addresses` | Quản lý địa chỉ |
| `POST` | `/profile/addresses/save` | Lưu địa chỉ |
| `POST` | `/review/submit` | Gửi đánh giá sản phẩm |

### Admin Routes (ROLE_ADMIN)

| Method | URL | Mô tả |
|---|---|---|
| `GET` | `/admin/dashboard` | Dashboard thống kê |
| `GET` | `/admin/products` | Danh sách sản phẩm |
| `GET` | `/admin/products/new` | Form thêm sản phẩm |
| `POST` | `/admin/products/save` | Lưu sản phẩm |
| `GET` | `/admin/products/edit/{id}` | Form sửa sản phẩm |
| `GET` | `/admin/products/delete/{id}` | Xóa sản phẩm |
| `GET` | `/admin/categories` | Danh sách danh mục |
| `GET` | `/admin/orders` | Danh sách đơn hàng |
| `GET` | `/admin/orders/{id}` | Chi tiết đơn hàng |
| `POST` | `/admin/orders/update-status` | Cập nhật trạng thái |
| `GET` | `/admin/coupons` | Danh sách coupon |
| `POST` | `/admin/coupons/save` | Lưu coupon |
| `GET` | `/admin/users` | Danh sách người dùng |
| `POST` | `/admin/users/save` | Lưu người dùng |
| `GET` | `/admin/reviews` | Danh sách đánh giá |
| `POST` | `/admin/reviews/{id}/approve` | Duyệt đánh giá |
| `POST` | `/admin/reviews/{id}/reject` | Từ chối đánh giá |

---

## 👤 Tài khoản mặc định

Khi chạy lần đầu, hệ thống tự động tạo các tài khoản mẫu:

| Username | Password | Role | Email |
|---|---|---|---|
| `admin` | `password` | ROLE_ADMIN | admin@kidsfashion.com |
| `john` | `password` hoặc `12345678' | ROLE_CUSTOMER | john@example.com |
| `jane` | `password` hoặc `12345678' | ROLE_CUSTOMER | jane@example.com |
| `bob` | `password` hoặc `12345678' | ROLE_CUSTOMER | bob@example.com |
> Nếu muốn có thể vào tài khoản admin, để chỉnh lại tài khoản user.
> **Bảo mật:** Mật khẩu được mã hóa bằng **BCrypt** trước khi lưu vào database.

---

## 🔐 Bảo mật

- **Authentication**: Form-based Login với Spring Security
- **Password Encoding**: BCrypt
- **Authorization**: Role-based (`ROLE_ADMIN`, `ROLE_CUSTOMER`)
- **Session Management**: HTTP Session-based cho giỏ hàng
- **CSRF Protection**: Bật mặc định, ngoại lệ cho các Cart API endpoint
- **Login URL**: `/?login` (modal ở trang chủ)
- **Logout**: Xóa session, xóa cookie `JSESSIONID`

---

## 📦 Dữ liệu mẫu

File `data.sql` cung cấp dữ liệu khởi tạo bao gồm:
- **5 danh mục**: Tops, Bottoms, Dresses, Outerwear, Accessories
- **20 sản phẩm** đa dạng với giá từ 8.99$ đến 49.99$
- **4 người dùng**: 1 admin + 3 khách hàng
- **3 mã giảm giá**: SAVE10 (10%), WELCOME5 (5%), SUMMER20 (20%)
- **4 đơn hàng** mẫu với các trạng thái khác nhau

---

## 🏷️ Tính năng nổi bật

### 📸 Upload ảnh sản phẩm
- Hỗ trợ các định dạng ảnh phổ biến (JPEG, PNG, WebP...)
- Giới hạn kích thước tối đa: **5MB**
- Tự động phát hiện ảnh trùng lặp bằng **MD5 checksum**
- Tự động xóa ảnh cũ khi cập nhật hoặc xóa sản phẩm

### 🛒 Giỏ hàng Session-based
- Giỏ hàng không yêu cầu đăng nhập
- Phân biệt sản phẩm theo **size** (S/M/L/XL)
- Kiểm tra tồn kho realtime khi thêm vào giỏ
- Hỗ trợ coupon giảm giá

### ⭐ Hệ thống Review
- Chỉ cho phép người dùng đã mua và nhận hàng mới được review
- Mỗi người dùng chỉ review một lần trên mỗi sản phẩm
- Review tự động được duyệt (APPROVED) khi gửi
- Admin có thể duyệt/từ chối/xóa review

---

## Đóng góp

Project được phát triển trong khuôn khổ bài tập Java Nâng Cao. Mọi ý kiến đóng góp và cải tiến đều được hoan nghênh!

---

*Được xây dựng với ❤️ bằng Spring Boot & Thymeleaf*
