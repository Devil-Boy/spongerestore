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
	LinkedList excludedWorlds;
	boolean spongeSaturation;
	boolean canPlaceWater;
	
	public Config(Properties p, final SpongeRestore plugin) throws NoSuchElementException {
        properties = p;
        this.plugin = plugin;
        
        // Grab values here.
        excludedWorlds = getList("excludedWorlds", "none");
        spongeSaturation = getBoolean("spongeSaturation", false);
        canPlaceWater = getBoolean("canPlaceWater", false);
        
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
    
    public HashSet getSet(String label, String thedefault) {
        String values;
        try {
        	values = getString(label);
        }catch (NoSuchElementException e) {
        	values = thedefault;
        }
        String[] tokens = values.split(",");
        HashSet set = new HashSet();
        for (int i = 0; i < tokens.length; i++) {
            set.add(tokens[i].trim().toLowerCase());
        }
        return set;
    }
    
    public LinkedList getList(String label, String thedefault) {
    	String values;
        try {
        	values = getString(label);
        }catch (NoSuchElementException e) {
        	values = thedefault;
        }
        if(plugin.debug) {
        	System.out.println("List from file: " + values);
        }
        String[] tokens = values.split(",");
        LinkedList set = new LinkedList();
        for (int i = 0; i < tokens.length; i++) {
            set.add(tokens[i].trim().toLowerCase());
        }
        return set;
    }
    
    public String getString(String label) throws NoSuchElementException {
        String value = properties.getProperty(label);
        if (value == null) {
            throw new NoSuchElementException("Config did not contain: " + label);
        }
        return value;
    }
    
    public void createConfig() {
    	try{
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plugin.pluginConfigLocation)));
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
    		out.newLine();
    		out.write("");
    		out.newLine();
    		out.write("# Water replacement");
    		out.newLine();
    		out.write("#	Can a player place water near a sponge");
    		out.newLine();
    		out.write("#	while it is still there?");
    		out.newLine();
    		out.write("canPlaceWater=false");
    		out.close();
    	} catch (Exception e) {
    		// Not sure what to do? O.o
    	}
    }
}
