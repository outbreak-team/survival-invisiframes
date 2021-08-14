package com.darkender.plugins.survivalinvisiframes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SurvivalInvisiframes extends JavaPlugin implements Listener
{
    private NamespacedKey invItemFrameRecipeKey;
    private NamespacedKey invGlowItemFrameRecipeKey;
    private static NamespacedKey invisibleKey;
    private final Set<DroppedFrameLocation> droppedFrames = new HashSet<>();
    
    // Stays null if not in 1.17
    private static Material GLOW_INK_SAC_MATERIAL = null;
    private static Material GLOW_ITEM_FRAME_MATERIAL = null;
    private static EntityType GLOW_ITEM_FRAME_ENTITY = null;

    public static boolean isGlowItemFramesSupported() {
        return GLOW_ITEM_FRAME_MATERIAL != null;
    }
    
    @Override
    public void onEnable()
    {
        invItemFrameRecipeKey = new NamespacedKey(this, "invisible-recipe");
        invGlowItemFrameRecipeKey = new NamespacedKey(this, "invisible-glow-recipe");
        invisibleKey = new NamespacedKey(this, "invisible");
    
        try {
            GLOW_INK_SAC_MATERIAL = Material.valueOf("GLOW_INK_SAC");
            GLOW_ITEM_FRAME_MATERIAL = Material.valueOf("GLOW_ITEM_FRAME");
            GLOW_ITEM_FRAME_ENTITY = EntityType.valueOf("GLOW_ITEM_FRAME");
        } catch(IllegalArgumentException ignored) {
            getLogger().info("1.17 items (GLOW_INK_SAC, GLOW_ITEM_FRAME) are not available.");
        }
        
        reload();
        
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("iframe").setExecutor(new InvisiFramesCommand(this));
    }
    
    @Override
    public void onDisable() {
        removeRecipes();
    }
    
    private void removeRecipes() {
        Iterator<Recipe> iter = getServer().recipeIterator();
        while(iter.hasNext()) {
            Recipe check = iter.next();
            if(isInvisibleRecipe(check)) {
                iter.remove();
            }
        }
    }

    private void addRecipes() {
        ItemStack invFrameItem = generateInvisibleItemFrame(false);
        ItemStack invGlowFrameItem = generateInvisibleItemFrame(true);
        invFrameItem.setAmount(8);
        invGlowFrameItem.setAmount(8);
        ItemStack recipeItem = Config.RECIPE_ITEM;

        ShapedRecipe invFrameRecipe = new ShapedRecipe(invItemFrameRecipeKey, invFrameItem);
        invFrameRecipe.shape("FFF", "FPF", "FFF");
        invFrameRecipe.setIngredient('F', Material.ITEM_FRAME);
        invFrameRecipe.setIngredient('P', new RecipeChoice.ExactChoice(recipeItem));
        Bukkit.addRecipe(invFrameRecipe);

        if (isGlowItemFramesSupported()) {
            ShapedRecipe invGlowFrameRecipe = new ShapedRecipe(invGlowItemFrameRecipeKey, invGlowFrameItem);
            invGlowFrameRecipe.shape("FFF", "FPF", "FFF");
            invGlowFrameRecipe.setIngredient('F', GLOW_ITEM_FRAME_MATERIAL);
            invGlowFrameRecipe.setIngredient('P', new RecipeChoice.ExactChoice(recipeItem));
            Bukkit.addRecipe(invGlowFrameRecipe);
        }
    }
    
    public void setRecipeItem(ItemStack item) {
        getConfig().set("recipe-item", item);
        saveConfig();
        reload();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        FileConfiguration config = getConfig();
        Config.load_data(config);
        Locale.load_data(config);
    }

    public void reload() {
        saveDefaultConfig();
        removeRecipes();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        forceRecheck();
        addRecipes();
    }
    
    public int forceRecheck() {
        int count = 0;
        for(World world : Bukkit.getWorlds())
        {
            for(ItemFrame frame : world.getEntitiesByClass(ItemFrame.class))
            {
                if(frame.getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
                {
                    // Make empty frames visible ang glowing
                    frame.setGlowing(frame.getItem().getType() == Material.AIR && Config.GLOWING_EFFECT);
                    frame.setVisible(frame.getItem().getType() == Material.AIR);
                }
                count++;
            }
        }
        getLogger().info("SurvivalInvisiframes checked " + count + " ItemFrames.");
        return count;
    }
    
    private boolean isInvisibleRecipe(Recipe recipe) {
        if (!(recipe instanceof ShapedRecipe)) return false;
        NamespacedKey key = ((ShapedRecipe) recipe).getKey();
        return key.equals(invItemFrameRecipeKey) || key.equals(invGlowItemFrameRecipeKey);
    }
    
    private boolean isFrameEntity(Entity e) {
        return (e != null && (e.getType() == EntityType.ITEM_FRAME || e.getType() == GLOW_ITEM_FRAME_ENTITY));
    }

    private boolean isFrameItem(ItemStack i) {
        return (i != null && (i.getType() == Material.ITEM_FRAME || i.getType() == GLOW_ITEM_FRAME_MATERIAL));
    }
    
    public static ItemStack generateInvisibleItemFrame(boolean glow_frame)
    {
        glow_frame = glow_frame && isGlowItemFramesSupported();
        ItemStack item;
        if (glow_frame)
            item = new ItemStack(GLOW_ITEM_FRAME_MATERIAL, 1);
        else
            item = new ItemStack(Material.ITEM_FRAME, 1);

        ItemMeta meta = item.getItemMeta();

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.DURABILITY, 1 ,true);
        if (glow_frame)
            meta.setDisplayName(Locale.INV_GLOWITEMFRAME_DISPLAYNAME.str());
        else
            meta.setDisplayName(Locale.INV_ITEMFRAME_DISPLAYNAME.str());
        meta.getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        return item;
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onCraft(PrepareItemCraftEvent event) {
        if(isInvisibleRecipe(event.getRecipe()) && !event.getView().getPlayer().hasPermission("survivalinvisiframes.craft")) {
            event.getInventory().setResult(null);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingPlace(HangingPlaceEvent event)
    {
        if(!isFrameEntity(event.getEntity()) || event.getPlayer() == null) {
            return;
        }
        
        // Get the frame item that the player placed
        ItemStack frame;
        Player p = event.getPlayer();
        if(isFrameItem(p.getInventory().getItemInMainHand()))
            frame = p.getInventory().getItemInMainHand();
        else if(isFrameItem(p.getInventory().getItemInOffHand()))
            frame = p.getInventory().getItemInOffHand();
        else
            return;
        
        // If the frame item has the invisible tag, make the placed item frame invisible
        if(frame.getItemMeta().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            if(!p.hasPermission("survivalinvisiframes.place")){
                event.setCancelled(true);
                return;
            }

            ItemFrame itemFrame = (ItemFrame) event.getEntity();

            itemFrame.setGlowing(Config.GLOWING_EFFECT);
            itemFrame.setVisible(Config.GLOWING_EFFECT);

            event.getEntity().getPersistentDataContainer().set(invisibleKey, PersistentDataType.BYTE, (byte) 1);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onHangingBreak(HangingBreakEvent event)
    {
        if(!isFrameEntity(event.getEntity()) || !event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            return;
        }
        
        // This is the dumbest possible way to change the drops of an item frame
        // Apparently, there's no api to change the dropped item
        // So this sets up a bounding box that checks for items near the frame and converts them
        DroppedFrameLocation droppedFrameLocation = new DroppedFrameLocation(event.getEntity().getLocation());
        droppedFrames.add(droppedFrameLocation);
        droppedFrameLocation.setRemoval((new BukkitRunnable()
        {
            @Override
            public void run()
            {
                droppedFrames.remove(droppedFrameLocation);
            }
        }).runTaskLater(this, 20L));
    }
    
    @EventHandler
    private void onItemSpawn(ItemSpawnEvent event)
    {
        Item item = event.getEntity();

        if(!isFrameItem(item.getItemStack())) {
            return;
        }

        boolean is_glowing_frame = item.getItemStack().getType() == GLOW_ITEM_FRAME_MATERIAL;
        Iterator<DroppedFrameLocation> iter = droppedFrames.iterator();
        while(iter.hasNext())
        {
            DroppedFrameLocation droppedFrameLocation = iter.next();
            if(droppedFrameLocation.isFrame(item))
            {
                ItemStack frame = generateInvisibleItemFrame(is_glowing_frame);
                event.getEntity().setItemStack(frame);
                droppedFrameLocation.getRemoval().cancel();
                iter.remove();
                break;
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(!Config.GLOWING_EFFECT) {
            return;
        }
        
        if(isFrameEntity(event.getRightClicked()) &&
                event.getRightClicked().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            ItemFrame frame = (ItemFrame) event.getRightClicked();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if(frame.getItem().getType() != Material.AIR)
                {
                    frame.setGlowing(false);
                    frame.setVisible(false);
                }
            }, 1L);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if(!Config.GLOWING_EFFECT) return;
        
        if(isFrameEntity(event.getEntity()) &&
                event.getEntity().getPersistentDataContainer().has(invisibleKey, PersistentDataType.BYTE))
        {
            ItemFrame frame = (ItemFrame) event.getEntity();
            Bukkit.getScheduler().runTaskLater(this, () ->
            {
                if(frame.getItem().getType() == Material.AIR)
                {
                    if(Config.GLOWING_EFFECT)
                    {
                        frame.setGlowing(true);
                        frame.setVisible(true);
                    }
                }
            }, 1L);
        }
    }
}
