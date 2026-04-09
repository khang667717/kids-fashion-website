# Kiến Trúc Spring Boot trong Kids Fashion Store

Dựa trên cấu trúc dự án **Kids Fashion Store**, hệ thống đang sử dụng mô hình kiến trúc chuẩn của Spring Boot (MVC + 3-tier architecture). 

Dưới đây là giải thích chi tiết về chức năng của từng thành phần (`config, controller, dto, entity, repository, service, exception`) được áp dụng thực tế trong code nguyên bản của bạn:

## 1. `config` (Cấu hình hệ thống)
*   **Chức năng chung:** Chứa các class để thiết lập, cấu hình hoặc ghi đè các hành vi cho toàn bộ ứng dụng Spring Boot.
*   **Trong code của bạn:** 
    *   `AppConfig.java`: Tạo các Bean dùng chung như `ModelMapper` để tự động map dữ liệu giữa Entity và DTO.
    *   `SecurityConfig.java`: Quản lý bảo mật phân quyền (Quy định rule cho `ADMIN` và `CUSTOMER`), thiết lập trang login, phân quyền các API (giỏ hàng, thanh toán, quản trị), tắt CSRF cho một số call AJAX nhất định.
    *   `WebConfig.java`: Cấu hình map URL (để Spring Boot biết phải lấy ảnh tĩnh tĩnh từ thư mục `/uploads/**` trên ổ cứng).

## 2. `controller` (Điều hướng & Tiếp nhận Request)
*   **Chức năng chung:** Đóng vai trò là lớp bề mặt (Presentation Layer). Tiếp nhận các request HTTP (GET, POST, PUT, DELETE ...) từ trình duyệt điện thoại/web, kiểm tra dữ liệu đầu vào cơ bản đầu tiên, gọi xuống lớp `service` để xử lý, sau đó định hướng và trả về dữ liệu cho Client (JSON cho API hoặc giao diện HTML qua Thymeleaf).
*   **Trong code của bạn:**
    *   Chia làm 2 nhóm rõ rệt: **Customer Controller** (`ProductController`, `CartController`, `OrderController`...) và **Admin Controller** (`AdminController`, `AdminReviewController`...).
    *   Ví dụ: `CartController` nhận các request liên quan đến giỏ hàng (thêm, cập nhật, áp dụng voucher) và trả về phản hồi dạng JSON để frontend xử lý giao diện AJAX (không cần load lại trang web).

## 3. `dto` (Data Transfer Object - Đối tượng truyền dữ liệu)
*   **Chức năng chung:** Đóng gói dữ liệu để mang từ tầng này sang tầng khác (thường là giữa View/Frontend ↔ Controller ↔ Service). Nó giúp **ẩn đi cấu trúc thật của Database** (Entities) để bảo mật, đồng thời gom lại hoặc loại bỏ những dữ liệu dư thừa cho phù hợp với từng Use Case (chức năng).
*   **Trong code của bạn:**
    *   `ProductDTO.java`: Chứa thêm các phương thức logic hỗ trợ hiển thị ngay trên view như `getSizes()` (sắp xếp size S, M, L cho đẹp mắt) và `getCategorySlug()` để tạo đường dẫn thân thiện (ví dụ: `ao-thun`).
    *   `AddressDTO.java` hoặc `ChangePasswordDTO.java`: Định nghĩa rạch ròi, chỉ mang những thông tin cần thiết nhất khi người dùng Submit form HTTP.

## 4. `entity` (Thực thể / Ánh xạ Database)
*   **Chức năng chung:** Ánh xạ 1-1 với các bảng (tables) trong cơ sở dữ liệu (Database MySQL) nhờ vào cơ chế JPA/Hibernate. Mỗi thuộc tính trong class tương ứng với một cột trong bảng, mỗi class là một bảng. 
*   **Trong code của bạn:** Bạn có các entity cực kỳ cơ bản và thiết yếu cho một hệ thống E-Commerce: `Product` (Sản phẩm), `Category` (Danh mục), `Order` (Đơn hàng), `OrderItem` (Chi tiết đơn hàng), `User` (Người dùng), `Coupon` (Mã giảm giá/Voucher)... Đây là kho chứa cốt lõi để duy trì cấu trúc dữ liệu dưới DB.

## 5. `repository` (Thao tác cơ sở dữ liệu)
*   **Chức năng chung:** Đóng vai trò là lớp truy cập dữ liệu (Data Access Layer). Thường dùng các interface kế thừa (`extends`) `JpaRepository` của Spring Data JPA. Lớp này cung cấp sẵn các chức năng CRUD (Lưu/Tạo, Cập nhật, Xóa, Đọc) trực tiếp với DB, cũng như cung cấp khả năng tự tạo các câu Query SQL ngắn gọn theo convention mà không cần gõ hẳn SQL.
*   **Trong code của bạn:** Có các interface như `ProductRepository` chuyên xử lý tìm sản phẩm theo ID, theo danh mục hoặc theo từ khoá tìm kiếm trả về các List hoặc Page dữ liệu.

## 6. `service` (Logic nghiệp vụ)
*   **Chức năng chung:** Là trái tim của ứng dụng (Business Layer). Chứa mọi thuật toán xử lý tính toán, logic kiểm tra điều kiện nghiệp vụ phức tạp trước khi yêu cầu `repository` lưu vào cơ sở dữ liệu, hoặc tính toán xào nấu xong dữ liệu thì mới chuyển ra ngoài cho `controller`.
*   **Trong code của bạn:**
    *   `OrderService`: Xử lý logic đặt hàng: tính tiền tổng, trừ tiền voucher, tạo đơn mới..
    *   `UserService`: Chứa logic liên quan tới tài khoản: thao tác mã hoá mật khẩu (Bcrypt Algorithm) lúc đăng ký, kiểm tra điều kiện mật khẩu cũ mới khi đổi mật khẩu...

## 7. `exception` (Xử lý Ngoại lệ/Lỗi)
*   **Chức năng chung:** Nơi định nghĩa các class chuyên biệt để phân loại lỗi tùy chỉnh của ứng dụng. Cũng là nơi chứa bộ xử lý lỗi tập trung toàn cục (Thường cấu hình qua `@ControllerAdvice` và `@ExceptionHandler`) nhận trách nhiệm "bắt" (catch) các Exception lọt ra trong hệ thống để trả về giao diện cảnh báo lỗi hoặc mã lỗi HTTP cụ thể thay vì màn hình crash/stack trace rùng rợn.
*   **Trong code của bạn:** Gồm việc xử lý khi không tìm thấy tài nguyên (Sản phẩm không tồn tại, người dùng không hợp lệ...), cảnh báo ra View thân thiện với khách hàng.

---

## 8. Chu kỳ hoạt động tuần hoàn (Request-Response Lifecycle)
Chu kỳ hoạt động trong dự án tuân theo một luồng xử lý đồng bộ và bảo mật qua các tầng. Dưới đây là thứ tự cụ thể của chu kỳ này, từ lúc hệ thống khởi động cho tới khi hoàn tất 1 vòng phục vụ khách hàng:

### ⚙️ Lớp nền (Chỉ chạy 1 lần khi khởi động)
**[1]. Config Layer:** 
Khi bạn chạy project (chạy file `KidsFashionApplication.java`), các file trong thư mục `config` sẽ được nạp đầu tiên. `SecurityConfig` thiết lập lớp lá chắn bảo vệ URL, `AppConfig` tạo sẵn các Bean tự động nạp như *ModelMapper*, `WebConfig` chuẩn bị sẵn đường cấu hình file ảnh tĩnh. Sau khi cấu hình xong, hệ thống ngầm chạy ứng dụng ở trạng thái **Chờ HTTP Request**.

### 🔄 Chu kỳ 1 vòng Request - Response (Xảy ra liên tục)
Giả sử khách hàng vừa bấm nút **"Thêm vào giỏ hàng"** ở trên giao diện. Chu kỳ sẽ diễn ra theo thứ tự như sau:

**[2]. Tiếp nhận (Client ➜ Controller ➜ DTO)**
*   **Hành động:** Trình duyệt web gửi thông tin qua một HTTP POST Request (VD: Gửi thông tin: *ID áo thun, số lượng: 2*).
*   **Bắt đầu:** Request qua được chốt chặn của `SecurityConfig` (hợp lệ) thì sẽ được đón bởi `CartController`.
*   **Đóng gói:** Controller nhận dữ liệu thô và gom/kiểm tra ngay lập tức qua một object **DTO** để tiện kiểm soát.

**[3]. Xử lý Nghiệp vụ (Controller ➜ Service)**
*   **Hành động:** `Controller` không tự xử lý tính toán mà gọi đến lớp `Service` và truyền **DTO** xuống.
*   **Logic:** `Service` kiểm tra nghiệp vụ cốt lõi: Sản phẩm còn hàng không? Kích cỡ này hợp lệ không? 
*   **Bẻ lái (Exception):** Nếu có vấn đề logic, `Service` lập tức ném ra lỗi (Throw Exception). Exception Handler sẽ "túm" báo lỗi đó, cắt đứt chu kỳ hiện tại và trả báo lỗi thân thiện ra client.

**[4]. Truy xuất Dữ liệu (Service ➜ Repository ➜ Entity)**
*   Nếu điều kiện hợp lệ, `Service` sẽ nhờ `Repository` tương tác với DB.
*   **Hành động:** Lớp `Repository` phát sinh lệnh SQL gửi xuống Database MySQL qua JPA.
*   **Nhận hàng:** Database trả kết quả dòng dữ liệu, JPA/Hibernate lập tức map dòng dữ liệu đó thành class **Entity** (ví dụ `Product.java`).

**[5]. Trả Dữ liệu ngược lên (Entity ➜ Service ➜ DTO)**
*   `Repository` trao lại đối tượng **Entity** cho `Service`.
*   Vì quy tắc bảo mật thiết kế, `Service` làm tính toán xong **không bao giờ ném trực tiếp Entity ra ngoài** (để tránh lộ schema Database nội bộ). Nó dùng `ModelMapper` để copy dữ liệu từ Entity đó sang một cái **DTO** mới gọn nhẹ hơn.

**[6]. Hoàn thành Chu kỳ (Service ➜ Controller ➜ Client)**
*   `Service` đẩy object **DTO** hoàn chỉnh lên cho `Controller`.
*   `Controller` gói cái DTO đó vào trong cấu trúc JSON (đối với gọi AJAX API) hoặc gửi ra View giao diện HTML Thymeleaf.
*   **Kết thúc:** Controller trả về gói nội dung lại thành bản HTTP Response, hiển thị popup "Đã thêm thành công!" trên màn hình của khách hàng.

> **🔁 Tóm tắt biểu đồ luồng gọi (Vòng tròn khép kín):**
> Website ➜ `Controller` ➜ *(DTO)* ➜ `Service` ➜ `Repository` ➜ *(Entity)* ➜ MySQL Database ➜ Tương tác DB thành công ➜ `Repository` ➜ *(Entity)* ➜ `Service` ➜ *(DTO)* ➜ `Controller` ➜ Website hiển thị JSON hoặc View HTML
