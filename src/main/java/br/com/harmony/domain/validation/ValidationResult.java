package br.com.harmony.domain.validation;

import java.util.List;
import java.util.Optional;

public record ValidationResult(ValidatedHarmonizationInput value, List<ValidationError> errors) {
    public ValidationResult {
        errors = List.copyOf(errors);
        if ((value == null) == errors.isEmpty()) {
            throw new IllegalArgumentException("Result must contain either a validated input or one or more errors");
        }
    }

    public static ValidationResult success(ValidatedHarmonizationInput value) {
        return new ValidationResult(value, List.of());
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(null, errors);
    }

    public boolean isValid() { return value != null; }
    public Optional<ValidatedHarmonizationInput> validatedInput() { return Optional.ofNullable(value); }
}
