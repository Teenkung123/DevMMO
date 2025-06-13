package com.teenkung.devmmo.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorTranslator {

    // Map single-character color/format codes to MiniMessage tags
    // (Including some extras like &m, &n, &k => <obfuscated>, etc.)
    private static final Map<Character, String> LEGACY_TO_MINI;
    static {
        LEGACY_TO_MINI = new HashMap<>();
        // Colors
        LEGACY_TO_MINI.put('0', "black");
        LEGACY_TO_MINI.put('1', "dark_blue");
        LEGACY_TO_MINI.put('2', "dark_green");
        LEGACY_TO_MINI.put('3', "dark_aqua");
        LEGACY_TO_MINI.put('4', "dark_red");
        LEGACY_TO_MINI.put('5', "dark_purple");
        LEGACY_TO_MINI.put('6', "gold");
        LEGACY_TO_MINI.put('7', "gray");
        LEGACY_TO_MINI.put('8', "dark_gray");
        LEGACY_TO_MINI.put('9', "blue");
        LEGACY_TO_MINI.put('a', "green");
        LEGACY_TO_MINI.put('b', "aqua");
        LEGACY_TO_MINI.put('c', "red");
        LEGACY_TO_MINI.put('d', "light_purple");
        LEGACY_TO_MINI.put('e', "yellow");
        LEGACY_TO_MINI.put('f', "white");

        // Formats
        LEGACY_TO_MINI.put('k', "obfuscated");
        LEGACY_TO_MINI.put('l', "bold");
        LEGACY_TO_MINI.put('m', "strikethrough");
        LEGACY_TO_MINI.put('n', "underlined");
        LEGACY_TO_MINI.put('o', "italic");
        LEGACY_TO_MINI.put('r', "reset");
    }

    /**
     * A single regex that matches either:
     * 1) &#RRGGBB or §#RRGGBB   (hex colors)
     * 2) &X or §X               (where X is [0-9A-FK-OR], standard codes)
     */
    private static final Pattern COMBINED_PATTERN = Pattern.compile(
            "(?i)([&§]#[0-9A-F]{6}|[&§][0-9A-FK-OR])"
    );

    /**
     * Converts all legacy color codes (both & and §) into MiniMessage tags.
     * For example:
     *   "§aHello &lWorld &#12fa43Hex" => "<green>Hello <bold>World <color:#12fa43>Hex"
     */
    public static String toMiniMessageFormat(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        Matcher matcher = COMBINED_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            // Group(1) will be something like "§a", "&l", "&#12FA43", or "§#12FA43"
            String match = matcher.group(1);
            char firstChar = match.charAt(0); // Could be '&' or '§'

            if (match.length() >= 2 && match.charAt(1) == '#') {
                // This is a hex code (e.g. "&#12FA43" or "§#12FA43")
                // Remove the leading '&' or '§', leaving "#12FA43"
                String hex = match.substring(1);
                // Turn it into "<color:#12FA43>"
                matcher.appendReplacement(sb, "<color:" + hex + ">");
            } else {
                // It's a standard color/format code, e.g. "&a", "§l", etc.
                // match.charAt(1) is the actual code character
                char code = Character.toLowerCase(match.charAt(1));
                String miniTag = LEGACY_TO_MINI.get(code);
                if (miniTag != null) {
                    matcher.appendReplacement(sb, "<" + miniTag + ">");
                } else {
                    // If not recognized, strip it out (shouldn't happen, but just in case)
                    matcher.appendReplacement(sb, "");
                }
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
