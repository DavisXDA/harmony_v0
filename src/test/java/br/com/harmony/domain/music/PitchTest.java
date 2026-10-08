package br.com.harmony.domain.music;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PitchTest {
    @ParameterizedTest
    @CsvSource({"C4,60", "D4,62", "E4,64", "F4,65", "F#4,66", "G4,67", "A4,69", "Bb4,70", "B4,71", "C5,72"})
    void parsesCanonicalPitches(String text, int number) {
        Pitch pitch = Pitch.parse(text);
        assertEquals(number, pitch.pitchNumber());
        assertEquals(text, pitch.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"H4", "Gb4", "A#4", "c4", "C 4", "C#4", "C", "4C", "C-1", ""})
    void rejectsMalformedSpellings(String text) { assertThrows(IllegalArgumentException.class, () -> Pitch.parse(text)); }

    @Test void rejectsNull() { assertThrows(IllegalArgumentException.class, () -> Pitch.parse(null)); }
}
