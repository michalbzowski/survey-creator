package pl.bzowski.integrations.messenger;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyParser {

    // Wzorzec regex dopasowujący UUID w standardowym formacie 8-4-4-4-12 znaków szesnastkowych
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"
    );

    // Wzorzec regex dopasowujący adres email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    /**
     * Szuka UUID w podanym tekście i zwraca go jako UUID,
     * lub null jeśli nie znaleziono poprawnego UUID
     */
    public static UUID parseUuidFromText(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = UUID_PATTERN.matcher(text);
        if (matcher.find()) {
            String uuidStr = matcher.group();
            try {
                return UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                // Niepoprawny format UUID
                return null;
            }
        }
        return null; // Brak UUID w tekście
    }

    public static String parseEmailFromText(String text) {
        if (text == null) {
            return "";
        }
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
