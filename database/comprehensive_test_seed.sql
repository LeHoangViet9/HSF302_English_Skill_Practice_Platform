USE ATS_DB;
GO

/*
  Comprehensive, repeatable test data for ESPP (SQL Server).

  Test login accounts:
    loadtest.admin@espp.local / password
    loadtest.staff@espp.local / password
    loadtest.student01@espp.local ... loadtest.student12@espp.local / password

  Re-running this script only replaces rows owned by the loadtest marker.
  Existing application/user data is preserved.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @Marker NVARCHAR(40) = N'[LOADTEST]';
    -- BCrypt hash for the literal password: password
    DECLARE @PasswordHash VARCHAR(255) = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

    ---------------------------------------------------------------------------
    -- Remove only data created by a previous run of this script.
    ---------------------------------------------------------------------------
    DELETE d
    FROM exam_attemp_detail d
    JOIN exam_histories h ON h.id = d.exam_history_id
    JOIN users u ON u.id = h.user_id
    WHERE u.email LIKE 'loadtest.%@espp.local';

    DELETE FROM exam_histories
    WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest.%@espp.local');

    DELETE FROM learning_progress
    WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest.%@espp.local');

    DELETE FROM book_marks
    WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest.%@espp.local');

    DELETE FROM srs_reviews
    WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest.%@espp.local');

    DELETE FROM system_logs
    WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest.%@espp.local');

    DELETE FROM auth_tokens
    WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest.%@espp.local');

    DELETE FROM student_users
    WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'loadtest.%@espp.local');

    DELETE FROM questions
    WHERE exam_id IN (SELECT id FROM exams WHERE LEFT(title, LEN(@Marker)) = @Marker);

    DELETE FROM exams WHERE LEFT(title, LEN(@Marker)) = @Marker;

    DELETE FROM lesson_contents
    WHERE lesson_id IN (SELECT id FROM lessons WHERE LEFT(title, LEN(@Marker)) = @Marker);

    DELETE FROM lessons WHERE LEFT(title, LEN(@Marker)) = @Marker;

    -- Delete users last because loadtest lessons reference the staff account.
    DELETE FROM users WHERE email LIKE 'loadtest.%@espp.local';

    ---------------------------------------------------------------------------
    -- Users: all roles/statuses, lockout and last-login states.
    ---------------------------------------------------------------------------
    INSERT INTO users
        (email, password_hash, full_name, role, status, login_attempts,
         locked_until, last_login_at, last_login_ip, password_changed_at,
         created_at, updated_at)
    VALUES
        ('loadtest.admin@espp.local', @PasswordHash, N'Load Test Admin', 'ADMIN', 'ACTIVE', 0,
         NULL, DATEADD(MINUTE, -15, GETDATE()), '127.0.0.1', DATEADD(DAY, -30, GETDATE()), DATEADD(DAY, -180, GETDATE()), GETDATE()),
        ('loadtest.staff@espp.local', @PasswordHash, N'Load Test Staff', 'STAFF', 'ACTIVE', 0,
         NULL, DATEADD(HOUR, -2, GETDATE()), '10.0.0.20', DATEADD(DAY, -20, GETDATE()), DATEADD(DAY, -150, GETDATE()), GETDATE());

    DECLARE @StudentNumber INT = 1;
    WHILE @StudentNumber <= 12
    BEGIN
        INSERT INTO users
            (email, password_hash, full_name, role, status, login_attempts,
             locked_until, last_login_at, last_login_ip, password_changed_at,
             created_at, updated_at)
        VALUES
            (CONCAT('loadtest.student', RIGHT(CONCAT('0', @StudentNumber), 2), '@espp.local'),
             @PasswordHash,
             CONCAT(N'Học viên kiểm thử ', RIGHT(CONCAT('0', @StudentNumber), 2)),
             'STUDENT',
             CASE WHEN @StudentNumber = 12 THEN 'BANNED' ELSE 'ACTIVE' END,
             CASE WHEN @StudentNumber = 11 THEN 5 ELSE @StudentNumber % 3 END,
             CASE WHEN @StudentNumber = 11 THEN DATEADD(MINUTE, 30, GETDATE()) ELSE NULL END,
             DATEADD(DAY, -@StudentNumber, GETDATE()),
             CONCAT('10.0.1.', @StudentNumber),
             DATEADD(DAY, -30 - @StudentNumber, GETDATE()),
             DATEADD(DAY, -200 + @StudentNumber, GETDATE()), GETDATE());
        SET @StudentNumber += 1;
    END;

    -- The extra duplicated identity columns support both the current entity and
    -- databases upgraded from the older standalone student_users model.
    INSERT INTO student_users
        (user_id, email, full_name, password_hash, phone, status, is_deleted,
         login_attempts, last_login_at, last_login_ip, password_changed_at,
         created_at, updated_at)
    SELECT id, email, full_name, password_hash,
           CONCAT('090100', RIGHT(CONCAT('00', ROW_NUMBER() OVER (ORDER BY id)), 3)),
           status, 0, login_attempts, last_login_at, last_login_ip,
           password_changed_at, created_at, GETDATE()
    FROM users
    WHERE email LIKE 'loadtest.student%@espp.local';

    DECLARE @AdminId BIGINT = (SELECT id FROM users WHERE email = 'loadtest.admin@espp.local');
    DECLARE @StaffId BIGINT = (SELECT id FROM users WHERE email = 'loadtest.staff@espp.local');

    ---------------------------------------------------------------------------
    -- Lessons and contents: every CEFR level and lesson type.
    ---------------------------------------------------------------------------
    DECLARE @Levels TABLE (seq INT, level_code VARCHAR(2));
    INSERT INTO @Levels VALUES (1,'A1'),(2,'A2'),(3,'B1'),(4,'B2'),(5,'C1'),(6,'C2');
    DECLARE @LessonTypes TABLE (seq INT, type_code VARCHAR(20));
    INSERT INTO @LessonTypes VALUES (1,'VOCABULARY'),(2,'GRAMMAR'),(3,'PRONUNCIATION');

    INSERT INTO lessons
        (title, level, type, created_by, description, is_published, created_at, updated_at)
    SELECT CONCAT(@Marker, N' ', l.level_code, N' ', t.type_code, N' Practice'),
           l.level_code, t.type_code, @StaffId,
           CONCAT(N'Dữ liệu kiểm thử ', t.type_code, N' trình độ ', l.level_code,
                  N': tìm kiếm, phân trang, học và ôn tập.'),
           CASE WHEN l.seq = 6 AND t.seq = 3 THEN 0 ELSE 1 END,
           DATEADD(DAY, -(l.seq * 10 + t.seq), GETDATE()), GETDATE()
    FROM @Levels l CROSS JOIN @LessonTypes t;

    DECLARE @LessonId BIGINT, @LessonTitle NVARCHAR(255), @LessonType VARCHAR(20), @ContentNo INT;
    DECLARE lesson_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT id, title, type FROM lessons WHERE LEFT(title, LEN(@Marker)) = @Marker ORDER BY id;
    OPEN lesson_cursor;
    FETCH NEXT FROM lesson_cursor INTO @LessonId, @LessonTitle, @LessonType;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @ContentNo = 1;
        WHILE @ContentNo <= 8
        BEGIN
            INSERT INTO lesson_contents
                (lesson_id, word_or_structure, ipa, meaning, content_order, explanation, example)
            VALUES
                (@LessonId,
                 CASE @LessonType
                    WHEN 'VOCABULARY' THEN CONCAT(N'word-', @LessonId, N'-', @ContentNo)
                    WHEN 'GRAMMAR' THEN CONCAT(N'Grammar pattern ', @ContentNo)
                    ELSE CONCAT(N'Sound practice ', @ContentNo)
                 END,
                 CASE WHEN @LessonType = 'PRONUNCIATION' THEN CONCAT(N'/sound-', @ContentNo, N'/') ELSE NULL END,
                 CONCAT(N'Nghĩa kiểm thử số ', @ContentNo, N' của ', @LessonTitle),
                 @ContentNo,
                 CONCAT(N'Giải thích chi tiết cho mục ', @ContentNo, N'.'),
                 CONCAT(N'This is test example number ', @ContentNo, N'.'));
            SET @ContentNo += 1;
        END;
        FETCH NEXT FROM lesson_cursor INTO @LessonId, @LessonTitle, @LessonType;
    END;
    CLOSE lesson_cursor;
    DEALLOCATE lesson_cursor;

    ---------------------------------------------------------------------------
    -- Exams/questions: both quiz types and all four skills.
    ---------------------------------------------------------------------------
    DECLARE @ExamNo INT = 1, @ExamId BIGINT, @QuestionNo INT;
    DECLARE @Skill VARCHAR(20), @Correct CHAR(1);
    WHILE @ExamNo <= 8
    BEGIN
        INSERT INTO exams (title, type, duration, total_questions, description)
        VALUES (CONCAT(@Marker, N' Exam ', RIGHT(CONCAT('0', @ExamNo), 2)),
                CASE WHEN @ExamNo <= 4 THEN 'QUIZ' ELSE 'MOCK_TEST' END,
                CASE WHEN @ExamNo <= 4 THEN 20 ELSE 60 END, 10,
                CONCAT(N'Đề kiểm thử tổng hợp số ', @ExamNo, N'.'));

        SET @ExamId = SCOPE_IDENTITY();
        SET @QuestionNo = 1;
        WHILE @QuestionNo <= 10
        BEGIN
            SET @Skill = CASE (@QuestionNo - 1) % 4
                WHEN 0 THEN 'LISTENING' WHEN 1 THEN 'READING'
                WHEN 2 THEN 'SPEAKING' ELSE 'WRITING' END;
            SET @Correct = CHAR(65 + ((@QuestionNo - 1) % 4));

            INSERT INTO questions
                (exam_id, skill, question_text, audio_url, options, correct_answer, explanation)
            VALUES
                (@ExamId, @Skill,
                 CONCAT(N'[LOADTEST] Exam ', @ExamNo, N' - question ', @QuestionNo, N': choose the best answer.'),
                 CASE WHEN @Skill = 'LISTENING' THEN CONCAT('/audio/loadtest-', @ExamNo, '-', @QuestionNo, '.mp3') ELSE NULL END,
                 N'{"A":"Answer A","B":"Answer B","C":"Answer C","D":"Answer D"}',
                 @Correct,
                 CONCAT(N'Đáp án đúng là ', @Correct, N' cho dữ liệu kiểm thử.'));
            SET @QuestionNo += 1;
        END;
        SET @ExamNo += 1;
    END;

    ---------------------------------------------------------------------------
    -- Per-student functional data.
    ---------------------------------------------------------------------------
    DECLARE @UserId BIGINT, @UserIndex INT;
    DECLARE student_cursor CURSOR LOCAL FAST_FORWARD FOR
        SELECT id, ROW_NUMBER() OVER (ORDER BY id)
        FROM users WHERE email LIKE 'loadtest.student%@espp.local' ORDER BY id;
    OPEN student_cursor;
    FETCH NEXT FROM student_cursor INTO @UserId, @UserIndex;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        -- Progress covers completed/incomplete states across all lessons.
        INSERT INTO learning_progress (user_id, lesson_id, is_completed, updated_at)
        SELECT @UserId, id, CASE WHEN (ROW_NUMBER() OVER (ORDER BY id) + @UserIndex) % 3 = 0 THEN 0 ELSE 1 END,
               DATEADD(DAY, -(ROW_NUMBER() OVER (ORDER BY id) + @UserIndex) % 30, GETDATE())
        FROM lessons WHERE LEFT(title, LEN(@Marker)) = @Marker;

        -- Six bookmarks and eight SRS cards per student.
        INSERT INTO book_marks (user_id, content_id, bookmarked_at)
        SELECT TOP (6) @UserId, lc.id, DATEADD(DAY, -ROW_NUMBER() OVER (ORDER BY lc.id), GETDATE())
        FROM lesson_contents lc JOIN lessons l ON l.id = lc.lesson_id
        WHERE LEFT(l.title, LEN(@Marker)) = @Marker ORDER BY lc.id + @UserIndex;

        INSERT INTO srs_reviews (user_id, content_id, repetition, srs_interval, ease_factor, next_review_date)
        SELECT TOP (8) @UserId, lc.id,
               (ROW_NUMBER() OVER (ORDER BY lc.id) + @UserIndex) % 6,
               CASE (ROW_NUMBER() OVER (ORDER BY lc.id) + @UserIndex) % 4 WHEN 0 THEN 1 WHEN 1 THEN 3 WHEN 2 THEN 7 ELSE 14 END,
               CAST(1.8 + (((ROW_NUMBER() OVER (ORDER BY lc.id) + @UserIndex) % 8) * 0.1) AS FLOAT),
               DATEADD(DAY, ((ROW_NUMBER() OVER (ORDER BY lc.id) + @UserIndex) % 7) - 3, GETDATE())
        FROM lesson_contents lc JOIN lessons l ON l.id = lc.lesson_id
        WHERE LEFT(l.title, LEN(@Marker)) = @Marker ORDER BY lc.id + @UserIndex;

        -- One history for each exam (96 total), then 10 answer details/history.
        INSERT INTO exam_histories (user_id, exam_id, score, correct_answers_count, time_spent, tested_at)
        SELECT @UserId, e.id,
               CAST(((e.id + @UserIndex) % 11) AS FLOAT),
               (e.id + @UserIndex) % 11,
               300 + ((e.id * 97 + @UserIndex * 31) % 3000),
               DATEADD(DAY, -((e.id + @UserIndex) % 45), GETDATE())
        FROM exams e WHERE LEFT(e.title, LEN(@Marker)) = @Marker;

        INSERT INTO exam_attemp_detail (exam_history_id, question_id, selected_answer, is_correct)
        SELECT h.id, q.id,
               CASE WHEN (q.id + @UserIndex) % 5 = 0 THEN 'X' ELSE q.correct_answer END,
               CASE WHEN (q.id + @UserIndex) % 5 = 0 THEN 0 ELSE 1 END
        FROM exam_histories h
        JOIN exams e ON e.id = h.exam_id
        JOIN questions q ON q.exam_id = e.id
        WHERE h.user_id = @UserId AND LEFT(e.title, LEN(@Marker)) = @Marker;

        DECLARE @LogNo INT = 1;
        WHILE @LogNo <= 10
        BEGIN
            INSERT INTO system_logs (user_id, action, ip_address, timestamp)
            VALUES (@UserId,
                    CASE @LogNo % 5 WHEN 0 THEN 'LOGIN' WHEN 1 THEN 'VIEW_LESSON'
                         WHEN 2 THEN 'COMPLETE_LESSON' WHEN 3 THEN 'TAKE_EXAM' ELSE 'REVIEW_FLASHCARD' END,
                    CONCAT('10.0.1.', @UserIndex), DATEADD(HOUR, -(@UserIndex * @LogNo), GETDATE()));
            SET @LogNo += 1;
        END;

        INSERT INTO auth_tokens
            (user_id, token_type, token_value, expires_at, revoked_at, used_at, ip_address, user_agent, created_at)
        VALUES
            (@UserId, 'session', CONCAT('loadtest-session-', @UserId), DATEADD(DAY, 7, GETDATE()), NULL, NULL,
             CONCAT('10.0.1.', @UserIndex), 'ESPP Load Test Browser', GETDATE()),
            (@UserId, 'password_reset', CONCAT('loadtest-reset-', @UserId), DATEADD(HOUR, -1, GETDATE()), NULL,
             CASE WHEN @UserIndex % 2 = 0 THEN GETDATE() ELSE NULL END,
             CONCAT('10.0.1.', @UserIndex), 'ESPP Load Test Browser', DATEADD(HOUR, -2, GETDATE()));

        FETCH NEXT FROM student_cursor INTO @UserId, @UserIndex;
    END;
    CLOSE student_cursor;
    DEALLOCATE student_cursor;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF CURSOR_STATUS('local', 'lesson_cursor') >= 0
    BEGIN
        CLOSE lesson_cursor;
    END;
    IF CURSOR_STATUS('local', 'lesson_cursor') > -3 DEALLOCATE lesson_cursor;
    IF CURSOR_STATUS('local', 'student_cursor') >= 0
    BEGIN
        CLOSE student_cursor;
    END;
    IF CURSOR_STATUS('local', 'student_cursor') > -3 DEALLOCATE student_cursor;
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- Verification summary. Expected counts are shown in the result set.
SELECT 'users' AS table_name, COUNT(*) AS actual_count, 14 AS expected_count
FROM users WHERE email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'student_users', COUNT(*), 12 FROM student_users su JOIN users u ON u.id = su.user_id WHERE u.email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'lessons', COUNT(*), 18 FROM lessons WHERE title LIKE N'[[]LOADTEST]%'
UNION ALL SELECT 'lesson_contents', COUNT(*), 144 FROM lesson_contents lc JOIN lessons l ON l.id = lc.lesson_id WHERE l.title LIKE N'[[]LOADTEST]%'
UNION ALL SELECT 'exams', COUNT(*), 8 FROM exams WHERE title LIKE N'[[]LOADTEST]%'
UNION ALL SELECT 'questions', COUNT(*), 80 FROM questions q JOIN exams e ON e.id = q.exam_id WHERE e.title LIKE N'[[]LOADTEST]%'
UNION ALL SELECT 'exam_histories', COUNT(*), 96 FROM exam_histories h JOIN users u ON u.id = h.user_id WHERE u.email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'exam_attemp_detail', COUNT(*), 960 FROM exam_attemp_detail d JOIN exam_histories h ON h.id = d.exam_history_id JOIN users u ON u.id = h.user_id WHERE u.email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'learning_progress', COUNT(*), 216 FROM learning_progress p JOIN users u ON u.id = p.user_id WHERE u.email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'book_marks', COUNT(*), 72 FROM book_marks b JOIN users u ON u.id = b.user_id WHERE u.email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'srs_reviews', COUNT(*), 96 FROM srs_reviews s JOIN users u ON u.id = s.user_id WHERE u.email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'system_logs', COUNT(*), 120 FROM system_logs s JOIN users u ON u.id = s.user_id WHERE u.email LIKE 'loadtest.%@espp.local'
UNION ALL SELECT 'auth_tokens', COUNT(*), 24 FROM auth_tokens a JOIN users u ON u.id = a.user_id WHERE u.email LIKE 'loadtest.%@espp.local';
GO
