package dynamic.content.library.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal recursive-descent JSON reader covering exactly what this library's schema needs
 * (objects, arrays, strings, numbers, booleans, null) - not a general-purpose JSON library.
 * Fabric Loader only vendors an internal, unexposed streaming tokenizer, so this replaces
 * pulling in a full third-party JSON library just to read a handful of flat mod-content files.
 *
 * <p>The whole input is read into a {@code String} up front (these are small hand-authored
 * content files, not data streams), then walked once with a single integer cursor ({@link #pos}).
 * There is no intermediate token stream - each {@code parseXxxValue} method both consumes
 * characters and builds the corresponding Java value in one pass, producing a plain tree of
 * {@link Map}, {@link List}, {@link String}, {@link Long}/{@link Double}, {@link Boolean} and
 * {@code null}. Use {@link JsonValue} to read typed fields back out of that tree.
 */
public final class Json {

    private final String text;
    private int pos;

    private Json(String text) {
        this.text = text;
    }

    /**
     * Parses {@code text} as a single JSON object and returns it as a {@code Map}. This is the
     * only entrypoint mod authors' schema files need, since every DCL content file is rooted at
     * an object (e.g. {@code {"items": [...]}}).
     *
     * @throws JsonParseException if {@code text} is not a well-formed JSON object.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Json parser = new Json(text);
        parser.skipWhitespace();
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.pos != parser.text.length()) {
            throw new JsonParseException("Unexpected trailing content at position " + parser.pos);
        }
        if (!(value instanceof Map)) {
            throw new JsonParseException("Expected a JSON object at the root.");
        }
        return (Map<String, Object>) value;
    }

    /** Dispatches to the appropriate parse method based on the next non-whitespace character. */
    private Object parseValue() {
        skipWhitespace();
        if (pos >= text.length()) {
            throw new JsonParseException("Unexpected end of input.");
        }
        char c = text.charAt(pos);
        switch (c) {
            case '{': return parseObjectValue();
            case '[': return parseArrayValue();
            case '"': return parseStringValue();
            case 't': return parseLiteral("true", Boolean.TRUE);
            case 'f': return parseLiteral("false", Boolean.FALSE);
            case 'n': return parseLiteral("null", null);
            default: return parseNumberValue();
        }
    }

    /** Parses a {@code { ... }} object, assuming the cursor is positioned at the opening brace. */
    private Map<String, Object> parseObjectValue() {
        Map<String, Object> result = new LinkedHashMap<>();
        pos++; // consume '{'
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return result;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw new JsonParseException("Expected string key at position " + pos);
            }
            String key = parseStringValue();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();
            char next = next();
            if (next == ',') {
                continue;
            }
            if (next == '}') {
                break;
            }
            throw new JsonParseException("Expected ',' or '}' at position " + (pos - 1));
        }
        return result;
    }

    /** Parses a {@code [ ... ]} array, assuming the cursor is positioned at the opening bracket. */
    private List<Object> parseArrayValue() {
        List<Object> result = new ArrayList<>();
        pos++; // consume '['
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return result;
        }
        while (true) {
            result.add(parseValue());
            skipWhitespace();
            char next = next();
            if (next == ',') {
                continue;
            }
            if (next == ']') {
                break;
            }
            throw new JsonParseException("Expected ',' or ']' at position " + (pos - 1));
        }
        return result;
    }

    /** Parses a quoted string, resolving backslash escapes, including four-hex-digit unicode escapes. */
    private String parseStringValue() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= text.length()) {
                throw new JsonParseException("Unterminated string.");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                break;
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= text.length()) {
                throw new JsonParseException("Unterminated escape sequence.");
            }
            char escape = text.charAt(pos++);
            switch (escape) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (pos + 4 > text.length()) {
                        throw new JsonParseException("Truncated unicode escape.");
                    }
                    sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                    pos += 4;
                    break;
                default:
                    throw new JsonParseException("Unknown escape sequence '\\" + escape + "'.");
            }
        }
        return sb.toString();
    }

    /** Parses a JSON number, returning a {@link Long} for integers or a {@link Double} once a fraction/exponent is seen. */
    private Object parseNumberValue() {
        int start = pos;
        if (peek() == '-') {
            pos++;
        }
        while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
            pos++;
        }
        boolean isDouble = false;
        if (pos < text.length() && text.charAt(pos) == '.') {
            isDouble = true;
            pos++;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
        }
        if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
            isDouble = true;
            pos++;
            if (pos < text.length() && (text.charAt(pos) == '+' || text.charAt(pos) == '-')) {
                pos++;
            }
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
        }
        String number = text.substring(start, pos);
        if (number.isEmpty() || "-".equals(number)) {
            throw new JsonParseException("Invalid number at position " + start);
        }
        return isDouble ? (Object) Double.parseDouble(number) : (Object) Long.parseLong(number);
    }

    /** Matches a fixed keyword ({@code true}/{@code false}/{@code null}) and returns its Java value. */
    private Object parseLiteral(String literal, Object value) {
        if (pos + literal.length() > text.length() || !text.regionMatches(pos, literal, 0, literal.length())) {
            throw new JsonParseException("Invalid literal at position " + pos);
        }
        pos += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        if (pos >= text.length()) {
            throw new JsonParseException("Unexpected end of input.");
        }
        return text.charAt(pos);
    }

    private char next() {
        if (pos >= text.length()) {
            throw new JsonParseException("Unexpected end of input.");
        }
        return text.charAt(pos++);
    }

    private void expect(char expected) {
        char actual = next();
        if (actual != expected) {
            throw new JsonParseException("Expected '" + expected + "' but found '" + actual + "' at position " + (pos - 1));
        }
    }
}
