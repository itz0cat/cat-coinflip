package com.itz0cat.catcoinflip.v26_1_2.modmenu;

import com.itz0cat.catcoinflip.v26_1_2.config.ClothConfigScreen26_1_2;

public class ModMenuCompat26_1_2 {

    public static Object getConfigScreenFactory() {
        return (java.util.function.Function<Object, Object>) parent -> ClothConfigScreen26_1_2.buildScreen((net.minecraft.client.gui.screens.Screen) parent);
    }
}
