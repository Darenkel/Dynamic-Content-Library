package dynamic.content.library.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dynamic.content.library.json.JsonValue;

/**
 * Library-defined JSON representation of a market to register with Sector Space's
 * market database. Field names match this library's public schema, not the game's
 * internal binary format - the merger translates these into the game's native types.
 *
 * <p>A market with no {@link #starSystems}, {@link #stationSpawnIndices} or
 * {@link #sectorRegionTypes} restrictions is effectively unreachable in normal gameplay, so
 * mod authors should set at least one of them to control where their market can appear.
 */
public class MarketDefinition {
    /** Unique market index. When omitted, the merger auto-assigns one above the vanilla range. */
    public Integer index;
    /** Minimum system economy level required for this market to spawn. */
    public float minEconomyLevel = 3.0f;
    /** Restricts this market to specific star system names, if any are given. */
    public List<String> starSystems;
    /** Restricts this market to specific station spawn indices, if any are given. */
    public List<Integer> stationSpawnIndices;
    /** Names of game.world.SectorRegion values, e.g. "NEUTRAL", "FRINGE". */
    public List<String> sectorRegionTypes;
    /** Items this market stocks, each referencing an id already known to the item database. */
    public List<MarketItemDefinition> items;

    /** Builds a {@link MarketDefinition} from a parsed JSON object, applying defaults for any missing fields. */
    public static MarketDefinition fromJson(Map<String, Object> json) {
        MarketDefinition definition = new MarketDefinition();
        definition.index = JsonValue.getInteger(json, "index");
        definition.minEconomyLevel = JsonValue.getFloat(json, "minEconomyLevel", 3.0f);
        definition.starSystems = JsonValue.getStringList(json, "starSystems");
        definition.stationSpawnIndices = JsonValue.getIntList(json, "stationSpawnIndices");
        definition.sectorRegionTypes = JsonValue.getStringList(json, "sectorRegionTypes");
        List<Object> itemsArray = JsonValue.getArray(json, "items");
        if (itemsArray != null) {
            List<MarketItemDefinition> items = new ArrayList<>(itemsArray.size());
            for (Object element : itemsArray) {
                items.add(MarketItemDefinition.fromJson(JsonValue.asObject(element)));
            }
            definition.items = items;
        }
        return definition;
    }
}
