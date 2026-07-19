USE [EnglishLearningDB];
GO

/*
  Sửa lỗi:
  Conversion failed when converting the nvarchar value 'READING' to data type smallint.

  Nguyên nhân:
  Entity Question đang dùng @Enumerated(EnumType.STRING), nên Java lưu skill là
  LISTENING / READING / SPEAKING / WRITING. Nhưng database cũ đang để
  questions.skill là smallint và còn có CHECK constraint phụ thuộc vào cột này.
*/

DECLARE @ConstraintName SYSNAME;
DECLARE @Sql NVARCHAR(MAX);

SELECT TOP 1 @ConstraintName = cc.name
FROM sys.check_constraints cc
JOIN sys.columns c
    ON c.object_id = cc.parent_object_id
WHERE cc.parent_object_id = OBJECT_ID(N'dbo.questions')
  AND c.name = N'skill'
  AND cc.definition LIKE N'%skill%';

IF @ConstraintName IS NOT NULL
BEGIN
    SET @Sql = N'ALTER TABLE dbo.questions DROP CONSTRAINT [' + @ConstraintName + N'];';
    EXEC sp_executesql @Sql;
END
GO

IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.questions')
      AND name = N'skill'
      AND system_type_id <> TYPE_ID(N'nvarchar')
)
BEGIN
    ALTER TABLE dbo.questions ALTER COLUMN skill NVARCHAR(30) NOT NULL;
END
GO

ALTER TABLE dbo.questions
ADD CONSTRAINT CK_questions_skill
CHECK (skill IN (N'LISTENING', N'READING', N'SPEAKING', N'WRITING'));
GO
