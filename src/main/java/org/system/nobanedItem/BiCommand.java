package org.system.nobanedItem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.ArrayList;

public class BiCommand implements CommandExecutor {

    private final NoBanedItem plugin;

    public BiCommand(NoBanedItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("nobaneditem.admin")) {
            player.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "/bi <itemadd|adminadd> [player]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "itemadd":
                handleItemAdd(player);
                break;

            case "adminadd":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "用法: /bi adminadd <玩家名称>");
                    return true;
                }
                handleAdminAdd(player, args[1]);
                break;

            default:
                player.sendMessage(ChatColor.RED + "未知子命令！用法: /bi <itemadd|adminadd>");
                break;
        }

        return true;
    }

    private void handleItemAdd(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "你手中没有物品！");
            return;
        }

        // 获取物品的类型和标识符
        String materialName = itemInHand.getType().name();
        String itemName = materialName;

        // 如果是自定义物品（带有NBT标签），可能需要额外处理
        // 这里我们只记录材质名称
        List<String> bannedItems = plugin.getBanedItemConfig().getStringList("banned_items");
        if (bannedItems.contains(itemName)) {
            player.sendMessage(ChatColor.YELLOW + "该物品已经存在于禁用列表中！");
            return;
        }

        bannedItems.add(itemName);
        plugin.getBanedItemConfig().set("banned_items", bannedItems);
        plugin.saveBanedItemConfig();

        player.sendMessage(ChatColor.GREEN + "成功将物品 " + materialName + " 添加到禁用列表！");
    }

    private void handleAdminAdd(Player player, String targetPlayerName) {
        if (plugin.isAdmin(targetPlayerName.toLowerCase())) {
            player.sendMessage(ChatColor.YELLOW + "该玩家已经是管理员！");
            return;
        }

        plugin.addAdmin(targetPlayerName);
        player.sendMessage(ChatColor.GREEN + "成功将 " + targetPlayerName + " 添加为管理员！");
    }
}