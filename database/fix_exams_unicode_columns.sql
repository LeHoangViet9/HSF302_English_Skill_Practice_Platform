USE EnglishLearningDB;
GO

-- 1. Sửa bảng exams (Cột mô tả)
ALTER TABLE exams ALTER COLUMN description NVARCHAR(MAX) NULL;

-- 2. Sửa bảng questions (Cột nội dung câu hỏi, lựa chọn, giải thích)
ALTER TABLE questions ALTER COLUMN question_text NVARCHAR(MAX) NOT NULL;
ALTER TABLE questions ALTER COLUMN options NVARCHAR(MAX) NULL;
ALTER TABLE questions ALTER COLUMN explanation NVARCHAR(MAX) NULL;

-- 3. Sửa bảng users (Họ tên)
ALTER TABLE users ALTER COLUMN full_name NVARCHAR(150) NOT NULL;

-- 4. Sửa bảng student_users (Họ tên)
ALTER TABLE student_users ALTER COLUMN full_name NVARCHAR(150) NOT NULL;
GO
