package org.system.nobanedItem;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

public class NoBanedItem extends JavaPlugin {

    private File banedItemFile;
    private YamlConfiguration banedItemConfig;
    private File adminFile;
    private YamlConfiguration adminConfig;

    @Override
    public void onEnable() {
        // 创建插件配置目录
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        // 初始化配置文件
        initializeConfigFiles();

        // 注册命令执行器
        getCommand("bi").setExecutor(new BiCommand(this));

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new ItemProtectionListener(this), this);

        getLogger().info("NoBanedItem 插件已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoBanedItem 插件已禁用！");
    }

    private void initializeConfigFiles() {
        // 创建 BanedItem.yml
        banedItemFile = new File(getDataFolder(), "BanedItem.yml");
        if (!banedItemFile.exists()) {
            try {
                banedItemFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("无法创建 BanedItem.yml 文件: " + e.getMessage());
            }
        }
        banedItemConfig = YamlConfiguration.loadConfiguration(banedItemFile);

        // 创建 admin.yml
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

    public boolean isAdmin(String playerName) {
        return adminConfig.getBoolean(playerName.toLowerCase(), false);
    }

    public void addAdmin(String playerName) {
        adminConfig.set(playerName.toLowerCase(), true);
        saveAdminConfig();
    }

    public void removeAdmin(String playerName) {
        adminConfig.set(playerName.toLowerCase(), false);
        saveAdminConfig();
    }
}