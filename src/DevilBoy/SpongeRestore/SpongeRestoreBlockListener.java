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
    	String theWorld = event.getBlock().getWorld().getName();
    	System.out.println(event.getPlayer().getName() + " placed a block...");
    	// Check if the block is a Sponge
    	if (isSponge(event.getBlock())) {
    		System.out.println("and it's a sponge!!!!!");
    		
    		for (int x=-2; x<3; x++) {
    			for (int y=-2; y<3; y++) {
    				for (int z=-2; z<3; z++) {		
    					System.out.println("Checking: " + x + ", " + y + ", " + z);
    					Block currentBlock = event.getBlock().getRelative(x, y, z);
    					addToSpongeAreas(getBlockCoords(currentBlock));
    					if (currentBlock.getTypeId() == 8 || currentBlock.getTypeId() == 9) {
    						currentBlock.setType(Material.AIR);
    						System.out.println("The sponge absorbed water.");
    					}
    	    		}
        		}
    		}
    		plugin.saveSpongeData();
    	}
    	
    	// Check if water is being placed within sponge's area
    }
    
    public void onBlockFromTo(BlockFromToEvent event) {
    	System.out.println("Water incoming at: " + event.getToBlock().getX() + ", " + event.getToBlock().getY() + ", " + event.getToBlock().getZ());
    	if (plugin.spongeAreas.containsKey(getBlockCoords(event.getToBlock()))) {
    		System.out.println("Recede from sponge!");
    		event.setCancelled(true);
    	}
    }
    
    public void onBlockBreak(BlockBreakEvent event) {
    	Block wasSponge = event.getBlock();
    	if (isSponge(wasSponge)) {
    		System.out.println("Sponge destroyed!");
    		
    		// Check the airea
    		for (int x=-2; x<3; x++) {
    			for (int y=-2; y<3; y++) {
    				for (int z=-2; z<3; z++) {
    					Block currentBlock = wasSponge.getRelative(x, y, z);
    					removeFromSpongeAreas(getBlockCoords(currentBlock));
    					System.out.println("AirSearching: " + x + ", " + y + ", " + z);
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
					System.out.println("SpongeSearching: " + x + ", " + y + ", " + z);
					Block currentBlock = originBlock.getRelative(x, y, z);
					if (currentBlock.getTypeId() == 19) {
						System.out.println("There's a sponge!");
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
}
