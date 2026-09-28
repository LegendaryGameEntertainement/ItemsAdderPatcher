package mathrixte.itemsAdderPatcher.pack;

import java.util.List;
import java.util.Objects;

public final class FontPatcher {

    private final List<String> blockedTextures;

    public FontPatcher(List<String> blockedTextures) {
        this.blockedTextures = Objects.requireNonNull(blockedTextures, "blockedTextures");
    }

    public String patch(String json) {
        Objects.requireNonNull(json, "json");

        String result = json;

        for (String texture : blockedTextures) {
            result = removeAllProvidersContaining(result, texture);
        }

        return result;
    }

    private String removeAllProvidersContaining(String json, String needle) {
        String result = json;
        int needleIndex = result.indexOf(needle);

        while (needleIndex >= 0) {
            result = removeProviderAt(result, needleIndex);
            needleIndex = result.indexOf(needle);
        }

        return result;
    }

    private String removeProviderAt(String json, int needleIndex) {
        int objectStart = json.lastIndexOf('{', needleIndex);
        if (objectStart < 0) {
            throw new IllegalStateException(
                    "Provider introuvable autour de l'index " + needleIndex
            );
        }

        int objectEnd = findObjectEnd(json, objectStart);
        if (objectEnd < 0) {
            throw new IllegalStateException(
                    "Objet JSON mal formé à partir de l'index " + objectStart
            );
        }

        int removeStart = objectStart;
        int removeEnd = objectEnd + 1;

        removeEnd = skipWhitespaceForward(json, removeEnd);

        if (removeEnd < json.length() && json.charAt(removeEnd) == ',') {
            removeEnd++;
        } else {
            int beforeComma = skipWhitespaceBackward(json, removeStart - 1);
            if (beforeComma >= 0 && json.charAt(beforeComma) == ',') {
                removeStart = beforeComma;
            }
        }

        return json.substring(0, removeStart) + json.substring(removeEnd);
    }

    private int skipWhitespaceForward(String text, int index) {
        int cursor = index;
        while (cursor < text.length() && Character.isWhitespace(text.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private int skipWhitespaceBackward(String text, int index) {
        int cursor = index;
        while (cursor >= 0 && Character.isWhitespace(text.charAt(cursor))) {
            cursor--;
        }
        return cursor;
    }

    private int findObjectEnd(String json, int start) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < json.length(); i++) {
            char current = json.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }
}