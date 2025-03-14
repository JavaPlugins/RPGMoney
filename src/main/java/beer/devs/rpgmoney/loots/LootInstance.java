package beer.devs.rpgmoney.loots;

import beer.devs.rpgmoney.Main;
import beer.devs.rpgmoney.loots.antifarm.AbstractActionTracker;
import beer.devs.rpgmoney.utils.Utils;
import beer.devs.rpgmoney.loots.config.LootData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class LootInstance
{
    @Nullable
    private final Entity killer;
    private final AbstractActionTracker tracker;
    public LootData loot;
    private String matchedId;

    public LootInstance(AbstractActionTracker tracker, @Nullable Entity killer)
    {
        this.tracker = tracker;
        this.killer = killer;
    }

    public float calculateMoney()
    {
        return tracker.getNerfedMoney(killer, loot, matchedId);
    }

    public float calculateMoneyFromKilledEntity(Player killedPlayer)
    {
        String moneyRule = loot.money;
        if (moneyRule.contains("%"))
        {
            String s = moneyRule.replace("%", "");
            int percent = Utils.getRandomInt(s);
            float money = Double.valueOf(Main.economy.getBalance(killedPlayer)).floatValue();
            money = Utils.takePercentage(money, percent);
            return tracker.nerfMoney(killedPlayer, money, loot, matchedId);
        }
        return loot.randomMoney();
    }

    public void increaseTracker()
    {
        if(killer == null)
            return;
        tracker.increase(killer, matchedId, loot);
    }

    public void setMatchedId(String matchedId)
    {
        this.matchedId = matchedId;
    }
}
