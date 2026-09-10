package dynamic.content.library.json;

/**
 * Thrown when {@link Json} encounters input that does not conform to the JSON grammar, such as
 * an unterminated string, an invalid escape sequence, or a missing comma/brace/bracket. The
 * message includes the character position at which parsing failed, to make it easier for a mod
 * author to locate the mistake in their own content file.
 */
public class JsonParseException extends RuntimeException {
    public JsonParseException(String message) {
        super(message);
    }
}
