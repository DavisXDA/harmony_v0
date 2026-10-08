package br.com.harmony.domain.validation;

import br.com.harmony.domain.music.Chord;
import br.com.harmony.domain.music.Key;
import br.com.harmony.domain.music.Pitch;
import java.util.List;

public record ValidatedHarmonizationInput(Key key, List<Chord> chords, List<Pitch> sopranoMelody) {
    public ValidatedHarmonizationInput {
        chords = List.copyOf(chords);
        sopranoMelody = List.copyOf(sopranoMelody);
    }
}
