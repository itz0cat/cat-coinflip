package com.itz0cat.catcoinflip.v26_1_2.compat;

import com.itz0cat.catcoinflip.compat.PlatformCompat;
import com.itz0cat.catcoinflip.v26_1_2.config.ClothConfigScreen26_1_2;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class PlatformCompat26_1_2 implements PlatformCompat {

    @Override
    public void sendClientChatMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(message));
        }
    }

    @Override
    public String getCurrentServerAddress() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            ServerData server = mc.getCurrentServer();
            if (server != null && server.ip != null) {
                return server.ip;
            }
        }
        return "";
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public void openConfigScreen(Object parentScreen) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            Screen parent = (parentScreen instanceof Screen) ? (Screen) parentScreen : mc.screen;
            mc.setScreen(ClothConfigScreen26_1_2.buildScreen(parent));
        }
    }

    @Override
    public String getMinecraftVersion() {
        return "26.1.2";
    }
}
