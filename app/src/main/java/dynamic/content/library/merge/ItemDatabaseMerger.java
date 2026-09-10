package dynamic.content.library.merge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import _database.ItemDatabase;
import dynamic.content.library.json.Json;
import dynamic.content.library.json.JsonParseException;
import dynamic.content.library.schema.ItemDatabaseFile;
import dynamic.content.library.schema.ItemDefinition;
import illuminatus.core.datastructures.DataQueue;
import illuminatus.core.graphics.Color;
import illuminatus.core.io.Console;
import items.Item;
import items.TypeTag;

/**
 * Reads item JSON files registered with the library and writes their entries into
 * {@link ItemDatabase}'s in-memory store. Runs after {@code ItemDatabase.loadDatabase()}
 * so the vanilla file on disk is never touched - additions only ever live in memory.
 *
 * <p>Each {@link ItemDefinition} is translated into the exact {@link DataQueue} shape the game
 * itself would produce (mirrored from {@link Item#getItemWriteDataQueue}) and written under its
 * requested id/tier key via {@link ItemDatabase#writeToDatabase(int, int, DataQueue)}. From the
 * game's point of view a merged item is indistinguishable from one it loaded from disk itself -
 * {@code Item#loadFromDatabase()} reads it back lazily the first time anything asks for that id.
 */
public final class ItemDatabaseMerger {

    private ItemDatabaseMerger() {
    }

    /** Merges every registered item source file, in registration order. Failures in one file do not stop the rest. */
    public static void merge(List<Path> sources) {
        for (Path source : sources) {
            mergeFile(source);
        }
    }

    private static void mergeFile(Path source) {
        ItemDatabaseFile file;
        try {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            Map<String, Object> json = Json.parseObject(text);
            file = ItemDatabaseFile.fromJson(json);
        } catch (IOException | JsonParseException e) {
            Console.printError("[DCL] Failed to read item database " + source + ": " + e.getMessage());
            return;
        }

        if (file.items == null) {
            Console.printWarning("[DCL] Item database " + source + " contained no items.");
            return;
        }

        for (ItemDefinition definition : file.items) {
            mergeItem(definition, source);
        }
    }

    /**
     * Converts a single {@link ItemDefinition} into the game's native {@link DataQueue} format
     * and writes it into {@link ItemDatabase}. Items with no name are skipped, since the game
     * has nothing sensible to display for them.
     */
    private static void mergeItem(ItemDefinition definition, Path source) {
        if (definition.name == null || definition.name.isEmpty()) {
            Console.printWarning("[DCL] Skipping item with id " + definition.id + " from " + source + ": missing name.");
            return;
        }

        Color tintColor = toColor(definition.color);
        TypeTag typeTag = toTypeTag(definition.typeTag);

        DataQueue queue = Item.getItemWriteDataQueue(
                definition.id,
                definition.icon,
                tintColor,
                definition.category,
                definition.name,
                definition.description,
                definition.volume,
                definition.creditValue,
                definition.tier,
                typeTag,
                definition.isStationOnly,
                definition.isCrafted,
                definition.isLooted,
                definition.blacklistSell,
                definition.greylistSell
        );

        ItemDatabase.writeToDatabase(definition.id, definition.tier, queue);
    }

    /** Converts a 0-255 [r,g,b] triple into a game {@link Color}, defaulting to white when absent or malformed. */
    private static Color toColor(int[] rgb) {
        if (rgb == null || rgb.length != 3) {
            return Color.WHITE;
        }
        return new Color(rgb[0] / 255f, rgb[1] / 255f, rgb[2] / 255f);
    }

    /** Maps a schema typeTag name onto one of the game's fixed {@link TypeTag} constants, defaulting to COMMON. */
    private static TypeTag toTypeTag(String name) {
        if (name == null) {
            return TypeTag.COMMON;
        }
        switch (name.toUpperCase(java.util.Locale.ROOT)) {
            case "NONE": return TypeTag.NONE;
            case "JUNK": return TypeTag.JUNK;
            case "COMMON": return TypeTag.COMMON;
            case "UNCOMMON": return TypeTag.UNCOMMON;
            case "RARE": return TypeTag.RARE;
            case "EXOTIC": return TypeTag.EXOTIC;
            case "LEGENDARY": return TypeTag.LEGENDARY;
            case "PLATFORM": return TypeTag.PLATFORM;
            case "STATION": return TypeTag.STATION;
            default:
                Console.printWarning("[DCL] Unknown typeTag \"" + name + "\", defaulting to COMMON.");
                return TypeTag.COMMON;
        }
    }
}
