package pgDev.bukkit.SpongeRestore.listeners;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;

import pgDev.bukkit.SpongeRestore.SRFlowTimer;
import pgDev.bukkit.SpongeRestore.SpongeRestore;

public class SRBaseListener implements Listener {
	private final SpongeRestore plugin;
	
	public SRBaseListener(final SpongeRestore pluginI) {
		plugin = pluginI;
	}
	
	// Deal with the birth of a sponge
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
    	if (!event.isCancelled()) {
	    	if (plugin.debug) {
	    		SpongeRestore.logger.log(Level.INFO, event.getPlayer().getName() + " placed a block...");
	    	}
	    	// Check if the block is a Sponge
	    	if (plugin.isSponge(event.getBlock()) && !SpongeRestore.pluginSettings.excludedWorlds.contains(event.getBlock().getWorld().getName())) {
	    		if (plugin.debug) {
	    			SpongeRestore.logger.log(Level.INFO, "and it's a sponge!!!!!");
	    		}
	    		plugin.enableSponge(event.getBlock());
	    	}
	    	
	    	// Check if a water block is being placed within sponge's area
	    	if (!SpongeRestore.pluginSettings.canPlaceWater && ((plugin.blockIsAffected(event.getBlock())) && plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getBlock())))) {
	        	event.setCancelled(true);
	        	if (plugin.debug) {
	            	SpongeRestore.logger.log(Level.INFO, "You canot put liquid there!! :O");
	        	}
	    	}
		}
    }
	
	// Check if water is being dumped in the sponge's area
	@EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    	Block involvedBlock = event.getBlockClicked().getRelative(event.getBlockFace()) ;
    	String dumpLocation = plugin.getBlockCoords(involvedBlock);
    	Material bucketType = event.getBucket();
    	if (plugin.debug) {
    		SpongeRestore.logger.log(Level.INFO, bucketType + " emptied!");
    	}
    	if (plugin.debug) {
    		SpongeRestore.logger.log(Level.INFO, involvedBlock.getType() + " dumped out!");
    	}
    	if (!SpongeRestore.pluginSettings.canPlaceWater && ((bucketType == Material.WATER_BUCKET || (SpongeRestore.pluginSettings.absorbLava && bucketType == Material.LAVA_BUCKET)) && plugin.spongeAreas.containsKey(dumpLocation))) {
        	event.setCancelled(true);
        	if(plugin.debug) {
        		SpongeRestore.logger.log(Level.INFO, "You can't dump liquid there!! :O (" + dumpLocation + ")");
        	}
        	event.setItemStack(new ItemStack(325));
    	}
    }
	
	// Remove broken sponges from database
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
    	if (plugin.isSponge(event.getBlock())) {
    		Block wasBlock = event.getBlock();
    		if (plugin.debug) {
    			SpongeRestore.logger.log(Level.INFO, "Sponge destroyed!");
    		}
    		
    		if (SpongeRestore.pluginSettings.restoreWater) {
    			SRFlowTimer flowTimer = new SRFlowTimer(plugin, plugin.disableSponge(wasBlock));
    			plugin.workerThreads.execute(flowTimer);
    			plugin.flowTimers.add(flowTimer);
    		} else {
    			plugin.disableSponge(wasBlock);
    		}
    	} else if (plugin.isIce(event.getBlock())) {
    		Block wasBlock = event.getBlock();
    		if(plugin.debug) {
    			SpongeRestore.logger.log(Level.INFO, "Ice destroyed!");
    		}
    		// Check if the ice was within a sponge's area.
        	if (!SpongeRestore.pluginSettings.canPlaceWater && plugin.spongeAreas.containsKey(plugin.getBlockCoords(wasBlock))) {
            	wasBlock.setType(Material.AIR);
            	if (plugin.debug) {
                	SpongeRestore.logger.log(Level.INFO, "Melted ice gone now :D");
            	}
        	}
    	}
    }
	
	// Save the sponge database
	@EventHandler
	public void onWorldSave(WorldSaveEvent event) {
		if (plugin.debug) {
			SpongeRestore.logger.log(Level.INFO, "World saved, along with sponges!");
		}
		plugin.saveSpongeData();
	}
}
