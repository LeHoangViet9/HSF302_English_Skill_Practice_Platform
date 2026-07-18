# Hướng dẫn test Module Nội dung học & Tra cứu

## 1. Chuẩn bị database

Mở SQL Server Management Studio hoặc Azure Data Studio, chọn database `EnglishLearningDB`, rồi chạy file an toàn encoding:

```text
database/learning_content_seed_ascii_safe.sql
```

File này thêm dữ liệu mẫu cho:

- `lessons`: bài học Grammar, Vocabulary, Pronunciation theo CEFR.
- `lesson_contents`: từ vựng, cấu trúc, IPA, nghĩa, ví dụ.
- `learning_progress`: dữ liệu bài đã hoàn thành cho `student@espp.com`.
- `book_marks`: dữ liệu từ đã lưu cho `student@espp.com`.

File có `IF NOT EXISTS`, nên có thể chạy lại mà không bị nhân đôi dữ liệu mẫu chính.
File `learning_content_seed_ascii_safe.sql` dùng dữ liệu ASCII để tránh lỗi vỡ dấu khi SSMS đọc sai encoding. Nếu muốn seed tiếng Việt có dấu trực tiếp từ file SQL, hãy dùng `learning_content_seed.sql` và mở/lưu file bằng `UTF-8 with BOM`.

## 2. Chạy ứng dụng

Chạy trong IntelliJ bằng main class:

```text
com.edu.espp.EnglishSkillPracticePlatformApplication
```

Hoặc chạy bằng Maven:

```powershell
.\mvnw.cmd spring-boot:run
```

Nếu báo cổng `8080` đang được dùng, dừng run cũ trong IntelliJ hoặc đổi tạm:

```properties
server.port=8081
```

## 3. Đăng nhập tài khoản test

Tài khoản có sẵn từ `DatabaseSeeder`:

```text
Email: student@espp.com
Password: 123456
```

Nếu cần vào staff/admin để nhập bài học:

```text
Email: admin@espp.com
Password: 123456
```

## 4. Test Screen 4: Learning Content

Vào:

```text
http://localhost:8080/student/lessons
```

Checklist:

- Trang hiển thị danh sách bài học.
- Lọc theo `Loại bài`: `GRAMMAR`, `VOCABULARY`, `PRONUNCIATION`.
- Lọc theo `CEFR`: `A1`, `A2`, `B1`, `B2`, `C1`, `C2`.
- Nhập keyword như `hiện tại`, `IPA`, `môi trường`.
- Bấm `Vào học` để xem chi tiết.
- Bấm `Đã hoàn thành`.
- Sau khi bấm, trang chi tiết hiện trạng thái `Đã hoàn thành`.
- Quay lại danh sách, bài đó có badge `Đã hoàn thành`.

API test nhanh:

```text
http://localhost:8080/student/lessons/api?type=VOCABULARY&level=A1
```

## 5. Test Screen 8: Dictionary & Bookmark

Vào:

```text
http://localhost:8080/student/dictionary
```

Checklist:

- Tìm từ `sustainable`.
- Tìm cấu trúc `Subject`.
- Tìm IPA hoặc từ `ship`.
- Lọc theo một bài học cụ thể.
- Bấm `Lưu` để bookmark.
- Bấm lại `Đã lưu` để bỏ bookmark.

Vào danh sách từ đã lưu:

```text
http://localhost:8080/student/bookmarks
```

Checklist:

- Từ vừa lưu xuất hiện trong danh sách.
- Bấm `Bỏ lưu`, từ biến mất khỏi danh sách.
- Bấm tên bài học để quay về bài chứa từ đó.

API test nhanh:

```text
http://localhost:8080/student/dictionary/api?keyword=sustainable
```

## 6. Test Screen 11: Staff Management Panel

Vào:

```text
http://localhost:8080/staff/lessons
```

Checklist:

- Xem danh sách bài học.
- Tìm kiếm/lọc bài học.
- Thêm bài học mới.
- Sửa bài học.
- Trong trang sửa bài học, thêm nội dung chi tiết:
  - Từ vựng / Cấu trúc
  - IPA
  - Nghĩa
  - Ví dụ
  - Giải thích
  - Thứ tự
- Sửa nội dung chi tiết.
- Xóa nội dung chi tiết.
- Quay lại `/student/lessons`, kiểm tra học viên thấy nội dung mới.

## 7. Lỗi thường gặp

Nếu bấm `Đã hoàn thành` bị lỗi:

- Kiểm tra URL đang là `/student/lessons/{id}/complete`.
- Chạy lại bản code mới nhất vì endpoint đã hỗ trợ cả `GET` và `POST`.
- Kiểm tra lesson id đó có tồn tại trong bảng `lessons`.

Nếu không thấy dữ liệu:

- Chạy lại `database/learning_content_seed.sql`.
- Kiểm tra app đang kết nối đúng database `EnglishLearningDB`.
- Kiểm tra `.env` có đúng mật khẩu SQL Server.

Nếu app báo lỗi cổng:

- Dừng process Java cũ trong IntelliJ.
- Hoặc đổi `server.port=8081`.
