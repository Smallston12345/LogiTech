package me.matl114.logitech.core.Machines.SpecialMachines;

import com.google.common.collect.Multimap;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import me.matl114.logitech.core.Blocks.MultiBlock.SmithWorkShop.SmithingInterface;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.MenuClass.DataMenuClickHandler;
import me.matl114.logitech.utils.UtilClass.MultiBlockClass.MultiBlockService;
import me.matl114.matlib.utils.version.VersionedAttribute;
import me.matl114.matlib.utils.version.VersionedRegistry;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import net.guizhanss.guizhanlib.minecraft.helper.attribute.AttributeHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class AttributeOperator extends SmithingInterface {
    private static final Set<String> RESTRICTED_ATTRIBUTES = FeatureConfig
            .getStringList("ATTRIBUTE_OPERATOR.blocked-attributes")
            .stream()
            .map(value -> value.toUpperCase(Locale.ROOT))
            .collect(Collectors.toUnmodifiableSet());

    private static boolean isRestrictedAttribute(Attribute attribute) {
        if (attribute == null) return false;
        String key = attribute.getKey().getKey().toUpperCase(Locale.ROOT);
        return RESTRICTED_ATTRIBUTES.contains(key)
                || RESTRICTED_ATTRIBUTES.contains(attribute.toString().toUpperCase(Locale.ROOT));
    }

    /** 是否只允許同類型物品之間轉移 */
    private static final boolean REQUIRE_SAME_CATEGORY =
            FeatureConfig.getBoolean("ATTRIBUTE_OPERATOR.require-same-item-category", true);
    /** 書本/附魔書是否可作為任意類型的轉移對象(附魔書機制依賴此項) */
    private static final boolean ALLOW_BOOK_TRANSFER =
            FeatureConfig.getBoolean("ATTRIBUTE_OPERATOR.allow-book-transfer", true);
    /** 每次轉移附魔的等級衰減量 */
    private static final int ENCHANT_LEVEL_DECAY =
            Math.max(0, FeatureConfig.getInt("ATTRIBUTE_OPERATOR.enchant-level-decay", 1));
    /** 轉移失敗機率(百分比) */
    private static final double TRANSFER_FAIL_CHANCE =
            Math.max(0.0, Math.min(100.0, FeatureConfig.getDouble("ATTRIBUTE_OPERATOR.transfer-fail-chance", 20.0)));
    /** 轉移失敗時,被轉移者身上的附魔/屬性是否照樣消失 */
    private static final boolean FAIL_LOSES_SOURCE =
            FeatureConfig.getBoolean("ATTRIBUTE_OPERATOR.fail-loses-source", true);

    private static final String[] CATEGORY_SUFFIXES =
            new String[] {"_PICKAXE", "_SWORD", "_AXE", "_SHOVEL", "_HOE", "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS"};

    /** 取得物品的"類型",同類型的物品之間才允許轉移 */
    public static String getItemCategory(Material material) {
        String name = material.toString();
        if ("BOOK".equals(name) || "ENCHANTED_BOOK".equals(name)) {
            return "BOOK";
        }
        for (String suffix : CATEGORY_SUFFIXES) {
            if (name.endsWith(suffix)) {
                return suffix.substring(1);
            }
        }
        // 其餘物品(弓/弩/三叉戟/重錘/盾牌/鞘翅/釣竿...)各自成類,只能與同種材質互轉
        return name;
    }

    /** 檢查兩個物品是否允許互相轉移 */
    public static boolean canTransferBetween(Material from, Material to) {
        if (!REQUIRE_SAME_CATEGORY) {
            return true;
        }
        String c1 = getItemCategory(from);
        String c2 = getItemCategory(to);
        if (ALLOW_BOOK_TRANSFER && ("BOOK".equals(c1) || "BOOK".equals(c2))) {
            return true;
        }
        return c1.equals(c2);
    }

    /** 套用等級衰減 */
    private static int decayLevel(int level) {
        if (ENCHANT_LEVEL_DECAY <= 0) {
            return level;
        }
        return Math.max(1, level - ENCHANT_LEVEL_DECAY);
    }

    private static boolean rollFailed() {
        return TRANSFER_FAIL_CHANCE > 0.0 && ThreadLocalRandom.current().nextDouble() * 100.0 < TRANSFER_FAIL_CHANCE;
    }
    protected final int[] BORDER = new int[] {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 12, 14, 15, 18, 19, 25, 26, 29, 30, 32, 33, 36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    protected final int[] INPUT_SLOT = new int[] {10, 28};
    protected final int[] OUTPUT_SLOT = new int[] {16, 34};
    protected final int[] OUTPUT_BORDER = new int[] {17, 35};

    protected final int TRANSFER_ALLENCHANT_SLOT = 20;
    protected final int TRANSFER_ALLATTRIBUTE_SLOT = 24;
    protected final int TRANSFER_ONEENCHANT_SLOT = 21;
    protected final int TRANSFER_ONEATTRIBUTE_SLOT = 23;
    protected final int CHOOSE_ENCHANT_SLOT = 13;
    protected final int CHOOSE_ATTRIBUTE_SLOT = 31;
    protected final int FORCE_ENCHANT_SLOT = 22;
    protected final int INPUT_ONE_INFO_SLOT = 9;
    protected final ItemStack INPUT_ONE_INFO_ITEM =
            new CustomItemStack(Material.PURPLE_STAINED_GLASS_PANE, "&6被轉移者輸入槽", "&7在此放入即將被轉移的物品", "&7一次最多隻能消耗一個");
    protected final int INPUT_TWO_INFO_SLOT = 27;
    protected final ItemStack INPUT_TWO_INFO_ITEM = new CustomItemStack(
            Material.PURPLE_STAINED_GLASS_PANE, "&6轉移目標輸入槽", "&7可以放入轉移的目標裝備,或者附魔書,或者書", "&7物品一次最多消耗一個");
    protected final ItemStack TRANSFER_ALLENCHANT_ITEM = new CustomItemStack(
            Material.ORANGE_STAINED_GLASS_PANE, "&6轉移所有附魔", "&7點擊轉移一次", "&7shift點擊切換是否自動運行", "&7自動運行: &c否");
    protected final ItemStack TRANSFER_ALLENCHANT_ITEM_ON = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE,
            "&6轉移所有附魔",
            "&7點擊轉移一次",
            "&7shift點擊切換是否自動運行",
            "&7自動運行: &a是",
            "&e當自動運行時禁止進行其他操作!");
    protected final ItemStack TRANSFER_ALLATTRIBUTE_ITEM = new CustomItemStack(
            Material.ORANGE_STAINED_GLASS_PANE, "&6轉移所有屬性", "&7點擊轉移一次", "&7shift點擊切換是否自動運行", "&7自動運行: &c否");
    protected final ItemStack TRANSFER_ALLATTRIBUTE_ITEM_ON = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE,
            "&6轉移所有屬性",
            "&7點擊轉移一次",
            "&7shift點擊切換是否自動運行",
            "&7自動運行: &a是",
            "&e當自動運行時禁止進行其他操作!");
    protected final ItemStack TRANSFER_ONEENCHANT_ITEM =
            new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6轉移單一附魔", "&7點擊轉移一次", "&7使用上方的按鈕切換選中的附魔");
    protected final ItemStack TRANSFER_ONEATTRIBUTE_ITEM =
            new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6轉移單一屬性", "&7點擊轉移一次", "&7使用下方的按鈕切換選中的屬性");
    protected final ItemStack CHOOSE_ENCHANT_ITEM =
            new CustomItemStack(Material.ENCHANTED_BOOK, "&6選擇你的附魔", "&7左鍵點擊順序切換", "&7右鍵點擊逆序切換", "&8⇨ &c當前暫無附魔");
    protected final ItemStack CHOOSE_ATTRIBUTE_ITEM =
            new CustomItemStack(Material.DIAMOND_CHESTPLATE, "&6選擇你的附魔", "&7左鍵點擊順序切換", "&7右鍵點擊逆序切換", "&8⇨ &c當前暫無屬性");
    protected final ItemStack FORCE_ENCHANT_ITEM_OFF = new CustomItemStack(
            Material.RED_STAINED_GLASS_PANE,
            "&6強制附魔",
            "&7當前模式: &c關",
            "&7關閉強制附魔將會在轉移附魔時將書本轉移為附魔書",
            "&7或在附魔書的已有附魔存儲中加入轉移的附魔",
            "&e開啓則會強制附魔書本和附魔書,並不會加入附魔存儲");
    protected final ItemStack FORCE_ENCHANT_ITEM_ON = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE,
            "&6強制附魔",
            "&7當前模式: &a開",
            "&7關閉強制附魔將讀取附魔書中存儲的附魔",
            "&7若目標物品為書本,則會自動轉為附魔書",
            "&e開啓則會強制附魔書本和附魔書,並不會加入附魔存儲");
    protected final ItemStack OUTPUT_BORDER_ITEM =
            new CustomItemStack(Material.PINK_STAINED_GLASS_PANE, "&6輸出槽", "&7對應輸入槽的輸出口");
    protected final int STATUS_SLOT = 4;
    protected final ItemStack STATUS_ON =
            new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&6運行中", "&7成功接入多方塊結構");
    protected final ItemStack NO_MULTIBLOCK_ITEM =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c待機中", "&7沒有接入多方塊結構");
    protected final ItemSetting<Boolean> CAN_TRANSFER_STACK_ABLE_ITEM = create("enable-stackable-item", true);

    public AttributeOperator(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energyBuffer,
            int energyConsumption) {
        super(category, item, recipeType, recipe, energyBuffer, energyConsumption, false);
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                                "&f機制",
                                "&7支援在物品之間轉移屬性增幅符和附魔",
                                "&7分別可以選擇轉移單項附魔/增幅符或者轉移全部附魔/增幅符",
                                "&7單項選擇器將顯示在選單中,可以選擇單項附魔/增幅符",
                                "&7點擊後消耗電力執行轉換",
                                "&c僅能在相同類型的物品之間轉移",
                                "&7例如:劍→劍、鎬→鎬、頭盔→頭盔",
                                "&7書本與附魔書不受物品類型限制",
                                "&c每次轉移後,附魔等級降低1級",
                                "&7附魔等級最低保留1級,不會降至0級",
                                "&c每次轉移有20%機率失敗",
                                "&7失敗時,目標物品不會獲得附魔/屬性",
                                "&7來源物品的附魔/屬性仍會被移除"),
                        null,
                AddUtils.getInfoShow(
                                "&f機制",
                                "&7該機器提供\"強制附魔\"機制",
                                "&7當啟用時,對於附魔書和書本,將會直接讀取其附魔列表",
                                "&7而非其存儲的附魔列表",
                                "&e啟用該機制通常得不到正常玩家想要的結果",
                                "&7當關閉該機制時,對於附魔書和書本",
                                "&7當附魔書作為被轉移物品時,會把書中存儲的附魔附加至目標物品",
                                "&7當普通書本作為目標物品時,會自動轉為附魔書",
                                "&7當附魔書作為目標物品時,會把被轉移物品的附魔列表添加到存儲的附魔中"),
                        null));
    }

    public void addInfo(ItemStack item) {
        if (this.energyConsumption > 0) {
            item.setItemMeta(AddUtils.workBenchInfoAdd(item, this.energybuffer, this.energyConsumption)
                    .getItemMeta());
        }
    }

    public DataMenuClickHandler createDataHolder() {
        return new DataMenuClickHandler() {
            // 0 為 數量 1 為 電力
            int[] intdata = new int[2];
            Object[] object = new Object[2];
            ItemStack itemStacks = null;

            public int getInt(int i) {
                return intdata[i];
            }

            public void setInt(int i, int val) {
                intdata[i] = val;
            }

            public ItemStack getItemStack(int val) {
                return itemStacks;
            }

            public void setItemStack(int val, ItemStack stack) {
                itemStacks = stack;
            }

            public Object getObject(int i) {
                return object[i];
            }

            public void setObject(int i, Object obj) {
                object[i] = obj;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    public final int DATA_SLOT = 0;

    public DataMenuClickHandler getDataHolder(Block b, BlockMenu inv) {
        ChestMenu.MenuClickHandler handler = inv.getMenuClickHandler(DATA_SLOT);
        if (handler instanceof DataMenuClickHandler dh) {
            return dh;
        } else {
            DataMenuClickHandler dh = createDataHolder();
            inv.addMenuClickHandler(DATA_SLOT, dh);
            return dh;
        }
    }

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    @Override
    public void processInterface(Block b, BlockMenu inv, SlimefunBlockData data, Location coreLocation, int speed) {
        if (inv.hasViewer()) {
            inv.replaceExistingItem(STATUS_SLOT, STATUS_ON);
            updateMenu(inv, b, Settings.RUN);
        }
        int code = getAutoTransferCode(inv);
        if (code >= 0) {
            Schedules.launchSchedules(
                    () -> {
                        refreshInputItemData(inv, getDataHolder(b, inv), true);
                        runTransferItemData(inv, code, true, null, speed + 1);
                    },
                    0,
                    false,
                    0);
        }
    }

    public void processFailed(Block b, BlockMenu inv, SlimefunBlockData data) {
        if (inv.hasViewer()) {
            inv.replaceExistingItem(STATUS_SLOT, NO_MULTIBLOCK_ITEM);
        }
    }

    public boolean online(BlockMenu inv) {
        return MultiBlockService.getStatus(inv.getLocation()) != 0;
    }

    public String getEnchantDisplay(Enchantment ench) {
        return "minecraft".equals(ench.getKey().getNamespace())
                ? ench.getKey().getKey()
                : ench.getKey().toString();
    }

    protected String AUTOKEY_E = "ate";
    protected String AUTOKEY_A = "ata";
    protected String FORCEKEY = "fe";

    public void newMenuInstance(@Nonnull BlockMenu inv, @Nonnull Block block) {
        SlimefunBlockData data = DataCache.safeLoadBlock(inv.getLocation());
        DataMenuClickHandler handler = getDataHolder(block, inv);
        inv.addMenuOpeningHandler(player -> updateMenu(inv, block, Settings.RUN));
        inv.addMenuCloseHandler(player -> updateMenu(inv, block, Settings.RUN));

        inv.addMenuClickHandler(TRANSFER_ALLENCHANT_SLOT, ((player, i, itemStack, clickAction) -> {
            if (!online(inv)) {
                AddUtils.sendMessage(player, "&c當前機器並未接入多方塊結構");
                return false;
            }
            int code = getAutoTransferCode(inv);
            if (clickAction.isShiftClicked()) {
                if (code == 0 || code == 2) {
                    DataCache.setCustomString(inv.getLocation(), AUTOKEY_E, "");
                    inv.replaceExistingItem(TRANSFER_ALLENCHANT_SLOT, TRANSFER_ALLENCHANT_ITEM);
                } else {
                    DataCache.setCustomString(inv.getLocation(), AUTOKEY_E, "1");
                    inv.replaceExistingItem(TRANSFER_ALLENCHANT_SLOT, TRANSFER_ALLENCHANT_ITEM_ON);
                }
                AddUtils.sendMessage(player, "&a成功切換自動運行");
                return false;
            }
            if (code >= 0) {

                AddUtils.sendMessage(player, "&c當前處於自動模式,禁止進行任何其餘操作");
                return false;
            }
            runTransferItemData(inv, 0, true, (str) -> AddUtils.sendMessage(player, str));

            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        inv.addMenuClickHandler(TRANSFER_ALLATTRIBUTE_SLOT, ((player, i, itemStack, clickAction) -> {
            if (!online(inv)) {
                AddUtils.sendMessage(player, "&c當前機器並未接入多方塊結構");
                return false;
            }
            int code = getAutoTransferCode(inv);
            if (clickAction.isShiftClicked()) {
                if (code == 1 || code == 2) {
                    DataCache.setCustomString(inv.getLocation(), AUTOKEY_A, "");
                    inv.replaceExistingItem(TRANSFER_ALLATTRIBUTE_SLOT, TRANSFER_ALLATTRIBUTE_ITEM);
                } else {
                    DataCache.setCustomString(inv.getLocation(), AUTOKEY_A, "1");
                    inv.replaceExistingItem(TRANSFER_ALLATTRIBUTE_SLOT, TRANSFER_ALLATTRIBUTE_ITEM_ON);
                }
                AddUtils.sendMessage(player, "&a成功切換自動運行");
                return false;
            }
            if (code >= 0) {

                AddUtils.sendMessage(player, "&c當前處於自動模式,禁止進行任何其餘操作");
                return false;
            }
            runTransferItemData(inv, 1, true, (str) -> AddUtils.sendMessage(player, str));
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        inv.replaceExistingItem(TRANSFER_ONEENCHANT_SLOT, TRANSFER_ONEENCHANT_ITEM);
        inv.addMenuClickHandler(TRANSFER_ONEENCHANT_SLOT, ((player, i, itemStack, clickAction) -> {
            if (!online(inv)) {
                AddUtils.sendMessage(player, "&c當前機器並未接入多方塊結構");
                return false;
            }
            if (getAutoTransferCode(inv) >= 0) {
                AddUtils.sendMessage(player, "&c當前處於自動模式,禁止進行任何其餘操作");
                return false;
            }
            runTransferItemData(inv, 0, false, (str) -> AddUtils.sendMessage(player, str));
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        inv.replaceExistingItem(TRANSFER_ONEATTRIBUTE_SLOT, TRANSFER_ONEATTRIBUTE_ITEM);
        inv.addMenuClickHandler(TRANSFER_ONEATTRIBUTE_SLOT, ((player, i, itemStack, clickAction) -> {
            if (!online(inv)) {
                AddUtils.sendMessage(player, "&c當前機器並未接入多方塊結構");
                return false;
            }
            if (getAutoTransferCode(inv) >= 0) {
                AddUtils.sendMessage(player, "&c當前處於自動模式,禁止進行任何其餘操作");
                return false;
            }
            runTransferItemData(inv, 1, false, (str) -> AddUtils.sendMessage(player, str));
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        inv.addMenuClickHandler(FORCE_ENCHANT_SLOT, ((player, i, itemStack, clickAction) -> {
            if (!online(inv)) {
                AddUtils.sendMessage(player, "&c當前機器並未接入多方塊結構");
                return false;
            }
            if (itemStack != null && itemStack.getType() == Material.RED_STAINED_GLASS_PANE) {
                DataCache.setCustomString(inv.getLocation(), FORCEKEY, "1");
                inv.replaceExistingItem(FORCE_ENCHANT_SLOT, FORCE_ENCHANT_ITEM_ON);
            } else {
                DataCache.setCustomString(inv.getLocation(), FORCEKEY, "");
                inv.replaceExistingItem(FORCE_ENCHANT_SLOT, FORCE_ENCHANT_ITEM_OFF);
            }
            updateMenu(inv, block, Settings.INIT);
            return false;
        }));
        inv.replaceExistingItem(CHOOSE_ATTRIBUTE_SLOT, CHOOSE_ATTRIBUTE_ITEM);
        inv.addMenuClickHandler(CHOOSE_ATTRIBUTE_SLOT, ((player, i, itemStack, clickAction) -> {
            if (!online(inv)) {
                AddUtils.sendMessage(player, "&c當前機器並未接入多方塊結構");
                return false;
            }
            if (getAutoTransferCode(inv) >= 0) {
                AddUtils.sendMessage(player, "&c當前處於自動模式,禁止進行任何其餘操作");
                return false;
            }
            DataMenuClickHandler dh = getDataHolder(block, inv);
            dh.setInt(1, dh.getInt(1) + (clickAction.isRightClicked() ? -1 : 1));
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        inv.replaceExistingItem(CHOOSE_ENCHANT_SLOT, CHOOSE_ENCHANT_ITEM);
        inv.addMenuClickHandler(CHOOSE_ENCHANT_SLOT, ((player, i, itemStack, clickAction) -> {
            if (!online(inv)) {
                AddUtils.sendMessage(player, "&c當前機器並未接入多方塊結構");
                return false;
            }
            if (getAutoTransferCode(inv) >= 0) {
                AddUtils.sendMessage(player, "&c當前處於自動模式,禁止進行任何其餘操作");
                return false;
            }
            DataMenuClickHandler dh = getDataHolder(block, inv);
            dh.setInt(0, dh.getInt(0) + (clickAction.isRightClicked() ? -1 : 1));
            updateMenu(inv, block, Settings.RUN);
            return false;
        }));
        int autoE = DataCache.getCustomString(data, AUTOKEY_E, "").length();
        int autoA = DataCache.getCustomString(data, AUTOKEY_A, "").length();
        int force = DataCache.getCustomString(data, FORCEKEY, "").length();
        if (autoE == 1) {
            inv.replaceExistingItem(TRANSFER_ALLENCHANT_SLOT, TRANSFER_ALLENCHANT_ITEM_ON);
        } else {
            inv.replaceExistingItem(TRANSFER_ALLENCHANT_SLOT, TRANSFER_ALLENCHANT_ITEM);
        }
        if (autoA == 1) {
            inv.replaceExistingItem(TRANSFER_ALLATTRIBUTE_SLOT, TRANSFER_ALLATTRIBUTE_ITEM_ON);
        } else {
            inv.replaceExistingItem(TRANSFER_ALLATTRIBUTE_SLOT, TRANSFER_ALLATTRIBUTE_ITEM);
        }
        if (force == 1) {
            inv.replaceExistingItem(FORCE_ENCHANT_SLOT, FORCE_ENCHANT_ITEM_ON);
        } else {
            inv.replaceExistingItem(FORCE_ENCHANT_SLOT, FORCE_ENCHANT_ITEM_OFF);
        }
        updateMenu(inv, block, Settings.INIT);
    }

    public int getAutoTransferCode(BlockMenu inv) {
        ItemStack enchantAutoFlag = inv.getItemInSlot(TRANSFER_ALLENCHANT_SLOT);
        boolean runEnchant = enchantAutoFlag != null && enchantAutoFlag.getType() == Material.GREEN_STAINED_GLASS_PANE;
        ItemStack attributeAutoFlag = inv.getItemInSlot(TRANSFER_ALLATTRIBUTE_SLOT);
        boolean runAttribute =
                attributeAutoFlag != null && attributeAutoFlag.getType() == Material.GREEN_STAINED_GLASS_PANE;
        int code = -1 + (runEnchant ? 1 : 0) + (runAttribute ? 2 : 0);
        return code;
    }

    public void updateMenu(BlockMenu inv, Block b, Settings mod) {
        DataMenuClickHandler handler = getDataHolder(b, inv);
        refreshInputItemData(inv, handler, mod == Settings.INIT);
        int indexE = handler.getInt(0);
        int indexA = handler.getInt(1);
        Object obj1 = handler.getObject(0);
        try {
            if (obj1 != null) {
                Map<Enchantment, Integer> emp = (Map<Enchantment, Integer>) obj1;
                if (!emp.isEmpty()) {
                    indexE = (indexE + emp.size()) % emp.size();
                    handler.setInt(0, indexE);
                }
                getEnchantmentShow(inv, emp, indexE);
            } else {
                inv.replaceExistingItem(CHOOSE_ENCHANT_SLOT, CHOOSE_ENCHANT_ITEM);
            }
        } catch (Throwable ex) {
            inv.replaceExistingItem(CHOOSE_ENCHANT_SLOT, CHOOSE_ENCHANT_ITEM);
            Debug.debug(ex);
        }
        Object obj2 = handler.getObject(1);
        try {
            if (obj2 != null) {
                Multimap<Attribute, AttributeModifier> emp2 = (Multimap<Attribute, AttributeModifier>) obj2;
                if (!emp2.isEmpty()) {
                    indexA = (indexA + emp2.size()) % emp2.size();
                    handler.setInt(1, indexA);
                }
                getAttributeShow(inv, emp2, indexA);
            } else {
                inv.replaceExistingItem(CHOOSE_ATTRIBUTE_SLOT, CHOOSE_ATTRIBUTE_ITEM);
            }
        } catch (Throwable ex) {
            inv.replaceExistingItem(CHOOSE_ATTRIBUTE_SLOT, CHOOSE_ATTRIBUTE_ITEM);
            Debug.debug(ex);
        }
    }

    public void refreshInputItemData(BlockMenu inv, DataMenuClickHandler handler, boolean forceReload) {
        ItemStack stack1 = inv.getItemInSlot(INPUT_SLOT[0]);
        ItemStack stackCache = handler.getItemStack(0);
        if (forceReload || (!(stack1 != null && CraftUtils.sameCraftItem(stack1, stackCache)))) {
            handler.setItemStack(0, stack1);
            handler.setInt(0, 0);
            handler.setInt(1, 0);
            if (stack1 == null || stack1.getType().isAir()) {
                handler.setObject(0, null);
                handler.setObject(1, null);
            } else {
                ItemStack forceFlag = inv.getItemInSlot(FORCE_ENCHANT_SLOT);
                boolean forceEnchant = forceFlag != null && forceFlag.getType() == Material.GREEN_STAINED_GLASS_PANE;
                ItemMeta meta = stack1.getItemMeta();
                if (!forceEnchant && meta instanceof EnchantmentStorageMeta esm) {
                    handler.setObject(0, esm.getStoredEnchants());
                } else {
                    handler.setObject(0, meta.getEnchants());
                }
                handler.setObject(1, meta.getAttributeModifiers());
            }
        }
    }

    protected List<String> LORES_ENCHANT =
            this.CHOOSE_ENCHANT_ITEM.getItemMeta().getLore().subList(0, 2);
    protected ItemMeta CHOOSE_ENCHANT_ITEM_META = this.CHOOSE_ENCHANT_ITEM.getItemMeta();
    protected List<String> LORES_ATTRIBUTE =
            this.CHOOSE_ATTRIBUTE_ITEM.getItemMeta().getLore().subList(0, 2);
    protected ItemMeta CHOOSE_ATTRIBUTE_ITEM_META = this.CHOOSE_ATTRIBUTE_ITEM.getItemMeta();
    protected String PREFIX_LORE = AddUtils.resolveColor("&8⇨ &c");
    protected String PREFIX_CHOOSEN = AddUtils.resolveColor("&e>> &x&e&b&3&3&e&b");

    public Enchantment getEnchantmentSelected(Map<Enchantment, Integer> map, int index) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        int t = 0;
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            if (index == t) {
                return entry.getKey();
            }
            ++t;
        }
        return null;
    }

    public void getEnchantmentShow(BlockMenu inv, Map<Enchantment, Integer> map, int index) {
        if (map == null || map.isEmpty()) {
            inv.replaceExistingItem(CHOOSE_ENCHANT_SLOT, CHOOSE_ENCHANT_ITEM);
            return;
        }
        List<String> lore = new ArrayList<>(this.LORES_ENCHANT);
        ItemMeta meta = CHOOSE_ENCHANT_ITEM_META.clone();
        int t = 0;
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            lore.add(AddUtils.concat(
                    (t == index) ? PREFIX_CHOOSEN : PREFIX_LORE,
                    getEnchantDisplay(entry.getKey()),
                    " lvl: ",
                    entry.getValue().toString()));
            ++t;
        }
        meta.setLore(lore);
        ItemStack stack = inv.getItemInSlot(CHOOSE_ENCHANT_SLOT);
        if (stack == null || stack.getType().isAir()) {
            stack = new ItemStack(Material.ENCHANTED_BOOK);
            stack.setItemMeta(meta);
            inv.replaceExistingItem(CHOOSE_ATTRIBUTE_SLOT, stack);
        } else stack.setItemMeta(meta);
    }

    public Pair<Attribute, AttributeModifier> getAttributeSelected(
            Multimap<Attribute, AttributeModifier> att, int index) {
        if (att == null || att.isEmpty()) {
            return null;
        }
        int t = 0;
        for (Map.Entry<Attribute, AttributeModifier> entry : att.entries()) {
            if (t == index) {
                return new Pair<>(entry.getKey(), entry.getValue());
            }
            ++t;
        }
        return null;
    }

    public void getAttributeShow(BlockMenu inv, Multimap<Attribute, AttributeModifier> att, int index) {
        if (att == null || att.isEmpty()) {
            inv.replaceExistingItem(CHOOSE_ATTRIBUTE_SLOT, CHOOSE_ATTRIBUTE_ITEM);
            return;
        }
        List<String> lore = new ArrayList<>(this.LORES_ATTRIBUTE);
        ItemMeta meta = CHOOSE_ATTRIBUTE_ITEM_META.clone();
        int t = 0;
        for (Map.Entry<Attribute, AttributeModifier> entry : att.entries()) {
            AttributeModifier mod = entry.getValue();
            String valueString = "";
            switch (mod.getOperation()) {
                case ADD_NUMBER -> valueString = AddUtils.concat(" +", String.valueOf(mod.getAmount()));
                case ADD_SCALAR -> valueString = AddUtils.concat(" +", String.valueOf(mod.getAmount() * 100), "%");
                case MULTIPLY_SCALAR_1 -> valueString =
                        AddUtils.concat(" x", String.valueOf(mod.getAmount() * 100), "%");
            }
            lore.add(AddUtils.concat(
                    (t == index) ? PREFIX_CHOOSEN : PREFIX_LORE, AttributeHelper.getName(entry.getKey()), valueString));
            ++t;
        }
        meta.setLore(lore);
        ItemStack stack = inv.getItemInSlot(CHOOSE_ATTRIBUTE_SLOT);
        if (stack == null || stack.getType().isAir()) {
            stack = new ItemStack(Material.DIAMOND_CHESTPLATE);
            stack.setItemMeta(meta);
            inv.replaceExistingItem(CHOOSE_ATTRIBUTE_SLOT, stack);
        } else stack.setItemMeta(meta);
    }

    public Pair<ItemStack, ItemStack> transferEnchantment(
            ItemStack stack1, ItemStack stack2, Enchantment enchantment, boolean forceEnchant) {
        if (!forceEnchant && stack2.getType() == Material.BOOK) {
            stack2 = new ItemStack(Material.ENCHANTED_BOOK);
        }
        ItemMeta meta1 = stack1.getItemMeta();
        ItemMeta meta2 = stack2.getItemMeta();
        if (meta1 == null || meta2 == null) {
            return null;
        }
        boolean hasChanged = false;
        if (enchantment == null) {
            Map<Enchantment, Integer> ems;
            if (!forceEnchant && meta1 instanceof EnchantmentStorageMeta em) {
                ems = em.getStoredEnchants();
                if (ems == null || ems.isEmpty()) {
                    return null;
                }
                for (Enchantment e : ems.keySet()) {
                    em.removeStoredEnchant(e);
                }
            } else {
                ems = meta1.getEnchants();
                if (ems == null || ems.isEmpty()) {
                    return null;
                }
                for (Enchantment e : ems.keySet()) {
                    meta1.removeEnchant(e);
                }
            }
            if (!forceEnchant && meta2 instanceof EnchantmentStorageMeta esm) {
                for (Map.Entry<Enchantment, Integer> e : ems.entrySet()) {
                    int lvl = decayLevel(e.getValue());
                    if (lvl > esm.getStoredEnchantLevel(e.getKey())) {
                        esm.addStoredEnchant(e.getKey(), lvl, true);
                    }
                    hasChanged = true;
                }
            } else {
                for (Map.Entry<Enchantment, Integer> e : ems.entrySet()) {
                    int lvl = decayLevel(e.getValue());
                    if (lvl > meta2.getEnchantLevel(e.getKey())) {
                        meta2.addEnchant(e.getKey(), lvl, true);
                    }
                    hasChanged = true;
                }
            }

        } else {
            int lvl;
            if (!forceEnchant && meta1 instanceof EnchantmentStorageMeta em) {
                lvl = em.getStoredEnchantLevel(enchantment);
                em.removeStoredEnchant(enchantment);
            } else {
                lvl = meta1.getEnchantLevel(enchantment);
                meta1.removeEnchant(enchantment);
            }
            int newLvl = decayLevel(lvl);
            if (!forceEnchant && meta2 instanceof EnchantmentStorageMeta esm) {
                if (newLvl > esm.getStoredEnchantLevel(enchantment)) {
                    esm.addStoredEnchant(enchantment, newLvl, true);
                }
            } else {
                if (newLvl > meta2.getEnchantLevel(enchantment)) {
                    meta2.addEnchant(enchantment, newLvl, true);
                }
            }
            hasChanged = true;
        }
        if (!hasChanged) {
            return null;
        }
        ItemStack res1 = new ItemStack(stack1.getType(), 1);
        res1.setItemMeta(meta1);
        ItemStack res2 = new ItemStack(stack2.getType(), 1);
        res2.setItemMeta(meta2);
        return new Pair(res1, res2);
    }

    public void runTransferItemData(BlockMenu inv, int mode, boolean transferAll, Consumer<String> outputStream) {
        runTransferItemData(inv, mode, transferAll, outputStream, 1);
    }

    public void runTransferItemData(
            BlockMenu inv, int mode, boolean transferAll, Consumer<String> outputStream, int transferAmount) {
        if (!this.conditionHandle(null, inv)) {
            if (outputStream != null) {
                outputStream.accept("&c電力不足!");
            }
            return;
        }
        ItemStack it3 = inv.getItemInSlot(OUTPUT_SLOT[0]);
        ItemStack it4 = inv.getItemInSlot(OUTPUT_SLOT[1]);
        if (it3 != null || it4 != null) {
            if (outputStream != null) {
                outputStream.accept("&c輸出空間不足!");
            }
            return;
        }
        ItemStack it1 = inv.getItemInSlot(INPUT_SLOT[0]);
        ItemStack it2 = inv.getItemInSlot(INPUT_SLOT[1]);
        if (it1 == null || it1.getType().isAir()) {
            if (outputStream != null) {
                outputStream.accept("&c被轉換的物品不能為空!");
            }
            return;
        }
        if (it2 == null || it2.getType().isAir()) {
            if (outputStream != null) {
                outputStream.accept("&c轉換的目標物品不能為空");
            }
            return;
        }
        if (!canTransferBetween(it1.getType(), it2.getType())) {
            if (outputStream != null) {
                outputStream.accept("&c只能在同類型的物品之間轉移!");
            }
            return;
        }
        ItemStack forceFlag = inv.getItemInSlot(FORCE_ENCHANT_SLOT);
        boolean forceEnchant = forceFlag != null && forceFlag.getType() == Material.GREEN_STAINED_GLASS_PANE;
        if (!CAN_TRANSFER_STACK_ABLE_ITEM.getValue()) {
            // 非force情況可以轉book變成enchant book, force的情況不行
            if (it2.getMaxStackSize() != 1 && (it2.getType() != Material.BOOK || forceEnchant)) {
                if (outputStream != null) {
                    outputStream.accept("&c轉換的目標物品不可以堆疊!");
                }
                return;
            }
        }
        Pair<ItemStack, ItemStack> transferResult = null;
        DataMenuClickHandler handler = getDataHolder(null, inv);
        if (mode == 0) {
            Object e1 = handler.getObject(0);

            try {
                Map<Enchantment, Integer> map = (Map<Enchantment, Integer>) e1;
                if (map != null && !map.isEmpty()) {
                    transferResult = transferEnchantment(
                            it1,
                            it2,
                            (transferAll) ? null : getEnchantmentSelected(map, handler.getInt(0)),
                            forceEnchant);
                } else {
                    if (outputStream != null) {
                        outputStream.accept("&c被轉換的物品不存在附魔");
                    }
                    return;
                }
            } catch (Throwable e) {
                Debug.debug(e);
                if (outputStream != null) {
                    outputStream.accept("&c未知錯誤,請檢查MC版本並聯系作者!");
                }
                return;
            }
        } else if (mode == 1) {
            Object e2 = handler.getObject(1);
            try {
                Multimap<Attribute, AttributeModifier> map = (Multimap<Attribute, AttributeModifier>) e2;
                if (map != null && !map.isEmpty()) {
                    if (transferAll) {
                        transferResult = transferAttributes(it1, it2, null, null);
                    } else {
                        Pair<Attribute, AttributeModifier> att = getAttributeSelected(map, handler.getInt(1));
                        transferResult = transferAttributes(it1, it2, att.getFirstValue(), att.getSecondValue());
                    }
                } else {
                    if (outputStream != null) {
                        outputStream.accept("&c被轉換的物品不存在屬性增幅符");
                    }
                    return;
                }

            } catch (Throwable e) {
                Debug.debug(e);
                if (outputStream != null) {
                    outputStream.accept("&c轉移失敗,該屬性增幅符疑似已在該物品上被註冊過");
                }
                return;
            }
        } else {
            Object e1 = handler.getObject(0);
            Object e2 = handler.getObject(1);
            try {
                Map<Enchantment, Integer> map = (Map<Enchantment, Integer>) e1;
                if (map != null && !map.isEmpty()) {
                    transferResult = transferEnchantment(
                            it1,
                            it2,
                            (transferAll) ? null : getEnchantmentSelected(map, handler.getInt(0)),
                            forceEnchant);
                }
                if (transferResult == null) {
                    return;
                }
                Multimap<Attribute, AttributeModifier> map2 = (Multimap<Attribute, AttributeModifier>) e2;
                if (map2 != null && !map2.isEmpty()) {
                    if (transferAll) {
                        transferResult = transferAttributes(
                                transferResult.getFirstValue(), transferResult.getSecondValue(), null, null);
                    } else {
                        Pair<Attribute, AttributeModifier> att = getAttributeSelected(map2, handler.getInt(1));
                        transferResult = transferAttributes(
                                transferResult.getFirstValue(),
                                transferResult.getSecondValue(),
                                att.getFirstValue(),
                                att.getSecondValue());
                    }
                }
                if (transferResult == null) {
                    return;
                }
            } catch (Throwable e) {
                Debug.debug(e);
                return;
            }
        }
        if (transferResult == null) {
            if (outputStream != null) {
                outputStream.accept("&c轉移失敗!目標物品上已有更高級的附魔/相同屬性值");
            }
            return;
        }
        boolean failed = rollFailed();
        if (failed) {
            // 失敗: 目標物品保持原樣; 來源是否照樣被剝離由設定決定
            ItemStack keptTarget = new ItemStack(it2.getType(), 1);
            keptTarget.setItemMeta(it2.getItemMeta());
            ItemStack sourceResult;
            if (FAIL_LOSES_SOURCE) {
                sourceResult = transferResult.getFirstValue();
            } else {
                sourceResult = new ItemStack(it1.getType(), 1);
                sourceResult.setItemMeta(it1.getItemMeta());
            }
            transferResult = new Pair<>(sourceResult, keptTarget);
        }
        int transfer = Math.min(it1.getAmount(), it2.getAmount());
        transfer = Math.min(transfer, transferAmount);
        inv.replaceExistingItem(OUTPUT_SLOT[0], transferResult.getFirstValue());
        var a = inv.getItemInSlot(OUTPUT_SLOT[0]);
        if (a != null) a.setAmount(transfer);
        inv.replaceExistingItem(OUTPUT_SLOT[1], transferResult.getSecondValue());
        a = inv.getItemInSlot(OUTPUT_SLOT[1]);
        if (a != null) a.setAmount(transfer);
        it1.setAmount(it1.getAmount() - transfer);
        it2.setAmount(it2.getAmount() - transfer);
        if (it1.getAmount() == 0) refreshInputItemData(inv, handler, true);
        progressorCost(null, inv);
        if (outputStream != null) {
            outputStream.accept(failed ? "&c轉移失敗! 屬性在轉移過程中逸散了" : "&a轉移成功!");
        }
    }

    public boolean equalsIgnoreAmount(AttributeModifier it, AttributeModifier mod) {
        boolean slots = VersionedAttribute.getInstance().getAttributeModifierSlot(it) != null
                ? VersionedAttribute.getInstance().getAttributeModifierSlot(it)
                        == VersionedAttribute.getInstance().getAttributeModifierSlot(mod)
                : VersionedAttribute.getInstance().getAttributeModifierSlot(mod) == null;
        if (!slots) {
            return false;
        }
        UUID uid = VersionedAttribute.getInstance().getAttributeModifierUid(it);
        if (uid != null) {
            if (!uid.equals(VersionedAttribute.getInstance().getAttributeModifierUid(mod))) {
                return false;
            }
        }
        return Objects.equals(
                        VersionedAttribute.getInstance().getAttributeModifierName(it),
                        VersionedAttribute.getInstance().getAttributeModifierName(mod))
                && it.getOperation() == mod.getOperation();
    }

    public Pair<ItemStack, ItemStack> transferAttributes(
            ItemStack stack1, ItemStack stack2, Attribute attribute, AttributeModifier modifier) {
        ItemMeta meta1 = stack1.getItemMeta();
        ItemMeta meta2 = stack2.getItemMeta();
        if (meta1 == null || meta2 == null) {
            return null;
        }
        boolean hasChanged = false;
        if (!meta1.hasAttributeModifiers()) {
            return null;
        }
        if (modifier == null) {
            for (Attribute a : VersionedRegistry.getInstance().getAttributes()) {
                if (isRestrictedAttribute(a)) continue;
                Collection<AttributeModifier> mods1 = meta1.getAttributeModifiers(a);
                if (mods1 == null || mods1.isEmpty()) {
                    continue;
                }
                Collection<AttributeModifier> mods2;

                if (meta2.hasAttributeModifiers()) mods2 = meta2.getAttributeModifiers(a);
                else mods2 = null;
                //                if(mods2!=null){
                //                    mods2.addAll(mods1);
                //
                //                }else {
                Collection<Pair> operations;
                if (mods2 != null) {
                    operations = mods2.stream()
                            .map(att -> new Pair(att.getOperation(), att.getSlot()))
                            .collect(Collectors.toSet());
                } else {
                    operations = Set.of();
                }
                for (AttributeModifier mod : mods1) {
                    if (!operations.contains(new Pair(mod.getOperation(), mod.getSlot()))) {
                        hasChanged = true;
                        meta1.removeAttributeModifier(a, mod);
                        meta2.addAttributeModifier(a, mod);
                    }
                }
            }
        } else {
            if (isRestrictedAttribute(attribute)) return null;
            if (!meta2.hasAttributeModifiers()
                    || meta2.getAttributeModifiers(attribute).stream()
                            .noneMatch(mod -> mod.getOperation() == modifier.getOperation()
                                    && mod.getSlot() == modifier.getSlot())) {
                meta1.removeAttributeModifier(attribute, modifier);
                meta2.addAttributeModifier(attribute, modifier);
                hasChanged = true;
            }
        }
        if (!hasChanged) {
            return null;
        }
        ItemStack res1 = new ItemStack(stack1.getType(), 1);
        res1.setItemMeta(meta1);
        ItemStack res2 = new ItemStack(stack2.getType(), 1);
        res2.setItemMeta(meta2);
        return new Pair(res1, res2);
    }

    public void constructMenu(BlockMenuPreset preset) {
        preset.setSize(45);
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = OUTPUT_BORDER;
        len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], OUTPUT_BORDER_ITEM, ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(INPUT_ONE_INFO_SLOT, INPUT_ONE_INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(INPUT_TWO_INFO_SLOT, INPUT_TWO_INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
    }
}
