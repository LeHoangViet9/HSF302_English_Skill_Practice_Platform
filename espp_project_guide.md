# Hướng dẫn Bảo vệ Project: Module Đề thi, Câu hỏi & Lịch sử thi

Tài liệu này giúp bạn hiểu rõ bản chất luồng code liên quan đến tính năng thi trắc nghiệm trong dự án **English Skill Practice Platform (ESPP)**, từ đó dễ dàng ứng phó khi giáo viên yêu cầu giải thích hoặc code thêm tính năng tại chỗ.

---

## 1. Mối quan hệ giữa các Bảng (Entities)

Trọng tâm của module này xoay quanh 4 file Entity chính nằm trong thư mục `src/main/java/com/edu/espp/entity/`:

- **`Exam` (Đề thi):** Chứa thông tin chung (Tiêu đề, Loại đề, Thời gian thi, Số lượng câu). Mỗi đề thi (`Exam`) sẽ có nhiều Câu hỏi (`Question`) -> Quan hệ `OneToMany`.
- **`Question` (Câu hỏi):** Thuộc về 1 `Exam` (Quan hệ `ManyToOne`). Lưu nội dung câu hỏi, 4 đáp án dạng JSON (`options`), đáp án đúng (`correctAnswer`) và giải thích (`explanation`).
- **`User` (Người dùng):** Đại diện cho học sinh hoặc admin.
- **`ExamHistory` (Lịch sử làm bài):** Lưu lại thông tin 1 lượt làm bài. Thuộc về 1 `User` (Ai làm?) và 1 `Exam` (Làm đề nào?) -> Quan hệ `ManyToOne` với cả 2 bảng. Lưu thêm điểm số (`score`), số câu đúng, thời gian làm bài, ngày thi.

---

## 2. Luồng hoạt động (Data Flow)

Kiến trúc code tuân thủ mô hình **MVC (Model - View - Controller)** kết hợp mô hình **Repository - Service**:

1. **Database (Repository):** `ExamRepository`, `QuestionRepository`, `ExamHistoryRepository` kế thừa `JpaRepository`. Dùng để giao tiếp với DB, ví dụ: `findAllByOrderByTestedAtDesc()`.
2. **Xử lý logic (Service):** Ví dụ `ExamHistoryServiceImpl.java` nhận dữ liệu từ DB, dùng `Stream API` (.map) để biến đổi các Entity (có quá nhiều thông tin) thành các **DTO (Data Transfer Object)** (ví dụ: `ExamHistoryResponse`) cho gọn nhẹ và an toàn trước khi đẩy lên View.
3. **Tiếp nhận Request (Controller):** Nhận yêu cầu từ URL (ví dụ `/admin/histories`), gọi hàm trong Service để lấy List DTO, gán vào `Model` (bằng `model.addAttribute`), và trả về tên file giao diện (HTML).
4. **Hiển thị (View - Thymeleaf):** File HTML (ví dụ `admin/history/list.html`) dùng vòng lặp `th:each="history : ${histories}"` để vẽ từng dòng dữ liệu ra bảng.

---

## 3. Các kịch bản Giáo viên yêu cầu sửa Code

Dưới đây là các yêu cầu thường gặp khi bảo vệ đồ án và cách bạn thực hiện chúng trực tiếp:

### Kịch bản 1: "Hãy sắp xếp danh sách Lịch sử thi theo Điểm số từ cao xuống thấp thay vì ngày thi"
- **Cách làm:**
  1. Mở `ExamHistoryRepository.java`.
  2. Viết thêm 1 hàm mới: 
     ```java
     List<ExamHistory> findAllByOrderByScoreDesc();
     ```
  3. Mở `ExamHistoryServiceImpl.java`. Tìm hàm `getAllExamHistories()`.
  4. Đổi lệnh gọi repo thành hàm vừa tạo:
     ```java
     // Cũ: examHistoryRepository.findAllByOrderByTestedAtDesc();
     List<ExamHistory> histories = examHistoryRepository.findAllByOrderByScoreDesc();
     ```
  5. Chạy lại server và F5 web.

### Kịch bản 2: "Làm thế nào để thêm cột 'Xếp loại' (Giỏi, Khá, Trung bình) vào bảng Lịch sử thi?"
- **Cách làm:** Không cần lưu vào DB vì Xếp loại tính được từ Điểm.
  1. Mở `ExamHistoryResponse.java` (DTO). Thêm: `private String rank;`
  2. Mở `ExamHistoryServiceImpl.java`. Trong khối `.map(history -> ...)` thêm logic:
     ```java
     String rank = "Trung bình";
     if(history.getScore() >= 8.0) rank = "Giỏi";
     else if(history.getScore() >= 6.5) rank = "Khá";
     ```
  3. Set biến rank vào `.rank(rank)` khi `.build()`.
  4. Mở file HTML (`admin/history/list.html`), thêm cột thẻ `<th>Xếp loại</th>` và in nó ra bằng `<td><span th:text="${history.rank}"></span></td>`.

### Kịch bản 3: "Tìm kiếm lịch sử thi theo tên bài thi thì làm thế nào?"
- **Cách làm:** 
  1. Trong `ExamHistoryRepository` viết câu Query:
     ```java
     List<ExamHistory> findByExamTitleContainingIgnoreCase(String title);
     ```
  2. Tại Controller, lấy thêm tham số `@RequestParam(required = false) String keyword`.
  3. Nếu `keyword` != null, gọi hàm tìm kiếm, nếu null thì gọi hàm lấy tất cả.

### Kịch bản 4: "Làm sao để đảm bảo khi Admin xoá 1 Đề thi, tất cả Câu hỏi của đề thi đó bị xoá theo?"
- **Trả lời vấn đáp:** "Dạ em sử dụng tính năng **Cascade** của JPA/Hibernate ạ. Trong file `Exam.java`, ở list các `Question` em đã đánh dấu là `@OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)`. Thuộc tính `cascade = CascadeType.ALL` sẽ giúp tự động xoá toàn bộ câu hỏi con khi đề thi cha bị xoá."

### Kịch bản 5: "Nếu muốn lấy ra Điểm thi cao nhất của 1 học sinh thì code thế nào?"
- **Cách làm (bằng Spring Data JPA):**
  1. Trong `ExamHistoryRepository`, viết: 
     ```java
     ExamHistory findTopByUserIdOrderByScoreDesc(Long userId);
     ```
  2. JPA sẽ tự dịch ra câu SQL có `TOP 1 ... ORDER BY score DESC` cực kỳ tiện lợi.

> [!TIP]
> **Lời khuyên:** Hãy chắc chắn bạn nhớ vị trí các package: Entity chứa bảng, Repository chứa truy vấn DB, DTO chứa dữ liệu trả về HTML, Service xử lý logic, Controller bắt link (URL). Nắm rõ 5 lớp này là bạn hoàn toàn có thể cân mọi câu hỏi của giáo viên!

---

## 4. Frontend - Cấu trúc Giao diện (Thymeleaf Templates)

Toàn bộ giao diện của dự án nằm trong thư mục `src/main/resources/templates/` và được render bởi **Thymeleaf** (Template Engine tích hợp Spring Boot).

### 4.1. Sơ đồ cây thư mục Templates

```
templates/
├── fragments/              ← Các mảnh giao diện dùng chung (tái sử dụng)
│   ├── layout.html         ← Chứa Sidebar, Navbar, Footer, Head
│   ├── password-input.html
│   ├── social-login.html
│   └── auth-topbar.html
│
├── auth/                   ← Giao diện đăng nhập / đăng ký
├── login.html
├── register.html
├── forgot-password.html
├── reset-password.html
├── verify-email.html
│
├── admin/                  ← Giao diện dành riêng cho ADMIN
│   ├── exam/
│   │   ├── create.html     ← Form tạo đề thi mới
│   │   └── edit.html       ← Form chỉnh sửa đề thi
│   ├── question/
│   │   ├── list.html       ← Danh sách câu hỏi toàn hệ thống
│   │   ├── create.html     ← Tạo câu hỏi mới
│   │   └── edit.html       ← Chỉnh sửa câu hỏi
│   └── history/
│       ├── list.html       ← Danh sách tất cả lịch sử thi
│       └── user-detail.html← Lịch sử thi của 1 học sinh
│
├── staff/                  ← Giao diện dành riêng cho STAFF
│   ├── lesson-list.html    ← Danh sách bài học
│   └── lesson-form.html    ← Form thêm / sửa bài học
│
├── student/                ← Giao diện dành riêng cho HỌC SINH
│   ├── dashboard.html      ← Trang chủ sau đăng nhập
│   ├── profile.html        ← Hồ sơ cá nhân & tiến độ học
│   ├── exam/
│   │   ├── take-exam.html  ← Giao diện phòng thi (đếm ngược, câu hỏi)
│   │   └── result.html     ← Kết quả sau khi nộp bài
│   ├── history/            ← Lịch sử thi của cá nhân học sinh
│   ├── lessons/
│   │   ├── list.html       ← Kho bài học
│   │   └── detail.html     ← Nội dung chi tiết 1 bài học
│   ├── dictionary/
│   │   ├── index.html      ← Trang từ điển
│   │   └── bookmarks.html  ← Từ vựng đã lưu (Bookmarks)
│   └── flashcards/
│       └── review.html     ← Ôn tập Flashcard (SRS)
```

### 4.2. Hệ thống Fragments (Mảnh tái sử dụng)

File `fragments/layout.html` là **xương sống** của toàn bộ giao diện, chứa các component dùng đi dùng lại:

| Fragment | Cú pháp gọi | Mô tả |
|---|---|---|
| `head(title)` | `th:replace="~{fragments/layout :: head('Tiêu đề trang')}"` | Thẻ `<head>`, khai báo Bootstrap, Font Awesome, CSS |
| `navbar(user)` | `th:replace="~{fragments/layout :: navbar(${user})}"` | Thanh nav trên cùng, hiển thị tên người dùng, nút logout |
| `sidebar(user, activeMenu)` | `th:replace="~{fragments/layout :: sidebar(${user}, 'exams')}"` | Menu bên trái, tự đổi mục được highlight dựa vào `activeMenu` |
| `footer` | `th:replace="~{fragments/layout :: footer}"` | Chân trang |
| `scripts` | `th:replace="~{fragments/layout :: scripts}"` | Bootstrap JS và các script cuối trang |

> [!NOTE]
> **Cách Sidebar tự highlight:** Fragment `sidebar` nhận tham số `activeMenu` (ví dụ `'exams'`). Nó dùng `th:classappend="${activeMenu == 'exams' ? 'active' : ''}"` để thêm class `active` vào đúng mục menu đang được chọn, giúp người dùng biết mình đang ở trang nào.

### 4.3. Bảng ánh xạ URL → File Template → Controller

| URL (Đường dẫn truy cập) | File Template | Controller |
|---|---|---|
| `/exams` | `admin/exam/...` (list chung) | `ExamController.java` |
| `/admin/exams/create` | `admin/exam/create.html` | `AdminExamController.java` |
| `/admin/questions` | `admin/question/list.html` | `QuestionController.java` |
| `/admin/histories` | `admin/history/list.html` | `AdminHistoryController.java` |
| `/staff/lessons` | `staff/lesson-list.html` | `StaffLessonController.java` |
| `/dashboard` | `student/dashboard.html` | `StudentDashboardController.java` |
| `/student/exams/{id}/take` | `student/exam/take-exam.html` | `student/ExamController.java` |
| `/student/exams/{id}/result` | `student/exam/result.html` | `student/ExamController.java` |
| `/student/lessons` | `student/lessons/list.html` | `LearningContentController.java` |
| `/student/dictionary` | `student/dictionary/index.html` | `DictionaryController.java` |
| `/student/flashcards/review` | `student/flashcards/review.html` | `FlashcardSRSController.java` |
| `/student/profile` | `student/profile.html` | `ProfileController.java` |

---

## 5. Kịch bản Frontend - Giáo viên yêu cầu sửa Giao diện

### Kịch bản 6: "Hãy thêm cột 'Kỹ năng' vào bảng Danh sách Câu hỏi"

**Vị trí file:** `templates/admin/question/list.html`
- **Bước 1:** Tìm thẻ `<thead>` trong bảng và thêm 1 thẻ `<th>` mới:
  ```html
  <th>Kỹ năng</th>
  ```
- **Bước 2:** Tìm thẻ `<tbody>` với vòng lặp `th:each` và thêm ô dữ liệu:
  ```html
  <td>
      <span th:text="${question.skill}">READING</span>
  </td>
  ```

### Kịch bản 7: "Thêm màu badge cho từng loại Kỹ năng (LISTENING xanh, READING vàng...)"

**Vị trí file:** `templates/admin/question/list.html`
- Thay vì chỉ in ra text, dùng Thymeleaf `th:classappend` để gán màu theo điều kiện:
  ```html
  <span class="badge"
        th:classappend="${question.skill == 'LISTENING' ? 'bg-primary' :
                         question.skill == 'READING'   ? 'bg-warning text-dark' :
                         question.skill == 'SPEAKING'  ? 'bg-success' : 'bg-secondary'}"
        th:text="${question.skill}">
  </span>
  ```

### Kịch bản 8: "Thay đổi đồng hồ đếm ngược trong phòng thi từ màu đỏ sang màu xanh"

**Vị trí file:** `templates/student/exam/take-exam.html`
- Tìm thẻ `<div>` chứa đồng hồ với class `bg-danger`:
  ```html
  <!-- Cũ -->
  <div class="bg-danger text-white px-3 py-2 ...">
  <!-- Sửa -->
  <div class="bg-primary text-white px-3 py-2 ...">
  ```

### Kịch bản 9: "Làm sao để truyền thêm dữ liệu mới xuống View HTML?"

**Đây là câu hỏi nguyên lý quan trọng nhất về Frontend:**
1. Trong **Service**, tính toán hoặc lấy giá trị mới, set vào DTO (ví dụ: `ExamHistoryResponse`).
2. Trong **Controller**, dữ liệu đã được `model.addAttribute("histories", list)` rồi - không cần sửa gì nếu DTO đã có trường mới.
3. Trong **HTML**, truy cập bằng cú pháp: `th:text="${history.tenTruongMoi}"`.

> [!TIP]
> **Mẹo đọc code Thymeleaf nhanh:** Tìm `th:each` để thấy vòng lặp dữ liệu. Tìm `th:text` để thấy nơi dữ liệu được in ra. Tìm `th:href` hoặc `th:action` để thấy URL mà form/link đang trỏ đến.

