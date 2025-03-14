package beer.devs.rpgmoney.loots.listener;

import beer.devs.rpgmoney.Main;
import beer.devs.rpgmoney.Settings;
import beer.devs.rpgmoney.loots.LootInstance;
import beer.devs.rpgmoney.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LootsEventsListener implements Listener
{
    public LootsEventsListener() throws ClassNotFoundException
    {
        if(Main.IS_PAPER)
            Bukkit.getPluginManager().registerEvents(new PaperListener(), Main.inst);

        if(!Settings.ALLOW_ENTITIES_PICKUP_MONEY)
            Bukkit.getPluginManager().registerEvents(new EntityPickupItemEventListener(), Main.inst);
    }

    public static boolean wasLastPlaced(Player player, Block block)
    {
        if (player.getGameMode() == GameMode.CREATIVE && Main.config.getBoolean("placed_blocks_drop_money.creative"))
            return false;
        if (player.getGameMode() == GameMode.SURVIVAL && Main.config.getBoolean("placed_blocks_drop_money.survival"))
            return false;
        return block.hasMetadata("JustPlaced");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void omPlayerSwapHandItems(PlayerSwapHandItemsEvent e)
    {
        if (!Main.config.getBoolean("get_money_method.press_f"))
            return;
        if (!e.getOffHandItem().hasItemMeta())
            return;
        if (!e.getOffHandItem().getItemMeta().hasLore())
            return;
        if (!e.getOffHandItem().getItemMeta().getLore().contains(Main.inst.language.getColored("lore-sack-of-money")) &&
                !e.getOffHandItem().getItemMeta().getLore().contains(Main.inst.language.getColored("lore-money")))
            return;
        e.setCancelled(true);
        Player player = e.getPlayer();

        ItemMeta meta = e.getOffHandItem().getItemMeta();
        List<String> lore = meta.getLore();
        String amount = "0";
        if (!Main.config.getBoolean("show_player_name_sack_of_money"))
            amount = lore.get(1);
        else
            amount = lore.get(2);
        amount = ChatColor.stripColor(amount);
        amount = amount.replace(ChatColor.stripColor(Main.inst.language.getColored("lore-amount")), "");
        Main.economy.depositPlayer(e.getPlayer(), Double.parseDouble(amount));
        if (Settings.SHOW_ACTIONBAR_MESSAGES)
            player.sendActionBar(ChatColor.GREEN + Main.inst.language.getColored("pickup").replace("{money}", amount));

        Main.playPickupMoneySound(player);
        Utils.decrementAmountMainHand(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void PlayerSwapHandItemsEvent_money(PlayerSwapHandItemsEvent e)
    {
        if (!Main.config.getBoolean("get_money_method.press_f"))
            return;
        if (e.getOffHandItem() == null || e.getOffHandItem().getType() == Material.AIR)
            return;

        float money = Main.inst.getMoneyFromPickup(e.getOffHandItem());
        if (money == -1)
            return;

        e.setCancelled(true);
        Player player = e.getPlayer();

        Main.economy.depositPlayer(e.getPlayer(), money);
        if(Settings.SHOW_ACTIONBAR_MESSAGES)
            player.sendActionBar(ChatColor.GREEN + Main.inst.language.getColored("pickup").replace("{money}", money + ""));

        Main.playPickupMoneySound(player);
        Utils.decrementAmountMainHand(player);
    }

    @EventHandler
    private void useItemRightClick(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (e.getItem() == null || e.getItem().getType() == Material.AIR)
            return;
        if (!e.getItem().hasItemMeta())
            return;
        if (!e.getItem().getItemMeta().hasLore())
            return;
        if (!e.getItem().getItemMeta().getLore().contains(Main.inst.language.getColored("lore-sack-of-money")) &&
                !e.getItem().getItemMeta().getLore().contains(Main.inst.language.getColored("lore-money")))
            return;

        if (!Main.config.getBoolean("get_money_method.right_click"))
            return;

        e.setCancelled(true);

        Player player = e.getPlayer();
        if (!Main.inst.hasCurrentCashID(e.getItem()))
        {
            if (Settings.SHOW_ACTIONBAR_MESSAGES)
                player.sendActionBar(ChatColor.RED + Main.inst.language.getColored("disabled-item"));
            return;
        }

        ItemMeta meta = e.getItem().getItemMeta();
        List<String> lore = meta.getLore();
        String amount = "0";
        if (!Main.config.getBoolean("show_player_name_sack_of_money"))
            amount = lore.get(1);
        else
            amount = lore.get(2);
        amount = ChatColor.stripColor(amount);
        amount = amount.replace(ChatColor.stripColor(Main.inst.language.getColored("lore-amount")), "");
        Main.economy.depositPlayer(e.getPlayer(), Double.parseDouble(amount));
        if(Settings.SHOW_ACTIONBAR_MESSAGES)
            player.sendActionBar(ChatColor.GREEN + Main.inst.language.getColored("pickup").replace("{money}", amount));

        Main.playPickupMoneySound(player);

        Utils.decrementAmountMainHand(player);
    }

    @EventHandler
    private void useItemRightClick_money(PlayerInteractEvent e)
    {
        if (e.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (e.getItem() == null || e.getItem().getType() == Material.AIR)
            return;

        float money = Main.inst.getMoneyFromPickup(e.getItem());

        if (money == -1)
            return;

        e.setCancelled(true);

        if (!Main.config.getBoolean("get_money_method.right_click"))
            return;

        Player player = e.getPlayer();

        if (!Main.inst.hasCurrentCashID(e.getItem()))
        {
            if (Settings.SHOW_ACTIONBAR_MESSAGES)
                player.sendActionBar(ChatColor.RED + Main.inst.language.getColored("disabled-item"));
            return;
        }

        Main.inst.economy.depositPlayer(e.getPlayer(), money);
        if (Settings.SHOW_ACTIONBAR_MESSAGES)
            player.sendActionBar(ChatColor.GREEN + Main.inst.language.getColored("pickup").replace("{money}", money + ""));

        Main.playPickupMoneySound(player);

        Utils.decrementAmountMainHand(player);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPickup(PlayerPickupItemEvent e)
    {
        handlePickup(e.getItem(), e.getPlayer(), e);
    }

    public static void handlePickup(Item drop, Player player, Cancellable e)
    {
        float money = Main.inst.getMoneyFromPickup(drop.getItemStack());
        if (money == -1)
            return;

        e.setCancelled(true);

        if (Utils.isVanished(player))
            return;

        Main.inst.giveMoney(player, drop, money);
    }

    @EventHandler
    private void onDeath(EntityDeathEvent e)
    {
        if (!Settings.LOOTS_FROM_ENTITIES)
            return;

        Entity killedEntity = e.getEntity();
        if (Main.inst.isWorldDisabled(killedEntity.getLocation()))
            return;

        @Nullable Entity killer;
        if(killedEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent)
            killer = ((EntityDamageByEntityEvent) killedEntity.getLastDamageCause()).getDamager();
        else
            killer = e.getEntity().getKiller();

        @Nullable LootInstance lootInstance = Main.inst.entitiesLootsRegistry.get(killer, killedEntity);
        if (lootInstance == null || !lootInstance.loot.randomSuccess())
            return;

        // If it's natural death
        if (killer == null && lootInstance.loot.naturalDeath)
        {
            float modifier;
            if (Main.config.getBoolean("spawners.enable") && Main.inst.spawners.contains(killedEntity.getUniqueId()))
                modifier = Main.config.getInt("spawners.percentage_of_money");
            else
                modifier = lootInstance.loot.naturalDeathPercentage;

            if (modifier > 0)
            {
                if (killedEntity instanceof Player)
                {
                    Player killedPlayer = (Player) killedEntity;
                    int dropsAmount = lootInstance.loot.randomAmount();
                    for (int i = 0; i < dropsAmount; i++)
                    {
                        float money = lootInstance.calculateMoneyFromKilledEntity((Player) killedEntity);
                        money = Utils.takePercentage(money, modifier);

                        if (money > 0 && Main.inst.takeMoneyFromPlayer(money, killedPlayer))
                        {
                            if (Settings.SHOW_ACTIONBAR_MESSAGES)
                                killedPlayer.sendActionBar(ChatColor.RED + Main.inst.language.getColored("drop-message").replace("{money}", String.valueOf(money)));
                            Main.inst.spawnMoney(money, killedEntity.getLocation());
                        }
                    }

                    if(dropsAmount > 0)
                        lootInstance.increaseTracker();
                }
                else
                {
                    int dropsAmount = lootInstance.loot.randomAmount();
                    for (int i = 0; i < dropsAmount; i++)
                    {
                        float money = Utils.takePercentage(lootInstance.calculateMoney(), modifier);
                        Main.inst.spawnMoney(money, killedEntity.getLocation());
                    }

                    if(dropsAmount > 0)
                        lootInstance.increaseTracker();
                }
            }
        }
        else
        {
            if(lootInstance.loot.onlyIfKillerIsAPlayer && !(killer instanceof Player))
                return;

            if (killer instanceof Player && !killer.hasPermission("rpgmoney.drop.kill"))
                return;

            if (killedEntity instanceof Player)
            {
                Player killedPlayer = (Player) killedEntity;
                float money = lootInstance.calculateMoneyFromKilledEntity((Player) killedEntity);
                if (money > 0 && Main.inst.takeMoneyFromPlayer(money, killedPlayer))
                {
                    if (Settings.SHOW_ACTIONBAR_MESSAGES)
                        killedPlayer.sendActionBar(ChatColor.RED + Main.inst.language.getColored("drop-message").replace("{money}", String.valueOf(money)));
                    Main.inst.spawnMoney(killer, money, killedEntity.getLocation());
                    lootInstance.increaseTracker();
                }
            }
            else
            {
                int perc = 100;
                if(Main.config.getBoolean("spawners.enable"))
                {
                    if (Main.inst.spawners.contains(killedEntity.getUniqueId()))
                        perc = Main.inst.config.getInt("spawners.percentage_of_money");
                }

                if (perc > 0)
                {
                    float money = Utils.takePercentage(lootInstance.calculateMoney(), perc);
                    int dropsAmount = lootInstance.loot.randomAmount();
                    for (int i = 0; i < dropsAmount; i++)
                    {
                        Main.inst.spawnMoney(killer, money, killedEntity.getLocation());
                    }

                    if(dropsAmount > 0)
                        lootInstance.increaseTracker();
                }
            }
        }

        Main.inst.spawners.remove(e.getEntity().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockBreak(BlockBreakEvent e)
    {
        if (!Settings.LOOTS_FROM_BLOCKS)
            return;

        Player player = e.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL)
            return;

        if (Utils.hasEnchant(player.getInventory().getItemInMainHand(), Enchantment.SILK_TOUCH))
            return;

        Block block = e.getBlock();
        if (!Utils.isGrowable(block) && wasLastPlaced(player, block))
            return;

        if (!Utils.isFullyGrown(block))
            return;

        if (Main.inst.isWorldDisabled(block.getLocation()))
            return;

        @Nullable LootInstance data = Main.inst.blocksLootsRegistry.get(player, block);
        if (data != null && data.loot.randomSuccess())
        {
            if (!player.hasPermission("rpgmoney.drop.blockbreak"))
                return;

            int randomAmount = data.loot.randomAmount();
            for (int i = 0; i < randomAmount; i++)
            {
                float randomMoney = data.calculateMoney();
                Main.inst.spawnMoney(player, randomMoney, block.getLocation());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerFish(PlayerFishEvent e)
    {
        if (!Settings.LOOTS_FROM_FISHES)
            return;
        if (e.getState() != PlayerFishEvent.State.CAUGHT_ENTITY && e.getState() != PlayerFishEvent.State.CAUGHT_FISH)
            return;

        Entity caught = e.getCaught();
        if (caught == null || caught.getTicksLived() > 0) // getTicksLived > 0 means it's a drop on the ground
            return;

        if (!(caught instanceof Item)) // Wtf? Can this happen?
            return;

        //TODO: IMPORTANTE, CONTROLLARE FORSE SE LIVED TICKS > X,
        // altrimenti ignoro se no uno sfrutta il buggggg -> EDIT: ma di che cazzo stai parlando?
        ItemStack caughtItem = ((Item) caught).getItemStack();
        @Nullable LootInstance data = Main.inst.fishesLootsRegistry.get(e.getPlayer(), caughtItem);
        if (data != null && data.loot != null && data.loot.randomSuccess())
        {
            if (!e.getPlayer().hasPermission("rpgmoney.drop.fishing"))
                return;

            int randomAmount = data.loot.randomAmount();
            for (int i = 0; i < randomAmount; i++)
            {
                Entity item = Main.inst.spawnMoney(e.getPlayer(), data.calculateMoney(), caught.getLocation());
                if (item != null)
                    item.teleport(e.getPlayer().getLocation());
            }

            if(randomAmount > 0)
                data.increaseTracker();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPlace(BlockPlaceEvent e)
    {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && Main.config.getBoolean("placed_blocks_drop_money.creative"))
            return;
        if (player.getGameMode() == GameMode.SURVIVAL && Main.config.getBoolean("placed_blocks_drop_money.survival"))
            return;

        Block block = e.getBlockPlaced();
        block.setMetadata("JustPlaced", new FixedMetadataValue(Main.inst, true));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBlockPistonExtend(BlockPistonExtendEvent e)
    {
        // To avoid pistons exploit to be able to get drops from silk touch (then placed) blocks.
        for (Block block : e.getBlocks())
        {
            block.getRelative(e.getDirection().getOppositeFace().getOppositeFace()).setMetadata("JustPlaced", new FixedMetadataValue(Main.inst, true));
        }
    }

    @EventHandler
    private void onCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        // Fix essentials repair command changing the money texture.
        String[] array = event.getMessage().split(" ");
        if (array[0].equalsIgnoreCase("/fix") || array[0].equalsIgnoreCase("/repair"))
        {
            if (!event.getPlayer().getItemInHand().hasItemMeta() || !event.getPlayer().getItemInHand().getItemMeta().hasLore())
                return;
            if (event.getPlayer().getItemInHand().getItemMeta().getLore().contains(Main.inst.language.getColored("lore-sack-of-money"))
                    || event.getPlayer().getItemInHand().getItemMeta().getLore().contains(Main.inst.language.getColored("lore-money")))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onInventoryClick(InventoryClickEvent e)
    {
        if (e.getAction() == InventoryAction.NOTHING)
            return;

        // Avoid using money items for recipes
        if (e.getInventory().getType() == InventoryType.WORKBENCH)
        {
            if (Main.inst.isSackOfMoney(e.getCurrentItem()) || Main.inst.isSackOfMoney(e.getCursor()))
                e.setCancelled(true);
            if (Main.inst.isMoneyPickup(e.getCurrentItem()) || Main.inst.isMoneyPickup(e.getCursor()))
                e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onSpawner(CreatureSpawnEvent e)
    {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
        {
            Main.inst.spawners.add(e.getEntity().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onHopper(InventoryPickupItemEvent e)
    {
        if (Main.config.getBoolean("allow_money_in_hoppers"))
            return;

        if (e.getInventory().getType() != InventoryType.HOPPER)
            return;

        if (!Main.inst.isMoneyPickup(e.getItem().getItemStack()))
            return;

        e.setCancelled(true);
    }
}
