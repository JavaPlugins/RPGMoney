package beer.devs.rpgmoney.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.math.BigDecimal;
import java.util.Random;

public class Utils
{
    public static String blockId(Block block)
    {
        return block.getType().toString().toLowerCase();
    }

    public static String entityId(Entity entity)
    {
        return entity.getType().toString().toLowerCase();
    }

    public static String itemId(ItemStack itemStack)
    {
        return itemStack.getType().toString().toLowerCase();
    }

    public static boolean isNumeric(String inputData)
    {
        return inputData.matches("[-+]?\\d+(\\.\\d+)?");
    }

    public static float takePercentage(float v, float perc)
    {
        return round(v * perc / 100, 2);
    }

    public static float getRandom(String level)
    {
        if (level.contains("-"))
        {
            String[] spl = level.split("-");
            return round(randomNumber(Float.parseFloat(spl[0]), Float.parseFloat(spl[1])), 2);
        }
        else return Integer.parseInt(level);
    }

    public static int getRandomInt(String level)
    {
        if (level.contains("-"))
        {
            String[] spl = level.split("-");
            return getRandomInt(Integer.parseInt(spl[0]), Integer.parseInt(spl[1]));
        }
        else return Integer.parseInt(level);
    }

    public static float round(float d, int decimalPlace)
    {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    @SuppressWarnings("unused")
    public static float randomNumber(float f, float g)
    {
        Random random = new Random();
        float number = random.nextFloat() * (g - f) + f;
        return random.nextFloat() * (g - f) + f;
    }

    public static int getRandomInt(int min, int max)
    {
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }

    public static boolean getSuccess(int percent)
    {
        int i = getRandomInt(1, 100);
        if (i <= percent) return true;
        return false;
    }

    public static String convertColor(String name)
    {
        return name.replace("&", "\u00a7");
    }

    @SuppressWarnings("removal")
    public static boolean isFullyGrown(Block block)
    {
        org.bukkit.material.MaterialData md = block.getState().getData();
        if (md instanceof org.bukkit.material.Crops)
            return (((org.bukkit.material.Crops) md).getState() == org.bukkit.CropState.RIPE);
        return true;
    }

    @SuppressWarnings("removal")
    public static boolean isGrowable(Block block)
    {
        return (block.getState().getData() instanceof org.bukkit.material.Crops);
    }

    public static boolean isVanished(Player player)
    {
        for (MetadataValue meta : player.getMetadata("vanished"))
        {
            if (meta.asBoolean()) return true;
        }
        return false;
    }

    public static boolean hasEnchant(ItemStack item, Enchantment enchant)
    {
        return item.hasItemMeta() && item.getItemMeta().hasEnchant(enchant);
    }

    public static boolean isInventoryFull(Player player)
    {
        return player.getInventory().firstEmpty() == -1;
    }

    public static void decrementAmountMainHand(Player player)
    {
        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() == Material.AIR || player.getInventory().getItemInMainHand().getAmount() <= 0 || player.getInventory().getItemInMainHand().getAmount() - 1 < 0)
            return;
        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
    }
}
