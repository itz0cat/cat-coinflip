package com.itz0cat.catcoinflip.compat;

import java.nio.file.Path;

public interface PlatformCompat {

    void sendClientChatMessage(String message);

    String getCurrentServerAddress();

    Path getConfigDirectory();

    void openConfigScreen(Object parentScreen);

    String getMinecraftVersion();
}
