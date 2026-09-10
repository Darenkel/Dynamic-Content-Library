package dynamic.content.library.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dynamic.content.library.json.JsonValue;

/**
 * Root object of a market database JSON file registered via {@code DynamicContentLibrary}.
 * A minimal file looks like {@code {"markets": [ { "minEconomyLevel": 3.0, ... } ]}}.
 */
public class MarketDatabaseFile {
    public List<MarketDefinition> markets;

    /** Builds a {@link MarketDatabaseFile} from a parsed JSON object, reading its {@code markets} array. */
    public static MarketDatabaseFile fromJson(Map<String, Object> json) {
        MarketDatabaseFile file = new MarketDatabaseFile();
        List<Object> marketsArray = JsonValue.getArray(json, "markets");
        if (marketsArray != null) {
            List<MarketDefinition> markets = new ArrayList<>(marketsArray.size());
            for (Object element : marketsArray) {
                markets.add(MarketDefinition.fromJson(JsonValue.asObject(element)));
            }
            file.markets = markets;
        }
        return file;
    }
}
