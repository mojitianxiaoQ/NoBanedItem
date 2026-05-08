package org.system.nobanedItem;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoBanedItem extends JavaPlugin {

    private File banedItemFile;
    private YamlConfiguration banedItemConfig;
    private File adminFile;
    private YamlConfiguration adminConfig;

    private final Set<String> bannedItemsCache = new HashSet<>();
    private final Set<String> adminsCache = new HashSet<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        initializeConfigFiles();
        loadCaches();

        getCommand("bi").setExecutor(new BiCommand(this));
        getCommand("bi").setTabCompleter(new BiTabCompleter(this));

        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);

        getLogger().info("NoBanedItem 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoBanedItem 插件已禁用！");
    }

    private void initializeConfigFiles() {
        banedItemFile = new File(getDataFolder(), "BanedItem.yml");
        if (!banedItemFile.exists()) {
            try {
                banedItemFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("无法创建 BanedItem.yml 文件: " + e.getMessage());
            }
        }
        banedItemConfig = YamlConfiguration.loadConfiguration(banedItemFile);

        adminFile = new File(getDataFolder(), "admin.yml");
        if (!adminFile.exists()) {
            try {
                adminFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("无法创建 admin.yml 文件: " + e.getMessage());
            }
        }
        adminConfig = YamlConfiguration.loadConfiguration(adminFile);
    }

    private void loadCaches() {
        bannedItemsCache.clear();
        List<String> bannedItems = banedItemConfig.getStringList("banned_items");
        bannedItemsCache.addAll(bannedItems);

        adminsCache.clear();
        for (String key : adminConfig.getKeys(false)) {
            if (adminConfig.getBoolean(key, false)) {
                adminsCache.add(key.toLowerCase());
            }
        }

        getLogger().info("已加载 " + bannedItemsCache.size() + " 个禁用物品, " + adminsCache.size() + " 个管理员");
    }

    public void reloadConfigs() {
        banedItemConfig = YamlConfiguration.loadConfiguration(banedItemFile);
        adminConfig = YamlConfiguration.loadConfiguration(adminFile);
        loadCaches();
    }

    public Set<String> getBannedItemsCache() {
        return Collections.unmodifiableSet(bannedItemsCache);
    }

    public Set<String> getAdminsCache() {
        return Collections.unmodifiableSet(adminsCache);
    }

    public YamlConfiguration getBanedItemConfig() {
        return banedItemConfig;
    }

    public File getBanedItemFile() {
        return banedItemFile;
    }

    public YamlConfiguration getAdminConfig() {
        return adminConfig;
    }

    public File getAdminFile() {
        return adminFile;
    }

    public void saveBanedItemConfig() {
        try {
            banedItemConfig.save(banedItemFile);
        } catch (IOException e) {
            getLogger().severe("无法保存 BanedItem.yml: " + e.getMessage());
        }
    }

    public void saveAdminConfig() {
        try {
            adminConfig.save(adminFile);
        } catch (IOException e) {
            getLogger().severe("无法保存 admin.yml: " + e.getMessage());
        }
    }

    public boolean isBannedItem(String materialName) {
        return bannedItemsCache.contains(materialName);
    }

    public void addBannedItem(String materialName) {
        List<String> bannedItems = banedItemConfig.getStringList("banned_items");
        if (!bannedItems.contains(materialName)) {
            bannedItems.add(materialName);
            banedItemConfig.set("banned_items", bannedItems);
            saveBanedItemConfig();
            bannedItemsCache.add(materialName);
        }
    }

    public void removeBannedItem(String materialName) {
        List<String> bannedItems = banedItemConfig.getStringList("banned_items");
        if (bannedItems.remove(materialName)) {
            banedItemConfig.set("banned_items", bannedItems);
            saveBanedItemConfig();
            bannedItemsCache.remove(materialName);
        }
    }

    public boolean isAdmin(String playerName) {
        return adminsCache.contains(playerName.toLowerCase());
    }

    public void addAdmin(String playerName) {
        String lowerName = playerName.toLowerCase();
        adminConfig.set(lowerName, true);
        saveAdminConfig();
        adminsCache.add(lowerName);
    }

    public void removeAdmin(String playerName) {
        String lowerName = playerName.toLowerCase();
        adminConfig.set(lowerName, false);
        saveAdminConfig();
        adminsCache.remove(lowerName);
    }
}
