package dynamic.content.library.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helpers for pulling typed values with defaults out of a {@link Json#parseObject(String)} result.
 *
 * <p>{@link Json} produces a plain object graph of {@link Map}, {@link List}, {@link String},
 * {@link Number}, {@link Boolean} and {@code null} - there is no schema validation or reflection
 * involved. These helpers centralize the casting and null/default handling so the schema classes'
 * {@code fromJson} methods can stay short and readable.
 */
public final class JsonValue {

    private JsonValue() {
    }

    /** Returns the string at {@code key}, or {@code defaultValue} if the key is absent or null. */
    public static String getString(Map<String, Object> object, String key, String defaultValue) {
        Object value = object.get(key);
        return value == null ? defaultValue : (String) value;
    }

    /** Returns the int at {@code key}, or {@code defaultValue} if the key is absent or null. */
    public static int getInt(Map<String, Object> object, String key, int defaultValue) {
        Object value = object.get(key);
        return value == null ? defaultValue : ((Number) value).intValue();
    }

    /** Returns the boxed Integer at {@code key}, or {@code null} if absent - used for optional ids like a market index. */
    public static Integer getInteger(Map<String, Object> object, String key) {
        Object value = object.get(key);
        return value == null ? null : ((Number) value).intValue();
    }

    /** Returns the long at {@code key}, or {@code defaultValue} if the key is absent or null. */
    public static long getLong(Map<String, Object> object, String key, long defaultValue) {
        Object value = object.get(key);
        return value == null ? defaultValue : ((Number) value).longValue();
    }

    /** Returns the float at {@code key}, or {@code defaultValue} if the key is absent or null. */
    public static float getFloat(Map<String, Object> object, String key, float defaultValue) {
        Object value = object.get(key);
        return value == null ? defaultValue : ((Number) value).floatValue();
    }

    /** Returns the double at {@code key}, or {@code defaultValue} if the key is absent or null. */
    public static double getDouble(Map<String, Object> object, String key, double defaultValue) {
        Object value = object.get(key);
        return value == null ? defaultValue : ((Number) value).doubleValue();
    }

    /** Returns the boolean at {@code key}, or {@code defaultValue} if the key is absent or null. */
    public static boolean getBoolean(Map<String, Object> object, String key, boolean defaultValue) {
        Object value = object.get(key);
        return value == null ? defaultValue : (Boolean) value;
    }

    /** Casts a value produced by {@link Json} (e.g. an array element) down to a JSON object. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        return (Map<String, Object>) value;
    }

    /** Returns the raw array at {@code key} as a {@code List<Object>}, or {@code null} if absent. */
    @SuppressWarnings("unchecked")
    public static List<Object> getArray(Map<String, Object> object, String key) {
        Object value = object.get(key);
        return value == null ? null : (List<Object>) value;
    }

    /** Returns the array at {@code key} as a list of strings, or {@code null} if absent. */
    public static List<String> getStringList(Map<String, Object> object, String key) {
        List<Object> array = getArray(object, key);
        if (array == null) {
            return null;
        }
        List<String> result = new ArrayList<>(array.size());
        for (Object element : array) {
            result.add((String) element);
        }
        return result;
    }

    /** Returns the array at {@code key} as a list of ints, or {@code null} if absent. */
    public static List<Integer> getIntList(Map<String, Object> object, String key) {
        List<Object> array = getArray(object, key);
        if (array == null) {
            return null;
        }
        List<Integer> result = new ArrayList<>(array.size());
        for (Object element : array) {
            result.add(((Number) element).intValue());
        }
        return result;
    }

    /** Returns the array at {@code key} as a primitive int array, or {@code null} if absent. */
    public static int[] getIntArray(Map<String, Object> object, String key) {
        List<Object> array = getArray(object, key);
        if (array == null) {
            return null;
        }
        int[] result = new int[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = ((Number) array.get(i)).intValue();
        }
        return result;
    }
}
