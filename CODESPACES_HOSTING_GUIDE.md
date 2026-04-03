# Hướng dẫn Hosting Spring Boot bằng GitHub Codespaces

Tài liệu này dựa trên nội dung file PDF "Bài 10 - Hướng dẫn Hosting chạy trên Github codespace.pdf" đã được trích xuất và tổng hợp.

**Mục tiêu:** Chạy và chia sẻ (public) một dự án Spring Boot trong GitHub Codespaces, kèm hướng dẫn kết nối MySQL và phần mở rộng (extension) trong VS Code.

---

## 1. Yêu cầu trước
- Tài khoản GitHub và repository chứa mã nguồn Spring Boot.
- Đã tạo Codespace hoặc có quyền tạo Codespace cho repo.
- Quyền sudo trong Codespace (mặc định có thể cho phép cài gói bằng apt).

## 2. Tạo Codespace
1. Truy cập: https://github.com/codespaces → chọn **New codespace**.
2. Chọn repository, nhánh (Branch), Region, Machine type (khuyến nghị: 2-core).
3. Click **Create codespace**.

## 3. Upload / mở dự án trong Codespace
- Bạn có thể copy/paste hoặc kéo-thả folder dự án từ máy local vào workspace Codespace (hoặc push mã lên repository rồi mở repository trong Codespace).
- Mở terminal trong Codespace và vào thư mục dự án:

```bash
cd /workspaces/<Repository>/<project-folder>
```

## 4. Cài JDK
Trong terminal Codespace chạy (sử dụng OpenJDK 17 để khớp với `pom.xml` của project):

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version
```

## 5. Chuẩn bị build/run
- Cấp quyền cho Maven Wrapper nếu dùng:

```bash
chmod +x mvnw
```

## 6. Cài MySQL (nếu muốn chạy DB trong Codespace)
> Lưu ý: database trong Codespace có thể bị reset/không bền khi stop/destroy Codespace. Nếu cần bền, dùng DB quản lý bên ngoài (RDS, PlanetScale, v.v.).

```bash
sudo apt install -y mysql-server
sudo service mysql start
sudo service mysql status
```

Tôi đã chuẩn bị một khối lệnh dưới đây để bạn có thể copy/paste toàn bộ và chạy trực tiếp trong Codespace (không cần chỉnh gì). Khối lệnh sẽ:
- chuyển vào thư mục repo `/workspaces/kids-fashion-store`
- cấp quyền cho `mvnw`
- cài OpenJDK 17
- cài và khởi động MySQL
- tạo database `kids_fashion_store_db` và user `kfs_user` với mật khẩu `kfs_pass123`
- chạy ứng dụng bằng `./mvnw spring-boot:run` với biến môi trường cấu hình datasource

---

## Khối lệnh copy/paste (dán nguyên vào terminal Codespace)

```bash
# Vào thư mục project (Codespaces mount vào /workspaces)
cd /workspaces/kids-fashion-store

# Cấp quyền cho Maven Wrapper
chmod +x mvnw

# Cập nhật apt và cài OpenJDK 17 (khớp với java.version trong pom.xml)
sudo apt update && sudo apt install -y openjdk-17-jdk
java -version

# Cài MySQL và khởi động
sudo apt install -y mysql-server
sudo service mysql start
sudo service mysql status

# Tạo database và user (không cần chỉnh)
sudo mysql -e "CREATE DATABASE IF NOT EXISTS kids_fashion_store_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "CREATE USER IF NOT EXISTS 'kfs_user'@'localhost' IDENTIFIED BY 'kfs_pass123';"
sudo mysql -e "GRANT ALL PRIVILEGES ON kids_fashion_store_db.* TO 'kfs_user'@'localhost'; FLUSH PRIVILEGES;"

# Kiểm tra database
sudo mysql -e "SHOW DATABASES LIKE 'kids_fashion_store_db';"

# Chạy ứng dụng bằng Maven với biến môi trường cho datasource (không cần chỉnh file properties)
SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/kids_fashion_store_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
SPRING_DATASOURCE_USERNAME='kfs_user' \
SPRING_DATASOURCE_PASSWORD='kfs_pass123' \
./mvnw spring-boot:run

# Sau khi app chạy, mở tab Ports trong Codespaces và set port 8080 thành Public, sau đó chọn Open in Browser để lấy link.
```

---

## 7. Cấu hình `application.properties`
Chỉnh file `src/main/resources/application.properties` (hoặc tương đương):

```
spring.application.name=ten_project
spring.datasource.url=jdbc:mysql://localhost:3306/<yourdatabase>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.forward-headers-strategy=framework
server.tomcat.use-relative-redirects=true
```

Thay `yourdatabase`, `yourusername`, `yourpassword` phù hợp.

## 8. Chạy ứng dụng Spring Boot
Trong thư mục dự án:

```bash
./mvnw spring-boot:run
# hoặc
mvn spring-boot:run
```

Nếu chạy thành công, ứng dụng mặc định lắng nghe trên port (ví dụ 8080).

## 9. Mở port và share (public) link
1. Trong giao diện Codespaces, mở tab **Ports**.
2. Chọn port ứng dụng (ví dụ 8080) → chuột phải → **Port visibility** → **Public**.
3. Chọn **Open in browser** để lấy link chia sẻ (URL public) và gửi cho người khác.

## 10. Dừng ứng dụng / Codespace
- Trong terminal, dừng app: `Ctrl+C`.
- Nếu không dùng Codespace nữa, stop để tiết kiệm thời gian/nguồn:
  - Truy cập https://github.com/codespaces, chọn codespace đang chạy → **Stop codespace**.

## 11. Extension (Phần mở rộng) — Quản lý Database trong VS Code
1. Cài extension quản lý MySQL/SQL (ví dụ: `MySQL` hoặc `SQLTools` hoặc extension tương tự trên Marketplace).
2. Mở thanh Database/Connections phía trái trong VS Code.
3. Tạo connection mới với thông tin:
   - Host: `localhost`
   - Port: `3306`
   - User / Password: theo user đã tạo
   - Database: tên database
4. Kết nối và kiểm tra bảng/dữ liệu.

Ghi chú: khi dùng extension trong Codespace, thông tin `localhost` trỏ đến máy ảo Codespace.

## 12. Gợi ý & lưu ý thêm
- Máy Codespace có giới hạn thời gian và tài nguyên; chọn `2-core` để có thêm thời gian sử dụng theo hướng dẫn gốc.
- Nếu cần dữ liệu giữ lâu dài, dùng một DB bên ngoài (hosted) để tránh mất dữ liệu khi Codespace bị dừng.
- Nếu gặp lỗi kết nối MySQL, kiểm tra `sudo service mysql status` và logs trong `/var/log/mysql`.
- Khi triển khai thực tế (production), không chạy DB trong Codespace — chỉ dùng Codespace cho phát triển và demo.

---

## Nơi lưu trữ tệp liên quan
- Bản text trích xuất từ PDF: `Bai10_codespaces.txt` (đã tạo trong workspace).
- Hướng dẫn này: `CODESPACES_HOSTING_GUIDE.md`.

## Các lệnh hữu ích (nếu bạn muốn chạy/kiểm thử thủ công)

- Build jar (bỏ qua test nhanh):

```bash
./mvnw clean package -DskipTests
```

- Chạy jar thủ công (sau khi build):

```bash
java -jar target/kids-fashion-store-0.0.1-SNAPSHOT.jar
```

- Chạy test:

```bash
./mvnw test
```

- (Tùy chọn) Docker: tạo `Dockerfile` rồi build/run như sau:

```bash
docker build -t kids-fashion-store:latest .
docker run -p 8080:8080 \
  --env SPRING_DATASOURCE_URL="jdbc:mysql://host:3306/yourdb" \
  --env SPRING_DATASOURCE_USERNAME=yourusername \
  --env SPRING_DATASOURCE_PASSWORD=yourpassword \
  kids-fashion-store:latest
```

Ghi chú: khối lệnh copy/paste phía trên đã bao gồm mọi bước thiết lập cần thiết cho Codespace; bạn chỉ cần dán và chạy nguyên khối trong terminal Codespace.

---

Nếu bạn muốn, tôi có thể:
- Mở pull request chứa hướng dẫn này, hoặc
- Chỉnh lại thành file tiếng Anh, hoặc
- Thêm mục cấu hình cụ thể cho project của bạn (đường dẫn, tên database, ví dụ dữ liệu).

Chọn bước tiếp theo bạn muốn tôi làm.