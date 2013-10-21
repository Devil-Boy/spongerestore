package pgDev.bukkit.SpongeRestore;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

public class SpongeSaveTask implements Runnable {
	final SpongeRestore plugin;
	
	public SpongeSaveTask(SpongeRestore plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		try {
    		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(plugin.spongeDbLocation));
    		oos.writeObject(plugin.spongeAreas);
    		oos.flush();
    		oos.close();
    	} catch (Exception e) {
    		SpongeRestore.logger.log(Level.SEVERE, "Error occured while saving sponge database!", e);
    	}
	}
}
