package me.matl114.logitech.core.Machines.SpecialMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import javax.annotation.Nullable;
import me.matl114.logitech.core.Blocks.Laser;
import me.matl114.logitech.core.Blocks.MultiBlock.FinalAltarCore;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.FeatureConfig;
import me.matl114.logitech.utils.MachineRecipeUtils;
import me.matl114.logitech.utils.UtilClass.ItemClass.RandOutItem;
import me.matl114.logitech.utils.UtilClass.ItemClass.RandomItemStack;
import me.matl114.logitech.utils.Utils;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class FinalConvertor extends AbstractMachine
        implements FinalAltarCore.FinalAltarChargable, Laser.LaserChargable {
    static final Random rand = new Random();
    static final Material[] MATERIALS = Material.values();
    static final int MAX_RANDOM_BUFFER =
            Math.max(1, FeatureConfig.getInt("FinalConvertor.random-buffer-range", 10));
    static final int MAX_RANDOM_OFFSET =
            (int) Math.min((long) MAX_RANDOM_BUFFER * MAX_RANDOM_BUFFER, Integer.MAX_VALUE);
    static final int MAX_TRY_COUNT = Math.max(1, FeatureConfig.getInt("FinalConvertor.max-attempts", 48));
    static final int MIN_OUTPUT_AMOUNT = Math.max(1, FeatureConfig.getInt("FinalConvertor.min-output-amount", 32));
    static final int MAX_OUTPUT_AMOUNT =
            Math.max(MIN_OUTPUT_AMOUNT, FeatureConfig.getInt("FinalConvertor.max-output-amount", 64));
    static final Set<String> EXCLUDED_OUTPUTS =
            Set.copyOf(FeatureConfig.getStringList("FinalConvertor.excluded-outputs"));

    public static Material getRandomMaterial(Material inputMaterial) {
        int start = inputMaterial.ordinal();
        for (int i = 0; i < MAX_TRY_COUNT; i++) {
            int offset = rand.nextInt(MAX_RANDOM_OFFSET);
            int direction = rand.nextBoolean() ? -1 : 1;
            long candidateIndex = start + (long) direction * offset;
            if (candidateIndex < 0 || candidateIndex >= MATERIALS.length) continue;

            Material candidate = MATERIALS[(int) candidateIndex];
            if (!candidate.isLegacy() && !EXCLUDED_OUTPUTS.contains(candidate.name())) {
                return candidate;
            }
        }
        return null;
    }

    private static int getRandomOutputAmount(Material material) {
        int maximum = Math.min(Math.max(1, material.getMaxStackSize()), MAX_OUTPUT_AMOUNT);
        int minimum = Math.min(maximum, MIN_OUTPUT_AMOUNT);
        return minimum + rand.nextInt(maximum - minimum + 1);
    }

    protected final int[] BORDER = new int[] {13, 22, 31, 40, 49};
    protected final int[] INPUT_BORDER = new int[] {0, 1, 2, 3};
    protected final int[] OUTPUT_BORDER = new int[] {5, 6, 7, 8};
    protected final int[] INPUT_SLOT = new int[] {
        9, 10, 11, 12,
        18, 19, 20, 21,
        27, 28, 29, 30,
        36, 37, 38, 39,
        45, 46, 47, 48
    };
    protected final int[] OUTPUT_SLOT = new int[] {
        14, 15, 16, 17,
        23, 24, 25, 26,
        32, 33, 34, 35,
        41, 42, 43, 44,
        50, 51, 52, 53
    };

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    protected final int STATUS_SLOT = 4;
    protected final ItemStack STATUS_ON =
            new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&6機器資訊", "&7狀態: &a已啟動");
    protected final ItemStack STATUS_OFF =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&6機器資訊", "&7狀態: &c未啟動");
    protected final ItemStack NULL_OUTPUT;
    protected final RandOutItem NULL_OUT;
    protected final MachineRecipe RECIPE_FOR_DISPLAY;

    public FinalConvertor(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption,
            RandomItemStack nullOutput) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption);
        this.NULL_OUTPUT = nullOutput;
        this.NULL_OUT = nullOutput;
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c充能",
                        "&7當置於貳級終極祭壇上時",
                        "&7且機器被終極祭壇結構中的所有宏激光發射器充能時",
                        "&7即終極祭壇中四個宏激光發射器分別位於四個壹級以上終極祭壇上時",
                        "&7機器啟動,進行運轉"),
                null,
                AddUtils.getInfoShow(
                        "&f機制 - &c隨機波動",
                        "&7當機器運轉時,",
                        "&7機器會隨機波動輸入物的虛擬材質序號",
                        "&7並嘗試將其重塑為附近序號的原版物品",
                        "&7成功時隨機產出"
                                + MIN_OUTPUT_AMOUNT
                                + "至"
                                + MAX_OUTPUT_AMOUNT
                                + "個,不會超過物品堆疊上限",
                        "&7若結果無法成為物品,將抽取下方的特殊產物")));
        this.RECIPE_FOR_DISPLAY = MachineRecipeUtils.stackFrom(
                -1, new ItemStack[] {AddUtils.getInfoShow("&f可能的輸出", "&7如下所示")}, new ItemStack[] {NULL_OUTPUT});
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = INPUT_BORDER;
        len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = OUTPUT_BORDER;
        len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(STATUS_SLOT, STATUS_OFF, ChestMenuUtils.getEmptyClickHandler());
    }

    public List<MachineRecipe> getMachineRecipes() {
        List<MachineRecipe> recipes = new ArrayList<>();
        recipes.add(RECIPE_FOR_DISPLAY);
        return recipes;
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

    public void process(Block b, BlockMenu inv, SlimefunBlockData data) {
        int hasPutOutput = 0;
        int hasPutInput = 0;
        ItemStack input;
        ItemStack output;
        loop:
        while (true) {
            do {
                if (hasPutInput >= INPUT_SLOT.length) {
                    break loop;
                }
                input = inv.getItemInSlot(INPUT_SLOT[hasPutInput]);
                ++hasPutInput;
            } while (input == null);
            do {
                if (hasPutOutput >= OUTPUT_SLOT.length) {
                    break loop;
                }
                output = inv.getItemInSlot(OUTPUT_SLOT[hasPutOutput]);
                ++hasPutOutput;

            } while (output != null);
            Material randMaterial = getRandomMaterial(input.getType());

            ItemStack result = (randMaterial != null && randMaterial.isItem() && !randMaterial.isAir())
                    ? new ItemStack(randMaterial, getRandomOutputAmount(randMaterial))
                    : NULL_OUT.getInstance();

            inv.replaceExistingItem(OUTPUT_SLOT[hasPutOutput - 1], result);
            inv.replaceExistingItem(INPUT_SLOT[hasPutInput - 1], null);
            progressorCost(b, inv);
            break;
        }
    }
}
