package beer.devs.rpgmoney;

import beer.devs.rpgmoney.utils.CustomConfigFile;

public class Settings
{
    public static boolean ALLOW_ENTITIES_PICKUP_MONEY;
    public static boolean PICKUPS_MERGE;
    public static double PICKUPS_MERGE_RADIUS;
    public static int PICKUPS_MERGE_MIN_PICKUPS;
    public static boolean SHOW_ACTIONBAR_MESSAGES;
    public static boolean LOOTS_FROM_BLOCKS;
    public static boolean LOOTS_FROM_ENTITIES;
    public static boolean LOOTS_FROM_FISHES;

    public static void load(CustomConfigFile config)
    {
        SHOW_ACTIONBAR_MESSAGES = config.getBoolean("show_actionbar_messages", true);
        PICKUPS_MERGE = config.getBoolean("pickups_merge.enabled", true);
        PICKUPS_MERGE_RADIUS = config.getDouble("pickups_merge.radius", 4);
        PICKUPS_MERGE_MIN_PICKUPS = config.getInt("pickups_merge.min_pickups", 4);
        ALLOW_ENTITIES_PICKUP_MONEY = config.getBoolean("allow_entities_pickup_money");
        LOOTS_FROM_ENTITIES = config.getBoolean("loots.from_entities");
        LOOTS_FROM_BLOCKS = config.getBoolean("loots.from_blocks");
        LOOTS_FROM_FISHES = config.getBoolean("loots.from_fishes");
    }
}