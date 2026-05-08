package org.system.nobanedItem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Set;

public class BiCommand implements CommandExecutor {

    private static final String PERMISSION = "nobaneditem.admin";

    private final NoBanedItem plugin;

    public BiCommand(NoBanedItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行！");
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "itemadd" -> handleItemAdd(player);
            case "itemremove" -> handleItemRemove(player, args);
            case "itemlist" -> handleItemList(player);
            case "adminadd" -> handleAdminAdd(player, args);
            case "adminremove" -> handleAdminRemove(player, args);
            case "adminlist" -> handleAdminList(player);
            case "reload" -> handleReload(player);
            default -> {
                player.sendMessage(ChatColor.RED + "未知子命令！");
                sendHelp(player);
            }
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== NoBanedItem 命令帮助 ===");
        player.sendMessage(ChatColor.YELLOW + "/bi itemadd" + ChatColor.WHITE + " - 将手中物品添加到禁用列表");
        player.sendMessage(ChatColor.YELLOW + "/bi itemremove <material>" + ChatColor.WHITE + " - 从禁用列表移除物品");
        player.sendMessage(ChatColor.YELLOW + "/bi itemlist" + ChatColor.WHITE + " - 查看所有禁用的物品");
        player.sendMessage(ChatColor.YELLOW + "/bi adminadd <玩家>" + ChatColor.WHITE + " - 添加管理员");
        player.sendMessage(ChatColor.YELLOW + "/bi adminremove <玩家>" + ChatColor.WHITE + " - 移除管理员");
        player.sendMessage(ChatColor.YELLOW + "/bi adminlist" + ChatColor.WHITE + " - 查看所有管理员");
        player.sendMessage(ChatColor.YELLOW + "/bi reload" + ChatColor.WHITE + " - 重载配置文件");
    }

    private void handleItemAdd(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "你手中没有物品！");
            return;
        }

        String materialName = itemInHand.getType().name();

        if (plugin.isBannedItem(materialName)) {
            player.sendMessage(ChatColor.YELLOW + "该物品已经存在于禁用列表中！");
            return;
        }

        plugin.addBannedItem(materialName);
        player.sendMessage(ChatColor.GREEN + "成功将物品 " + materialName + " 添加到禁用列表！");
    }

    private void handleItemRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /bi itemremove <material>");
            return;
        }

        String materialName = args[1].toUpperCase();

        if (!plugin.isBannedItem(materialName)) {
            player.sendMessage(ChatColor.YELLOW + "该物品不在禁用列表中！");
            return;
        }

        plugin.removeBannedItem(materialName);
        player.sendMessage(ChatColor.GREEN + "成功将物品 " + materialName + " 从禁用列表移除！");
    }

    private void handleItemList(Player player) {
        Set<String> bannedItems = plugin.getBannedItemsCache();

        if (bannedItems.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "当前没有禁用的物品。");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== 禁用物品列表 (" + bannedItems.size() + " 个) ===");
        for (String item : bannedItems) {
            player.sendMessage(ChatColor.WHITE + " - " + item);
        }
    }

    private void handleAdminAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /bi adminadd <玩家名称>");
            return;
        }

        String targetPlayerName = args[1];

        if (plugin.isAdmin(targetPlayerName.toLowerCase())) {
            player.sendMessage(ChatColor.YELLOW + "该玩家已经是管理员！");
            return;
        }

        plugin.addAdmin(targetPlayerName);
        player.sendMessage(ChatColor.GREEN + "成功将 " + targetPlayerName + " 添加为管理员！");
    }

    private void handleAdminRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "用法: /bi adminremove <玩家名称>");
            return;
        }

        String targetPlayerName = args[1];

        if (!plugin.isAdmin(targetPlayerName.toLowerCase())) {
            player.sendMessage(ChatColor.YELLOW + "该玩家不是管理员！");
            return;
        }

        plugin.removeAdmin(targetPlayerName);
        player.sendMessage(ChatColor.GREEN + "成功将 " + targetPlayerName + " 从管理员列表移除！");
    }

    private void handleAdminList(Player player) {
        Set<String> admins = plugin.getAdminsCache();

        if (admins.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "当前没有管理员。");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== 管理员列表 (" + admins.size() + " 人) ===");
        for (String admin : admins) {
            player.sendMessage(ChatColor.WHITE + " - " + admin);
        }
    }

    private void handleReload(Player player) {
        plugin.reloadConfigs();
        player.sendMessage(ChatColor.GREEN + "配置已重新加载！");
    }
}
