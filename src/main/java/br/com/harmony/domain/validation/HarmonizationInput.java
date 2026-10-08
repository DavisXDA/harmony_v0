package br.com.harmony.domain.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record HarmonizationInput(String key, List<String> chords, List<String> melody) {
    public HarmonizationInput {
        chords = chords == null ? null : Collections.unmodifiableList(new ArrayList<>(chords));
        melody = melody == null ? null : Collections.unmodifiableList(new ArrayList<>(melody));
    }
}
