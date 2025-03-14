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
    private final Entity player;
    private final AbstractActionTracker tracker;
    public LootData loot;
    public String matchedId;

    public LootInstance(AbstractActionTracker tracker, @Nullable Entity player)
    {
        this.tracker = tracker;
        this.player = player;
    }

    public float calculateMoney()
    {
        return tracker.getNerfedMoney(player, loot, matchedId);
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
        tracker.increase(player, matchedId, loot);
    }
}
