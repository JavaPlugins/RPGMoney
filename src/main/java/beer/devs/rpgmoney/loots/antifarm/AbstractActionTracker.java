package beer.devs.rpgmoney.loots.antifarm;

import beer.devs.rpgmoney.utils.Utils;
import beer.devs.rpgmoney.loots.config.LootData;
import beer.devs.rpgmoney.loots.config.AbstractLootsRegistry;
import beer.devs.rpgmoney.loots.config.NerfData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractActionTracker
{
    HashMap<UUID, List<ActionRecord>> data = new HashMap<>();
    AbstractLootsRegistry loots;

    public AbstractActionTracker(AbstractLootsRegistry loots)
    {
        this.loots = loots;
    }

    public ActionRecord get(Entity killer, String prop)
    {
        if (!data.containsKey(killer.getUniqueId()))
            return initHashmap(killer, prop);

        for (ActionRecord entry : data.get(killer.getUniqueId()))
        {
            if (entry.action.equals(prop) && entry.worldName.equals(killer.getWorld().getName()))
                return entry;
        }
        return initKillsDataForEntity(killer, prop);
    }

    private ActionRecord initHashmap(Entity killer, String prop)
    {
        ActionRecord actionRecord = new ActionRecord(killer.getUniqueId(), 0, killer.getWorld().getName(), prop);
        data.put(killer.getUniqueId(), new ArrayList<>(Collections.singletonList(actionRecord)));
        return actionRecord;
    }

    private ActionRecord initKillsDataForEntity(Entity killer, String prop)
    {
        ActionRecord actionRecord = new ActionRecord(killer.getUniqueId(), 0, killer.getWorld().getName(), prop);
        data.get(killer.getUniqueId()).add(actionRecord);
        return actionRecord;
    }

    public void increase(Entity killer, String prop, LootData lootData)
    {
        ActionRecord actionRecord = get(killer, prop);
        actionRecord.count++;

        if (System.currentTimeMillis() >= actionRecord.ms + (lootData.antifarm.resetAfterSeconds * 1000L))
            actionRecord.count = 0;
        actionRecord.updateMs();
    }

    public float getNerfedMoney(@Nullable Entity killer, String prop)
    {
        ActionRecord actionRecord = get(killer, prop);
        LootData lootData = loots.get(killer.getWorld(), prop);
        NerfData nerfData = lootData.antifarm.getNerfData(actionRecord.count);
        return Utils.round(Utils.getRandom(lootData.money) * nerfData.getMoneyPercent() / 100, 2);
    }

    public float getNerfedMoney(@Nullable Entity killer, LootData data, String prop)
    {
        if (killer == null)
            return Utils.getRandom(data.money);
        ActionRecord actionRecord = get(killer, prop);
        NerfData nerfData = data.antifarm.getNerfData(actionRecord.count);
        return Utils.round(Utils.getRandom(data.money) * nerfData.getMoneyPercent() / 100, 2);
    }

    public float nerfMoney(Player killer, float money, LootData data, String prop)
    {
        if (killer == null)
            return money;
        ActionRecord actionRecord = get(killer, prop);
        NerfData nerfData = data.antifarm.getNerfData(actionRecord.count);
        return Utils.round(money * nerfData.getMoneyPercent() / 100, 2);
    }
}
