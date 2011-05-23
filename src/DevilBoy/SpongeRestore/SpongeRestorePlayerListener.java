package DevilBoy.SpongeRestore;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handle events for all Player related events
 * @author DevilBoy
 */

public class SpongeRestorePlayerListener extends PlayerListener {
    private final SpongeRestore plugin;
    private Config pluginSettings;

    public SpongeRestorePlayerListener(SpongeRestore instance) {
        plugin = instance;
        pluginSettings = instance.pluginSettings;
    }

    // Check if water is being dumped in the sponge's area
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    	Block involvedBlock = event.getBlockClicked().getFace(event.getBlockFace()) ;
    	String dumpLocation = plugin.blockListener.getBlockCoords(involvedBlock);
    	Material bucketType = event.getBucket();
    	if(plugin.debug) {
    		System.out.println(bucketType + " emptied!");
    	}
    	if(plugin.debug) {
    		System.out.println(involvedBlock.getType() + " dumped out!");
    	}
    	if (!pluginSettings.canPlaceWater && ((bucketType == Material.WATER_BUCKET || (pluginSettings.absorbLava && bucketType == Material.WATER_BUCKET)) && plugin.spongeAreas.containsKey(dumpLocation))) {
        	event.setCancelled(true);
        	if(plugin.debug) {
        		System.out.println("You can't dump liquid there!! :O (" + dumpLocation + ")");
        	}
        	event.setItemStack(new ItemStack(325));
    	}
    }
}

