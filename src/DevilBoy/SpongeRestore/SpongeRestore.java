package DevilBoy.SpongeRestore;

import java.io.File;
import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
 * SpongeRestore for Bukkit
 *
 * @author DevilBoy
 */
public class SpongeRestore extends JavaPlugin {
    private final SpongeRestorePlayerListener playerListener = new SpongeRestorePlayerListener(this);
    private final SpongeRestoreBlockListener blockListener = new SpongeRestoreBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    public final HashMap<String, Integer> spongeAreas = new HashMap<String, Integer>();

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

        // Register our events
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
    	pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
    	pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
    	
    	//Adding sponge recipe
    	ShapedRecipe spongerecipie = new ShapedRecipe(new ItemStack(19));
        spongerecipie.shape("SXS","XSX","SXS");
        spongerecipie.setIngredient('S', Material.SAND);
        spongerecipie.setIngredient('X', Material.STRING);
        getServer().addRecipe(spongerecipie);

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        System.out.println("SpongeRestore disabled!");
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
    
    public void BlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild) {
    	
    }
}

