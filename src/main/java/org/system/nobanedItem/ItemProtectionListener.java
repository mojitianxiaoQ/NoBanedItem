package org.system.nobanedItem;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Inventory;

public class ItemProtectionListener implements Listener {

    private final NoBanedItem plugin;

    public ItemProtectionListener(NoBanedItem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (plugin.isAdmin(player.getName().toLowerCase())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        boolean clickedBanned = isBannedItem(clickedItem);
        boolean cursorBanned = isBannedItem(cursorItem);

        if (!clickedBanned && !cursorBanned) {
            return;
        }

        event.setCancelled(true);

        if (event.getInventory().getType() == InventoryType.PLAYER) {
            clearAllBannedItemsFromInventory(player);
            player.sendMessage(ChatColor.RED + "发现非法物品，已清除背包内所有非法物品！");
        } else {
            event.setCurrentItem(null);
            event.setCursor(null);
            player.sendMessage(ChatColor.RED + "该物品已被禁止使用！");
            clearAllBannedItemsFromInventory(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (plugin.isAdmin(player.getName().toLowerCase())) {
            return;
        }

        ItemStack itemInHand = event.getItem();

        if (itemInHand != null && isBannedItem(itemInHand)) {
            Action action = event.getAction();
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);
                clearAllBannedItemsFromInventory(player);
                player.sendMessage(ChatColor.RED + "禁止使用");
            }
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (plugin.isAdmin(player.getName().toLowerCase())) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();

        if (isBannedItem(item)) {
            event.setCancelled(true);
            event.getItem().remove();
            clearAllBannedItemsFromInventory(player);
            player.sendMessage(ChatColor.RED + "该物品已被禁止拾取！");
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (plugin.isAdmin(player.getName().toLowerCase())) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();

        if (isBannedItem(result)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "该物品已被禁止合成！");
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();

        if (!isBannedItem(item)) {
            return;
        }

        Inventory destination = event.getDestination();
        Inventory source = event.getSource();

        if (destination.getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
        }

        if (source.getType() == InventoryType.PLAYER && destination.getType() != InventoryType.PLAYER) {
            event.setCancelled(true);
        }
    }

    private void clearAllBannedItemsFromInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && isBannedItem(item)) {
                inventory.setItem(i, null);
            }
        }

        ItemStack cursorItem = player.getItemOnCursor();
        if (isBannedItem(cursorItem)) {
            player.setItemOnCursor(null);
        }
    }

    private boolean isBannedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        return plugin.isBannedItem(item.getType().name());
    }
}
