package br.com.harmony.domain.music;

import java.util.Objects;

public record VoiceRange(Pitch absoluteMinimum, Pitch absoluteMaximum,
                         Pitch comfortableMinimum, Pitch comfortableMaximum) {
    public VoiceRange {
        Objects.requireNonNull(absoluteMinimum); Objects.requireNonNull(absoluteMaximum);
        Objects.requireNonNull(comfortableMinimum); Objects.requireNonNull(comfortableMaximum);
        if (absoluteMinimum.compareTo(comfortableMinimum) > 0
                || comfortableMinimum.compareTo(comfortableMaximum) > 0
                || comfortableMaximum.compareTo(absoluteMaximum) > 0) {
            throw new IllegalArgumentException("Comfortable range must be ordered within absolute range");
        }
    }

    public boolean isWithinAbsoluteRange(Pitch pitch) { return contains(pitch, absoluteMinimum, absoluteMaximum); }
    public boolean isWithinComfortableRange(Pitch pitch) { return contains(pitch, comfortableMinimum, comfortableMaximum); }

    private static boolean contains(Pitch pitch, Pitch minimum, Pitch maximum) {
        Objects.requireNonNull(pitch);
        return pitch.compareTo(minimum) >= 0 && pitch.compareTo(maximum) <= 0;
    }
}
