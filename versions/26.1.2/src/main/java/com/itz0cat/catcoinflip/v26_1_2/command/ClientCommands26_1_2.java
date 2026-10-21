package com.itz0cat.catcoinflip.v26_1_2.command;

import com.itz0cat.catcoinflip.CatCoinflip;
import com.itz0cat.catcoinflip.profile.Profile;
import com.itz0cat.catcoinflip.v26_1_2.config.ClothConfigScreen26_1_2;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class ClientCommands26_1_2 {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("catcf")
                .executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> mc.setScreen(ClothConfigScreen26_1_2.buildScreen(mc.screen)));
                    return 1;
                })
                .then(literal("config").executes(ctx -> {
                    Minecraft mc = Minecraft.getInstance();
                    mc.execute(() -> mc.setScreen(ClothConfigScreen26_1_2.buildScreen(mc.screen)));
                    return 1;
                }))
                .then(literal("resetstats").executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    CatCoinflip.getInstance().getStatsManager().resetAll();
                    source.sendFeedback(Component.literal("§6[CatCoinflip] §aAll player tracking and session statistics have been reset!"));
                    return 1;
                }))
                .then(literal("guiinfo").executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Minecraft mc = Minecraft.getInstance();
                    Screen screen = mc.screen;
                    if (screen == null) {
                        source.sendFeedback(Component.literal("§6[CatCoinflip] §cNo GUI is currently open."));
                        return 1;
                    }

                    String title = screen.getTitle().getString();
                    source.sendFeedback(Component.literal("§6[CatCoinflip] §e=== GUI Info ==="));
                    source.sendFeedback(Component.literal("§7Title: §f" + title));

                    if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                        AbstractContainerMenu menu = containerScreen.getMenu();
                        int totalSlots = menu.slots.size();
                        int containerSlots = Math.max(0, totalSlots - 36);
                        source.sendFeedback(Component.literal("§7Container Slots: §f" + containerSlots + " §7(Total: " + totalSlots + ")"));

                        source.sendFeedback(Component.literal("§eSlots 0-3:"));
                        for (int i = 0; i <= 3 && i < containerSlots; i++) {
                            ItemStack stack = menu.getSlot(i).getItem();
                            String name = stack.isEmpty() ? "<Empty>" : stack.getHoverName().getString();
                            source.sendFeedback(Component.literal("  Slot " + i + ": §f" + name));
                        }

                        source.sendFeedback(Component.literal("§eSlots 30-35:"));
                        for (int i = 30; i <= 35 && i < containerSlots; i++) {
                            ItemStack stack = menu.getSlot(i).getItem();
                            String name = stack.isEmpty() ? "<Empty>" : stack.getHoverName().getString();
                            source.sendFeedback(Component.literal("  Slot " + i + ": §f" + name));
                        }
                    } else {
                        source.sendFeedback(Component.literal("§7Screen is not a container / AbstractContainerScreen."));
                    }
                    return 1;
                }))
                .then(literal("status").executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Profile active = CatCoinflip.getInstance().getProfileManager().getActiveProfile();
                    source.sendFeedback(Component.literal("§6[CatCoinflip] §e=== Status ==="));
                    source.sendFeedback(Component.literal("§7Active Profile: §f" + (active != null ? active.getName() : "None")));
                    source.sendFeedback(Component.literal("§7Server Connected: §f" + CatCoinflip.getInstance().getProfileManager().getCurrentServerAddress()));
                    source.sendFeedback(Component.literal("§7Quick Create: " + (active != null && active.isQuickCreate() ? "§aON" : "§cOFF")));
                    return 1;
                }))
            );
        });
    }
}
