package pgDev.bukkit.SpongeRestore.listeners;

import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.*;

import pgDev.bukkit.SpongeRestore.SRMultiSpongeThread;
import pgDev.bukkit.SpongeRestore.SpongeRestore;

public class SRSuperSpongeListener implements Listener {
	private final SpongeRestore plugin;
    
	public SRSuperSpongeListener(final SpongeRestore pluginI) {
		plugin = pluginI;
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
    	if (plugin.debug) {
    		SpongeRestore.logger.log(Level.INFO, "Liquid incoming at: " + event.getToBlock().getX() + ", " + event.getToBlock().getY() + ", " + event.getToBlock().getZ());
    	}
    	if (plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getToBlock()))) {
    		if(plugin.debug) {
    			SpongeRestore.logger.log(Level.INFO, "Recede from sponge!");
    		}
    		if (plugin.blockIsAffected(event.getBlock())) {
    			event.setCancelled(true);
    		}
    	} else if (plugin.spongeAreas.containsKey(plugin.getDeletedBlockCoords(event.getToBlock())) && plugin.blockIsAffected(event.getBlock())) {
    		Block receivingBlock = event.getToBlock();
    		if (plugin.isAir(receivingBlock)) {
    			receivingBlock.setTypeId(event.getBlock().getTypeId(), true);
    		}
    	}
    }
	
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
    	if (plugin.debug) {
    		SpongeRestore.logger.log(Level.INFO, "Fire incoming at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
    	}
    	if (SpongeRestore.pluginSettings.absorbFire) {
    		if (plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getBlock()))) {
        		if(plugin.debug) {
        			SpongeRestore.logger.log(Level.INFO, "Fire shall not pass!");
        		}
        		event.setCancelled(true);
    		} else if ((plugin.isNextToSpongeArea(event.getBlock()) && SpongeRestore.pluginSettings.attackFire) && 
    				!SpongeRestore.pluginSettings.excludedWorlds.contains(event.getBlock().getWorld().getName())) {
    			if (plugin.debug) {
        			SpongeRestore.logger.log(Level.INFO, "Extinguish fire with sponge!");
        		}
    			event.getBlock().setTypeId(0, true);
    			event.setCancelled(true);
    		}
    	}
    }
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
    	if (plugin.debug) {
    		SpongeRestore.logger.log(Level.INFO, "Block Burning at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
    	}
    	if (SpongeRestore.pluginSettings.absorbFire) {
    		if (plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getBlock())) && !SpongeRestore.pluginSettings.excludedWorlds.contains(event.getBlock().getWorld().getName())) {
        		if (plugin.debug) {
        			SpongeRestore.logger.log(Level.INFO, "Sponge never lets a block burn!");
        		}
        		event.setCancelled(true);
        		plugin.killSurroundingFire(event.getBlock());
    		}
    	}
    }
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockFade(BlockFadeEvent event) {
    	if (event.getBlock().getType() == Material.ICE) {
    		if (plugin.debug) {
        		SpongeRestore.logger.log(Level.INFO, "Ice melting at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
        	}
    		if (!SpongeRestore.pluginSettings.canPlaceWater && plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getBlock()))) {
        		if (plugin.debug) {
        			SpongeRestore.logger.log(Level.INFO, "Sneaky ice, you thought you could let water in!");
        		}
        		event.setCancelled(true);
        		event.getBlock().setType(Material.AIR);
    		}
    	}
    }
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (plugin.hasSponges(event.getBlocks())) {
			if (SpongeRestore.pluginSettings.pistonMove) {
				LinkedList<Block> movedSponges = plugin.getSponges(event.getBlocks());
				LinkedList<Block> toEnable = new LinkedList<Block>();
				LinkedList<Block> toDisable = new LinkedList<Block>();
				if (movedSponges.size() == 1) { // No need to check for sequencial sponges
					Block spawnge = movedSponges.getLast();
					//plugin.disableSponge((spawnge));
					//plugin.enableSponge(spawnge.getRelative(event.getDirection()));
					toDisable.add(spawnge);
					toEnable.add(spawnge.getRelative(event.getDirection()));
				} else {
					for (Block spawnge : movedSponges) {
						// Disable old spot?
						if (!plugin.isSponge(spawnge.getRelative(event.getDirection().getOppositeFace()))) {
							//plugin.disableSponge((spawnge));
							toDisable.add(spawnge);
						}
						// Enable new spot?
						if (!plugin.isSponge(spawnge.getRelative(event.getDirection()))) {
							//plugin.enableSponge(spawnge.getRelative(event.getDirection()));
							toEnable.add(spawnge.getRelative(event.getDirection()));
						}
					}
				}
				plugin.workerThreads.execute(new SRMultiSpongeThread(toEnable, toDisable, plugin));
			} else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (event.isSticky() && plugin.isSponge(event.getRetractLocation().getBlock())) {
			if (SpongeRestore.pluginSettings.pistonMove) {
				LinkedList<Block> toEnable = new LinkedList<Block>();
				LinkedList<Block> toDisable = new LinkedList<Block>();
				//plugin.disableSponge(event.getRetractLocation().getBlock());
				//plugin.enableSponge(event.getBlock().getRelative(event.getDirection()));
				toDisable.add(event.getRetractLocation().getBlock());
				toEnable.add(event.getBlock().getRelative(event.getDirection()));
				plugin.workerThreads.execute(new SRMultiSpongeThread(toEnable, toDisable, plugin));
			} else {
				event.setCancelled(true);
			}
		}
	}
}
