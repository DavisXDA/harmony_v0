package br.com.harmony.domain.music;

public enum Voice {
    SOPRANO("C4", "G5", "D4", "F5"),
    ALTO("G3", "D5", "G3", "C5"),
    TENOR("C3", "G4", "C3", "E4"),
    BASS("E2", "C4", "G2", "G3");

    private final VoiceRange range;

    Voice(String absoluteMinimum, String absoluteMaximum, String comfortableMinimum, String comfortableMaximum) {
        range = new VoiceRange(Pitch.parse(absoluteMinimum), Pitch.parse(absoluteMaximum),
                Pitch.parse(comfortableMinimum), Pitch.parse(comfortableMaximum));
    }

    public VoiceRange range() { return range; }
    public boolean isWithinAbsoluteRange(Pitch pitch) { return range.isWithinAbsoluteRange(pitch); }
    public boolean isWithinComfortableRange(Pitch pitch) { return range.isWithinComfortableRange(pitch); }
}
