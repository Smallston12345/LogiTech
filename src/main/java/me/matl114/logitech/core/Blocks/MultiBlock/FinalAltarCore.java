package me.matl114.logitech.core.Blocks.MultiBlock;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashMap;
import java.util.Map;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Blocks.MultiBlockCore.MultiCore;
import me.matl114.logitech.core.Registries.MultiBlockTypes;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.DataCache;
import me.matl114.logitech.utils.Settings;
import me.matl114.logitech.utils.UtilClass.FunctionalClass.OutputStream;
import me.matl114.logitech.utils.UtilClass.MultiBlockClass.*;
import me.matl114.logitech.utils.UtilClass.MultiBlockClass.MultiLevelBlock.MultiLevelBlock;
import me.matl114.logitech.utils.UtilClass.MultiBlockClass.MultiLevelBlock.MultiLevelBlockType;
import me.matl114.logitech.utils.Utils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class FinalAltarCore extends MultiCore {
    protected static final String LVL_KEY = "lv";

    public interface FinalAltarChargable {
        public static String FORCE_STATE = "fi";

        default int getForced(SlimefunBlockData data) {
            return DataCache.getCustomData(data, FORCE_STATE, 0);
        }

        static void setForced(SlimefunBlockData data, int lvl) {
            DataCache.setCustomString(data, FORCE_STATE, String.valueOf(lvl));
        }

        default int mayForced(SlimefunBlockData data) {
            int forcelvl = DataCache.getCustomData(data, FORCE_STATE, 0);
            if (forcelvl > 0) {
                return forcelvl;
            }
            Location loc = data.getLocation().clone().add(0, -1, 0);
            // check if multiblock active
            if (DataCache.getSfItem(loc) instanceof FinalAltarCore && MultiBlockService.getStatus(loc) != 0) {
                // get lvl key in active multiblock
                int lv = DataCache.getCustomData(loc, FinalAltarCore.LVL_KEY, 0);
                if (lv > 0) {
                    DataCache.setCustomData(data, FORCE_STATE, lv);
                    return lv;
                }
            }
            return 0;
        }

        static void clearForced(Location loc) {
            SlimefunItem it = DataCache.getSfItem(loc);
            if (it instanceof FinalAltarChargable fac) {
                DataCache.setCustomData(loc, FORCE_STATE, 0);
            }
        }
    }

    protected final int[] BORDER = new int[] {0, 1, 2, 3, 5, 6, 7};
    protected final int HOLOGRAM_SLOT = 8;
    protected final int STATUS_SLOT = 4;
    protected final ItemStack STATUS_ITEM_ON_1 = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE,
            "&6點擊切換祭壇狀態",
            "&6shift點擊嘗試刷新祭壇",
            "&7祭壇狀態: &a開啓",
            "&7等級: &6壹",
            "&7關閉祭壇不會破壞祭壇本體,只會取消上方機器的激活狀態");
    protected final ItemStack STATUS_ITEM_ON_2 = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE,
            "&6點擊切換祭壇狀態",
            "&6shift點擊嘗試刷新祭壇",
            "&7祭壇狀態: &a開啓",
            "&7等級: &6貳",
            "&7關閉祭壇不會破壞祭壇本體,只會取消上方機器的激活狀態");
    protected final ItemStack STATUS_ITEM_OFF = new CustomItemStack(
            Material.RED_STAINED_GLASS_PANE,
            "&6點擊切換祭壇狀態",
            "&6shift點擊嘗試刷新祭壇",
            "&7祭壇狀態: &c關閉",
            "&7關閉祭壇不會破壞祭壇本體,只會取消上方機器的激活狀態");
    protected final ItemStack HOLOGRAM_ITEM_ON = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &a一級祭壇");
    protected final ItemStack HOLOGRAM_ITEM_ON_2 = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &a二級祭壇");
    protected final ItemStack HOLOGRAM_ITEM_OFF = new CustomItemStack(
            Material.RED_STAINED_GLASS_PANE, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &c關閉");
    protected final MultiLevelBlockType MBTYPE;
    public HashMap<String, ItemStack> MBID_TO_ITEM = new HashMap<>() {
        {
            put("final.sub", AddItem.FINAL_LASER.clone());
            put("final.frame", AddItem.FINAL_FRAME.clone());
            put("final.base", AddItem.FINAL_BASE.clone());
        }
    };

    public Map<String, ItemStack> getIdMappingDisplayUse() {
        return Map.copyOf(MBID_TO_ITEM);
    }

    public int[] getInputSlots() {
        return new int[0];
    }

    public int[] getOutputSlots() {
        return new int[0];
    }

    public FinalAltarCore(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, String blockId) {
        super(itemGroup, item, recipeType, recipe, blockId);
        this.MBTYPE = MultiBlockTypes.FINAL_ALTAR;
        this.setDisplayRecipes(Utils.list(AddUtils.getInfoShow(
                "&f機制 - &c多級多方塊機器",
                "&7終極祭壇擁有兩個等級",
                "&7其中,貳級終極祭壇需要在壹級終極祭壇上增加額外的多方塊結構",
                "&7兩者的構造在內置的投影中均可查看",
                "&7當你需要在壹,貳級終極祭壇切換時,請關閉多方塊並重啓,或者嘗試點擊刷新祭壇按鈕")));
        this.setAutoBuildDefault(false);
    }

    public MultiLevelBlockType getMultiBlockType() {
        return MBTYPE;
    }

    public MultiBlockService.MultiBlockBuilder BUILDER = ((core, type, uid) -> {
        int previouslevel = DataCache.getCustomData(core, LVL_KEY, 0);
        AbstractMultiBlock block = type;
        int level = 0;
        if (block instanceof MultiLevelBlock lb) {
            level = lb.getLevel();
        }
        if (previouslevel != 0 && level != previouslevel) {
            // not matched
            return null;
        }
        AbstractMultiBlockHandler blockHandler = MultiBlockHandler.createHandler(core, type, uid);

        DataCache.setCustomData(core, LVL_KEY, level);
        return blockHandler;
    });

    public MultiBlockService.MultiBlockBuilder getBuilder() {
        return BUILDER;
    }

    public void constructMenu(BlockMenuPreset inv) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; i++) {
            inv.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        inv.addItem(HOLOGRAM_SLOT, HOLOGRAM_ITEM_OFF);
    }

    public void onMultiBlockDisable(
            Location loc, AbstractMultiBlockHandler handler, MultiBlockService.DeleteCause cause) {
        super.onMultiBlockDisable(loc, handler, cause);
        // 這裏也要清除,lvl數據 防止上面重新讀取回來
        DataCache.setCustomString(loc, LVL_KEY, "0");
        // 要設置上面的機器,清除裏面的force level數據
        Location poweredLoc = loc.clone().add(0, 1, 0);
        FinalAltarChargable.clearForced(poweredLoc);
        BlockMenu inv = DataCache.getMenu(loc);
        if (inv != null) {
            inv.replaceExistingItem(STATUS_SLOT, STATUS_ITEM_OFF);
        }
    }

    public void onMultiBlockEnable(Location loc, AbstractMultiBlockHandler handler) {
        super.onMultiBlockEnable(loc, handler);
        BlockMenu inv = DataCache.getMenu(loc);
        if (inv != null) {
            updateMenu(inv, loc.getBlock(), Settings.RUN);
        }
    }

    public void updateMenu(BlockMenu inv, Block block, Settings mod) {
        int holoStatus = DataCache.getCustomData(inv.getLocation(), MultiBlockService.getHologramKey(), 0);
        if (holoStatus == 0) {
            inv.replaceExistingItem(HOLOGRAM_SLOT, HOLOGRAM_ITEM_OFF);

        } else if (holoStatus == 1) {
            inv.replaceExistingItem(HOLOGRAM_SLOT, HOLOGRAM_ITEM_ON);

        } else {
            inv.replaceExistingItem(HOLOGRAM_SLOT, HOLOGRAM_ITEM_ON_2);
        }
        int status = MultiBlockService.getStatus(inv.getLocation());
        if (status != 0) {
            int code = DataCache.getCustomData(inv.getLocation(), LVL_KEY, 0);
            inv.replaceExistingItem(STATUS_SLOT, code == 1 ? STATUS_ITEM_ON_1 : STATUS_ITEM_ON_2);
        } else {
            inv.replaceExistingItem(STATUS_SLOT, STATUS_ITEM_OFF);
        }
    }

    public void newMenuInstance(BlockMenu inv, Block block) {
        Location loc2 = block.getLocation();
        if (MultiBlockService.getStatus(loc2) != 0) {
            int lvl = DataCache.getCustomData(inv.getLocation(), LVL_KEY, 0);
            inv.replaceExistingItem(STATUS_SLOT, lvl == 1 ? STATUS_ITEM_ON_1 : STATUS_ITEM_ON_2);
        } else {
            inv.replaceExistingItem(STATUS_SLOT, STATUS_ITEM_OFF);
        }
        inv.addMenuClickHandler(STATUS_SLOT, ((player, i, itemStack, clickAction) -> {
            Location loc = inv.getLocation();
            if (MultiBlockService.getStatus(loc) == 0) { // not working
                if (MultiBlockService.createNewHandler(
                        loc, getBuilder(), getMultiBlockType(), OutputStream.getPlayerOut(player))) {
                    int code = DataCache.getCustomData(loc, LVL_KEY, 0);
                    if (code > 0) {
                        inv.replaceExistingItem(STATUS_SLOT, code == 1 ? STATUS_ITEM_ON_1 : STATUS_ITEM_ON_2);
                        AddUtils.sendMessage(player, "&a成功激活終極祭壇!");
                    } else {
                        MultiBlockService.deleteMultiBlock(loc, MultiBlockService.GENERIC);
                        AddUtils.sendMessage(player, "&c構建祭壇時出現未知方塊數據錯誤!請聯繫管理員");
                    }
                } else {
                    AddUtils.sendMessage(player, "&c終極祭壇結構不完整或者結構衝突!");
                    inv.replaceExistingItem(STATUS_SLOT, STATUS_ITEM_OFF);
                }

            } else { // working toggle off
                MultiBlockService.deleteMultiBlock(loc, MultiBlockService.MANUALLY);
                AddUtils.sendMessage(player, "&a終極祭壇成功關閉");
                inv.replaceExistingItem(STATUS_SLOT, STATUS_ITEM_OFF);
            }
            return false;
        }));
        DataCache.setCustomData(inv.getLocation(), MultiBlockService.getHologramKey(), 0);
        inv.addMenuClickHandler(HOLOGRAM_SLOT, ((player, i, itemStack, clickAction) -> {
            Location loc = inv.getLocation();
            int holoStatus = DataCache.getCustomData(inv.getLocation(), MultiBlockService.getHologramKey(), 0);
            int statusCode = MultiBlockService.getStatus(loc);
            MultiBlockService.removeHologramSync(loc);
            if (statusCode == 0) {
                if (holoStatus == 0) {
                    AddUtils.sendMessage(player, "&a全息投影已切換至一級祭壇!");
                    MultiBlockService.createHologram(
                            loc, MBTYPE.getSubParts(0), MultiBlockService.Direction.NORTH, MBID_TO_ITEM);
                    DataCache.setCustomData(loc, MultiBlockService.getHologramKey(), 1);
                } else if (holoStatus == 1) {
                    AddUtils.sendMessage(player, "&a全息投影已切換至二級祭壇!");
                    MultiBlockService.createHologram(loc, MBTYPE, MultiBlockService.Direction.WEST, MBID_TO_ITEM);
                    DataCache.setCustomData(loc, MultiBlockService.getHologramKey(), 2);
                } else {
                    AddUtils.sendMessage(player, "&a全息投影已關閉!");
                }
            }
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        updateMenu(inv, block, Settings.RUN);
    }

    public void processCore(Block b, BlockMenu menu) {
        if (menu.hasViewer()) {
            updateMenu(menu, b, Settings.RUN);
        }
    }

    public void onBreak(BlockBreakEvent e, BlockMenu inv) {
        super.onBreak(e, inv);
    }
}
