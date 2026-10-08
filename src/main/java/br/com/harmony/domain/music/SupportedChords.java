package br.com.harmony.domain.music;

import java.util.Map;
import java.util.Set;

public final class SupportedChords {
    private static final Map<Key, Set<Chord>> BY_KEY = Map.of(
            Key.C, parse("C", "Dm", "Em", "F", "G", "Am"),
            Key.G, parse("G", "Am", "Bm", "C", "D", "Em"),
            Key.F, parse("F", "Gm", "Am", "Bb", "C", "Dm"));

    private SupportedChords() {}

    public static boolean supports(Key key, Chord chord) { return BY_KEY.get(key).contains(chord); }
    public static Set<Chord> forKey(Key key) { return BY_KEY.get(key); }

    private static Set<Chord> parse(String... symbols) {
        return java.util.Arrays.stream(symbols).map(Chord::parse).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
