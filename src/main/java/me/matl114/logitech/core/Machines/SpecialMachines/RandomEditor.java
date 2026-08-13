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
import me.matl114.logitech.core.Blocks.Laser;
import me.matl114.logitech.core.Blocks.MultiBlock.FinalAltarCore;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.utils.AddUtils;
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
    private static final int[] INPUT_SLOTS = new int[] {TARGET_SLOT, SACRIFICE_SLOT};
    private static final int[] OUTPUT_SLOTS = new int[] {RESULT_SLOT};
    private static final int[] PREVIEW_SLOTS = new int[] {10, 13, 16};
    private static final int LOCK_SLOT = 31;
    private static final String LOCK_KEY = "quantum-lock";
    private static final String COHERENCE_KEY = "quantum-coherence";
    private final Map<Location, QuantumState> quantumStates = new ConcurrentHashMap<>();

    private record QuantumState(ItemStack target, ItemStack sacrifice, ItemStack[] previews, boolean singularityCollapsed) {
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
            "&6量子三重態－操作說明",
            "&e① &7左下放入 &b1 件目標物品",
            "&e② &7右下放入 &d1 件帶有附魔的祭品",
            "&8　&7一般附魔物品與附魔書都可以使用",
            "&e③ &7等待上方生成三個可預覽結果",
            "&e④ &7點擊想要的結果，完成量化",
            "",
            "&a穩定態 &7－少量強化，沒有附魔坍縮",
            "&d畸變態 &7－一次降級後，獲得多次強化",
            "&c奇點態 &7－強化次數最多，但有 65% 坍縮機率",
            "",
            "&6附魔鎖定：&7點擊下方鐵柵欄切換附魔",
            "&7被鎖定的附魔不會遭到降級或坍縮",
            "&6相干保底：&7奇點連續坍縮 3 次後",
            "&7下一次奇點態必定不會坍縮",
            "",
            "&e只有選定結果時才會消耗祭品與電力",
            "&c輸出槽必須保持空白才能完成量化");
    protected final ItemStack LOCK_NONE = new CustomItemStack(
            Material.IRON_BARS, "&6附魔鎖定", "&7目前沒有鎖定附魔", "&e點擊循環選擇要保護的附魔");
    protected final ItemStack[] EMPTY_PREVIEWS = new ItemStack[] {
        new CustomItemStack(
                Material.LIME_STAINED_GLASS_PANE,
                "&a穩定態－等待計算",
                "&7少量強化，不會發生附魔坍縮",
                "&8放入目標物品與附魔祭品後生成預覽"),
        new CustomItemStack(
                Material.PURPLE_STAINED_GLASS_PANE,
                "&d畸變態－等待計算",
                "&7一項未鎖定附魔可能降低 1 級",
                "&7之後會獲得多次隨機強化",
                "&8放入目標物品與附魔祭品後生成預覽"),
        new CustomItemStack(
                Material.RED_STAINED_GLASS_PANE,
                "&c奇點態－等待計算",
                "&7強化次數最多，但有 65% 坍縮機率",
                "&7坍縮會使約一半未鎖定附魔減半或消失",
                "&8放入目標物品與附魔祭品後生成預覽")
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
    protected final int MAX_UPGRADE_ONE_TIME = 3;
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
                            "&f機制 - &c量子三重態",
                            "&7左下放入一件目標物品，右下放入一件附魔祭品",
                            "&7一般附魔物品與附魔書都能作為祭品",
                            "&7機器會生成三種可預覽的量化結果",
                            "&a穩定態：低風險、小幅強化",
                            "&d畸變態：附魔重新分配並獲得多次強化",
                            "&c奇點態：大幅強化，也可能發生嚴重坍縮",
                            "&7點擊其中一個預覽，才會消耗祭品與電力",
                            "&7選定的結果會出現在中央輸出槽",
                            "&7僅支援說明中列出的附魔，每種附魔都有獨立上限"),
                    resultDisplay,
                    AddUtils.getInfoShow(
                            "&f機制 - &c相干與鎖定",
                            "&7坍縮會累積相干值，連續三次後保證不再坍縮",
                            "&7可鎖定目標物品的一項附魔，使其不會遭到降級",
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
            if (slot != TARGET_SLOT && slot != SACRIFICE_SLOT && slot != RESULT_SLOT) {
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
                        "&7左側放入附魔物品或附魔書",
                        "&7祭品的附魔總等級越高，強化次數越多",
                        "&e每次量化只消耗 1 件祭品"),
                ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void newMenuInstance(@Nonnull BlockMenu menu, @Nonnull Block block) {
        menu.addMenuOpeningHandler(player -> refreshQuantumMenu(menu));
        menu.addMenuClickHandler(LOCK_SLOT, (player, slot, item, action) -> {
            cycleLockedEnchantment(menu);
            invalidateQuantumState(menu);
            refreshQuantumMenu(menu);
            return false;
        });
        for (int i = 0; i < PREVIEW_SLOTS.length; i++) {
            final int selectedTimeline = i;
            menu.addMenuClickHandler(PREVIEW_SLOTS[i], (player, slot, item, action) -> {
                collapseTimeline(player, block, menu, selectedTimeline);
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

    public void randomEdit(ItemMeta meta) {
        Enchantment[] enchantments = getRegisteredEnchantments();
        if (enchantments.length == 0) return;
        int index = rand.nextInt(enchantments.length);
        int upgrade = rand.nextInt(MAX_UPGRADE_ONE_TIME) + 1;
        Enchantment enchantment = enchantments[index];
        int level = meta.getEnchantLevel(enchantment);
        int limit = getEnchantmentLimit(enchantment);
        if (level < limit) {
            meta.addEnchant(enchantment, Math.min(limit, level + upgrade), true);
        }
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
            if (state != null) {
                invalidateQuantumState(inv);
            }
            return;
        }
        if (state == null || !state.matches(target, sacrifice)) {
            createQuantumState(inv, target, sacrifice);
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
        return sacrificeMeta != null && !getSacrificeEnchantments(sacrificeMeta).isEmpty();
    }

    private void createQuantumState(BlockMenu menu, ItemStack target, ItemStack sacrifice) {
        int lockIndex = DataCache.getCustomData(menu.getLocation(), LOCK_KEY, -1);
        Enchantment locked = getLockedEnchantment(target, lockIndex);
        int coherence = Math.max(0, DataCache.getCustomData(menu.getLocation(), COHERENCE_KEY, 0));
        int sacrificePower = getSacrificeEnchantments(sacrifice.getItemMeta()).values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        ItemStack stable = createBaseTimeline(target, sacrifice);
        applyEdits(stable, 1 + Math.min(2, sacrificePower / 12));

        ItemStack distorted = createBaseTimeline(target, sacrifice);
        redistributeEnchantments(distorted, locked, 1);
        applyEdits(distorted, 3 + Math.min(4, sacrificePower / 8));

        ItemStack singularity = createBaseTimeline(target, sacrifice);
        applyEdits(singularity, 7 + Math.min(8, sacrificePower / 5));
        boolean collapsed = coherence < 3 && rand.nextDouble() < 0.65;
        if (collapsed) {
            collapseEnchantments(singularity, locked);
        }

        ItemStack[] previews = new ItemStack[] {
            addTimelineLore(stable, "&a穩定態", "&7小幅強化，結果最可靠"),
            addTimelineLore(distorted, "&d畸變態", "&7附魔重新分配，獲得多次強化"),
            addTimelineLore(
                    singularity,
                    "&c奇點態",
                    collapsed ? "&c本時間線偵測到附魔坍縮" : "&6本時間線已避開附魔坍縮")
        };
        quantumStates.put(menu.getLocation(), new QuantumState(target.clone(), sacrifice.clone(), previews, collapsed));
        for (int i = 0; i < PREVIEW_SLOTS.length; i++) {
            menu.replaceExistingItem(PREVIEW_SLOTS[i], previews[i]);
        }
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

    private void applyEdits(ItemStack item, int count) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        for (int i = 0; i < count; i++) {
            randomEdit(meta);
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

    private void collapseEnchantments(ItemStack item, Enchantment locked) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<Enchantment> candidates = meta.getEnchants().keySet().stream()
                .filter(RandomEditor::isSupportedEnchantment)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        candidates.remove(locked);
        candidates.sort(Comparator.comparingInt(meta::getEnchantLevel).reversed());
        int affected = Math.max(1, candidates.size() / 2);
        for (int i = 0; i < affected && i < candidates.size(); i++) {
            Enchantment enchantment = candidates.get(i);
            int level = meta.getEnchantLevel(enchantment);
            if (level <= 2) meta.removeEnchant(enchantment);
            else meta.addEnchant(enchantment, Math.max(1, level / 2), true);
        }
        item.setItemMeta(meta);
    }

    private ItemStack addTimelineLore(ItemStack item, String title, String description) {
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(AddUtils.resolveColor(""));
        lore.add(AddUtils.resolveColor(title));
        lore.add(AddUtils.resolveColor(description));
        lore.add(AddUtils.resolveColor("&e點擊選擇此時間線"));
        meta.setLore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private void collapseTimeline(Player player, Block block, BlockMenu menu, int timeline) {
        QuantumState state = quantumStates.get(menu.getLocation());
        ItemStack target = menu.getItemInSlot(TARGET_SLOT);
        ItemStack sacrifice = menu.getItemInSlot(SACRIFICE_SLOT);
        if (menu.getItemInSlot(RESULT_SLOT) != null) {
            AddUtils.sendMessage(player, "&c請先清空中央的輸出槽");
            return;
        }
        if (!isValidInput(target, sacrifice)) {
            AddUtils.sendMessage(player, "&c請放入 1 件目標物品，以及 1 件帶有附魔的祭品");
            return;
        }
        if (state == null || !state.matches(target, sacrifice)) {
            AddUtils.sendMessage(player, "&e輸入內容已改變，正在重新計算三條時間線");
            invalidateQuantumState(menu);
            return;
        }
        SlimefunBlockData data = DataCache.safeLoadBlock(block.getLocation());
        if (!FinalFeature.isFinalAltarCharged(this, data)) {
            AddUtils.sendMessage(player, "&c機器尚未獲得終極祭壇與四座宏激光發射器充能");
            return;
        }
        if (!conditionHandle(block, menu)) {
            AddUtils.sendMessage(player, "&c機器電力不足，無法固定這條時間線");
            return;
        }
        ItemStack result = state.previews()[timeline].clone();
        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta != null && resultMeta.hasLore()) {
            List<String> lore = new ArrayList<>(resultMeta.getLore());
            if (lore.size() >= 4) lore.subList(lore.size() - 4, lore.size()).clear();
            resultMeta.setLore(lore.isEmpty() ? null : lore);
            result.setItemMeta(resultMeta);
        }
        menu.replaceExistingItem(RESULT_SLOT, result);
        menu.replaceExistingItem(TARGET_SLOT, null);
        sacrifice.setAmount(sacrifice.getAmount() - 1);
        if (sacrifice.getAmount() <= 0) menu.replaceExistingItem(SACRIFICE_SLOT, null);
        if (timeline == 2) {
            DataCache.setCustomData(
                    menu.getLocation(), COHERENCE_KEY, state.singularityCollapsed()
                            ? Math.min(3, DataCache.getCustomData(menu.getLocation(), COHERENCE_KEY, 0) + 1)
                            : 0);
        }
        progressorCost(block, menu);
        invalidateQuantumState(menu);
        String[] names = new String[] {"&a穩定態", "&d畸變態", "&c奇點態"};
        AddUtils.sendMessage(player, "&a量化完成！已固定「" + names[timeline] + "&a」時間線");
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
        if (state == null) {
            for (int i = 0; i < PREVIEW_SLOTS.length; i++) {
                menu.replaceExistingItem(PREVIEW_SLOTS[i], EMPTY_PREVIEWS[i]);
            }
        }
    }

    private void invalidateQuantumState(BlockMenu menu) {
        quantumStates.remove(menu.getLocation());
        menu.replaceExistingItem(INFO_SLOT, createGuideItem(menu));
        for (int i = 0; i < PREVIEW_SLOTS.length; i++) {
            menu.replaceExistingItem(PREVIEW_SLOTS[i], EMPTY_PREVIEWS[i]);
        }
    }

    private ItemStack createGuideItem(BlockMenu menu) {
        int coherence = Math.max(0, Math.min(3, DataCache.getCustomData(menu.getLocation(), COHERENCE_KEY, 0)));
        ItemStack guide = INFO_ITEM.clone();
        ItemMeta meta = guide.getItemMeta();
        if (meta == null) return guide;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(AddUtils.resolveColor(""));
        lore.add(AddUtils.resolveColor("&6目前相干值：&e" + coherence + "&7／3"));
        lore.add(AddUtils.resolveColor(coherence >= 3
                ? "&a下一次奇點態已觸發保底，不會坍縮"
                : "&7奇點態再坍縮 &e" + (3 - coherence) + " &7次即可觸發保底"));
        meta.setLore(lore);
        guide.setItemMeta(meta);
        return guide;
    }
}
