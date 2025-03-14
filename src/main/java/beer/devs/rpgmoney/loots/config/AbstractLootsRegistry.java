package beer.devs.rpgmoney.loots.config;

import beer.devs.rpgmoney.Main;
import beer.devs.rpgmoney.utils.SS;
import org.apache.commons.io.FileUtils;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractLootsRegistry
{
    Main plugin;
    protected File configFile;
    protected FileConfiguration config;
    protected Map<String, LootData> data = new HashMap<>();
    private final String stringsPropertyName;

    public AbstractLootsRegistry(Main plugin, String stringsPropertyName)
    {
        this.plugin = plugin;
        this.stringsPropertyName = stringsPropertyName;
    }

    public void reloadConfig()
    {
        try
        {
            handleIfExists();

            Map<String, LootData> tmpData = new HashMap<>();
            Set<String> list = config.getConfigurationSection("").getKeys(false);
            for (String key : list)
            {
                try
                {
                    if(!config.getBoolean(key + ".enabled"))
                        continue;

                    LootData lootData = new LootData();
                    loadCommonData(lootData, key);
                    loadAntiFarm(config, key, lootData);

                    lootData.setIds(config.getStringList(key + "." + stringsPropertyName));

                    if (config.contains(key + ".anti_farm.reset_after_seconds"))
                        lootData.antifarm.resetAfterSeconds = config.getInt(key + ".anti_farm.reset_after_seconds");

                    tmpData.put(key, lootData);
                }
                catch (IllegalArgumentException ignored) {}
            }

            createLookup(tmpData);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    protected void createLookup(Map<String, LootData> tmpData)
    {
        // Create a fast lookup table to search by world+entity instead of iterating
        tmpData.forEach((s, lootData) -> {
            for (String world : lootData.worlds)
            {
                for (String entity : lootData.getIds())
                {
                    data.put(world + "_" + entity, lootData);
                }
            }
        });
    }

    public LootData get(World world, String id)
    {
        LootData a = data.get(world.getName() + "_" + id);
        if (a == null)
            return data.get("ALL_" + id);
        return a;
    }

    public void handleIfExists() throws IOException, InvalidConfigurationException
    {
        if (!configFile.exists())
        {
            FileUtils.copyInputStreamToFile(plugin.getResource(configFile.getName()), configFile);
            config.load(configFile);
        }
    }

    protected void loadCommonData(LootData lootData, String key)
    {
        lootData.chance = config.getInt(key + ".chance", 100);
        lootData.money = config.getString(key + ".money", "1");
        lootData.dropsCount = config.getString(key + ".drops_count", "1");
        lootData.onlyIfKillerIsAPlayer = config.getBoolean(key + ".only_if_killer_is_a_player", true);
        lootData.naturalDeath = config.getBoolean(key + ".natural_death.enable", true);
        lootData.naturalDeathPercentage = (float) config.getDouble(key + ".natural_death.percentage_of_money", 100d);
        //noinspection unchecked
        lootData.worlds = (List<String>) config.getList(key + ".worlds", SS.list("ALL"));
    }

    protected void loadAntiFarm(FileConfiguration config, String key, LootData lootData)
    {
        if (config.contains(key + ".anti_farm"))
        {
            Set<String> nerfList = config.getConfigurationSection(key + ".anti_farm").getKeys(false);
            for (String nerfKey : nerfList)
            {
                // It's a single setting, for example reset_after_seconds
                if(!(config.get(key + ".anti_farm." + nerfKey) instanceof ConfigurationSection))
                    continue;
                lootData.antifarm.data.add(new NerfData(
                        nerfKey,
                        config.getInt(key + ".anti_farm." + nerfKey + ".counter"),
                        Integer.parseInt(config.getString(key + ".anti_farm." + nerfKey + ".money_percent").replace("%", ""))
                ));
            }
        }
    }
}
