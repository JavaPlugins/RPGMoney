package beer.devs.rpgmoney.loots.listener;

import beer.devs.rpgmoney.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupItemEventListener implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent e)
    {
        float money = Main.inst.getMoneyFromPickup(e.getItem().getItemStack());
        if (money == -1)
            return;

        e.setCancelled(true);
    }
}
