package DevilBoy.SpongeRestore;

import java.util.LinkedList;

/**
 * This is a thread that clears the deleted sponge area
 * database after the water has had a chance to
 * propagate.
 * @author Devil Boy
 *
 */
public class SpongeRestoreFlowTimer implements Runnable {
	private final SpongeRestore plugin;
	private LinkedList<String> removedCoords;
	int waittime;
	
	public SpongeRestoreFlowTimer(SpongeRestore plugin, LinkedList<String> removedCoords) {
		this.plugin = plugin;
		this.removedCoords = removedCoords;
		
		waittime = plugin.pluginSettings.spongeRadius * 2000;
	}
	
	@Override
	public void run() {
		try {
			wait(waittime);
		} catch (InterruptedException e) {
		}
		for (int i=0; i<removedCoords.size(); i++) {
			plugin.blockListener.removeFromSpongeAreas(removedCoords.get(i));
		}
	}
}
