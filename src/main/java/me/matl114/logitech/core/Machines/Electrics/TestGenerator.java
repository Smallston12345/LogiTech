package me.matl114.logitech.core.Machines.Electrics;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Machines.Abstracts.AbstractEnergyProvider;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.TickerClass.Ticking;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class TestGenerator extends AbstractEnergyProvider {
    public final Random rand = new Random();
    public final int ENERGY_ABSMAX;
    public final int OUTPUT_MIN;
    public final int OUTPUT_MAX;
    public final ItemStack OUTPUT = AddItem.PARADOX;
    public final int DISPLAY_SLOT = 0;
    public final int[] OUTPUT_SLOT = new int[] {1, 2, 3, 4, 5, 6, 7, 8};

    public int[] getInputSlots() {
        return new int[0];
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    public void addInfo(ItemStack stack) {
        stack.setItemMeta(AddUtils.addLore(
                        stack,
                        new StringBuilder("&8⇨ &e⚡ &7")
                                .append(AddUtils.formatDouble(-ENERGY_ABSMAX))
                                .append(" ~ ")
                                .append(AddUtils.formatDouble(ENERGY_ABSMAX))
                                .append(" J/t")
                                .toString())
                .getItemMeta());
    }

    public final int RANDOM_THREASHOLD;

    public TestGenerator(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energyBuffer,
            int energyAbsMax,
            int outputMin,
            int outputMax) {
        super(itemGroup, item, recipeType, recipe, energyBuffer, 0);
        this.OUTPUT_MIN = outputMin;
        this.OUTPUT_MAX = outputMax;
        this.ENERGY_ABSMAX = energyAbsMax;
        this.machineRecipes = new ArrayList<>();
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow("&f生成機制", "&7當機器中電力位於%dJ~%dJ時".formatted(outputMin, outputMax), "&7清空電力並生成"),
                OUTPUT,
                AddUtils.getInfoShow("&f免責聲明", "&7經過測試該機器的絕大部分卡頓來源於黏液電網方法", "&7由於黏液自身的電網性能問題", "&a作者不對該機器性能問題負責"),
                null));
        this.machineRecipes = new ArrayList<>();
        this.RANDOM_THREASHOLD = 2 * this.ENERGY_ABSMAX + 1;
    }

    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.GENERATOR;
    }

    public ItemStack[] OUTPUTS = new ItemStack[] {OUTPUT};

    public int getGeneratedOutput(@Nonnull Location l, @Nonnull SlimefunBlockData data) {
        return ThreadLocalRandom.current().nextInt(RANDOM_THREASHOLD) - ENERGY_ABSMAX;
    }

    public void constructMenu(BlockMenuPreset inv) {
        inv.addItem(DISPLAY_SLOT, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
    }

    public void newMenuInstance(BlockMenu inv, Block block) {
        inv.addMenuOpeningHandler((player -> {
            inv.replaceExistingItem(DISPLAY_SLOT, ChestMenuUtils.getBackground());
        }));
    }

    public void process(Block b, BlockMenu inv, SlimefunBlockData data) {
        Location loc = inv.getLocation();
        int charge = getCharge(loc);

        if (charge > OUTPUT_MIN && charge < OUTPUT_MAX) {
            CraftUtils.pushItems(OUTPUTS, inv, getOutputSlots());
            charge = 0;
            setCharge(loc, 0);
        }
        if (inv.hasViewer()) {
            inv.replaceExistingItem(
                    DISPLAY_SLOT, AddUtils.getGeneratorDisplay(true, "虛空量子", charge, this.energybuffer));
        }
    }

    public void registerTick(SlimefunItem item) {
        item.addItemHandler(new BlockTicker() {
            public boolean isSynchronized() {
                return ((Ticking) TestGenerator.this).isSync();
            }

            @ParametersAreNonnullByDefault
            public void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
                BlockMenu menu = data.getBlockMenu();
                // BlockMenu menu = BlockStorage.getInventory(b);
                ((Ticking) TestGenerator.this).tick(b, menu, data, 0);
            }
        });
    }
}
