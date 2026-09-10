package dynamic.content.library.api;

import dynamic.content.library.merge.ItemDatabaseMerger;
import dynamic.content.library.merge.MarketDatabaseMerger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Public registration point for other mods that want their content merged into Sector Space's
 * item and market databases by DCL (Dynamic Content Library).
 *
 * <p>Usage: from your own mod's Fabric entrypoint (which runs before the game itself boots),
 * call {@link #registerItemSource(Path)} and/or {@link #registerMarketSource(Path)} with the
 * path to a JSON file describing the content you want added. DCL only keeps a queue of the
 * registered paths at this point - it does not read or validate them yet. Each file is parsed
 * and applied later, once the vanilla database it targets has finished loading, via
 * {@link #mergeItems()} and {@link #mergeMarkets()}.
 *
 * <p>This class is a simple process-wide singleton, matching the fact that there is exactly
 * one item database and one market database per running game instance.
 */
public final class DynamicContentLibrary {

    private static final DynamicContentLibrary INSTANCE = new DynamicContentLibrary();

    private final List<Path> itemSources = new ArrayList<>();
    private final List<Path> marketSources = new ArrayList<>();

    private DynamicContentLibrary() {
    }

    /** Returns the single shared instance used by every mod that integrates with DCL. */
    public static DynamicContentLibrary getInstance() {
        return INSTANCE;
    }

    /**
     * Queues a JSON file describing items to add to Sector Space's item database. The file is
     * not read until {@link #mergeItems()} runs, at the tail of {@code ItemDatabase.loadDatabase()}.
     *
     * @param jsonFile path to a file matching the {@code ItemDatabaseFile} schema.
     */
    public void registerItemSource(Path jsonFile) {
        itemSources.add(jsonFile);
    }

    /**
     * Queues a JSON file describing markets to register with Sector Space's market database.
     * The file is not read until {@link #mergeMarkets()} runs, at the tail of
     * {@code MarketDatabase.loadDatabase()}.
     *
     * @param jsonFile path to a file matching the {@code MarketDatabaseFile} schema.
     */
    public void registerMarketSource(Path jsonFile) {
        marketSources.add(jsonFile);
    }

    /** Invoked by {@code ItemDatabaseMixin} once the vanilla item database has finished loading. */
    public void mergeItems() {
        ItemDatabaseMerger.merge(Collections.unmodifiableList(itemSources));
    }

    /** Invoked by {@code MarketDatabaseMixin} once the vanilla market database has finished loading. */
    public void mergeMarkets() {
        MarketDatabaseMerger.merge(Collections.unmodifiableList(marketSources));
    }
}
