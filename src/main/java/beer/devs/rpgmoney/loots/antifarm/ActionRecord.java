package beer.devs.rpgmoney.loots.antifarm;

import java.util.UUID;

public class ActionRecord
{
    public UUID uuid;
    public int count;
    public String worldName;
    public String action;
    public Long ms;

    public ActionRecord(UUID uuid, int count, String worldName, String action)
    {
        this.uuid = uuid;
        this.count = count;
        this.worldName = worldName;
        this.action = action;
        updateMs();
    }

    public void updateMs()
    {
        ms = System.currentTimeMillis();
    }
}
