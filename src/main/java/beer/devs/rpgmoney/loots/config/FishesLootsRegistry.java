package beer.devs.rpgmoney.loots.config;

import beer.devs.rpgmoney.Main;
import beer.devs.rpgmoney.loots.LootInstance;
import beer.devs.rpgmoney.loots.antifarm.FishingTracker;
import beer.devs.rpgmoney.utils.Utils;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class FishesLootsRegistry extends AbstractLootsRegistry
{
    public FishingTracker tracker = new FishingTracker(this);

    public FishesLootsRegistry(Main plugin)
    {
        super(plugin, "fishes");
        configFile = new File("plugins/RPGMoney/loots_fishes.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        reloadConfig();
    }

    @Nullable
    public LootInstance get(Player player, ItemStack itemStack)
    {
        World world = player.getWorld();
        LootInstance lootInstance = new LootInstance(tracker, player);
        if (Main.HAS_ITEMSADDER)
        {
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            if (customStack != null)
            {
                LootData lootData = get(world, customStack.getNamespacedID());
                if (lootData != null)
                {
                    lootInstance.loot = get(world, customStack.getNamespacedID());
                    lootInstance.matchedId = customStack.getNamespacedID();
                }
            }
        }
        else
        {
            lootInstance.loot = get(world, Utils.itemId(itemStack));
            lootInstance.matchedId = Utils.itemId(itemStack);
        }

        if(lootInstance.loot == null)
            return null;

        return lootInstance;
    }
}
