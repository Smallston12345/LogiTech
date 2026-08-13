package me.matl114.logitech.core.Blocks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import me.matl114.logitech.listeners.Listeners.SlimefunBlockPlaceLimitListener;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.TickerClass.Ticking;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SpreadBlock extends AbstractBlock implements Ticking {
    protected final SlimefunItem RESULT;
    protected final Material SPREAD_MATERIAL;
    protected final Material RESULT_MATERIAL;

    public SpreadBlock(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            SlimefunItem result,
            Material spreadMaterial,
            Material finalMaterial) {
        super(itemGroup, item, recipeType, recipe);
        this.RESULT = result;
        this.SPREAD_MATERIAL = spreadMaterial;
        this.RESULT_MATERIAL = finalMaterial;
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c同化",
                        "&7該物品擁有着將鄰接的方塊同化為自身的能力",
                        "&7方塊嘗試同化鄰接方塊的行為,我們稱之為一次同化",
                        "&7被玩家放下或者被同化出來的方塊會進行一次同化",
                        "&a進行同化行為後該位置的方塊將將不會繼續同化,也不會被同化",
                        "&7在隨機的時長後被該位置的方塊將會轉變為目標物品",
                        "&e當轉變為目標物品時,將有%d%%的機率不改變方塊材質".formatted(CHANCE_KEEP_MATERIAL)),
                AddUtils.addLore(result.getItem(), "&a同化最終的結果"),
                AddUtils.getInfoShow(
                        "&f機制 - &c反能量",
                        "&7該物品同化的能量來源是所謂的\"反能量值\"",
                        "&7當玩家放下時候,方塊反能量值為%d".formatted(LIFE_DEFAULT),
                        "&7被同化出的方塊將會繼承同化者的反能量值,並有60%的機率衰減1",
                        "&7同化進行後的方塊每sft將有60%的機率將自身反能量值衰減1",
                        "&7當反能量值為0的時候,方塊將轉變為目標物品"),
                null,
                AddUtils.getInfoShow(
                        "&f機制 - &c數量閾值",
                        "&7整個伺服器中最多有%d個方塊同時進行同化行為".formatted(ONE_TICK_SPREAD_MAXCNT),
                        "&7當超過該閾值後餘下的方塊將&c停止運行",
                        "&a因此該物品並不會對伺服器tps造成影響")));
    }

    protected Random rand = new Random();
    protected final boolean ENABLE_SPREAD = FeatureConfig.getBoolean("SpreadBlock.Antimass.enable-spread", true);
    protected final int LIFE_DEFAULT = Math.max(0, FeatureConfig.getInt("SpreadBlock.Antimass.life-default", 13));
    protected final int ONE_TICK_SPREAD_MAXCNT =
            Math.max(0, FeatureConfig.getInt("SpreadBlock.Antimass.max-spread-per-tick", 1600));
    protected final int CHANCE_KEEP_MATERIAL = Math.max(
            0, Math.min(100, FeatureConfig.getInt("SpreadBlock.Antimass.chance-keep-material", 2)));
    protected final int TICK_RATE = Math.max(1, FeatureConfig.getInt("SpreadBlock.Antimass.tick-rate", 1));
    protected final ConcurrentHashMap<Location, Player> SPREAD_PLAYER = new ConcurrentHashMap<>();

    public Map<Location, Player> getSpreadOwner() {
        return SPREAD_PLAYER;
    }

    protected final ConcurrentHashMap<Location, Integer> SPREAD_TICKER = new ConcurrentHashMap<>();
    protected final Set<Location> PENDING_CREATE = ConcurrentHashMap.newKeySet();

    public Map<Location, Integer> getSpreadTicker() {
        return SPREAD_TICKER;
    }

    protected final Vector[] SPREAD_DELTA = new Vector[] {
        new Vector(1, 0, 0),
        new Vector(0, 0, 1),
        new Vector(-1, 0, 0),
        new Vector(0, 0, -1),
        new Vector(0, 1, 0),
        new Vector(0, -1, 0),
    };

    public void createResultAt(Location loc) {
        Player player = SPREAD_PLAYER.remove(loc);
        SPREAD_TICKER.remove(loc);
        WorldUtils.createSlimefunBlock(
                loc,
                player,
                RESULT,
                (rand.nextInt(100) < (100 - CHANCE_KEEP_MATERIAL)) ? RESULT_MATERIAL : SPREAD_MATERIAL,
                true,
                () -> {});
    }

    public void registerTick(SlimefunItem item) {
        item.addItemHandler(new BlockTicker() {
            int runPerTick = 0;
            int currentTick = 0;

            public boolean isSynchronized() {
                return isSync();
            }

            @ParametersAreNonnullByDefault
            public void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
                if (currentTick % TICK_RATE != 0) return;
                Location loc = data.getLocation();
                if (PENDING_CREATE.contains(loc)) {
                    return;
                }
                Integer life = SPREAD_TICKER.get(loc);
                if (life == null || life == 0) {
                    createResultAt(loc);
                } else if (life < 0) {
                    SPREAD_TICKER.put(loc, life + rand.nextInt(100) < 60 ? 1 : 0);
                } else {
                    int lifeToSet = life - (rand.nextInt(100) < 60 ? 1 : 0);
                    Player player = SPREAD_PLAYER.get(loc);
                    for (int i = 0; ENABLE_SPREAD && i < SPREAD_DELTA.length && runPerTick < ONE_TICK_SPREAD_MAXCNT; i++) {
                        Location loc2 = loc.clone().add(SPREAD_DELTA[i]);
                        if (SPREAD_TICKER.containsKey(loc2)) {
                            continue;
                        }
                        SlimefunItem it = DataCache.getSfItem(loc2);
                        if (it != SpreadBlock.this
                                && it != RESULT
                                && !loc2.getBlock().getType().isAir()) {
                            runPerTick++;
                            PENDING_CREATE.add(loc2);
                            WorldUtils.createSlimefunBlock(
                                    loc2, player, SpreadBlock.this, SPREAD_MATERIAL, false, () -> {
                                        // create after permission check ,if fail, no create
                                        SPREAD_TICKER.put(loc2, lifeToSet);
                                        SPREAD_PLAYER.put(loc2, player);
                                        PENDING_CREATE.remove(loc2);
                                    });
                        }
                    }
                    SPREAD_TICKER.put(loc, -life + 1);
                }
            }

            @Override
            public void uniqueTick() {
                this.runPerTick = 0;
                this.currentTick = (this.currentTick + 1) % TICK_RATE;
            }
        });
    }

    public void tick(Block b, @Nullable BlockMenu menu, SlimefunBlockData data, int tickCount) {}

    public void onPlace(BlockPlaceEvent e, Block b) {
        SPREAD_PLAYER.put(b.getLocation(), e.getPlayer());
        SPREAD_TICKER.put(b.getLocation(), LIFE_DEFAULT);
        b.setType(SPREAD_MATERIAL);
    }

    public boolean isSync() {
        return false;
    }

    public void preRegister() {
        super.preRegister();
        registerTick(this);
        handleBlock(this);
        SlimefunBlockPlaceLimitListener.registerBlockLimit(this, (event) -> {
            if (!this.SPREAD_TICKER.isEmpty()) {
                reCheckSpreadTicker();
                if (!this.SPREAD_TICKER.isEmpty()) {
                    event.setCancelled(true);
                    if (event.getPlayer() != null) {
                        AddUtils.sendMessage(
                                event.getPlayer(),
                                "&c不能同時存在超過1個[%s]&c的擴散進程"
                                        .formatted(this.getItem().getItemMeta().getDisplayName()));
                    }
                }
            }
        });
    }

    public void reCheckSpreadTicker() {
        var iter = SPREAD_TICKER.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next().getKey();
            if (DataCache.getSfItem(entry) != this) {
                iter.remove();
            }
        }
    }
}
