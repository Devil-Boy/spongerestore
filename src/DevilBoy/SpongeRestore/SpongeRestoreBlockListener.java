package DevilBoy.SpongeRestore;

import java.util.HashSet;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * SpongeRestore block listener
 * @author DevilBoy
 */

public class SpongeRestoreBlockListener extends BlockListener {
    private final SpongeRestore plugin;
    private Config pluginSettings;
    public int spongeAreaUpLimit;
    public int spongeAreaDownLimit;
    public HashSet<Byte> transparentBlocks = new HashSet<Byte>();

    public SpongeRestoreBlockListener(final SpongeRestore plugin) {
        this.plugin = plugin;
        pluginSettings = plugin.pluginSettings;
        
        //Setting transparent blocks.
        transparentBlocks.add((byte) 0);
        transparentBlocks.add((byte) 8);
        transparentBlocks.add((byte) 9);
    }

    // Catch the events!
    public void onBlockPlace(BlockPlaceEvent event) {
    	Block involvedBlock = event.getBlock();
    	String theWorld = event.getBlock().getWorld().getName();
    	if(plugin.debug) {
    		System.out.println(event.getPlayer().getName() + " placed a block...");
    	}
    	// Check if the block is a Sponge
    	if (isSponge(involvedBlock) && !pluginSettings.excludedWorlds.contains(theWorld)) {
    		if(plugin.debug) {
    			System.out.println("and it's a sponge!!!!!");
    		}
    		
    		// Check for water or Lava
    		for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
    			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
    				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {		
    					if(plugin.debug) {
    						System.out.println("Checking: " + x + ", " + y + ", " + z);
    					}
    					Block currentBlock = event.getBlock().getRelative(x, y, z);
    					addToSpongeAreas(getBlockCoords(currentBlock));
    					if (blockIsAffected(currentBlock)) {
    						currentBlock.setType(Material.AIR);
    						if (plugin.debug) {
    							System.out.println("The sponge absorbed " + currentBlock.getType());
    						}
    					}
    	    		}
        		}
    		}
    		if (!pluginSettings.reduceOverhead) {
    			plugin.saveSpongeData();
    		}
    	}
    	
    	// Check if a water block is being placed within sponge's area
    	if (!pluginSettings.canPlaceWater && ((blockIsAffected(involvedBlock)) && plugin.spongeAreas.containsKey(getBlockCoords(involvedBlock)))) {
        	involvedBlock.setType(Material.AIR);
        	if(plugin.debug) {
            	System.out.println("You canot put liquid there!! :O");
        	}
    	}
    }
    
    public void onBlockFromTo(BlockFromToEvent event) {
    	if(plugin.debug) {
    		System.out.println("Liquid incoming at: " + event.getToBlock().getX() + ", " + event.getToBlock().getY() + ", " + event.getToBlock().getZ());
    	}
    	if (plugin.spongeAreas.containsKey(getBlockCoords(event.getToBlock())) && 
    			!pluginSettings.excludedWorlds.contains(event.getToBlock().getWorld().getName())) {
    		if(plugin.debug) {
    			System.out.println("Recede from sponge!");
    		}
    		if (blockIsAffected(event.getBlock())) {
    			event.setCancelled(true);
    		}
    	}
    }
    
    public void onBlockBreak(BlockBreakEvent event) {
    	Block wasSponge = event.getBlock();
    	if (isSponge(wasSponge)) {
    		if(plugin.debug) {
    			System.out.println("Sponge destroyed!");
    		}
    		
    		// Check the airea
    		for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
    			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
    				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {
    					Block currentBlock = wasSponge.getRelative(x, y, z);
    					removeFromSpongeAreas(getBlockCoords(currentBlock));
    					if(plugin.debug) {
    						System.out.println("AirSearching: " + x + ", " + y + ", " + z);
    					}
    					if (isAir(currentBlock)) {
    						currentBlock.setTypeId(0, true); // Turn air into air.
    					}
    	    		}
        		}
    		}
    		if (!pluginSettings.reduceOverhead) {
    			plugin.saveSpongeData();
    		}
    	}
    }
    
    public void onBlockIgnite(BlockIgniteEvent event) {
    	if(plugin.debug) {
    		System.out.println("Fire incoming at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
    	}
    	if (pluginSettings.absorbFire) {
    		if (plugin.spongeAreas.containsKey(getBlockCoords(event.getBlock())) && 
        				!pluginSettings.excludedWorlds.contains(event.getBlock().getWorld().getName())) {
        		if(plugin.debug) {
        			System.out.println("Extinguish fire with sponge!");
        		}
        		event.setCancelled(true);
    		}
    	}
    }
    
    public void onBlockBurn(BlockBurnEvent event) {
    	if(plugin.debug) {
    		System.out.println("Block Burning at: " + event.getBlock().getX() + ", " + event.getBlock().getY() + ", " + event.getBlock().getZ());
    	}
    	if (pluginSettings.absorbFire) {
    		if (plugin.spongeAreas.containsKey(getBlockCoords(event.getBlock())) && !pluginSettings.excludedWorlds.contains(event.getBlock().getWorld().getName())) {
        		if(plugin.debug) {
        			System.out.println("Sponge never lets a block burn!");
        		}
        		event.setCancelled(true);
        		killSurroundingFire(event.getBlock());
    		}
    	}
    }
    
    // Old Function
    public boolean isNearSponge(Block originBlock) {
    	for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {
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
    
    public boolean isLava(Block theBlock) {
    	if (theBlock.getTypeId() == 10 || theBlock.getTypeId() == 11) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean isFire(Block theBlock) {
    	if (theBlock.getTypeId() == 51) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public boolean isAir(Block theBlock) {
    	if (theBlock.getType() == Material.AIR) {
    		return true;
    	} else {
    		return false;
    	}
    }

	public void setConfig(Config pluginSettings2) {
		pluginSettings = pluginSettings2;
		spongeAreaUpLimit = pluginSettings.spongeRadius + 1;
	    spongeAreaDownLimit = pluginSettings.spongeRadius * -1;
	}
	
	public boolean blockIsAffected(Block theBlock) {
		if (isWater(theBlock)) {
			return true;
		} else if (isLava(theBlock)) {
			if (pluginSettings.absorbLava) {
				return true;
			}
		} else if (isFire(theBlock)) {
			if(pluginSettings.absorbFire) {
				return true;
			}
		}
		return false;
	}
	
	public Boolean isNextToSpongeArea(Block theBlock) {
		if (plugin.spongeAreas.containsKey(getBlockCoords(theBlock.getFace(BlockFace.NORTH)))) {
			if(plugin.debug) {
				System.out.println("Fire wont spread north!");
			}
			return true;
		}
		if (plugin.spongeAreas.containsKey(getBlockCoords(theBlock.getFace(BlockFace.EAST)))) {
			if(plugin.debug) {
				System.out.println("Fire wont spread east!");
			}
			return true;
		}
		if (plugin.spongeAreas.containsKey(getBlockCoords(theBlock.getFace(BlockFace.SOUTH)))) {
			if(plugin.debug) {
				System.out.println("Fire wont spread south!");
			}
			return true;
		}
		if (plugin.spongeAreas.containsKey(getBlockCoords(theBlock.getFace(BlockFace.WEST)))) {
			if(plugin.debug) {
				System.out.println("Fire wont spread west!");
			}
			return true;
		}
		if (plugin.spongeAreas.containsKey(getBlockCoords(theBlock.getFace(BlockFace.UP)))) {
			if(plugin.debug) {
				System.out.println("Fire wont spread up!");
			}
			return true;
		}
		if (plugin.spongeAreas.containsKey(getBlockCoords(theBlock.getFace(BlockFace.DOWN)))) {
			if(plugin.debug) {
				System.out.println("Fire wont spread down!");
			}
			return true;
		}
		return false;
	}
	
	public void killSurroundingFire(Block fireMan) {
		if (isFire(fireMan.getFace(BlockFace.NORTH))) {
			fireMan.getFace(BlockFace.NORTH).setTypeId(0, true);
		}
		if (isFire(fireMan.getFace(BlockFace.EAST))) {
			fireMan.getFace(BlockFace.EAST).setTypeId(0, true);
		}
		if (isFire(fireMan.getFace(BlockFace.SOUTH))) {
			fireMan.getFace(BlockFace.SOUTH).setTypeId(0, true);
		}
		if (isFire(fireMan.getFace(BlockFace.WEST))) {
			fireMan.getFace(BlockFace.WEST).setTypeId(0, true);
		}
		if (isFire(fireMan.getFace(BlockFace.UP))) {
			fireMan.getFace(BlockFace.UP).setTypeId(0, true);
		}
		if (isFire(fireMan.getFace(BlockFace.DOWN))) {
			fireMan.getFace(BlockFace.DOWN).setTypeId(0, true);
		}
	}
	
	public boolean enableSponge(Block spongeBlock) {
    	String theWorld = spongeBlock.getWorld().getName();
    	// Check if the block is a Sponge
    	if (isSponge(spongeBlock) && !pluginSettings.excludedWorlds.contains(theWorld)) {
    		// Check for water or Lava
    		for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
    			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
    				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {		
    					if(plugin.debug) {
    						System.out.println("Checking: " + x + ", " + y + ", " + z);
    					}
    					Block currentBlock = spongeBlock.getRelative(x, y, z);
    					addToSpongeAreas(getBlockCoords(currentBlock));
    					if (blockIsAffected(currentBlock)) {
    						currentBlock.setType(Material.AIR);
    						if (plugin.debug) {
    							System.out.println("The sponge absorbed " + currentBlock.getType());
    						}
    					}
    	    		}
        		}
    		}
    		if (!pluginSettings.reduceOverhead) {
    			plugin.saveSpongeData();
    		}
    		return true;
    	} else {
    		return false;
    	}
	}
	
	public boolean disableSponge(Block theSponge) {
    	if (isSponge(theSponge)) {
    		// Check the airea
    		for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
    			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
    				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {
    					Block currentBlock = theSponge.getRelative(x, y, z);
    					removeFromSpongeAreas(getBlockCoords(currentBlock));
    					if(plugin.debug) {
    						System.out.println("AirSearching: " + x + ", " + y + ", " + z);
    					}
    					if (isAir(currentBlock)) {
    						currentBlock.setTypeId(90, true);
    						currentBlock.setTypeId(0, true); // Turn air into air.
    					}
    	    		}
        		}
    		}
    		if (!pluginSettings.reduceOverhead) {
    			plugin.saveSpongeData();
    		}
    		return true;
    	} else {
    		return false;
    	}
    }
	
	public int convertAreaSponges(Player thePlayer, int radius, boolean enable) {
		Block theOrigin = thePlayer.getLocation().getBlock();
		int checkAreaUpLimit = radius + 1;
	    int checkAreaDownLimit = radius * -1;
	    int spongesConverted = 0;
		for (int x=checkAreaDownLimit; x<checkAreaUpLimit; x++) {
			for (int y=checkAreaDownLimit; y<checkAreaUpLimit; y++) {
				for (int z=checkAreaDownLimit; z<checkAreaUpLimit; z++) {
					Block currentBlock = theOrigin.getRelative(x, y, z);
					if (isSponge(currentBlock)) {
						if(plugin.debug) {
							System.out.println("Sponge found at: " + getBlockCoords(currentBlock));
						}
						if (enable) {
							enableSponge(currentBlock);
						} else {
							disableSponge(currentBlock);
						}
						spongesConverted++;
					}
	    		}
    		}
		}
		return spongesConverted;
	}
}
