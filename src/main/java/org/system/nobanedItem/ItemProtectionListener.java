package org.system.nobanedItem;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Inventory;

import java.util.List;

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

        // 检查玩家是否为管理员
        if (plugin.isAdmin(player.getName().toLowerCase())) {
            return; // 管理员不受限制
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (isBannedItem(clickedItem)) {
            event.setCancelled(true); // 取消点击事件
            // 如果点击的是玩家自己的背包（非容器）
            if (event.getInventory().getType() == InventoryType.PLAYER) {
                event.setCancelled(true);
                // 清除玩家背包内所有非法物品
                clearAllBannedItemsFromInventory(player);
                player.sendMessage(ChatColor.RED + "发现非法物品，已清除背包内所有非法物品！");
            } else {
                // 如果点击的是容器中的非法物品，则取消点击并将物品替换为空气
                event.setCancelled(true); // 取消点击事件
                // 将物品替换为空气
                event.setCurrentItem(null);
                player.sendMessage(ChatColor.RED + "该物品已被禁止使用！");
                clearAllBannedItemsFromInventory(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否为管理员
        if (plugin.isAdmin(player.getName().toLowerCase())) {
            return; // 管理员不受限制
        }

        ItemStack itemInHand = event.getItem();

        if (itemInHand != null && isBannedItem(itemInHand)) {
            // 检查是否是右键方块或空气
            Action action = event.getAction();
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true); // 取消点击事件

                // 清除玩家背包内所有非法物品
                clearAllBannedItemsFromInventory(player);

                player.sendMessage(ChatColor.RED + "禁止使用");
            }
        }
    }

    /**
     * 清除玩家背包内所有非法物品
     */
    private void clearAllBannedItemsFromInventory(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && isBannedItem(item)) {
                // 将非法物品设置为空气
                inventory.setItem(i, null);
            }
        }
    }

    private boolean isBannedItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        String materialName = item.getType().name();

        List<String> bannedItems = plugin.getBanedItemConfig().getStringList("banned_items");
        return bannedItems.contains(materialName);
    }
}