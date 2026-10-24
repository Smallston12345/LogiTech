package me.matl114.logitech.utils.UtilClass.StorageClass;

import me.matl114.logitech.utils.MathUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;

public class LocationStorageProxy extends ItemStorageCache {
    Location location;
    boolean lock = false;
    long lastStorageAmount;
    // 是否已經切換過繫結型別,一次代理生命週期內只允許切換一次
    boolean rebound = false;

    protected LocationStorageProxy(
          ItemStack item, ItemStack source, ItemMeta sourceMeta, int saveslot, StorageType type, Location location) {
        super(null, item, source, sourceMeta, saveslot, type);
        this.location = location;
        this.lastStorageAmount = ((LocationProxy) storageType).getItemAmount(location, item);
    }

    /**
     * 將該代理切換到繫結儲存中的另一個物品型別,並重新整理數量快照
     * @return true 表示已切換,呼叫方需要重新執行匹配
     */
    public boolean rebindToType(ItemStack sample, boolean forInput) {
        if (rebound || sample == null || !(storageType instanceof LocationProxy lp)) {
            return false;
        }
        if (item != null && sample.isSimilar(item)) {
            return false;
        }
        if (!lp.canAcceptItem(location, sample)) {
            return false;
        }
        ItemStack fresh = lp.getItemStack(location, sample);
        if (fresh == null) {
            return false;
        }
        long amount;
        if (!fresh.isSimilar(sample)) {
            if (forInput) {
                return false;
            }
            fresh = sample;
            amount = 0;
        } else {
            amount = lp.getItemAmount(location, fresh);
            if (forInput && amount <= 0) {
                return false;
            }
        }
        this.rebound = true;
        this.item = fresh;
        this.cnt = amount;
        this.storageAmount = amount;
        this.lastStorageAmount = amount;
        this.dirty = true;
        return true;
    }

    public boolean isDirty() {
        return lock || dirty;
    }

    public void updateStorage() {
        // 這裏是代理存儲 並不是唯一修改源
        // 需要刷新一下並加上用當前記錄-歷史記錄 的東西
        ItemStack item = getItem();
        LocationProxy proxy = (LocationProxy) storageType;
        
        long locationNowAmount = proxy.getItemAmount(location, item);
        long delta = locationNowAmount - this.lastStorageAmount;
        long locationSetAmount = Math.min(getMaxStackCnt(), getStorageAmountLong() + delta);
        if (locationSetAmount < 0) {
            locationSetAmount = 0;
        }
        proxy.setItemAmount(location, item, locationSetAmount);
        proxy.updateLocation(location);
        this.lastStorageAmount = locationSetAmount;
    }

    public boolean isNotValid(){
        return this.deprecated;
    }

    public void updateMenu(@Nonnull BlockMenu menu) {
        if (getItem() != null && !getItem().getType().isAir()) {
            updateItemStack();
            updateStorage();
        }
        dirty = false;
    }

    public Location getProxyLocation() {
        return location;
    }
}
