package br.com.harmony.domain.music;

import java.util.Arrays;
import java.util.Set;

public enum Key {
    C("C", Set.of(PitchClass.C, PitchClass.D, PitchClass.E, PitchClass.F, PitchClass.G, PitchClass.A, PitchClass.B)),
    G("G", Set.of(PitchClass.G, PitchClass.A, PitchClass.B, PitchClass.C, PitchClass.D, PitchClass.E, PitchClass.F_SHARP)),
    F("F", Set.of(PitchClass.F, PitchClass.G, PitchClass.A, PitchClass.B_FLAT, PitchClass.C, PitchClass.D, PitchClass.E));

    private final String symbol;
    private final Set<PitchClass> pitchClasses;

    Key(String symbol, Set<PitchClass> pitchClasses) {
        this.symbol = symbol;
        this.pitchClasses = pitchClasses;
    }

    public static Key parse(String value) {
        return Arrays.stream(values()).filter(key -> key.symbol.equals(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported key: " + value));
    }

    public boolean supports(PitchClass pitchClass) { return pitchClasses.contains(pitchClass); }
    public Set<PitchClass> pitchClasses() { return pitchClasses; }
    @Override public String toString() { return symbol; }
}
