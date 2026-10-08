package br.com.harmony.domain.validation;

import static br.com.harmony.domain.validation.ValidationErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class M1IndependentQaTest {
    private final HarmonizationInputValidator validator = new HarmonizationInputValidator();

    @Test
    void malformedKeyDoesNotSuppressIndependentSopranoRangeError() {
        assertEquals(
                List.of("key:INVALID_KEY_FORMAT", "melody[0]:SOPRANO_OUT_OF_RANGE"),
                locationsAndCodes(new HarmonizationInput("C major", List.of("C"), List.of("C6"))));
    }

    @Test
    void unsupportedPitchDoesNotSuppressIndependentSopranoRangeError() {
        assertEquals(
                List.of("melody[0]:SOPRANO_OUT_OF_RANGE", "melody[0]:UNSUPPORTED_PITCH"),
                locationsAndCodes(new HarmonizationInput("G", List.of("G"), List.of("F6"))));
    }

    @Test
    void fieldErrorsFollowNormativeFieldLocationAndCodeOrdering() {
        assertEquals(
                List.of(
                        "key:INVALID_KEY_FORMAT",
                        "chords:PHRASE_TOO_LONG",
                        "chords:SEQUENCE_LENGTH_MISMATCH",
                        "melody:PHRASE_TOO_LONG"),
                locationsAndCodes(new HarmonizationInput(
                        "C major",
                        Collections.nCopies(9, "C"),
                        Collections.nCopies(10, "C4"))));
    }

    private List<String> locationsAndCodes(HarmonizationInput input) {
        return validator.validate(input).errors().stream()
                .map(error -> error.field() + ":" + error.code())
                .toList();
    }
}
