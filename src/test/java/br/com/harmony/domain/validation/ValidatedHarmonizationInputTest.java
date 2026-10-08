package br.com.harmony.domain.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.harmony.domain.music.Chord;
import br.com.harmony.domain.music.Key;
import br.com.harmony.domain.music.Pitch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidatedHarmonizationInputTest {
    @Test
    void rejectsNullKey() {
        assertThrows(NullPointerException.class, () -> input(null, List.of("C"), List.of("C4")));
    }

    @Test
    void rejectsNullSequences() {
        assertThrows(NullPointerException.class, () -> new ValidatedHarmonizationInput(Key.C, null, List.of(Pitch.parse("C4"))));
        assertThrows(NullPointerException.class, () -> new ValidatedHarmonizationInput(Key.C, List.of(Chord.parse("C")), null));
    }

    @Test
    void rejectsEmptySequences() {
        assertThrows(IllegalArgumentException.class, () -> input(Key.C, List.of(), List.of()));
    }

    @Test
    void rejectsUnequalSequenceLengths() {
        assertThrows(IllegalArgumentException.class, () -> input(Key.C, List.of("C"), List.of("C4", "D4")));
    }

    @Test
    void rejectsMoreThanEightEvents() {
        assertThrows(IllegalArgumentException.class, () -> input(
                Key.C, Collections.nCopies(9, "C"), Collections.nCopies(9, "C4")));
    }

    @Test
    void rejectsNullSequenceElements() {
        assertThrows(NullPointerException.class, () -> new ValidatedHarmonizationInput(
                Key.C, Collections.singletonList(null), List.of(Pitch.parse("C4"))));
        assertThrows(NullPointerException.class, () -> new ValidatedHarmonizationInput(
                Key.C, List.of(Chord.parse("C")), Collections.singletonList(null)));
    }

    @Test
    void rejectsChordUnsupportedByKey() {
        assertThrows(IllegalArgumentException.class, () -> input(Key.C, List.of("D"), List.of("D4")));
    }

    @Test
    void rejectsSopranoPitchUnsupportedByKey() {
        assertThrows(IllegalArgumentException.class, () -> input(Key.G, List.of("G"), List.of("F4")));
    }

    @Test
    void rejectsSopranoPitchOutsideAbsoluteRange() {
        assertThrows(IllegalArgumentException.class, () -> input(Key.C, List.of("C"), List.of("C6")));
    }

    @Test
    void permitsSupportedNonChordToneAndDefensivelyCopiesSequences() {
        List<Chord> chords = new ArrayList<>(List.of(Chord.parse("C")));
        List<Pitch> melody = new ArrayList<>(List.of(Pitch.parse("D4")));

        ValidatedHarmonizationInput input = new ValidatedHarmonizationInput(Key.C, chords, melody);
        chords.clear();
        melody.clear();

        assertThrows(UnsupportedOperationException.class, () -> input.chords().add(Chord.parse("C")));
        assertThrows(UnsupportedOperationException.class, () -> input.sopranoMelody().add(Pitch.parse("C4")));
    }

    private static ValidatedHarmonizationInput input(Key key, List<String> chordSymbols, List<String> pitches) {
        return new ValidatedHarmonizationInput(
                key,
                chordSymbols.stream().map(Chord::parse).toList(),
                pitches.stream().map(Pitch::parse).toList());
    }
}
