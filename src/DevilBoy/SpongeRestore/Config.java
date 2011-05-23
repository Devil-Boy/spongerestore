package DevilBoy.SpongeRestore;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class Config implements java.io.Serializable {
	private Properties properties;
	private final SpongeRestore plugin;
	
	// List of Config Options
	LinkedList<String> excludedWorlds = new LinkedList<String>();
	boolean spongeSaturation = false;
	boolean canPlaceWater = false;
	boolean debug = false;
	boolean craftableSponges = true;
	boolean absorbLava = false;
	
	public Config(Properties p, final SpongeRestore plugin) throws NoSuchElementException {
        properties = p;
        this.plugin = plugin;
        
        // Grab values here.
        excludedWorlds = getList("excludedWorlds", "");
        spongeSaturation = getBoolean("spongeSaturation", false);
        canPlaceWater = getBoolean("canPlaceWater", false);
        debug = getBoolean("debug", false);
        craftableSponges = getBoolean("craftableSponges", true);
        absorbLava = getBoolean("absorbLava", false);
        
    }
	
	public int getInt(String label) throws NoSuchElementException {
        String value = getString(label);
        return Integer.parseInt(value);
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
    
    public void createConfig() {
    	try{
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plugin.pluginConfigLocation)));
    		out.write("#\n");
    		out.write("# SpongeRestore Configuration\n");
    		out.write("#\n");
    		out.write("\n");
    		out.write("# Excluded worlds [names separated by commas]\n");
    		out.write("#	Here you list all the worlds in which you\n");
    		out.write("#	do not want this plugin to work in.\n");
    		out.write("excludedWorlds=" + linkedListToString(excludedWorlds) + "\n");
    		out.write("\n");
    		out.write("# Sponge saturation [true or false]\n");
    		out.write("#	Add more realism to sponges by making them only\n");
    		out.write("#	absorb water from an area without blocking\n");
    		out.write("#	water's flow afterwards.\n");
    		out.write("spongeSaturation=" + spongeSaturation + "\n");
    		out.write("\n");
    		out.write("# Water replacement\n");
    		out.write("#	Can a player place water near a sponge\n");
    		out.write("#	while it is still there?\n");
    		out.write("canPlaceWater=" + canPlaceWater + "\n");
    		out.write("\n");
    		out.write("# Craftable Sponges\n");
    		out.write("#	Choose whether players can craft sponges or not.\n");
    		out.write("craftableSponges=" + craftableSponges + "\n");
    		out.write("\n");
    		out.write("# Lava\n");
    		out.write("#	Should lava be affected also? Lava will be treated\n");
    		out.write("#	exactly like water as chosen in previous settings.\n");
    		out.write("absorbLava=" + absorbLava + "\n");
    		out.write("\n");
    		out.write("# Debug Messages\n");
    		out.write("#	This tends to spam your console, so you'd be best\n");
    		out.write("#	served leaving this off unless you know what\n");
    		out.write("#	you're doing.\n");
    		out.write("debug=" + debug);
    		out.close();
    	} catch (Exception e) {
    		// Not sure what to do? O.o
    	}
    }
}
