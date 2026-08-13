package me.matl114.logitech.core.Registries;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.SlimefunRegistry;
import io.github.thebusybiscuit.slimefun4.core.handlers.GlobalItemHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import me.matl114.logitech.core.AddGroups;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.AddSlimefunItems;
import me.matl114.logitech.core.Items.Abstracts.MyVanillaItem;
import me.matl114.logitech.core.Machines.AutoMachines.AdvanceRecipeCrafter;
import me.matl114.logitech.core.Machines.ManualMachines.ManualCrafter;
import me.matl114.logitech.core.Machines.WorkBenchs.BugCrafter;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.ConstSlimefunItemStack;
import me.matl114.logitech.utils.UtilClass.SpecialItemClass.CustomHead;
import me.matl114.matlib.utils.version.VersionedAttribute;
import me.matl114.matlib.utils.version.VersionedRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * only enable when certain addon are enable
 */
public class AddDepends {
    public static void setup(SlimefunAddon plugin) {
        AddDepends.registerSlimefunItems(plugin);
    }

    public static void registerSlimefunItems(SlimefunAddon plugin) {
        try {
            if (hasInfiniteExpansion)
                MOBDATA_MANUAL = new ManualCrafter(
                                AddGroups.MANUAL,
                                AddItem.MOBDATA_MANUAL,
                                BugCrafter.TYPE,
                                AddSlimefunItems.recipe(
                                        null,
                                        null,
                                        AddItem.BUG,
                                        AddItem.BUG,
                                        null,
                                        null,
                                        null,
                                        AddItem.BUG,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.BUG,
                                        null,
                                        null,
                                        AddItem.LFIELD,
                                        "DATA_INFUSER",
                                        "DATA_INFUSER",
                                        AddItem.LFIELD,
                                        null,
                                        null,
                                        AddItem.LFIELD,
                                        "DATA_INFUSER",
                                        "DATA_INFUSER",
                                        AddItem.LFIELD,
                                        null,
                                        null,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        null),
                                600_000,
                                20_000,
                                MOBDATA_TYPE)
                        .register();
        } catch (Throwable e) {
            Debug.logger("AN ERROR OCCURED WHILE REGISTERING ITEM: MOBDATA_MANUAL,ITEM DISABLED");
            e.printStackTrace();
        }
        try {
            if (hasInfiniteExpansion)
                INFINITY_MANUAL = new ManualCrafter(
                                AddGroups.MANUAL,
                                AddItem.INFINITY_MANUAL,
                                BugCrafter.TYPE,
                                AddSlimefunItems.recipe(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        AddItem.ABSTRACT_INGOT,
                                        "REINFORCED_PLATE",
                                        "REINFORCED_PLATE",
                                        "REINFORCED_PLATE",
                                        "REINFORCED_PLATE",
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        "MACHINE_PLATE",
                                        "INFINITY_FORGE",
                                        "INFINITY_FORGE",
                                        "MACHINE_PLATE",
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        AddItem.ABSTRACT_INGOT),
                                100_000_000,
                                2_500_000,
                                INFINITYWORKBENCH_TYPE)
                        .register();
        } catch (Throwable e) {
            Debug.logger("AN ERROR OCCURED WHILE REGISTERING ITEM: INFINITY_MANUAL,ITEM DISABLED");
            e.printStackTrace();
        }
        try {
            if (hasNetwork)
                NTWWORKBENCH_MANUAL = new ManualCrafter(
                                AddGroups.MANUAL,
                                AddItem.NTWWORKBENCH_MANUAL,
                                BugCrafter.TYPE,
                                AddSlimefunItems.recipe(
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        "NTW_BRIDGE",
                                        "ADVANCED_CIRCUIT_BOARD",
                                        "ADVANCED_CIRCUIT_BOARD",
                                        "NTW_BRIDGE",
                                        null,
                                        null,
                                        "NTW_BRIDGE",
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        "NTW_BRIDGE",
                                        null,
                                        null,
                                        AddItem.ABSTRACT_INGOT,
                                        "NTW_QUANTUM_WORKBENCH",
                                        "NTW_QUANTUM_WORKBENCH",
                                        AddItem.ABSTRACT_INGOT,
                                        null,
                                        null,
                                        AddItem.ABSTRACT_INGOT,
                                        "NTW_QUANTUM_WORKBENCH",
                                        "NTW_QUANTUM_WORKBENCH",
                                        AddItem.ABSTRACT_INGOT,
                                        null,
                                        null,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        AddItem.ABSTRACT_INGOT,
                                        null),
                                0,
                                0,
                                Stream.of(NTWQTWORKBENCH_TYPE, NTWEP_WORKBENCH_TYPE, NTWEP_DRAWER_TYPE)
                                        .filter(Objects::nonNull)
                                        .toArray(RecipeType[]::new))
                        .register();
        } catch (Throwable e) {
            Debug.logger("AN ERROR OCCURED WHILE REGISTERING ITEM: NTWWORKBENCH_MANUAL,ITEM DISABLED");
            e.printStackTrace();
        }
        try {
            if (hasInfiniteExpansion)
                INFINITY_AUTOCRAFT = new AdvanceRecipeCrafter(
                                AddGroups.BASIC,
                                AddItem.INFINITY_AUTOCRAFT,
                                BugCrafter.TYPE,
                                AddSlimefunItems.recipe(
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        "INFINITE_INGOT",
                                        "INFINITE_INGOT",
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        AddItem.LCRAFT,
                                        "VOID_INGOT",
                                        "VOID_INGOT",
                                        AddItem.LMOTOR,
                                        AddItem.LFIELD,
                                        "INFINITE_INGOT",
                                        "VOID_INGOT",
                                        "INFINITY_FORGE",
                                        "INFINITY_FORGE",
                                        "VOID_INGOT",
                                        "INFINITE_INGOT",
                                        "INFINITE_INGOT",
                                        "VOID_INGOT",
                                        "INFINITY_FORGE",
                                        "INFINITY_FORGE",
                                        "VOID_INGOT",
                                        "INFINITE_INGOT",
                                        AddItem.LFIELD,
                                        AddItem.LMOTOR,
                                        "VOID_INGOT",
                                        "VOID_INGOT",
                                        AddItem.LCRAFT,
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        AddItem.LFIELD,
                                        "INFINITE_INGOT",
                                        "INFINITE_INGOT",
                                        AddItem.LFIELD,
                                        AddItem.LFIELD),
                                Material.RESPAWN_ANCHOR,
                                100_000,
                                1_500_000,
                                8,
                                INFINITYWORKBENCH_TYPE)
                        .register();
        } catch (Throwable e) {
            Debug.logger("AN ERROR OCCURED WHILE REGISTERING ITEM: INFINITY_AUTOCRAFT,ITEM DISABLED");
            e.printStackTrace();
        }
        try {
            if (hasNetwork)
                NTW_STORAGE_DISPLAY = new MyVanillaItem(
                                AddGroups.SINGULARITY,
                                SlimefunItem.getById("NTW_QUANTUM_STORAGE_8")
                                        .getItem()
                                        .clone(),
                                "NTW_QUANTUM_STORAGE_DISPLAY",
                                RecipeType.ENHANCED_CRAFTING_TABLE,
                                AddSlimefunItems.recipe(
                                        AddItem.ABSTRACT_INGOT,
                                        "NTW_QUANTUM_STORAGE_1",
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.IOPORT,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.STORAGE_SINGULARITY,
                                        AddItem.ABSTRACT_INGOT,
                                        AddItem.STORAGE_SINGULARITY))
                        .setDisplayRecipes(Utils.list(
                                AddUtils.getInfoShow(
                                        "&f機制 - &c終極合成",
                                        "&c特性 ",
                                        "&7終極合成是本附屬某些終極機器中的機制",
                                        "&c具體說明請看\"版本與說明\"分類中的資訊",
                                        "&7直白了説是讓物品存儲中的物品直接參與合成/進程",
                                        "&7從而打破槽位限制",
                                        "&7該物品是終極機器支援的物品之一",
                                        "&a建議:不推薦直接將量子存儲放入終極機器,不方便且會有更高卡頓!"),
                                null,
                                AddUtils.getInfoShow(
                                        "&7機制 -&c 物品存儲",
                                        "&7本物品可以存儲某種物品",
                                        "&7本物品的最大存儲量為2147483647",
                                        "&7當該物品被放入終極機器的槽位中時,",
                                        "&7可以代理其內部存儲的物品&e直接參與合成!",
                                        "&7產出的物品也可以&e直接進入內部存儲的物品中!",
                                        "&e本物品是網絡附屬裏的支援物品,直接置於地上即可使用"),
                                null,
                                AddUtils.getInfoShow(
                                        "&7機制 -&c 報錯",
                                        "&e如果網絡版本不兼容或使用了錯誤的測試版,會造成報錯",
                                        "&7如果使用量子存儲進行合成時,出現合成不成功的情況",
                                        "&7很可能這是資料損壞的存儲或者錯誤版本導致的存儲不兼容",
                                        "&e請立刻將其移出槽位",
                                        "&7並判斷是存儲資料損壞問題還是網絡版本問題",
                                        "&7若累計報錯達到一定數目,將會禁用該兼容"),
                                null))
                        .register();

        } catch (Throwable e) {
            Debug.logger("AN ERROR OCCURED WHILE REGISTERING ITEM: NTW_STORAGE_DISPLAY,ITEM DISABLED");
            e.printStackTrace();
        }
        try {
            if (hasInfiniteExpansion) {
                SlimefunItem instance = SlimefunItem.getById("INFINITY_CROWN");
                //                SlimefunItemStack new_infinity_helmet=new
                // SlimefunItemStack("INFINITY_CROWN",CustomHead.INF_HELMET.getItem(),
                //                        AddUtils.color("無盡頭盔"));
                try {
                    Config cfg = (Config) ReflectUtils.invokeGetRecursively(
                            Slimefun.getItemTextureService(), Settings.FIELD, "config");
                    cfg.setValue(instance.getId(), Integer.valueOf(0));
                } catch (Throwable e) {

                }
                SlimefunItemStack it = (SlimefunItemStack)
                        ReflectUtils.invokeGetRecursively(instance, Settings.FIELD, "itemStackTemplate");
                Object locked = ReflectUtils.invokeGetRecursively(it, Settings.FIELD, "locked");
                ReflectUtils.invokeSetRecursively(it, "locked", Boolean.valueOf(false));
                it.setType(Material.PLAYER_HEAD);
                ItemMeta s = it.getItemMeta();
                if (s instanceof SkullMeta smt) {
                    smt.setOwnerProfile(
                            ((SkullMeta) CustomHead.INF_HELMET.getItem().getItemMeta()).getOwnerProfile());
                }
                s.addAttributeModifier(
                        VersionedRegistry.attribute("armor"),
                        VersionedAttribute.getInstance()
                                .createAttributeModifier(
                                        UUID.nameUUIDFromBytes(new byte[] {1, 1, 4, 5}),
                                        "logitech_armor",
                                        6.0,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlot.HEAD));
                s.addAttributeModifier(
                        VersionedRegistry.attribute("armor_toughness"),
                        VersionedAttribute.getInstance()
                                .createAttributeModifier(
                                        UUID.nameUUIDFromBytes(new byte[] {1, 9, 1, 9}),
                                        "logitech_armor_toughness",
                                        6.0,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlot.HEAD));
                s.addAttributeModifier(
                        VersionedRegistry.attribute("knockback_resistance"),
                        VersionedAttribute.getInstance()
                                .createAttributeModifier(
                                        UUID.nameUUIDFromBytes(new byte[] {8, 1, 0, 0}),
                                        "logitech_knockback_resistence",
                                        0.2,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlot.HEAD));
                s.setCustomModelData(null);
                s.setDisplayName(AddUtils.resolveColor(AddUtils.color("無盡頭盔")));
                it.setItemMeta(s);
                ReflectUtils.invokeSetRecursively(it, "locked", locked);
                ReflectUtils.invokeSetRecursively(instance, "itemStackTemplate", new ConstSlimefunItemStack(it));
                ReflectUtils.invokeSetRecursively(instance, "recipeOutput", new ConstSlimefunItemStack(it));
                try {
                    ItemHandler handler = AddHandlers.stopPlacementHandler;
                    ((Map) ReflectUtils.invokeGetRecursively(instance, Settings.FIELD, "itemHandlers"))
                            .put(handler.getIdentifier(), handler);
                    if (handler instanceof GlobalItemHandler) {
                        SlimefunRegistry registry = Slimefun.getRegistry();
                        registry.getGlobalItemHandlers(handler.getIdentifier()).add(handler);
                    }
                } catch (Throwable e) {

                }
            }
        } catch (Throwable e) {
            Debug.logger("AN ERROR OCCURED WHILE CHANGING ITEM: INFINITY_CROW,CHANGE DISABLED");
            e.printStackTrace();
        }
        try {
            if (hasInfiniteExpansion) {
                Class infinityMobDataClass =
                        SlimefunItem.getById("MOB_SIMULATION_CHAMBER").getClass();
                Constructor constructor = ReflectUtils.getSuitableConstructor(
                        infinityMobDataClass,
                        ItemGroup.class,
                        SlimefunItemStack.class,
                        RecipeType.class,
                        ItemStack[].class,
                        int.class,
                        int.class);
                AddItem.INF_MOBSIMULATION.setItemMeta(AddUtils.addLore(
                                AddItem.INF_MOBSIMULATION, AddUtils.speedDisplay(64), AddUtils.energyPerSecond(800))
                        .getItemMeta());
                INFINITY_MOBSIMNULATOR = (SlimefunItem) constructor.newInstance(
                        AddGroups.BASIC,
                        AddItem.INF_MOBSIMULATION,
                        INFINITYWORKBENCH_TYPE,
                        AddSlimefunItems.recipe(
                                null,
                                "MACHINE_PLATE",
                                "MACHINE_PLATE",
                                "MACHINE_PLATE",
                                "MACHINE_PLATE",
                                null,
                                null,
                                "VOID_INGOT",
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                "VOID_INGOT",
                                null,
                                null,
                                "VOID_INGOT",
                                "MOB_SIMULATION_CHAMBER",
                                "MOB_SIMULATION_CHAMBER",
                                "VOID_INGOT",
                                null,
                                null,
                                "VOID_INGOT",
                                "MOB_SIMULATION_CHAMBER",
                                "MOB_SIMULATION_CHAMBER",
                                "VOID_INGOT",
                                null,
                                null,
                                "INFINITE_INGOT",
                                AddItem.LENGINE,
                                AddItem.LENGINE,
                                "INFINITE_INGOT",
                                null,
                                "INFINITE_INGOT",
                                "INFINITE_INGOT",
                                "INFINITE_INGOT",
                                "INFINITE_INGOT",
                                "INFINITE_INGOT",
                                "INFINITE_INGOT"),
                        800,
                        1);

                INFINITY_MOBSIMNULATOR.register(plugin);
            }
        } catch (Throwable e) {

        }
        try {
            if (hasInfiniteExpansion) {
                Class infinityGeoMiner = SlimefunItem.getById("GEO_QUARRY").getClass();
                Constructor constructor = ReflectUtils.getSuitableConstructor(
                        infinityGeoMiner,
                        ItemGroup.class,
                        SlimefunItemStack.class,
                        RecipeType.class,
                        ItemStack[].class);
                AddItem.INF_GEOQUARRY.setItemMeta(AddUtils.addLore(
                                AddItem.INF_GEOQUARRY, AddUtils.speedDisplay(64), AddUtils.energyPerSecond(4500))
                        .getItemMeta());
                INFINITY_GEOQURRY = (SlimefunItem) constructor.newInstance(
                        AddGroups.BASIC,
                        AddItem.INF_GEOQUARRY,
                        INFINITYWORKBENCH_TYPE,
                        AddSlimefunItems.recipe(
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                "MYTHRIL",
                                "VOID_INGOT",
                                "VOID_INGOT",
                                "VOID_INGOT",
                                "VOID_INGOT",
                                "MYTHRIL",
                                "MYTHRIL",
                                "VOID_INGOT",
                                "INFINITE_MACHINE_CIRCUIT",
                                "INFINITE_MACHINE_CIRCUIT",
                                "VOID_INGOT",
                                "MYTHRIL",
                                "MYTHRIL",
                                "VOID_INGOT",
                                "GEO_QUARRY",
                                "GEO_QUARRY",
                                "VOID_INGOT",
                                "MYTHRIL",
                                "MYTHRIL",
                                "VOID_INGOT",
                                "VOID_INGOT",
                                "VOID_INGOT",
                                "VOID_INGOT",
                                "MYTHRIL",
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT,
                                AddItem.ABSTRACT_INGOT));
                ReflectUtils.invokeSetRecursively(INFINITY_GEOQURRY, "ticksPerOutput", 6);
                ReflectUtils.invokeSetRecursively(INFINITY_GEOQURRY, "energyPerTick", 4500);
                INFINITY_GEOQURRY.register(plugin);
            }
        } catch (Throwable e) {

        }
    }

    public static boolean hasInfiniteExpansion = false;
    public static boolean hasNetwork = false;
    public static boolean hasNetworkExpansion = false;
    public static RecipeType MOBDATA_TYPE;
    public static RecipeType INFINITYWORKBENCH_TYPE;
    public static RecipeType NTWQTWORKBENCH_TYPE;
    public static SlimefunItem NTWEP_WORKBENCH;
    public static RecipeType NTWEP_WORKBENCH_TYPE;
    public static RecipeType NTWEP_DRAWER_TYPE;
    public static RecipeType VOIDHARVEST;
    public static RecipeType SINGULARITY_CONSTRUCTOR;
    public static SlimefunItem MOBDATA_MANUAL;
    public static SlimefunItem INFINITY_MANUAL;
    public static SlimefunItem NTWWORKBENCH_MANUAL;
    public static SlimefunItem INFINITY_AUTOCRAFT;
    public static SlimefunItem INFINITY_MOBSIMNULATOR;
    public static SlimefunItem INFINITY_GEOQURRY;
    public static MyVanillaItem NTW_STORAGE_DISPLAY;
    public static Class NETWORKSQUANTUMSTORAGE;
    public static NamespacedKey NTWQUANTUMKEY;
}
