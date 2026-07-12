package me.matl114.logitech.core.Cargo.StorageMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusher;
import me.matl114.logitech.utils.UtilClass.StorageClass.ItemStorageCache;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractIOPort extends AbstractMachine {
    public AbstractIOPort(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe, 0, 0);
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&7機制 - &e存儲性方塊",
                        "&7該方塊為存儲性方塊",
                        "提供一個特殊的存儲槽,用於承載和交互存儲類物品",
                        "&7本交互接口支持的存儲類物品:",
                        "&7- 邏輯工藝 概念奇點",
                        "&7- 網絡(拓展) 量子存儲系列",
                        "&7作為存儲性方塊,該方塊可以被[%s]綁定".formatted(Language.get("Cargo.QUANTUM_LINK.Name"))),
                null,
                AddUtils.getInfoShow(
                        "&7機制 - &e交互存儲",
                        "&7該方塊可以用於設置存儲類物品的種類",
                        "&7將空的&c存儲類物品&7置於其中指定存儲槽中",
                        "&7在輸入槽內放入物品即可設置存儲種類",
                        "&7當存儲槽中的存儲物品已設置物品種類,",
                        "&7該機器會嘗試將輸入槽中的物品輸入存儲,",
                        "&7該機器會嘗試將物品從存儲輸出至輸出槽",
                        "&7註明: &e網絡量子系列不支持空存儲設置存儲種類")));
    }

    public static final String ITEM_DISPLAY_INFO_1 = "&8奇點中存儲的物品樣式";
    public static final ItemStack ITEM_DISPLAY_NULL =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c未檢測到物品存儲!");
    protected static final ItemStack INFO_ITEM =
            new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6將奇點置於下方", "&a機器將自動檢測奇點內容並運行");

    public abstract void addInfo(ItemStack item);

    public abstract void constructMenu(BlockMenuPreset preset);

    public abstract int[] getInputSlots();

    public abstract int[] getOutputSlots();

    public abstract int getStorageSlot();

    public abstract int getDisplaySlot();

    public void updateMenu(BlockMenu inv, Block b, Settings mod) {
        Location loc = inv.getLocation();
        ItemStorageCache cache = ItemStorageCache.getCache(loc);
        if (cache != null) {
            // 保證newInstance的時候也會call到該方法
            cache.setPersistent(false);
            cache.updateMenu(inv);
            cache.setPersistent(true);
            if (cache.getItem() != null && getDisplaySlot() >= 0) {
                ItemStack tmp = AddUtils.addLore(cache.getItem(), ITEM_DISPLAY_INFO_1);
                inv.replaceExistingItem(getDisplaySlot(), tmp);
            }
        }
    }

    public void newMenuInstance(BlockMenu inv, Block b) {
        inv.addMenuOpeningHandler(player -> {
            updateMenu(inv, b, Settings.RUN);
        });
        inv.addMenuCloseHandler(player -> {
            updateMenu(inv, b, Settings.RUN);
        });
        inv.addMenuClickHandler(getStorageSlot(), ((player, i, itemStack, clickAction) -> {
            ItemStorageCache cache = ItemStorageCache.getCache(inv.getLocation());
            if (itemStack == null || itemStack.getAmount() != 1) {
                if (cache != null) {
                    AddUtils.sendMessage(player, "&c存儲物品與緩存並不同步,請稍後再試");
                    return false;
                } else return true;
            } else {
                // 當有人企圖從中間取出物品時
                if (cache != null) {
                    if (cache.keepRelated(itemStack)) {
                        // ItemStorageCache.removeCache(inv.getLocation());
                        cache.setPersistent(false);
                        cache.updateMenu(inv);
                        cache.setPersistent(true);
                        cache.setDeprecate(true);
                        inv.replaceExistingItem(getDisplaySlot(), ITEM_DISPLAY_NULL);
                        return true;
                    } else {
                        AddUtils.sendMessage(player, "&c存儲物品與緩存並不同步,請稍後再試");
                        return false;
                    }
                } else {
                    return true;
                }
            }
        }));
        // 清除可能的殘留
        ItemStack stack = inv.getItemInSlot(getStorageSlot());
        if (stack == null || stack.getAmount() != 1) {
            ItemStorageCache.removeCache(inv.getLocation());
        } else {
            long history = getStorageAmount(inv.getLocation());
            if (history >= 0) {
                ItemStorageCache cache = ItemStorageCache.getOrCreate(
                      inv, stack, stack.getItemMeta(), getStorageSlot(), i -> !i.isStorageProxy());
                if (cache == null) {
                    if (getDisplaySlot() >= 0) {
                        inv.replaceExistingItem(getDisplaySlot(), ITEM_DISPLAY_NULL);
                    }
                    ItemStorageCache.removeCache(inv.getLocation());
                } else { // 重啓之時
                    cache.setSaveSlot(getStorageSlot());
                    // 載入history
                    cache.setAmount(history);
                    // 設置為長期存在的 不會實時clone保存
                    cache.updateMenu(inv);
                    cache.setPersistent(true);
                    ItemStorageCache.setCache(inv, cache);
                }
            } else {
                ItemStorageCache.removeCache(inv.getLocation());
            }
        }
    }

    public boolean useAdvancedMenu() {
        return true;
    }

    @Override
    public void listenDragClick(BlockMenu inv, InventoryDragEvent e) {
        if (e.getRawSlots().contains(getStorageSlot())) {
            e.setCancelled(true);
        }
    }

    @Override
    public void listenOriginClick(BlockMenu inv, InventoryClickEvent e) {
        super.listenOriginClick(inv, e);
        if (e.isCancelled()) {
            return;
        }
        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
            if (e.getRawSlot() >= inv.getInventory().getSize()) {
                ItemStack stack = e.getCurrentItem();
                ItemStack storage = inv.getItemInSlot(getStorageSlot());
                if (storage != null && stack != null && CraftUtils.matchItemStack(stack, storage, false)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    public static long getStorageAmount(Location loc) {
        return DataCache.getCustomDataLong(loc, "amt", -1L);
    }

    public static void setStorageAmount(Location loc, long t, boolean withException) {
        SlimefunBlockData data = DataCache.safeGetBlockCacheWithLoad(loc);
        if (data != null && SlimefunItem.getById(data.getSfId()) instanceof AbstractIOPort) {
            setAmount(data, t);

        } else {
            if (withException) throw new IllegalArgumentException("該位置的粘液數據不是AbstractIOPort!");
        }
    }

    protected static void setAmount(SlimefunBlockData data, long t) {
        DataCache.setCustomDataLong(data, "amt", t);
    }

    public void process(Block b, BlockMenu menu, SlimefunBlockData data) {
        // 先確認存儲cache
        ItemStack stack = menu.getItemInSlot(getStorageSlot());
        Location loc = menu.getLocation();
        // 如果沒有放存儲 則立刻就要清除當前cache
        if (stack == null || stack.getAmount() != 1) {
            ItemStorageCache cache = ItemStorageCache.removeCache(loc);
            if (cache != null && !cache.isNotValid()) {
                // 同步 如果他真的被拿走了 但是一般來説這是不可能的事情
                cache.setPersistent(false);
                cache.updateMenu(menu);

                cache.setDeprecate(true);
            }
            // 刪除cache後 物品record變為-1個
            // 在remove中已經有操作了，刪除
            // setAmount(data,-1);

            if (menu.hasViewer() && getDisplaySlot() >= 0) {
                menu.replaceExistingItem(getDisplaySlot(), ITEM_DISPLAY_NULL);
            }
            return;
        }
        // 現在 有存儲在的 嘗試找到cache
        ItemStorageCache cache = ItemStorageCache.getCache(loc);
        // cache不存在或者    cache和現在的奇點記錄的不同 記錄不同只有可能是玩家替換,這時候是因為
        if (cache == null || cache.isNotValid() || (!cache.keepRelated(stack))) {
            // 對不上了自然要丟掉廢棄的
            if (cache != null) {
                // 這裏是否要刷新？ 應該不用
                // 這裏的情況是 存在一個cache 在某兩個tick之間 玩家切換了存儲為另一個物品，導致cache被標記為deprecated 或者是產生了奇怪的updateMenu使得cache與stack不同步了
                // 不管怎麼樣 在鏈接斷開的時候 已經進行了同步了
                // 這裏我們只需要執行移除就行
                ItemStorageCache.removeCache(loc);
            }
            // 嘗試載入新的cache 可能是空的
            // don't support proxy ,that's a nightmare
            cache = ItemStorageCache.getOrCreate(
                   menu, stack, stack.getItemMeta(), getStorageSlot(), i -> !i.isStorageProxy());
            if (cache == null) {
                if (menu.hasViewer() && getDisplaySlot() >= 0) {
                    menu.replaceExistingItem(getDisplaySlot(), ITEM_DISPLAY_NULL);
                }
                return;
            } else {
                cache.setSaveSlot(getStorageSlot());
                // 只有重啓的時候需要使用history 其他時間物品都是同步的
                // 設置為長期存在的 不會實時clone保存
                cache.updateMenu(menu);
                cache.setPersistent(true);
                ItemStorageCache.setCache(menu, cache);
            }
        }
        // 執行貨運
        ItemPusher[] cachelst = new ItemPusher[] {cache};
        TransportUtils.cacheTransportation(menu, cachelst, menu, getInputSlots(), Settings.INPUT);
        TransportUtils.cacheTransportation(menu, cachelst, menu, getOutputSlots(), Settings.OUTPUT);
        cache.updateMenu(menu);
        // 例行刷新
        if (menu.hasViewer()) {
            updateMenu(menu, b, Settings.RUN);
        }
    }

    public void onBreak(BlockBreakEvent e, BlockMenu menu) {
        Location loc = menu.getLocation();
        ItemStorageCache cache = ItemStorageCache.removeCache(loc);
        if (cache != null) {
            cache.setPersistent(false);
            cache.updateMenu(menu);
        }
        menu.dropItems(loc, getStorageSlot());
        //
        super.onBreak(e, menu);
    }

    public void registerTick(SlimefunItem that) {
        // make IOPort operation also sync
        that.addItemHandler(FinalFeature.FINAL_SYNC_TICKER);
    }
}
