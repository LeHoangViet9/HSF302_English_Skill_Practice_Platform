package edu.fu.common.converter;

import edu.fu.common.enums.StudentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

/**
 * Maps {@link StudentStatus} (Java, uppercase) to the {@code student_users.status}
 * column (SQL Server, lowercase — enforced by {@code CK_student_users_status}).
 */
@Converter(autoApply = false)
public class StudentStatusConverter implements AttributeConverter<StudentStatus, String> {

    @Override
    public String convertToDatabaseColumn(StudentStatus attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public StudentStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : StudentStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}
