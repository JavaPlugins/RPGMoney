package beer.devs.rpgmoney.loots.config;

import beer.devs.rpgmoney.Main;
import beer.devs.rpgmoney.loots.LootInstance;
import beer.devs.rpgmoney.loots.antifarm.BlocksTracker;
import beer.devs.rpgmoney.utils.Utils;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BlocksLootsRegistry extends AbstractLootsRegistry
{
    BlocksTracker tracker = new BlocksTracker(this);

    public BlocksLootsRegistry(Main plugin)
    {
        super(plugin, "blocks");
        configFile = new File("plugins/RPGMoney/loots_blocks.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        reloadConfig();
    }

    @Nullable
    public LootInstance get(Player player, Block block)
    {
        World world = player.getWorld();
        LootInstance lootInstance = new LootInstance(tracker, player);
        if (Main.HAS_ITEMSADDER)
        {
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
            if (customBlock != null)
            {
                LootData lootData = get(world, customBlock.getNamespacedID());
                if (lootData != null)
                {
                    lootInstance.loot = get(world, customBlock.getNamespacedID());
                    lootInstance.setMatchedId(customBlock.getNamespacedID());
                }
            }
        }
        else
        {
            lootInstance.loot = get(world, Utils.blockId(block));
            lootInstance.setMatchedId(Utils.blockId(block));
        }

        if(lootInstance.loot == null)
            return null;

        return lootInstance;
    }
}
