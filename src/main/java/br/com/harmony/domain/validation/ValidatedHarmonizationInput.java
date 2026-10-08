package br.com.harmony.domain.validation;

import br.com.harmony.domain.music.Chord;
import br.com.harmony.domain.music.Key;
import br.com.harmony.domain.music.Pitch;
import br.com.harmony.domain.music.SupportedChords;
import br.com.harmony.domain.music.Voice;
import java.util.List;
import java.util.Objects;

public record ValidatedHarmonizationInput(Key key, List<Chord> chords, List<Pitch> sopranoMelody) {
    private static final int MAX_EVENTS = 8;

    public ValidatedHarmonizationInput {
        Objects.requireNonNull(key, "key");
        chords = List.copyOf(chords);
        sopranoMelody = List.copyOf(sopranoMelody);
        if (chords.isEmpty() || sopranoMelody.isEmpty()) {
            throw new IllegalArgumentException("Chord and Soprano sequences must not be empty");
        }
        if (chords.size() != sopranoMelody.size()) {
            throw new IllegalArgumentException("Chord and Soprano sequence lengths must match");
        }
        if (chords.size() > MAX_EVENTS) {
            throw new IllegalArgumentException("Phrase may contain at most 8 events");
        }
        if (chords.stream().anyMatch(chord -> !SupportedChords.supports(key, chord))) {
            throw new IllegalArgumentException("Every chord must be supported by the selected key");
        }
        if (sopranoMelody.stream().anyMatch(pitch -> !key.supports(pitch.pitchClass()))) {
            throw new IllegalArgumentException("Every Soprano pitch must be supported by the selected key");
        }
        if (sopranoMelody.stream().anyMatch(pitch -> !Voice.SOPRANO.isWithinAbsoluteRange(pitch))) {
            throw new IllegalArgumentException("Every Soprano pitch must be within its absolute range");
        }
    }
}
