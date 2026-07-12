package me.matl114.logitech.core.Cargo.SpaceStorage;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import javax.annotation.Nullable;
import me.matl114.logitech.ConfigLoader;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.utils.*;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class SpaceTower extends AbstractMachine {
    protected final int[] INPUT_SLOTS = new int[] {22};
    protected final int[] OUTPUT_SLOTS = new int[0];
    protected final int[] BORDER =
            new int[] {0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26};
    protected final int OPERATE_SLOT = 4;
    protected final int INFO_SLOT = 13;

    public int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    public SpaceTower(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption);
    }

    protected final int MAX_SIZE = 63;

    public void addInfo(ItemStack stack) {
        stack.setItemMeta(AddUtils.addLore(
                        stack,
                        LoreBuilder.powerBuffer(energybuffer),
                        "&8⇨ &e⚡ &7" + AddUtils.formatDouble(energyConsumption) + " J/方塊")
                .getItemMeta());
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(
                OPERATE_SLOT,
                new CustomItemStack(
                        Material.PURPLE_STAINED_GLASS_PANE,
                        "&6點擊進行空間IO操作",
                        "&7單位體積耗電: %dJ".formatted(energyConsumption),
                        "&e請確保你已經閲讀機器説明"));
        preset.addItem(INFO_SLOT, new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&6狀態", "&c未檢測出延申的空間塔框架"));
    }

    public void newMenuInstance(BlockMenu menu, Block block) {
        menu.addMenuClickHandler(OPERATE_SLOT, ((player, i, itemStack, clickAction) -> {
            onStorage(player, menu);
            return false;
        }));
        menu.addMenuClickHandler(INFO_SLOT, ((player, i, itemStack, clickAction) -> {
            updateMenu(menu, block, Settings.RUN);
            return false;
        }));
        menu.addMenuOpeningHandler(player -> updateMenu(menu, block, Settings.RUN));
        menu.addMenuCloseHandler(player -> updateMenu(menu, block, Settings.RUN));
    }

    public void onStorage(Player player, BlockMenu inv) {
        Location loc = inv.getLocation();
        ItemStack storageCard = inv.getItemInSlot(INPUT_SLOTS[0]);

        if (storageCard == null) {
            AddUtils.sendMessage(player, "&c磁盤槽位為空!");
            return;
        }
        inv.replaceExistingItem(INPUT_SLOTS[0], storageCard);
        // 防止數據保存不上!
        storageCard = MenuUtils.syncSlot(inv, INPUT_SLOTS[0]);
        var locationInfo = getSpaceTowerRange(loc);
        if (locationInfo == null) {
            AddUtils.sendMessage(player, "&c未檢測到延申的空間塔框架");
            return;
        } else {
            Location start = locationInfo.getFirstValue();
            Location end = locationInfo.getSecondValue();
            int dx = end.getBlockX() - start.getBlockX();
            int dy = end.getBlockY() - start.getBlockY();
            int dz = end.getBlockZ() - start.getBlockZ();
            int volumn = dx * dy * dz;
            int charge = this.getCharge(loc);
            if (charge >= volumn * this.energyConsumption) {
                if (StorageSpace.threadRunning()) {
                    AddUtils.sendMessage(player, "&c當前正在有空間塔進行IO操作,請稍後再試");
                    return;
                } else {
                    onStorageOperation(
                            () -> this.setCharge(loc, charge - volumn * this.energyConsumption),
                            player,
                            start,
                            end,
                            storageCard);
                }
            } else {
                AddUtils.sendMessage(player, "&c電力不足! %d/%dJ".formatted(charge, volumn * this.energyConsumption));
                return;
            }
        }
    }

    public static boolean onStorageOperation(
            Runnable cost, Player player, Location startLocation, Location endLocation, ItemStack storageCard) {
        ItemMeta meta = storageCard.getItemMeta();
        SlimefunItem item = CraftUtils.parseSfItem(meta);
        if (item instanceof SpaceStorageCard card) {
            int x = startLocation.getBlockX();
            int y = startLocation.getBlockY();
            int z = startLocation.getBlockZ();
            int x_ = endLocation.getBlockX();
            int y_ = endLocation.getBlockY();
            int z_ = endLocation.getBlockZ();
            int index = card.getIndex(meta);
            Location storedLocation;
            int status = card.getStatus(meta);
            if (index < 0) {
                var newStorageData = StorageSpace.generateNewLocation(x_ - x, y_ - y, z_ - z);
                index = newStorageData.getFirstValue();
                card.setIndex(meta, index);
                storedLocation = newStorageData.getSecondValue();
                status = 1;
                card.addStorageInfo(meta, index, x_ - x, y_ - y, z_ - z);
            } else {
                String parentPath = AddUtils.concat("storages.", String.valueOf(index), ".");
                String locationInfo = ConfigLoader.SPACE_STORAGE.getString(parentPath + "loc");
                int status2 = ConfigLoader.SPACE_STORAGE.getInt(parentPath + "status");
                try {
                    String[] intDatas = locationInfo.split(",");
                    // 判斷是否數據對其
                    if (x_ - x != Integer.parseInt(intDatas[2])
                            || y_ - y != Integer.parseInt(intDatas[3])
                            || z_ - z != Integer.parseInt(intDatas[4])) {
                        AddUtils.sendMessage(
                                player, "&c空間塔圈出的空間 %d,%d,%d 與磁盤中所記錄的空間大小不符!".formatted(x_ - x, y_ - y, z_ - z));
                        return false;
                    }
                    storedLocation = new Location(
                            StorageSpace.STORAGE_WORLD,
                            Integer.parseInt(intDatas[0]),
                            0,
                            Integer.parseInt(intDatas[1]));
                } catch (Throwable e) {
                    AddUtils.sendMessage(player, "&c讀取配置文件時出現異常,請找服務器管理員解決");
                    Debug.logger(e);
                    Debug.logger("請檢查space-storage.yml中數據是否有異常,或者聯繫作者解決");
                    return false;
                }
                if (status != status2) {
                    AddUtils.sendMessage(player, "&c磁盤中存儲狀態數據損壞!");
                    return false;
                }
                String uid = card.getUid(meta);
                String uid2 = ConfigLoader.SPACE_STORAGE.getString(parentPath + "uid");
                if (!uid2.equals(uid)) {
                    AddUtils.sendMessage(player, "&c磁盤中存儲uid與配置文件不符!");
                    return false;
                }
            }
            String parentPath = AddUtils.concat("storages.", String.valueOf(index), ".");
            String newUid = AddUtils.getUUID();
            int newStatus = status == 1 ? 2 : 1;
            card.setUid(meta, newUid);
            card.setStatus(meta, newStatus);
            storageCard.setItemMeta(meta);
            ConfigLoader.SPACE_STORAGE.setValue(parentPath + "uid", newUid);
            ConfigLoader.SPACE_STORAGE.setValue(parentPath + "status", newStatus);
            ConfigLoader.SPACE_STORAGE.save();

            AddUtils.sendMessage(player, "&a磁盤數據檢測無誤...");
            AddUtils.sendMessage(player, "&a加載磁盤空間中...");
            AddUtils.sendMessage(player, "&a空間轉移中,這可能會持續數秒...");
            cost.run();
            StorageSpace.onSpaceExchange(player, startLocation, endLocation, storedLocation, status, true);
            return true;
        } else {
            AddUtils.sendMessage(player, "&c這不是有效的空間磁盤");
            return false;
        }
    }

    public Pair<Location, Location> getSpaceTowerRange(Location loc) {
        Location testLocation1 = loc.clone().add(1, 0, 0);
        Location testLocation2 = loc.clone().add(-1, 0, 0);
        int dx = 1;
        if (DataCache.getSfItem(testLocation1) instanceof SpaceTowerFrame) {
            if (DataCache.getSfItem(testLocation2) instanceof SpaceTowerFrame) {
                return null;
            } else {
                dx = 1;
            }
        } else if (DataCache.getSfItem(testLocation2) instanceof SpaceTowerFrame) {
            dx = -1;
        } else return null;
        int length = 1;
        Vector vec = new Vector(dx, 0, 0);
        for (; length <= MAX_SIZE; ++length) {
            testLocation1 = loc.clone().add(vec.clone().multiply(length));
            if (!(DataCache.getSfItem(testLocation1) instanceof SpaceTowerFrame)) {
                break;
            }
        }
        int minX = dx < 0 ? loc.getBlockX() - length + 1 : loc.getBlockX() + 1;
        int maxX = dx < 0 ? loc.getBlockX() : loc.getBlockX() + length;
        testLocation1 = loc.clone().add(0, 1, 0);
        testLocation2 = loc.clone().add(0, -1, 0);
        dx = 1;
        if (DataCache.getSfItem(testLocation1) instanceof SpaceTowerFrame) {
            if (DataCache.getSfItem(testLocation2) instanceof SpaceTowerFrame) {
                return null;
            } else {
                dx = 1;
            }
        } else if (DataCache.getSfItem(testLocation2) instanceof SpaceTowerFrame) {
            dx = -1;
        } else return null;
        length = 1;
        vec = new Vector(0, dx, 0);
        for (; length <= MAX_SIZE; length++) {
            testLocation1 = loc.clone().add(vec.clone().multiply(length));
            if (!(DataCache.getSfItem(testLocation1) instanceof SpaceTowerFrame)) {
                break;
            }
        }
        int minY = dx < 0 ? loc.getBlockY() - length + 1 : loc.getBlockY() + 1;
        int maxY = dx < 0 ? loc.getBlockY() : loc.getBlockY() + length;
        testLocation1 = loc.clone().add(0, 0, 1);
        testLocation2 = loc.clone().add(0, 0, -1);
        dx = 1;
        if (DataCache.getSfItem(testLocation1) instanceof SpaceTowerFrame) {
            if (DataCache.getSfItem(testLocation2) instanceof SpaceTowerFrame) {
                return null;
            } else {
                dx = 1;
            }
        } else if (DataCache.getSfItem(testLocation2) instanceof SpaceTowerFrame) {
            dx = -1;
        } else return null;
        length = 1;
        vec = new Vector(0, 0, dx);
        for (; length <= MAX_SIZE; length++) {
            testLocation1 = loc.clone().add(vec.clone().multiply(length));
            if (!(DataCache.getSfItem(testLocation1) instanceof SpaceTowerFrame)) {
                break;
            }
        }
        int minZ = dx < 0 ? loc.getBlockZ() - length + 1 : loc.getBlockZ() + 1;
        int maxZ = dx < 0 ? loc.getBlockZ() : loc.getBlockZ() + length;
        return new Pair<>(
                new Location(loc.getWorld(), minX, minY, minZ), new Location(loc.getWorld(), maxX, maxY, maxZ));
    }

    public void updateMenu(BlockMenu menu, Block block, Settings mod) {
        var locationInfo = getSpaceTowerRange(menu.getLocation());
        if (locationInfo == null) {
            menu.replaceExistingItem(
                    INFO_SLOT, new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&6點擊刷新狀態", "&c未檢測出延申的空間塔框架"));
        } else {
            Location start = locationInfo.getFirstValue();
            Location end = locationInfo.getSecondValue();
            int dx = end.getBlockX() - start.getBlockX();
            int dy = end.getBlockY() - start.getBlockY();
            int dz = end.getBlockZ() - start.getBlockZ();
            menu.replaceExistingItem(
                    INFO_SLOT,
                    new CustomItemStack(
                            Material.GREEN_STAINED_GLASS_PANE,
                            "&6狀態",
                            "&a檢測出框架圈成的空間",
                            "&7位置1(包含): " + DataCache.locationToDisplayString(start),
                            "&7位置2(不包含): " + DataCache.locationToDisplayString(end),
                            "&7空間大小: " + String.join("x", String.valueOf(dx), String.valueOf(dy), String.valueOf(dz))));
        }
    }

    public void tick(Block b, @Nullable BlockMenu menu, SlimefunBlockData data, int tickCount) {
        if (menu.hasViewer()) {
            updateMenu(menu, b, Settings.RUN);
        }
    }

    public void process(Block b, BlockMenu menu, SlimefunBlockData data) {}
}
