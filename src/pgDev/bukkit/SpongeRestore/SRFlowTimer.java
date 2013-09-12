package pgDev.bukkit.SpongeRestore;

import java.util.LinkedList;
import java.util.logging.Level;

/**
 * This is a thread that clears the deleted sponge area
 * database after the water has had a chance to
 * propagate.
 * @author Devil Boy
 *
 */
public class SRFlowTimer implements Runnable {
	private final SpongeRestore plugin;
	private LinkedList<String> removedCoords;
	int waittime;
	
	public SRFlowTimer(SpongeRestore plugin, LinkedList<String> removedCoords) {
		this.plugin = plugin;
		this.removedCoords = removedCoords;
		
		waittime = SpongeRestore.pluginSettings.spongeRadius * SpongeRestore.pluginSettings.flowTimeMult;
	}
	
	@Override
	public void run() {
		if (SpongeRestore.pluginSettings.debug) {
			SpongeRestore.logger.log(Level.INFO, "FlowTimer running!");
		}
		try {
			Thread.sleep(waittime);
		} catch (InterruptedException e) {
		}
		for (String currentCoord : removedCoords) {
			plugin.removeFromSpongeAreas(currentCoord);
		}
		if (SpongeRestore.pluginSettings.debug) {
			SpongeRestore.logger.log(Level.INFO, "Water is out of time!");
		}
		plugin.flowTimers.remove(this);
	}
}
