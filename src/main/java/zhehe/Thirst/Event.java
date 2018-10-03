package zhehe.Thirst;

import java.util.HashMap;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;

public class Event {
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		Player p = event.getTargetEntity();
		Waterbar.getWaterbar().addPlayer(p);
	}
	
	@Listener
	public void onLogout(ClientConnectionEvent.Disconnect event) {
		Player p = event.getTargetEntity();
		Waterbar.getWaterbar().removePlayer(p);
		Waterbar.getWaterbar().writeDBPlayer(p);
	}
	
	@Listener
	public void onItemUse(UseItemStackEvent.Finish e, @First Player p) {
		ItemType stack = e.getItemStackInUse().createStack().getType();
		String item = Config.ItemTypeToString(stack);
		HashMap<String, Float> items = Config.getConfig().item;
		//p.sendMessage(Text.of("Use " + item));
		if(items.containsKey(item)) {
			//p.sendMessage(Text.of("Contains item"));
			int thirst = items.get(item).intValue();
			if(Waterbar.getWaterbar().addThirst(p, thirst)) {
				Waterbar.getWaterbar().addEffect(p);
			}
		}
	}
}
