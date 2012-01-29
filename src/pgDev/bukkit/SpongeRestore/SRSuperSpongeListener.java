package pgDev.bukkit.SpongeRestore;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.*;

public class SRSuperSpongeListener implements Listener {
	private final SpongeRestore plugin;
    
	public SRSuperSpongeListener(final SpongeRestore pluginI) {
		plugin = pluginI;
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
    	if (plugin.debug) {
    		System.out.println("Liquid incoming at: " + event.getToBlock().getX() + ", " + event.getToBlock().getY() + ", " + event.getToBlock().getZ());
    	}
    	if (plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getToBlock()))) {
    		if(plugin.debug) {
    			System.out.println("Recede from sponge!");
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
	public void onBlockBreak(BlockBreakEvent event) {
    	if (plugin.isSponge(event.getBlock())) {
    		Block wasBlock = event.getBlock();
    		if (plugin.debug) {
    			System.out.println("Sponge destroyed!");
    		}
    		
    		if (plugin.pluginSettings.restoreWater) {
    			SRFlowTimer flowTimer = new SRFlowTimer(plugin, plugin.disableSponge(wasBlock));
    			Thread timerThread = new Thread(flowTimer);
    			timerThread.start();
    			plugin.flowTimers.add(flowTimer);
    		} else {
    			plugin.disableSponge(wasBlock);
    		}
    	} else if (plugin.isIce(event.getBlock())) {
    		Block wasBlock = event.getBlock();
    		if(plugin.debug) {
    			System.out.println("Ice destroyed!");
    		}
    		// Check if the ice was within a sponge's area.
        	if (!plugin.pluginSettings.canPlaceWater && plugin.spongeAreas.containsKey(plugin.getBlockCoords(wasBlock))) {
            	wasBlock.setType(Material.AIR);
            	if (plugin.debug) {
                	System.out.println("Melted ice gone now :D");
            	}
        	}
    	}
    }
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
    	if (plugin.debug) {
    		System.out.println("Fire incoming at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
    	}
    	if (plugin.pluginSettings.absorbFire) {
    		if (plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getBlock()))) {
        		if(plugin.debug) {
        			System.out.println("Fire shall not pass!");
        		}
        		event.setCancelled(true);
    		} else if ((plugin.isNextToSpongeArea(event.getBlock()) && plugin.pluginSettings.attackFire) && 
    				!plugin.pluginSettings.excludedWorlds.contains(event.getBlock().getWorld().getName())) {
    			if (plugin.debug) {
        			System.out.println("Extinguish fire with sponge!");
        		}
    			event.getBlock().setTypeId(0, true);
    			event.setCancelled(true);
    		}
    	}
    }
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
    	if (plugin.debug) {
    		System.out.println("Block Burning at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
    	}
    	if (plugin.pluginSettings.absorbFire) {
    		if (plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getBlock())) && !plugin.pluginSettings.excludedWorlds.contains(event.getBlock().getWorld().getName())) {
        		if (plugin.debug) {
        			System.out.println("Sponge never lets a block burn!");
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
        		System.out.println("Ice melting at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
        	}
    		if (!plugin.pluginSettings.canPlaceWater && plugin.spongeAreas.containsKey(plugin.getBlockCoords(event.getBlock()))) {
        		if (plugin.debug) {
        			System.out.println("Sneaky ice, you thought you could let water in!");
        		}
        		event.setCancelled(true);
        		event.getBlock().setType(Material.AIR);
    		}
    	}
    }
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (plugin.hasSponges(event.getBlocks())) {
			if (plugin.pluginSettings.pistonMove) {
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
				(new Thread(new SRMultiSpongeThread(toEnable, toDisable, plugin))).start();
			} else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (event.isSticky() && plugin.isSponge(event.getRetractLocation().getBlock())) {
			if (plugin.pluginSettings.pistonMove) {
				LinkedList<Block> toEnable = new LinkedList<Block>();
				LinkedList<Block> toDisable = new LinkedList<Block>();
				//plugin.disableSponge(event.getRetractLocation().getBlock());
				//plugin.enableSponge(event.getBlock().getRelative(event.getDirection()));
				toDisable.add(event.getRetractLocation().getBlock());
				toEnable.add(event.getBlock().getRelative(event.getDirection()));
				(new Thread(new SRMultiSpongeThread(toEnable, toDisable, plugin))).start();
			} else {
				event.setCancelled(true);
			}
		}
	}
}
