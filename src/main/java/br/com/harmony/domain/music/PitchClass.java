package br.com.harmony.domain.music;

import java.util.Arrays;

public enum PitchClass {
    C("C", 0), D("D", 2), E("E", 4), F("F", 5), G("G", 7),
    A("A", 9), B("B", 11), F_SHARP("F#", 6), B_FLAT("Bb", 10);

    private final String symbol;
    private final int semitone;

    PitchClass(String symbol, int semitone) {
        this.symbol = symbol;
        this.semitone = semitone;
    }

    public String symbol() { return symbol; }
    public int semitone() { return semitone; }

    public static PitchClass parse(String value) {
        return Arrays.stream(values()).filter(pc -> pc.symbol.equals(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid canonical pitch class: " + value));
    }

    @Override public String toString() { return symbol; }
}
