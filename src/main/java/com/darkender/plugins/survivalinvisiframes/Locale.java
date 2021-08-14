package com.darkender.plugins.survivalinvisiframes;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Locale {
    RECHECK("locale.recheck"),
    PLAYER_ONLY("locale.player-only"),
    ADDED_TO_YOUR_INVENTORY("locale.added-to-your-inventory"),
    RECIPE_ITEM_UPDATED("locale.recipe-item-updated"),

    INV_ITEMFRAME_DISPLAYNAME("locale.invisible-itemframe-displayname"),
    INV_GLOWITEMFRAME_DISPLAYNAME("locale.invisible-glow-itemframe-displayname"),

    RELOAD("locale.reload");

    private final String key;
    private static FileConfiguration config;

    private static final Pattern rgbchatpattern =  Pattern.compile("&#[a-fA-F0-9]{6}");

    Locale(String key) {
        this.key = key;
    }

    public static void load_data(FileConfiguration config) {
        Locale.config = config;
    }

    public static String hexformat(String msg) {
        Matcher match = rgbchatpattern.matcher(msg);
        while (match.find()) {
            String color = msg.substring(match.start()+1, match.end());
            msg = msg.replace("&"+color, ChatColor.of(color)+"");
            match = rgbchatpattern.matcher(msg);
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private static String replaceAll(String str, String... replace) {
        if (str == null)
            return "null";
        if (replace.length % 2 != 0)
            throw new RuntimeException("Invalid replace arguments count!");
        for (int i = 1; i < replace.length; i += 2) {
            if (i % 2 == 1)
                str = str.replace("%"+replace[i-1]+"%", replace[i]);
        }
        return str;
    }

    public static String processString(String text, String... replacing) {
        return hexformat(replaceAll(text, replacing));
    }

    public String str(String... replacing) {
        return processString(config.getString(this.key), replacing);
    }
}
