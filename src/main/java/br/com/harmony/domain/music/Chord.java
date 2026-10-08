package br.com.harmony.domain.music;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Chord(PitchClass root, ChordQuality quality) {
    private static final Pattern CANONICAL = Pattern.compile("^(C|D|E|F|G|A|B|F#|Bb)(m)?$");

    public Chord { Objects.requireNonNull(root); Objects.requireNonNull(quality); }

    public static Chord parse(String value) {
        if (value == null) throw new IllegalArgumentException("Chord is required");
        Matcher matcher = CANONICAL.matcher(value);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid canonical chord: " + value);
        return new Chord(PitchClass.parse(matcher.group(1)), matcher.group(2) == null ? ChordQuality.MAJOR : ChordQuality.MINOR);
    }

    public String symbol() { return root.symbol() + (quality == ChordQuality.MINOR ? "m" : ""); }
    @Override public String toString() { return symbol(); }
}
