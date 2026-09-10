package dynamic.content.library.mixin;

import dynamic.content.library.api.DynamicContentLibrary;
import game.markets.MarketDatabase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the tail of {@link MarketDatabase#loadDatabase()} so DCL can register mod-contributed
 * markets right after the vanilla {@code market_data.dat} file has finished loading, but before
 * anything else in the game has a chance to read from it. The vanilla file on disk is never
 * touched - {@code MarketDatabase.saveDatabase()} is never called by DCL, so additions only
 * ever live in memory for the current session.
 */
@Mixin(MarketDatabase.class)
public class MarketDatabaseMixin {

    /**
     * Runs once the vanilla market load logic completes. Delegates to
     * {@link DynamicContentLibrary#mergeMarkets()} to parse and apply all registered content.
     */
    @Inject(method = "loadDatabase", at = @At("TAIL"))
    private static void dynamiccontentlibrary$onLoadDatabase(CallbackInfo ci) {
        DynamicContentLibrary.getInstance().mergeMarkets();
    }
}
