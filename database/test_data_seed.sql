USE EnglishLearningDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    -- ==========================================
    -- 1. TẮT TẠM THỜI RÀNG BUỘC KHÓA NGOẠI
    -- ==========================================
    ALTER TABLE book_marks NOCHECK CONSTRAINT ALL;
    ALTER TABLE srs_reviews NOCHECK CONSTRAINT ALL;
    ALTER TABLE learning_progress NOCHECK CONSTRAINT ALL;
    ALTER TABLE exam_histories NOCHECK CONSTRAINT ALL;
    ALTER TABLE questions NOCHECK CONSTRAINT ALL;
    ALTER TABLE exams NOCHECK CONSTRAINT ALL;

    -- ==========================================
    -- 2. XÓA SẠCH DỮ LIỆU CŨ ĐỂ KIỂM THỬ SẠCH SẼ
    -- ==========================================
    DELETE FROM book_marks;
    DELETE FROM srs_reviews;
    DELETE FROM learning_progress;
    DELETE FROM exam_histories;
    DELETE FROM questions;
    DELETE FROM exams;

    -- ==========================================
    -- 3. RESET LẠI IDENTITY VỀ 0
    -- ==========================================
    DBCC CHECKIDENT ('book_marks', RESEED, 0);
    DBCC CHECKIDENT ('srs_reviews', RESEED, 0);
    DBCC CHECKIDENT ('learning_progress', RESEED, 0);
    DBCC CHECKIDENT ('exam_histories', RESEED, 0);
    DBCC CHECKIDENT ('questions', RESEED, 0);
    DBCC CHECKIDENT ('exams', RESEED, 0);

    -- ==========================================
    -- 4. BẬT LẠI RÀNG BUỘC KHÓA NGOẠI
    -- ==========================================
    ALTER TABLE exams WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE questions WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE exam_histories WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE learning_progress WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE srs_reviews WITH CHECK CHECK CONSTRAINT ALL;
    ALTER TABLE book_marks WITH CHECK CHECK CONSTRAINT ALL;

    -- ==========================================
    -- 5. LẤY DỰ PHÒNG USER ID CHO HỌC VIÊN
    -- ==========================================
    DECLARE @studentId BIGINT;
    SELECT @studentId = id FROM users WHERE email = 'student@espp.com';

    DECLARE @adminId BIGINT;
    SELECT @adminId = id FROM users WHERE email = 'admin@espp.com';

    -- Đảm bảo có ít nhất các user kiểm thử này
    IF @adminId IS NULL
    BEGIN
        INSERT INTO users (email, password_hash, full_name, role, status, created_at, updated_at, login_attempts)
        VALUES ('admin@espp.com', '$2a$10$7QJ8GJr3X9V8wW7h9wOaUeVh6H0o9f1w2Y0c3h4s5p6q7r8t9u0vW', N'Admin ESPP', 'ADMIN', 'ACTIVE', GETDATE(), GETDATE(), 0);
        SET @adminId = SCOPE_IDENTITY();
    END;

    IF @studentId IS NULL
    BEGIN
        INSERT INTO users (email, password_hash, full_name, role, status, created_at, updated_at, login_attempts)
        VALUES ('student@espp.com', '$2a$10$7QJ8GJr3X9V8wW7h9wOaUeVh6H0o9f1w2Y0c3h4s5p6q7r8t9u0vW', N'Student ESPP', 'STUDENT', 'ACTIVE', GETDATE(), GETDATE(), 0);
        SET @studentId = SCOPE_IDENTITY();
    END;

    -- ==========================================
    -- 6. CHÈN DỮ LIỆU ĐỀ THI MẪU (Bảng exams)
    -- ==========================================
    INSERT INTO exams (title, type, duration, total_questions, description, approval_status)
    VALUES 
        (N'Đề thi thử Tiếng Anh B1 - Đề số 1', 'MOCK_TEST', 60, 4, N'Đề thi thử tổng hợp 4 kỹ năng theo cấu trúc chuẩn B1.', 'APPROVED'),
        (N'Bài kiểm tra nhanh kỹ năng Reading', 'QUIZ', 15, 2, N'Kiểm tra đọc hiểu nhanh các biển báo và thông báo ngắn.', 'APPROVED'),
        (N'Luyện tập Từ vựng A1 - Chào hỏi', 'QUIZ', 10, 1, N'Bài kiểm tra từ vựng cơ bản A1.', 'APPROVED');

    DECLARE @examB1 BIGINT = (SELECT id FROM exams WHERE title = N'Đề thi thử Tiếng Anh B1 - Đề số 1');
    DECLARE @examReading BIGINT = (SELECT id FROM exams WHERE title = N'Bài kiểm tra nhanh kỹ năng Reading');
    DECLARE @examGreeting BIGINT = (SELECT id FROM exams WHERE title = N'Luyện tập Từ vựng A1 - Chào hỏi');

    -- ==========================================
    -- 7. CHÈN DỮ LIỆU CÂU HỎI MẪU (Bảng questions)
    -- ==========================================
    INSERT INTO questions (exam_id, skill, question_text, audio_url, options, correct_answer, explanation)
    VALUES
        (@examB1, 'LISTENING', N'Listen to the conversation. Where is the woman going?', 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3', N'{"A":"Supermarket","B":"School","C":"Hospital","D":"Airport"}', 'A', N'Người phụ nữ nói "I need to buy some food", suy ra chọn Supermarket.'),
        (@examB1, 'READING', N'Read the text: "Regular exercise improves both physical and mental health." What is the main benefit mentioned?', NULL, N'{"A":"Weight loss only","B":"Physical and mental health improvement","C":"Better sleeping habits"}', 'B', N'Văn bản ghi rõ "improves both physical and mental health".'),
        (@examB1, 'WRITING', N'Complete the second sentence: "It is not necessary for you to finish this today." -> You don''t...', NULL, NULL, 'have to finish this today', N'Cấu trúc "not necessary" chuyển thành "don''t have to".'),
        (@examB1, 'SPEAKING', N'Talk about your favorite hobby for 1 minute.', NULL, NULL, 'MOCK_ANSWER', N'Câu hỏi tự luận không có đáp án cố định.'),
        (@examReading, 'READING', N'Read the sentence: "The library will be closed on public holidays." When can you NOT visit the library?', NULL, N'{"A":"On weekends","B":"On public holidays","C":"On weekdays"}', 'B', N'"Closed on public holidays" nghĩa là thư viện đóng cửa vào các ngày lễ.'),
        (@examReading, 'READING', N'Fill in the blank: "If it rains tomorrow, we _______ the picnic."', NULL, N'{"A":"will cancel","B":"would cancel","C":"cancelled"}', 'A', N'Câu điều kiện loại 1 (vế If hiện tại đơn, vế sau tương lai đơn).'),
        (@examGreeting, 'READING', N'Choose the correct response to: "Nice to meet you."', NULL, N'{"A":"Nice to meet you too","B":"Thank you","C":"I am fine"}', 'A', N'Đáp lại "Nice to meet you" bằng "Nice to meet you too".');

    -- ==========================================
    -- 8. CHÈN DỮ LIỆU LỊCH SỬ THI MẪU (Bảng exam_histories)
    -- ==========================================
    -- Chèn lịch sử cho cả Admin (ID 1) và Student (ID 3)
    IF @studentId IS NOT NULL
    BEGIN
        INSERT INTO exam_histories (user_id, exam_id, score, correct_answers_count, time_spent, tested_at)
        VALUES
            (@studentId, @examB1, 8.5, 3, 1200, DATEADD(DAY, -3, GETDATE())),
            (@studentId, @examReading, 9.0, 2, 600, DATEADD(DAY, -1, GETDATE())),
            (@studentId, @examGreeting, 10.0, 1, 300, DATEADD(DAY, -5, GETDATE()));
    END;

    IF @adminId IS NOT NULL
    BEGIN
        INSERT INTO exam_histories (user_id, exam_id, score, correct_answers_count, time_spent, tested_at)
        VALUES
            (@adminId, @examB1, 8.5, 3, 1200, DATEADD(DAY, -3, GETDATE())),
            (@adminId, @examReading, 9.0, 2, 600, DATEADD(DAY, -1, GETDATE())),
            (@adminId, @examGreeting, 10.0, 1, 300, DATEADD(DAY, -5, GETDATE()));
    END;

    -- ==========================================
    -- 9. LẤY ID CÁC BÀI HỌC VÀ NỘI DUNG ĐỂ SEED TIẾN ĐỘ & FLASHCARD
    -- ==========================================
    DECLARE @l1 BIGINT = (SELECT TOP 1 id FROM lessons WHERE title LIKE N'%A1 Greetings and Self Introduction%');
    DECLARE @l2 BIGINT = (SELECT TOP 1 id FROM lessons WHERE title LIKE N'%A1 Daily Vocabulary%');
    DECLARE @l3 BIGINT = (SELECT TOP 1 id FROM lessons WHERE title LIKE N'%A2 Present Simple%');
    DECLARE @l5 BIGINT = (SELECT TOP 1 id FROM lessons WHERE title LIKE N'%B1 IPA Short and Long Vowels%');

    -- Chèn tiến độ học tập mẫu
    IF @studentId IS NOT NULL
    BEGIN
        IF @l1 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@studentId, @l1, 1, DATEADD(DAY, -5, GETDATE()));
        IF @l2 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@studentId, @l2, 1, DATEADD(DAY, -3, GETDATE()));
        IF @l3 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@studentId, @l3, 0, GETDATE());
        IF @l5 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@studentId, @l5, 1, DATEADD(DAY, -1, GETDATE()));
    END;

    IF @adminId IS NOT NULL
    BEGIN
        IF @l1 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@adminId, @l1, 1, DATEADD(DAY, -5, GETDATE()));
        IF @l2 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@adminId, @l2, 1, DATEADD(DAY, -3, GETDATE()));
        IF @l3 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@adminId, @l3, 0, GETDATE());
        IF @l5 IS NOT NULL INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at) VALUES (@adminId, @l5, 1, DATEADD(DAY, -1, GETDATE()));
    END;

    -- Lấy nội dung bài học để làm flashcard
    DECLARE @contentNice BIGINT = (SELECT TOP 1 id FROM lesson_contents WHERE word_or_structure = 'Nice to meet you');
    DECLARE @contentWhere BIGINT = (SELECT TOP 1 id FROM lesson_contents WHERE word_or_structure = 'Where are you from?');
    DECLARE @contentMyName BIGINT = (SELECT TOP 1 id FROM lesson_contents WHERE word_or_structure = 'My name is');
    DECLARE @contentBreakfast BIGINT = (SELECT TOP 1 id FROM lesson_contents WHERE word_or_structure = 'breakfast');
    DECLARE @contentCommute BIGINT = (SELECT TOP 1 id FROM lesson_contents WHERE word_or_structure = 'commute');
    DECLARE @contentSchedule BIGINT = (SELECT TOP 1 id FROM lesson_contents WHERE word_or_structure = 'schedule');

    -- Chèn srs_reviews (Flashcard SRS) cho học viên
    -- Gồm 3 từ cần ôn hôm nay (next_review_date <= hiện tại)
    -- Gồm 3 từ ôn sắp tới (next_review_date > hiện tại)
    IF @studentId IS NOT NULL
    BEGIN
        IF @contentNice IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@studentId, @contentNice, 1, 1, 2.5, DATEADD(HOUR, -2, GETDATE()));
        IF @contentWhere IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@studentId, @contentWhere, 2, 3, 2.5, DATEADD(HOUR, -5, GETDATE()));
        IF @contentBreakfast IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@studentId, @contentBreakfast, 0, 1, 2.3, DATEADD(HOUR, -1, GETDATE()));
        IF @contentMyName IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@studentId, @contentMyName, 3, 6, 2.6, DATEADD(DAY, 2, GETDATE()));
        IF @contentCommute IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@studentId, @contentCommute, 1, 1, 2.5, DATEADD(DAY, 3, GETDATE()));
        IF @contentSchedule IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@studentId, @contentSchedule, 2, 3, 2.5, DATEADD(DAY, 5, GETDATE()));
    END;

    IF @adminId IS NOT NULL
    BEGIN
        IF @contentNice IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@adminId, @contentNice, 1, 1, 2.5, DATEADD(HOUR, -2, GETDATE()));
        IF @contentWhere IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@adminId, @contentWhere, 2, 3, 2.5, DATEADD(HOUR, -5, GETDATE()));
        IF @contentBreakfast IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@adminId, @contentBreakfast, 0, 1, 2.3, DATEADD(HOUR, -1, GETDATE()));
        IF @contentMyName IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@adminId, @contentMyName, 3, 6, 2.6, DATEADD(DAY, 2, GETDATE()));
        IF @contentCommute IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@adminId, @contentCommute, 1, 1, 2.5, DATEADD(DAY, 3, GETDATE()));
        IF @contentSchedule IS NOT NULL INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date) VALUES (@adminId, @contentSchedule, 2, 3, 2.5, DATEADD(DAY, 5, GETDATE()));
    END;

    -- Chèn Bookmark mẫu
    IF @studentId IS NOT NULL
    BEGIN
        IF @contentNice IS NOT NULL INSERT INTO book_marks (user_id, content_id, bookmarked_at) VALUES (@studentId, @contentNice, GETDATE());
        IF @contentBreakfast IS NOT NULL INSERT INTO book_marks (user_id, content_id, bookmarked_at) VALUES (@studentId, @contentBreakfast, GETDATE());
    END;

    IF @adminId IS NOT NULL
    BEGIN
        IF @contentNice IS NOT NULL INSERT INTO book_marks (user_id, content_id, bookmarked_at) VALUES (@adminId, @contentNice, GETDATE());
        IF @contentBreakfast IS NOT NULL INSERT INTO book_marks (user_id, content_id, bookmarked_at) VALUES (@adminId, @contentBreakfast, GETDATE());
    END;

    COMMIT TRANSACTION;
    PRINT N'=== ĐÃ SEED THÀNH CÔNG DỮ LIỆU KIỂM THỬ CHO CẢ HAI USER ID 1 VÀ 3 ===';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
    DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
    DECLARE @ErrorState INT = ERROR_STATE();
    RAISERROR (@ErrorMessage, @ErrorSeverity, @ErrorState);
END CATCH;
GO
