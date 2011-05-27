package DevilBoy.SpongeRestore;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

/**
 * Editable configuration class (user input)
 * @author DevilBoy
 */

public class Config implements java.io.Serializable {
	private Properties properties;
	private final SpongeRestore plugin;
	public boolean upToDate = true;
	
	// List of Config Options
	LinkedList<String> excludedWorlds = new LinkedList<String>();
	boolean spongeSaturation = false;
	boolean canPlaceWater = false;
	boolean debug = false;
	boolean craftableSponges = true;
	boolean absorbLava = false;
	boolean absorbFire = false;
	boolean reduceOverhead = false;
	int spongeRadius = 2;
	
	// The Sponge Crafting Recipe
	ShapedRecipe spongeRecipe;
	
	public Config(Properties p, final SpongeRestore plugin, boolean customRecipe) throws NoSuchElementException {
        properties = p;
        this.plugin = plugin;
        
        // Sponge Recipe Default
        spongeRecipe = new ShapedRecipe(new ItemStack(19, 1));
    	spongeRecipe.shape("SXS","XSX","SXS");
    	spongeRecipe.setIngredient('S', Material.SAND);
    	spongeRecipe.setIngredient('X', Material.STRING);
        
        // Grab values here.
        excludedWorlds = getList("excludedWorlds", "");
        spongeSaturation = getBoolean("spongeSaturation", false);
        canPlaceWater = getBoolean("canPlaceWater", false);
        debug = getBoolean("debug", false);
        craftableSponges = getBoolean("craftableSponges", true);
        absorbLava = getBoolean("absorbLava", false);
        absorbFire = getBoolean("absorbFire", false);
        reduceOverhead = getBoolean("reduceOverhead", false);
        spongeRadius = getInt("spongeRadius", 2);
        
        if (customRecipe) {
        	spongeRecipe = getRecipe();
        }
        
    }
	
	public int getInt(String label, int thedefault) {
		String value;
        try {
        	value = getString(label);
        	return Integer.parseInt(value);
        }catch (NoSuchElementException e) {
        	return thedefault;
        }
    }
    
    public double getDouble(String label) throws NoSuchElementException {
        String value = getString(label);
        return Double.parseDouble(value);
    }
    
    public File getFile(String label) throws NoSuchElementException {
        String value = getString(label);
        return new File(value);
    }

    public boolean getBoolean(String label, boolean thedefault) {
    	String values;
        try {
        	values = getString(label);
        	return Boolean.valueOf(values).booleanValue();
        }catch (NoSuchElementException e) {
        	return thedefault;
        }
    }
    
    public Color getColor(String label) {
        String value = getString(label);
        Color color = Color.decode(value);
        return color;
    }
    
    public HashSet<String> getSet(String label, String thedefault) {
        String values;
        try {
        	values = getString(label);
        }catch (NoSuchElementException e) {
        	values = thedefault;
        }
        String[] tokens = values.split(",");
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < tokens.length; i++) {
            set.add(tokens[i].trim().toLowerCase());
        }
        return set;
    }
    
    public LinkedList<String> getList(String label, String thedefault) {
    	String values;
        try {
        	values = getString(label);
        }catch (NoSuchElementException e) {
        	values = thedefault;
        }
        if(plugin.debug) {
        	System.out.println("List from file: " + values);
        }
        if(!values.equals("")) {
            String[] tokens = values.split(",");
            LinkedList<String> set = new LinkedList<String>();
            for (int i = 0; i < tokens.length; i++) {
                set.add(tokens[i].trim().toLowerCase());
            }
            return set;
        }else {
        	return new LinkedList<String>();
        }
    }
    
    public String getString(String label) throws NoSuchElementException {
        String value = properties.getProperty(label);
        if (value == null) {
        	upToDate = false;
            throw new NoSuchElementException("Config did not contain: " + label);
        }
        return value;
    }
    
    public String linkedListToString(LinkedList<String> list) {
    	if(list.size() > 0) {
    		String compounded = "";
    		boolean first = true;
        	for(String value : list) {
        		if(first) {
        			compounded = value;
        			first = false;
        		}else {
        			compounded = compounded + "," + value;
        		}
        	}
        	return compounded;
    	}
    	return "";
    }
    
    public ShapedRecipe getRecipe() {
    	String topRow = "ABC";
    	String middleRow = "DEF";
    	String bottomRow = "GHI";
    	ShapedRecipe preRecipe = new ShapedRecipe(new ItemStack(19, 1));
    	preRecipe.shape(topRow, middleRow, bottomRow);
    	try {
    		preRecipe.setIngredient('A', Material.valueOf(getString("a")));
    	} catch (Exception e) {
    		topRow.replace("A", " ");
    	}
    	try {
    		preRecipe.setIngredient('B', Material.valueOf(getString("b")));
    	} catch (Exception e) {
    		topRow.replace("B", " ");
    	}
    	try {
    		preRecipe.setIngredient('C', Material.valueOf(getString("c")));
    	} catch (Exception e) {
    		topRow.replace("C", " ");
    	}
    	try {
    		preRecipe.setIngredient('D', Material.valueOf(getString("d")));
    	} catch (Exception e) {
    		topRow.replace("D", " ");
    	}
    	try {
    		preRecipe.setIngredient('E', Material.valueOf(getString("e")));
    	} catch (Exception e) {
    		topRow.replace("E", " ");
    	}
    	try {
    		preRecipe.setIngredient('F', Material.valueOf(getString("f")));
    	} catch (Exception e) {
    		topRow.replace("F", " ");
    	}
    	try {
    		preRecipe.setIngredient('G', Material.valueOf(getString("g")));
    	} catch (Exception e) {
    		topRow.replace("G", " ");
    	}
    	try {
    		preRecipe.setIngredient('H', Material.valueOf(getString("h")));
    	} catch (Exception e) {
    		topRow.replace("H", " ");
    	}
    	try {
    		preRecipe.setIngredient('I', Material.valueOf(getString("i")));
    	} catch (Exception e) {
    		topRow.replace("I", " ");
    	}
    	preRecipe.shape(topRow, middleRow, bottomRow);
    	return preRecipe;
    }
    
    public void createConfig() {
    	try{
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plugin.pluginConfigLocation)));
    		out.write("#\r\n");
    		out.write("# SpongeRestore Configuration\r\n");
    		out.write("#\r\n");
    		out.write("\r\n");
    		out.write("# Excluded worlds [names separated by commas]\r\n");
    		out.write("#	Here you list all the worlds in which you\r\n");
    		out.write("#	do not want this plugin to work in.\r\n");
    		out.write("excludedWorlds=" + linkedListToString(excludedWorlds) + "\r\n");
    		out.write("\r\n");
    		out.write("# Sponge saturation [true or false]\r\n");
    		out.write("#	Add more realism to sponges by making them only\r\n");
    		out.write("#	absorb water from an area without blocking\r\n");
    		out.write("#	water's flow afterwards.\r\n");
    		out.write("spongeSaturation=" + spongeSaturation + "\r\n");
    		out.write("\r\n");
    		out.write("# Water replacement\r\n");
    		out.write("#	Can a player place water near a sponge\r\n");
    		out.write("#	while it is still there?\r\n");
    		out.write("canPlaceWater=" + canPlaceWater + "\r\n");
    		out.write("\r\n");
    		out.write("# Craftable Sponges\r\n");
    		out.write("#	Choose whether this plugin lets players craft\r\n");
    		out.write("#	sponges or not. Useful for if you use anothe\r\n");
    		out.write("#	plugin to handle crafting recipes.\r\n");
    		out.write("craftableSponges=" + craftableSponges + "\r\n");
    		out.write("\r\n");
    		out.write("# Lava\r\n");
    		out.write("#	Should lava be affected also? Lava will be treated\r\n");
    		out.write("#	exactly like water as chosen in the other settings.\r\n");
    		out.write("absorbLava=" + absorbLava + "\r\n");
    		out.write("\r\n");
    		out.write("# Debug Messages\r\n");
    		out.write("#	This tends to spam your console, so you'd be best\r\n");
    		out.write("#	served leaving this off unless you know what\r\n");
    		out.write("#	you're doing.\r\n");
    		out.write("debug=" + debug + "\r\n");
    		out.write("\r\n");
    		out.write("# Fire\r\n");
    		out.write("#	Should fire be affected too??? It'll get treated\r\n");
    		out.write("#	just like the liquids.\r\n");
    		out.write("absorbFire=" + absorbFire + "\r\n");
    		out.write("\r\n");
    		out.write("# Reduce Overhead\r\n");
    		out.write("#	I recommand you keep this off. In normal circumstances\r\n");
    		out.write("#	the sponge database is saved on every sponge break\r\n");
    		out.write("#	and place, thus keeping the plugin safe for server\r\n");
    		out.write("#	crashes. With this option turned on, the sponge database\r\n");
    		out.write("#	will only be saved on plugin disable (clean exits).\r\n");
    		out.write("reduceOverhead=" + reduceOverhead +"\r\n");
    		out.write("\r\n");
    		out.write("# Affected Radius\r\n");
    		out.write("#	Here you can choose how large the area the sponge affects\r\n");
    		out.write("#	will be. It is based on the numbers of blocks away from\r\n");
    		out.write("#	the sponge. For example, setting this to 2 will give you a\r\n");
    		out.write("#	5x5x5 block area. It's recommended that you do not set\r\n");
    		out.write("#	this value to high as the plugin must check every block\r\n");
    		out.write("#	in the set radius.\r\n");
    		out.write("spongeRadius=" + spongeRadius + "\r\n");
    		out.close();
    	} catch (Exception e) {
    		// Not sure what to do? O.o
    	}
    }
    
    public void createRecipeConfig() {
    	try{
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plugin.spongeRecipeLocation)));
    		out.write("#\r\n");
    		out.write("# SpongeRestore crafting recipe configuration\r\n");
    		out.write("#\r\n");
    		out.write("\r\n");
    		out.write("# Choose your materials according to the\r\n");
    		out.write("# diagram below.To indicate an empty slot\r\n");
    		out.write("# leave the value blank.\r\n");
    		out.write("# You can get the list of material names from:\r\n");
    		out.write("# http://jd.bukkit.org/apidocs/org/bukkit/Material.html\r\n");
    		out.write("# Remember that thses are case-sensitive.\r\n");
    		out.write("\r\n");
    		out.write("# a b c\r\n");
    		out.write("# d e f\r\n");
    		out.write("# g h i\r\n");
    		out.write("\r\n");
    		out.write("a=SAND\r\n");
    		out.write("b=STRING\r\n");
    		out.write("c=SAND\r\n");
    		out.write("\r\n");
    		out.write("d=STRING\r\n");
    		out.write("e=SAND\r\n");
    		out.write("f=STRING\r\n");
    		out.write("\r\n");
    		out.write("g=SAND\r\n");
    		out.write("h=STRING\r\n");
    		out.write("i=SAND\r\n");
    		out.close();
    	} catch (Exception e) {
    		// You somehow failed?
    	}
    }
}
