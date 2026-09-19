package me.matl114.logitech.core.Machines.SpecialMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Blocks.Laser;
import me.matl114.logitech.core.Blocks.MultiBlock.FinalAltarCore;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.CraftUtils;
import me.matl114.logitech.utils.DataCache;
import me.matl114.logitech.utils.FeatureConfig;
import me.matl114.logitech.utils.Utils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class RandomEditor extends AbstractMachine implements FinalAltarCore.FinalAltarChargable, Laser.LaserChargable {
    private static final Map<String, Integer> ENCHANTMENT_LIMITS = createEnchantmentLimits();

    private static Map<String, Integer> createEnchantmentLimits() {
        Map<String, Integer> limits = new LinkedHashMap<>();
        limits.put("sharpness", 53);
        limits.put("mending", 30);
        limits.put("unbreaking", 45);
        limits.put("looting", 30);
        limits.put("fortune", 45);
        limits.put("protection", 45);
        limits.put("thorns", 30);
        limits.put("projectile_protection", 45);
        limits.put("respiration", 45);
        limits.put("fire_protection", 45);
        limits.put("blast_protection", 45);
        limits.put("aqua_affinity", 45);
        limits.put("feather_falling", 10);
        limits.put("depth_strider", 45);
        limits.put("frost_walker", 4);
        limits.put("soul_speed", 6);
        limits.put("swift_sneak", 6);
        limits.put("smite", 12);
        limits.put("bane_of_arthropods", 12);
        limits.put("knockback", 5);
        limits.put("fire_aspect", 30);
        limits.put("sweeping", 7);
        limits.put("efficiency", 45);
        limits.put("silk_touch", 8);
        limits.put("power", 53);
        limits.put("punch", 5);
        limits.put("flame", 30);
        limits.put("infinity", 38);
        limits.put("luck_of_the_sea", 7);
        limits.put("lure", 7);
        limits.put("loyalty", 7);
        limits.put("impaling", 12);
        limits.put("riptide", 7);
        limits.put("channeling", 1);
        limits.put("multishot", 1);
        limits.put("quick_charge", 5);
        limits.put("piercing", 10);
        limits.replaceAll((id, defaultLimit) ->
                Math.max(1, FeatureConfig.getInt("enchantment-limits." + id, defaultLimit)));
        return Map.copyOf(limits);
    }

    private static String getEnchantmentId(Enchantment enchantment) {
        String id = enchantment.getKey().getKey().toLowerCase(Locale.ROOT);
        return "sweeping_edge".equals(id) ? "sweeping" : id;
    }

    private static boolean isSupportedEnchantment(Enchantment enchantment) {
        return enchantment != null && ENCHANTMENT_LIMITS.containsKey(getEnchantmentId(enchantment));
    }

    private static int getEnchantmentLimit(Enchantment enchantment) {
        return ENCHANTMENT_LIMITS.getOrDefault(getEnchantmentId(enchantment), 0);
    }

    private static final int TARGET_SLOT = 19;
    private static final int RESULT_SLOT = 22;
    private static final int SACRIFICE_SLOT = 25;
    private static final int STABILIZER_SLOT = 34;
    private static final int[] INPUT_SLOTS = new int[] {TARGET_SLOT, SACRIFICE_SLOT, STABILIZER_SLOT};
    private static final int[] OUTPUT_SLOTS = new int[] {RESULT_SLOT};
    private static final int[] PREVIEW_SLOTS = new int[] {10, 13, 16};
    private static final int LOCK_SLOT = 31;
    private static final String LOCK_KEY = "quantum-lock";
    private static final int MAX_OBSERVATIONS = 5;
    private final Map<Location, QuantumState> quantumStates = new ConcurrentHashMap<>();

    private static final class QuantumState {
        private final ItemStack target;
        private final ItemStack sacrifice;
        private final Enchantment locked;
        private ItemStack result;
        private int observations;

        private QuantumState(ItemStack target, ItemStack sacrifice, ItemStack result, Enchantment locked) {
            this.target = target;
            this.sacrifice = sacrifice;
            this.result = result;
            this.locked = locked;
        }

        private boolean matches(ItemStack currentTarget, ItemStack currentSacrifice) {
            return currentTarget != null
                    && currentSacrifice != null
                    && target.isSimilar(currentTarget)
                    && sacrifice.isSimilar(currentSacrifice);
        }
    }

    protected final int INFO_SLOT = 30;
    protected final ItemStack INFO_ITEM = new CustomItemStack(
            Material.KNOWLEDGE_BOOK,
            "&6量子深潛－操作說明",
            "&e① &7左下放入 &b1 件目標物品",
            "&e② &7右下放入 &d1 件帶有附魔的書、武器或工具",
            "&8　&7盔甲與其他類型物品不能作為祭品",
            "&e③ &7先設定附魔鎖，再放入祭品建立量子狀態",
            "&e④ &7右下放入量子觀測核心，選擇下一步",
            "&c量子狀態建立後與輸入綁定，重新放入也不會重抽",
            "",
            "&a收束 &7－消耗 1 個核心，立即取回目前成果",
            "&d深入觀測 &7－消耗 1 個核心，可能增幅、反噬或坍縮",
            "&7觀測越深越危險，第 5 次會強制收束",
            "&c奇點過載 &7－消耗 4 個核心，豪賭後立即強制收束",
            "&7過載只有低機率大幅增幅，失敗會嚴重坍縮",
            "",
            "&6附魔鎖定：&7點擊下方鐵柵欄切換附魔",
            "&7被鎖定的附魔不會遭到反噬或坍縮",
            "",
            "&e每次操作都會消耗核心與電力",
            "&e完成收束時才會消耗附魔祭品",
            "&c輸出槽必須保持空白才能完成量化");
    protected final ItemStack LOCK_NONE = new CustomItemStack(
            Material.IRON_BARS, "&6附魔鎖定", "&7目前沒有鎖定附魔", "&e點擊循環選擇要保護的附魔");
    protected final ItemStack[] EMPTY_PREVIEWS = new ItemStack[] {
        new CustomItemStack(
                Material.LIME_STAINED_GLASS_PANE,
                "&a收束－等待建立狀態",
                "&7支付代價並取回目前隱藏成果"),
        new CustomItemStack(
                Material.PURPLE_STAINED_GLASS_PANE,
                "&d深入觀測－等待建立狀態",
                "&7逐層深入並承受不斷增加的風險"),
        new CustomItemStack(
                Material.RED_STAINED_GLASS_PANE,
                "&c奇點過載－等待建立狀態",
                "&7高代價、低勝率，立即強制結算")
    };

    public int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    protected final int STATUS_SLOT = 4;
    protected final ItemStack STATUS_ON =
            new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&6機器資訊", "&7狀態：&a已啟動");
    protected final ItemStack STATUS_OFF =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&6機器資訊", "&7狀態：&c未啟動");
    protected final ItemSetting<Boolean> ENABLE_STACKABLE_ITEM = create("enable-stackable-item", true);

    public RandomEditor(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption);
        PostSetupTasks.addPostRegisterTask(() -> {
            ItemStack resultDisplay = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = resultDisplay.getItemMeta();
            for (Enchantment enchantment : getRegisteredEnchantments()) {
                meta.addEnchant(enchantment, getEnchantmentLimit(enchantment), true);
            }
            meta.setDisplayName(AddUtils.resolveColor(AddUtils.color("支援附魔與目前上限")));
            resultDisplay.setItemMeta(meta);
            this.setDisplayRecipes(Utils.list(
                    AddUtils.getInfoShow(
                            "&f機制 - &c充能",
                            "&7當置於貳級終極祭壇上時",
                            "&7且機器被終極祭壇結構中的所有宏激光發射器充能時",
                            "&7即終極祭壇中四個宏激光發射器分別位於四個壹級以上終極祭壇上時",
                            "&7機器啟動並開始運轉"),
                    null,
                    AddUtils.getInfoShow(
                            "&f機制 - &c量子深潛",
                            "&7左下放入一件目標物品，右下放入一件附魔祭品",
                            "&7祭品僅限帶有支援附魔的附魔書、武器或工具",
                            "&7盔甲與其他類型物品不能作為祭品",
                            "&7先設定附魔鎖，再放入祭品建立不可重抽的量子狀態",
                            "&a收束：消耗 1 個量子觀測核心並取回目前成果",
                            "&d深入觀測：消耗 1 個核心，可能增幅、反噬或坍縮",
                            "&7風險會逐次提高，第五次觀測強制收束",
                            "&c奇點過載：消耗 4 個核心豪賭，隨後立即強制收束",
                            "&7所有附魔變化在收束前皆不可查看",
                            "&7僅支援說明中列出的附魔，每種附魔都有獨立上限"),
                    resultDisplay,
                    AddUtils.getInfoShow(
                            "&f機制 - &c風險與鎖定",
                            "&7深入觀測越多次，增幅率越低，反噬與坍縮率越高",
                            "&7可鎖定目標物品的一項附魔，使其不會遭到反噬或坍縮",
                            "&e鎖定不會突破既有的附魔限制"),
                    ENABLE_STACKABLE_ITEM.getValue()
                            ? null
                            : AddUtils.getInfoShow(
                                    "&f注意 - &c堆疊限制", "&7本伺服器禁止強化可堆疊物品", "&7只有最大堆疊數為 1 的物品可以進行量化")));
        });
    }

    public void constructMenu(BlockMenuPreset preset) {
        preset.setSize(36);
        for (int slot = 0; slot < 36; slot++) {
            if (slot != TARGET_SLOT
                    && slot != SACRIFICE_SLOT
                    && slot != RESULT_SLOT
                    && slot != STABILIZER_SLOT) {
                preset.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }
        }
        preset.addItem(STATUS_SLOT, STATUS_OFF, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(INFO_SLOT, INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(LOCK_SLOT, LOCK_NONE, ChestMenuUtils.getEmptyClickHandler());
        for (int i = 0; i < PREVIEW_SLOTS.length; i++) {
            preset.addItem(PREVIEW_SLOTS[i], EMPTY_PREVIEWS[i], ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(
                18,
                new CustomItemStack(
                        Material.BLUE_STAINED_GLASS_PANE,
                        "&9目標物品槽",
                        "&7右側放入要進行量化的物品",
                        "&c一次只能放入 1 件"),
                ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(20, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(21, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(23, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(24, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(
                26,
                new CustomItemStack(
                        Material.MAGENTA_STAINED_GLASS_PANE,
                        "&d附魔祭品槽",
                        "&7左側放入附魔書、武器或工具",
                        "&c不接受盔甲與其他類型物品",
                        "&7祭品上的支援附魔會併入初始隱藏成果",
                        "&e每次量化只消耗 1 件祭品"),
                ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(
                33,
                new CustomItemStack(
                        Material.ECHO_SHARD,
                        "&d量子觀測核心槽",
                        "&7右側放入量子觀測核心",
                        "&7收束與深入觀測每次消耗 1 個",
                        "&c奇點過載一次消耗 4 個"),
                ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(35, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void newMenuInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        menu.addMenuOpeningHandler(player -> refreshQuantumMenu(menu));
        menu.addMenuClickHandler(LOCK_SLOT, (player, slot, item, action) -> {
            if (quantumStates.containsKey(menu.getLocation())) {
                AddUtils.sendMessage(player, "&c時間線已固定，完成本次量化前不能更換附魔鎖");
                return false;
            }
            cycleLockedEnchantment(menu);
            refreshQuantumMenu(menu);
            return false;
        });
        for (int i = 0; i < PREVIEW_SLOTS.length; i++) {
            final int selectedAction = i;
            menu.addMenuClickHandler(PREVIEW_SLOTS[i], (player, slot, item, action) -> {
                handleQuantumAction(player, block, menu, selectedAction);
                return false;
            });
        }
        refreshQuantumMenu(menu);
    }

    public void tick(Block b, @Nullable BlockMenu menu, SlimefunBlockData data, int tickCount) {
        if (menu == null) return;
        if (conditionHandle(b, menu) && FinalFeature.isFinalAltarCharged(this, data)) {
            if (menu.hasViewer()) {
                menu.replaceExistingItem(STATUS_SLOT, STATUS_ON);
            }
            process(b, menu, data);

        } else {
            if (menu.hasViewer()) {
                menu.replaceExistingItem(STATUS_SLOT, STATUS_OFF);
            }
        }
    }

    private Random rand = new Random();
    protected static Enchantment[] registeredEnchantments;

    public Enchantment[] getRegisteredEnchantments() {
        if (registeredEnchantments == null || registeredEnchantments.length == 0) {
            registeredEnchantments = java.util.Arrays.stream(Enchantment.values())
                    .filter(RandomEditor::isSupportedEnchantment)
                    .toArray(Enchantment[]::new);
        }
        return registeredEnchantments;
    }

    @Override
    public void onBreak(BlockBreakEvent e, @Nullable BlockMenu menu) {
        super.onBreak(e, menu);
        if (menu != null) {
            quantumStates.remove(menu.getLocation());
        }
    }

    public void process(Block b, BlockMenu inv, SlimefunBlockData data) {
        ItemStack target = inv.getItemInSlot(TARGET_SLOT);
        ItemStack sacrifice = inv.getItemInSlot(SACRIFICE_SLOT);
        QuantumState state = quantumStates.get(inv.getLocation());
        if (!isValidInput(target, sacrifice)) {
            hideQuantumPreviews(inv);
            return;
        }
        if (state == null) {
            createQuantumState(inv, target, sacrifice);
        } else if (state.matches(target, sacrifice)) {
            showQuantumPreviews(inv, state);
        } else {
            hideQuantumPreviews(inv);
        }
    }

    private boolean isValidInput(ItemStack target, ItemStack sacrifice) {
        if (target == null || sacrifice == null || target.getAmount() != 1 || sacrifice.getAmount() < 1) {
            return false;
        }
        if (!ENABLE_STACKABLE_ITEM.getValue() && target.getMaxStackSize() != 1) {
            return false;
        }
        ItemMeta sacrificeMeta = sacrifice.getItemMeta();
        return isAllowedSacrificeType(sacrifice)
                && sacrificeMeta != null
                && !getSacrificeEnchantments(sacrificeMeta).isEmpty();
    }

    private boolean isAllowedSacrificeType(ItemStack sacrifice) {
        String type = sacrifice.getType().name();
        return type.equals("ENCHANTED_BOOK")
                || type.equals("BOW")
                || type.equals("CROSSBOW")
                || type.equals("TRIDENT")
                || type.equals("MACE")
                || type.equals("SHEARS")
                || type.equals("FISHING_ROD")
                || type.equals("FLINT_AND_STEEL")
                || type.equals("BRUSH")
                || type.endsWith("_SWORD")
                || type.endsWith("_AXE")
                || type.endsWith("_PICKAXE")
                || type.endsWith("_SHOVEL")
                || type.endsWith("_HOE");
    }

    private void createQuantumState(BlockMenu menu, ItemStack target, ItemStack sacrifice) {
        int lockIndex = DataCache.getCustomData(menu.getLocation(), LOCK_KEY, -1);
        Enchantment locked = getLockedEnchantment(target, lockIndex);
        quantumStates.put(
                menu.getLocation(),
                new QuantumState(target.clone(), sacrifice.clone(), createBaseTimeline(target, sacrifice), locked));
        showQuantumPreviews(menu, quantumStates.get(menu.getLocation()));
        refreshLockItem(menu, target);
    }

    private ItemStack createBaseTimeline(ItemStack target, ItemStack sacrifice) {
        ItemStack result = target.clone();
        ItemMeta resultMeta = result.getItemMeta();
        ItemMeta sacrificeMeta = sacrifice.getItemMeta();
        if (resultMeta != null && sacrificeMeta != null) {
            for (Map.Entry<Enchantment, Integer> enchantment : getSacrificeEnchantments(sacrificeMeta).entrySet()) {
                int current = resultMeta.getEnchantLevel(enchantment.getKey());
                int limit = getEnchantmentLimit(enchantment.getKey());
                resultMeta.addEnchant(enchantment.getKey(), Math.min(limit, Math.max(current, enchantment.getValue())), true);
            }
            result.setItemMeta(resultMeta);
        }
        return result;
    }

    private double getEnchantmentSaturation(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0.0;
        int levels = 0;
        int limits = 0;
        for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
            if (!isSupportedEnchantment(entry.getKey())) continue;
            int limit = getEnchantmentLimit(entry.getKey());
            levels += Math.min(limit, Math.max(0, entry.getValue()));
            limits += limit;
        }
        return limits == 0 ? 0.0 : Math.min(1.0, levels / (double) limits);
    }

    private void applyGuaranteedEdits(ItemStack item, int attempts, int maxUpgrade) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        for (int i = 0; i < attempts; i++) {
            List<Enchantment> candidates = java.util.Arrays.stream(getRegisteredEnchantments())
                    .filter(enchantment -> meta.getEnchantLevel(enchantment) < getEnchantmentLimit(enchantment))
                    .toList();
            if (candidates.isEmpty()) break;
            Enchantment selected = candidates.get(rand.nextInt(candidates.size()));
            int level = meta.getEnchantLevel(selected);
            int upgrade = rand.nextInt(Math.max(1, maxUpgrade)) + 1;
            meta.addEnchant(selected, Math.min(getEnchantmentLimit(selected), level + upgrade), true);
        }
        item.setItemMeta(meta);
    }

    private void redistributeEnchantments(ItemStack item, Enchantment locked, int losses) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<Enchantment> candidates = meta.getEnchants().keySet().stream()
                .filter(RandomEditor::isSupportedEnchantment)
                .filter(enchantment -> enchantment != locked)
                .toList();
        for (int i = 0; i < losses && !candidates.isEmpty(); i++) {
            Enchantment selected = candidates.get(rand.nextInt(candidates.size()));
            int level = meta.getEnchantLevel(selected);
            if (level <= 1) meta.removeEnchant(selected);
            else meta.addEnchant(selected, level - 1, true);
        }
        item.setItemMeta(meta);
    }

    private void collapseEnchantments(ItemStack item, Enchantment locked, double saturation) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<Enchantment> candidates = meta.getEnchants().keySet().stream()
                .filter(RandomEditor::isSupportedEnchantment)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        candidates.remove(locked);
        candidates.sort(Comparator.comparingInt(meta::getEnchantLevel).reversed());
        int affected = Math.max(1, (int) Math.ceil(candidates.size() * (0.65 + saturation * 0.30)));
        for (int i = 0; i < affected && i < candidates.size(); i++) {
            Enchantment enchantment = candidates.get(i);
            int level = meta.getEnchantLevel(enchantment);
            if (level <= 2) meta.removeEnchant(enchantment);
            else meta.addEnchant(enchantment, Math.max(1, level / 2), true);
        }
        item.setItemMeta(meta);
    }

    private void handleQuantumAction(Player player, Block block, BlockMenu menu, int action) {
        QuantumState state = quantumStates.get(menu.getLocation());
        ItemStack target = menu.getItemInSlot(TARGET_SLOT);
        ItemStack sacrifice = menu.getItemInSlot(SACRIFICE_SLOT);
        if (menu.getItemInSlot(RESULT_SLOT) != null) {
            AddUtils.sendMessage(player, "&c請先清空中央的輸出槽");
            return;
        }
        if (!isValidInput(target, sacrifice)) {
            AddUtils.sendMessage(player, "&c祭品必須是帶有支援附魔的附魔書、武器或工具");
            return;
        }
        if (state == null) {
            AddUtils.sendMessage(player, "&e尚未生成量化時間線");
            return;
        }
        if (!state.matches(target, sacrifice)) {
            AddUtils.sendMessage(player, "&c本次時間線已與原輸入綁定，請放回原本的目標物品與祭品");
            hideQuantumPreviews(menu);
            return;
        }
        SlimefunBlockData data = DataCache.safeLoadBlock(block.getLocation());
        if (!FinalFeature.isFinalAltarCharged(this, data)) {
            AddUtils.sendMessage(player, "&c機器尚未獲得終極祭壇與四座宏激光發射器充能");
            return;
        }
        if (!conditionHandle(block, menu)) {
            AddUtils.sendMessage(player, "&c機器電力不足，無法干涉量子狀態");
            return;
        }
        int coreCost = action == 2 ? 4 : 1;
        if (!consumeObservationCores(menu, coreCost)) {
            AddUtils.sendMessage(player, "&c本次操作需要 " + coreCost + " 個量子觀測核心");
            return;
        }
        progressorCost(block, menu);

        if (action == 0) {
            finishQuantumState(player, menu, state, "&a主動收束");
        } else if (action == 1) {
            observeDeeper(player, menu, state);
        } else {
            overloadQuantumState(player, menu, state);
        }
    }

    private boolean consumeObservationCores(BlockMenu menu, int amount) {
        ItemStack cores = menu.getItemInSlot(STABILIZER_SLOT);
        if (cores == null
                || !CraftUtils.matchItemStack(cores, AddItem.QUANTUM_OBSERVATION_CORE, false)
                || cores.getAmount() < amount) {
            return false;
        }
        cores.setAmount(cores.getAmount() - amount);
        if (cores.getAmount() <= 0) menu.replaceExistingItem(STABILIZER_SLOT, null);
        return true;
    }

    private void observeDeeper(Player player, BlockMenu menu, QuantumState state) {
        int depth = ++state.observations;
        double gainChance = 0.62 - (depth - 1) * 0.12;
        double collapseChance = 0.08 + (depth - 1) * 0.12;
        double roll = rand.nextDouble();
        String event;
        if (roll < gainChance) {
            applyGuaranteedEdits(state.result, depth >= 4 ? 2 : 1, depth <= 2 ? 2 : 1);
            event = "&a量子共振";
        } else if (roll < gainChance + 0.30) {
            redistributeEnchantments(state.result, state.locked, 1 + depth / 2);
            event = "&6量子反噬";
        } else {
            collapseEnchantments(state.result, state.locked, getEnchantmentSaturation(state.result));
            event = "&c附魔坍縮";
        }

        if (depth >= MAX_OBSERVATIONS) {
            AddUtils.sendMessage(player, event + "&7！已達觀測極限，量子狀態被強制收束");
            finishQuantumState(player, menu, state, "&d五層深潛");
        } else {
            AddUtils.sendMessage(
                    player,
                    event + "&7！已完成第 &e" + depth + "&7／" + MAX_OBSERVATIONS + " &7層觀測，附魔明細仍被遮蔽");
            showQuantumPreviews(menu, state);
        }
    }

    private void overloadQuantumState(Player player, BlockMenu menu, QuantumState state) {
        double saturation = getEnchantmentSaturation(state.result);
        double successChance = Math.max(0.10, 0.25 - saturation * 0.15);
        if (rand.nextDouble() < successChance) {
            applyGuaranteedEdits(state.result, 5 + rand.nextInt(4), 2);
            AddUtils.sendMessage(player, "&d奇點過載成功！量子狀態正在強制收束");
        } else {
            collapseEnchantments(state.result, state.locked, 1.0);
            redistributeEnchantments(state.result, state.locked, 2);
            AddUtils.sendMessage(player, "&4奇點過載失敗！大部分未鎖定附魔已遭到坍縮");
        }
        finishQuantumState(player, menu, state, "&c奇點過載");
    }

    private void finishQuantumState(Player player, BlockMenu menu, QuantumState state, String method) {
        ItemStack sacrifice = menu.getItemInSlot(SACRIFICE_SLOT);
        menu.replaceExistingItem(RESULT_SLOT, state.result.clone());
        menu.replaceExistingItem(TARGET_SLOT, null);
        sacrifice.setAmount(sacrifice.getAmount() - 1);
        if (sacrifice.getAmount() <= 0) menu.replaceExistingItem(SACRIFICE_SLOT, null);
        invalidateQuantumState(menu);
        AddUtils.sendMessage(player, "&a量化完成！收束方式：" + method);
    }

    private void cycleLockedEnchantment(BlockMenu menu) {
        ItemStack target = menu.getItemInSlot(TARGET_SLOT);
        List<Enchantment> enchantments = getEnchantments(target);
        if (enchantments.isEmpty()) {
            DataCache.setCustomData(menu.getLocation(), LOCK_KEY, -1);
            return;
        }
        int current = DataCache.getCustomData(menu.getLocation(), LOCK_KEY, -1);
        DataCache.setCustomData(menu.getLocation(), LOCK_KEY, current >= enchantments.size() - 1 ? -1 : current + 1);
    }

    private Enchantment getLockedEnchantment(ItemStack target, int index) {
        List<Enchantment> enchantments = getEnchantments(target);
        return index >= 0 && index < enchantments.size() ? enchantments.get(index) : null;
    }

    private List<Enchantment> getEnchantments(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return List.of();
        return item.getEnchantments().keySet().stream()
                .filter(RandomEditor::isSupportedEnchantment)
                .sorted(Comparator.comparing(enchantment -> enchantment.getKey().toString()))
                .toList();
    }

    private Map<Enchantment, Integer> getSacrificeEnchantments(ItemMeta meta) {
        Map<Enchantment, Integer> enchantments;
        if (meta instanceof EnchantmentStorageMeta storageMeta && !storageMeta.getStoredEnchants().isEmpty()) {
            enchantments = storageMeta.getStoredEnchants();
        } else {
            enchantments = meta == null ? Map.of() : meta.getEnchants();
        }
        Map<Enchantment, Integer> supported = new LinkedHashMap<>();
        enchantments.forEach((enchantment, level) -> {
            if (isSupportedEnchantment(enchantment)) supported.put(enchantment, level);
        });
        return supported;
    }

    private void refreshLockItem(BlockMenu menu, ItemStack target) {
        Enchantment locked = getLockedEnchantment(target, DataCache.getCustomData(menu.getLocation(), LOCK_KEY, -1));
        if (locked == null) {
            menu.replaceExistingItem(LOCK_SLOT, LOCK_NONE);
            return;
        }
        menu.replaceExistingItem(
                LOCK_SLOT,
                new CustomItemStack(
                        Material.IRON_BARS,
                        "&6附魔鎖定",
                        "&7已鎖定：&e" + locked.getKey().getKey(),
                        "&7此附魔不會遭到坍縮降級",
                        "&e點擊切換下一項附魔"));
    }

    private void refreshQuantumMenu(BlockMenu menu) {
        menu.replaceExistingItem(INFO_SLOT, createGuideItem(menu));
        refreshLockItem(menu, menu.getItemInSlot(TARGET_SLOT));
        QuantumState state = quantumStates.get(menu.getLocation());
        ItemStack target = menu.getItemInSlot(TARGET_SLOT);
        ItemStack sacrifice = menu.getItemInSlot(SACRIFICE_SLOT);
        if (state != null && state.matches(target, sacrifice)) {
            showQuantumPreviews(menu, state);
        } else {
            hideQuantumPreviews(menu);
        }
    }

    private void showQuantumPreviews(BlockMenu menu, QuantumState state) {
        int nextDepth = Math.min(MAX_OBSERVATIONS, state.observations + 1);
        int gainChance = (int) Math.round((0.62 - (nextDepth - 1) * 0.12) * 100);
        int collapseChance = (int) Math.round((0.08 + (nextDepth - 1) * 0.12) * 100);
        menu.replaceExistingItem(
                PREVIEW_SLOTS[0],
                new CustomItemStack(
                        Material.LIME_STAINED_GLASS_PANE,
                        "&a收束並取回",
                        "&7目前深度：&e" + state.observations + "&7／" + MAX_OBSERVATIONS,
                        "&7消耗：&d1 個量子觀測核心",
                        "&7不再改變目前的隱藏成果",
                        "&e點擊立即完成量化"));
        menu.replaceExistingItem(
                PREVIEW_SLOTS[1],
                new CustomItemStack(
                        Material.PURPLE_STAINED_GLASS_PANE,
                        "&d深入第 " + nextDepth + " 層觀測",
                        "&7消耗：&d1 個量子觀測核心",
                        "&a共振增幅：" + gainChance + "%",
                        "&6附魔反噬：30%",
                        "&c附魔坍縮：" + collapseChance + "%",
                        nextDepth >= MAX_OBSERVATIONS ? "&4本次操作後將強制收束" : "&e點擊承受本層風險"));
        menu.replaceExistingItem(
                PREVIEW_SLOTS[2],
                new CustomItemStack(
                        Material.RED_STAINED_GLASS_PANE,
                        "&c奇點過載",
                        "&7消耗：&d4 個量子觀測核心",
                        "&7成功率會隨附魔飽和度下降至 10%",
                        "&a成功：一次進行 5～8 次高幅增幅",
                        "&4失敗：大部分未鎖定附魔坍縮並追加反噬",
                        "&c點擊後無論結果都會強制收束"));
        menu.replaceExistingItem(INFO_SLOT, createGuideItem(menu));
    }

    private void hideQuantumPreviews(BlockMenu menu) {
        for (int i = 0; i < PREVIEW_SLOTS.length; i++) {
            menu.replaceExistingItem(PREVIEW_SLOTS[i], EMPTY_PREVIEWS[i]);
        }
    }

    private void invalidateQuantumState(BlockMenu menu) {
        quantumStates.remove(menu.getLocation());
        menu.replaceExistingItem(INFO_SLOT, createGuideItem(menu));
        hideQuantumPreviews(menu);
    }

    private ItemStack createGuideItem(BlockMenu menu) {
        ItemStack guide = INFO_ITEM.clone();
        ItemMeta meta = guide.getItemMeta();
        if (meta == null) return guide;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        QuantumState state = quantumStates.get(menu.getLocation());
        if (state != null) {
            lore.add(AddUtils.resolveColor(""));
            lore.add(AddUtils.resolveColor("&6目前觀測深度：&e" + state.observations + "&7／" + MAX_OBSERVATIONS));
            lore.add(AddUtils.resolveColor("&c附魔結果仍處於遮蔽狀態"));
        }
        meta.setLore(lore);
        guide.setItemMeta(meta);
        return guide;
    }
}
