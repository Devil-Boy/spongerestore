package DevilBoy.SpongeRestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Properties;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import DevilBoy.SpongeRestore.Config;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * SpongeRestore for Bukkit
 *
 * @author DevilBoy
 * 
 * TODO: Give sponges self-consciousness.
 * TODO: Grant sponges locomotion.
 * TODO: Inject sponges with the lust for domination over all liquids in the Minecraft universe, including those within the bodies of living humans.
 */

public class SpongeRestore extends JavaPlugin {
    public final SpongeRestorePlayerListener playerListener = new SpongeRestorePlayerListener(this);
    public final SpongeRestoreBlockListener blockListener = new SpongeRestoreBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    Config pluginSettings;
    public HashMap<String, Integer> spongeAreas = new HashMap<String, Integer>();
    String pluginMainDir = "./plugins/SpongeRestore";
    String pluginConfigLocation = pluginMainDir + "/SpongeRestore.cfg";
    String spongeRecipeLocation = pluginMainDir + "/SpongeRecipe.cfg";
    String spongeDbLocation = pluginMainDir + "/spongeAreas.dat";
	public boolean debug = false;
	private static PermissionHandler Permissions;

    public void onEnable() {
        spongeAreas = loadSpongeData();
        
        // Obtain Configuration
        try {
        	Properties preSettings = new Properties();
        	if ((new File(pluginConfigLocation)).exists()) {
        		preSettings.load(new FileInputStream(new File(pluginConfigLocation)));
        		if ((new File(spongeRecipeLocation)).exists()) {
        			preSettings.load(new FileInputStream(new File(spongeRecipeLocation)));
        			pluginSettings = new Config(preSettings, this, true);
        		} else {
        			pluginSettings = new Config(preSettings, this, false);
        			pluginSettings.createRecipeConfig();
        			System.out.println("SpongeRecipe created!");
        		}
        		debug = pluginSettings.debug;
        		if (!pluginSettings.upToDate) {
        			pluginSettings.createConfig();
        			System.out.println("SpongeRestore Configuration updated!");
        		}
        	} else {
        		if ((new File(spongeRecipeLocation)).exists()) {
        			preSettings.load(new FileInputStream(new File(spongeRecipeLocation)));
        			pluginSettings = new Config(preSettings, this, true);
        		} else {
        			pluginSettings = new Config(preSettings, this, false);
        			pluginSettings.createRecipeConfig();
        			System.out.println("SpongeRecipe created!");
        		}
        		pluginSettings.createConfig();
        		System.out.println("SpongeRestore Configuration created!");
        	}
        } catch (Exception e) {
        	System.out.println("Could not load configuration! " + e);
        }
        
        blockListener.setConfig(pluginSettings);
        playerListener.setConfig(pluginSettings);
        // Register our events
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
    	//The block fromto listener, and block break only need to be activated if sponge
    	//Saturation is off.
    	if(!pluginSettings.spongeSaturation) {
    		pm.registerEvent(Event.Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
    		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
    		pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Priority.Normal, this);
    		pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Priority.Normal, this);
    	}
    	pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY , playerListener, Priority.Normal, this);
    	
    	// Adding sponge recipe
    	if (pluginSettings.craftableSponges) {
	        getServer().addRecipe(pluginSettings.spongeRecipe);
    	}
    	
    	// Permissions turn on!
    	setupPermissions();
    	
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
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    	if(cmd.getName().equalsIgnoreCase("sponge") || cmd.getName().equalsIgnoreCase("sponges") || cmd.getName().equalsIgnoreCase("sr")) {
    		if (sender instanceof Player) {
    			Player player = (Player)sender;
    			if(hasPermissions(player, "SpongeRestore.set")) {
	    			if (args.length <2) {
	    				if (args.length <1) {
	    					args = new String[] {"", "", ""};
	    				}
	    				args = new String[] {args[0], "", ""};
	    			}
    				if (debug) {
    					System.out.println(player + " used /" + cmd.getName() + " " + args[0] + " " + args[1]);
    				}
    				if (args[0].equalsIgnoreCase("enable")) {
    					if (args[1].equalsIgnoreCase("target") || args[1].equalsIgnoreCase("this") || args[1].equalsIgnoreCase("one")) {
    						if (blockListener.disableSponge(player.getTargetBlock(blockListener.transparentBlocks, 100))) {
    							blockListener.enableSponge(player.getTargetBlock(blockListener.transparentBlocks, 100));
    							player.sendMessage(ChatColor.GREEN + "Successfully enabled sponge!");
    						} else {
    							player.sendMessage(ChatColor.GREEN + "That is not a sponge.");
    						}
    					} else if (args[1].equalsIgnoreCase("radius")) {
    						if (args.length >2) {
    							try {
    								blockListener.convertAreaSponges(player, Integer.parseInt(args[2]), false);
    								player.sendMessage(ChatColor.GREEN + "Sponges enabled: " + blockListener.convertAreaSponges(player, Integer.parseInt(args[2]), true));
    							} catch (NumberFormatException e) {
    								player.sendMessage(ChatColor.GREEN + "The radius must be a number.");
    							}
    						} else {
    							player.sendMessage(ChatColor.GREEN + "You must specify the radius. For example: /" + cmd.getName() + " enable radius 5");
    						}
    					} else {
    						player.sendMessage(ChatColor.GREEN + "Usage: /" + cmd.getName() + " enable <target/radius #>");
    						player.sendMessage(ChatColor.GREEN + "Chooose whether you want to enable just the sponge you're looking at, or all sponges within a certain radius.");
    					}
    				} else if (args[0].equalsIgnoreCase("disable")) {
    					if (args[1].equalsIgnoreCase("target") || args[1].equalsIgnoreCase("this") || args[1].equalsIgnoreCase("one")) {
    						if (blockListener.disableSponge(player.getTargetBlock(blockListener.transparentBlocks, 100))) {
    							player.sendMessage(ChatColor.GREEN + "Successfully disabled sponge!");
    						} else {
    							player.sendMessage(ChatColor.GREEN + "That is not a sponge.");
    						}
    					} else if (args[1].startsWith("radius")) {
    						if (args.length >2) {
    							try {
    								player.sendMessage(ChatColor.GREEN + "Sponges disabled: " + blockListener.convertAreaSponges(player, Integer.parseInt(args[2]), false));
    							} catch (NumberFormatException e) {
    								player.sendMessage(ChatColor.GREEN + "The radius must be a number.");
    							}
    						} else {
    							player.sendMessage(ChatColor.GREEN + "You must specify the radius. For example: /" + cmd.getName() + " disable radius 5");
    						}
    					} else {
    						player.sendMessage(ChatColor.GREEN + "Usage: /" + cmd.getName() + " disable <target/radius #>");
    						player.sendMessage(ChatColor.GREEN + "Chooose whether you want to enable just the sponge you're looking at, or all sponges within a certain radius.");
    					}
    				} else {
    					player.sendMessage(ChatColor.GREEN + "Usage: /" + cmd.getName() + " <enable/disable> <target/radius #>");
						player.sendMessage(ChatColor.GREEN + "Chooose whether you want to enable or disable sponges.");
    				}
    				
    			} else {
    				player.sendMessage(ChatColor.GREEN + "You do not have permission to use this command.");
    			}
    		}
    		return true;
    	}
    	return false;
    }
    
    private void setupPermissions() {
        Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");

        if (Permissions == null) {
            if (permissions != null) {
                Permissions = ((Permissions)permissions).getHandler();
            } else {
            }
        }
    }
    
    public static boolean hasPermissions(Player player, String node) {
        if (Permissions != null) {
            return Permissions.has(player, node);
        } else {
            return player.isOp();
        }
    }
    
}

