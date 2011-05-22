package DevilBoy.SpongeRestore;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * SpongeRestore block listener
 * @author DevilBoy
 */
public class SpongeRestoreBlockListener extends BlockListener {
    private final SpongeRestore plugin;

    public SpongeRestoreBlockListener(final SpongeRestore plugin) {
        this.plugin = plugin;
    }

    // Catch the events!
    public void onBlockPlace(BlockPlaceEvent event) {
    	Block involvedBlock = event.getBlock();
    	String theWorld = event.getBlock().getWorld().getName();
    	if(plugin.debug) {
    		System.out.println(event.getPlayer().getName() + " placed a block...");
    	}
    	// Check if the block is a Sponge
    	if (isSponge(involvedBlock) && !plugin.pluginSettings.excludedWorlds.contains(theWorld)) {
    		if(plugin.debug) {
    			System.out.println("and it's a sponge!!!!!");
    		}
    		
    		for (int x=-2; x<3; x++) {
    			for (int y=-2; y<3; y++) {
    				for (int z=-2; z<3; z++) {		
    					if(plugin.debug) {
    						System.out.println("Checking: " + x + ", " + y + ", " + z);
    					}
    					Block currentBlock = event.getBlock().getRelative(x, y, z);
    					addToSpongeAreas(getBlockCoords(currentBlock));
    					if (isWater(currentBlock)) {
    						currentBlock.setType(Material.AIR);
    						System.out.println("The sponge absorbed water.");
    					}
    	    		}
        		}
    		}
    		plugin.saveSpongeData();
    	}
    	
    	// Check if a water block is being placed within sponge's area
    	if (!plugin.pluginSettings.canPlaceWater && (isWater(involvedBlock) && plugin.spongeAreas.containsKey(getBlockCoords(involvedBlock)))) {
        	involvedBlock.setType(Material.AIR);
        	if(plugin.debug) {
            	System.out.println("You canot put water there!! :O");
        	}
    	}
    }
    
    public void onBlockFromTo(BlockFromToEvent event) {
    	System.out.println("Water incoming at: " + event.getToBlock().getX() + ", " + event.getToBlock().getY() + ", " + event.getToBlock().getZ());
    	if (plugin.spongeAreas.containsKey(getBlockCoords(event.getToBlock())) && !plugin.pluginSettings.excludedWorlds.contains(event.getToBlock().getWorld().getName())) {
    		if(plugin.debug) {
    			System.out.println("Recede from sponge!");
    		}
    		event.setCancelled(true);
    	}
    }
    
    public void onBlockBreak(BlockBreakEvent event) {
    	Block wasSponge = event.getBlock();
    	if (isSponge(wasSponge)) {
    		if(plugin.debug) {
    			System.out.println("Sponge destroyed!");
    		}
    		
    		// Check the airea
    		for (int x=-2; x<3; x++) {
    			for (int y=-2; y<3; y++) {
    				for (int z=-2; z<3; z++) {
    					Block currentBlock = wasSponge.getRelative(x, y, z);
    					removeFromSpongeAreas(getBlockCoords(currentBlock));
    					if(plugin.debug) {
    						System.out.println("AirSearching: " + x + ", " + y + ", " + z);
    					}
    					if (currentBlock.getType() == Material.AIR) {
    						currentBlock.setTypeId(0, true); // Turn air into air.
    					}
    	    		}
        		}
    		}
    		plugin.saveSpongeData();
    	}
    }
    
    // Old Function
    public boolean isNearSponge(Block originBlock) {
    	for (int x=-2; x<3; x++) {
			for (int y=-2; y<3; y++) {
				for (int z=-2; z<3; z++) {
					if(plugin.debug) {
						System.out.println("SpongeSearching: " + x + ", " + y + ", " + z);
					}
					Block currentBlock = originBlock.getRelative(x, y, z);
					if (currentBlock.getTypeId() == 19) {
						if(plugin.debug) {
							System.out.println("There's a sponge!");
						}
						return true;
					}
	    		}
    		}
		}
    	return false;
    }
    
    public void addToSpongeAreas(String coords) {
    	if (plugin.spongeAreas.containsKey(coords)) {
    		plugin.spongeAreas.put(coords, plugin.spongeAreas.get(coords) + 1);
    	} else {
    		plugin.spongeAreas.put(coords, 1);
    	}
    }
    
    public void removeFromSpongeAreas(String coords) {
    	if (plugin.spongeAreas.containsKey(coords)) {
    		plugin.spongeAreas.put(coords, plugin.spongeAreas.get(coords) - 1);
    		if (plugin.spongeAreas.get(coords) == 0) {
    			plugin.spongeAreas.remove(coords);
    		}
    	}
    }
    
    public String getBlockCoords(Block theBlock) {
    	return theBlock.getWorld().getName() + "." + theBlock.getX() + "." + theBlock.getY() + "." + theBlock.getZ();
    }
    
    public boolean isSponge(Block theBlock) {
    	if (theBlock.getType() == Material.SPONGE) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean isWater(Block theBlock) {
    	if (theBlock.getTypeId() == 8 || theBlock.getTypeId() == 9) {
    		return true;
    	} else {
    		return false;
    	}
    }
}
