-- ====================================================================
-- XÓA SẠCH DỮ LIỆU CŨ THEO THỨ TỰ PHẢI TRƯỚC TRÁI SAU (TRÁNH LỖI KHÓA NGOẠI)
-- ====================================================================

-- -- 1. Xóa các bảng con (bảng chứa khóa ngoại trỏ đi) trước
-- DELETE FROM exam_histories;
-- DELETE FROM questions;
-- DELETE FROM lesson_contents;
-- DELETE FROM lessons;
-- DELETE FROM exams;
-- DELETE FROM student_users;
-- DELETE FROM users;
--
-- -- 2. RESET LẠI GIÁ TRỊ ID TỰ TĂNG VỀ 0 ĐỂ BẮT ĐẦU TỪ 1
-- DBCC CHECKIDENT ('exam_histories', RESEED, 0);
-- DBCC CHECKIDENT ('questions', RESEED, 0);
-- DBCC CHECKIDENT ('lesson_contents', RESEED, 0);
-- DBCC CHECKIDENT ('lessons', RESEED, 0);
-- DBCC CHECKIDENT ('exams', RESEED, 0);
-- DBCC CHECKIDENT ('student_users', RESEED, 0);
-- DBCC CHECKIDENT ('users', RESEED, 0);

-- ====================================================================
-- 3. CHÈN DỮ LIỆU TÀI KHOẢN MẪU (Bảng users)
-- ====================================================================
INSERT INTO users (email, password_hash, full_name, role, status, created_at)
VALUES
    ('admin@gmail.com', '$2a$10$wY...', N'Quản Trị Viên', 'ADMIN', 'ACTIVE', GETDATE()),
    ('staff1@gmail.com', '$2a$10$wY...', N'Nhân Viên Hỗ Trợ 1', 'STAFF', 'ACTIVE', GETDATE()),
    ('staff2@gmail.com', '$2a$10$wY...', N'Nhân Viên Hỗ Trợ 2', 'STAFF', 'ACTIVE', GETDATE()),
    ('hocvien01@gmail.com', '$2a$10$wY...', N'Nguyễn Văn Học Viên', 'STUDENT', 'ACTIVE', GETDATE()),
    ('hocvien02@gmail.com', '$2a$10$wY...', N'Trần Thị Chăm Chỉ', 'STUDENT', 'ACTIVE', GETDATE()),
    ('hocvien03@gmail.com', '$2a$10$wY...', N'Lê Văn Lười', 'STUDENT', 'BANNED', GETDATE()),
    ('hocvien04@gmail.com', '$2a$10$wY...', N'Phạm Thị Tốt', 'STUDENT', 'ACTIVE', GETDATE()),
    ('hocvien05@gmail.com', '$2a$10$wY...', N'Vũ Ngọc Đẹp', 'STUDENT', 'ACTIVE', GETDATE());

-- ====================================================================
-- 4. CHÈN DỮ LIỆU TÀI KHOẢN HỌC VIÊN (Bảng student_users)
-- ====================================================================
INSERT INTO student_users (email, password_hash, full_name, phone, status, login_attempts, created_at, updated_at, is_deleted)
VALUES
    ('hocvien01@gmail.com', '$2a$10$wY...', N'Nguyễn Văn Học Viên', '0987654321', 'ACTIVE', 0, GETDATE(), GETDATE(), 0),
    ('hocvien02@gmail.com', '$2a$10$wY...', N'Trần Thị Chăm Chỉ', '0912345678', 'ACTIVE', 0, GETDATE(), GETDATE(), 0),
    ('hocvien03@gmail.com', '$2a$10$wY...', N'Lê Văn Lười', '0900000000', 'SUSPENDED', 5, GETDATE(), GETDATE(), 0),
    ('hocvien04@gmail.com', '$2a$10$wY...', N'Phạm Thị Tốt', '0922222222', 'ACTIVE', 0, GETDATE(), GETDATE(), 0),
    ('hocvien05@gmail.com', '$2a$10$wY...', N'Vũ Ngọc Đẹp', '0933333333', 'ACTIVE', 0, GETDATE(), GETDATE(), 0);

-- ====================================================================
-- 5. CHÈN DỮ LIỆU BÀI HỌC (Bảng lessons)
-- ====================================================================
INSERT INTO lessons (title, level, type, created_by, description, is_published, approval_status, created_at, updated_at)
VALUES
    (N'Thì hiện tại đơn (Present Simple)', 'A1', 'GRAMMAR', 2, N'Bài học về thì hiện tại đơn cơ bản, cách sử dụng và cấu trúc', 1, 'APPROVED', GETDATE(), GETDATE()),
    (N'Thì quá khứ đơn (Past Simple)', 'A2', 'GRAMMAR', 2, N'Bài học về thì quá khứ đơn, động từ bất quy tắc', 1, 'APPROVED', GETDATE(), GETDATE()),
    (N'Từ vựng chủ đề Gia đình (Family)', 'A1', 'VOCABULARY', 3, N'Các từ vựng cơ bản về các thành viên trong gia đình', 1, 'APPROVED', GETDATE(), GETDATE()),
    (N'Từ vựng chủ đề Công việc (Jobs)', 'B1', 'VOCABULARY', 3, N'Các từ vựng phổ biến mô tả nghề nghiệp và nơi làm việc', 1, 'APPROVED', GETDATE(), GETDATE()),
    (N'Phát âm đuôi -s, -es', 'A2', 'PRONUNCIATION', 2, N'Cách phát âm đuôi s, es chuẩn xác trong tiếng Anh', 1, 'APPROVED', GETDATE(), GETDATE()),
    (N'Câu điều kiện loại 1, 2', 'B1', 'GRAMMAR', 2, N'Ngữ pháp câu điều kiện loại 1 và loại 2, cách áp dụng', 1, 'APPROVED', GETDATE(), GETDATE()),
    (N'Phát âm đuôi -ed', 'A2', 'PRONUNCIATION', 3, N'Quy tắc phát âm đuôi -ed với các động từ có quy tắc', 1, 'APPROVED', GETDATE(), GETDATE()),
    (N'Mệnh đề quan hệ (Relative Clauses)', 'B2', 'GRAMMAR', 2, N'Cách sử dụng đại từ quan hệ who, whom, which, that', 1, 'APPROVED', GETDATE(), GETDATE());

-- ====================================================================
-- 6. CHÈN NỘI DUNG BÀI HỌC (Bảng lesson_contents)
-- ====================================================================
-- Bài 1 (Thì hiện tại đơn) - lesson_id = 1
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES
    (1, N'S + V(s/es) + O', NULL, N'Cấu trúc khẳng định', 1, N'Dùng để diễn tả hành động lặp đi lặp lại hoặc sự thật hiển nhiên.', N'I go to school every day.'),
    (1, N'S + do/does + not + V + O', NULL, N'Cấu trúc phủ định', 2, N'Sử dụng trợ động từ do (với I, You, We, They) hoặc does (với He, She, It).', N'She does not (doesn''t) like apples.'),
    (1, N'Do/Does + S + V + O?', NULL, N'Cấu trúc nghi vấn', 3, N'Đảo trợ động từ lên trước chủ ngữ để tạo câu hỏi.', N'Do you play soccer?');

-- Bài 2 (Thì quá khứ đơn) - lesson_id = 2
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES
    (2, N'S + V(ed)/V(cot 2) + O', NULL, N'Cấu trúc khẳng định', 1, N'Động từ theo quy tắc thêm -ed, bất quy tắc học thuộc cột 2.', N'He visited his grandparents yesterday.'),
    (2, N'S + did + not + V(nguyên mẫu) + O', NULL, N'Cấu trúc phủ định', 2, N'Dùng trợ động từ did cho tất cả các ngôi.', N'We didn''t go to the cinema last night.'),
    (2, N'Yesterday, last week, ago', NULL, N'Dấu hiệu nhận biết', 3, N'Các trạng từ chỉ thời gian trong quá khứ.', N'I met her two days ago.');

-- Bài 3 (Từ vựng Gia đình) - lesson_id = 3
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES
    (3, N'Father', N'/''fɑːðər/', N'Bố, cha', 1, N'Người cha trong gia đình.', N'My father is a doctor.'),
    (3, N'Mother', N'/''mʌðər/', N'Mẹ', 2, N'Người mẹ trong gia đình.', N'Her mother is cooking.'),
    (3, N'Brother', N'/''brʌðər/', N'Anh/em trai', 3, N'Anh hoặc em trai.', N'I have a younger brother.'),
    (3, N'Sister', N'/''sɪstər/', N'Chị/em gái', 4, N'Chị hoặc em gái.', N'My sister is 5 years old.'),
    (3, N'Parents', N'/''peərənts/', N'Bố mẹ', 5, N'Cả bố và mẹ.', N'My parents are very strict.');

-- Bài 4 (Từ vựng Công việc) - lesson_id = 4
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES
    (4, N'Teacher', N'/''tiːtʃər/', N'Giáo viên', 1, N'Người dạy học tại trường.', N'She is an English teacher.'),
    (4, N'Doctor', N'/''dɒktər/', N'Bác sĩ', 2, N'Người chữa bệnh tại bệnh viện.', N'The doctor is examining a patient.'),
    (4, N'Engineer', N'/ˌendʒɪ''nɪər/', N'Kỹ sư', 3, N'Người thiết kế, xây dựng máy móc hoặc công trình.', N'He works as a software engineer.'),
    (4, N'Accountant', N'/ə''kaʊntənt/', N'Kế toán', 4, N'Người làm việc với sổ sách, tài chính.', N'My uncle is an accountant.');

-- ====================================================================
-- 7. CHÈN DỮ LIỆU ĐỀ THI (Bảng exams)
-- ====================================================================
INSERT INTO exams (title, type, duration, total_questions, description, approval_status)
VALUES
    (N'Đề thi thử Tiếng Anh B1 - Đề số 1', 'MOCK_TEST', 60, 10, N'Đề thi thử 4 kỹ năng chuẩn B1, bao gồm Nghe, Đọc, Nói, Viết', 'APPROVED'),
    (N'Bài kiểm tra nhanh kỹ năng Reading', 'QUIZ', 15, 6, N'Kiểm tra kỹ năng đọc hiểu văn bản ngắn', 'APPROVED'),
    (N'Đề thi thử Tiếng Anh A2 - Đề số 1', 'MOCK_TEST', 45, 10, N'Đề thi thử 4 kỹ năng chuẩn A2 cho người mới bắt đầu', 'APPROVED'),
    (N'Kiểm tra từ vựng Gia đình', 'QUIZ', 10, 5, N'Kiểm tra nhanh từ vựng bài 3', 'APPROVED'),
    (N'Kiểm tra Grammar - Thì hiện tại', 'QUIZ', 15, 5, N'Bài tập tổng hợp về thì hiện tại đơn và hiện tại tiếp diễn', 'APPROVED');

-- ====================================================================
-- 8. CHÈN CÂU HỎI (Bảng questions)
-- ====================================================================
-- --- ĐỀ 1 (exam_id = 1) ---
INSERT INTO questions (exam_id, skill, question_text, audio_url, options, correct_answer, explanation)
VALUES
    (1, 'LISTENING', N'Listen to the conversation. Where is the woman going?', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', N'{"A":"Supermarket","B":"School","C":"Hospital","D":"Airport"}', 'A', N'Người phụ nữ nói "I need to buy some food", suy ra chọn Supermarket.'),
    (1, 'LISTENING', N'What time will the train leave?', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3', N'{"A":"7:15 AM","B":"7:30 AM","C":"8:00 AM","D":"8:15 AM"}', 'B', N'Người thông báo nói "The train bound for London departs at thirty past seven".'),
    (1, 'LISTENING', N'Why is the man calling the customer service?', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3', N'{"A":"To cancel an order","B":"To complain about a broken item","C":"To change delivery address"}', 'B', N'Man mentions: "The laptop arrived but the screen is cracked."'),
    (1, 'READING', N'Read the text: "Regular exercise improves both physical and mental health." What is the main benefit mentioned?', NULL, N'{"A":"Weight loss only","B":"Physical and mental health improvement","C":"Better sleeping habits"}', 'B', N'Văn bản ghi rõ "improves both physical and mental health".'),
    (1, 'READING', N'According to the notice, what must employees do before leaving the office?', NULL, N'{"A":"Turn off all computers and lights","B":"Lock the main entrance","C":"Submit daily reports"}', 'A', N'Notice states: "Ensure all workstations and lights are powered off before departure."'),
    (1, 'READING', N'Choose the word that best fits the blank: "She has been working hard to _______ her English speaking skills."', NULL, N'{"A":"decrease","B":"improve","C":"ignore","D":"refuse"}', 'B', N'Dựa vào ngữ cảnh "working hard" (làm việc chăm chỉ) chọn "improve" (cải thiện).'),
    (1, 'READING', N'What does the word "prohibit" mean in the sentence: "Smoking is strictly prohibited inside the building."?', NULL, N'{"A":"Allowed","B":"Banned","C":"Encouraged"}', 'B', N'"Prohibited" nghĩa là bị cấm, đồng nghĩa với "Banned".'),
    (1, 'WRITING', N'Complete the second sentence: "It is not necessary for you to finish this today." -> You don''t...', NULL, NULL, 'have to finish this today', N'Cấu trúc "not necessary" chuyển thành "don''t have to".'),
    (1, 'WRITING', N'Rewrite the sentence in passive voice: "The teacher correct the essays." -> The essays...', NULL, NULL, 'are corrected by the teacher', N'Chuyển đổi sang câu bị động thì hiện tại đơn.'),
    (1, 'SPEAKING', N'Talk about your favorite hobby for 1 minute.', NULL, NULL, 'MOCK_ANSWER', N'Câu hỏi tự luận không có đáp án cố định.');

-- --- ĐỀ 2 (exam_id = 2) ---
INSERT INTO questions (exam_id, skill, question_text, audio_url, options, correct_answer, explanation)
VALUES
    (2, 'READING', N'Learning a new language opens new career paths. What is a benefit?', NULL, N'{"A":"Better job opportunities","B":"Forgetting native culture","C":"Traveling for free"}', 'A', N'"New career paths" đồng nghĩa với "Better job opportunities".'),
    (2, 'READING', N'The library will be closed on public holidays. When can you NOT visit?', NULL, N'{"A":"On weekends","B":"On public holidays","C":"On weekdays"}', 'B', N'Nghĩa là thư viện đóng cửa vào ngày lễ.'),
    (2, 'READING', N'According to a study, reading books 15 minutes a day can significantly reduce stress. How long should you read daily to lower stress?', NULL, N'{"A":"15 minutes","B":"1 hour","C":"5 minutes"}', 'A', N'Đoạn văn nêu rõ mốc thời gian là "15 minutes a day".'),
    (2, 'READING', N'Identify the synonym of "furious" in: "The customer was furious about the delayed shipment."', NULL, N'{"A":"Very happy","B":"Very angry","C":"Calm","D":"Disappointed"}', 'B', N'"Furious" nghĩa là cực kỳ tức giận.'),
    (2, 'READING', N'Fill in the blank: "If it rains tomorrow, we _______ the picnic."', NULL, N'{"A":"will cancel","B":"would cancel","C":"cancelled"}', 'A', N'Câu điều kiện loại 1 (vế If hiện tại đơn, vế sau tương lai đơn).'),
    (2, 'READING', N'What is the main topic of a text discussing renewable energy like solar and wind power?', NULL, N'{"A":"Fossil fuels","B":"Green energy solutions","C":"Nuclear weapon"}', 'B', N'Năng lượng mặt trời và gió thuộc "Green energy solutions" (giải pháp năng lượng xanh).');

-- --- ĐỀ 4 (exam_id = 4) ---
INSERT INTO questions (exam_id, skill, question_text, audio_url, options, correct_answer, explanation)
VALUES
    (4, 'READING', N'What do you call your father''s brother?', NULL, N'{"A":"Uncle","B":"Aunt","C":"Cousin","D":"Nephew"}', 'A', N'Anh/em trai của bố là chú/bác (Uncle).'),
    (4, 'READING', N'What is the meaning of "Siblings"?', NULL, N'{"A":"Parents","B":"Brothers and sisters","C":"Grandparents","D":"Friends"}', 'B', N'Siblings nghĩa là anh chị em ruột.'),
    (4, 'READING', N'Who is the mother of your father?', NULL, N'{"A":"Aunt","B":"Grandmother","C":"Sister","D":"Niece"}', 'B', N'Mẹ của bố là bà (Grandmother).'),
    (4, 'READING', N'What do you call the son of your brother?', NULL, N'{"A":"Nephew","B":"Niece","C":"Cousin","D":"Uncle"}', 'A', N'Con trai của anh/em trai là cháu trai (Nephew).'),
    (4, 'READING', N'What do you call the daughter of your aunt?', NULL, N'{"A":"Niece","B":"Sister","C":"Cousin","D":"Nephew"}', 'C', N'Con gái của cô/dì/chú/bác là anh chị em họ (Cousin).');

-- --- ĐỀ 5 (exam_id = 5) ---
INSERT INTO questions (exam_id, skill, question_text, audio_url, options, correct_answer, explanation)
VALUES
    (5, 'READING', N'She _______ to school every day.', NULL, N'{"A":"go","B":"goes","C":"going","D":"went"}', 'B', N'Thì hiện tại đơn, chủ ngữ số ít "She" động từ thêm -es.'),
    (5, 'READING', N'They _______ English right now.', NULL, N'{"A":"study","B":"studies","C":"are studying","D":"studied"}', 'C', N'Thì hiện tại tiếp diễn do có dấu hiệu "right now".'),
    (5, 'READING', N'He _______ like coffee. He prefers tea.', NULL, N'{"A":"don''t","B":"doesn''t","C":"isn''t","D":"aren''t"}', 'B', N'Phủ định thì hiện tại đơn với chủ ngữ số ít dùng doesn''t.'),
    (5, 'READING', N'_______ you play tennis at the weekend?', NULL, N'{"A":"Do","B":"Does","C":"Are","D":"Is"}', 'A', N'Câu hỏi thì hiện tại đơn với chủ ngữ "you" dùng trợ động từ "Do".'),
    (5, 'READING', N'Look! The dog _______ after the cat.', NULL, N'{"A":"run","B":"runs","C":"is running","D":"ran"}', 'C', N'Có từ "Look!" nên dùng thì hiện tại tiếp diễn.');


-- ====================================================================
-- 9. CHÈN LỊCH SỬ LÀM BÀI MẪU (Bảng exam_histories)
-- ====================================================================
INSERT INTO exam_histories (user_id, exam_id, score, correct_answers_count, time_spent, tested_at)
VALUES
    (4, 1, 8.5, 8, 2500, GETDATE()),
    (4, 1, 5.0, 5, 3200, DATEADD(day, -1, GETDATE())),
    (4, 2, 9.0, 5, 800, DATEADD(day, -3, GETDATE())),
    (4, 5, 10.0, 5, 650, DATEADD(day, -5, GETDATE())),
    (5, 1, 7.0, 7, 2800, DATEADD(hour, -2, GETDATE())),
    (5, 4, 8.0, 4, 500, DATEADD(hour, -12, GETDATE())),
    (7, 2, 10.0, 6, 400, DATEADD(day, -2, GETDATE())),
    (8, 5, 6.0, 3, 700, DATEADD(hour, -5, GETDATE()));


-- ====================================================================
-- KẾT THÚC
-- ====================================================================

