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
