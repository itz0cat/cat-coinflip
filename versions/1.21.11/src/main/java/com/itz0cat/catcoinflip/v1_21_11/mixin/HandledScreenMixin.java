package com.itz0cat.catcoinflip.v1_21_11.mixin;

import com.itz0cat.catcoinflip.CatCoinflip;
import com.itz0cat.catcoinflip.profile.Profile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    @Final
    protected ScreenHandler handler;

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected int backgroundWidth;

    @Shadow
    protected int backgroundHeight;

    @Unique
    private static final Identifier LOGO_ID = Identifier.of("catcoinflip", "textures/gui/itz0cat_logo.png");

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V"))
    private void catcf$renderWatermark(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Profile profile = CatCoinflip.getInstance().getProfileManager().getActiveProfile();
        if (profile == null || !profile.isShowWatermark()) {
            return;
        }

        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        String rawTitle = screen.getTitle().getString().replaceAll("(?i)§[0-9a-fklmnor]", "").trim();

        boolean matches = false;
        for (String patternStr : profile.getWatermarkScreens()) {
            try {
                if (Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE).matcher(rawTitle).find()) {
                    matches = true;
                    break;
                }
            } catch (Exception ignored) {}
        }

        // Fallback slot signature
        if (!matches && profile.isWatermarkUseSignature()) {
            int containerSlots = Math.max(0, this.handler.slots.size() - 36);
            if (containerSlots == 36) {
                String s30 = getSlotName(30);
                String s31 = getSlotName(31);
                String s32 = getSlotName(32);
                String s35 = getSlotName(35);
                if (s30.contains("PREVIOUS PAGE") && s31.contains("STATISTICS") &&
                    s32.contains("NEXT PAGE") && s35.contains("CREATE GAME")) {
                    matches = true;
                }
            }
        }

        if (!matches) {
            return;
        }

        // Original logo dimensions: 938 x 280 (approx ~3.35 aspect ratio).
        // Standard baseline draw: 134 x 40
        int scale = Math.max(1, profile.getWatermarkScale());
        int w = 134 * scale;
        int h = 40 * scale;

        int posX;
        int posY;
        String anchor = profile.getWatermarkAnchor();

        if ("top_center_above_gui".equalsIgnoreCase(anchor)) {
            posX = (screen.width - w) / 2;
            posY = Math.max(2, this.y - h - 4);
        } else if ("bottom_center".equalsIgnoreCase(anchor)) {
            posX = (screen.width - w) / 2;
            posY = screen.height - h - 4;
        } else {
            // default bottom_right
            posX = screen.width - w - 6;
            posY = screen.height - h - 6;
        }

        float opacity = profile.getWatermarkOpacity();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        RenderSystem.enableBlend();

        context.drawTexture(LOGO_ID, posX, posY, 0, 0, w, h, w, h);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Unique
    private String getSlotName(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < this.handler.slots.size()) {
            ItemStack stack = this.handler.getSlot(slotIndex).getStack();
            if (!stack.isEmpty()) {
                return stack.getName().getString().replaceAll("(?i)§[0-9a-fklmnor]", "").toUpperCase();
            }
        }
        return "";
    }
}
