package beer.devs.rpgmoney.loots.config;

import java.util.Objects;

public class NerfData
{
    String key;
    int counter;
    int moneyPercent;

    public NerfData(String key, int kills, int moneyPercent)
    {
        this.key = key;
        this.counter = kills;
        this.moneyPercent = moneyPercent;
    }

    public int getCounter()
    {
        return counter;
    }

    public int getMoneyPercent()
    {
        return moneyPercent;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NerfData data = (NerfData) o;
        return counter == data.counter && moneyPercent == data.moneyPercent && Objects.equals(key, data.key);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(key, counter, moneyPercent);
    }
}
