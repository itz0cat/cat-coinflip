package com.itz0cat.catcoinflip.v26_1_2.mixin;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.itz0cat.catcoinflip.CatCoinflip;
import com.itz0cat.catcoinflip.profile.Profile;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Shadow
    @Final
    private EditBox input;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Inject(method = "showSuggestions", at = @At("TAIL"))
    private void catcf$injectAmountSuggestions(boolean narrateFirstSuggestion, CallbackInfo ci) {
        if (this.input == null) return;
        String text = this.input.getValue();
        if (text == null) return;

        Profile profile = CatCoinflip.getInstance().getProfileManager().getActiveProfile();
        if (profile == null || !profile.isQuickCreate()) return;

        String trimmed = text.trim();
        boolean isAlias = false;
        for (String alias : profile.getCommandAliases()) {
            if (trimmed.equalsIgnoreCase("/" + alias) || trimmed.equalsIgnoreCase("/" + alias + " create")) {
                isAlias = true;
                break;
            }
        }

        if (isAlias && this.pendingSuggestions != null) {
            this.pendingSuggestions = this.pendingSuggestions.thenApply(suggestions -> {
                List<Suggestion> list = (suggestions != null) ? new ArrayList<>(suggestions.getList()) : new ArrayList<>();
                List<String> hints = CatCoinflip.getInstance().getQuickCreateHandler().getSuggestions(text, profile);
                com.mojang.brigadier.context.StringRange range = (suggestions != null) ? suggestions.getRange() : com.mojang.brigadier.context.StringRange.at(text.length());

                for (String hint : hints) {
                    list.add(new Suggestion(range, hint));
                }
                return new Suggestions(range, list);
            });
        }
    }
}
