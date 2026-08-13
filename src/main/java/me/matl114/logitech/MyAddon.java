package me.matl114.logitech;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import java.util.logging.Level;
import lombok.Getter;
import me.matl114.logitech.core.AddGroups;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.AddSlimefunItems;
import me.matl114.logitech.core.Cargo.SpaceStorage.StorageSpace;
import me.matl114.logitech.core.Cargo.Storages;
import me.matl114.logitech.core.Depends.DependencyInfinity;
import me.matl114.logitech.core.Depends.DependencyNetwork;
import me.matl114.logitech.core.Depends.SupportedPluginManager;
import me.matl114.logitech.core.Registries.AddDepends;
import me.matl114.logitech.core.Registries.CustomEffects;
import me.matl114.logitech.core.Registries.MultiBlockTypes;
import me.matl114.logitech.listeners.ListenerManager;
import me.matl114.logitech.listeners.ProtectionManager;
import me.matl114.logitech.manager.EquipmentFUManager;
import me.matl114.logitech.manager.RadiationRegionManager;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.CommandClass.LogitechMain;
import me.matl114.logitech.utils.UtilClass.MultiBlockClass.MultiBlockService;
import me.matl114.matlib.core.AddonInitialization;
import me.matl114.matlib.utils.command.commandGroup.AbstractMainCommand;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddon extends JavaPlugin implements SlimefunAddon {
    public static boolean testmod = false;
    public static boolean clearConfig = false;

    public static boolean testmode() {
        return testmod;
    }

    private static MyAddon instance;
    private static PluginManager manager;

    public static MyAddon getInstance() {
        return instance;
    }

    public static PluginManager getManager() {
        return manager;
    }

    public static String username;
    public static String repo;
    public static String branch;
    private static AbstractMainCommand command;
    private static AddonInitialization matlibInstance;

    @Getter
    public static SupportedPluginManager supportedPluginManager;

    static {
        username = "m1919810";
        repo = "LogiTech";
        branch = "master";
    }

    @Override
    public void onEnable() {
        instance = this;
        if (!PaperLib.isPaper()) {
            getLogger().log(Level.WARNING, "#######################################################");
            getLogger().log(Level.WARNING, "");
            getLogger().log(Level.WARNING, "自 25/2/1 起 Slimefun及其附屬");
            getLogger().log(Level.WARNING, "轉為 Paper 插件, 你必須要使用 Paper");
            getLogger().log(Level.WARNING, "或其分支才可使用 Slimefun及其附屬");
            getLogger().log(Level.WARNING, "立即下載 Paper: https://papermc.io/downloads/paper");
            getLogger().log(Level.WARNING, "");
            getLogger().log(Level.WARNING, "#######################################################");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        matlibInstance = new AddonInitialization(this, "LOGITECH")
                .displayName("邏輯工藝")
                .onEnable()
                .cast();
        manager = getServer().getPluginManager();
        checkVersion();
        // 從 config.yml 中讀取插件配置
        Config cfg = new Config(this);

        if (cfg.getBoolean("options.auto-update")) {
            tryUpdate();
        }
        // 你可以在這裏添加自動更新功能
        //        }if(cfg.getBoolean("options.test")||testmod) {
        //            testmod = true;
        //            Debug.logger("Addon is running on TEST MODE");
        //        }
        ConfigLoader.load(this);
        Language.loadConfig(ConfigLoader.LANGUAGE);
        try {
            DependencyNetwork.init();
        } catch (Throwable e) {
            Debug.logger("在載入軟依賴 NETWORKS時出現錯誤! 出現版本不匹配的附屬,禁用附屬相應內容");
            Debug.logger(e);
        }
        try {
            DependencyInfinity.init();
        } catch (Throwable e) {
            Debug.logger("在載入軟依賴 INFINITY時出現錯誤! 出現版本不匹配的附屬,禁用附屬相應內容");
            Debug.logger(e);
        }
        try {
            supportedPluginManager = new SupportedPluginManager();
        } catch (Throwable e) {
            Debug.logger("在載入插件兼容時出現錯誤");
            Debug.logger(e);
        }
        Debug.logger("軟依賴檢測完畢");
        AddGroups.registerGroups(this);
        Debug.logger("物品組載入完畢");
        Debug.logger("自定義物品載入完畢");
        // 物品註冊
        AddItem.registerItemStack();
        Debug.logger("物品模板載入完畢");
        // 黏液物品註冊
        AddSlimefunItems.registerSlimefunItems();
        Debug.logger("黏液物品註冊完畢");
        // 世界配置
        StorageSpace.setup();
        Debug.logger("空間存儲世界配置完畢");
        // 註冊關於依賴的相關內容
        AddDepends.setup(this);
        Debug.logger("依賴註冊完畢");
        Schedules.setupSchedules(this);
        Debug.logger("計劃線程設立完畢");
        // 載入監聽器
        ListenerManager.registerListeners(getInstance(), getManager());

        // 載入黏液保護模塊
        ProtectionManager.registerProtection(getInstance(), getManager());

        Debug.logger("監聽器註冊完畢");
        // 載入bs工具
        DataCache.setup();
        // 註冊存儲類型
        Storages.setup();
        // 註冊多方塊服務
        MultiBlockService.setup();
        // 註冊多方塊類型
        MultiBlockTypes.setup();
        // 載入自定義效果機制
        CustomEffects.setup();
        // 載入輻射機制
        RadiationRegionManager.setup();
        // 載入裝備監聽管理器
        new EquipmentFUManager();
        // 載入配方工具
        CraftUtils.setup();
        // 載入世界操作工具
        WorldUtils.setup();
        // 載入Bukkit操作工具
        BukkitUtils.setup();
        // 載入貨運工具
        TransportUtils.setup();
        // 載入容器impl工具
        ContainerUtils.setup();
        Debug.logger("指令註冊完畢");
        //        command = new AddonCommand(this);
        //        command.register();
        command = new LogitechMain().registerCommand(this);
        // 註冊
        Debug.logger("附屬特性註冊完畢");
        // test

    }

    public void tryUpdate() {
        if (getDescription().getVersion().startsWith("Build")) {
            GuizhanUpdater.start(this, getFile(), username, repo, branch);
        }
    }

    public void checkVersion() {
        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "本插件需要 鬼斬前置庫插件(GuizhanLibPlugin) 才能運行!");
            getLogger().log(Level.SEVERE, "從此處下載: https://50l.cc/gzlib");
            getLogger().log(Level.SEVERE, "當出現該報錯時,作者對一切後續的報錯不負責");
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException("Dependency not found");
        }
        //        try {
        //            if( !Slimefun.getMinecraftVersion().isAtLeast(MinecraftVersion.MINECRAFT_1_20) ){
        //                getLogger().log(Level.SEVERE, "本插件需要在MC 1.20/1.20.x的版本運行");
        //                getLogger().log(Level.SEVERE, "當出現該報錯時,作者對一切後續的報錯不負責");
        //            }else{
        //                Debug.logger("MC最低版本檢測通過");
        //            }
        //        } catch (NoClassDefFoundError | NoSuchFieldError e) {
        //            for (int i = 0; i < 20; i++) {
        //                getLogger().severe("你需要更新 Slimefun4 才能進行版本檢測！");
        //            }
        //        }

    }

    @Override
    public void onDisable() {
        matlibInstance.onDisable();
        // 禁用插件的邏輯...
        Schedules.onDisableSchedules(this);
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public String getBugTrackerURL() {
        // 你可以在這裏返回你的問題追蹤器的網址，而不是 null
        return null;
    }

    public String getWikiURL() {
        return "https://github.com/m1919810/LogiTech/wiki/{0}";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        /*
         * 你需要返回對你插件的引用。
         * 如果這是你插件的主類，只需要返回 "this" 即可。
         */
        return this;
    }
}
