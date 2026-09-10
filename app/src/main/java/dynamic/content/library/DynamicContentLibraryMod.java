package dynamic.content.library;

import dynamic.content.library.api.DynamicContentLibrary;
import illuminatus.core.io.Console;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entrypoint for DCL (Dynamic Content Library).
 *
 * <p>DCL itself does not add any content to Sector Space. Its job is to sit between other
 * mods and the game's item/market databases: other mods register their own JSON content
 * files with {@link DynamicContentLibrary} during their own Fabric initialization, and DCL's
 * Mixins ({@code ItemDatabaseMixin} and {@code MarketDatabaseMixin}) merge that content into
 * the game's in-memory databases immediately after the vanilla data has finished loading.
 *
 * <p>This class only logs a ready message and touches {@link DynamicContentLibrary#getInstance()}
 * so the registry exists before any other mod tries to use it.
 */
public class DynamicContentLibraryMod implements ModInitializer {

    @Override
    public void onInitialize() {
        Console.println("[DCL] Ready to merge item and market content contributed by other mods.");
        DynamicContentLibrary.getInstance();
    }
}
