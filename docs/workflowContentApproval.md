# Workflow Staff gửi nội dung để Admin duyệt

Tài liệu mô tả luồng Lesson và Exam từ frontend của Staff, qua trạng thái `PENDING`, đến khi Admin Approve hoặc Reject và kết quả được hiển thị cho Staff/Student.

> Project không có role `MANAGER`. Chức năng duyệt thuộc `/admin/**` và role `ADMIN`, nên Manager trong tài liệu này chính là Admin.

## 1. Tổng quan

```text
Staff nhập Lesson hoặc Exam
    ↓ POST /manage/...
Controller nhận form
    ↓
Service lấy user đăng nhập
    ↓
createdBy = Staff hiện tại
approvalStatus = PENDING
    ↓ Repository.save()
Admin mở /admin/approvals
    ↓ chỉ tải nội dung PENDING
    ├── Approve → APPROVED
    │      ├── Staff thấy “Đã duyệt”
    │      └── Student được xem
    └── Reject(reason) → REJECTED + rejectReason
           ├── Staff thấy lý do
           └── Student bị ẩn và chặn
```

## 2. Phân quyền

Trang duyệt chỉ dành cho Admin tại [SecurityConfig.java (line 29)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L29):

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

Lưu ý: các URL `/manage/**` hiện chỉ rơi xuống [`anyRequest().authenticated()` (line 47)](../src/main/java/com/edu/espp/config/SecurityConfig.java#L47), chưa bị giới hạn riêng cho ADMIN/STAFF. Student đăng nhập có thể thử gọi trực tiếp URL quản lý dù UI không hiện nút.

## 3. Staff tạo Lesson từ frontend

Staff mở `GET /manage/lessons/create`. [LessonController.showCreateForm() (line 44)](../src/main/java/com/edu/espp/controller/LessonController.java#L44) tạo Lesson rỗng và render [staff/lesson-form.html](../src/main/resources/templates/staff/lesson-form.html).

Form nằm tại [staff/lesson-form.html (line 27)](../src/main/resources/templates/staff/lesson-form.html#L27) và gửi:

```http
POST /manage/lessons/save
```

Các field chính:

- ID ẩn: [line 28](../src/main/resources/templates/staff/lesson-form.html#L28).
- Tiêu đề: [line 34](../src/main/resources/templates/staff/lesson-form.html#L34).
- Loại: [line 41](../src/main/resources/templates/staff/lesson-form.html#L41).
- Cấp độ: [line 51](../src/main/resources/templates/staff/lesson-form.html#L51).
- Mô tả: [line 61](../src/main/resources/templates/staff/lesson-form.html#L61).
- Nút lưu: [line 66](../src/main/resources/templates/staff/lesson-form.html#L66).

[LessonController.saveLesson() (line 53)](../src/main/java/com/edu/espp/controller/LessonController.java#L53) gọi `lessonService.saveLesson(lesson)`. Nếu là Lesson mới, controller redirect tới trang edit ở [line 57](../src/main/java/com/edu/espp/controller/LessonController.java#L57) để Staff thêm content.

## 4. LessonService đặt PENDING

[LessonService.saveLesson() (line 39)](../src/main/java/com/edu/espp/service/LessonService.java#L39) chỉ khởi tạo trạng thái khi `lesson.id == null`.

Service lấy Authentication tại [line 41](../src/main/java/com/edu/espp/service/LessonService.java#L41), tìm User theo email và đặt `createdBy` tại [line 44](../src/main/java/com/edu/espp/service/LessonService.java#L44).

Quy tắc trạng thái tại [line 45](../src/main/java/com/edu/espp/service/LessonService.java#L45):

| Người tạo | Trạng thái ban đầu |
|---|---|
| Admin | `APPROVED` |
| Staff | `PENDING` |

Sau đó [line 52](../src/main/java/com/edu/espp/service/LessonService.java#L52) gọi `lessonRepository.save(lesson)`.

### Staff thêm LessonContent

Nút thêm content nằm tại [staff/lesson-form.html (line 82)](../src/main/resources/templates/staff/lesson-form.html#L82).

`POST /manage/lessons/{lessonId}/contents/save` đi vào [LessonController.saveContent() (line 101)](../src/main/java/com/edu/espp/controller/LessonController.java#L101), rồi [LessonService.saveLessonContent() (line 93)](../src/main/java/com/edu/espp/service/LessonService.java#L93) gắn content vào Lesson và lưu.

Không có nút “Gửi duyệt” riêng. Lesson đã `PENDING` ngay khi lưu thông tin cơ bản, nên Admin có thể thấy nó trước khi Staff hoàn thiện content.

## 5. Staff tạo Exam từ frontend

Nút tạo Exam nằm tại [exam/list.html (line 53)](../src/main/resources/templates/exam/list.html#L53) và mở `GET /manage/exams/create`.

[AdminExamController.showCreateForm() (line 28)](../src/main/java/com/edu/espp/controller/admin/AdminExamController.java#L28) render [admin/exam/create.html](../src/main/resources/templates/admin/exam/create.html). Controller mang tên Admin nhưng hiện cũng phục vụ Staff.

Form tại [admin/exam/create.html (line 24)](../src/main/resources/templates/admin/exam/create.html#L24) gửi:

```http
POST /manage/exams
```

[AdminExamController.createExam() (line 35)](../src/main/java/com/edu/espp/controller/admin/AdminExamController.java#L35) validate `ExamRequest` và gọi `examService.createExam()` tại [line 40](../src/main/java/com/edu/espp/controller/admin/AdminExamController.java#L40).

## 6. ExamService đặt PENDING

[ExamServiceImpl.createExam() (line 147)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L147) lấy user hiện tại.

Status mặc định là `PENDING` tại [line 153](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L153). Nếu user có `ROLE_ADMIN`, status đổi thành `APPROVED`.

Entity được gắn `createdBy` và `approvalStatus` tại [line 168](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L168), rồi lưu qua ExamRepository.

| Người tạo | Trạng thái ban đầu |
|---|---|
| Admin | `APPROVED` |
| Staff | `PENDING` |

## 7. Admin tải hàng chờ duyệt

`GET /admin/approvals` đi vào [AdminApprovalController.listPendingApprovals() (line 21)](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L21).

Controller gọi tại [line 31](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L31):

```java
lessonService.getPendingLessons(pageable);
examService.getPendingExams(pageable);
```

- [LessonService.getPendingLessons() (line 30)](../src/main/java/com/edu/espp/service/LessonService.java#L30) gọi `LessonRepository.findByApprovalStatus(PENDING)`.
- [ExamServiceImpl.getPendingExams() (line 209)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L209) gọi `ExamRepository.findByApprovalStatus(PENDING)`.

Kết quả render tại [admin/approval/list.html](../src/main/resources/templates/admin/approval/list.html).

## 8. Admin Approve Lesson

Form Approve tại [admin/approval/list.html (line 78)](../src/main/resources/templates/admin/approval/list.html#L78) gửi:

```http
POST /admin/approvals/lessons/{id}/approve
```

[AdminApprovalController.approveLesson() (line 37)](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L37) gọi [LessonService.approveLesson() (line 71)](../src/main/java/com/edu/espp/service/LessonService.java#L71):

```java
lesson.setApprovalStatus(APPROVED);
lessonRepository.save(lesson);
```

## 9. Admin Reject Lesson

Modal Reject nằm tại [admin/approval/list.html (line 93)](../src/main/resources/templates/admin/approval/list.html#L93). Admin nhập `reason` tại [line 111](../src/main/resources/templates/admin/approval/list.html#L111) và gửi:

```http
POST /admin/approvals/lessons/{id}/reject
```

[AdminApprovalController.rejectLesson() (line 44)](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L44) gọi [LessonService.rejectLesson() (line 77)](../src/main/java/com/edu/espp/service/LessonService.java#L77):

```java
lesson.setApprovalStatus(REJECTED);
lesson.setRejectReason(reason);
lessonRepository.save(lesson);
```

## 10. Admin Approve Exam

Form tại [admin/approval/list.html (line 166)](../src/main/resources/templates/admin/approval/list.html#L166) gửi `POST /admin/approvals/exams/{id}/approve`.

[AdminApprovalController.approveExam() (line 52)](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L52) gọi [ExamServiceImpl.approveExam() (line 216)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L216) để đặt `APPROVED` và lưu.

## 11. Admin Reject Exam

Modal tại [admin/approval/list.html (line 181)](../src/main/resources/templates/admin/approval/list.html#L181). Admin nhập `reason` tại [line 199](../src/main/resources/templates/admin/approval/list.html#L199).

[AdminApprovalController.rejectExam() (line 59)](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java#L59) gọi [ExamServiceImpl.rejectExam() (line 225)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L225):

```java
exam.setApprovalStatus(REJECTED);
exam.setRejectReason(reason);
examRepository.save(exam);
```

## 12. Kết quả bên Staff

Không có push hoặc redirect từ Admin về Staff. Khi Staff tải lại trang, dữ liệu được query lại từ database.

### Lesson

[LessonController.listLessons() (line 22)](../src/main/java/com/edu/espp/controller/LessonController.java#L22) render [staff/lesson-list.html](../src/main/resources/templates/staff/lesson-list.html):

- `PENDING`: [line 106](../src/main/resources/templates/staff/lesson-list.html#L106).
- `APPROVED`: [line 109](../src/main/resources/templates/staff/lesson-list.html#L109).
- `REJECTED`: nút tại [line 113](../src/main/resources/templates/staff/lesson-list.html#L113).
- Lý do Reject: collapse tại [line 132](../src/main/resources/templates/staff/lesson-list.html#L132).

### Exam

[ExamController.getAllExams() (line 26)](../src/main/java/com/edu/espp/controller/ExamController.java#L26) cho Admin/Staff xem mọi status.

[exam/list.html](../src/main/resources/templates/exam/list.html) hiển thị:

- `PENDING`: [line 121](../src/main/resources/templates/exam/list.html#L121).
- `APPROVED`: [line 126](../src/main/resources/templates/exam/list.html#L126).
- `REJECTED`: nút tại [line 130](../src/main/resources/templates/exam/list.html#L130).
- Lý do Reject: collapse tại [line 145](../src/main/resources/templates/exam/list.html#L145).

## 13. Kết quả bên Student

Lesson list chỉ lấy `APPROVED` qua query tại [LessonRepository.java (line 25)](../src/main/java/com/edu/espp/repository/LessonRepository.java#L25). URL chi tiết cũng dùng [LessonService.getApprovedLessonById() (line 107)](../src/main/java/com/edu/espp/service/LessonService.java#L107).

Exam list chỉ trả `APPROVED` cho Student trong [ExamServiceImpl.searchExams() (line 45)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L45). URL làm bài dùng [getApprovedExamById() (line 278)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L278).

| Status | Staff | Student |
|---|---|---|
| `PENDING` | Thấy “Chờ duyệt” | Không thấy/không truy cập |
| `APPROVED` | Thấy “Đã duyệt” | Thấy và truy cập |
| `REJECTED` | Thấy lý do | Không thấy/không truy cập |

## 14. Hạn chế của luồng hiện tại

### Không có DRAFT/Submit

Staff vừa tạo là `PENDING`; không có bước hoàn thiện rồi mới “Gửi duyệt”.

### Lesson sửa lại không tự PENDING

[LessonService.saveLesson()](../src/main/java/com/edu/espp/service/LessonService.java#L39) chỉ đặt status khi tạo mới. Sửa Lesson `REJECTED/APPROVED` hoặc thêm LessonContent không tự chuyển về `PENDING` và không xóa `rejectReason`.

### Exam sửa lại không tự PENDING

[ExamServiceImpl.updateExam() (line 184)](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java#L184) không đặt lại `PENDING` và không xóa `rejectReason`. Exam bị Reject sau khi Staff sửa chưa tự quay lại hàng chờ.

### Approve không xóa rejectReason

Hai hàm Approve chỉ đổi `approvalStatus`. Lý do Reject cũ có thể còn trong database.

### Không lọc theo createdBy

Danh sách quản lý Lesson/Exam của Staff không lọc người tạo, nên Staff có thể thấy nội dung của Staff khác.

## 15. File chính

### Tạo nội dung

- [staff/lesson-form.html](../src/main/resources/templates/staff/lesson-form.html)
- [LessonController.java](../src/main/java/com/edu/espp/controller/LessonController.java)
- [LessonService.java](../src/main/java/com/edu/espp/service/LessonService.java)
- [admin/exam/create.html](../src/main/resources/templates/admin/exam/create.html)
- [AdminExamController.java](../src/main/java/com/edu/espp/controller/admin/AdminExamController.java)
- [ExamServiceImpl.java](../src/main/java/com/edu/espp/service/exam/impl/ExamServiceImpl.java)

### Duyệt nội dung

- [admin/approval/list.html](../src/main/resources/templates/admin/approval/list.html)
- [AdminApprovalController.java](../src/main/java/com/edu/espp/controller/admin/AdminApprovalController.java)
- [LessonRepository.java](../src/main/java/com/edu/espp/repository/LessonRepository.java)
- [ExamRepository.java](../src/main/java/com/edu/espp/repository/ExamRepository.java)
- [ApprovalStatus.java](../src/main/java/com/edu/espp/common/enums/ApprovalStatus.java)

### Hiển thị kết quả

- [staff/lesson-list.html](../src/main/resources/templates/staff/lesson-list.html)
- [exam/list.html](../src/main/resources/templates/exam/list.html)
- [LearningContentController.java](../src/main/java/com/edu/espp/controller/student/LearningContentController.java)
- [student/ExamController.java](../src/main/java/com/edu/espp/controller/student/ExamController.java)

## 16. Tóm tắt

```text
Staff nhập frontend
    ↓ save
createdBy = Staff + PENDING
    ↓
Admin /admin/approvals
    ├── Approve → APPROVED → Student được xem
    └── Reject → REJECTED + reason → Staff xem lý do
```
