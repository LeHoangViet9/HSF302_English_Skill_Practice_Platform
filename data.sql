-- ====================================================================
-- XÓA SẠCH DỮ LIỆU CŨ ĐỂ KHÔNG BỊ TRÙNG LẶP KHÓA
-- ====================================================================
-- (Tắt ràng buộc khóa ngoại tạm thời để xóa cho mượt nếu có dữ liệu cũ kẹt)
-- ====================================================================
-- XÓA SẠCH DỮ LIỆU CŨ THEO THỨ TỰ PHẢI TRƯỚC TRÁI SAU (TRÁNH LỖI KHÓA NGOẠI)
-- ====================================================================

-- 1. Xóa các bảng con (bảng chứa khóa ngoại trỏ đi) trước
DELETE FROM exam_histories;
DELETE FROM support_tickets;
DELETE FROM questions;

-- 2. Xóa các bảng cha (bảng gốc) sau
DELETE FROM exams;
DELETE FROM users;

-- ====================================================================
-- RESET LẠI GIÁ TRỊ ID TỰ TĂNG (IDENTITY) VỀ 0 ĐỂ BẮT ĐẦU TỪ 1
-- ====================================================================
DBCC CHECKIDENT ('exam_histories', RESEED, 0);
DBCC CHECKIDENT ('support_tickets', RESEED, 0);
DBCC CHECKIDENT ('questions', RESEED, 0);
DBCC CHECKIDENT ('exams', RESEED, 0);
DBCC CHECKIDENT ('users', RESEED, 0);

-- ====================================================================
-- 1. CHÈN DỮ LIỆU TÀI KHOẢN MẪU (Bảng users)
-- ====================================================================
INSERT INTO users (email, password_hash, full_name, role, status, created_at)
VALUES
    ('hocvien01@gmail.com', 'hashed_password_123', N'Nguyễn Văn Học Viên', 'STUDENT', 'ACTIVE', GETDATE()),
    ('hocvien02@gmail.com', 'hashed_password_456', N'Trần Thị Chăm Chỉ', 'STUDENT', 'ACTIVE', GETDATE());

-- ====================================================================
-- 2. CHÈN DỮ LIỆU ĐỀ THI (Bảng exams)
-- ====================================================================
INSERT INTO exams (title, type, duration, total_questions)
VALUES
    (N'Đề thi thử Tiếng Anh B1 - Đề số 1', 'MOCK_TEST', 60, 10),
    (N'Bài kiểm tra nhanh kỹ năng Reading', 'QUIZ', 15, 6);


-- 1. Tìm và xóa hết các token liên quan của thằng học viên này ở bảng con trước
DELETE FROM auth_tokens
WHERE student_id = (SELECT auth_tokens.student_id FROM student_users WHERE email = 'leviet20051509@gmail.com');

-- 2. Giờ thằng học viên đã "sạch nợ", xóa thoải mái ở bảng mẹ
DELETE FROM student_users
WHERE email = 'leviet20051509@gmail.com';

select * from users
-- ====================================================================
-- 3. CHÈN CÂU HỎI (Bảng questions)
-- ====================================================================

-- --- ĐỀ SỐ 1 (exam_id = 1): 10 CÂU ĐẦY ĐỦ 4 KỸ NĂNG ---
-- ====================================================================
-- 3. CHÈN CÂU HỎI (Bảng questions) - KHÔNG CHÈN COMMENT TRONG VALUES
-- ====================================================================

-- --- ĐỀ SỐ 1 (exam_id = 1): 10 CÂU ĐẦY ĐỦ 4 KỸ NĂNG ---
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


-- --- ĐỀ SỐ 2 (exam_id = 2): 6 CÂU CHUYÊN READING ---
INSERT INTO questions (exam_id, skill, question_text, audio_url, options, correct_answer, explanation)
VALUES
    (2, 'READING', N'Read the text: "Learning a new language opens new career paths and broadens cultural understanding." What is a benefit of learning a language?', NULL, N'{"A":"Better job opportunities","B":"Forgetting native culture","C":"Traveling for free"}', 'A', N'"New career paths" đồng nghĩa với "Better job opportunities".'),
    (2, 'READING', N'Read the sentence: "The library will be closed on public holidays." When can you NOT visit the library?', NULL, N'{"A":"On weekends","B":"On public holidays","C":"On weekdays"}', 'B', N'"Closed on public holidays" nghĩa là thư viện đóng cửa vào các ngày lễ.'),
    (2, 'READING', N'According to a study, reading books 15 minutes a day can significantly reduce stress. How long should you read daily to lower stress?', NULL, N'{"A":"15 minutes","B":"1 hour","C":"5 minutes"}', 'A', N'Đoạn văn nêu rõ mốc thời gian là "15 minutes a day".'),
    (2, 'READING', N'Identify the synonym of the word "furious" in the context: "The customer was furious about the delayed shipment."', NULL, N'{"A":"Very happy","B":"Very angry","C":"Calm","D":"Disappointed"}', 'B', N'"Furious" nghĩa là cực kỳ tức giận, đồng nghĩa với "Very angry".'),
    (2, 'READING', N'Fill in the blank: "If it rains tomorrow, we _______ the picnic."', NULL, N'{"A":"will cancel","B":"would cancel","C":"cancelled"}', 'A', N'Câu điều kiện loại 1 (vế If hiện tại đơn, vế sau tương lai đơn).'),
    (2, 'READING', N'What is the main topic of a text discussing renewable energy like solar and wind power?', NULL, N'{"A":"Fossil fuels","B":"Green energy solutions","C":"Nuclear weapon"}', 'B', N'Năng lượng mặt trời và gió thuộc "Green energy solutions" (giải pháp năng lượng xanh).');
-- ====================================================================
-- 4. CHÈN LỊCH SỬ LÀM BÀI MẪU (Bảng exam_histories)
-- ====================================================================
INSERT INTO exam_histories (user_id, exam_id, score, correct_answers_count, time_spent, tested_at)
VALUES
    (1, 1, 8.5, 8, 2500, GETDATE()),
    (1, 1, 5.0, 5, 3200, DATEADD(day, -1, GETDATE())),
    (1, 2, 9.0, 5, 800, DATEADD(day, -3, GETDATE())),
    (1, 2, 10.0, 6, 650, DATEADD(day, -5, GETDATE())),
    (2, 1, 7.0, 7, 2800, DATEADD(hour, -2, GETDATE()));


-- ====================================================================
-- 5. CHÈN VÉ HỖ TRỢ / BÁO CÁO LỖI (Bảng support_tickets)
-- ====================================================================
INSERT INTO support_tickets (user_id, title, description, status, created_at)
VALUES
    (1, N'Lỗi không phát được file Audio', N'Khi em bấm vào bài Nghe của Đề số 1, trình phát nhạc hiển thị nhưng không phát được âm thanh.', 'OPEN', GETDATE()),
    (1, N'Lỗi Micro không nhận diện', N'Hệ thống không kích hoạt được Micro của em ở phần thi Nói, mặc dù em đã cấp quyền trình duyệt.', 'RESOLVED', DATEADD(day, -2, GETDATE())),
    (1, N'Sai đáp án câu hỏi số 4 đề Reading', N'Em thấy giải thích ghi chọn B nhưng hệ thống chấm điểm lại tính đáp án C là đúng, phiền thầy cô xem lại.', 'PENDING', DATEADD(hour, -4, GETDATE())),
    (2, N'Không bấm được nút Nộp bài', N'Mạng nhà em hoàn toàn bình thường nhưng lúc ấn nút Nộp bài cứ bị xoay vòng tròn không dừng.', 'OPEN', DATEADD(day, -1, GETDATE()));

UPDATE support_tickets
SET status = 'IN_PROGRESS'
WHERE status = 'PENDING';