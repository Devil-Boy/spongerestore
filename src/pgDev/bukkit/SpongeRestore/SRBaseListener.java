package pgDev.bukkit.SpongeRestore;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;

public class SRBaseListener implements Listener {
	private final SpongeRestore plugin;
	
	public SRBaseListener(final SpongeRestore pluginI) {
		plugin = pluginI;
	}
	
	// Deal with the birth of a sponge
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
    	if (!event.isCancelled()) {
	    	Block involvedBlock = event.getBlock();
	    	String theWorld = event.getBlock().getWorld().getName();
	    	if(plugin.debug) {
	    		System.out.println(event.getPlayer().getName() + " placed a block...");
	    	}
	    	// Check if the block is a Sponge
	    	if (SpongeRestore.isSponge(involvedBlock) && !plugin.pluginSettings.excludedWorlds.contains(theWorld)) {
	    		if(plugin.debug) {
	    			System.out.println("and it's a sponge!!!!!");
	    		}
	    		plugin.enableSponge(involvedBlock);
	    	}
	    	
	    	// Check if a water block is being placed within sponge's area
	    	if (!plugin.pluginSettings.canPlaceWater && ((plugin.blockIsAffected(involvedBlock)) && plugin.spongeAreas.containsKey(SpongeRestore.getBlockCoords(involvedBlock)))) {
	        	involvedBlock.setType(Material.AIR);
	        	if(plugin.debug) {
	            	System.out.println("You canot put liquid there!! :O");
	        	}
	    	}
		}
    }
	
	// Check if water is being dumped in the sponge's area
	@EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    	Block involvedBlock = event.getBlockClicked().getRelative(event.getBlockFace()) ;
    	String dumpLocation = SpongeRestore.getBlockCoords(involvedBlock);
    	Material bucketType = event.getBucket();
    	if(plugin.debug) {
    		System.out.println(bucketType + " emptied!");
    	}
    	if(plugin.debug) {
    		System.out.println(involvedBlock.getType() + " dumped out!");
    	}
    	if (!plugin.pluginSettings.canPlaceWater && ((bucketType == Material.WATER_BUCKET || (plugin.pluginSettings.absorbLava && bucketType == Material.LAVA_BUCKET)) && plugin.spongeAreas.containsKey(dumpLocation))) {
        	event.setCancelled(true);
        	if(plugin.debug) {
        		System.out.println("You can't dump liquid there!! :O (" + dumpLocation + ")");
        	}
        	event.setItemStack(new ItemStack(325));
    	}
    }
}
