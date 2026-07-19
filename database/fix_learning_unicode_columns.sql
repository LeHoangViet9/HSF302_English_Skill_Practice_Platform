USE EnglishLearningDB;
GO

SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN ('lessons', 'lesson_contents')
  AND COLUMN_NAME IN (
      'title',
      'description',
      'word_or_structure',
      'ipa',
      'meaning',
      'explanation',
      'example'
  )
ORDER BY TABLE_NAME, COLUMN_NAME;
GO

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'lessons' AND COLUMN_NAME = 'title' AND DATA_TYPE <> 'nvarchar'
)
BEGIN
    ALTER TABLE lessons ALTER COLUMN title NVARCHAR(255) NOT NULL;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'lessons' AND COLUMN_NAME = 'description' AND DATA_TYPE <> 'nvarchar'
)
BEGIN
    ALTER TABLE lessons ALTER COLUMN description NVARCHAR(MAX) NULL;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'lesson_contents' AND COLUMN_NAME = 'word_or_structure' AND DATA_TYPE <> 'nvarchar'
)
BEGIN
    ALTER TABLE lesson_contents ALTER COLUMN word_or_structure NVARCHAR(255) NOT NULL;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'lesson_contents' AND COLUMN_NAME = 'ipa' AND DATA_TYPE <> 'nvarchar'
)
BEGIN
    ALTER TABLE lesson_contents ALTER COLUMN ipa NVARCHAR(100) NULL;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'lesson_contents' AND COLUMN_NAME = 'meaning' AND DATA_TYPE <> 'nvarchar'
)
BEGIN
    ALTER TABLE lesson_contents ALTER COLUMN meaning NVARCHAR(255) NULL;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'lesson_contents' AND COLUMN_NAME = 'explanation' AND DATA_TYPE <> 'nvarchar'
)
BEGIN
    ALTER TABLE lesson_contents ALTER COLUMN explanation NVARCHAR(MAX) NULL;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'lesson_contents' AND COLUMN_NAME = 'example' AND DATA_TYPE <> 'nvarchar'
)
BEGIN
    ALTER TABLE lesson_contents ALTER COLUMN example NVARCHAR(MAX) NULL;
END;
GO

SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME IN ('lessons', 'lesson_contents')
  AND COLUMN_NAME IN (
      'title',
      'description',
      'word_or_structure',
      'ipa',
      'meaning',
      'explanation',
      'example'
  )
ORDER BY TABLE_NAME, COLUMN_NAME;
GO
