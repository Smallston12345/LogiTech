package me.matl114.logitech.core.Blocks.MultiBlock.SmithWorkShop;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import me.matl114.logitech.core.AddSlimefunItems;
import me.matl114.logitech.core.Interface.LogiTechChargable;
import me.matl114.logitech.core.Interface.MenuTogglableBlock;
import me.matl114.logitech.core.Items.SpecialItems.PlayerIdCard;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.DataCache;
import me.matl114.logitech.utils.MathUtils;
import me.matl114.logitech.utils.Settings;
import me.matl114.logitech.utils.UtilClass.MenuClass.DataMenuClickHandler;
import me.matl114.matlib.algorithms.dataStructures.struct.Pair;
import me.matl114.matlib.utils.inventory.itemStacks.CleanItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class SmithInterfaceCharger extends SmithingInterface implements MenuTogglableBlock {
    protected final int[] INPUT_SLOT = {19, 20, 21, 22, 23, 28, 29, 30, 31, 32, 37, 38, 39, 40, 41, 46, 47, 48, 49, 50};
    protected final int[] IDCARD_SLOT = {17, 26, 35, 44, 53};

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getExtraDropSlot() {
        return IDCARD_SLOT;
    }

    protected final int[] CHARGER_BORDER = {9, 10, 11, 12, 13, 14, 15, 18, 24, 27, 33, 36, 42, 45, 51, 6};
    protected final int[] REMOTE_BORDER = {16, 25, 34, 43, 52, 7};
    protected final int DISPLAY_SLOT = 0;
    protected final int[] DISPLAY_CHARGE_SLOT = {1, 2, 3, 4, 5};
    protected final double SPEED_MULTIPLY;
    protected final ItemStack CHARGER_DISPLAY = new CleanItemStack(
            ChestMenuUtils.getOutputSlotTexture(),
            "&6充電物品輸入槽",
            "&7放入可充電物品以充電",
            "&7可以向本附屬自定義的充電物品充電",
            "&a充電速率 = <基礎充電速率>* <速度增幅量>^(<速度增幅組件數量>)");
    protected final ItemStack REMOTE_DISPLAY = new CleanItemStack(
            ChestMenuUtils.getInputSlotTexture(),
            "&6玩家ID卡插入槽",
            "&7放入綁定玩家的玩家ID卡",
            "&7可以對該玩家進行遠程充電",
            "&7充電範圍包括玩家的盔甲槽和主手副手",
            "&7可以向本附屬自定義的充電物品充電",
            "&a充電速率 = <遠程充電速率>* <速度增幅量>^(<速度增幅組件數量>)",
            "&a充電範圍受<範圍增幅組件>數量影響",
            "&7分別為16格(0個),1600格(1個),同一世界(2個),任意位置(>2個)");
    protected final int LV0_LEN_SQ = 256;
    protected final int LV1_LEN_SQ = 2560000;
    protected final int TOGGLE_LAZYMOD = 8;
    protected final int remoteSpeed;
    protected final ItemStack TOGGLE_LAZYMOD_OFF =
            new CleanItemStack(Material.RED_STAINED_GLASS_PANE, "&6懶惰模式: &c關", "&7懶惰模式開啓時", "&7僅裝備電量過半才會進行遠程充電");
    protected final ItemStack TOGGLE_LAZYMOD_ON =
            new CleanItemStack(Material.GREEN_STAINED_GLASS_PANE, "&6懶惰模式: &a開", "&7懶惰模式開啓時", "&7僅裝備電量過半才會進行遠程充電");
    protected final ItemStack FUNCTION_DISPLAY = new CleanItemStack(
            SlimefunItems.BATTERY,
            "&6充電台功能概述",
            "&7充電台基礎充電速率: %dJ".formatted(energyConsumption),
            "&7本充電台擁有三種充電方式,他們同時進行",
            "&71.將可充電物品放入下方槽位,對所有可充電物品進行充電",
            "&72.玩家打開界面時,將對玩家的盔甲槽和主副手進行充電",
            "&73.將綁定了玩家的玩家ID卡插入右側槽位,會對玩家進行遠程充電",
            "&7機器會盡可能嘗試保證自身加載(暫未實現)",
            "&a向鍛鑄工坊插入對應增幅組件可以提升充電台的相應功能");
    // todo: 這個顯示正確數值
    protected final ItemStack[] DISPLAY_CHARGE_LEVEL = {
        new CleanItemStack(Material.RED_STAINED_GLASS_PANE, "&6電力顯示: &c不足20%"),
        new CleanItemStack(Material.YELLOW_STAINED_GLASS_PANE, "&6電力顯示: &e20%~40%"),
        new CleanItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6電力顯示: &e40%~60%"),
        new CleanItemStack(Material.GREEN_STAINED_GLASS_PANE, "&6電力顯示: &b60%~80%"),
        new CleanItemStack(Material.LIME_STAINED_GLASS_PANE, "&6電力顯示: &a80%~100%")
    };

    public Pair<ItemStack, Integer> getChargeDisplay(int charge) {
        float per = (((float) charge) / ((float) this.energybuffer));
        int level = Math.min(Math.max((int) (5.0f * per), 0), 4);
        ItemStack template = DISPLAY_CHARGE_LEVEL[level];
        return Pair.of(AddUtils.addLore(template, "", "&8⇨ &e⚡ &7%dJ/%dJ".formatted(charge, this.energybuffer)), level);
    }

    public SmithInterfaceCharger(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption,
            int remoteSpeed,
            double multiplier) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption, false);
        SPEED_MULTIPLY = multiplier;
        this.remoteSpeed = remoteSpeed;
    }

    public void processInterface(Block b, BlockMenu inv, SlimefunBlockData data, Location coreLocation, int speed) {
        // implement logic here
        int charge = getCharge(inv.getLocation(), data);
        for (int slot : INPUT_SLOT) {
            ItemStack stack = inv.getItemInSlot(slot);
            charge = processCharge(stack, charge, energyConsumption, speed, false);
        }
        // process player charge
        if (inv.hasViewer()) {
            updateMenu(inv, b, Settings.RUN);
            for (var pl : inv.toInventory().getViewers()) {
                if (pl instanceof Player player) {
                    processCharge(player, charge, energyConsumption, speed, false);
                }
            }
        }
        setCharge(inv.getLocation(), charge);
        Object uuidList = getDataHolder(inv).getObject(0);

        if (uuidList instanceof Set<?>) {
            Set<UUID> uidLists = (Set<UUID>) uuidList;
            if (!uidLists.isEmpty()) {
                if (DataCache.getSfItem(coreLocation) instanceof SmithingWorkshop workshop) {
                    int rangeExpandingLevel =
                            workshop.getAmplifyCompentLevel(coreLocation, AddSlimefunItems.SWAMP_RANGE);
                    Predicate<Player> playerRangePredicate =
                            switch (rangeExpandingLevel) {
                                case 0 -> (player -> player.getWorld() == coreLocation.getWorld()
                                        && player.getLocation().distanceSquared(coreLocation) < LV0_LEN_SQ);
                                case 1 -> (player -> player.getWorld() == coreLocation.getWorld()
                                        && player.getLocation().distanceSquared(coreLocation) < LV1_LEN_SQ);
                                case 2 -> (player -> player.getWorld() == coreLocation.getWorld());
                                default -> player -> true;
                            };
                    boolean lazyMode = getStatus(inv)[0];
                    Schedules.execute(
                            () -> {
                                int energyCharge = getCharge(inv.getLocation(), data);
                                for (var uid : uidLists) {
                                    OfflinePlayer player = Bukkit.getOfflinePlayer(uid);
                                    if (player.isOnline()
                                            && player instanceof Player online
                                            && playerRangePredicate.test(online)) {
                                        energyCharge =
                                                processCharge(online, energyCharge, remoteSpeed, speed, lazyMode);
                                    }
                                }
                                setCharge(inv.getLocation(), energyCharge);
                            },
                            true);
                }
            }
        }
    }

    public int processCharge(Player pl, int energyCharge, int base, int speed, boolean lazyMode) {
        if (pl.isValid() && pl.isOnline()) {
            PlayerInventory inventory = pl.getInventory();
            for (var it : inventory.getArmorContents()) {
                energyCharge = processCharge(it, energyCharge, base, speed, lazyMode);
            }
            energyCharge = processCharge(inventory.getItemInMainHand(), energyCharge, base, speed, lazyMode);
            energyCharge = processCharge(inventory.getItemInOffHand(), energyCharge, base, speed, lazyMode);
        }
        return energyCharge;
    }

    public int processCharge(ItemStack item, int energyCharge, int base, int speed, boolean lazyMode) {
        if (energyCharge <= 0) {
            return 0;
        }
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return energyCharge;
        }
        int tobeCharge = Math.min(
                (int) MathUtils.clamp(base * Math.pow(SPEED_MULTIPLY, speed), 0.0D, Integer.MAX_VALUE), energyCharge);

        float left = LogiTechChargable.changeChargeSafe(
                item, tobeCharge, lazyMode ? ((meta, maxCharg, charge, deltaCharge) -> charge < maxCharg / 2) : null);
        return Math.max(0, energyCharge - tobeCharge + (int) left);
    }

    public void addInfo(ItemStack stack) {
        stack.setItemMeta(AddUtils.addLore(
                        stack,
                        LoreBuilder.powerBuffer(energybuffer),
                        "&8⇨ &e⚡ &7" + energyConsumption + " J/次 基礎充電速率",
                        "&8⇨ &e⚡ &7" + remoteSpeed + " J/次 遠程充電速率",
                        "&8⇨ &e⚡ &7" + SPEED_MULTIPLY + " 速度增幅量")
                .getItemMeta());
    }

    @Override
    public void constructMenu(BlockMenuPreset preset) {
        for (int i : CHARGER_BORDER) {
            preset.addItem(i, CHARGER_DISPLAY, ChestMenuUtils.getEmptyClickHandler());
        }
        for (int i : REMOTE_BORDER) {
            preset.addItem(i, REMOTE_DISPLAY, ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(DISPLAY_SLOT, FUNCTION_DISPLAY, ChestMenuUtils.getEmptyClickHandler());
        for (int i : DISPLAY_CHARGE_SLOT) {
            preset.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    @Override
    public void newMenuInstance(@NotNull BlockMenu blockMenu, @NotNull Block block) {
        toggleStatus(blockMenu, getStatus(blockMenu));
        //        blockMenu.addMenuClickHandler(TOGGLE_WHITELIST_SLOT,((player, i, itemStack, clickAction) -> {
        //            var re = getStatus(blockMenu);
        //            toggleStatus(blockMenu,new boolean[]{!re[0],re[1]});
        //            return false;
        //        }));
        blockMenu.addMenuClickHandler(TOGGLE_LAZYMOD, ((player, i, itemStack, clickAction) -> {
            var re = getStatus(blockMenu);
            toggleStatus(blockMenu, new boolean[] {!re[0]});
            return false;
        }));
        updateMenu(blockMenu, block, Settings.INIT);
    }

    @Override
    public void updateMenu(BlockMenu blockMenu, Block block, Settings mod) {
        int charge = getCharge(blockMenu.getLocation());
        var result = getChargeDisplay(charge);
        var item = result.getA();
        // be in 0~4
        var val = result.getB();
        for (int i = 0; i <= val; ++i) {
            blockMenu.replaceExistingItem(DISPLAY_CHARGE_SLOT[i], item);
        }
        for (int i = val + 1; i < DISPLAY_CHARGE_SLOT.length; ++i) {
            blockMenu.replaceExistingItem(DISPLAY_CHARGE_SLOT[i], ChestMenuUtils.getBackground());
        }
        var dh = getDataHolder(blockMenu);
        Set<UUID> Uids = Arrays.stream(IDCARD_SLOT)
                .mapToObj(blockMenu::getItemInSlot)
                .filter(Objects::nonNull)
                .map(itemThis -> {
                    if (SlimefunItem.getByItem(itemThis) instanceof PlayerIdCard card) {
                        return card.getUid(itemThis);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        dh.setObject(0, Uids);
    }

    public DataMenuClickHandler getDataHolder(BlockMenu inv) {
        if (inv.getMenuClickHandler(0) instanceof DataMenuClickHandler dh) {
            return dh;
        } else {
            var result = new DataMenuClickHandler() {
                Object uuidList;

                @Override
                public void setObject(int val, Object val2) {
                    uuidList = val2;
                }

                @Override
                public Object getObject(int val) {
                    return uuidList;
                }

                @Override
                public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                    return false;
                }
            };
            inv.addMenuClickHandler(0, result);
            return result;
        }
    }

    @Override
    public boolean[] getStatus(BlockMenu inv) {
        ItemStack item = inv.getItemInSlot(TOGGLE_LAZYMOD);
        boolean status2 = item != null && item.getType() == Material.GREEN_STAINED_GLASS_PANE;
        return new boolean[] {status2};
    }

    @Override
    public void toggleStatus(BlockMenu inv, boolean... result) {
        if (result.length > 0)
            inv.replaceExistingItem(TOGGLE_LAZYMOD, result[0] ? TOGGLE_LAZYMOD_ON : TOGGLE_LAZYMOD_OFF);
    }
}
