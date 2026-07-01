package edu.fu.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates password strength as one combined rule (BR-02: min 8 chars, 1 uppercase,
 * 1 digit) so it can surface the single AC-02-03 message rather than one message
 * per broken rule. Blank values are considered valid here — pair with
 * {@code @NotBlank} to enforce presence.
 */
@Documented
@Constraint(validatedBy = PasswordStrengthValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordStrength {

    String message() default "Mật khẩu quá yếu: cần ≥ 8 ký tự, 1 hoa, 1 số";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
