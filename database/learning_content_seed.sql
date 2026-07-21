USE EnglishLearningDB;
GO

DECLARE @studentUserId BIGINT;
SELECT TOP 1 @studentUserId = id FROM users WHERE email = 'student@espp.com';

IF @studentUserId IS NULL
BEGIN
INSERT INTO users (email, password_hash, full_name, role, status, created_at)
VALUES ('student@espp.com', '$2a$10$7QJ8GJr3X9V8wW7h9wOaUeVh6H0o9f1w2Y0c3h4s5p6q7r8t9u0vW', N'Student ESPP', 'STUDENT', 'ACTIVE', GETDATE());

SET @studentUserId = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

DECLARE @lessonA1 BIGINT;
DECLARE @lessonA2 BIGINT;
DECLARE @lessonB1 BIGINT;
DECLARE @lessonB2 BIGINT;

SELECT TOP 1 @lessonA1 = id FROM lessons WHERE title = N'A1_GREETING_INTRO';
IF @lessonA1 IS NULL
BEGIN
INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
VALUES (N'A1_GREETING_INTRO', 'A1', 'VOCABULARY',
        N'Từ vựng cơ bản dùng khi chào hỏi, giới thiệu tên, tuổi và quê quán.', 1, GETDATE(), GETDATE());

SET @lessonA1 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

SELECT TOP 1 @lessonA2 = id FROM lessons WHERE title = N'A2_PRESENT_SIMPLE';
IF @lessonA2 IS NULL
BEGIN
INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
VALUES (N'A2_PRESENT_SIMPLE', 'A2', 'GRAMMAR',
        N'Cách dùng thì hiện tại đơn để nói về thói quen, sự thật hiển nhiên và lịch trình.', 1, GETDATE(), GETDATE());

SET @lessonA2 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

SELECT TOP 1 @lessonB1 = id FROM lessons WHERE title = N'B1_IPA_SHORT_LONG_VOWELS';
IF @lessonB1 IS NULL
BEGIN
INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
VALUES (N'B1_IPA_SHORT_LONG_VOWELS', 'B1', 'PRONUNCIATION',
        N'Luyện phân biệt các cặp nguyên âm phổ biến trong tiếng Anh.', 1, GETDATE(), GETDATE());

SET @lessonB1 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

SELECT TOP 1 @lessonB2 = id FROM lessons WHERE title = N'B2_ENVIRONMENT_VOCABULARY';
IF @lessonB2 IS NULL
BEGIN
INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
VALUES (N'B2_ENVIRONMENT_VOCABULARY', 'B2', 'VOCABULARY',
        N'Từ vựng học thuật thường gặp về môi trường, biến đổi khí hậu và bảo tồn.', 1, GETDATE(), GETDATE());

SET @lessonB2 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

IF @lessonA1 IS NULL OR @lessonA2 IS NULL OR @lessonB1 IS NULL OR @lessonB2 IS NULL
BEGIN
    THROW 50001, 'Khong tao duoc lesson id. Kiem tra bang lessons va cot identity id.', 1;
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA1 AND word_or_structure = N'Nice to meet you')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonA1, N'Nice to meet you', N'/naɪs tə miːt juː/', N'Rất vui được gặp bạn', 1,
        N'Dùng khi gặp ai đó lần đầu trong tình huống lịch sự hoặc thân thiện.',
        N'Nice to meet you, Lan.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA1 AND word_or_structure = N'Where are you from?')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonA1, N'Where are you from?', N'/wer ɑːr juː frəm/', N'Bạn đến từ đâu?', 2,
        N'Câu hỏi dùng để hỏi quê quán hoặc quốc gia của người đối thoại.',
        N'Where are you from? I am from Vietnam.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA2 AND word_or_structure = N'Subject + V(s/es)')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonA2, N'Subject + V(s/es)', NULL, N'Cấu trúc hiện tại đơn', 1,
        N'Thêm s/es khi chủ ngữ là he, she, it hoặc danh từ số ít.',
        N'She studies English every day.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA2 AND word_or_structure = N'Adverbs of frequency')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonA2, N'Adverbs of frequency', NULL, N'Trạng từ chỉ tần suất', 2,
        N'Các từ như always, usually, often, sometimes, never thường đi với hiện tại đơn.',
        N'I usually review vocabulary at night.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB1 AND word_or_structure = N'ship / sheep')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonB1, N'ship / sheep', N'/ʃɪp/ - /ʃiːp/', N'tàu / con cừu', 1,
        N'Cặp âm /ɪ/ ngắn và /iː/ dài. Cần kéo dài âm trong từ sheep.',
        N'The ship is near the sheep farm.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB1 AND word_or_structure = N'full / fool')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonB1, N'full / fool', N'/fʊl/ - /fuːl/', N'đầy / kẻ ngốc', 2,
        N'Cặp âm /ʊ/ ngắn và /uː/ dài. Chú ý độ tròn môi và độ dài âm.',
        N'The cup is full.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB2 AND word_or_structure = N'sustainable')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonB2, N'sustainable', N'/səˈsteɪnəbl/', N'bền vững', 1,
        N'Dùng để mô tả giải pháp hoặc thói quen có thể duy trì lâu dài mà không gây hại môi trường.',
        N'We need sustainable energy solutions.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB2 AND word_or_structure = N'carbon footprint')
BEGIN
INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
VALUES (@lessonB2, N'carbon footprint', N'/ˌkɑːrbən ˈfʊtprɪnt/', N'lượng khí thải carbon', 2,
        N'Lượng khí nhà kính do một người, tổ chức hoặc hoạt động tạo ra.',
        N'Taking public transport can reduce your carbon footprint.');
END;

DECLARE @contentNice BIGINT;
DECLARE @contentSustainable BIGINT;

SELECT TOP 1 @contentNice = id
FROM lesson_contents
WHERE lesson_id = @lessonA1 AND word_or_structure = N'Nice to meet you';

SELECT TOP 1 @contentSustainable = id
FROM lesson_contents
WHERE lesson_id = @lessonB2 AND word_or_structure = N'sustainable';

IF @studentUserId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_progress WHERE user_id = @studentUserId AND lesson_id = @lessonA1)
BEGIN
INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at)
VALUES (@studentUserId, @lessonA1, 1, GETDATE());
END;

IF @studentUserId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_progress WHERE user_id = @studentUserId AND lesson_id = @lessonA2)
BEGIN
INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at)
VALUES (@studentUserId, @lessonA2, 0, GETDATE());
END;

IF @studentUserId IS NOT NULL AND @contentNice IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM book_marks WHERE user_id = @studentUserId AND content_id = @contentNice)
BEGIN
INSERT INTO book_marks (user_id, content_id, bookmarked_at)
VALUES (@studentUserId, @contentNice, GETDATE());
END;

IF @studentUserId IS NOT NULL AND @contentSustainable IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM book_marks WHERE user_id = @studentUserId AND content_id = @contentSustainable)
BEGIN
INSERT INTO book_marks (user_id, content_id, bookmarked_at)
VALUES (@studentUserId, @contentSustainable, GETDATE());
END;

SELECT
    @lessonA1 AS lesson_a1_id,
    @lessonA2 AS lesson_a2_id,
    @lessonB1 AS lesson_b1_id,
    @lessonB2 AS lesson_b2_id,
    @contentNice AS sample_bookmark_1,
    @contentSustainable AS sample_bookmark_2,
    'learning_content_seed.sql completed' AS result;
GO
