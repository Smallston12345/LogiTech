package me.matl114.logitech.core.Machines.AutoMachines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import java.util.List;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.Utils;
import org.bukkit.inventory.ItemStack;

public class FinalMGenerator extends MMGenerator {
    public FinalMGenerator(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int time,
            int energybuffer,
            int energyConsumption,
            List<Pair<Object[], Object[]>> outputs_w) {
        super(itemGroup, item, recipeType, recipe, time, energybuffer, energyConsumption, outputs_w);
        setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c終極合成",
                        "&7機器的輸出槽支援終極合成的特性",
                        "&e可以直接消耗存儲類物品或者連結綁定的實體存儲內的物品參與合成",
                        "&e產出的物品也可以直接地存入存儲類物品或者連結綁定的實體存儲",
                        "&7當前支援的物品:",
                        "&7邏輯工藝 概念奇點存儲(存儲類物品)",
                        "&7邏輯工藝 量子糾纏奇點(存儲連結物品)",
                        "&7網絡(拓展) 量子存儲系列(存儲類物品)",
                        "&a建議:多使用量子糾纏奇點,可以大幅度減少卡頓!"),
                null));
        this.CRAFT_PROVIDER = FinalFeature.STORAGE_AND_LOCPROXY_READER;
    }

    public void registerTick(SlimefunItem item) {
        this.addItemHandler(FinalFeature.FINAL_SYNC_TICKER);
    }
}
