package ru.practicum.event.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TimeAtLeastTwoHoursValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeAtLeastTwoHours {
    String message() default "Время не может быть раньше, чем через два часа";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
