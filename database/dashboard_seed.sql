USE ATS_DB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    -- DashboardService hiện đọc dữ liệu của user_id = 1.
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = 1)
        THROW 50001, N'Không tồn tại users.id = 1. Hãy tạo tài khoản học viên trước.', 1;

    DECLARE @VocabularyLessonId BIGINT;
    DECLARE @GrammarLessonId BIGINT;
    DECLARE @PronunciationLessonId BIGINT;

    SELECT @VocabularyLessonId = id FROM lessons WHERE title = N'Daily English Vocabulary';
    IF @VocabularyLessonId IS NULL
    BEGIN
        INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
        VALUES (N'Daily English Vocabulary', 'A1', 'VOCABULARY',
                N'Từ vựng tiếng Anh thông dụng trong giao tiếp hằng ngày.', 1, GETDATE(), GETDATE());
        SET @VocabularyLessonId = SCOPE_IDENTITY();
    END;

    SELECT @GrammarLessonId = id FROM lessons WHERE title = N'Present Simple Basics';
    IF @GrammarLessonId IS NULL
    BEGIN
        INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
        VALUES (N'Present Simple Basics', 'A1', 'GRAMMAR',
                N'Cách dùng thì hiện tại đơn trong câu khẳng định và phủ định.', 1, GETDATE(), GETDATE());
        SET @GrammarLessonId = SCOPE_IDENTITY();
    END;

    SELECT @PronunciationLessonId = id FROM lessons WHERE title = N'English Ending Sounds';
    IF @PronunciationLessonId IS NULL
    BEGIN
        INSERT INTO lessons (title, level, type, description, is_published, created_at, updated_at)
        VALUES (N'English Ending Sounds', 'A2', 'PRONUNCIATION',
                N'Luyện phát âm các âm cuối thường gặp trong tiếng Anh.', 1, GETDATE(), GETDATE());
        SET @PronunciationLessonId = SCOPE_IDENTITY();
    END;

    IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @VocabularyLessonId AND word_or_structure = N'hello')
        INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order)
        VALUES (@VocabularyLessonId, N'hello', N'/həˈləʊ/', N'xin chào', 1);

    IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @VocabularyLessonId AND word_or_structure = N'improve')
        INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order)
        VALUES (@VocabularyLessonId, N'improve', N'/ɪmˈpruːv/', N'cải thiện', 2);

    IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @GrammarLessonId AND word_or_structure = N'usually')
        INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order)
        VALUES (@GrammarLessonId, N'usually', N'/ˈjuːʒuəli/', N'thường xuyên', 1);

    IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @GrammarLessonId AND word_or_structure = N'He/She + V-s/es')
        INSERT INTO lesson_contents (lesson_id, word_or_structure, meaning, content_order)
        VALUES (@GrammarLessonId, N'He/She + V-s/es', N'Cấu trúc hiện tại đơn ngôi thứ ba', 2);

    IF NOT EXISTS (SELECT 1 FROM lesson_contents WHERE lesson_id = @PronunciationLessonId AND word_or_structure = N'watched')
        INSERT INTO lesson_contents (lesson_id, word_or_structure, ipa, meaning, content_order)
        VALUES (@PronunciationLessonId, N'watched', N'/wɒtʃt/', N'đã xem', 1);

    IF NOT EXISTS (SELECT 1 FROM learning_progress WHERE user_id = 1 AND lesson_id = @VocabularyLessonId)
        INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at)
        VALUES (1, @VocabularyLessonId, 1, DATEADD(DAY, -3, GETDATE()));

    IF NOT EXISTS (SELECT 1 FROM learning_progress WHERE user_id = 1 AND lesson_id = @GrammarLessonId)
        INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at)
        VALUES (1, @GrammarLessonId, 1, DATEADD(DAY, -1, GETDATE()));

    IF NOT EXISTS (SELECT 1 FROM learning_progress WHERE user_id = 1 AND lesson_id = @PronunciationLessonId)
        INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at)
        VALUES (1, @PronunciationLessonId, 0, GETDATE());

    DECLARE @HelloId BIGINT = (SELECT id FROM lesson_contents WHERE lesson_id = @VocabularyLessonId AND word_or_structure = N'hello');
    DECLARE @ImproveId BIGINT = (SELECT id FROM lesson_contents WHERE lesson_id = @VocabularyLessonId AND word_or_structure = N'improve');
    DECLARE @UsuallyId BIGINT = (SELECT id FROM lesson_contents WHERE lesson_id = @GrammarLessonId AND word_or_structure = N'usually');
    DECLARE @ThirdPersonId BIGINT = (SELECT id FROM lesson_contents WHERE lesson_id = @GrammarLessonId AND word_or_structure = N'He/She + V-s/es');
    DECLARE @WatchedId BIGINT = (SELECT id FROM lesson_contents WHERE lesson_id = @PronunciationLessonId AND word_or_structure = N'watched');

    IF NOT EXISTS (SELECT 1 FROM srs_reviews WHERE user_id = 1 AND content_id = @HelloId)
        INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date)
        VALUES (1, @HelloId, 3, 7, 2.5, DATEADD(HOUR, -2, GETDATE()));

    IF NOT EXISTS (SELECT 1 FROM srs_reviews WHERE user_id = 1 AND content_id = @ImproveId)
        INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date)
        VALUES (1, @ImproveId, 2, 3, 2.5, DATEADD(HOUR, 4, GETDATE()));

    IF NOT EXISTS (SELECT 1 FROM srs_reviews WHERE user_id = 1 AND content_id = @UsuallyId)
        INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date)
        VALUES (1, @UsuallyId, 1, 1, 2.5, DATEADD(DAY, 1, GETDATE()));

    IF NOT EXISTS (SELECT 1 FROM srs_reviews WHERE user_id = 1 AND content_id = @ThirdPersonId)
        INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date)
        VALUES (1, @ThirdPersonId, 2, 3, 2.5, DATEADD(DAY, 2, GETDATE()));

    IF NOT EXISTS (SELECT 1 FROM srs_reviews WHERE user_id = 1 AND content_id = @WatchedId)
        INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date)
        VALUES (1, @WatchedId, 1, 1, 2.5, DATEADD(DAY, 3, GETDATE()));

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Kiểm tra dữ liệu vừa thêm.
SELECT
    (SELECT COUNT(*) FROM srs_reviews WHERE user_id = 1) AS total_flashcards,
    (SELECT COUNT(*) FROM srs_reviews WHERE user_id = 1 AND next_review_date <= GETDATE()) AS due_today,
    (SELECT COUNT(*) FROM learning_progress WHERE user_id = 1) AS learned_lessons,
    (SELECT COUNT(*) FROM learning_progress WHERE user_id = 1 AND is_completed = 1) AS completed_lessons;
GO
