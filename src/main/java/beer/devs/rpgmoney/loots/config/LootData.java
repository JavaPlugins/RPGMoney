package beer.devs.rpgmoney.loots.config;

import beer.devs.rpgmoney.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LootData
{
    List<String> ids;
    protected List<String> worlds;
    public int chance;
    public String money;
    public String dropsCount;
    public boolean onlyIfKillerIsAPlayer;
    public boolean naturalDeath;
    public float naturalDeathPercentage;
    public AntiFarmData antifarm;

    public LootData()
    {
        this.antifarm = new AntiFarmData();
        ids = new ArrayList<>();
    }

    public List<String> getIds()
    {
        return ids;
    }

    public void setIds(List<String> ids)
    {
        for (String id : ids)
        {
            this.ids.add(id.toLowerCase());
        }
    }

    public List<String> getWorlds()
    {
        return worlds;
    }

    public boolean randomSuccess()
    {
        return Utils.getSuccess(chance);
    }

    public int randomAmount()
    {
        return Utils.getRandomInt(dropsCount);
    }

    public float randomMoney()
    {
        return Utils.round(Utils.getRandom(money), 2);
    }

    public static class AntiFarmData
    {
        static final NerfData UNKNOWN = new NerfData("unk", 0, 100);

        public int resetAfterSeconds;
        public List<NerfData> data = new ArrayList<>();

        @NotNull
        public NerfData getNerfData(int counter)
        {
            NerfData found = null;
            for (NerfData nerfData : data)
            {
                if (counter >= nerfData.counter)
                    found = nerfData;
            }
            if (found == null)
                return UNKNOWN;
            return found;
        }
    }
}
