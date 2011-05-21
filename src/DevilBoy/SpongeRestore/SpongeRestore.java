package DevilBoy.SpongeRestore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Properties;

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
    public HashMap<String, Integer> spongeAreas = new HashMap<String, Integer>();
    String pluginMainDir = "./plugins/SpongeRestore";
    String pluginConfigLocation = pluginMainDir + "/SpongeRestore.cfg";
    String spongeDbLocation = pluginMainDir + "/spongeAreas.dat";

    public void onEnable() {
        spongeAreas = loadSpongeData();
        
        // Obtain Configuration
        try {
        	Properties pluginSettings = new Properties();
            pluginSettings.load(new FileInputStream(new File(pluginConfigLocation)));
        } catch (Exception e) {
        	System.out.println("Could not load configuration! " + e);
        }
        
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
    	saveSpongeData();
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
    
    public void saveSpongeData() {
    	try{
    		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(spongeDbLocation));
    		oos.writeObject(spongeAreas);
    		oos.flush();
    		oos.close();
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public HashMap<String, Integer> loadSpongeData() {
    	if (!(new File(spongeDbLocation)).exists()) {
    		// Create the directory and database files!
    		boolean success = (new File(pluginMainDir)).mkdir();
    		if (success) {
    			System.out.println("New SpongeRestore directory created!");
    		}   
    		saveSpongeData();
    	}
    	if (!(new File(pluginConfigLocation)).exists()) {
    		createConfig();
    		System.out.println("SpongeRestore Configuration created!");
    	}
    	
    	try{
    		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(spongeDbLocation));
    		Object result = ois.readObject();
    		//you can feel free to cast result to HashMap<Player,Boolean> if you know there's that HashMap in the file
    		return (HashMap<String, Integer>)result;
    	} catch(Exception e){
    		e.printStackTrace();
    	}
		return spongeAreas;
    }
    
    public void createConfig() {
    	try{
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pluginConfigLocation)));
    		out.write("#");
    		out.newLine();
    		out.write("# SpongeRestore Configuration");
    		out.newLine();
    		out.write("#");
    		out.newLine();
    		out.write("");
    		out.newLine();
    		out.write("# Excluded worlds [names separated by commas]");
    		out.newLine();
    		out.write("#	Here you list all the worlds in which you");
    		out.newLine();
    		out.write("#	do not want this plugin to work in.");
    		out.newLine();
    		out.write("excludedWorlds=none");
    		out.newLine();
    		out.write("");
    		out.newLine();
    		out.write("# Sponge saturation [true or false]");
    		out.newLine();
    		out.write("#	Add more realism to sponges by making them only");
    		out.newLine();
    		out.write("#	absorb water from an area without blocking");
    		out.newLine();
    		out.write("#	water's flow afterwards.");
    		out.newLine();
    		out.write("spongeSaturation=false");
    		out.close();
    	} catch (Exception e) {
    		
    	}
    }
    
}

