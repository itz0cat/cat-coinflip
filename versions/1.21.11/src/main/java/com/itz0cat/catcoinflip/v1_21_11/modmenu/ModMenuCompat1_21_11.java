package com.itz0cat.catcoinflip.v1_21_11.modmenu;

import com.itz0cat.catcoinflip.v1_21_11.config.ClothConfigScreen1_21_11;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat1_21_11 implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClothConfigScreen1_21_11::buildScreen;
    }
}
