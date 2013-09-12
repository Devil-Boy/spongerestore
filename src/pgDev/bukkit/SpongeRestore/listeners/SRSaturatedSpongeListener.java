package pgDev.bukkit.SpongeRestore.listeners;

import org.bukkit.event.*;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import pgDev.bukkit.SpongeRestore.SpongeRestore;

public class SRSaturatedSpongeListener implements Listener {
	final SpongeRestore plugin;
	
	public SRSaturatedSpongeListener(final SpongeRestore pluginI) {
		plugin = pluginI;
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (!SpongeRestore.pluginSettings.pistonMove && plugin.hasSponges(event.getBlocks())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (!SpongeRestore.pluginSettings.pistonMove && event.isSticky() && plugin.isSponge(event.getRetractLocation().getBlock())) {
			event.setCancelled(true);
		}
	}
}
