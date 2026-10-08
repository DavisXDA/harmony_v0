package br.com.harmony.domain.music;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ChordTest {
    @ParameterizedTest @CsvSource({"C,MAJOR", "Bb,MAJOR", "Dm,MINOR", "F#m,MINOR"})
    void parsesQualities(String symbol, ChordQuality quality) {
        Chord chord = Chord.parse(symbol); assertEquals(quality, chord.quality()); assertEquals(symbol, chord.toString());
    }

    @ParameterizedTest @ValueSource(strings = {"C7", "C/E", "Bdim", "Csus4", "C m", "A#", "Gb", "c", ""})
    void rejectsInvalidSyntax(String symbol) { assertThrows(IllegalArgumentException.class, () -> Chord.parse(symbol)); }

    @ParameterizedTest
    @CsvSource({"C,C", "C,Dm", "C,Em", "C,F", "C,G", "C,Am", "G,G", "G,Am", "G,Bm", "G,C", "G,D", "G,Em", "F,F", "F,Gm", "F,Am", "F,Bb", "F,C", "F,Dm"})
    void supportsApprovedChords(Key key, String symbol) { assertTrue(SupportedChords.supports(key, Chord.parse(symbol))); }

    @ParameterizedTest @CsvSource({"C,D", "G,F", "F,G", "C,Bb", "G,F#"})
    void rejectsChordOutsideKey(Key key, String symbol) { assertFalse(SupportedChords.supports(key, Chord.parse(symbol))); }

    @Test void eachKeyHasSixChords() { for (Key key : Key.values()) assertEquals(6, SupportedChords.forKey(key).size()); }
}
