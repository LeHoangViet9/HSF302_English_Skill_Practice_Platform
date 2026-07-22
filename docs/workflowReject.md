# Workflow Reject nội dung

Tài liệu này mô tả luồng xử lý khi Admin từ chối Lesson hoặc Exam do Staff gửi lên, từ lúc Admin nhập lý do cho đến khi Staff tải lại trang và nhìn thấy trạng thái `REJECTED` cùng `rejectReason`.

> Hệ thống hiện không tự động "đẩy" hoặc redirect nội dung sang Staff. Admin cập nhật trạng thái trong database; khi Staff mở hoặc tải lại trang danh sách, hệ thống truy vấn lại dữ liệu và hiển thị kết quả mới.

## 1. Luồng Reject Lesson

```text
Admin nhập lý do và bấm Từ chối
        ↓
POST /admin/approvals/lessons/{id}/reject
        ↓
AdminApprovalController.rejectLesson()
        ↓
LessonService.rejectLesson()
        ↓
LessonRepository.save()
        ↓
Database:
approval_status = REJECTED
reject_reason = lý do Admin nhập
```

### 1.1. Form Admin gửi yêu cầu

Form từ chối Lesson nằm tại [admin/approval/list.html (line 99)](../src/main/resources/templates/admin/approval/list.html#L99).

Form gửi request:

```http
POST /admin/approvals/lessons/{id}/reject
```

Trong request có tham số `reason`, chứa lý do Admin nhập.

### 1.2. AdminApprovalController nhận request

Hàm [AdminApprovalController.rejectLesson() (line 44)](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L44):

```java
@PostMapping("/lessons/{id}/reject")
public String rejectLesson(
        @PathVariable Long id,
        @RequestParam String reason,
        RedirectAttributes redirectAttributes
) {
    lessonService.rejectLesson(id, reason);
    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Đã từ chối bài học."
    );
    return "redirect:/admin/approvals";
}
```

Controller truyền `id` và `reason` xuống `LessonService`. Lệnh redirect chỉ đưa Admin trở lại `/admin/approvals`, không redirect sang trang Staff.

### 1.3. LessonService cập nhật Lesson

Hàm [LessonService.rejectLesson() (line 77)](../src/main/java/com/edu/espp/service/LessonService.java#L77):

```java
public void rejectLesson(Long id, String reason) {
    Lesson lesson = getLessonById(id);
    lesson.setApprovalStatus(ApprovalStatus.REJECTED);
    lesson.setRejectReason(reason);
    lessonRepository.save(lesson);
}
```

Hai giá trị được cập nhật:

```java
approvalStatus = REJECTED
rejectReason = reason
```

Các trường dữ liệu được khai báo trong [Lesson.java (line 54)](../src/main/java/com/edu/espp/entity/Lesson.java#L54).

## 2. Lesson xuất hiện lại bên Staff

Khi Staff mở:

```http
GET /manage/lessons
```

Luồng xử lý:

```text
GET /manage/lessons
        ↓
LessonController.listLessons()
        ↓
LessonService.searchLessons()
        ↓
LessonRepository.searchLessons()
        ↓
Model: lessonPage
        ↓
staff/lesson-list.html
```

### 2.1. LessonController lấy danh sách

Hàm [LessonController.listLessons() (line 22)](../src/main/java/com/edu/espp/controller/LessonController.java#L22) gọi:

```java
lessonService.searchLessons(keyword, type, level, pageable)
```

Kết quả được đưa vào model:

```java
model.addAttribute("lessonPage", ...);
```

### 2.2. LessonService gọi Repository

Trong [LessonService.searchLessons() (line 59)](../src/main/java/com/edu/espp/service/LessonService.java#L59):

```java
return lessonRepository.searchLessons(
        normalizeKeyword(keyword),
        type,
        level,
        pageable
);
```

### 2.3. Repository lấy cả Lesson bị từ chối

Query [LessonRepository.searchLessons() (line 40)](../src/main/java/com/edu/espp/repository/LessonRepository.java#L40) không lọc theo `approvalStatus`, nên kết quả có thể chứa cả:

```text
PENDING
APPROVED
REJECTED
```

### 2.4. Giao diện Staff hiển thị

Template [staff/lesson-list.html (line 89)](../src/main/resources/templates/staff/lesson-list.html#L89) duyệt qua `lessonPage`. Badge từ chối nằm tại [line 97](../src/main/resources/templates/staff/lesson-list.html#L97):

```html
<span class="badge bg-danger"
      th:if="${lesson.approvalStatus != null
              and lesson.approvalStatus.name() == 'REJECTED'}"
      th:title="${lesson.rejectReason}">
    Từ chối
</span>
```

Hiện `rejectReason` chỉ nằm trong thuộc tính `title`, vì vậy Staff phải rê chuột vào badge mới thấy. Nếu muốn nhấn badge để mở lý do thì sửa đoạn này thành Bootstrap `collapse` hoặc modal.

## 3. Luồng Reject Exam

```text
Admin nhập lý do và bấm Từ chối
        ↓
POST /admin/approvals/exams/{id}/reject
        ↓
AdminApprovalController.rejectExam()
        ↓
ExamServiceImpl.rejectExam()
        ↓
ExamRepository.save()
        ↓
Database:
approval_status = REJECTED
reject_reason = lý do Admin nhập
```

### 3.1. Form Admin gửi yêu cầu

Form từ chối Exam nằm tại [admin/approval/list.html (line 187)](../src/main/resources/templates/admin/approval/list.html#L187).

Form gửi request:

```http
POST /admin/approvals/exams/{id}/reject
```

### 3.2. AdminApprovalController nhận request

Trong [AdminApprovalController.rejectExam() (line 59)](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L59):

```java
@PostMapping("/exams/{id}/reject")
public String rejectExam(
        @PathVariable Long id,
        @RequestParam String reason,
        RedirectAttributes redirectAttributes
) {
    examService.rejectExam(id, reason);
    redirectAttributes.addFlashAttribute(
            "successMessage",
            "Đã từ chối bài thi."
    );
    return "redirect:/admin/approvals";
}
```

### 3.3. ExamServiceImpl cập nhật Exam

Hàm [ExamServiceImpl.rejectExam() (line 223)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L223):

```java
@Override
@Transactional
public void rejectExam(Long examId, String reason) {
    Exam exam = examRepository.findById(examId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            EXAM_NOT_FOUND_MSG + " để từ chối"
                    ));

    exam.setApprovalStatus(ApprovalStatus.REJECTED);
    exam.setRejectReason(reason);
    examRepository.save(exam);
}
```

Các trường `approvalStatus` và `rejectReason` được khai báo trong [Exam.java (line 41)](../src/main/java/com/edu/espp/entity/Exam.java#L41).

## 4. Exam xuất hiện lại bên Staff

Khi Staff mở:

```http
GET /exams
```

Luồng xử lý:

```text
GET /exams
        ↓
ExamController.getAllExams()
        ↓
Kiểm tra người dùng có role STAFF
        ↓
includeAllStatuses = true
        ↓
ExamServiceImpl.searchExams()
        ↓
ExamRepository.findAll()
        ↓
ExamServiceImpl.convertToResponse()
        ↓
exam/list.html
```

### 4.1. ExamController xác định quyền Staff

Hàm [ExamController.getAllExams() (line 26)](../src/main/java/com/edu/espp/controller/ExamController.java#L26) kiểm tra role:

```java
boolean includeAllStatuses = user != null &&
        (user.getRole() == Role.ADMIN || user.getRole() == Role.STAFF);
```

Sau đó gọi:

```java
examService.searchExams(keyword, type, includeAllStatuses);
```

### 4.2. ExamServiceImpl lấy cả Exam bị từ chối

Trong [ExamServiceImpl.searchExams() (line 45)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L45):

```java
exams = includeAllStatuses
        ? examRepository.findAll()
        : examRepository.findByApprovalStatus(ApprovalStatus.APPROVED);
```

Với Staff, `includeAllStatuses = true`, nên `findAll()` trả về cả `PENDING`, `APPROVED` và `REJECTED`.

### 4.3. Chuyển Entity sang DTO

Hàm [ExamServiceImpl.convertToResponse() (line 233)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L233) truyền cả trạng thái và lý do:

```java
.approvalStatus(exam.getApprovalStatus())
.rejectReason(exam.getRejectReason())
```

### 4.4. Giao diện Staff hiển thị

Badge hiện tại nằm trong [exam/list.html (line 100)](../src/main/resources/templates/exam/list.html#L100):

```html
<span class="badge bg-danger"
      th:if="${exam.approvalStatus != null
              and exam.approvalStatus.name() == 'REJECTED'}"
      th:title="${exam.rejectReason}">
    Từ chối
</span>
```

Backend đã truyền đầy đủ `approvalStatus` và `rejectReason`. Muốn Staff nhấn badge để hiện lý do thì chỉ cần sửa phần giao diện trong `exam/list.html`.

## 5. Những file chính trong workflow

### Admin gửi Reject

- [admin/approval/list.html](../src/main/resources/templates/admin/approval/list.html)
- [AdminApprovalController.java](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java)

### Xử lý Lesson

- [LessonService.java](../src/main/java/com/edu/espp/service/LessonService.java)
- [LessonRepository.java](../src/main/java/com/edu/espp/repository/LessonRepository.java)
- [Lesson.java](../src/main/java/com/edu/espp/entity/Lesson.java)
- [LessonController.java](../src/main/java/com/edu/espp/controller/LessonController.java)
- [staff/lesson-list.html](../src/main/resources/templates/staff/lesson-list.html)

### Xử lý Exam

- [ExamService.java](../src/main/java/com/edu/espp/service/exam/ExamService.java)
- [ExamServiceImpl.java](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java)
- [ExamRepository.java](../src/main/java/com/edu/espp/repository/ExamRepository.java)
- [Exam.java](../src/main/java/com/edu/espp/entity/Exam.java)
- [ExamController.java](../src/main/java/com/edu/espp/controller/ExamController.java)
- [exam/list.html](../src/main/resources/templates/exam/list.html)

## 6. Tóm tắt

```text
Admin Reject
    ↓
Controller nhận id + reason
    ↓
Service đặt approvalStatus = REJECTED
    ↓
Service đặt rejectReason = reason
    ↓
Repository lưu database
    ↓
Staff mở/tải lại trang danh sách
    ↓
Controller và Service truy vấn lại dữ liệu
    ↓
Template đọc approvalStatus và rejectReason
    ↓
Hiển thị badge Từ chối và lý do
```
