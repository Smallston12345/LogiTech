package me.matl114.logitech.core.Cargo.Transportation;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.List;
import me.matl114.logitech.utils.*;
import org.bukkit.inventory.ItemStack;

public class AdjacentCargoPlus extends AdjacentCargo {
    public AdjacentCargoPlus(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            List<ItemStack> displayList) {
        super(itemGroup, item, recipeType, recipe, displayList);
        setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7這是一個智能貨運機器",
                        "&7智能貨運機器的行為會包括若干對源方塊和目標方塊",
                        "&7智能貨運機器會進行從源方塊到目標方塊的物流傳輸",
                        "&7智能貨運機器支持目標方塊設置的輸入槽限制",
                        "&7但是相應的,其最大傳輸量會被限制為min(576,配置數)"),
                null,
                AddUtils.getInfoShow("&f機制", "&7本機器可以選擇與其相鄰的方塊參與傳輸"),
                null));
        this.transportSmarter = true;
    }
}
