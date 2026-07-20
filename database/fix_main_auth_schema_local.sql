USE [EnglishLearningDB];
GO

/*
  Local schema fix for running the rewritten main branch on an older
  EnglishLearningDB.

  This script is safe to run more than once.

  It clears temporary auth/student-profile rows because the latest code changed:
  - auth_tokens now references users(id) through user_id
  - student_users is now a profile table referencing users(id) through user_id
*/

/* 1. Bring users table in line with the latest User entity. */
IF COL_LENGTH('dbo.users', 'login_attempts') IS NULL
BEGIN
    ALTER TABLE dbo.users
    ADD login_attempts INT NOT NULL
        CONSTRAINT DF_users_login_attempts DEFAULT (0);
END
GO

IF COL_LENGTH('dbo.users', 'locked_until') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD locked_until DATETIME2 NULL;
END
GO

IF COL_LENGTH('dbo.users', 'last_login_at') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD last_login_at DATETIME2 NULL;
END
GO

IF COL_LENGTH('dbo.users', 'last_login_ip') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD last_login_ip NVARCHAR(45) NULL;
END
GO

IF COL_LENGTH('dbo.users', 'password_changed_at') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD password_changed_at DATETIME2 NULL;
END
GO

IF COL_LENGTH('dbo.users', 'created_at') IS NULL
BEGIN
    ALTER TABLE dbo.users
    ADD created_at DATETIME2 NOT NULL
        CONSTRAINT DF_users_created_at DEFAULT (SYSUTCDATETIME());
END
GO

IF COL_LENGTH('dbo.users', 'updated_at') IS NULL
BEGIN
    ALTER TABLE dbo.users
    ADD updated_at DATETIME2 NOT NULL
        CONSTRAINT DF_users_updated_at DEFAULT (SYSUTCDATETIME());
END
GO

/* 2. Drop old foreign keys that point auth_tokens to student_users. */
DECLARE @DropFkSql NVARCHAR(MAX) = N'';

SELECT @DropFkSql = @DropFkSql +
    N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(fk.parent_object_id)) +
    N'.' + QUOTENAME(OBJECT_NAME(fk.parent_object_id)) +
    N' DROP CONSTRAINT ' + QUOTENAME(fk.name) + N';' + CHAR(13)
FROM sys.foreign_keys fk
WHERE fk.parent_object_id = OBJECT_ID(N'dbo.auth_tokens')
  AND fk.referenced_object_id = OBJECT_ID(N'dbo.student_users');

IF @DropFkSql <> N''
BEGIN
    EXEC sp_executesql @DropFkSql;
END
GO

/* 3. Clear temporary rows before changing table shapes. */
IF OBJECT_ID('dbo.auth_tokens', 'U') IS NOT NULL
BEGIN
    DELETE FROM dbo.auth_tokens;
END
GO

IF OBJECT_ID('dbo.student_users', 'U') IS NOT NULL
BEGIN
    DELETE FROM dbo.student_users;
END
GO

/* 4. Bring auth_tokens in line with the latest AuthToken entity. */
IF OBJECT_ID('dbo.auth_tokens', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.auth_tokens', 'user_id') IS NULL
    BEGIN
        ALTER TABLE dbo.auth_tokens ADD user_id BIGINT NULL;
    END
END
GO

IF OBJECT_ID('dbo.auth_tokens', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.auth_tokens', 'user_id') IS NOT NULL
BEGIN
    DECLARE @AnyUserId BIGINT;
    SELECT TOP 1 @AnyUserId = id FROM dbo.users ORDER BY id;

    IF @AnyUserId IS NOT NULL
    BEGIN
        EXEC sp_executesql
            N'UPDATE dbo.auth_tokens SET user_id = @AnyUserId WHERE user_id IS NULL;',
            N'@AnyUserId BIGINT',
            @AnyUserId = @AnyUserId;
    END
END
GO

IF OBJECT_ID('dbo.auth_tokens', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.auth_tokens', 'user_id') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.auth_tokens WHERE user_id IS NULL)
BEGIN
    ALTER TABLE dbo.auth_tokens ALTER COLUMN user_id BIGINT NOT NULL;
END
GO

IF OBJECT_ID('dbo.auth_tokens', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.auth_tokens', 'user_id') IS NOT NULL
   AND NOT EXISTS (
        SELECT 1
        FROM sys.foreign_keys
        WHERE name = N'FK_auth_tokens_user'
   )
BEGIN
    ALTER TABLE dbo.auth_tokens
    ADD CONSTRAINT FK_auth_tokens_user
    FOREIGN KEY (user_id) REFERENCES dbo.users(id);
END
GO

/* 5. Bring student_users in line with the latest StudentUser entity. */
IF OBJECT_ID('dbo.student_users', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.student_users', 'user_id') IS NULL
    BEGIN
        ALTER TABLE dbo.student_users ADD user_id BIGINT NULL;
    END
END
GO

IF OBJECT_ID('dbo.student_users', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.student_users', 'created_at') IS NULL
    BEGIN
        ALTER TABLE dbo.student_users
        ADD created_at DATETIME2 NOT NULL
            CONSTRAINT DF_student_users_created_at DEFAULT (SYSUTCDATETIME());
    END

    IF COL_LENGTH('dbo.student_users', 'updated_at') IS NULL
    BEGIN
        ALTER TABLE dbo.student_users
        ADD updated_at DATETIME2 NOT NULL
            CONSTRAINT DF_student_users_updated_at DEFAULT (SYSUTCDATETIME());
    END
END
GO

/* 5.1. Relax legacy columns left from the old standalone student account table. */
DECLARE @DropStudentEmailSql NVARCHAR(MAX) = N'';

SELECT @DropStudentEmailSql = @DropStudentEmailSql +
    N'ALTER TABLE dbo.student_users DROP CONSTRAINT ' + QUOTENAME(kc.name) + N';' + CHAR(13)
FROM sys.key_constraints kc
WHERE kc.parent_object_id = OBJECT_ID(N'dbo.student_users')
  AND kc.name = N'UQ_student_users_email';

IF @DropStudentEmailSql <> N''
BEGIN
    EXEC sp_executesql @DropStudentEmailSql;
END
GO

DECLARE @DropStudentEmailIndexSql NVARCHAR(MAX) = N'';

SELECT @DropStudentEmailIndexSql = @DropStudentEmailIndexSql +
    N'DROP INDEX ' + QUOTENAME(i.name) + N' ON dbo.student_users;' + CHAR(13)
FROM sys.indexes i
WHERE i.object_id = OBJECT_ID(N'dbo.student_users')
  AND i.name = N'UQ_student_users_email';

IF @DropStudentEmailIndexSql <> N''
BEGIN
    EXEC sp_executesql @DropStudentEmailIndexSql;
END
GO

IF OBJECT_ID('dbo.student_users', 'U') IS NOT NULL
BEGIN
    IF COL_LENGTH('dbo.student_users', 'email') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.student_users ALTER COLUMN email NVARCHAR(255) NULL;
    END

    IF COL_LENGTH('dbo.student_users', 'password_hash') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.student_users ALTER COLUMN password_hash NVARCHAR(255) NULL;
    END

    IF COL_LENGTH('dbo.student_users', 'full_name') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.student_users ALTER COLUMN full_name NVARCHAR(150) NULL;
    END

    IF COL_LENGTH('dbo.student_users', 'status') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.student_users ALTER COLUMN status NVARCHAR(30) NULL;
    END

    IF COL_LENGTH('dbo.student_users', 'is_deleted') IS NOT NULL
       AND NOT EXISTS (
            SELECT 1
            FROM sys.default_constraints
            WHERE parent_object_id = OBJECT_ID(N'dbo.student_users')
              AND name = N'DF_student_users_is_deleted'
       )
    BEGIN
        ALTER TABLE dbo.student_users
        ADD CONSTRAINT DF_student_users_is_deleted DEFAULT (0) FOR is_deleted;
    END

    IF COL_LENGTH('dbo.student_users', 'login_attempts') IS NOT NULL
       AND NOT EXISTS (
            SELECT 1
            FROM sys.default_constraints
            WHERE parent_object_id = OBJECT_ID(N'dbo.student_users')
              AND name = N'DF_student_users_login_attempts'
       )
    BEGIN
        ALTER TABLE dbo.student_users
        ADD CONSTRAINT DF_student_users_login_attempts DEFAULT (0) FOR login_attempts;
    END
END
GO

IF OBJECT_ID('dbo.student_users', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.student_users', 'user_id') IS NOT NULL
   AND NOT EXISTS (
        SELECT 1
        FROM sys.foreign_keys
        WHERE name = N'FK_student_users_user'
   )
BEGIN
    ALTER TABLE dbo.student_users
    ADD CONSTRAINT FK_student_users_user
    FOREIGN KEY (user_id) REFERENCES dbo.users(id);
END
GO

IF OBJECT_ID('dbo.student_users', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.student_users', 'user_id') IS NOT NULL
   AND NOT EXISTS (
        SELECT 1
        FROM sys.key_constraints
        WHERE name = N'UQ_student_users_user_id'
   )
BEGIN
    ALTER TABLE dbo.student_users
    ADD CONSTRAINT UQ_student_users_user_id UNIQUE (user_id);
END
GO
