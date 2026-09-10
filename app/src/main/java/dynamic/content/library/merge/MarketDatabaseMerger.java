package dynamic.content.library.merge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import dynamic.content.library.json.Json;
import dynamic.content.library.json.JsonParseException;
import dynamic.content.library.schema.MarketDatabaseFile;
import dynamic.content.library.schema.MarketDefinition;
import dynamic.content.library.schema.MarketItemDefinition;
import game.markets.Market;
import game.markets.MarketDatabase;
import game.world.SectorRegion;
import illuminatus.core.io.Console;

/**
 * Reads market JSON files registered with the library and registers their entries with
 * {@link MarketDatabase}'s in-memory store. Runs after {@code MarketDatabase.loadDatabase()}
 * so the vanilla file on disk is never touched - additions only ever live in memory.
 *
 * <p>Each {@link MarketDefinition} is built into a real {@link Market} object using the game's
 * own public builder methods ({@code addStarSystems}, {@code addStationIndices},
 * {@code addSectorRegionTypes}, {@code add}) and then handed to
 * {@link MarketDatabase#setMarket(int, Market)} so it becomes visible to the rest of the game
 * exactly as if it had been loaded from {@code market_data.dat}.
 */
public final class MarketDatabaseMerger {

    /** Auto-assigned indices start well above the vanilla market index range to avoid collisions. */
    private static final int AUTO_INDEX_BASE = 100_000;

    private static final AtomicInteger nextAutoIndex = new AtomicInteger(AUTO_INDEX_BASE);

    private MarketDatabaseMerger() {
    }

    /** Merges every registered market source file, in registration order. Failures in one file do not stop the rest. */
    public static void merge(List<Path> sources) {
        for (Path source : sources) {
            mergeFile(source);
        }
    }

    private static void mergeFile(Path source) {
        MarketDatabaseFile file;
        try {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            Map<String, Object> json = Json.parseObject(text);
            file = MarketDatabaseFile.fromJson(json);
        } catch (IOException | JsonParseException e) {
            Console.printError("[DCL] Failed to read market database " + source + ": " + e.getMessage());
            return;
        }

        if (file.markets == null) {
            Console.printWarning("[DCL] Market database " + source + " contained no markets.");
            return;
        }

        for (MarketDefinition definition : file.markets) {
            mergeMarket(definition);
        }
    }

    /**
     * Builds a {@link Market} from a single {@link MarketDefinition} and registers it live.
     * When {@code definition.index} is absent, an index from {@link #nextAutoIndex} is used
     * instead, so mod authors do not need to coordinate index numbers with each other.
     */
    private static void mergeMarket(MarketDefinition definition) {
        int index;
        if (definition.index != null) {
            index = definition.index;
        } else {
            index = nextAutoIndex.getAndIncrement();
        }

        Market market = new Market(index, definition.minEconomyLevel);

        if (definition.starSystems != null) {
            market.addStarSystems(definition.starSystems.toArray(String[]::new));
        }
        if (definition.stationSpawnIndices != null) {
            market.addStationIndices(definition.stationSpawnIndices.toArray(Integer[]::new));
        }
        if (definition.sectorRegionTypes != null) {
            market.addSectorRegionTypes(toSectorRegions(definition.sectorRegionTypes));
        }
        if (definition.items != null) {
            for (MarketItemDefinition item : definition.items) {
                market.add(item.itemId, item.tier);
            }
        }

        MarketDatabase.setMarket(index, market);
    }

    /** Resolves schema region-type names to {@link SectorRegion} constants, skipping and warning on unknown names. */
    private static SectorRegion[] toSectorRegions(List<String> names) {
        SectorRegion[] regions = new SectorRegion[names.size()];
        for (int i = 0; i < names.size(); i++) {
            try {
                regions[i] = SectorRegion.valueOf(names.get(i).toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException e) {
                Console.printWarning("[DCL] Unknown sectorRegionType \"" + names.get(i) + "\", skipping.");
            }
        }
        return regions;
    }
}
