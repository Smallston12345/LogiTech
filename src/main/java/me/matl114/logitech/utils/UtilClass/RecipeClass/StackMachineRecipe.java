package me.matl114.logitech.utils.UtilClass.RecipeClass;

import java.util.Collections;
import java.util.Set;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.inventory.ItemStack;

/**
 * unshaped recipe with stacked input ItemStack can be used in most Abstract machines
 */
public class StackMachineRecipe extends MachineRecipe {
    /**
     * 標記哪些輸入項索引不消耗（僅作為催化劑存在，不從輸入槽扣除）
     * 索引對應 getInput() 數組的位置
     */
    private Set<Integer> noConsumeInputs = Collections.emptySet();

    public StackMachineRecipe(int ticks, ItemStack[] inputs, ItemStack[] outputs) {
        super(0, inputs, outputs);
        this.setTicks(ticks);
    }

    public StackMachineRecipe(int ticks, ItemStack[] inputs, ItemStack[] outputs, Set<Integer> noConsumeInputs) {
        super(0, inputs, outputs);
        this.setTicks(ticks);
        if (noConsumeInputs != null && !noConsumeInputs.isEmpty()) {
            this.noConsumeInputs = noConsumeInputs;
        }
    }

    public Set<Integer> getNoConsumeInputs() {
        return noConsumeInputs;
    }

    public boolean isNoConsumeInput(int index) {
        return noConsumeInputs.contains(index);
    }
}
