package com.itz0cat.catcoinflip.v1_21_11.command;

import com.itz0cat.catcoinflip.CatCoinflip;
import com.itz0cat.catcoinflip.profile.Profile;
import com.itz0cat.catcoinflip.v1_21_11.config.ClothConfigScreen1_21_11;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientCommands1_21_11 {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("catcf")
                .executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.send(() -> client.setScreen(ClothConfigScreen1_21_11.buildScreen(client.currentScreen)));
                    return 1;
                })
                .then(literal("config").executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.send(() -> client.setScreen(ClothConfigScreen1_21_11.buildScreen(client.currentScreen)));
                    return 1;
                }))
                .then(literal("resetstats").executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    CatCoinflip.getInstance().getStatsManager().resetAll();
                    source.sendFeedback(Text.literal("§6[CatCoinflip] §aAll player tracking and session statistics have been reset!"));
                    return 1;
                }))
                .then(literal("guiinfo").executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    MinecraftClient client = MinecraftClient.getInstance();
                    Screen screen = client.currentScreen;
                    if (screen == null) {
                        source.sendFeedback(Text.literal("§6[CatCoinflip] §cNo GUI is currently open."));
                        return 1;
                    }

                    String title = screen.getTitle().getString();
                    source.sendFeedback(Text.literal("§6[CatCoinflip] §e=== GUI Info ==="));
                    source.sendFeedback(Text.literal("§7Title: §f" + title));

                    if (screen instanceof HandledScreen<?> handled) {
                        ScreenHandler handler = handled.getScreenHandler();
                        int totalSlots = handler.slots.size();
                        // Top inventory slots excluding player's 36 slots
                        int containerSlots = Math.max(0, totalSlots - 36);
                        source.sendFeedback(Text.literal("§7Container Slots: §f" + containerSlots + " §7(Total: " + totalSlots + ")"));

                        // Check slots 0-3
                        source.sendFeedback(Text.literal("§eSlots 0-3:"));
                        for (int i = 0; i <= 3 && i < containerSlots; i++) {
                            ItemStack stack = handler.getSlot(i).getStack();
                            String name = stack.isEmpty() ? "<Empty>" : stack.getName().getString();
                            source.sendFeedback(Text.literal("  Slot " + i + ": §f" + name));
                        }

                        // Check slots 30-35
                        source.sendFeedback(Text.literal("§eSlots 30-35:"));
                        for (int i = 30; i <= 35 && i < containerSlots; i++) {
                            ItemStack stack = handler.getSlot(i).getStack();
                            String name = stack.isEmpty() ? "<Empty>" : stack.getName().getString();
                            source.sendFeedback(Text.literal("  Slot " + i + ": §f" + name));
                        }
                    } else {
                        source.sendFeedback(Text.literal("§7Screen is not a container / HandledScreen."));
                    }
                    return 1;
                }))
                .then(literal("status").executes(ctx -> {
                    FabricClientCommandSource source = ctx.getSource();
                    Profile active = CatCoinflip.getInstance().getProfileManager().getActiveProfile();
                    source.sendFeedback(Text.literal("§6[CatCoinflip] §e=== Status ==="));
                    source.sendFeedback(Text.literal("§7Active Profile: §f" + (active != null ? active.getName() : "None")));
                    source.sendFeedback(Text.literal("§7Server Connected: §f" + CatCoinflip.getInstance().getProfileManager().getCurrentServerAddress()));
                    source.sendFeedback(Text.literal("§7Quick Create: " + (active != null && active.isQuickCreate() ? "§aON" : "§cOFF")));
                    return 1;
                }))
            );
        });
    }
}
