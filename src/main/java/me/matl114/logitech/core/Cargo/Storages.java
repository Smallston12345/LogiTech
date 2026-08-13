package me.matl114.logitech.core.Cargo;

import java.util.function.Supplier;
import me.matl114.logitech.MyAddon;
import me.matl114.logitech.core.AddSlimefunItems;
import me.matl114.logitech.core.Cargo.Singularity.SingularityProxy;
import me.matl114.logitech.core.Cargo.Singularity.SingularityStorage;
import me.matl114.logitech.core.CustomSlimefunItem;
import me.matl114.logitech.core.Depends.NetworksQuantumProxyAdapter;
import me.matl114.logitech.core.Depends.NetworksQuantumStorageAdapter;
import me.matl114.logitech.core.Registries.AddDepends;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.Debug;
import me.matl114.logitech.utils.UtilClass.StorageClass.StorageType;

public class Storages {
    public static void setup() {}

    public static void disableNetworkStorage() {

        AddDepends.NTW_STORAGE_DISPLAY.addDisplayRecipe(AddUtils.getInfoShow(
                "&c警告!",
                "&7由於網絡拓展未知錯誤,該物品已經不兼容",
                "&7請聯繫伺服器管理員檢查網絡版本",
                "&7如果你認為這是一個偶發性錯誤或者損壞的物品導致的問題,請重啓伺服器",
                "&7如果你認為這是網絡版本問題,請更換網絡版本以重新兼容該物品"));
        AddDepends.NTW_STORAGE_DISPLAY.addDisplayRecipe(null);
    }

    public static void disableNetworkProxy() {
        try {
            ((CustomSlimefunItem) AddSlimefunItems.QUANTUM_LINK)
                    .addDisplayRecipe(AddUtils.getInfoShow(
                            "&c警告!",
                            "&7由於網絡拓展未知錯誤,該物品已經不兼容網絡存儲",
                            "&7請聯繫伺服器管理員檢查網絡版本",
                            "&7如果你認為這是一個偶發性錯誤或者損壞的物品導致的問題,請重啓伺服器",
                            "&7如果你認為這是網絡版本問題,請更換網絡版本以重新兼容該物品"));
            ((CustomSlimefunItem) AddSlimefunItems.QUANTUM_LINK).addDisplayRecipe(null);
        } catch (Throwable es) {
            Debug.logger(es);
        }
    }

    public static final StorageType NTWSTORAGE_PROXY = ((Supplier<StorageType>) (() -> {
                if (MyAddon.getInstance().getServer().getPluginManager().isPluginEnabled("Networks")) {
                    try {
                        return new NetworksQuantumProxyAdapter();
                    } catch (Throwable e) {
                        Debug.logger(
                                "WARNING: AN INTERNAL ERROR OCCUR WHEN ADAPTING NETWORK STORAGE,DISABLING RELAVENT FUNCTION");
                        disableNetworkProxy();
                    }
                }
                return null;
            }))
            .get();
    public static final StorageType SINGULARITY_PROXY = new SingularityProxy();
    public static final StorageType NTWSTORAGE_ADAPTOR = ((Supplier<StorageType>) (() -> {
                if (MyAddon.getInstance().getServer().getPluginManager().isPluginEnabled("Networks")) {
                    try {
                        return new NetworksQuantumStorageAdapter();
                    } catch (Throwable e) {
                        Debug.logger(
                                "WARNING: AN INTERNAL ERROR OCCUR WHEN ADAPTING NETWORK STORAGE,DISABLING RELAVENT FUNCTION");
                        disableNetworkStorage();
                    }
                }
                return null;
            }))
            .get();
    public static final StorageType SINGULARITY = new SingularityStorage();
}
