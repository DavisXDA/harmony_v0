package br.com.harmony.domain.music;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Pitch(PitchClass pitchClass, int octave) implements Comparable<Pitch> {
    private static final Pattern CANONICAL = Pattern.compile("^(C|D|E|F|G|A|B|F#|Bb)([0-9]+)$");

    public Pitch {
        Objects.requireNonNull(pitchClass, "pitchClass");
        if (octave < 0) throw new IllegalArgumentException("Octave must be non-negative");
        Math.addExact(Math.multiplyExact(12, octave + 1), pitchClass.semitone());
    }

    public static Pitch parse(String value) {
        if (value == null) throw new IllegalArgumentException("Pitch is required");
        Matcher matcher = CANONICAL.matcher(value);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid canonical pitch: " + value);
        try {
            return new Pitch(PitchClass.parse(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException("Pitch octave is too large: " + value, exception);
        }
    }

    public int pitchNumber() {
        return 12 * (octave + 1) + pitchClass.semitone();
    }

    @Override public int compareTo(Pitch other) { return Integer.compare(pitchNumber(), other.pitchNumber()); }
    @Override public String toString() { return pitchClass.symbol() + octave; }
}
