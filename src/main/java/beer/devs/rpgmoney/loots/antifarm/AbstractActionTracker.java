package beer.devs.rpgmoney.loots.antifarm;

import beer.devs.rpgmoney.utils.Utils;
import beer.devs.rpgmoney.loots.config.LootData;
import beer.devs.rpgmoney.loots.config.AbstractLootsRegistry;
import beer.devs.rpgmoney.loots.config.NerfData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class AbstractActionTracker
{
    HashMap<UUID, List<ActionRecord>> data = new HashMap<>();
    AbstractLootsRegistry loots;

    public AbstractActionTracker(AbstractLootsRegistry loots)
    {
        this.loots = loots;
    }

    public ActionRecord get(Entity player, String prop)
    {
        if (!data.containsKey(player.getUniqueId()))
            return initHashmap(player, prop);

        for (ActionRecord entry : data.get(player.getUniqueId()))
        {
            if (entry.action.equals(prop) && entry.worldName.equals(player.getWorld().getName()))
                return entry;
        }
        return initKillsDataForEntity(player, prop);
    }

    private ActionRecord initHashmap(Entity player, String prop)
    {
        ActionRecord actionRecord = new ActionRecord(player.getUniqueId(), 0, player.getWorld().getName(), prop);
        data.put(player.getUniqueId(), new ArrayList<>(Collections.singletonList(actionRecord)));
        return actionRecord;
    }

    private ActionRecord initKillsDataForEntity(Entity player, String prop)
    {
        ActionRecord actionRecord = new ActionRecord(player.getUniqueId(), 0, player.getWorld().getName(), prop);
        data.get(player.getUniqueId()).add(actionRecord);
        return actionRecord;
    }

    public void increase(Entity player, String prop, LootData lootData)
    {
        ActionRecord actionRecord = get(player, prop);
        actionRecord.count++;

        if (System.currentTimeMillis() >= actionRecord.ms + (lootData.antifarm.resetAfterSeconds * 1000L))
            actionRecord.count = 0;
        actionRecord.updateMs();
    }

    public float getNerfedMoney(Entity player, String prop)
    {
        ActionRecord actionRecord = get(player, prop);
        LootData lootData = loots.get(player.getWorld(), prop);
        NerfData nerfData = lootData.antifarm.getNerfData(actionRecord.count);
        return Utils.round(Utils.getRandom(lootData.money) * nerfData.getMoneyPercent() / 100, 2);
    }

    public float getNerfedMoney(Entity player, LootData data, String prop)
    {
        if(player == null)
            return Utils.getRandom(data.money);
        ActionRecord actionRecord = get(player, prop);
        NerfData nerfData = data.antifarm.getNerfData(actionRecord.count);
        return Utils.round(Utils.getRandom(data.money) * nerfData.getMoneyPercent() / 100, 2);
    }

    public float nerfMoney(Player player, float money, LootData data, String prop)
    {
        if(player == null)
            return money;
        ActionRecord actionRecord = get(player, prop);
        NerfData nerfData = data.antifarm.getNerfData(actionRecord.count);
        return Utils.round(money * nerfData.getMoneyPercent() / 100, 2);
    }
}
