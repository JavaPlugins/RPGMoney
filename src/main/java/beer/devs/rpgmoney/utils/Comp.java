package beer.devs.rpgmoney.utils;

import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility wrapper to call useful Adventure API methods.
 */
public class Comp
{
    private static final MiniMessage MINIMESSAGE = MiniMessage.miniMessage();

    /**
     * Equivalent of {@link Component#text(String)}} but supports also Spigot hex color notation.
     * It surely is slower than {@link Component#text(String)}}.
     *
     * @param text a legacy text
     * @return text component
     */
    public static Component text(String text)
    {
        TextComponent component = BukkitComponentSerializer.legacy().deserialize(text);
        if (component.style().hasDecoration(TextDecoration.ITALIC))
            return component;
        return component.style(builder -> {
            builder.decoration(TextDecoration.ITALIC, false);
        });
    }

    public static String legacy(Component component)
    {
        return LegacyComponentSerializer.legacy('\u00A7').serialize(component);
    }

    public static Component minimessage(String str)
    {
        // Has legacy HEX notation
        // https://github.com/PluginBugs/Issues-ItemsAdder/issues/3057
        if(str.contains("&#") || str.contains("\u00A7#"))
        {
            str = Utils.convertColor(str);
            return text(str);
        }

        try
        {
            str = Utils.convertColor(str);
            return MINIMESSAGE.deserialize(str);
        }
        catch (ParsingException ex)
        {
//            if(ex.getMessage().contains("Legacy formatting codes"))
//                Msg.error(ChatColor.RED + "Do not use both Minimessage and legacy formatting code in the same text. Text: " + str);
            return text(str);
        }
    }
}
