package dynamic.content.library.mixin;

import _database.ItemDatabase;
import dynamic.content.library.api.DynamicContentLibrary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the tail of {@link ItemDatabase#loadDatabase()} so DCL can write mod-contributed items
 * into the database right after the vanilla {@code item_data.dat} file has finished loading,
 * but before anything else in the game has a chance to read from it. The vanilla file on disk
 * is never touched - {@code ItemDatabase.saveDatabase()} is never called by DCL, so additions
 * only ever live in memory for the current session.
 */
@Mixin(ItemDatabase.class)
public class ItemDatabaseMixin {

    /**
     * Runs once the vanilla item load logic completes. Delegates to
     * {@link DynamicContentLibrary#mergeItems()} to parse and apply all registered content.
     */
    @Inject(method = "loadDatabase", at = @At("TAIL"))
    private static void dynamiccontentlibrary$onLoadDatabase(CallbackInfo ci) {
        DynamicContentLibrary.getInstance().mergeItems();
    }
}
