package br.com.harmony.domain.music;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VoiceRangeTest {
    @ParameterizedTest
    @CsvSource({"SOPRANO,C4,G5,B3,A5,D4,F5", "ALTO,G3,D5,F#3,E5,G3,C5", "TENOR,C3,G4,B2,A4,C3,E4", "BASS,E2,C4,D2,D4,G2,G3"})
    void boundaries(Voice voice, String min, String max, String below, String above, String comfortableMin, String comfortableMax) {
        assertTrue(voice.isWithinAbsoluteRange(Pitch.parse(min)));
        assertTrue(voice.isWithinAbsoluteRange(Pitch.parse(max)));
        assertFalse(voice.isWithinAbsoluteRange(Pitch.parse(below)));
        assertFalse(voice.isWithinAbsoluteRange(Pitch.parse(above)));
        assertTrue(voice.isWithinComfortableRange(Pitch.parse(comfortableMin)));
        assertTrue(voice.isWithinComfortableRange(Pitch.parse(comfortableMax)));
    }
}
