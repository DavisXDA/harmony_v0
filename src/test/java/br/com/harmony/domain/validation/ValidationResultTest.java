package br.com.harmony.domain.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.harmony.domain.music.Chord;
import br.com.harmony.domain.music.Key;
import br.com.harmony.domain.music.Pitch;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationResultTest {
    private static final ValidationError ERROR = new ValidationError(
            "key", ValidationErrorCode.REQUIRED_FIELD, "Key is required.", null);

    @Test
    void successRequiresValueAndNoErrors() {
        ValidationResult result = ValidationResult.success(validatedInput());

        assertTrue(result.isValid());
        assertTrue(result.validatedInput().isPresent());
        assertTrue(result.errors().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new ValidationResult(null, List.of()));
    }

    @Test
    void failureRequiresErrorsAndNoValue() {
        ValidationResult result = ValidationResult.failure(List.of(ERROR));

        assertFalse(result.isValid());
        assertTrue(result.validatedInput().isEmpty());
        assertFalse(result.errors().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new ValidationResult(validatedInput(), List.of(ERROR)));
    }

    @Test
    void factoriesRejectContradictoryStates() {
        assertThrows(IllegalArgumentException.class, () -> ValidationResult.success(null));
        assertThrows(IllegalArgumentException.class, () -> ValidationResult.failure(List.of()));
        assertThrows(NullPointerException.class, () -> ValidationResult.failure(null));
    }

    @Test
    void errorsAreDefensivelyCopiedAndImmutable() {
        List<ValidationError> errors = new ArrayList<>(List.of(ERROR));
        ValidationResult result = ValidationResult.failure(errors);
        errors.clear();

        assertFalse(result.errors().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> result.errors().add(ERROR));
    }

    private static ValidatedHarmonizationInput validatedInput() {
        return new ValidatedHarmonizationInput(
                Key.C, List.of(Chord.parse("C")), List.of(Pitch.parse("D4")));
    }
}
