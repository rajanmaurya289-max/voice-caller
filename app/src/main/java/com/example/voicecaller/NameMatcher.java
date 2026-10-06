package com.example.voicecaller;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class NameMatcher {

    private NameMatcher() {}

    public static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFC)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^\\p{L}\\p{M}\\p{N}\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    public static String extractName(String sentence) {
        String s = normalize(sentence);

        s = s.replaceFirst(
            "^(?:please\\s+)?(?:call|phone|dial|कॉल|फोन)\\s+",
            "");

        s = s.replaceFirst(
            "\\s+(?:(?:ko|को)\\s+)?(?:call|phone|कॉल|फोन)"
            + "(?:\\s+(?:karo|kar|करो|कर|kijiye|कीजिए|do|दो))?$",
            "");

        s = s.replaceFirst("\\s+(?:ko|को)$", "");

        if (s.matches("(?:call|phone|dial|कॉल|फोन|please)")) {
            return "";
        }

        return s.trim();
    }

    public static boolean containsAllWords(
            String contact, String query) {

        String q = normalize(query);
        if (q.isEmpty()) return false;

        Set<String> words = new HashSet<>(
            Arrays.asList(normalize(contact).split(" ")));

        return words.containsAll(Arrays.asList(q.split(" ")));
    }
}
