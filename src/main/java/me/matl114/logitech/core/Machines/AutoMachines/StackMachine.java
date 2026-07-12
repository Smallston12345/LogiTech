package me.matl114.logitech.core.Machines.AutoMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import me.matl114.logitech.core.Interface.MultiCraftType;
import me.matl114.logitech.core.Machines.Abstracts.AbstractAdvancedProcessor;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusher;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusherProvider;
import me.matl114.logitech.utils.UtilClass.MenuClass.DataMenuClickHandler;
import me.matl114.logitech.utils.UtilClass.MenuClass.MenuFactory;
import me.matl114.logitech.utils.UtilClass.RecipeClass.ImportRecipes;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class StackMachine extends AbstractAdvancedProcessor implements MultiCraftType, ImportRecipes {
    protected final int[] BORDER = {12, 14, 21, 23, 30, 31, 32, 39, 41, 48, 49, 50};
    protected final int[] INPUT_SLOT = {0, 1, 2, 9, 10, 11, 18, 19, 20, 27, 28, 29, 36, 37, 38, 45, 46, 47};
    protected final int[] OUTPUT_SLOT = {6, 7, 8, 15, 16, 17, 24, 25, 26, 33, 34, 35, 42, 43, 44, 51, 52, 53};

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    protected static final List<SlimefunItem> BW_LIST = new ArrayList<>();
    protected static int[] BW_LIST_ENERGYCOMSUME;
    protected static int BWSIZE;
    protected final int MACHINE_SLOT = 13;
    protected final int MINFO_SLOT = 40;
    protected final int INFO_SLOT = 4;
    protected final ItemStack INFO_ITEM = new CustomItemStack(
            Material.ORANGE_STAINED_GLASS_PANE,
            "&b機制",
            "&6將要模擬的機器放在下方槽位",
            "&6機器會進行模擬,其中",
            "&7<並行處理數>=<機器數*工作效率>",
            "&7<耗電數>=min(<機器數量*單個機器耗電/工作效率>,<最大電容量>)",
            "&6有關高級機器和並行處理數的信息,請見黏液書<版本與説明>",
            "&6支持的機器可以在黏液書<通用機器類型大全>",
            "&6或者左邊按鈕查看",
            "&6機器支持的配方可以點擊右側按鈕查看");
    protected final ItemStack MINFO_ITEM_OFF = new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c機器信息", "&7待機中");
    protected final int MACHINEMENU_SLOT = 3;
    protected final int RECIPEMENU_SLOT = 5;
    protected final ItemStack MACHINEMENU_ICON =
            new CustomItemStack(Material.BLAST_FURNACE, "&b該機器支持的機器列表", "&6點擊打開菜單");
    protected final ItemStack RECIPEMENU_ICON =
            new CustomItemStack(Material.KNOWLEDGE_BOOK, "&b當前模擬的機器配方列表", "&6點擊打開菜單");

    protected ItemStack getInfoItem(int craftlimit, int energyCost, int charge, double efficiency, String name) {
        return new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&a機器信息",
                AddUtils.concat("&7當前模擬的機器名稱: ", (name)),
                "&7當前並行處理數: %-3d".formatted(craftlimit),
                "&7當前每刻耗電量: %sJ/t".formatted(AddUtils.formatDouble(energyCost)),
                "&7當前電量: %sJ".formatted(AddUtils.formatDouble(charge)),
                AddUtils.concat("&7當前工作效率: ", AddUtils.getPercentFormat(efficiency)));
    }

    protected ItemStack getInfoOffItem(int craftlimit, int energyCost, int charge, String name) {
        return new CustomItemStack(
                Material.RED_STAINED_GLASS_PANE,
                "&c機器信息",
                "&c缺少電力!",
                AddUtils.concat("&7當前模擬的機器名稱: ", (name)),
                "&7當前並行處理數: %-3d".formatted(craftlimit),
                "&7當前每刻耗電量: %sJ/t".formatted(AddUtils.formatDouble(energyCost)),
                "&7當前電量: %sJ".formatted(AddUtils.formatDouble(charge)));
    }

    protected Double efficiency;
    protected static MenuFactory MACHINE_LIST_MENU;

    static {
        PostSetupTasks.addPostRegisterTask(() -> {
            getMachineList();
            MACHINE_LIST_MENU =
                    MenuUtils.createMachineListDisplay(getMachineList(), null).setBack(1);
        });
    }

    protected ItemPusherProvider MACHINE_PROVIDER = CraftUtils.getpusher;

    public StackMachine(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            Material progressItem,
            int energyConsumption,
            int energyBuffer,
            double efficiency) {
        super(category, item, recipeType, recipe, progressItem, energyConsumption, energyBuffer, null);
        AddUtils.addGlow(getProgressBar());
        this.efficiency = efficiency;
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c堆疊",
                        "&7在界面的機器槽中放入機器",
                        "&7堆疊機器會載入該機器的配方列表並進行進程",
                        "&7機器槽中的機器數目決定並行處理數",
                        "&7堆疊機器並行處理數=<機器數>*<工作效率>",
                        "&7堆疊機器耗電量=<機器數>*<機器耗電量>"),
                null));
    }

    public void addInfo(ItemStack stack) {
        stack.setItemMeta(AddUtils.capacitorInfoAdd(stack, energybuffer).getItemMeta());
    }

    public static boolean hasInit = false;

    public static List<SlimefunItem> getMachineList() {
        if (!hasInit)
            synchronized (BW_LIST) {
                if (BW_LIST.isEmpty()) {
                    RecipeSupporter.init();
                    BWSIZE = RecipeSupporter.STACKMACHINE_LIST.size();
                    BW_LIST_ENERGYCOMSUME = new int[BWSIZE];
                    int i = 0;
                    for (Map.Entry<SlimefunItem, Integer> e : RecipeSupporter.STACKMACHINE_LIST.entrySet()) {
                        BW_LIST.add(e.getKey());
                        BW_LIST_ENERGYCOMSUME[i] = e.getValue();
                        ++i;
                    }
                    hasInit = true;
                }
            }
        return BW_LIST;
    }

    public static final int getListSize() {
        if (BW_LIST == null || BWSIZE == 0) {
            BWSIZE = getMachineList().size();
        }
        return BWSIZE;
    }

    public static int getEnergy(int index) {
        if (BW_LIST_ENERGYCOMSUME == null || BW_LIST_ENERGYCOMSUME.length == 0) {
            getMachineList();
        }
        return BW_LIST_ENERGYCOMSUME[index];
    }

    public ItemStack getProgressBar() {
        return progressbar;
    }

    public MachineRecipe getRecordRecipe(SlimefunBlockData data) {
        List<MachineRecipe> lst = getMachineRecipes(data);
        int size = lst.size();
        if (size > 0) {
            int index = DataCache.getLastRecipe(data);
            if (index >= 0 && index < size) {
                return lst.get(index);
            }
        }
        return null;
    }

    public List<MachineRecipe> getMachineRecipes(Block b, BlockMenu inv) {
        return getMachineRecipes(DataCache.safeLoadBlock(inv.getLocation()));
    }

    public List<MachineRecipe> getMachineRecipes(SlimefunBlockData data) {
        int index = MultiCraftType.getRecipeTypeIndex(data);
        if (index >= 0 && index < getListSize()) {
            List<MachineRecipe> lst =
                    RecipeSupporter.MACHINE_RECIPELIST.get(getMachineList().get(index));
            return lst != null ? lst : new ArrayList<>();
        }
        return new ArrayList<>();
    }
    // 該方法一般只有updateMenu的時候調用
    // 有沒有可能就是
    // 老子直接不要了塞updateMenu裏
    public void anaylsisMachine(BlockMenu inv) {
        // 首先 檢測機器
        // 其次 核驗下標 使用safeset 需要重置歷史嗎 隨便 但是你禁用更好
        // 如果禁用

    }

    public void constructMenu(BlockMenuPreset preset) {
        // 空白背景 禁止點擊
        int[] border = BORDER;
        int len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {
            preset.addItem(border[var4], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(MACHINEMENU_SLOT, MACHINEMENU_ICON);
        preset.addItem(INFO_SLOT, INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(RECIPEMENU_SLOT, RECIPEMENU_ICON);
        preset.addItem(MINFO_SLOT, MINFO_ITEM_OFF, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL, ChestMenuUtils.getEmptyClickHandler());
    }

    public void newMenuInstance(BlockMenu inv, Block block) {
        inv.addMenuOpeningHandler((player -> {
            updateMenu(inv, block, Settings.RUN);
        }));
        inv.addMenuCloseHandler((player -> {
            updateMenu(inv, block, Settings.RUN);
        }));
        inv.addMenuClickHandler(RECIPEMENU_SLOT, ((player, i, itemStack, clickAction) -> {
            int index = MultiCraftType.getRecipeTypeIndex(inv.getLocation());
            if (index >= 0 && index < getListSize()) {
                SlimefunItem item = getMachineList().get(index);

                MenuUtils.createMRecipeListDisplay(item.getItem(), RecipeSupporter.MACHINE_RECIPELIST.get(item), null)
                        .build()
                        .setBackHandler((player1, i1, itemStack1, clickAction1) -> {
                            inv.open(player1);
                            return false;
                        })
                        .open(player);
            } else {
                player.sendMessage(ChatColors.color("&e您所放置的機器為空或者為不支持的機器"));
                // player.sendMessage();
            }
            return false;
        }));
        inv.addMenuClickHandler(MACHINEMENU_SLOT, ((player, i, itemStack, clickAction) -> {
            MACHINE_LIST_MENU
                    .build()
                    .setBackHandler((player1, i1, itemStack1, clickAction1) -> {
                        inv.open(player1);
                        return false;
                    })
                    .open(player);
            return false;
        }));

        updateMenu(inv, block, Settings.INIT);
    }

    public DataMenuClickHandler createDataHolder() {
        return new DataMenuClickHandler() {
            // 0 為 數量 1 為 電力
            int[] intdata = new int[2];

            public int getInt(int i) {
                return intdata[i];
            }

            public void setInt(int i, int val) {
                intdata[i] = val;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    public final int DATA_SLOT = 4;

    public DataMenuClickHandler getDataHolder(Block b, BlockMenu inv) {
        ChestMenu.MenuClickHandler handler = inv.getMenuClickHandler(DATA_SLOT);
        if (handler instanceof DataMenuClickHandler dh) {
            return dh;
        } else {
            DataMenuClickHandler dh = createDataHolder();
            inv.addMenuClickHandler(DATA_SLOT, dh);
            Schedules.launchSchedules(
                    () -> {
                        updateMenu(inv, b, Settings.INIT);
                    },
                    20,
                    false,
                    0);
            return dh;
        }
    }

    public final int getCraftLimit(Block b, BlockMenu inv) {
        return MathUtils.clamp((int) (this.efficiency * getDataHolder(b, inv).getInt(0)), 0, Integer.MAX_VALUE);
    }

    public void progressorCost(Block b, BlockMenu inv) {
        DataMenuClickHandler dh = getDataHolder(b, inv);
        long charge = dh.getInt(1);
        long machineCnt = dh.getInt(0);
        int consumption = (int) Math.min(charge * machineCnt, this.energybuffer);
        this.removeCharge(inv.getLocation(), consumption);
    }

    public void updateMenu(BlockMenu inv, Block block, Settings mod) {
        SlimefunBlockData data = DataCache.safeGetBlockCacheWithLoad(inv.getLocation());
        if (data == null) {
            Debug.logger("SF DATA LOST AT %s! PLEASE REPORT THIS LOG TO THE AUTHOR"
                    .formatted(DataCache.locationToString(inv.getLocation())));
            return;
        } else if (!data.isDataLoaded()) {
            Debug.logger("SF DATA NOT LOAD YET IN %s! PLEASE REPORT THIS LOG TO THE AUTHOR"
                    .formatted(DataCache.locationToString(inv.getLocation())));
            Schedules.launchSchedules(
                    () -> {
                        updateMenu(inv, block, mod);
                    },
                    20,
                    false,
                    0);
            return;
        }
        if (mod == Settings.INIT) {
            // new CraftItemStack instance to trigger slimefun save action
            ItemStack rawStack = inv.getItemInSlot(this.MACHINE_SLOT);
            if (rawStack != null) {
                inv.replaceExistingItem(this.MACHINE_SLOT, rawStack);
            }
        }
        ItemPusher it = this.MACHINE_PROVIDER.getPusher(Settings.INPUT, inv, this.MACHINE_SLOT);
        int index = MultiCraftType.getRecipeTypeIndex(data);
        DataMenuClickHandler db = this.getDataHolder(block, inv);
        if (it != null) {
            SlimefunItem sfitem = SlimefunItem.getByItem(it.getItem());
            if (sfitem != null) {
                SlimefunItem historyMachine;
                if (index >= 0 && index < getListSize()) {
                    historyMachine = getMachineList().get(index);
                } else {
                    historyMachine = null;
                }
                if (sfitem == historyMachine) {
                    // 和歷史機器一樣
                    // 設置當前數量
                    int lastCount = db.getInt(0);
                    if (lastCount != it.getAmount()) {
                        // 切換機器數目 想逃？
                        db.setInt(0, it.getAmount());
                        this.processor.endOperation(inv.getLocation());
                        inv.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL);
                    }
                    int charge = getEnergy(index);
                    db.setInt(1, charge == -1 ? energyConsumption : charge);
                    return;
                } else {
                    int size = getListSize();
                    List<SlimefunItem> lst = getMachineList();
                    String thisId = sfitem.getId();
                    for (int i = 0; i < size; ++i) {
                        SlimefunItem item = lst.get(i);
                        if (item == null) {
                            continue;
                        }
                        if (sfitem == item || thisId.equals(item.getId())) {
                            // 是該機器,設置下標，都不用查了 肯定不一樣
                            MultiCraftType.forceSetRecipeTypeIndex(data, i);
                            int charge = getEnergy(i);
                            //  DataCache.setCustomData(data,"mae",charge==-1?energyConsumption:charge);
                            db.setInt(0, it.getAmount());
                            db.setInt(1, charge == -1 ? energyConsumption : charge);
                            // 切換機器數目 想逃？
                            this.processor.endOperation(inv.getLocation());
                            inv.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL);
                            return;
                        }
                    }
                } // 找不到,給你機會你不重用啊
            }
        }
        if (index != -1) // 即將變成-1,不一樣才變,不用重複查詢
        MultiCraftType.forceSetRecipeTypeIndex(data, -1);
        db.setInt(0, 0);
        db.setInt(1, 0);
        // 拿走機器,想逃?
        this.processor.endOperation(inv.getLocation());
        inv.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL);
    }

    public void tick(Block b, @Nullable BlockMenu inv, SlimefunBlockData data, int tickCount) {
        // 首先 加載
        if (inv.hasViewer()) {
            updateMenu(inv, b, Settings.RUN);
        } else {
            //            CompletableFuture.runAsync(()->{
            //                if(!inv.hasViewer()){
            //                    MenuUtils.syncSlot(inv,MACHINE_SLOT);
            //                }
            //            });
        }
        int index = MultiCraftType.getRecipeTypeIndex(data);
        if (index >= 0 && index < getListSize()) { // 有效機器
            DataMenuClickHandler db = this.getDataHolder(b, inv);
            long charge = (long) db.getInt(1);
            long craftLimit = db.getInt(0);
            // fix multiply error int overflow
            int consumption = (int) Math.min(craftLimit * charge, (long) this.energybuffer);
            int energy = this.getCharge(inv.getLocation(), data);
            if (energy >= consumption) {
                if (inv.hasViewer()) {
                    inv.replaceExistingItem(
                            MINFO_SLOT,
                            getInfoItem(
                                    (int) (craftLimit * efficiency),
                                    consumption,
                                    energy,
                                    this.efficiency,
                                    CraftUtils.getItemName(this.MACHINE_PROVIDER
                                            .getPusher(Settings.INPUT, inv, this.MACHINE_SLOT)
                                            .getItem())));
                }
                process(b, inv, data);
            } else {
                // 沒電
                if (inv.hasViewer()) {
                    inv.replaceExistingItem(
                            MINFO_SLOT,
                            getInfoOffItem(
                                    (int) (craftLimit * efficiency),
                                    consumption,
                                    energy,
                                    CraftUtils.getItemName(this.MACHINE_PROVIDER
                                            .getPusher(Settings.INPUT, inv, this.MACHINE_SLOT)
                                            .getItem())));
                }
            }

        } else {
            if (inv.hasViewer()) {
                inv.replaceExistingItem(MINFO_SLOT, MINFO_ITEM_OFF);
            }
        }
    }

    public void onBreak(BlockBreakEvent e, BlockMenu inv) {
        if (inv != null) {
            Location loc = inv.getLocation();
            inv.dropItems(loc, MACHINE_SLOT);
        }
        super.onBreak(e, inv);
    }
}
