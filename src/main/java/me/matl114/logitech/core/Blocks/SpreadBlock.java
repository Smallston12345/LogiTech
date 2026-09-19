package me.matl114.logitech.core.Blocks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.listeners.Listeners.SlimefunBlockPlaceLimitListener;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.CraftUtils;
import me.matl114.logitech.utils.DataCache;
import me.matl114.logitech.utils.FeatureConfig;
import me.matl114.logitech.utils.Utils;
import me.matl114.logitech.utils.WorldUtils;
import me.matl114.logitech.utils.UtilClass.TickerClass.Ticking;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * A bounded antimass process.
 *
 * <p>Only the core is a Slimefun ticking block. Observation is represented by bounded internal progress and never
 * reads or changes surrounding blocks, so runtime cost is independent of terrain and radius.
 */
public class SpreadBlock extends AbstractBlock implements Ticking {
    private static final int DATA_VERSION = 2;
    private static final String PLACEMENT_BYPASS_PERMISSION = "logitech.antimass.limit-bypass";
    private static final String KEY_VERSION = "antimass_version";
    private static final String KEY_OWNER = "antimass_owner";
    private static final String KEY_ABSORBED = "antimass_absorbed";
    private static final String KEY_MASS = "antimass_mass";
    private static final String KEY_PENDING = "antimass_pending";
    private static final String KEY_RUNNING = "antimass_running";
    private static final String KEY_PAUSED = "antimass_paused";
    private static final String KEY_RADIUS = "antimass_radius";
    private static final String KEY_MATERIAL_CREDIT = "antimass_material_credit";
    private static final String KEY_AUTO_CONTINUE = "antimass_auto_continue";
    private static final String KEY_BYPASS_PLACEMENT = "antimass_bypass_placement";

    private static final int STATUS_SLOT = 4;
    private static final int SACRIFICE_STATUS_SLOT = 8;
    private static final int INPUT_GUIDE_SLOT = 29;
    private static final int OUTPUT_GUIDE_SLOT = 33;
    private static final int[] SACRIFICE_INPUT_SLOTS = new int[] {0, 1, 2, 3, 5, 6, 7};
    private static final int[] OUTPUT_SLOTS = new int[] {
        9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 26
    };
    private final SlimefunItem result;
    private final Material trailMaterial;
    private final Material resultMaterial;
    private final Map<Location, ProcessState> processes = new ConcurrentHashMap<>();

    private final boolean enabled = FeatureConfig.getBoolean("SpreadBlock.Antimass.enable-spread", true);
    private final int tickRate = Math.max(1, FeatureConfig.getInt("SpreadBlock.Antimass.tick-rate", 5));
    private final int tendrilCount = clamp(FeatureConfig.getInt("SpreadBlock.Antimass.tendril-count", 6), 1, 12);
    private final int observationsPerUpdate =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.base-observation-rate", 12), 1, 32);
    private final int globalObservationsPerUpdate =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.active-observation-budget", 1152), 1, 2048);
    private final int maxRadius = clamp(FeatureConfig.getInt("SpreadBlock.Antimass.max-radius", 24), 1, 128);
    private final int maxTotalRadius =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.maximum-observation-radius", 144), maxRadius, 512);
    private final int observationsPerCycle =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.observation-budget-per-full-step", 216), 1, 1_000_000);
    private final int observationsPerLogicCore =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.logic-core-observation-cost", 8), 1, 4096);
    private final int sacrificePointCost =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.overclock-material-cost", 20), 1, 1_000_000);
    private final int sacrificeObservationBonus =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.overclock-observation-bonus", 24), 1, 512);
    private final int completedResetMaterialCost =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.completed-reset-material-cost", 360), 1, 1_000_000);
    private final Map<String, Integer> sacrificeMaterialValues = loadSacrificeMaterialValues();
    private final int maxPlacedProcesses =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.max-placed-processes", 32), 1, 256);
    private final int maxSharedProcessesPerPlayer =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.max-shared-processes-per-player", 2), 1, 64);
    private final int maxBypassProcessesPerPlayer =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.max-bypass-processes-per-player", 2), 1, 64);
    private final int maxConcurrentProcesses =
            clamp(FeatureConfig.getInt("SpreadBlock.Antimass.max-active-processes", 32), 1, 64);

    private int remainingGlobalSteps;
    private int claimedSharedProductionSlots;
    private final Map<UUID, Integer> claimedSharedProductionSlotsByOwner = new HashMap<>();
    private final Map<UUID, Integer> claimedBypassProductionSlots = new HashMap<>();
    private final Map<UUID, Integer> remainingBypassObservationSteps = new HashMap<>();
    private boolean globalBudgetPrepared;

    public SpreadBlock(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            SlimefunItem result,
            Material spreadMaterial,
            Material finalMaterial) {
        super(itemGroup, item, recipeType, recipe);
        this.result = result;
        // Kept for legacy conversion and for the material of the placed core. Nearby blocks are never changed.
        this.trailMaterial = finalMaterial;
        this.resultMaterial = finalMaterial;
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c反概念觸鬚",
                        "&7核心以%d條虛擬觸鬚累積概念觀測".formatted(tendrilCount),
                        "&7觀測不會讀取、替換或破壞附近方塊",
                        "&7每輪擴張: &e%d格 &7/ 最終半徑: &e%d格".formatted(maxRadius, maxTotalRadius),
                        "&7進度只由核心自身計算，不受周圍地形影響"),
                null,
                null,
                AddUtils.addLore(
                        result.getItem(), "&a每累積%d點觀測可在核心內生成一個".formatted(observationsPerLogicCore)),
                AddUtils.getInfoShow(
                        "&f機制 - &a安全輸出",
                        "&7產物只會進入核心的輸出槽，不會生成地板掉落物",
                        "&7可使用黏液貨運抽取；輸出已滿時觀測會自動暫停",
                        "&7點擊狀態按鈕可暫停或繼續，Shift點擊可安全結算"),
                null,
                AddUtils.getInfoShow(
                        "&f反概念獻祭",
                        "&7所有輪次皆須由玩家親自喚醒核心",
                        "&7投入槽支援黏液貨運；未投入材料時仍會緩慢觀測",
                        "&7運行中每次吞噬&e%d&7點材料，可額外累積&e%d&7點觀測"
                                .formatted(sacrificePointCost, sacrificeObservationBonus),
                        "&7超頻中自然完成本輪時，將自動啟動下一輪",
                        "&7抵達極限後可獻祭&e%d&7點材料原地重啟".formatted(completedResetMaterialCost),
                        "&7停止、暫停或等待下一輪時不會消耗材料"),
                null,
                AddUtils.getInfoShow(
                        "&f獻祭代價 - &5概念權重",
                        materialValueLore("ENERGIZED_CAPACITOR", "充能電容"),
                        materialValueLore("LOGITECH_REINFORCED_CHIP_INGOT", "強化薯片合金"),
                        materialValueLore("LOGITECH_SPACE_PLATE", "空間強化板"),
                        materialValueLore("LOGITECH_PDCECDMD", "鈽鈰鎘鍆合金"),
                        materialValueLore("LOGITECH_HGTLPBBI", "汞鉈鉛鉍合金"),
                        materialValueLore("LOGITECH_VIRTUAL_SPACE", "虛擬空間"),
                        materialValueLore("LOGITECH_FINAL_FRAME", "終極邏輯框架")),
                null,
                AddUtils.getInfoShow(
                        "&f機制 - &a效能限制",
                        "&7全伺服器共用池最多放置&e%d&7個核心".formatted(maxPlacedProcesses),
                        "&7每位玩家最多使用&e%d&7個共用池名額".formatted(maxSharedProcessesPerPlayer),
                        "&7全伺服器同時最多&e%d&7個核心進行觀測".formatted(maxConcurrentProcesses),
                        "&7擁有者離線時停產並釋放運行名額",
                        "&7每次最多累積%d點，全服共享%d點進度預算"
                                .formatted(observationsPerUpdate, globalObservationsPerUpdate),
                        "&7不會掃描區塊，運行成本不受觀測範圍影響"),
                null));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Map<String, Integer> loadSacrificeMaterialValues() {
        String root = "SpreadBlock.Antimass.sacrifice-material-values";
        Map<String, Integer> defaults = Map.of(
                "ENERGIZED_CAPACITOR", 1,
                "LOGITECH_REINFORCED_CHIP_INGOT", 2,
                "LOGITECH_SPACE_PLATE", 4,
                "LOGITECH_PDCECDMD", 8,
                "LOGITECH_HGTLPBBI", 8,
                "LOGITECH_VIRTUAL_SPACE", 16,
                "LOGITECH_FINAL_FRAME", 64);
        Set<String> configuredKeys = FeatureConfig.getKeys(root);
        Map<String, Integer> values = new HashMap<>();
        for (Map.Entry<String, Integer> entry : defaults.entrySet()) {
            String itemId = entry.getKey();
            int defaultValue = entry.getValue();
            int configuredValue = configuredKeys.contains(itemId)
                    ? FeatureConfig.getInt(root + "." + itemId, defaultValue)
                    : defaultValue;
            values.put(itemId, clamp(configuredValue, 0, 1_000_000));
        }
        return Map.copyOf(values);
    }

    private String materialValueLore(String itemId, String name) {
        return "&7%s: &e%d點".formatted(name, sacrificeMaterialValues.getOrDefault(itemId, 0));
    }

    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    public int[] getInputSlots() {
        return SACRIFICE_INPUT_SLOTS;
    }

    @Override
    public void constructMenu(BlockMenuPreset preset) {
        preset.setSize(36);
        for (int slot = 0; slot < 9; slot++) {
            if (isSacrificeInputSlot(slot)) continue;
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot = 27; slot < 36; slot++) {
            preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(STATUS_SLOT, statusItem("&e等待初始化", "&7放置後將建立受限的反概念觀測進程"));
        preset.addItem(SACRIFICE_STATUS_SLOT, sacrificeStatusItem(0), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(
                INPUT_GUIDE_SLOT,
                new CustomItemStack(
                        Material.HOPPER,
                        "&a獻祭材料投入區",
                        "&7最上排除狀態核心與獻祭狀態外，共有七格投入槽",
                        "&7僅接納設定權重的黏液物品",
                        "",
                        "&5支援材料與點數",
                        materialValueLore("ENERGIZED_CAPACITOR", "充能電容"),
                        materialValueLore("LOGITECH_REINFORCED_CHIP_INGOT", "強化薯片合金"),
                        materialValueLore("LOGITECH_SPACE_PLATE", "空間強化板"),
                        materialValueLore("LOGITECH_PDCECDMD", "鈽鈰鎘鍆合金"),
                        materialValueLore("LOGITECH_HGTLPBBI", "汞鉈鉛鉍合金"),
                        materialValueLore("LOGITECH_VIRTUAL_SPACE", "虛擬空間"),
                        materialValueLore("LOGITECH_FINAL_FRAME", "終極邏輯框架"),
                        "",
                        "&a支援黏液貨運自動送入"),
                ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(
                OUTPUT_GUIDE_SLOT,
                new CustomItemStack(
                        Material.CHEST,
                        "&6邏輯核心輸出區",
                        "&7中間兩排為安全輸出槽",
                        "&7所有產物保存在此，不會掉落地面",
                        "&7完整觀測至%d格最多產生: &e%d顆".formatted(maxTotalRadius, getMaximumLogicCoreOutputs()),
                        "&a支援黏液貨運自動抽取"),
                ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void newMenuInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        menu.addMenuOpeningHandler(player -> updateStatus(menu, block.getLocation(), processes.get(block.getLocation())));
        menu.addMenuClickHandler(STATUS_SLOT, (player, slot, item, action) -> {
            ProcessState state = processes.get(block.getLocation());
            if (state == null) {
                AddUtils.sendMessage(player, "&c反概念進程尚未載入");
                return false;
            }
            if (action.isShiftClicked()) {
                finish(state);
                persist(state);
                AddUtils.sendMessage(player, "&a已安全終止並結算反概念進程");
            } else if (state.running) {
                state.paused = !state.paused;
                persist(state);
                AddUtils.sendMessage(player, state.paused ? "&e反概念進程已暫停" : "&a反概念進程已繼續");
            } else if (canRestart(state)) {
                if (!canActivate(state)) {
                    AddUtils.sendMessage(player, "&c目前運行中的反概念進程已達上限，請稍後再啟動下一輪");
                } else if (restartCycle(state)) {
                    persist(state);
                    AddUtils.sendMessage(player, "&a反概念核心已擴大作用範圍並開始下一輪觀測");
                }
            } else if (hasReachedRadiusLimit(state)) {
                resetCompletedProcess(player, state, menu);
            }
            updateStatus(menu, block.getLocation(), state);
            return false;
        });
    }

    private ItemStack statusItem(String name, String... lore) {
        return new CustomItemStack(Material.RESPAWN_ANCHOR, name, lore);
    }

    private ItemStack sacrificeStatusItem(long storedPoints) {
        return new CustomItemStack(
                Material.CRYING_OBSIDIAN,
                "&5反概念獻祭",
                "&7運行中每次消耗: &e%d點".formatted(sacrificePointCost),
                "&7每次額外提供: &e%d點觀測".formatted(sacrificeObservationBonus),
                "&7目前可用: &e%d點".formatted(storedPoints),
                "&7支援材料與點數請查看下方漏斗",
                "&7材料投入: 最上排其餘七格，支援貨運",
                "&a超頻中完成本輪會自動啟動下一輪",
                "&5抵達極限後可消耗%d點原地重啟".formatted(completedResetMaterialCost),
                "&a沒有材料時仍會以基礎速度運行",
                "&c投入槽內的物品將被永久消耗");
    }

    private void updateStatus(@Nullable BlockMenu menu, Location location, @Nullable ProcessState state) {
        if (menu == null) return;
        if (!enabled) {
            menu.replaceExistingItem(STATUS_SLOT, statusItem("&c反概念觀測已停用", "&7可在 config.yml 中啟用"));
            return;
        }
        if (state == null) {
            menu.replaceExistingItem(STATUS_SLOT, statusItem("&e等待載入", "&7進程資料將於下一次更新載入"));
            menu.replaceExistingItem(SACRIFICE_STATUS_SLOT, sacrificeStatusItem(0));
            return;
        }
        long sacrificePoints = countSacrificeMaterialPoints(state, menu);
        menu.replaceExistingItem(SACRIFICE_STATUS_SLOT, sacrificeStatusItem(sacrificePoints));
        String status;
        if (!state.running && state.radiusLimit == 0) status = "&e等待首次喚醒";
        else if (!state.running && state.autoContinue) status = "&5超頻續行等待中";
        else if (!state.running && !hasReachedRadiusLimit(state)) status = "&a本輪完成，可再次啟動";
        else if (!state.running) status = "&a此區域觀測完成";
        else if (state.paused) status = "&e已由玩家暫停";
        else if (state.waitingForOwner) status = "&e等待擁有者上線";
        else if (state.waitingForCapacity) status = "&e等待可用產能名額";
        else if (state.pendingCores > 0 && !canInsertOne(menu)) status = "&c輸出槽已滿";
        else if (sacrificePoints >= sacrificePointCost) status = "&5獻祭超頻中";
        else status = "&d觀測中";
        String actionLore;
        if (state.running) actionLore = state.paused ? "&a點擊可繼續觀測" : "&a點擊可暫停觀測";
        else if (state.autoContinue) actionLore = "&5條件允許後將自動啟動下一輪";
        else if (canRestart(state)) actionLore = "&a點擊可手動啟動下一輪";
        else if (hasReachedRadiusLimit(state))
            actionLore = "&5準備%d點材料，點擊可原地重啟".formatted(completedResetMaterialCost);
        else actionLore = "&7此觀測進程已結束";
        int cycleObservationTarget = getCycleObservationTarget(state);
        menu.replaceExistingItem(
                STATUS_SLOT,
                statusItem(
                        "&6反概念核心狀態: " + status,
                        "&7概念觀測: &e%d / %d".formatted(state.absorbed, cycleObservationTarget),
                        "&7活動觸鬚: &e%d / %d".formatted(state.running ? tendrilCount : 0, tendrilCount),
                        "&7目前作用半徑: &e%d / %d".formatted(state.radiusLimit, maxTotalRadius),
                        "&7未轉換質量: &e%d / %d".formatted(state.mass, observationsPerLogicCore),
                        "&7獻祭儲備: &e%d點".formatted(sacrificePoints),
                        "",
                        actionLore,
                        "&eShift點擊安全終止並結算"));
    }

    @Override
    public void onPlace(BlockPlaceEvent event, Block block) {
        Location origin = block.getLocation();
        ProcessState state = ProcessState.create(origin, event.getPlayer().getUniqueId());
        if (event.getPlayer().hasPermission(PLACEMENT_BYPASS_PERMISSION)) {
            long usedBypassSlots = processes.values().stream()
                    .filter(other -> state.owner.equals(other.owner) && other.bypassPlacement)
                    .count();
            state.bypassPlacement = usedBypassSlots < maxBypassProcessesPerPlayer;
        }
        processes.put(origin, state);
        DataCache.runAfterSafeLoad(origin, data -> persist(state, data), false);
        block.setType(trailMaterial, false);
    }

    @Override
    public void onBreak(BlockBreakEvent event, @Nullable BlockMenu menu) {
        Location location = event.getBlock().getLocation();
        ProcessState state = processes.get(location);
        if (state == null) {
            SlimefunBlockData data = DataCache.safeLoadBlock(location);
            if (data != null && isCurrentVersion(data)) state = loadState(location, data);
        }
        if (state != null && (state.running || state.pendingCores > 0 || hasOutput(menu) || hasInput(menu))) {
            event.setCancelled(true);
            AddUtils.sendMessage(event.getPlayer(), "&c請先安全終止進程並取空所有輸入與輸出，避免物品遺失");
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE
                && !canFitItem(event.getPlayer(), getItem().clone())) {
            event.setCancelled(true);
            AddUtils.sendMessage(event.getPlayer(), "&c背包空間不足，無法安全回收反概念物質");
            return;
        }
        processes.remove(location);
        // Input and output slots are guaranteed empty here.
        super.onBreak(event, menu);
    }

    private boolean canFitItem(Player player, ItemStack incoming) {
        for (ItemStack existing : player.getInventory().getStorageContents()) {
            if (existing == null || existing.getType() == Material.AIR) return true;
            if (existing.isSimilar(incoming) && existing.getAmount() < existing.getMaxStackSize()) return true;
        }
        return false;
    }

    @Override
    public void handleBlock(SlimefunItem machine) {
        machine.addItemHandler(
                new BlockBreakHandler(false, false) {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onPlayerBreak(BlockBreakEvent event, ItemStack tool, java.util.List<ItemStack> drops) {
                        BlockMenu menu = DataCache.getMenu(event.getBlock().getLocation());
                        SpreadBlock.this.onBreak(event, menu);
                        if (event.isCancelled()) return;

                        // Slimefun would normally drop this list into the world. Return the core directly instead.
                        event.setDropItems(false);
                        drops.clear();
                        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                            event.getPlayer().getInventory().addItem(machine.getItem().clone());
                            AddUtils.sendMessage(event.getPlayer(), "&a反概念物質已安全返還至背包");
                        }
                    }
                },
                new BlockPlaceHandler(false) {
                    @Override
                    @ParametersAreNonnullByDefault
                    public void onPlayerPlace(BlockPlaceEvent event) {
                        SpreadBlock.this.onPlace(event, event.getBlockPlaced());
                    }
                });
    }

    private boolean hasOutput(@Nullable BlockMenu menu) {
        if (menu == null) return false;
        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && item.getType() != Material.AIR) return true;
        }
        return false;
    }

    private boolean hasInput(@Nullable BlockMenu menu) {
        if (menu == null) return false;
        for (int slot : SACRIFICE_INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item != null && item.getType() != Material.AIR) return true;
        }
        return false;
    }

    @Override
    public boolean isSync() {
        return true;
    }

    @Override
    public void tick(Block block, @Nullable BlockMenu menu, SlimefunBlockData data, int tickCount) {
        Location origin = block.getLocation();
        if (!isCurrentVersion(data)) {
            // Legacy spread nodes had no persistent owner or bounded process state. Convert them gradually through the
            // migration budget in the custom ticker rather than letting all loaded nodes mutate in one server tick.
            createResultAt(origin);
            return;
        }

        ProcessState state = processes.computeIfAbsent(origin, ignored -> loadState(origin, data));
        if (!enabled || menu == null) {
            state.paused = true;
            return;
        }

        flushPendingOutputs(state, menu);
        Player owner = state.owner == null ? null : Bukkit.getPlayer(state.owner);
        state.waitingForOwner = owner == null || !owner.isOnline();
        if (state.waitingForOwner) state.waitingForCapacity = false;
        if (!state.running && !state.waitingForOwner && tryAutoContinue(state, menu)) persist(state);
        if (!state.running) {
            if (menu.hasViewer()) updateStatus(menu, origin, state);
            return;
        }
        if (state.waitingForOwner) {
            if (menu.hasViewer()) updateStatus(menu, origin, state);
            return;
        }
        boolean outputBlocked = state.pendingCores > 0 && !canInsertOne(menu);
        if (state.paused || outputBlocked) {
            if (menu.hasViewer()) updateStatus(menu, origin, state);
            return;
        }
        if (!claimProductionSlot(state)) {
            state.waitingForCapacity = true;
            if (menu.hasViewer()) updateStatus(menu, origin, state);
            return;
        }
        state.waitingForCapacity = false;

        int cycleObservationTarget = getCycleObservationTarget(state);
        if (state.absorbed >= cycleObservationTarget) {
            completeCycle(state);
            flushPendingOutputs(state, menu);
            persist(state);
            if (menu.hasViewer()) updateStatus(menu, origin, state);
            return;
        }

        int remainingObservationBudget = getRemainingObservationBudget(state);
        int baseObservations = Math.min(
                Math.min(observationsPerUpdate, remainingObservationBudget), cycleObservationTarget - state.absorbed);
        int observations = baseObservations;
        boolean overclockedThisUpdate = false;
        int remainingCycleCapacity = cycleObservationTarget - state.absorbed - baseObservations;
        int remainingBudgetCapacity = remainingObservationBudget - baseObservations;
        if (remainingCycleCapacity >= sacrificeObservationBonus
                && remainingBudgetCapacity >= sacrificeObservationBonus
                && consumeSacrificeMaterials(state, menu)) {
            observations += sacrificeObservationBonus;
            overclockedThisUpdate = true;
        }
        state.absorbed += observations;
        state.mass += observations;
        consumeObservationBudget(state, observations);
        convertMass(state);
        state.ticksSinceSave++;

        if (state.absorbed >= cycleObservationTarget) {
            completeCycle(state);
            state.autoContinue = overclockedThisUpdate && canRestart(state);
        }
        flushPendingOutputs(state, menu);
        if (!state.running && !state.waitingForOwner && tryAutoContinue(state, menu)) persist(state);
        if (state.ticksSinceSave >= 20 || !state.running) persist(state);
        if (menu.hasViewer()) updateStatus(menu, origin, state);
    }

    private void prepareGlobalBudget() {
        if (globalBudgetPrepared) return;
        remainingGlobalSteps = globalObservationsPerUpdate;
        globalBudgetPrepared = true;
    }

    private int getRemainingObservationBudget(ProcessState state) {
        if (!state.bypassPlacement) {
            prepareGlobalBudget();
            return remainingGlobalSteps;
        }
        if (state.owner == null) return 0;
        int perOwnerBudget = maxBypassProcessesPerPlayer * (observationsPerUpdate + sacrificeObservationBonus);
        return remainingBypassObservationSteps.computeIfAbsent(state.owner, ignored -> perOwnerBudget);
    }

    private void consumeObservationBudget(ProcessState state, int observations) {
        if (!state.bypassPlacement) {
            remainingGlobalSteps -= observations;
        } else if (state.owner != null) {
            remainingBypassObservationSteps.computeIfPresent(
                    state.owner, (ignored, remaining) -> Math.max(0, remaining - observations));
        }
    }

    private void convertMass(ProcessState state) {
        if (state.mass >= observationsPerLogicCore) {
            state.pendingCores += state.mass / observationsPerLogicCore;
            state.mass %= observationsPerLogicCore;
        }
    }

    private int getCycleObservationTarget(ProcessState state) {
        int radiusIncrease = maxRadius;
        if (state.radiusLimit >= maxTotalRadius) {
            int finalRadiusIncrease = maxTotalRadius % maxRadius;
            if (finalRadiusIncrease > 0) radiusIncrease = finalRadiusIncrease;
        }
        return Math.max(1, (int) ((long) observationsPerCycle * radiusIncrease / maxRadius));
    }

    private int getMaximumLogicCoreOutputs() {
        long totalObservations = (long) observationsPerCycle * maxTotalRadius / maxRadius;
        return (int) Math.min(Integer.MAX_VALUE, totalObservations / observationsPerLogicCore);
    }

    private void finish(ProcessState state) {
        if (state.running) completeCycle(state);
        state.autoContinue = false;
    }

    private void completeCycle(ProcessState state) {
        if (!state.running) return;
        convertMass(state);
        state.running = false;
        state.paused = false;
        state.waitingForCapacity = false;
        state.autoContinue = false;
    }

    private boolean tryAutoContinue(ProcessState state, BlockMenu menu) {
        if (!state.autoContinue || state.pendingCores > 0 || !canInsertOne(menu) || !canActivate(state)) return false;
        return restartCycle(state);
    }

    private boolean restartCycle(ProcessState state) {
        if (!canRestart(state)) return false;
        state.radiusLimit = Math.min(maxTotalRadius, state.radiusLimit + maxRadius);
        state.absorbed = 0;
        state.running = true;
        state.paused = false;
        state.waitingForOwner = false;
        state.waitingForCapacity = false;
        state.autoContinue = false;
        return true;
    }

    private boolean canRestart(ProcessState state) {
        return !state.running && !hasReachedRadiusLimit(state);
    }

    private boolean hasReachedRadiusLimit(ProcessState state) {
        return state.radiusLimit >= maxTotalRadius;
    }

    private boolean isSacrificeInputSlot(int slot) {
        for (int inputSlot : SACRIFICE_INPUT_SLOTS) {
            if (inputSlot == slot) return true;
        }
        return false;
    }

    private long countSacrificeMaterialPoints(ProcessState state, BlockMenu menu) {
        long points = state.sacrificeMaterialCredit;
        for (int slot : SACRIFICE_INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            int value = getSacrificeMaterialValue(item);
            if (value > 0) points += (long) item.getAmount() * value;
        }
        return points;
    }

    private int getSacrificeMaterialValue(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
        return slimefunItem == null ? 0 : sacrificeMaterialValues.getOrDefault(slimefunItem.getId(), 0);
    }

    private boolean isAllowedSacrificeMaterial(@Nullable ItemStack item) {
        return getSacrificeMaterialValue(item) > 0;
    }

    private boolean hasSacrificeMaterials(ProcessState state, BlockMenu menu) {
        return countSacrificeMaterialPoints(state, menu) >= sacrificePointCost;
    }

    private boolean consumeSacrificeMaterials(ProcessState state, BlockMenu menu) {
        return consumeSacrificePoints(state, menu, sacrificePointCost);
    }

    private boolean consumeSacrificePoints(ProcessState state, BlockMenu menu, int pointCost) {
        if (countSacrificeMaterialPoints(state, menu) < pointCost) return false;
        if (state.sacrificeMaterialCredit >= pointCost) {
            state.sacrificeMaterialCredit -= pointCost;
            return true;
        }
        int remainingPoints = pointCost - state.sacrificeMaterialCredit;
        state.sacrificeMaterialCredit = 0;
        for (int slot : SACRIFICE_INPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            int value = getSacrificeMaterialValue(item);
            if (value <= 0) continue;
            int neededItems = (remainingPoints + value - 1) / value;
            int consumed = Math.min(item.getAmount(), neededItems);
            int newAmount = item.getAmount() - consumed;
            if (newAmount <= 0) menu.replaceExistingItem(slot, null, false);
            else item.setAmount(newAmount);
            remainingPoints -= consumed * value;
            if (remainingPoints <= 0) {
                state.sacrificeMaterialCredit = -remainingPoints;
                return true;
            }
        }
        return false;
    }

    private void resetCompletedProcess(Player player, ProcessState state, BlockMenu menu) {
        long availablePoints = countSacrificeMaterialPoints(state, menu);
        if (availablePoints < completedResetMaterialCost) {
            AddUtils.sendMessage(
                    player,
                    "&c原地重啟需要%d點獻祭材料，目前只有%d點"
                            .formatted(completedResetMaterialCost, availablePoints));
            return;
        }
        if (!canActivate(state)) {
            AddUtils.sendMessage(player, "&c全服反概念產能已滿，請稍後再重啟");
            return;
        }
        if (!consumeSacrificePoints(state, menu, completedResetMaterialCost)) return;
        state.absorbed = 0;
        state.radiusLimit = 0;
        state.autoContinue = false;
        state.paused = false;
        if (restartCycle(state)) {
            persist(state);
            AddUtils.sendMessage(player, "&5舊有觀測已崩解，反概念核心已自原點重新囂囈");
        }
    }

    private boolean canActivate(ProcessState target) {
        if (!target.bypassPlacement) {
            if (target.owner == null) return false;
            long sharedCandidates = processes.values().stream()
                    .filter(state -> state != target && !state.bypassPlacement && isProductionCandidate(state))
                    .count();
            if (sharedCandidates >= maxConcurrentProcesses) return false;
            long ownerSharedCandidates = processes.values().stream()
                    .filter(state -> state != target
                            && !state.bypassPlacement
                            && target.owner.equals(state.owner)
                            && isProductionCandidate(state))
                    .count();
            return ownerSharedCandidates < maxSharedProcessesPerPlayer;
        }
        if (target.owner == null) return false;
        long ownerBypassCandidates = processes.values().stream()
                .filter(state -> state != target
                        && state.bypassPlacement
                        && target.owner.equals(state.owner)
                        && isProductionCandidate(state))
                .count();
        return ownerBypassCandidates < maxBypassProcessesPerPlayer;
    }

    private boolean isProductionCandidate(ProcessState state) {
        return state.running && !state.paused && isOwnerOnlineAndChunkLoaded(state);
    }

    private boolean isOwnerOnlineAndChunkLoaded(ProcessState state) {
        Player owner = state.owner == null ? null : Bukkit.getPlayer(state.owner);
        if (owner == null || !owner.isOnline()) return false;
        Location location = state.origin;
        return location.getWorld() != null
                && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private boolean claimProductionSlot(ProcessState state) {
        if (state.bypassPlacement) {
            if (state.owner == null) return false;
            int claimed = claimedBypassProductionSlots.getOrDefault(state.owner, 0);
            if (claimed >= maxBypassProcessesPerPlayer) return false;
            claimedBypassProductionSlots.put(state.owner, claimed + 1);
            return true;
        }
        if (claimedSharedProductionSlots >= maxConcurrentProcesses) return false;
        if (state.owner == null) return false;
        int ownerClaimed = claimedSharedProductionSlotsByOwner.getOrDefault(state.owner, 0);
        if (ownerClaimed >= maxSharedProcessesPerPlayer) return false;
        claimedSharedProductionSlots++;
        claimedSharedProductionSlotsByOwner.put(state.owner, ownerClaimed + 1);
        return true;
    }

    private void flushPendingOutputs(ProcessState state, BlockMenu menu) {
        int inserted = 0;
        while (state.pendingCores > 0 && inserted < 8 && insertOne(menu)) {
            state.pendingCores--;
            inserted++;
        }
        if (inserted > 0) {
            persist(state);
        }
    }

    private boolean canInsertOne(BlockMenu menu) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = menu.getItemInSlot(slot);
            if (existing == null || existing.getType() == Material.AIR) return true;
            if (existing.getAmount() < existing.getMaxStackSize()
                    && CraftUtils.matchItemStack(existing, AddItem.LOGIC_CORE, false)) return true;
        }
        return false;
    }

    private boolean insertOne(BlockMenu menu) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = menu.getItemInSlot(slot);
            if (existing != null
                    && existing.getAmount() < existing.getMaxStackSize()
                    && CraftUtils.matchItemStack(existing, AddItem.LOGIC_CORE, false)) {
                existing.setAmount(existing.getAmount() + 1);
                return true;
            }
        }
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = menu.getItemInSlot(slot);
            if (existing == null || existing.getType() == Material.AIR) {
                ItemStack output = AddItem.LOGIC_CORE.clone();
                output.setAmount(1);
                menu.replaceExistingItem(slot, output, false);
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentVersion(SlimefunBlockData data) {
        return Integer.toString(DATA_VERSION).equals(data.getData(KEY_VERSION));
    }

    private ProcessState loadState(Location origin, SlimefunBlockData data) {
        ProcessState state = new ProcessState(origin);
        try {
            String owner = data.getData(KEY_OWNER);
            state.owner = owner == null || owner.equals("null") ? null : UUID.fromString(owner);
        } catch (IllegalArgumentException ignored) {
            state.owner = null;
        }
        state.absorbed = DataCache.getCustomData(data, KEY_ABSORBED, 0);
        state.mass = DataCache.getCustomData(data, KEY_MASS, 0);
        state.pendingCores = DataCache.getCustomData(data, KEY_PENDING, 0);
        state.running = DataCache.getCustomData(data, KEY_RUNNING, 0) == 1;
        state.paused = DataCache.getCustomData(data, KEY_PAUSED, 0) == 1;
        state.sacrificeMaterialCredit = clamp(
                DataCache.getCustomData(data, KEY_MATERIAL_CREDIT, 0), 0, 1_000_000);
        state.autoContinue = DataCache.getCustomData(data, KEY_AUTO_CONTINUE, 0) == 1;
        state.bypassPlacement = DataCache.getCustomData(data, KEY_BYPASS_PLACEMENT, 0) == 1;
        state.radiusLimit = clamp(DataCache.getCustomData(data, KEY_RADIUS, maxRadius), 0, maxTotalRadius);
        return state;
    }

    private void persist(ProcessState state) {
        SlimefunBlockData data = DataCache.safeLoadBlock(state.origin);
        if (data == null) return;
        persist(state, data);
    }

    private void persist(ProcessState state, SlimefunBlockData data) {
        data.setData(KEY_VERSION, Integer.toString(DATA_VERSION));
        data.setData(KEY_OWNER, state.owner == null ? "null" : state.owner.toString());
        data.setData(KEY_ABSORBED, Integer.toString(state.absorbed));
        data.setData(KEY_MASS, Integer.toString(state.mass));
        data.setData(KEY_PENDING, Integer.toString(state.pendingCores));
        data.setData(KEY_RUNNING, state.running ? "1" : "0");
        data.setData(KEY_PAUSED, state.paused ? "1" : "0");
        data.setData(KEY_MATERIAL_CREDIT, Integer.toString(state.sacrificeMaterialCredit));
        data.setData(KEY_AUTO_CONTINUE, state.autoContinue ? "1" : "0");
        data.setData(KEY_BYPASS_PLACEMENT, state.bypassPlacement ? "1" : "0");
        data.setData(KEY_RADIUS, Integer.toString(state.radiusLimit));
        state.ticksSinceSave = 0;
    }

    /** Stops all loaded processes. Used by the administrator cleanup item. */
    public void abortAllProcesses() {
        for (ProcessState state : processes.values()) {
            finish(state);
            persist(state);
        }
        processes.clear();
    }

    /** Returns a read-only snapshot of the currently loaded antimass production capacity. */
    public CapacitySnapshot getCapacitySnapshot() {
        int sharedPlaced = 0;
        int sharedReady = 0;
        int sharedWaiting = 0;
        int bypassPlaced = 0;
        int bypassReady = 0;
        int bypassWaiting = 0;
        int offline = 0;
        int paused = 0;
        Set<UUID> bypassOwners = new HashSet<>();
        Map<UUID, Integer> sharedReadyByOwner = new HashMap<>();
        Map<UUID, Integer> bypassReadyByOwner = new HashMap<>();

        for (ProcessState state : processes.values()) {
            if (state.bypassPlacement) {
                bypassPlaced++;
                if (state.owner != null) bypassOwners.add(state.owner);
            } else {
                sharedPlaced++;
            }
            if (state.paused) paused++;
            Player owner = state.owner == null ? null : Bukkit.getPlayer(state.owner);
            if (state.running && (owner == null || !owner.isOnline())) offline++;
            if (!isProductionCandidate(state)) continue;
            if (state.bypassPlacement) {
                if (state.owner != null) bypassReadyByOwner.merge(state.owner, 1, Integer::sum);
                if (state.waitingForCapacity) bypassWaiting++;
            } else {
                if (state.owner != null) sharedReadyByOwner.merge(state.owner, 1, Integer::sum);
                if (state.waitingForCapacity) sharedWaiting++;
            }
        }

        sharedReady = sharedReadyByOwner.values().stream()
                .mapToInt(count -> Math.min(count, maxSharedProcessesPerPlayer))
                .sum();
        bypassReady = bypassReadyByOwner.values().stream()
                .mapToInt(count -> Math.min(count, maxBypassProcessesPerPlayer))
                .sum();

        int bypassBudgetPerOwner = maxBypassProcessesPerPlayer * (observationsPerUpdate + sacrificeObservationBonus);
        return new CapacitySnapshot(
                processes.size(),
                sharedPlaced,
                sharedReady,
                sharedWaiting,
                maxPlacedProcesses,
                maxConcurrentProcesses,
                globalObservationsPerUpdate,
                bypassPlaced,
                bypassReady,
                bypassWaiting,
                bypassOwners.size(),
                maxBypassProcessesPerPlayer,
                bypassBudgetPerOwner,
                offline,
                paused);
    }

    public record CapacitySnapshot(
            int trackedProcesses,
            int sharedPlaced,
            int sharedReady,
            int sharedWaiting,
            int sharedPlacementLimit,
            int sharedActiveLimit,
            int sharedObservationBudget,
            int bypassPlaced,
            int bypassReady,
            int bypassWaiting,
            int bypassOwners,
            int bypassLimitPerOwner,
            int bypassObservationBudgetPerOwner,
            int offlineProcesses,
            int pausedProcesses) {}

    /** Converts a core (or a legacy spread node) into the configured final Slimefun block. */
    public void createResultAt(Location location) {
        processes.remove(location);
        WorldUtils.createSlimefunBlock(location, null, result, resultMaterial, true, () -> {});
    }

    private void recheckProcesses() {
        processes.entrySet().removeIf(entry -> {
            Location location = entry.getKey();
            if (location.getWorld() == null) return true;
            if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return false;
            return DataCache.getSfItem(location) != this;
        });
    }

    @Override
    public void preRegister() {
        super.preRegister();
        createPreset(this, getItemName(), this::constructMenu);
        addItemHandler(new BlockTicker() {
            private int currentTick;
            private int legacyMigrations;

            @Override
            public boolean isSynchronized() {
                return true;
            }

            @Override
            @ParametersAreNonnullByDefault
            public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                if (!isCurrentVersion(data)) {
                    ProcessState newlyPlaced = processes.get(block.getLocation());
                    if (newlyPlaced != null) {
                        persist(newlyPlaced, data);
                    } else if (legacyMigrations < 16) {
                        legacyMigrations++;
                        SpreadBlock.this.tick(block, data.getBlockMenu(), data, 0);
                        return;
                    } else {
                        return;
                    }
                }
                if (currentTick % tickRate == 0) {
                    SpreadBlock.this.tick(block, data.getBlockMenu(), data, 0);
                }
            }

            @Override
            public void uniqueTick() {
                legacyMigrations = 0;
                globalBudgetPrepared = false;
                claimedSharedProductionSlots = 0;
                claimedSharedProductionSlotsByOwner.clear();
                claimedBypassProductionSlots.clear();
                remainingBypassObservationSteps.clear();
                currentTick = (currentTick + 1) % tickRate;
            }
        });
        SlimefunBlockPlaceLimitListener.registerBlockLimit(this, event -> {
            recheckProcesses();
            Location placed = event.getBlockPlaced().getLocation();
            Player player = event.getPlayer();
            UUID owner = player.getUniqueId();
            long usedBypassSlots = processes.values().stream()
                    .filter(state -> !state.origin.equals(placed)
                            && owner.equals(state.owner)
                            && state.bypassPlacement)
                    .count();
            boolean canUseBypassSlot = player.hasPermission(PLACEMENT_BYPASS_PERMISSION)
                    && usedBypassSlots < maxBypassProcessesPerPlayer;
            if (canUseBypassSlot) return;

            long ownerSharedPlacedCount = processes.values().stream()
                    .filter(state -> !state.origin.equals(placed)
                            && owner.equals(state.owner)
                            && !state.bypassPlacement)
                    .count();
            if (ownerSharedPlacedCount >= maxSharedProcessesPerPlayer) {
                event.setCancelled(true);
                processes.remove(placed);
                AddUtils.sendMessage(
                        player,
                        "&c你已使用全部%d個反概念共用名額，物品未被消耗"
                                .formatted(maxSharedProcessesPerPlayer));
                return;
            }

            long sharedPlacedCount = processes.values().stream()
                    .filter(state -> !state.origin.equals(placed) && !state.bypassPlacement)
                    .count();
            if (sharedPlacedCount >= maxPlacedProcesses) {
                event.setCancelled(true);
                processes.remove(placed);
                AddUtils.sendMessage(
                        player, "&c全伺服器的%d個共用反概念核心名額已滿".formatted(maxPlacedProcesses));
            }
        });
    }

    @Override
    public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
        return flow == ItemTransportFlow.WITHDRAW ? OUTPUT_SLOTS : SACRIFICE_INPUT_SLOTS;
    }

    @Override
    public int[] getSlotsAccessedByItemTransportPlus(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) return OUTPUT_SLOTS;
        return isAllowedSacrificeMaterial(item) ? SACRIFICE_INPUT_SLOTS : new int[0];
    }

    private static final class ProcessState {
        private final Location origin;
        private UUID owner;
        private int absorbed;
        private int mass;
        private int pendingCores;
        private boolean running;
        private boolean paused;
        private int sacrificeMaterialCredit;
        private boolean autoContinue;
        private boolean waitingForOwner;
        private boolean waitingForCapacity;
        private boolean bypassPlacement;
        private int ticksSinceSave;
        private int radiusLimit;

        private ProcessState(Location origin) {
            this.origin = origin.clone();
        }

        private static ProcessState create(Location origin, UUID owner) {
            ProcessState state = new ProcessState(origin);
            state.owner = owner;
            state.running = false;
            state.radiusLimit = 0;
            return state;
        }
    }
}
