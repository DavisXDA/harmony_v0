package br.com.harmony.domain.validation;

import java.util.List;
import java.util.Optional;

public record ValidationResult(ValidatedHarmonizationInput value, List<ValidationError> errors) {
    public ValidationResult { errors = List.copyOf(errors); }
    public boolean isValid() { return errors.isEmpty(); }
    public Optional<ValidatedHarmonizationInput> validatedInput() { return Optional.ofNullable(value); }
}
