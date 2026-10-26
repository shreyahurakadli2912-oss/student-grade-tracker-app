package com.studentgradetracker.util;

/**
 * Utility to sanitize user-controlled text fields before writing to CSV or Excel,
 * mitigating CSV/Formula Injection attacks (CWE-1236 / OWASP).
 */
public final class FormulaSanitizer {

    private FormulaSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        // If the string starts with formula trigger characters, prepend a single quote
        char firstChar = trimmed.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
            return "'" + trimmed;
        }
        return trimmed;
    }
}
