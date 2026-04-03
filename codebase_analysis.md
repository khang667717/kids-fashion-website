# Phân Tích Codebase - Kids Fashion Store

## 1. Tổng Quan Kiến Trúc (Architecture Overview)
Dự án **Kids Fashion Store** là một ứng dụng web thương mại điện tử (E-Commerce) nguyên khối (Monolithic), cung cấp nền tảng mua sắm quần áo trẻ em trực tuyến. Hệ thống được xây dựng theo mô hình **MVC** (Model-View-Controller) kết hợp chặt chẽ giữa Backend Java Spring Boot và Frontend sử dụng Template Engine Thymeleaf (SSR - Server Side Rendering) tích hợp JavaScript/AJAX để tăng tính tương tác.

### 2. Công Nghệ Sử Dụng (Tech Stack)
*   **Ngôn ngữ lập trình:** Java 17
*   **Framework chính:** Spring Boot 3.x
*   **Quản lý phụ thuộc:** Maven (`pom.xml` bao gồm các starter của Spring)
*   **Cơ sở dữ liệu:** MySQL (sử dụng thư viện kết nối `mysql-connector-j`)
*   **ORM (Object-Relational Mapping):** Spring Data JPA / Hibernate (tự động tạo bảng qua `ddl-auto=update`)
*   **Bảo mật:** Spring Security 6 (Mã hóa mật khẩu BCrypt, phân quyền role `ROLE_ADMIN` & `ROLE_CUSTOMER`, chống luồng giả mạo qua Token CSRF, ngoại trừ các endpoint Public và một phần của giỏ hàng cập nhật bằng AJAX)
*   **Frontend Rendering:** Thymeleaf (kết hợp `thymeleaf-extras-springsecurity6` để xử lý giao diện theo Role đóng/mở luồng người dùng)
*   **Tiện ích:** Lombok (giảm thiểu boilerplate code getters/setters), ModelMapper (ánh xạ Entity sang DTO và ngược lại).
*   **Môi trường / DevOps:** Có hỗ trợ DevTools LiveReload, có sẵn cấu hình hướng dẫn deploy lên Github Codespace (`Bai10_codespaces.txt`, `CODESPACES_HOSTING_GUIDE.md`).

## 3. Cấu Trúc Thư Mục Hệ Thống (Folder Structure)
Cấu trúc đi theo tiêu chuẩn của một Spring Boot project `src/main`:
*   Thư mục **`java/com/example/kidsfashion/`**:
    *   `config/`: Cấu hình hệ thống (Security, MVC/Static resources, Config Beans).
    *   `controller/`: Xử lý HTTP Request/Response, điều hướng View, hoặc API trả JSON.
    *   `dto/`: Các lớp Data Transfer Object truyền tải dữ liệu giữa Client và Server nhằm che giấu cấu trúc Entity thật bên dưới CSDL (VD: `ProductDTO`, `CartItemDTO`...).
    *   `entity/`: Định nghĩa các cấu trúc bảng trong CSDL bằng JPA Mapping (VD: `@Entity`, `@OneToMany`, `@ManyToOne`).
    *   `repository/`: Các Interface cấu hình kế thừa từ `JpaRepository` hỗ trợ query truy vấn vào MySQL mà không cần viết lệnh SQL gốc.
    *   `service/`: Lớp chứa lõi logic nghiệp vụ kinh doanh (Business Logic) nhằm giải phóng áp lực rác cho Controller.
    *   `exception/`: Xử lý ngoại lệ toàn cục (`GlobalExceptionHandler`), trả về JSON hoặc trang báo lỗi nhất quán.
*   Thư mục **`resources/`**:
    *   `static/`: Giao diện tĩnh (CSS, JS, Images, Fonts).
    *   `templates/`: Chứa file HTML Thymeleaf (`index.html`, `cart.html`, `checkout.html`, `admin-*.html`...).
    *   `application.properties`: Cấu hình Database, Hibernate, Server Tomcat Port 8080, Upload path (`uploads/`).
    *   `data.sql`: Dữ liệu mẫu ban đầu (Categories, Users, Roles, Products) để giả lập khi chạy dev.

## 4. Các Luồng Nghiệp Vụ Chính (Core Features/Workflows)
### Cho Người Dùng Mua Hàng (Customer)
*   **Xác thực:** Đăng ký, Đăng nhập (thông qua Pop-up Modal xử lý dạng AJAX vì có cấu hình `customAuthenticationFailureHandler` trả JSON trong `SecurityConfig` khi request bằng XMLHttpRequest).
*   **Mua sắm (Shopping):** Tìm kiếm động, lọc, hiển thị ảnh thời trang chuẩn tỷ lệ, chọn kích cỡ (`SizeEnum` lưu dưới dạng `@ElementCollection`).
*   **Giỏ hàng (Cart) & Thanh toán:** Giỏ hàng vận hành bằng cơ chế **Session** (chưa login vẫn lưu giỏ) đối với khách vãng lai thông qua API `/cart/add`, `/cart/api/*`.  Có apply Coupon discount, thanh toán tiền mặt (COD).
*   **Tương tác mở rộng:** Đánh giá độ hài lòng (Review & Ratings).

### Cho Quản Trị Hệ Thống (Admin)
*   **Thống kê Dashboard:** Xem tổng đơn hàng, doanh thu định kỳ, tổng sản phẩm.
*   **Quản Lý Vận Hành:** 
    *   Sản phẩm & Danh mục (Thêm hình, sửa nội dung mô tả, quản lý số lượng tồn).
    *   Quản lý thông tin Đơn hàng & Thay đổi trạng thái (Pending, Processing, Shipped...).
    *   Quản lý mã Khuyến mãi (Coupons).
    *   Quét/ẩn các Đánh giá (Reviews) có nội dung và trải nghiệm xấu.

## 5. Tổ Chức Database (ERD Modeling)
Thông qua JPA Entity (`@ManyToOne`, `@OneToMany`) và lược đồ Data, DB có 10 bảng lõi, phản ánh mối liên kết phức tạp:
1.  **`users`**: Khách hàng, Quản trị.
2.  **`roles / authorities`**: Phân quyền hệ thống qua Spring Security.
3.  **`addresses`**: Liên kết nhiều địa chỉ cho một `user`.
4.  **`categories`** (1-N) **`products`**: Sản phẩm nằm trong một nhóm danh mục hàng.
5.  **`product_sizes`**: Một sản phẩm có nhiều định dạng kích cỡ (S, M, L, XL...).
6.  **`orders`** & **`order_items`**: Lưu trạng thái bill và chi tiết list mặt hàng chốt đơn.
7.  **`coupons`**: Bảng dữ liệu ưu đãi mã giảm giá.
8.  **`reviews`**: Nhận xét theo ràng buộc liên kết (`product_id`, `user_id`).
9.  **`cart_items`**: Track DB khi Session giỏ hàng bị lưu nếu đăng nhập. 

## 6. Đánh Giá Khách Quan 
*   **Điểm mạnh:**
    *   Code tổ chức logic rất gọn gàng với mô hình phân lớp rõ rệt.
    *   Xây dựng hệ thống Security an toàn có mã hóa mật khẩu, phân chia Admin/User role chuẩn tắc cùng Session/Cookies.
    *   UI/UX được thiết kế chi tiết (responsive, aspect-ratio hình ảnh đồng bộ). Có bắt luồng bất đồng bộ API cho giỏ hàng để tránh phiền toái tải lại trang của khách hàng.
*   **Điểm cần mở rộng:**
    *   Chưa tích hợp thanh toán Online thực tế (cổng Payment Gateways như Stripe, VNPAY...).
    *   Hệ thống hóa đơn cần email gửi động (Java Mail Sender SMTP).
    *   Cart hiện tại phụ thuộc phần lớn vào HTTP Session, có thể gây mất giỏ khi hết thời gian Session timeout nếu chưa liên kết vào User account. Lịch sử Log ra file khá chuẩn nhưng chưa tích hợp ELK stack.
