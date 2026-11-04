package beer.devs.rpgmoney.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompTest
{
    @Test
    void legacyColorAndExplicitItalicArePreserved()
    {
        assertEquals(NamedTextColor.GREEN, Comp.text("\u00a7aCoins").color());
        assertEquals(TextDecoration.State.TRUE, Comp.text("\u00a7oCoins").decoration(TextDecoration.ITALIC));
        assertEquals(TextDecoration.State.FALSE, Comp.text("Coins").decoration(TextDecoration.ITALIC));
    }

    @Test
    void bothLegacyHexFormatsRemainSupported()
    {
        TextColor color = TextColor.color(0x12ab34);
        assertEquals(color, Comp.text("\u00a7#12ab34Coins").color());
        assertEquals(color, Comp.text("\u00a7x\u00a71\u00a72\u00a7a\u00a7b\u00a73\u00a74Coins").color());
        assertEquals(color, Comp.minimessage("&#12ab34Coins").color());
    }

    @Test
    void miniMessageAndLegacyFallbackRemainSupported()
    {
        assertEquals(Component.text("Coins", NamedTextColor.GREEN), Comp.minimessage("<green>Coins</green>"));
        assertEquals(NamedTextColor.GREEN, Comp.minimessage("&aCoins").color());
    }
}
