USE EnglishLearningDB;
GO

DECLARE @studentUserId BIGINT;
SELECT TOP 1 @studentUserId = id FROM users WHERE email = 'student@espp.com';

IF @studentUserId IS NULL
BEGIN
    INSERT INTO users (email, password_hash, full_name, role, status, created_at)
    VALUES ('student@espp.com', '$2a$10$7QJ8GJr3X9V8wW7h9wOaUeVh6H0o9f1w2Y0c3h4s5p6q7r8t9u0vW', 'Student ESPP', 'STUDENT', 'ACTIVE', GETDATE());
    SET @studentUserId = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

DECLARE @lessonA1 BIGINT;
DECLARE @lessonA2 BIGINT;
DECLARE @lessonB1 BIGINT;
DECLARE @lessonB2 BIGINT;

SELECT TOP 1 @lessonA1 = id FROM lessons WHERE title = 'A1 Greetings and Self Introduction';
IF @lessonA1 IS NULL
BEGIN
    INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
    VALUES ('A1 Greetings and Self Introduction', 'A1', 'VOCABULARY',
            'Basic expressions for greeting people and introducing yourself.', 1, GETDATE(), GETDATE());
    SET @lessonA1 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

SELECT TOP 1 @lessonA2 = id FROM lessons WHERE title = 'A2 Present Simple';
IF @lessonA2 IS NULL
BEGIN
    INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
    VALUES ('A2 Present Simple', 'A2', 'GRAMMAR',
            'How to use the present simple for habits, facts, and schedules.', 1, GETDATE(), GETDATE());
    SET @lessonA2 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

SELECT TOP 1 @lessonB1 = id FROM lessons WHERE title = 'B1 IPA Short and Long Vowels';
IF @lessonB1 IS NULL
BEGIN
    INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
    VALUES ('B1 IPA Short and Long Vowels', 'B1', 'PRONUNCIATION',
            'Practice common English vowel pairs and pronunciation contrast.', 1, GETDATE(), GETDATE());
    SET @lessonB1 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

SELECT TOP 1 @lessonB2 = id FROM lessons WHERE title = 'B2 Environment Vocabulary';
IF @lessonB2 IS NULL
BEGIN
    INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
    VALUES ('B2 Environment Vocabulary', 'B2', 'VOCABULARY',
            'Academic vocabulary for environment, climate change, and conservation.', 1, GETDATE(), GETDATE());
    SET @lessonB2 = CONVERT(BIGINT, SCOPE_IDENTITY());
END;

IF @lessonA1 IS NULL OR @lessonA2 IS NULL OR @lessonB1 IS NULL OR @lessonB2 IS NULL
BEGIN
    THROW 50001, 'Cannot create lesson ids. Please check lessons.id identity column.', 1;
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA1 AND word_or_structure = 'Nice to meet you')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonA1, 'Nice to meet you', '/nais tu miit yu/', 'Rat vui duoc gap ban', 1,
            'Use this phrase when you meet someone for the first time.',
            'Nice to meet you, Lan.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA1 AND word_or_structure = 'Where are you from?')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonA1, 'Where are you from?', '/wer ar yu from/', 'Ban den tu dau?', 2,
            'Use this question to ask about a person country or hometown.',
            'Where are you from? I am from Vietnam.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA2 AND word_or_structure = 'Subject + V(s/es)')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonA2, 'Subject + V(s/es)', NULL, 'Cau truc hien tai don', 1,
            'Add s or es when the subject is he, she, it, or a singular noun.',
            'She studies English every day.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonA2 AND word_or_structure = 'Adverbs of frequency')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonA2, 'Adverbs of frequency', NULL, 'Trang tu chi tan suat', 2,
            'Words like always, usually, often, sometimes, and never are common with present simple.',
            'I usually review vocabulary at night.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB1 AND word_or_structure = 'ship / sheep')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonB1, 'ship / sheep', '/ship/ - /sheep/', 'tau / con cuu', 1,
            'Contrast the short vowel in ship with the long vowel in sheep.',
            'The ship is near the sheep farm.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB1 AND word_or_structure = 'full / fool')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonB1, 'full / fool', '/ful/ - /fuul/', 'day / ke ngoc', 2,
            'Contrast the short vowel in full with the long vowel in fool.',
            'The cup is full.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB2 AND word_or_structure = 'sustainable')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonB2, 'sustainable', '/suh-stay-nuh-bul/', 'ben vung', 1,
            'Used to describe practices or solutions that can continue without harming the environment.',
            'We need sustainable energy solutions.');
END;

IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @lessonB2 AND word_or_structure = 'carbon footprint')
BEGIN
    INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
    VALUES (@lessonB2, 'carbon footprint', '/kar-buhn foot-print/', 'luong khi thai carbon', 2,
            'The amount of greenhouse gas produced by a person, organization, or activity.',
            'Taking public transport can reduce your carbon footprint.');
END;

DECLARE @contentNice BIGINT;
DECLARE @contentSustainable BIGINT;

SELECT TOP 1 @contentNice = id
FROM lesson_contents
WHERE lesson_id = @lessonA1 AND word_or_structure = 'Nice to meet you';

SELECT TOP 1 @contentSustainable = id
FROM lesson_contents
WHERE lesson_id = @lessonB2 AND word_or_structure = 'sustainable';

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
    'learning_content_seed_ascii_safe.sql completed' AS result;
GO
