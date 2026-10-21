package com.itz0cat.catcoinflip.v26_1_2.mixin;

import com.itz0cat.catcoinflip.CatCoinflip;
import com.itz0cat.catcoinflip.profile.Profile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;

    @Unique
    private static final ResourceLocation LOGO_ID = ResourceLocation.fromNamespaceAndPath("catcoinflip", "textures/gui/itz0cat_logo.png");

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void catcf$renderWatermark(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Profile profile = CatCoinflip.getInstance().getProfileManager().getActiveProfile();
        if (profile == null || !profile.isShowWatermark()) {
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
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

        if (!matches && profile.isWatermarkUseSignature()) {
            int containerSlots = Math.max(0, this.menu.slots.size() - 36);
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

        int scale = Math.max(1, profile.getWatermarkScale());
        int w = 134 * scale;
        int h = 40 * scale;

        int posX;
        int posY;
        String anchor = profile.getWatermarkAnchor();

        if ("top_center_above_gui".equalsIgnoreCase(anchor)) {
            posX = (screen.width - w) / 2;
            posY = Math.max(2, this.topPos - h - 4);
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

        guiGraphics.blit(LOGO_ID, posX, posY, 0, 0, w, h, w, h);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Unique
    private String getSlotName(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < this.menu.slots.size()) {
            ItemStack stack = this.menu.getSlot(slotIndex).getItem();
            if (!stack.isEmpty()) {
                return stack.getHoverName().getString().replaceAll("(?i)§[0-9a-fklmnor]", "").toUpperCase();
            }
        }
        return "";
    }
}
