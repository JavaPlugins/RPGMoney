package beer.devs.rpgmoney.loots.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

public class PaperListener implements Listener
{
    @EventHandler
    private void onPlayerAttemptPickupItemEvent(PlayerAttemptPickupItemEvent e)
    {
       LootsEventsListener.handlePickup(e.getItem(), e.getPlayer(), e);
    }
}
