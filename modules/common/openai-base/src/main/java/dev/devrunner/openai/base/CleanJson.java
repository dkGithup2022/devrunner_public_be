package dev.devrunner.openai.base;

public class CleanJson {

    public static String cleanJsonString(String raw) {
        if (raw == null) return null;

        // 앞뒤 트리밍 후 제거
        String cleaned = raw.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim(); // 7 == "```json".length()
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim(); // 3 == "```".length()
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        return cleaned;
    }

}
