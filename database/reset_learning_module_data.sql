USE EnglishLearningDB;
GO

ALTER TABLE book_marks NOCHECK CONSTRAINT ALL;
ALTER TABLE srs_reviews NOCHECK CONSTRAINT ALL;
ALTER TABLE learning_progress NOCHECK CONSTRAINT ALL;
ALTER TABLE lesson_contents NOCHECK CONSTRAINT ALL;
ALTER TABLE lessons NOCHECK CONSTRAINT ALL;
GO

DELETE FROM book_marks;
DELETE FROM srs_reviews;
DELETE FROM learning_progress;
DELETE FROM lesson_contents;
DELETE FROM lessons;
GO

DBCC CHECKIDENT ('book_marks', RESEED, 0);
DBCC CHECKIDENT ('srs_reviews', RESEED, 0);
DBCC CHECKIDENT ('learning_progress', RESEED, 0);
DBCC CHECKIDENT ('lesson_contents', RESEED, 0);
DBCC CHECKIDENT ('lessons', RESEED, 0);
GO

ALTER TABLE lessons WITH CHECK CHECK CONSTRAINT ALL;
ALTER TABLE lesson_contents WITH CHECK CHECK CONSTRAINT ALL;
ALTER TABLE learning_progress WITH CHECK CHECK CONSTRAINT ALL;
ALTER TABLE srs_reviews WITH CHECK CHECK CONSTRAINT ALL;
ALTER TABLE book_marks WITH CHECK CHECK CONSTRAINT ALL;
GO

DECLARE @studentUserId BIGINT;
SELECT TOP 1 @studentUserId = id FROM users WHERE email = 'student@espp.com';

IF @studentUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, created_at)
    VALUES ('student@espp.com', '$2a$10$7QJ8GJr3X9V8wW7h9wOaUeVh6H0o9f1w2Y0c3h4s5p6q7r8t9u0vW', N'Student ESPP', 'STUDENT', 'ACTIVE', GETDATE());
    SET @studentUserId = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
VALUES
('A1 Greetings and Self Introduction', 'A1', 'VOCABULARY', N'Từ vựng cơ bản dùng khi chào hỏi và giới thiệu bản thân.', 1, GETDATE(), GETDATE()),
('A1 Daily Vocabulary', 'A1', 'VOCABULARY', N'Từ vựng hằng ngày giúp học viên giao tiếp trong các tình huống quen thuộc.', 1, GETDATE(), GETDATE()),
('A2 Present Simple', 'A2', 'GRAMMAR', N'Cách dùng thì hiện tại đơn để nói về thói quen, sự thật hiển nhiên và lịch trình.', 1, GETDATE(), GETDATE()),
('A2 Past Simple', 'A2', 'GRAMMAR', N'Cách dùng thì quá khứ đơn để kể lại sự kiện đã xảy ra trong quá khứ.', 1, GETDATE(), GETDATE()),
('B1 IPA Short and Long Vowels', 'B1', 'PRONUNCIATION', N'Luyện phân biệt các cặp nguyên âm ngắn và dài trong tiếng Anh.', 1, GETDATE(), GETDATE()),
('B1 Speaking Connectors', 'B1', 'GRAMMAR', N'Cấu trúc nối ý giúp câu trả lời nói mạch lạc và tự nhiên hơn.', 1, GETDATE(), GETDATE()),
('B2 Environment Vocabulary', 'B2', 'VOCABULARY', N'Từ vựng học thuật về môi trường, biến đổi khí hậu và bảo tồn.', 1, GETDATE(), GETDATE()),
('B2 Academic Writing Structures', 'B2', 'GRAMMAR', N'Cấu trúc câu hữu ích khi viết đoạn văn học thuật và nêu quan điểm.', 1, GETDATE(), GETDATE());

DECLARE @l1 BIGINT = (SELECT id FROM lessons WHERE title = 'A1 Greetings and Self Introduction');
DECLARE @l2 BIGINT = (SELECT id FROM lessons WHERE title = 'A1 Daily Vocabulary');
DECLARE @l3 BIGINT = (SELECT id FROM lessons WHERE title = 'A2 Present Simple');
DECLARE @l4 BIGINT = (SELECT id FROM lessons WHERE title = 'A2 Past Simple');
DECLARE @l5 BIGINT = (SELECT id FROM lessons WHERE title = 'B1 IPA Short and Long Vowels');
DECLARE @l6 BIGINT = (SELECT id FROM lessons WHERE title = 'B1 Speaking Connectors');
DECLARE @l7 BIGINT = (SELECT id FROM lessons WHERE title = 'B2 Environment Vocabulary');
DECLARE @l8 BIGINT = (SELECT id FROM lessons WHERE title = 'B2 Academic Writing Structures');

INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES
(@l1, 'Nice to meet you', '/nais tu miit yu/', N'Rất vui được gặp bạn', 1, N'Dùng khi gặp ai đó lần đầu trong tình huống lịch sự hoặc thân thiện.', 'Nice to meet you, Lan.'),
(@l1, 'Where are you from?', '/wer ar yu from/', N'Bạn đến từ đâu?', 2, N'Câu hỏi dùng để hỏi quê quán hoặc quốc gia của người đối thoại.', 'Where are you from? I am from Vietnam.'),
(@l1, 'My name is ...', '/mai neim iz/', N'Tên của tôi là ...', 3, N'Mẫu câu cơ bản để giới thiệu tên trong giao tiếp.', 'My name is Huy.'),

(@l2, 'appointment', '/uh-point-muhnt/', N'cuộc hẹn', 1, N'Danh từ dùng khi nói về một cuộc hẹn đã được sắp xếp trước.', 'I have a dentist appointment tomorrow.'),
(@l2, 'receipt', '/ri-seet/', N'hóa đơn, biên lai', 2, N'Dùng khi nói về giấy xác nhận đã thanh toán.', 'Please keep your receipt.'),
(@l2, 'borrow', '/bor-oh/', N'mượn', 3, N'Dùng khi lấy tạm vật gì từ người khác và sẽ trả lại.', 'Can I borrow your pen?'),

(@l3, 'Subject + V(s/es)', NULL, N'Cấu trúc hiện tại đơn', 1, N'Thêm s hoặc es khi chủ ngữ là he, she, it hoặc danh từ số ít.', 'She studies English every day.'),
(@l3, 'Adverbs of frequency', NULL, N'Trạng từ chỉ tần suất', 2, N'Các từ như always, usually, often, sometimes, never thường đi với hiện tại đơn.', 'I usually review vocabulary at night.'),
(@l3, 'Do/Does + subject + verb?', NULL, N'Câu hỏi hiện tại đơn', 3, N'Dùng do hoặc does để đặt câu hỏi với thì hiện tại đơn.', 'Does he speak English?'),

(@l4, 'Subject + V2/ed', NULL, N'Cấu trúc quá khứ đơn', 1, N'Dùng động từ quá khứ để diễn tả hành động đã kết thúc.', 'They visited Da Nang last summer.'),
(@l4, 'Yesterday / last / ago', NULL, N'Dấu hiệu quá khứ', 2, N'Các cụm thời gian này thường xuất hiện với thì quá khứ đơn.', 'I finished the lesson two days ago.'),
(@l4, 'Did + subject + verb?', NULL, N'Câu hỏi quá khứ đơn', 3, N'Dùng did để đặt câu hỏi, động từ chính trở về dạng nguyên mẫu.', 'Did you watch the video?'),

(@l5, 'ship / sheep', '/ship/ - /sheep/', N'tàu / con cừu', 1, N'Phân biệt âm ngắn trong ship và âm dài trong sheep.', 'The ship is near the sheep farm.'),
(@l5, 'full / fool', '/ful/ - /fuul/', N'đầy / kẻ ngốc', 2, N'Phân biệt âm ngắn trong full và âm dài trong fool.', 'The cup is full.'),
(@l5, 'sit / seat', '/sit/ - /seet/', N'ngồi / chỗ ngồi', 3, N'Cặp từ giúp luyện độ dài nguyên âm.', 'Please sit in your seat.'),

(@l6, 'In my opinion', NULL, N'theo ý kiến của tôi', 1, N'Dùng để mở đầu quan điểm cá nhân trong bài nói.', 'In my opinion, online learning is convenient.'),
(@l6, 'On the other hand', NULL, N'mặt khác', 2, N'Dùng để chuyển sang ý đối lập hoặc bổ sung góc nhìn khác.', 'On the other hand, it can be distracting.'),
(@l6, 'For example', NULL, N'ví dụ', 3, N'Dùng để đưa ví dụ minh họa cho luận điểm.', 'For example, students can review lessons anytime.'),

(@l7, 'sustainable', '/suh-stay-nuh-bul/', N'bền vững', 1, N'Dùng để mô tả giải pháp hoặc thói quen có thể duy trì lâu dài mà không gây hại môi trường.', 'We need sustainable energy solutions.'),
(@l7, 'carbon footprint', '/kar-buhn foot-print/', N'lượng khí thải carbon', 2, N'Lượng khí nhà kính do một người, tổ chức hoặc hoạt động tạo ra.', 'Taking public transport can reduce your carbon footprint.'),
(@l7, 'renewable energy', '/ri-new-uh-bul en-er-jee/', N'năng lượng tái tạo', 3, N'Năng lượng đến từ nguồn có thể tái tạo như mặt trời, gió hoặc nước.', 'Solar power is a form of renewable energy.'),

(@l8, 'It is widely believed that ...', NULL, N'Nhiều người tin rằng ...', 1, N'Cấu trúc dùng để giới thiệu một quan điểm phổ biến.', 'It is widely believed that education improves career opportunities.'),
(@l8, 'This essay will discuss ...', NULL, N'Bài viết này sẽ thảo luận ...', 2, N'Câu dùng trong mở bài để nêu phạm vi thảo luận.', 'This essay will discuss the benefits of learning English.'),
(@l8, 'A key advantage is ...', NULL, N'Một lợi ích quan trọng là ...', 3, N'Cấu trúc dùng để trình bày luận điểm chính trong thân bài.', 'A key advantage is better access to global information.');

DECLARE @contentNice BIGINT = (
    SELECT TOP 1 id FROM lesson_contents WHERE lesson_id = @l1 AND word_or_structure = 'Nice to meet you'
);
DECLARE @contentSustainable BIGINT = (
    SELECT TOP 1 id FROM lesson_contents WHERE lesson_id = @l7 AND word_or_structure = 'sustainable'
);

IF @studentUserId IS NOT NULL
BEGIN
    INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at)
    VALUES
    (@studentUserId, @l1, 1, GETDATE()),
    (@studentUserId, @l3, 0, GETDATE());

    IF @contentNice IS NOT NULL
    BEGIN
        INSERT INTO book_marks (user_id, content_id, bookmarked_at)
        VALUES (@studentUserId, @contentNice, GETDATE());
    END;

    IF @contentSustainable IS NOT NULL
    BEGIN
        INSERT INTO book_marks (user_id, content_id, bookmarked_at)
        VALUES (@studentUserId, @contentSustainable, GETDATE());
    END;
END;

SELECT id, title, type, level, description
FROM lessons
ORDER BY id ASC;
GO
