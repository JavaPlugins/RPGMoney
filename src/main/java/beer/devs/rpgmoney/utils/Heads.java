package beer.devs.rpgmoney.utils;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class Heads
{
    public static ItemStack get(Head head)
    {
        return get(head.textureValue);
    }

    public static ItemStack get(String texture)
    {
        ItemStack item = newPlayerHead(1);
        // Let Paper handle the profile component format for the running server.
        var profile = Bukkit.createProfile(UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8)));
        profile.setProperty(new ProfileProperty("textures", texture));
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack newPlayerHead(int amount)
    {
        return new ItemStack(Material.PLAYER_HEAD, amount);// 17
    }

    public enum Head
    {
//        WOODEN_PLUS("b4f61444-f5fa-4668-91f0-9e00408d6298", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0="),
//        WOODEN_MINUS("1cd93afd-bce6-4e46-b3c6-cb26544b5cef", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ4YTk5ZGIyYzM3ZWM3MWQ3MTk5Y2Q1MjYzOTk4MWE3NTEzY2U5Y2NhOTYyNmEzOTM2Zjk2NWIxMzExOTMifX19"),
//        ...
        // REMOVED, since I don't use them in this plugin.
        ;

        public final String uuid;
        public final String textureValue;

        Head(String uuid, String textureValue)
        {
            this.uuid = uuid;
            this.textureValue = textureValue;
        }

        public ItemStack toItemStack()
        {
            return Heads.get(this);
        }

        @SuppressWarnings("DataFlowIssue")
        public ItemStack toItemStack(String displayName, String... loreLines)
        {
            ItemStack itemStack = toItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(Arrays.asList(loreLines));
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }
    }
}
