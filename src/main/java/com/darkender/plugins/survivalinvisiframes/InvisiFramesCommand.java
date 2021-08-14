package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InvisiFramesCommand implements CommandExecutor, TabCompleter
{
    private final SurvivalInvisiframes plugin;
    
    public InvisiFramesCommand(SurvivalInvisiframes plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String permMsg = command.getPermissionMessage();
        if(args.length == 0 || args[0].equalsIgnoreCase("get"))
        {
            if(!(sender instanceof Player)) {
                sender.sendMessage(Locale.PLAYER_ONLY.str());
                return true;
            }
            if (!sender.hasPermission("survivalinvisiframes.get")) {
                sender.sendMessage(permMsg);
                return true;
            }

            boolean glow = false;
            int count = 1;
            if (SurvivalInvisiframes.isGlowItemFramesSupported()) {
                for (String arg : args) {
                    if (arg.equalsIgnoreCase("glow")) {
                        glow = true;
                        break;
                    }
                }
            }
            try {
                count = Integer.parseInt(args[args.length-1]);
            } catch (Exception ignore) {}
            giveItem(sender, glow, count);
            return true;
        }
        else if(args[0].equalsIgnoreCase("reload"))
        {
            if(!sender.hasPermission("survivalinvisiframes.reload")) {
                sender.sendMessage(permMsg);
                return true;
            }
            plugin.reload();
            sender.sendMessage(Locale.RELOAD.str());
            return true;
        }
        else if(args[0].equalsIgnoreCase("force-recheck"))
        {
            if(!sender.hasPermission("survivalinvisiframes.forcerecheck")) {
                sender.sendMessage(permMsg);
                return true;
            }
            sender.sendMessage(Locale.RECHECK.str("count", String.valueOf(plugin.forceRecheck())));
            return true;
        }
        else if(args[0].equalsIgnoreCase("setitem"))
        {
            if(!sender.hasPermission("survivalinvisiframes.setitem")) {
                sender.sendMessage(permMsg);
                return true;
            }
            if(!(sender instanceof Player)) {
                sender.sendMessage(Locale.PLAYER_ONLY.str());
                return true;
            }
            ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
            plugin.setRecipeItem(item);
            sender.sendMessage(Locale.RECIPE_ITEM_UPDATED.str());
            return true;
        }
        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        List<String> options = new ArrayList<>();
        String arg = "";
        if (args.length == 1) {
            arg = args[0];
            if(sender.hasPermission("survivalinvisiframes.get"))
            {
                options.add("get");
            }
            if(sender.hasPermission("survivalinvisiframes.reload"))
            {
                options.add("reload");
            }
            if(sender.hasPermission("survivalinvisiframes.forcerecheck"))
            {
                options.add("force-recheck");
            }
            if(sender.hasPermission("survivalinvisiframes.setitem"))
            {
                options.add("setitem");
            }
        } else if (args.length == 2 && SurvivalInvisiframes.isGlowItemFramesSupported()) {
            arg = args[1];
            options.add("glow");
        }
        List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(arg, options, completions);
        Collections.sort(completions);
        return options;
    }
    
    private void giveItem(CommandSender sender, boolean glowing_frame, int count)
    {
        Player player = (Player) sender;
        ItemStack itemStack = SurvivalInvisiframes.generateInvisibleItemFrame(glowing_frame);
        itemStack.setAmount(count);
        player.getInventory().addItem(itemStack);
        player.sendMessage(Locale.ADDED_TO_YOUR_INVENTORY.str());
    }
}
