package beer.devs.rpgmoney;

import beer.devs.rpgmoney.utils.CustomConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.Plugin;

public class ResPackManager implements Listener
{
    Plugin plugin;
    CustomConfigFile config;
    CustomConfigFile lang;

    public ResPackManager(Plugin plugin, CustomConfigFile config, CustomConfigFile lang)
    {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        if (Main.HAS_ITEMSADDER)
            return;

        if (config.getBoolean("resource_pack.show_loading_message"))
            e.getPlayer().sendMessage(Main.PREFIX + lang.getColored("resourcepack-loading"));

        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (config.getBoolean("resource_pack.apply_on_join"))
                e.getPlayer().setResourcePack(config.getString("resource_pack.link"));
            if (config.getBoolean("resource_pack.show_success_message"))
                e.getPlayer().sendMessage(Main.PREFIX + lang.getColored("resourcepack-success"));
        }, config.getInt("resource_pack.delay_milliseconds"));
    }

    @EventHandler
    public void onResourcepackStatusEvent(PlayerResourcePackStatusEvent event)
    {
        if (Main.HAS_ITEMSADDER)
            return;

        if (!config.getBoolean("resource_pack.apply_on_join"))
            return;

        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED)
        {
            if (config.getBoolean("resource_pack.kick_player_on_decline"))
                event.getPlayer().kickPlayer(lang.getColored("resourcepack-kick-declined"));
        }

        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
        {
            event.getPlayer().sendMessage(Main.PREFIX + lang.getColored("resourcepack-error"));
        }
    }
}
