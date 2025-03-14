package beer.devs.rpgmoney.utils;

import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CustomConfigFile
{
    private FileConfiguration config;
    private final Plugin plugin;
    private final String fileName;
    private final File configFile;
    private boolean needsToBeUpdatedIfDifferentFromResourceFile = false;

    public CustomConfigFile(Plugin plugin, String fileName, boolean needsToBeUpdatedIfDifferentFromResourceFile)
    {
        this.plugin = plugin;
        this.fileName = fileName;
        this.needsToBeUpdatedIfDifferentFromResourceFile = needsToBeUpdatedIfDifferentFromResourceFile;
        this.configFile = new File(plugin.getDataFolder(), this.fileName + ".yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        update();
    }

    public void realoadFromFile()
    {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void update()
    {
        try
        {
            if (!configFile.exists())
            {
                // carico il file di default contenuto nel jar
                FileUtils.copyInputStreamToFile(plugin.getResource(this.fileName + ".yml"), configFile);
            }
            else
            {
                if (needsToBeUpdatedIfDifferentFromResourceFile)
                {
                    FileConfiguration c = YamlConfiguration
                            .loadConfiguration((new InputStreamReader(plugin.getResource(this.fileName + ".yml"))));
                    for (String k : c.getKeys(true))
                    {
                        if (!config.contains(k))
                            config.set(k, c.get(k));
                    }
                    config.save(configFile);
                }
            }
            config.load(configFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    public String getColored(String name)
    {
        String str = config.getString(name);
        if(str == null)
            return ChatColor.RED +  "unknown_text!";
        return Comp.legacy(Comp.minimessage(str));
    }

    public FileConfiguration getConfig()
    {
        return config;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void set(String path, Object value)
    {
        this.config.set(path, value);
    }

    public void save()
    {
        try
        {
            this.config.save(configFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getString(String path)
    {
        return config.getString(path);
    }

    public int getInt(String path)
    {
        return config.getInt(path);
    }

    /**
     * Riturna interno se trovato, se no ritorna defaultValue
     *
     * @param path
     * @param defaultValue
     * @return
     */
    public int getInt(String path, int defaultValue)
    {
        if (hasKey(path))
            return config.getInt(path);
        return defaultValue;
    }

    /**
     * Riturna double se trovato, se no ritorna defaultValue
     *
     * @param path
     * @param defaultValue
     * @return
     */
    public double getDouble(String path, double defaultValue)
    {
        if (hasKey(path))
            return config.getDouble(path);
        return defaultValue;
    }

    public double getDouble(String path)
    {
        if (hasKey(path))
            return config.getDouble(path);
        return 0;
    }

    public boolean getBoolean(String path)
    {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean defaultValue)
    {
        if (hasKey(path))
            return config.getBoolean(path);
        return defaultValue;
    }

    public Material getMaterial(String path)
    {
        if (hasKey(path))
            return Material.valueOf(config.getString(path));
        else
            return null;
    }

    public PotionEffectType getPotionEffect(String path)
    {
        if (hasKey(path))
            return PotionEffectType.getByName(config.getString(path));
        else
            return null;
    }

    public boolean hasKey(String path)
    {
        return (config.get(path) != null);
    }

    public List<?> getList(String key)
    {
        return config.getList(key);
    }
}
