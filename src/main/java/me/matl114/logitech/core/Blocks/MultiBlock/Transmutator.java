package me.matl114.logitech.core.Blocks.MultiBlock;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.manager.RadiationRegionManager;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.FunctionalClass.OutputStream;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemCounter;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemGreedyConsumer;
import me.matl114.logitech.utils.UtilClass.MultiBlockClass.*;
import me.matl114.logitech.utils.UtilClass.MultiBlockClass.CubeMultiBlock.CubeMultiBlock;
import me.matl114.logitech.utils.UtilClass.RecipeClass.MultiCraftingOperation;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class Transmutator extends MultiBlockAdvancedProcessor {
    protected final int[] BORDER = new int[] {1, 2, 3, 5, 6, 7, 13, 40, 49};
    protected final int TOGGLE_SLOT = 4;
    protected final int HOLOGRAM_SLOT = 8;
    protected final ItemStack[] TOGGLE_ITEM = new ItemStack[] {
        new CustomItemStack(
                        Material.RED_STAINED_GLASS_PANE,
                        "&6點擊切換嬗變機狀態",
                        "&6shift點擊切換是否自動構建",
                        "&7當前狀態: &c關閉",
                        "&7自動構建: &c關",
                        "&7警告:你最好別在進程中破壞機器"),
                new CustomItemStack(
                        Material.RED_STAINED_GLASS_PANE,
                        "&6點擊切換嬗變機狀態",
                        "&6shift點擊切換是否自動構建",
                        "&7當前狀態: &c關閉",
                        "&7自動構建: &a開",
                        "&7警告:你最好別在進程中破壞機器"),
        new CustomItemStack(
                        Material.GREEN_STAINED_GLASS_PANE,
                        "&6點擊切換嬗變機狀態",
                        "&6shift點擊切換是否自動構建",
                        "&7當前狀態: &a開啓",
                        "&7自動構建: &c關",
                        "&7警告:你最好別在進程中破壞機器"),
                new CustomItemStack(
                        Material.GREEN_STAINED_GLASS_PANE,
                        "&6點擊切換嬗變機狀態",
                        "&6shift點擊切換是否自動構建",
                        "&7當前狀態: &a開啓",
                        "&7自動構建: &a開",
                        "&7警告:你最好別在進程中破壞機器")
    };
    /**
     * 保證冷卻劑供應即可
     * 不得中途破壞
     */
    protected final ItemStack[] HOLOGRAM_ITEM_ON = new ItemStack[] {
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &a北向"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &a東向"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &a南向"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &a西向")
    };

    protected final ItemStack HOLOGRAM_ITEM_OFF = new CustomItemStack(
            SlimefunItems.HOLOGRAM_PROJECTOR, "&6點擊切換全息投影", "&e或使用/logitech multiblock 查看搭建教程", "&7當前狀態: &c關閉");
    protected final ItemStack COOLER_INFO_ITEM = new CustomItemStack(
            SlimefunItems.NETHER_ICE_COOLANT_CELL,
            "&b冷卻劑輸入槽",
            "&7請保證冷卻劑供應",
            "&e上方填入冷卻劑",
            "&e下方填入下界冰冷卻劑",
            "&7冷卻劑槽位會限制貨運只傳入冷卻劑");
    protected final int COOLER_INFO_SLOT = 27;
    protected final int HALT_SLOT = 0;
    protected final ItemStack HALT_ITEM = new CustomItemStack(Material.BARRIER, "&c點擊安全終止進程", "&6不會引發熔燬");
    protected final int INFO_SLOT = 22;
    protected final ItemStack INFO_ITEM = new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&a機器狀態信息", "");
    protected final int[] INPUT_SLOT = new int[] {20, 29, 38};
    protected final int[] COOLER_INPUT_SLOT = new int[] {18};
    protected final int[] NETHERICE_INPUT_SLOT = new int[] {36};
    protected final int[] COMMON_INPUT_SLOT = new int[] {20, 29, 38};
    protected final int[] OUTPUT_SLOT = new int[] {23, 24, 25, 32, 33, 34, 41, 42, 43};
    protected final int[] ALL_INPUT_SLOT = new int[] {18, 36, 20, 29, 38};

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    protected final MachineProcessor<MultiCraftingOperation> coolerProcessor;

    protected final int[] BORDER_IN = new int[] {9, 10, 11, 12, 19, 28, 37, 45, 46, 47, 48, 21, 30, 39};
    protected final int[] BORDER_OUT = new int[] {14, 15, 16, 17, 26, 35, 44, 50, 51, 52, 53};

    public Transmutator(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            String blockId,
            AbstractMultiBlockType type,
            int energyConsumption,
            int energyBuffer,
            List<Pair<Object, Integer>> customRecipes) {
        super(
                itemGroup,
                item,
                recipeType,
                recipe,
                blockId,
                type,
                AddUtils.addGlow(new ItemStack(Material.FIRE_CHARGE)),
                energyConsumption,
                energyBuffer,
                customRecipes);

        this.PROCESSOR_SLOT = 31;
        this.coolerProcessor = new MachineProcessor<MultiCraftingOperation>(this);
        this.coolerProcessor.setProgressBar(COOLER_INFO_ITEM);
    }

    public void onMultiBlockDisable(
            Location loc, AbstractMultiBlockHandler handler, MultiBlockService.DeleteCause cause) {
        MultiCraftingOperation op = Transmutator.this.processor.getOperation(loc);
        if (op != null && (!op.isFinished())) { // 如果還在進行中就暫停
            onDestroyEffect(loc, handler, cause);
        } else {
            removeEffect(loc, handler);
        }
        DataCache.setCustomData(loc, this.HEIGHT_KEY, 0);

        super.onMultiBlockDisable(loc, handler, cause);
    }

    public void onMultiBlockEnable(Location loc, AbstractMultiBlockHandler handler) {
        super.onMultiBlockEnable(loc, handler);
        createEffect(loc, handler);
    }
    // check if lava
    protected int[] lavadx = new int[] {2, 2, 2, 3, 3, 4, 4, 4, 5, 5, 6, 6, 6};
    protected int[] lavady = new int[] {-2, 0, 2, -1, 1, -2, 0, 2, -1, 1, -2, 0, 2};
    protected int baseHeight = 2;
    protected final int RADIATION_PERIOD = 1000;
    protected final int RADIATION_RANGE = 40;
    protected final int RADIATION_SPEED = 80;

    public void createEffect(Location loc, AbstractMultiBlockHandler handler) {
        MultiBlockService.Direction dir = handler.getDirection();
        AbstractMultiBlock block = handler.getMultiBlock();
        if (block instanceof CubeMultiBlock cb) {
            List<Location> fillWithLava = new ArrayList<>();
            int height = baseHeight + cb.getHeight();
            int len = lavadx.length;
            Location locer;
            for (int y = 0; y < height; y++) {
                for (int j = 0; j < len; j++) {
                    locer = loc.clone()
                            .add(dir.getRotateX(lavadx[j], lavady[j]), y, dir.getRotateZ(lavadx[j], lavady[j]));
                    fillWithLava.add(locer);
                    WorldUtils.removeSlimefunBlock(locer, true);
                }
            }
            Schedules.launchSchedules(
                    () -> {
                        int size = fillWithLava.size();
                        for (int i = 0; i < size; i++) {
                            fillWithLava.get(i).getBlock().setType(Material.LAVA);
                        }
                    },
                    0,
                    true,
                    0);
        }
    }

    public void removeEffect(Location loc, AbstractMultiBlockHandler handler) {
        MultiBlockService.Direction dir = handler.getDirection();
        AbstractMultiBlock block = handler.getMultiBlock();
        if (block instanceof CubeMultiBlock cb) {
            List<Location> fillWithLava = new ArrayList<>();
            int height = baseHeight + cb.getHeight();
            int len = lavadx.length;
            Location locer;
            for (int y = 0; y < height; y++) {
                for (int j = 0; j < len; j++) {
                    locer = loc.clone()
                            .add(dir.getRotateX(lavadx[j], lavady[j]), y, dir.getRotateZ(lavadx[j], lavady[j]));
                    fillWithLava.add(locer);
                    WorldUtils.removeSlimefunBlock(locer, true);
                }
            }
            Schedules.launchSchedules(
                    () -> {
                        int size = fillWithLava.size();
                        for (int i = 0; i < size; i++) {
                            fillWithLava.get(i).getBlock().setType(Material.AIR);
                        }
                    },
                    0,
                    true,
                    0);
        }
    }

    public void onDestroyEffect(Location loc, AbstractMultiBlockHandler handler, MultiBlockService.DeleteCause cause) {
        if (broadCastWhenDump.getValue()) {
            AddUtils.broadCast("&e位於[%s,%.0f,%.0f,%.0f]的元素嬗變機因 [%s] 熔燬!"
                    .formatted(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), cause.getMessage()));
        } else {
            AddUtils.broadCast("&e位於[&k看你嗎呢,0.0,0.0,0.0&f&e]的元素嬗變機因 [%s] 熔燬!".formatted(cause.getRawCause()));
        }

        MultiBlockService.Direction dir = handler.getDirection();
        AbstractMultiBlock block = handler.getMultiBlock();
        if (block instanceof CubeMultiBlock cb) {
            int height = baseHeight + cb.getHeight();
            Location locer;
            for (int y = 0; y < height; y++) {
                for (int j = 2; j < 7; j++) {
                    for (int k = -2; k < 3; k++) {
                        locer = loc.clone().add(dir.getRotateX(j, k), y, dir.getRotateZ(j, k));
                        WorldUtils.removeSlimefunBlock(locer, true);
                    }
                }
            }
            List<Location> fillWithLava = new ArrayList<>();
            int len = lavadx.length;
            for (int y = 0; y < height; y++) {
                for (int j = 0; j < len; j++) {
                    locer = loc.clone()
                            .add(dir.getRotateX(lavadx[j], lavady[j]), y, dir.getRotateZ(lavadx[j], lavady[j]));
                    fillWithLava.add(locer);
                }
            }
            Schedules.launchSchedules(
                    () -> {
                        int size = fillWithLava.size();
                        for (int i = 0; i < size; i++) {
                            fillWithLava.get(i).getBlock().setType(Material.CRYING_OBSIDIAN);
                        }
                    },
                    0,
                    true,
                    0);
            Schedules.launchSchedules(
                    () -> {
                        RadiationRegionManager.runRadiation(loc, RADIATION_RANGE, 2);
                    },
                    0,
                    true,
                    0);
            RadiationRegionManager.addRadiation(loc, RADIATION_RANGE, RADIATION_PERIOD, RADIATION_SPEED, 1);
        }
    }

    public HashMap<String, ItemStack> MBID_TO_ITEM = new HashMap<>() {
        {
            put("nuclear.frame", AddUtils.addGlow(AddItem.TRANSMUTATOR_FRAME.clone()));
            put("nuclear.glass", AddUtils.addGlow(AddItem.TRANSMUTATOR_GLASS.clone()));
            put("nuclear.rod", AddUtils.addGlow(AddItem.TRANSMUTATOR_ROD.clone()));
        }
    };

    public Map<String, ItemStack> getIdMappingDisplayUse() {
        return Map.copyOf(MBID_TO_ITEM);
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {

            preset.addItem(border[var4], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        // 輸入槽邊框
        border = BORDER_IN;
        len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {

            preset.addItem(border[var4], ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        // 輸出槽邊框
        border = BORDER_OUT;
        len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {
            preset.addItem(border[var4], ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        // 空白邊框
        preset.addItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(INFO_SLOT, INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(HALT_SLOT, HALT_ITEM);
        preset.addItem(COOLER_INFO_SLOT, COOLER_INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
    }

    public void updateMenu(BlockMenu inv, Block block, Settings mod) {
        Location loc = block.getLocation();
        int holoStatus = DataCache.getCustomData(loc, MultiBlockService.getHologramKey(), 0);
        if (holoStatus == 0) {
            inv.replaceExistingItem(HOLOGRAM_SLOT, HOLOGRAM_ITEM_OFF);
        } else {
            inv.replaceExistingItem(HOLOGRAM_SLOT, HOLOGRAM_ITEM_ON[holoStatus - 1]);
        }
        int status = MultiBlockService.getStatus(loc) == 0 ? 0 : 1;
        // 負數為
        int autoRec = DataCache.getCustomData(loc, MultiBlockService.getAutoKey(), 0) <= 0 ? 0 : 1;
        inv.replaceExistingItem(TOGGLE_SLOT, TOGGLE_ITEM[status * 2 + autoRec]);
        if (status == 0) {
            inv.replaceExistingItem(INFO_SLOT, INFO_ITEM);
        }
    }

    public void newMenuInstance(BlockMenu inv, Block block) {
        inv.addMenuOpeningHandler((player -> {
            updateMenu(inv, block, Settings.RUN);
        }));
        inv.addMenuClickHandler(HALT_SLOT, ((player, i, itemStack, clickAction) -> {
            this.processor.endOperation(block);
            inv.replaceExistingItem(PROCESSOR_SLOT, MenuUtils.PROCESSOR_NULL);
            return false;
        }));
        inv.addMenuClickHandler(TOGGLE_SLOT, ((player, i, itemStack, clickAction) -> {
            Location loc = inv.getLocation();
            int statusCode = MultiBlockService.getStatus(loc);
            int autoCode = DataCache.getCustomData(loc, MultiBlockService.getAutoKey(), 0) <= 0 ? 0 : 1;
            if (clickAction.isShiftClicked()) {
                DataCache.setCustomData(loc, MultiBlockService.getAutoKey(), 1 - autoCode);
            } else if (autoCode == 0) {
                if (statusCode == 0) {
                    if (this.getCharge(loc) < energyConsumption) {
                        // 沒電,
                        AddUtils.sendMessage(player, "&c電力不足,無法構建多方塊結構!");
                    } else {
                        if (MultiBlockService.createNewHandler(
                                loc, getBuilder(), getMultiBlockType(), OutputStream.getPlayerOut(player))) {
                            AddUtils.sendMessage(player, "&a成功激活多方塊結構!");
                        } else {
                            AddUtils.sendMessage(player, "&c多方塊結構不全或者結構衝突!");
                        }
                    }
                } else {
                    MultiBlockService.deleteMultiBlock(MultiBlockService.safeGetUUID(loc), MultiBlockService.MANUALLY);
                    AddUtils.sendMessage(player, "&a成功關閉多方塊結構!");
                }
            } else {
                AddUtils.sendMessage(player, "&c多方塊核心處於自動構建模式無法進行手動操作");
            }
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        DataCache.setCustomData(inv.getLocation(), MultiBlockService.getHologramKey(), 0);
        inv.addMenuClickHandler(HOLOGRAM_SLOT, ((player, i, itemStack, clickAction) -> {
            Location loc = inv.getLocation();
            int holoStatus = DataCache.getCustomData(inv.getLocation(), MultiBlockService.getHologramKey(), 0);
            int statusCode = MultiBlockService.getStatus(loc);
            MultiBlockService.removeHologramSync(loc);
            if (statusCode == 0) {
                if (holoStatus != 4) {
                    if (holoStatus == 0) AddUtils.sendMessage(player, "&a全息投影已開啓!");
                    else AddUtils.sendMessage(player, "&a全息投影已切換方向!");
                    MultiBlockService.createHologram(
                            loc, MBTYPE, MultiBlockService.Direction.fromInt(holoStatus), MBID_TO_ITEM);
                    DataCache.setCustomData(loc, MultiBlockService.getHologramKey(), holoStatus + 1);
                } else {
                    AddUtils.sendMessage(player, "&a全息投影已關閉!");
                }
            }
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
    }

    protected ItemCounter COOLER = CraftUtils.getConsumer(SlimefunItems.REACTOR_COOLANT_CELL);
    protected ItemCounter NETHERICE = CraftUtils.getConsumer(SlimefunItems.NETHER_ICE_COOLANT_CELL);

    public boolean checkCooler(BlockMenu inv) {
        Location loc = inv.getLocation();
        MultiCraftingOperation op = this.coolerProcessor.getOperation(loc);
        if (op == null) {
            ItemStack it = inv.getItemInSlot(COOLER_INPUT_SLOT[0]);
            if (it == null) {
                return false;
            }
            ItemStack it2 = inv.getItemInSlot(NETHERICE_INPUT_SLOT[0]);
            if (it2 == null) {
                return false;
            }
            if (CraftUtils.matchItemStack(it, COOLER, false) && CraftUtils.matchItemStack(it2, NETHERICE, false)) {
                it.setAmount(it.getAmount() - 1);
                it2.setAmount(it2.getAmount() - 1);
                op = new MultiCraftingOperation(new ItemGreedyConsumer[0], 2);
                coolerProcessor.startOperation(loc, op);
            } else return false;
        } else if (op.isFinished()) {
            if (inv.hasViewer()) {
                inv.replaceExistingItem(COOLER_INFO_SLOT, COOLER_INFO_ITEM);
            }
            this.coolerProcessor.endOperation(loc);
            return true;
        } else {
            op.progress(1);
        }
        if (inv.hasViewer()) this.coolerProcessor.updateProgressBar(inv, COOLER_INFO_SLOT, op);
        return true;
    }

    public void tick(Block b, BlockMenu inv, SlimefunBlockData data, int tickCount) {
        // in this case .blockMenu is null
        int statusCode = MultiBlockService.getStatus(data);
        Location loc = b.getLocation();
        int charge = this.getCharge(loc);
        String code = data.getData(MultiBlockService.getAutoKey());
        int autoCode = code == null ? 0 : Integer.parseInt(code);
        if (statusCode == 0) {
            if (autoCode > 0 && charge > 2 * energyConsumption) { // 自動構建開啓 且有能量
                autoBuild(loc, data, autoCode);
            }
        } else if (MultiBlockService.acceptCoreRequest(b.getLocation(), getBuilder(), getMultiBlockType())) {
            // runtime檢查是否完整,每3tick檢查一次,每次有1/10的概率檢測一個方塊
            runtimeCheck(loc, data, autoCode);
            // 直接開銷電量
            super.progressorCost(b, inv);
            if (charge < energyConsumption) {
                // 避免重連的時候出現問題,重連的時候statusCode為-3到-1,但是如果沒有電 直接寄
                MultiBlockService.deleteMultiBlock(
                        MultiBlockService.safeGetUUID(loc),
                        MultiBlockService.EnergyOutCause.get(charge, energyConsumption));
                return;
            }
            if (inv.hasViewer()) {
                updateMenu(inv, b, Settings.RUN);
                ItemStack it = inv.getItemInSlot(INFO_SLOT);
                if (it != null) {
                    MultiCraftingOperation currentOperation = (MultiCraftingOperation) this.processor.getOperation(b);
                    AddUtils.setLore(
                            it,
                            "&7當前機器狀態: %s".formatted((currentOperation == null) ? "&7待機中" : "&a進程中"),
                            "&7當前電量: %dJ/%dJ".formatted(charge, energybuffer),
                            "&7當前耗電: %dJ/t".formatted(energyConsumption),
                            "&7當前並行處理數: %-3d".formatted(getCraftLimit(b, inv)));
                } else inv.replaceExistingItem(INFO_SLOT, INFO_ITEM);
            }
            super.process(b, inv, data);
        }
    }

    public MultiBlockService.DeleteCause NOCOOLER = new MultiBlockService.DeleteCause("冷卻劑不足", true);

    public void progressorCost(Block b, BlockMenu inv) {
        // 覆蓋父類 讓process中不扣電
        // 轉到我的ticker里扣
        if (!checkCooler(inv)) {
            MultiBlockService.toggleOff(DataCache.safeLoadBlock(inv.getLocation()), NOCOOLER);
        }
    }

    public void onBreak(BlockBreakEvent e, BlockMenu menu) {
        super.onBreak(e, menu);
        if (menu != null) {
            menu.dropItems(menu.getLocation(), COOLER_INPUT_SLOT);
            menu.dropItems(menu.getLocation(), NETHERICE_INPUT_SLOT);
            this.coolerProcessor.endOperation(menu.getLocation());
        }
    }

    public int[] getSlotsAccessedByItemTransportPlus(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) {
            return getOutputSlots();
        }
        if (item == null || item.getType().isAir()) {
            return ALL_INPUT_SLOT;
        }
        if (item.getType() == Material.PLAYER_HEAD) {
            if (CraftUtils.matchItemStack(item, COOLER, false)) {
                return COOLER_INPUT_SLOT;
            } else {
                return NETHERICE_INPUT_SLOT;
            }
        }
        return getInputSlots();
    }

    public void preRegister() {

        super.preRegister();
    }
}
