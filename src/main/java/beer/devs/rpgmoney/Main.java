package beer.devs.rpgmoney;

import beer.devs.fastnbt.nms.Version;
import beer.devs.rpgmoney.utils.*;
import de.tr7zw.changeme.nbtapi.NBTItem;
import dev.lone.itemsadder.api.ItemsAdder;
import beer.devs.rpgmoney.loots.config.BlocksLootsRegistry;
import beer.devs.rpgmoney.loots.config.EntitiesLootsRegistry;
import beer.devs.rpgmoney.loots.config.FishesLootsRegistry;
import beer.devs.rpgmoney.loots.listener.LootsEventsListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Main extends JavaPlugin implements Listener
{
    int RESPACK_VERSION = 4;
    String RESPACK_LINK = "https://www.matteodev.it/spigot/realmoney/res/RealMoney3.3.zip";

    public static Main inst;
    public static Economy economy = null;

    public static String PREFIX = "[RPGMoney] ";

    public static boolean IS_PAPER;
    public static boolean HAS_ITEMSADDER = false;

    public static CustomConfigFile config;
    public static CustomConfigFile language;

    public static HashMap<String, ItemStack> cachedHeadItems = new HashMap<>();

    public List<UUID> spawners = new ArrayList<>();

    public BlocksLootsRegistry blocksLootsRegistry = new BlocksLootsRegistry(this);
    public FishesLootsRegistry fishesLootsRegistry = new FishesLootsRegistry(this);
    public EntitiesLootsRegistry entitiesLootsRegistry = new EntitiesLootsRegistry(this);

    public static List<Permission> permissions = new ArrayList<>();
    public static HashMap<String, Integer> multipliersDrops = new HashMap<>();
    public static HashMap<String, Integer> multipliersPickup = new HashMap<>();

    private static boolean shownFailedIconError = false;

    @Override
    public void onEnable()
    {
        inst = this;

        if (Version.isOlderThan(Version.v1_20_5))
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This plugin is not compatible with this version of Minecraft.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Please update your server to 1.20.5 or newer.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        initConfig();

        PREFIX = language.getColored("prefix");

        if (!getServer().getPluginManager().isPluginEnabled("Vault"))
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Vault not detected! Disabling plugin");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        IS_PAPER = isPaper();

        if (getServer().getPluginManager().isPluginEnabled("ItemsAdder"))
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Detected ItemsAdder! Working with it (using its textures files)");
            HAS_ITEMSADDER = true;
        }

        if (!setupEconomy())
        {
            Bukkit.getLogger().info("Disabled due to no Vault dependency found or no economy plugin installed (example: EssentialsX)!");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new ResPackManager(this, config, language);

        try
        {
            Bukkit.getServer().getPluginManager().registerEvents(new LootsEventsListener(), this);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        updateResPack();

        getCommand("rpgmoney").setExecutor(this);
    }

    @Override
    public void onDisable()
    {
        for (Permission permission : permissions)
        {
            Bukkit.getPluginManager().removePermission(permission);
        }
    }

    public static boolean isPaper()
    {
        String name = Bukkit.getServer().getName();
        if (name.contains("Paper") || name.contains("Purpur"))
            return true;

        return
                hasClass("com.destroystokyo.paper.event.player.PlayerSetSpawnEvent$Cause") ||
                        hasClass("com.destroystokyo.paper.utils.PaperPluginLogger") ||
                        hasClass("io.papermc.paper.ServerBuildInfo") ||
                        hasClass("io.papermc.paper.text.PaperComponents");
    }

    private static boolean hasClass(String path)
    {
        try
        {
            Class.forName(path);
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    void updateResPack()
    {
        if (config.getInt("resource-pack-version") < RESPACK_VERSION)
        {
            config.set("resource-pack-version", RESPACK_VERSION);
            config.set("resource-pack-link", RESPACK_LINK);
        }
    }

    private void initConfig()
    {
        config = new CustomConfigFile(this, "config", true);
        language = new CustomConfigFile(this, "language", true);
        Settings.load(config);

        entitiesLootsRegistry = new EntitiesLootsRegistry(this);
        blocksLootsRegistry = new BlocksLootsRegistry(this);
        fishesLootsRegistry = new FishesLootsRegistry(this);

        ConfigurationSection sec = config.getConfig().getConfigurationSection("multipliers_groups.drop");
        if (sec != null)
        {
            for (String key : sec.getKeys(false))
            {
                String permissionStr = "rpgmoney.multiply.drop." + key;
                multipliersDrops.put(permissionStr, sec.getInt(key));
                Permission permission = new Permission(permissionStr, PermissionDefault.FALSE);
                permissions.add(permission);
                Bukkit.getServer().getPluginManager().addPermission(permission);
            }
        }

        sec = config.getConfig().getConfigurationSection("multipliers_groups.pickup");
        if (sec != null)
        {
            for (String key : sec.getKeys(false))
            {
                String permissionStr = "rpgmoney.multiply.pickup." + key;
                multipliersPickup.put(permissionStr, sec.getInt(key));
                Permission permission = new Permission(permissionStr, PermissionDefault.FALSE);
                permissions.add(permission);
                Bukkit.getServer().getPluginManager().addPermission(permission);
            }
        }
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = (RegisteredServiceProvider<Economy>) this.getServer().getServicesManager().getRegistration((Class) Economy.class);
        if (economyProvider != null)
            economy = economyProvider.getProvider();
        return (economy != null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnableEvent(PluginEnableEvent e)
    {
        //WARNING: https://hub.spigotmc.org/jira/browse/SPIGOT-6209

        if (e.getPlugin().getName().equals("ItemsAdder"))
        {
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Detected ItemsAdder! Working with it (using its textures files)");
            HAS_ITEMSADDER = true;
        }
    }

    private void showHelp(CommandSender sender)
    {
        sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------" + ChatColor.RED + "\n ");
        sender.sendMessage(ChatColor.GOLD + "RPGMoney  version: " + ChatColor.AQUA + getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "Server version: " + ChatColor.AQUA + Bukkit.getVersion());
        if (sender.hasPermission("rpgmoney.reload") || sender.hasPermission("rpgmoney.get"))
            sender.sendMessage(language.getColored("commands"));
        if (sender.hasPermission("rpgmoney.reload"))
            sender.sendMessage(language.getColored("reload-help"));
        if (sender.hasPermission("rpgmoney.get"))
            sender.sendMessage(language.getColored("get-help"));
        sender.sendMessage(ChatColor.RED + "\n" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (args.length >= 1)
        {
            try
            {
                switch (args[0])
                {
                    case "reload":
                        if (sender.hasPermission("rpgmoney.reload"))
                        {
                            shownFailedIconError = false;
                            reloadConfig();
                            initConfig();
                            Main.inst.blocksLootsRegistry.reloadConfig();
                            Main.inst.fishesLootsRegistry.reloadConfig();
                            Main.inst.entitiesLootsRegistry.reloadConfig();
                            sender.sendMessage(language.getColored("reload"));
                        }
                        break;
                    case "get":
                        boolean bypassWithdraw = sender.hasPermission("rpgmoney.get.nowithdraw");

                        if (sender.hasPermission("rpgmoney.get"))
                        {
                            if (!(sender instanceof Player))
                            {
                                sender.sendMessage(Main.PREFIX + ChatColor.RED + "This command is only for users.");
                                return true;
                            }
                            Player player = (Player) sender;
                            if (args.length >= 2 && args[1] != null && Utils.isNumeric(args[1]))
                            {
                                if (Double.parseDouble(args[1]) == 0)
                                {
                                    if (Settings.SHOW_ACTIONBAR_MESSAGES)
                                        player.sendActionBar(language.getColored("invalid-amount"));
                                    showHelp(sender);
                                    return true;
                                }
                                if (bypassWithdraw || economy.getBalance(player) - Double.parseDouble(args[1]) >= 0)
                                {
                                    double amount = Double.parseDouble(args[1]);

                                    double coinThresh = config.getDouble("item_icon_thresholds.coin", 0);
                                    double banknoteThresh = config.getDouble("item_icon_thresholds.banknote", 5);
                                    double sack_of_moneyThresh = config.getDouble("item_icon_thresholds.sack_of_money", 100);

                                    ItemStack item = getSackOfMoney();
                                    if (amount >= sack_of_moneyThresh)
                                    {
                                        item = getSackOfMoney();
                                    }
                                    else if (amount > banknoteThresh)
                                    {
                                        item = getBanknoteItem();
                                    }
                                    else if (amount > coinThresh)
                                    {
                                        item = getCoinItem();
                                    }

                                    ItemMeta meta = item.getItemMeta();
                                    List<String> lore = new ArrayList<>();
                                    meta.setUnbreakable(true);
                                    meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});

                                    if (amount >= sack_of_moneyThresh)
                                    {
                                        meta.setDisplayName(language.getColored("sack-of-money"));
                                        lore.add(language.getColored("lore-sack-of-money"));
                                    }
                                    else
                                    {
                                        meta.setDisplayName(language.getColored("money"));
                                        lore.add(language.getColored("lore-money"));
                                    }


                                    if (config.getBoolean("show_player_name_sack_of_money"))
                                        lore.add(language.getColored("lore-player-name") + sender.getName());


                                    String formatted = "";
                                    if (amount % 1 != 0)
                                        formatted = String.format(Locale.ROOT, "%.2f", amount);
                                    else
                                        formatted = String.format(Locale.ROOT, "%.0f", amount);

                                    lore.add(language.getColored("lore-amount") + formatted);

                                    if (Main.config.getBoolean("get_money_method.press_f"))
                                        lore.add(language.getColored("get-money-from-sack-guide"));

                                    meta.setLore(lore);
                                    item.setItemMeta(meta);
                                    player.getInventory().addItem(item);

                                    if (!bypassWithdraw)
                                        economy.withdrawPlayer(player, Double.parseDouble(args[1]));
                                    if (Settings.SHOW_ACTIONBAR_MESSAGES)
                                        player.sendActionBar(language.getColored("obtained") + language.getColored("money"));
                                }
                                else
                                {
                                    if (Settings.SHOW_ACTIONBAR_MESSAGES)
                                        player.sendActionBar(language.getColored("no-enough-money"));
                                }
                            }
                            else
                            {
                                if (Settings.SHOW_ACTIONBAR_MESSAGES)
                                    player.sendActionBar(language.getColored("invalid-amount"));
                                showHelp(sender);
                            }
                        }
                        break;
                    default:
                        showHelp(sender);
                        break;
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            showHelp(sender);
        }
        return true;
    }

    public static int getMultiplierDrop(Entity player)
    {
        if (player == null)
            return 1;

        int multiplier = 1;
        for (Map.Entry<String, Integer> entry : multipliersDrops.entrySet())
            if (player.hasPermission(entry.getKey()))
                multiplier = entry.getValue();
        return multiplier;
    }

    @Nullable
    public static Integer getMultiplierPickup(Player player)
    {
        @Nullable Integer multiplier = null;
        for (Map.Entry<String, Integer> entry : multipliersPickup.entrySet())
            if (player.hasPermission(entry.getKey()))
                multiplier = entry.getValue();
        return multiplier;
    }

    public static boolean takeMoneyFromPlayer(float amount, Player player)
    {
        if (economy.getBalance(player) >= amount)
        {
            economy.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public static void giveMoney(Player player, Item item, float money)
    {
        if (player.hasPermission("rpgmoney.pickup"))
        {
            if (item != null)
                item.remove();

            @Nullable Integer pickupMultiplier = getMultiplierPickup(player);
            if (pickupMultiplier != null)
                money *= pickupMultiplier;

            economy.depositPlayer(player, money);

            if (Settings.SHOW_ACTIONBAR_MESSAGES)
            {
                String m;
                if (money % 1 != 0)
                    m = String.format(Locale.ROOT, "%.2f", money);
                else
                    m = String.format(Locale.ROOT, "%.0f", money);

                if (pickupMultiplier == null)
                {
                    player.sendActionBar(ChatColor.GREEN + inst.language.getColored("pickup").replace("{money}", m));
                }
                else
                {
                    player.sendActionBar(ChatColor.GREEN + inst.language.getColored("pickup_with_pickup_multiplier")
                            .replace("{money}", m)
                            .replace("{multiplier}", String.valueOf(pickupMultiplier)));
                }
            }

            playPickupMoneySound(player);
        }
    }

    public static void playPickupMoneySound(Player p)
    {
        if (config.getBoolean("pickup_sound.enabled"))
        {
            try
            {
                p.getLocation().getWorld().playSound(p.getLocation(),
                        Sound.valueOf(config.getString("pickup_sound.type")),
                        (float) config.getDouble("pickup_sound.volume"),
                        (float) config.getDouble("pickup_sound.pitch"));
            }
            catch (IllegalArgumentException e)
            {
                p.getLocation().getWorld().playSound(p.getLocation(),
                        config.getString("pickup_sound.type"),
                        (float) config.getDouble("pickup_sound.volume"),
                        (float) config.getDouble("pickup_sound.pitch"));
            }
        }
    }

    private static float mergeNearMoney(Location location, float money)
    {
        if (Settings.PICKUPS_MERGE)
        {
            Collection<Item> nearDrops = location.getWorld().getNearbyEntitiesByType(Item.class,
                    location,
                    Settings.PICKUPS_MERGE_RADIUS,
                    Settings.PICKUPS_MERGE_RADIUS,
                    Settings.PICKUPS_MERGE_RADIUS);

            int count = 0;
            for (Item drop : nearDrops)
            {
                float otherMoney = Main.inst.getMoneyFromPickup(drop.getItemStack());
                if (otherMoney == -1)
                    continue;
                money += otherMoney;
                count++;
            }

            if (count >= Settings.PICKUPS_MERGE_MIN_PICKUPS)
            {
                nearDrops.forEach(Entity::remove);
                return Utils.round(money, 2);
            }
        }

        return money;
    }

    @Nullable
    public static Item spawnMoney(Entity player, float money, Location location)
    {
        money *= getMultiplierDrop(player);
        money = mergeNearMoney(location, money);

        String formatted = format(money);

        // Paper can detect pickup with full inventory. Check my PaperListener class.
        if (player instanceof Player)
        {
            if (!Main.IS_PAPER && Utils.isInventoryFull((Player) player))
            {
                giveMoney((Player) player, null, money);
                return null;
            }
        }

        Item item = location.getWorld().dropItemNaturally(location, getItem(money));
        item.setCustomName(Main.language.getColored("currency-format").replace("{money}", formatted));
        item.setCustomNameVisible(true);

        return item;
    }

    public static void spawnMoney(float money, Location location)
    {
        money = mergeNearMoney(location, money);
        String formatted = format(money);

        Item item = location.getWorld().dropItemNaturally(location, getItem(money));
        item.setCustomName(language.getColored("currency-format").replace("{money}", formatted));
        item.setCustomNameVisible(true);
    }

    public static String format(float money)
    {
        String formatted;
        if (Main.config.getBoolean("remove_decimals.enabled"))
        {
            if (Main.config.getBoolean("remove_decimals.approximate_bigger"))
            {
                if (money - (int) money > 0.5d)
                    money = (int) money + 1;
                else
                    money = (int) money;
            }
            else
                money = (int) money;

            if (money <= 0.0001f)
                return null;

            formatted = ((money - (int) money) == 0) ? String.valueOf((int) money) : String.valueOf(money);
        }
        else
        {
            if (money % 1 != 0)
                formatted = String.format(Locale.ROOT, "%.2f", money);
            else
                formatted = String.format(Locale.ROOT, "%.0f", money);
        }

        return formatted;
    }

    public boolean isWorldDisabled(Location location)
    {
        return config.getList("disabled_worlds").contains(location.getWorld().getName());
    }

    private static ItemStack getItem(String type, int id)
    {
        String iconSetting = Main.config.getString("item_icon." + type);

        if (iconSetting.equals("default"))
        {
            //default
            ItemStack icon = new ItemStack(Material.WOODEN_AXE, 1, (short) id); // TODO: refactor using CustomModelData
            return setCashID(icon);
        }
        else if (iconSetting.length() >= 125) // Probably head texture
        {
            if (cachedHeadItems.containsKey(type))
                return cachedHeadItems.get(type);

            ItemStack icon = Heads.get(iconSetting);
            cachedHeadItems.put(type, icon);
            return icon;
        }
        else if (HAS_ITEMSADDER)
        {
            ItemStack icon = ItemsAdder.getCustomItem(iconSetting);
            if (icon != null)
            {
                icon = icon.clone();
                return setCashID(icon);
            }
        }

        ItemStack icon;
        try
        {
            Material material = Material.valueOf(iconSetting);
            icon = new ItemStack(material, 1);
        }
        catch (Throwable ex)
        {
            if (!shownFailedIconError)
            {
                inst.getLogger().warning("Failed to find icon '" + iconSetting + "'. Using default.");
                inst.getLogger().warning("This is caused by a dependency failing to load or a bad icon name in RPGMoney configuration. Check your logs.");
                shownFailedIconError = true;
            }

            icon = new ItemStack(Material.WOODEN_AXE, 1, (short) id);
        }

        return setCashID(icon);
    }

    static ItemStack setCashID(ItemStack itemStack)
    {
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("CashID", Main.config.getString("cash_id.id"));
        return nbtItem.getItem();
    }

    public static ItemStack getCoinItem()
    {
        return getItem("coin", 2);
    }

    public static ItemStack getBanknoteItem()
    {
        return getItem("banknote", 1);
    }

    public static ItemStack getSackOfMoney()
    {
        return getItem("sack_of_money", 3);
    }

    public static ItemStack getItem(float money)
    {
        String m = String.valueOf(money);
        ItemStack item;
        item = new ItemStack(Material.WOODEN_AXE, 1, (short) 1); // TODO: refactor using CustomModelData
        if (money < 5.0f)
            item = getCoinItem().clone();
        else if (money >= 5.0f)
            item = getBanknoteItem().clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(language.getColored("currency-format").replace("{money}", m));
        meta.setUnbreakable(true);
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE});
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setBoolean("RPGMoney", true);
        nbtItem.setFloat("Money", money);
        nbtItem.setInteger("Rnd", Utils.getRandomInt(1, 100000000));
        nbtItem.setString("CashID", Main.config.getString("cash_id.id"));
        return nbtItem.getItem();
    }

    public boolean isMoneyPickup(ItemStack item)
    {
        if (isNewMoneyPickup(item))
            return true;
        return false;
    }

    private static boolean isNewMoneyPickup(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR)
            return false;
        if (!item.hasItemMeta())
            return false;
        return new NBTItem(item).hasKey("RPGMoney");
    }

    public boolean isSackOfMoney(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR)
            return false;
        if (!item.hasItemMeta())
            return false;
        if (!item.getItemMeta().hasLore())
            return false;
        //noinspection DataFlowIssue
        return item.getItemMeta().getLore().contains(inst.language.getColored("lore-sack-of-money")) ||
                item.getItemMeta().getLore().contains(inst.language.getColored("lore-money"));
    }

    public float getMoneyFromPickup(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR)
            return -1;

        if (!item.hasItemMeta())
            return -1;

        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.hasKey("RPGMoney"))
            return nbtItem.getFloat("Money");
        return -1;
    }

    public String getCashIDFromPickup(ItemStack item)
    {
        if (item == null || item.getType() == Material.AIR)
            return "";
        if (!item.hasItemMeta())
            return "";

        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.hasKey("CashID"))
            return nbtItem.getString("CashID");
        return Main.config.getString("cash_id.id");
    }

    public boolean hasCurrentCashID(ItemStack item)
    {
        return Main.config.getString("cash_id.id").equals(getCashIDFromPickup(item));
    }
}