## Thông tin đồ án

**Đề tài:** Ứng dụng đặt vé xem phim  
**Môn học:** Nhập môn Ứng dụng Di động - SE114.Q22  
**Giảng viên hướng dẫn:** Nguyễn Tấn Toàn  
**Nhóm thực hiện:** Nhóm 12

1. Đoàn Việt Khang - 24520730 - Trưởng nhóm
2. Phạm Ngọc Gia Khang - 24520765 - Thành viên
3. Bùi Vạn Khải - 24520719 - Thành viên
4. Hồ Quốc Việt - 24521995 - Thành viên

---

## 1. Giới thiệu dự án

**Ứng dụng đặt vé xem phim CINEMAX** là ứng dụng đặt vé xem phim trực tuyến gồm:
- **Android Client** cho người dùng cuối.
- **Spring Boot Backend** xử lý nghiệp vụ và cung cấp REST API.
- **Firebase / Cloud Firestore** dùng cho xác thực, lưu trữ dữ liệu và đồng bộ thời gian thực.

### Mục tiêu hệ thống
- Cho phép khách hàng đăng ký, đăng nhập và xem phim.
- Tra cứu lịch chiếu, chọn ghế, đặt vé và thanh toán trực tuyến.
- Nhận vé điện tử qua QR Code.
- Hỗ trợ chatbot chăm sóc khách hàng.
- Cung cấp giao diện quản trị cho admin để quản lý phim, rạp, suất chiếu, voucher, người dùng và thống kê.

---

## 2. Công nghệ sử dụng

### Android Client
- **Ngôn ngữ:** Java
- **Android Studio:** Koala / Ladybug hoặc mới hơn
- **Gradle:** Gradle Wrapper của dự án
- **Thư viện chính:**
  - Firebase Authentication
  - Firebase Firestore
  - Firebase Storage
  - Retrofit
  - OkHttp Logging Interceptor
  - Gson
  - Glide
  - Material Components
  - ZXing QR Code

### Backend
- **Framework:** Spring Boot 3.2.5
- **Ngôn ngữ:** Java 17
- **Build Tool:** Maven
- **Bảo mật:** Spring Security + FirebaseTokenFilter
- **Scheduler:** Spring Scheduler
- **API Documentation:** Swagger/OpenAPI (springdoc)

### Cloud & Database
- Firebase Authentication
- Cloud Firestore
- Firebase Cloud Storage
- Render (deploy backend)

---

## 3. Yêu cầu cài đặt trước

Trước khi chạy dự án, cần chuẩn bị:
- JDK 17 hoặc cao hơn
- Android Studio
- Git

---

## 4. Hướng dẫn chạy Backend

### Bước 1: Clone mã nguồn
```bash
git clone <URL_CUA_REPO>
cd <TEN_THU_MUC_DU_AN>
```

### Bước 2: Cấu hình Firebase Admin SDK
Thiết lập biến môi trường `FIREBASE_SERVICE_ACCOUNT_BASE64` cho backend theo file service account của Firebase.
Lấy giá trị biến trong link này: https://docs.google.com/document/d/1pG79sXH5gbDwkwwVOQjEn2Y5XhLLc1mPq0dZ0p05FJc/edit?usp=sharing

### Bước 3: Chạy Backend local

**Windows**
```bash
cd cinema-booking-backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

**Linux/macOS**
```bash
cd cinema-booking-backend
chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Bước 4: Kiểm tra backend
- Truy cập: `http://localhost:8080/api/ping`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## 5. Hướng dẫn chạy Android Client

### Bước 1: Mở project
Mở **thư mục gốc** của dự án bằng Android Studio, không mở riêng thư mục `app`.

### Bước 2: Sync Gradle
Chọn **File → Sync Project with Gradle Files** nếu Android Studio chưa tự sync.

### Bước 3: Cấu hình Firebase
- Tải `google-services.json` từ Firebase Console.
- Copy vào thư mục `app/`.
- Đảm bảo package name khớp với `com.example.cinemabookingapp`.

### Bước 4: Cấu hình BASE_URL
Mở file:

`app/src/main/java/com/example/cinemabookingapp/core/config/ApiConfig.java`

Ví dụ:
- Emulator:
  ```java
  public static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";
  ```
- Thiết bị thật:
  ```java
  public static final String BASE_URL = "http://<IP_MAY_TINH_CUA_BAN>:8080/api/v1/";
  ```
- Render:
  ```java
  public static final String BASE_URL = "https://<APP_NAME>.onrender.com/api/v1/";
  ```

### Bước 5: Bật đăng nhập trên Firebase
- Bật `Email/Password`
- Bật `Google Sign-In` nếu cần
- Cập nhật `default_web_client_id` trong `strings.xml` nếu dùng Google Sign-In

### Bước 6: Chạy ứng dụng
- Kết nối emulator hoặc điện thoại thật
- Nhấn **Run 'app'** trong Android Studio

---

## 6. Cấu hình quan trọng

| File / Biến | Mục đích | Ghi chú |
|---|---|---|
| `application-local.yml` | Cấu hình backend local | Thiết lập port và Firebase Admin |
| `ApiConfig.java` | Cấu hình BASE_URL cho Android | Chỉnh theo local / Render |
| `google-services.json` | Firebase Android config | Bắt buộc có |
| `strings.xml` | Web Client ID cho Google Sign-In | Cập nhật nếu dùng Google login |
| `FIREBASE_SERVICE_ACCOUNT_BASE64` | Firebase Admin cho backend | Cấu hình trên máy hoặc Render |

---

## 7. Kiểm tra sau khi cài đặt

1. Đăng ký / đăng nhập tài khoản.
2. Xem danh sách phim.
3. Xem chi tiết phim.
4. Chọn suất chiếu và ghế.
5. Đặt vé và thanh toán.
6. Kiểm tra vé QR và lịch sử giao dịch.
7. Thử chatbot hỗ trợ.

---

## 8. Lỗi thường gặp

- Sai phiên bản JDK
- Sai `BASE_URL`
- Thiếu `google-services.json`
- Sai package name Firebase
- Backend chưa cấu hình Firebase Admin
- Render sleep khi gọi lần đầu
- Lỗi Firestore index

---

## 9. Kết luận

Nếu cấu hình đúng các file quan trọng và làm theo đúng thứ tự:
1. Chạy Backend
2. Cấu hình Android Client
3. Kết nối Firebase
4. Kiểm tra API

thì dự án có thể chạy ổn định từ môi trường local đến cloud.

