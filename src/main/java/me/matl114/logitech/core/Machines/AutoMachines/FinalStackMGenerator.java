package me.matl114.logitech.core.Machines.AutoMachines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.utils.AddUtils;
import org.bukkit.inventory.ItemStack;

public class FinalStackMGenerator extends StackMGenerator {
    public FinalStackMGenerator(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int time,
            int energyBuffer,
            int energyConsumption,
            int efficiency) {
        super(category, item, recipeType, recipe, time, energyBuffer, energyConsumption, efficiency);
        this.CRAFT_PROVIDER = FinalFeature.STORAGE_AND_LOCPROXY_READER;
        this.MACHINE_PROVIDER = FinalFeature.STORAGE_READER;
        this.addDisplayRecipe(AddUtils.getInfoShow(
                "&f機制 - &c終極合成",
                "&7機器的輸入槽,輸出槽,機器槽均支持終極合成的特性",
                "&e可以直接消耗存儲類物品或者鏈接綁定的實體存儲內的物品參與合成",
                "&e產出的物品也可以直接地存入存儲類物品或者鏈接綁定的實體存儲",
                "&7當前支持的物品:",
                "&7邏輯工藝 概念奇點存儲(存儲類物品)",
                "&7邏輯工藝 量子糾纏奇點(存儲鏈接物品)",
                "&7網絡(拓展) 量子存儲系列(存儲類物品)",
                "&c機器槽中只能放入存儲類物品!不能放入存儲鏈接!",
                "&a建議:多使用量子糾纏奇點,可以大幅度減少卡頓!"));
        this.addDisplayRecipe(null);
    }

    public void registerTick(SlimefunItem item) {
        this.addItemHandler(FinalFeature.FINAL_SYNC_TICKER);
    }
}
