package edu.fu.common.converter;

import edu.fu.common.enums.AuthTokenType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

/**
 * Maps {@link AuthTokenType} (Java, uppercase) to the {@code auth_tokens.token_type}
 * column (SQL Server, lowercase — enforced by {@code CK_auth_tokens_token_type}).
 */
@Converter(autoApply = false)
public class AuthTokenTypeConverter implements AttributeConverter<AuthTokenType, String> {

    @Override
    public String convertToDatabaseColumn(AuthTokenType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public AuthTokenType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AuthTokenType.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}
