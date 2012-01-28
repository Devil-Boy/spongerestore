package pgDev.bukkit.SpongeRestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;

import pgDev.bukkit.SpongeRestore.SRConfig;


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
	// Listeners
	public final SRBaseListener baseListener = new SRBaseListener(this);
	public final SRSuperSpongeListener superListener = new SRSuperSpongeListener(this);
    
	// Configuration
	SRConfig pluginSettings;
	
	// Main database
    public HashMap<String, Integer> spongeAreas = new HashMap<String, Integer>();
    
    // File locations
    String pluginMainDir = "./plugins/SpongeRestore";
    String pluginConfigLocation = pluginMainDir + "/SpongeRestore.cfg";
    String spongeRecipeLocation = pluginMainDir + "/SpongeRecipe.cfg";
    String spongeDbLocation = pluginMainDir + "/spongeAreas.dat";
    
    // Debug switch
	public boolean debug = false;
	
	// Legacy permissions handler
	private static PermissionHandler Permissions;
	
	// Water return flow timers
	public LinkedList<SRFlowTimer> flowTimers = new LinkedList<SRFlowTimer>();
	
	// Sponge area size limits
	public int spongeAreaUpLimit;
    public int spongeAreaDownLimit;
    
    // List of transparent blocks
    public HashSet<Byte> transparentBlocks = new HashSet<Byte>();

    public void onEnable() {
        spongeAreas = loadSpongeData();
        
        // Obtain Configuration
        try {
        	Properties preSettings = new Properties();
        	if ((new File(pluginConfigLocation)).exists()) {
        		preSettings.load(new FileInputStream(new File(pluginConfigLocation)));
        		if ((new File(spongeRecipeLocation)).exists()) {
        			preSettings.load(new FileInputStream(new File(spongeRecipeLocation)));
        			pluginSettings = new SRConfig(preSettings, this, true);
        		} else {
        			pluginSettings = new SRConfig(preSettings, this, false);
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
        			pluginSettings = new SRConfig(preSettings, this, true);
        		} else {
        			pluginSettings = new SRConfig(preSettings, this, false);
        			pluginSettings.createRecipeConfig();
        			System.out.println("SpongeRecipe created!");
        		}
        		pluginSettings.createConfig();
        		System.out.println("SpongeRestore Configuration created!");
        	}
        } catch (Exception e) {
        	System.out.println("Could not load configuration! " + e);
        }
        
        // Set the limits
        spongeAreaUpLimit = pluginSettings.spongeRadius + 1;
	    spongeAreaDownLimit = pluginSettings.spongeRadius * -1;
	    
	    // Set transparent blocks.
        transparentBlocks.add((byte) 0); // Air
        transparentBlocks.add((byte) 8); // Water
        transparentBlocks.add((byte) 9); // Stationary Water
        transparentBlocks.add((byte) 65); // Ladder
        transparentBlocks.add((byte) 66); // Rail
        transparentBlocks.add((byte) 78); // Snow
        
        // Register our events
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(baseListener, this);
    	if(!pluginSettings.spongeSaturation) {
    		pm.registerEvents(superListener, this);
    	}
    	
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
    
    @SuppressWarnings("unchecked")
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
    			if(hasPermissions(player, "SpongeRestore.enable") || hasPermissions(player, "SpongeRestore.disable") || hasPermissions(player, "SpongeRestore.clear")) {
	    			if (args.length <2) {
	    				if (args.length <1) {
	    					args = new String[] {"", "", ""};
	    				}
	    				args = new String[] {args[0], "", ""};
	    			}
    				if (debug) {
    					System.out.println(player + " used /" + cmd.getName() + " " + args[0] + " " + args[1]);
    				}
    				if (args[0].equalsIgnoreCase("enable") && hasPermissions(player, "SpongeRestore.enable")) {
    					if (args[1].equalsIgnoreCase("target") || args[1].equalsIgnoreCase("this") || args[1].equalsIgnoreCase("one")) {
    						if (isSponge(player.getTargetBlock(transparentBlocks, 100))) {
    							enableSponge(player.getTargetBlock(transparentBlocks, 100));
    							player.sendMessage(ChatColor.GREEN + "Successfully enabled sponge!");
    						} else {
    							player.sendMessage(ChatColor.GREEN + "That is not a sponge.");
    						}
    					} else if (args[1].equalsIgnoreCase("radius")) {
    						if (args.length >2) {
    							try {
    								player.sendMessage(ChatColor.GREEN + "Sponges enabled: " + convertAreaSponges(player, Integer.parseInt(args[2]), true));
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
    				} else if (args[0].equalsIgnoreCase("disable") && hasPermissions(player, "SpongeRestore.disable")) {
    					if (args[1].equalsIgnoreCase("target") || args[1].equalsIgnoreCase("this") || args[1].equalsIgnoreCase("one")) {
    						Block targetBlock = player.getTargetBlock(transparentBlocks, 100);
    						if (isSponge(targetBlock)) {
    							disableSponge(targetBlock);
    							player.sendMessage(ChatColor.GREEN + "Successfully disabled sponge!");
    						} else {
    							player.sendMessage(ChatColor.GREEN + "That is not a sponge.");
    						}
    					} else if (args[1].startsWith("radius")) {
    						if (args.length >2) {
    							try {
    								player.sendMessage(ChatColor.GREEN + "Sponges disabled: " + convertAreaSponges(player, Integer.parseInt(args[2]), false));
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
    				} else if (args[0].equalsIgnoreCase("clear") && hasPermissions(player, "SpongeRestore.clear")) {
    					spongeAreas.clear();
						player.sendMessage(ChatColor.GREEN + "spongeAreas database cleared!");
    				} else {
    					player.sendMessage(ChatColor.GREEN + "Either that command doesn't exist, or you don't have the permissions to use it.");
    					//player.sendMessage(ChatColor.GREEN + "Usage: /" + cmd.getName() + " <enable/disable/clear>");
						//player.sendMessage(ChatColor.GREEN + "Chooose whether you want to enable or disable sponges.");
    				}
    				
    			} else {
    				player.sendMessage(ChatColor.GREEN + "You do not have permission to use this command.");
    			}
    		}
    		return true;
    	}
    	return false;
    }
    
    // Permissions Methods
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
            return player.hasPermission(node);
        }
    }
    
    // Non-Static Functions
    public void enableSponge(Block spongeBlock) {
    	// Check for water or Lava
		for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {		
					if(debug) {
						System.out.println("Checking: " + x + ", " + y + ", " + z);
					}
					Block currentBlock = spongeBlock.getRelative(x, y, z);
					addToSpongeAreas(getBlockCoords(currentBlock));
					if (blockIsAffected(currentBlock)) {
						currentBlock.setType(Material.AIR);
						if (debug) {
							System.out.println("The sponge absorbed " + currentBlock.getType());
						}
					}
	    		}
    		}
		}
		if (!pluginSettings.reduceOverhead) {
			saveSpongeData();
		}
    }
	
	public LinkedList<String> disableSponge(Block theSponge) {
		LinkedList<String> markedBlocks = new LinkedList<String>();
		for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {
					Block currentBlock = theSponge.getRelative(x, y, z);
					removeFromSpongeAreas(SpongeRestore.getBlockCoords(currentBlock));
					if (pluginSettings.restoreWater) {
						if (!spongeAreas.containsKey(SpongeRestore.getBlockCoords(currentBlock))) {
							markAsRemoved(SpongeRestore.getBlockCoords(currentBlock));
    						markedBlocks.add(SpongeRestore.getDeletedBlockCoords(currentBlock));
						}
					}
					if(debug) {
						System.out.println("AirSearching: " + x + ", " + y + ", " + z);
					}
					if (SpongeRestore.isAir(currentBlock)) {
						currentBlock.setTypeId(90, true);
						currentBlock.setTypeId(0, true); // Turn air into air.
					}
	    		}
    		}
		}
		if (!pluginSettings.reduceOverhead) {
			saveSpongeData();
		}
		return markedBlocks;
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
    
    public void addToSpongeAreas(String coords) {
    	if (spongeAreas.containsKey(coords)) {
    		spongeAreas.put(coords, spongeAreas.get(coords) + 1);
    	} else {
    		spongeAreas.put(coords, 1);
    	}
    }
    
    public void removeFromSpongeAreas(String coords) {
    	if (spongeAreas.containsKey(coords)) {
    		spongeAreas.put(coords, spongeAreas.get(coords) - 1);
    		if (spongeAreas.get(coords) == 0) {
    			spongeAreas.remove(coords);
    		}
    	}
    }
    
    public void completeRemoveFromSpongeAreas(String coords) {
    	spongeAreas.remove(coords);
    }
    
    public void markAsRemoved(String coords) {
    	String removedCoord = coords + ".removed";
    	if (spongeAreas.containsKey(removedCoord)) {
    		spongeAreas.put(removedCoord, spongeAreas.get(removedCoord) + 1);
    	} else {
        	spongeAreas.put(removedCoord, 1);
    	}
    }
    
    public Boolean isNextToSpongeArea(Block theBlock) {
		if (spongeAreas.containsKey(getBlockCoords(theBlock.getRelative(BlockFace.NORTH)))) {
			if (debug) {
				System.out.println("Fire wont spread north!");
			}
			return true;
		}
		if (spongeAreas.containsKey(getBlockCoords(theBlock.getRelative(BlockFace.EAST)))) {
			if (debug) {
				System.out.println("Fire wont spread east!");
			}
			return true;
		}
		if (spongeAreas.containsKey(getBlockCoords(theBlock.getRelative(BlockFace.SOUTH)))) {
			if (debug) {
				System.out.println("Fire wont spread south!");
			}
			return true;
		}
		if (spongeAreas.containsKey(getBlockCoords(theBlock.getRelative(BlockFace.WEST)))) {
			if (debug) {
				System.out.println("Fire wont spread west!");
			}
			return true;
		}
		if (spongeAreas.containsKey(getBlockCoords(theBlock.getRelative(BlockFace.UP)))) {
			if (debug) {
				System.out.println("Fire wont spread up!");
			}
			return true;
		}
		if (spongeAreas.containsKey(getBlockCoords(theBlock.getRelative(BlockFace.DOWN)))) {
			if (debug) {
				System.out.println("Fire wont spread down!");
			}
			return true;
		}
		return false;
	}
    
    public void killSurroundingFire(Block fireMan) {
		if (isFire(fireMan.getRelative(BlockFace.NORTH))) {
			fireMan.getRelative(BlockFace.NORTH).setTypeId(0, true);
		}
		if (isFire(fireMan.getRelative(BlockFace.EAST))) {
			fireMan.getRelative(BlockFace.EAST).setTypeId(0, true);
		}
		if (isFire(fireMan.getRelative(BlockFace.SOUTH))) {
			fireMan.getRelative(BlockFace.SOUTH).setTypeId(0, true);
		}
		if (isFire(fireMan.getRelative(BlockFace.WEST))) {
			fireMan.getRelative(BlockFace.WEST).setTypeId(0, true);
		}
		if (isFire(fireMan.getRelative(BlockFace.UP))) {
			fireMan.getRelative(BlockFace.UP).setTypeId(0, true);
		}
		if (isFire(fireMan.getRelative(BlockFace.DOWN))) {
			fireMan.getRelative(BlockFace.DOWN).setTypeId(0, true);
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
						if(debug) {
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
    
    // Static Functions
    public static String getBlockCoords(Block theBlock) {
    	return theBlock.getWorld().getName() + "." + theBlock.getX() + "." + theBlock.getY() + "." + theBlock.getZ();
    }
    
    public static String getDeletedBlockCoords(Block theBlock) {
    	return theBlock.getWorld().getName() + "." + theBlock.getX() + "." + theBlock.getY() + "." + theBlock.getZ() + ".removed";
    }
    
    public static boolean isSponge(Block theBlock) {
    	if (theBlock.getType() == Material.SPONGE) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean isWater(Block theBlock) {
    	if (theBlock.getTypeId() == 8 || theBlock.getTypeId() == 9) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean isLava(Block theBlock) {
    	if (theBlock.getTypeId() == 10 || theBlock.getTypeId() == 11) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean isFire(Block theBlock) {
    	if (theBlock.getTypeId() == 51) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean isAir(Block theBlock) {
    	if (theBlock.getType() == Material.AIR) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean isIce(Block theBlock) {
    	if (theBlock.getType() == Material.ICE) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
}

