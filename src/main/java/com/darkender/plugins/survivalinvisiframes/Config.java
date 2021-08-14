package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class Config {
    public static boolean GLOWING_EFFECT;
    public static ItemStack RECIPE_ITEM;

    public static void load_data(FileConfiguration config) {
        GLOWING_EFFECT = config.getBoolean("item-frames-glow-effect");
        RECIPE_ITEM = config.getItemStack("recipe-item");
    }
}