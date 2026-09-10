package dynamic.content.library.schema;

import java.util.Map;

import dynamic.content.library.json.JsonValue;

/** An item stocked in a {@link MarketDefinition}, referencing an item id already known to the database. */
public class MarketItemDefinition {
    /** Id of an item already registered in the item database (vanilla or contributed by any mod). */
    public int itemId;
    /** Tier the item is stocked at, matching the tier it was registered under in the item database. */
    public int tier = 1;

    /** Builds a {@link MarketItemDefinition} from a parsed JSON object, applying defaults for any missing fields. */
    public static MarketItemDefinition fromJson(Map<String, Object> json) {
        MarketItemDefinition definition = new MarketItemDefinition();
        definition.itemId = JsonValue.getInt(json, "itemId", 0);
        definition.tier = JsonValue.getInt(json, "tier", 1);
        return definition;
    }
}
