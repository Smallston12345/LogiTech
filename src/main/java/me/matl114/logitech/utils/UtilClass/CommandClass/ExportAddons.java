package me.matl114.logitech.utils.UtilClass.CommandClass;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import me.matl114.logitech.ConfigLoader;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.Debug;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ExportAddons {
    public static void export(
            String addonName, boolean doExportIfAbsent, boolean doExportWhenEmpty, Consumer<String> outputStream) {
        Plugin pl = Bukkit.getPluginManager().getPlugin(addonName);
        if (pl instanceof SlimefunAddon sfaddon) {
            outputStream.accept("檢測到相關的附屬! %s".formatted(sfaddon.toString()));
            outputStream.accept("準備導出該附屬相關機器信息");
            HashSet<SlimefunItem> addonItems = new LinkedHashSet<>();
            HashSet<SlimefunItem> addonItemsWithoutRecipe = new LinkedHashSet<>();
            for (SlimefunItem sfitem : Slimefun.getRegistry().getAllSlimefunItems()) {
                if (sfitem.getAddon() == sfaddon) {
                    addonItems.add(sfitem);
                    if (!RecipeSupporter.MACHINE_RECIPELIST.containsKey(sfitem)) {
                        addonItemsWithoutRecipe.add(sfitem);
                    }
                }
            }
            outputStream.accept("讀取到該附屬的 %d 項sf物品".formatted(addonItems.size()));
            outputStream.accept("檢測到該附屬的 %d 項擁有標準配方表的物品".formatted(addonItems.size() - addonItemsWithoutRecipe.size()));
            outputStream.accept("其餘物品將導出展示用配方列表");
            Config outputConfig = ConfigLoader.loadExternalConfig("exports/" + addonName);
            outputConfig.setValue(addonName + ".enable", true);
            if (doExportIfAbsent) {
                outputStream.accept("提示:暫未支持轉換隨機配方!");
            }
            for (SlimefunItem sfitem : addonItems) {
                String root = addonName + "." + sfitem.getId();
                String recipeRoot = root + ".recipe";
                boolean isEmpty = true;
                if (addonItemsWithoutRecipe.contains(sfitem)) {
                    if (sfitem instanceof RecipeDisplayItem rdi) {
                        outputConfig.setValue(root + ".source", "displayed");
                        List<ItemStack> stacks = rdi.getDisplayRecipes();
                        int len = stacks.size();
                        int index = 0;
                        for (int i = 0; 2 * i < len; i += 1) {
                            ItemStack a1 = stacks.get(2 * i);
                            ItemStack a2 = null;
                            if (2 * i + 1 < len) {
                                a2 = stacks.get(2 * i + 1);
                            }
                            if (a1 != null)
                                outputConfig.setValue(
                                        recipeRoot + ".%d.input.1".formatted(index), AddUtils.getItemId(a1));
                            if (a2 != null)
                                outputConfig.setValue(
                                        recipeRoot + ".%d.output.1".formatted(index), AddUtils.getItemId(a2));
                            if (a1 != null || a2 != null) {
                                isEmpty = false;
                                outputConfig.setValue(recipeRoot + ".%d.tick".formatted(index), 0);
                                index += 1;
                            }
                        }
                    } else {
                        if (doExportWhenEmpty) {
                            outputConfig.setValue(root + ".source", "empty");
                        }
                    }
                } else {
                    if (doExportIfAbsent) {
                        List<MachineRecipe> recipes = RecipeSupporter.MACHINE_RECIPELIST.get(sfitem);
                        outputConfig.setValue(root + ".source", "recipe");
                        int index = 0;
                        for (MachineRecipe recipe : recipes) {
                            ItemStack[] inputs = recipe.getInput();
                            ItemStack[] outputs = recipe.getOutput();
                            for (int i = 0; i < inputs.length; i += 2) {
                                outputConfig.setValue(
                                        recipeRoot + ".%d.input.%d".formatted(index, i), AddUtils.getItemId(inputs[i]));
                            }
                            for (int j = 0; j < outputs.length; j += 2) {
                                outputConfig.setValue(
                                        recipeRoot + ".%d.output.%d".formatted(index, j),
                                        AddUtils.getItemId(outputs[j]));
                            }
                            isEmpty = false;
                            outputConfig.setValue(recipeRoot + ".%d.tick".formatted(index), recipe.getTicks());
                        }
                    }
                }
                if (!isEmpty || doExportWhenEmpty) {
                    outputConfig.setValue(root + ".mode", "w");
                    outputConfig.setValue(root + ".type", "machine");
                    int energy = RecipeSupporter.tryGetMachineEnergy(sfitem);
                    if (energy >= 0) outputConfig.setValue(root + ".energy", energy);
                }
            }
            outputConfig.save();
            outputStream.accept("配方配置已導出至 %s".formatted(outputConfig.getFile().getPath()));
        } else {
            Debug.logger("this is not a SlimefunAddon");
            outputStream.accept("這不是一個有效的粘液附屬名稱! %s".formatted(addonName));
        }
    }
}
