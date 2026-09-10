package dynamic.content.library.schema;

import java.util.Map;

import dynamic.content.library.json.JsonValue;

/**
 * Library-defined JSON representation of a single item to merge into Sector Space's
 * item database. Field names match this library's public schema, not the game's
 * internal binary format - the merger translates these into the game's native types.
 *
 * <p>Every field has a sensible default so a mod author's JSON only needs to specify what
 * actually matters for their item; anything left out falls back to the values assigned here
 * (see {@link #fromJson(Map)} for exactly which JSON key maps to which field).
 */
public class ItemDefinition {
    /** Unique base item id. Must not collide with vanilla or another mod's ids. */
    public int id;
    /** Item rarity/power tier. Combined with {@link #id} to form the database storage key. */
    public int tier = 1;
    /** Index into the game's item icon sheet. */
    public int icon;
    /** Numeric item category, matching one of the game's own category constants. */
    public int category;
    /** Display name shown to the player. Required - items without a name are skipped. */
    public String name;
    /** Flavor/description text shown on the item's tooltip. */
    public String description = "";
    /** Cargo volume a single unit of this item occupies. */
    public double volume;
    /** Base credit value used for buy/sell pricing. */
    public long creditValue;
    /** One of: NONE, JUNK, COMMON, UNCOMMON, RARE, EXOTIC, LEGENDARY, PLATFORM, STATION. */
    public String typeTag = "COMMON";
    /** Optional [r,g,b] 0-255 tint. Defaults to white when omitted. */
    public int[] color;
    /** Whether the item only functions while installed on a station rather than a ship. */
    public boolean isStationOnly;
    /** Whether the item is flagged as player-crafted. */
    public boolean isCrafted;
    /** Whether the item is flagged as looted from a wreck/container. */
    public boolean isLooted;
    /** Whether the item is barred from being sold at all. */
    public boolean blacklistSell;
    /** Whether the item is sold at a reduced ("greylist") rate. */
    public boolean greylistSell;

    /** Builds an {@link ItemDefinition} from a parsed JSON object, applying defaults for any missing fields. */
    public static ItemDefinition fromJson(Map<String, Object> json) {
        ItemDefinition definition = new ItemDefinition();
        definition.id = JsonValue.getInt(json, "id", 0);
        definition.tier = JsonValue.getInt(json, "tier", 1);
        definition.icon = JsonValue.getInt(json, "icon", 0);
        definition.category = JsonValue.getInt(json, "category", 0);
        definition.name = JsonValue.getString(json, "name", null);
        definition.description = JsonValue.getString(json, "description", "");
        definition.volume = JsonValue.getDouble(json, "volume", 0);
        definition.creditValue = JsonValue.getLong(json, "creditValue", 0);
        definition.typeTag = JsonValue.getString(json, "typeTag", "COMMON");
        definition.color = JsonValue.getIntArray(json, "color");
        definition.isStationOnly = JsonValue.getBoolean(json, "isStationOnly", false);
        definition.isCrafted = JsonValue.getBoolean(json, "isCrafted", false);
        definition.isLooted = JsonValue.getBoolean(json, "isLooted", false);
        definition.blacklistSell = JsonValue.getBoolean(json, "blacklistSell", false);
        definition.greylistSell = JsonValue.getBoolean(json, "greylistSell", false);
        return definition;
    }
}
