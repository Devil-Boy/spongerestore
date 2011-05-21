package DevilBoy.SpongeRestore;

import java.awt.Color;
import java.io.File;
import java.util.*;

public class Config implements java.io.Serializable {
	private Properties properties;
	
	// List of Config Options
	LinkedList excludedWorlds;
	boolean spongeSaturation;
	
	public Config(Properties p) throws NoSuchElementException {
        properties = p;
        
        // Grab values here.
        excludedWorlds = getList("excludedWorlds");
        spongeSaturation = getBoolean("spongeSaturation");
        
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

    public boolean getBoolean(String label) {
        String value = getString(label);
        return Boolean.valueOf(value).booleanValue();
    }
    
    public Color getColor(String label) {
        String value = getString(label);
        Color color = Color.decode(value);
        return color;
    }
    
    public HashSet getSet(String label) {
        String values = getString(label);
        String[] tokens = values.split(",");
        HashSet set = new HashSet();
        for (int i = 0; i < tokens.length; i++) {
            set.add(tokens[i].trim().toLowerCase());
        }
        return set;
    }
    
    public LinkedList getList(String label) {
    	String values = getString(label);
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
}
