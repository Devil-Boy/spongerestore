package pgDev.bukkit.SpongeRestore;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

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
	public final SRSaturatedSpongeListener saturatedListener = new SRSaturatedSpongeListener(this);
	public final SRSuperSpongeListener superListener = new SRSuperSpongeListener(this);
    
	// Configuration
	SRConfig pluginSettings;
	
	// Main database
    public ConcurrentHashMap<String, Integer> spongeAreas = new ConcurrentHashMap<String, Integer>();
    
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
    	if(pluginSettings.spongeSaturation) {
    		pm.registerEvents(saturatedListener, this);
    	} else {
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
	public ConcurrentHashMap<String, Integer> loadSpongeData() {
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
    		if (result instanceof ConcurrentHashMap) {
    			return (ConcurrentHashMap<String, Integer>)result;
    		} else if (result instanceof HashMap) {
    			System.out.println("Updated sponge database to ConcurrentHashMap.");
    			return new ConcurrentHashMap<String, Integer>((Map<String, Integer>)result);
    		}
    	} catch(Exception e){
    		e.printStackTrace();
    	}
		return spongeAreas;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    	if (sender instanceof Player) {
			Player player = (Player)sender;
			if(hasPermissions(player, "spongerestore.enable") || hasPermissions(player, "spongerestore.disable") || hasPermissions(player, "spongerestore.clear")) {
    			if (args.length == 0) { // Give aide!
    				player.sendMessage(ChatColor.GREEN + "SpongeRestore Commands:");
    				if (hasPermissions(player, "spongerestore.enable")) {
    					player.sendMessage(ChatColor.GREEN + "/sponge enable <target/radius/selection> [#]");
    				}
    				if (hasPermissions(player, "spongerestore.disable")) {
    					player.sendMessage(ChatColor.GREEN + "/sponge disable <target/radius/selection> [#]");
    				}
					if (hasPermissions(player, "spongerestore.clear")) {
						if (hasPermissions(player, "spongerestore.clear.all")) {
							player.sendMessage(ChatColor.GREEN + "/sponge clear <all/selection/world> [worldname]");
						} else {
							player.sendMessage(ChatColor.GREEN + "/sponge clear selection");
						}
					}
    			} else {
    				if (args[0].equalsIgnoreCase("enable") && hasPermissions(player, "spongerestore.enable")) {
    					if (args[1].toLowerCase().startsWith("t")) {
    						if (isSponge(player.getTargetBlock(transparentBlocks, 100))) {
    							enableSponge(player.getTargetBlock(transparentBlocks, 100));
    							player.sendMessage(ChatColor.GOLD + "Successfully enabled sponge!");
    						} else {
    							player.sendMessage(ChatColor.RED + "That is not a sponge.");
    						}
    					} else if (args[1].toLowerCase().startsWith("r")) {
    						if (args.length > 2) {
    							try {
    								player.sendMessage(ChatColor.GOLD + "Sponges enabled: " + convertAreaSponges(player, Integer.parseInt(args[2]), true));
    							} catch (NumberFormatException e) {
    								player.sendMessage(ChatColor.RED + "The radius must be a number.");
    							}
    						} else {
    							player.sendMessage(ChatColor.GREEN + "You must specify the radius. For example: /" + cmd.getName() + " enable radius 5");
    						}
    					} else if (args[1].toLowerCase().startsWith("s")) {
    						Plugin wePlugin = player.getServer().getPluginManager().getPlugin("WorldEdit");
    						if (wePlugin == null) {
    							player.sendMessage(ChatColor.RED + "WorldEdit was not found on this server.");
    						} else {
    							Selection chosenArea = ((WorldEditPlugin) wePlugin).getSelection(player);
    							if (chosenArea instanceof CuboidSelection) {
    								LinkedList<Block> toEnable = getSponges(getBlocksInSelection(chosenArea));
    								LinkedList<Block> toDisable = new LinkedList<Block>();
    								(new Thread(new SRMultiSpongeThread(toEnable, toDisable, this))).start();
    								player.sendMessage(ChatColor.GOLD + "Sponges being enabled: " + toEnable.size());
    							} else {
    								player.sendMessage(ChatColor.RED + "Your selection must be cuboid.");
    							}
    						}
    					} else {
    						player.sendMessage(ChatColor.GREEN + "Usage: /" + cmd.getName() + " enable <target/radius #/selection>");
    						player.sendMessage(ChatColor.GREEN + "Choose whether you want to enable just the sponge you're looking at, or all sponges within a certain radius.");
    					}
    				} else if (args[0].equalsIgnoreCase("disable") && hasPermissions(player, "spongerestore.disable")) {
    					if (args[1].toLowerCase().startsWith("t")) {
    						Block targetBlock = player.getTargetBlock(transparentBlocks, 100);
    						if (isSponge(targetBlock)) {
    							disableSponge(targetBlock);
    							player.sendMessage(ChatColor.GOLD + "Successfully disabled sponge!");
    						} else {
    							player.sendMessage(ChatColor.RED + "That is not a sponge.");
    						}
    					} else if (args[1].toLowerCase().startsWith("r")) {
    						if (args.length > 2) {
    							try {
    								player.sendMessage(ChatColor.GOLD + "Sponges disabled: " + convertAreaSponges(player, Integer.parseInt(args[2]), false));
    							} catch (NumberFormatException e) {
    								player.sendMessage(ChatColor.RED + "The radius must be a number.");
    							}
    						} else {
    							player.sendMessage(ChatColor.GREEN + "You must specify the radius. For example: /" + cmd.getName() + " disable radius 5");
    						}
    					} else if (args[1].toLowerCase().startsWith("s")) {
    						Plugin wePlugin = player.getServer().getPluginManager().getPlugin("WorldEdit");
    						if (wePlugin == null) {
    							player.sendMessage(ChatColor.RED + "WorldEdit was not found on this server.");
    						} else {
    							Selection chosenArea = ((WorldEditPlugin) wePlugin).getSelection(player);
    							if (chosenArea instanceof CuboidSelection) {
    								LinkedList<Block> toDisable = getSponges(getBlocksInSelection(chosenArea));
    								LinkedList<Block> toEnable = new LinkedList<Block>();
    								(new Thread(new SRMultiSpongeThread(toEnable, toDisable, this))).start();
    								player.sendMessage(ChatColor.GOLD + "Sponges being disabled: " + toDisable.size());
    							} else {
    								player.sendMessage(ChatColor.RED + "Your selection must be cuboid.");
    							}
    						}
    					} else {
    						player.sendMessage(ChatColor.GREEN + "Usage: /" + cmd.getName() + " disable <target/radius #/selection>");
    						player.sendMessage(ChatColor.GREEN + "Choose whether you want to enable just the sponge you're looking at, or all sponges within a certain radius.");
    					}
    				} else if (args[0].equalsIgnoreCase("clear") && hasPermissions(player, "spongerestore.clear")) {
    					if (args[1].equalsIgnoreCase("all")) { // Don't let them mess up!
    						if (hasPermissions(player, "spongerestore.clear.world.all")) {
    							spongeAreas.clear();
        						player.sendMessage(ChatColor.GOLD + "spongeAreas database cleared!");
    						} else {
    							player.sendMessage(ChatColor.RED + "You do not have permission to clear the whole sponge database!");
    						}
    					} else if (args[1].toLowerCase().startsWith("s")) {
    						Plugin wePlugin = player.getServer().getPluginManager().getPlugin("WorldEdit");
    						if (wePlugin == null) {
    							player.sendMessage(ChatColor.RED + "WorldEdit was not found on this server.");
    						} else {
    							Selection chosenArea = ((WorldEditPlugin) wePlugin).getSelection(player);
    							if (chosenArea instanceof CuboidSelection) {
    								int num = completeRemoveBlocksFromAreas(getBlocksInSelection(chosenArea));
    								player.sendMessage(ChatColor.GOLD + "Water restricted areas removed: " + num);
    							} else {
    								player.sendMessage(ChatColor.RED + "Your selection must be cuboid.");
    							}
    						}
    					} else if (args[1].toLowerCase().startsWith("w")) { // wants to wipe a world
    						if (hasPermissions(player, "spongerestore.clear.all")) {
    							if (args.length > 2) {
        							World chosenWorld = getServer().getWorld(args[2]);
        							if (chosenWorld == null) {
        								player.sendMessage(ChatColor.RED + "The world you specified was not found on this server.");
        							} else {
        								spongeAreas = wipeWorld(spongeAreas, chosenWorld.getName());
        								saveSpongeData();
        								player.sendMessage(ChatColor.GOLD + "All sponge areas cleared from world: " + chosenWorld.getName());
        							}
        						} else {
        							player.sendMessage(ChatColor.GREEN + "You must specify the world. For example: /" + cmd.getName() + " clear world world_nether");
        						}
    						} else {
    							player.sendMessage(ChatColor.RED + "You do not have permission to clear worlds of sponges.");
    						}
    					} else {
    						player.sendMessage(ChatColor.GREEN + "Usage: /" + cmd.getName() + " clear <all/selection/world> [worldname]");
    						player.sendMessage(ChatColor.GREEN + "Clear the whole databse? Or just a WorldEdit selection?");
    					}
    				} else {
    					player.sendMessage(ChatColor.GREEN + "Either that command doesn't exist, or you don't have the permissions to use it.");
    				}
    			}
			} else {
				player.sendMessage(ChatColor.GREEN + "You do not have permission to use this command.");
			}
		}
		return true;
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
    }
	
	public LinkedList<String> disableSponge(Block theSponge) {
		LinkedList<String> markedBlocks = new LinkedList<String>();
		for (int x=spongeAreaDownLimit; x<spongeAreaUpLimit; x++) {
			for (int y=spongeAreaDownLimit; y<spongeAreaUpLimit; y++) {
				for (int z=spongeAreaDownLimit; z<spongeAreaUpLimit; z++) {
					Block currentBlock = theSponge.getRelative(x, y, z);
					removeFromSpongeAreas(getBlockCoords(currentBlock));
					if (pluginSettings.restoreWater) {
						if (!spongeAreas.containsKey(getBlockCoords(currentBlock))) {
							markAsRemoved(getBlockCoords(currentBlock));
    						markedBlocks.add(getDeletedBlockCoords(currentBlock));
						}
					}
					if(debug) {
						System.out.println("AirSearching: " + x + ", " + y + ", " + z);
					}
					if (isAir(currentBlock)) {
						currentBlock.setTypeId(36, true); // Technical clear block
						currentBlock.setTypeId(0, true); // Turn air into air.
					}
	    		}
    		}
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
    
    public int completeRemoveBlocksFromAreas(LinkedList<Block> blawks) {
    	int output = 0;
    	for (Block blawk : blawks) {
    		String coords = getBlockCoords(blawk);
    		if (spongeAreas.containsKey(coords)) {
    			spongeAreas.remove(getBlockCoords(blawk));
    			output++;
    		}
    	}
    	return output;
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
    
    // Universal Functions
    public String getBlockCoords(Block theBlock) {
    	return theBlock.getWorld().getName() + "." + theBlock.getX() + "." + theBlock.getY() + "." + theBlock.getZ();
    }
    
    public String getDeletedBlockCoords(Block theBlock) {
    	return theBlock.getWorld().getName() + "." + theBlock.getX() + "." + theBlock.getY() + "." + theBlock.getZ() + ".removed";
    }
    
    public boolean isSponge(Block theBlock) {
    	return (theBlock.getType() == Material.SPONGE);
    }
    
    public boolean isWater(Block theBlock) {
    	return (theBlock.getTypeId() == 8 || theBlock.getTypeId() == 9);
    }
    
    public boolean isLava(Block theBlock) {
    	return (theBlock.getTypeId() == 10 || theBlock.getTypeId() == 11);
    }
    
    public boolean isFire(Block theBlock) {
    	return (theBlock.getTypeId() == 51);
    }
    
    public boolean isAir(Block theBlock) {
    	return (theBlock.getType() == Material.AIR);
    }
    
    public boolean isIce(Block theBlock) {
    	return (theBlock.getType() == Material.ICE);
    }
    
    public boolean hasSponges(List<Block> blocks) {
    	for (Block blawk : blocks) {
    		if (isSponge(blawk)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public LinkedList<Block> getSponges(List<Block> blocks) {
    	LinkedList<Block> output = new LinkedList<Block>();
    	for (Block blawk : blocks) {
    		if (isSponge(blawk)) {
    			output.add(blawk);
    		}
    	}
    	return output;
    }
    
    public LinkedList<Block> getBlocksInSelection(Selection sel) {
    	LinkedList<Block> output = new LinkedList<Block>();
    	World w = sel.getWorld();
    	Location minPoint = sel.getMinimumPoint();
    	Location maxPoint = sel.getMaximumPoint();
    	for (int x=minPoint.getBlockX(); x<=maxPoint.getBlockX(); x++) {
    		for (int y=minPoint.getBlockY(); y<=maxPoint.getBlockY(); y++) {
    			for (int z=minPoint.getBlockZ(); z<=maxPoint.getBlockZ(); z++) {
    	    		output.add(w.getBlockAt(x, y, z));
    	    	}
        	}
    	}
    	return output;
    }
    
    public ConcurrentHashMap<String, Integer> wipeWorld(ConcurrentHashMap<String, Integer> originalDB, String world) {
    	for (String coord : originalDB.keySet()) {
    		if (coord.split("\\.")[0].equals(world)) {
    			originalDB.remove(coord);
    		}
    	}
    	return originalDB;
    }
}

