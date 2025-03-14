package beer.devs.rpgmoney.loots.config;

import beer.devs.rpgmoney.Main;
import beer.devs.rpgmoney.loots.LootInstance;
import beer.devs.rpgmoney.loots.antifarm.KillsTracker;
import beer.devs.rpgmoney.utils.Utils;
import dev.lone.itemsadder.api.CustomEntity;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class EntitiesLootsRegistry extends AbstractLootsRegistry
{
    public KillsTracker tracker = new KillsTracker(this);

    public EntitiesLootsRegistry(Main plugin)
    {
        super(plugin, "entities");
        configFile = new File("plugins/RPGMoney/loots_entities.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        reloadConfig();
    }

    @Nullable
    public LootInstance get(Entity player, Entity entity)
    {
        World world = entity.getWorld();
        LootInstance lootInstance = new LootInstance(tracker, player);
        if(!Main.HAS_ITEMSADDER)
        {
            lootInstance.loot = get(world, Utils.entityId(entity));
            lootInstance.matchedId = Utils.entityId(entity);
            if(lootInstance.loot == null)
                return null;
            return lootInstance;
        }

        CustomEntity customEntity = CustomEntity.byAlreadySpawned(entity);
        if(customEntity == null)
        {
            lootInstance.loot = get(world, Utils.entityId(entity));
            lootInstance.matchedId = Utils.entityId(entity);
            if(lootInstance.loot == null)
                return null;
            return lootInstance;
        }

        LootData a = data.get(world.getName() + "_" + customEntity.getNamespacedID());
        if (a == null)
        {
            lootInstance.loot = get(world, Utils.entityId(entity));
            lootInstance.matchedId = Utils.entityId(entity);
        }
        else
        {
            lootInstance.loot = get(world, customEntity.getNamespacedID());
            lootInstance.matchedId = customEntity.getNamespacedID();
        }

        if(lootInstance.loot == null)
            return null;
        return lootInstance;
    }
}
