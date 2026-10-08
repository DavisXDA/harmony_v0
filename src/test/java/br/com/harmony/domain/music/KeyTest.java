package br.com.harmony.domain.music;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KeyTest {
    @ParameterizedTest @ValueSource(strings = {"C", "G", "F"})
    void supportedKeysRoundTrip(String text) { assertEquals(text, Key.parse(text).toString()); }

    @ParameterizedTest @ValueSource(strings = {"D", "Am", "C major", "c", ""})
    void rejectsUnsupportedValues(String text) { assertThrows(IllegalArgumentException.class, () -> Key.parse(text)); }

    @Test void exposesExactCPitchClasses() {
        assertEquals(Set.of(PitchClass.C, PitchClass.D, PitchClass.E, PitchClass.F, PitchClass.G, PitchClass.A, PitchClass.B), Key.C.pitchClasses());
        assertFalse(Key.C.supports(PitchClass.F_SHARP)); assertFalse(Key.C.supports(PitchClass.B_FLAT));
    }
    @Test void exposesExactGPitchClasses() {
        assertEquals(Set.of(PitchClass.G, PitchClass.A, PitchClass.B, PitchClass.C, PitchClass.D, PitchClass.E, PitchClass.F_SHARP), Key.G.pitchClasses());
        assertFalse(Key.G.supports(PitchClass.F));
    }
    @Test void exposesExactFPitchClasses() {
        assertEquals(Set.of(PitchClass.F, PitchClass.G, PitchClass.A, PitchClass.B_FLAT, PitchClass.C, PitchClass.D, PitchClass.E), Key.F.pitchClasses());
        assertFalse(Key.F.supports(PitchClass.B));
    }
}
