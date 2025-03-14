package beer.devs.rpgmoney.loots.antifarm;

import beer.devs.rpgmoney.loots.config.LootData;
import beer.devs.rpgmoney.loots.config.AbstractLootsRegistry;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlocksTracker extends AbstractActionTracker
{
    public BlocksTracker(AbstractLootsRegistry lootsRegistry)
    {
        super(lootsRegistry);
    }

    public float nerfMoney(Player player, LootData data, Block block)
    {
        return getNerfedMoney(player, data, block.getType().toString().toLowerCase());
    }
}
