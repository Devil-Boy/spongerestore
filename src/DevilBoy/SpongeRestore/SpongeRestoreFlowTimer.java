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
		
		waittime = plugin.pluginSettings.spongeRadius * plugin.pluginSettings.flowTimeMult;
	}
	
	@Override
	public void run() {
		if (plugin.pluginSettings.debug) {
			System.out.println("FlowTimer running!");
		}
		try {
			Thread.sleep(waittime);
		} catch (InterruptedException e) {
		}
		for (String currentCoord : removedCoords) {
			plugin.blockListener.removeFromSpongeAreas(currentCoord);
		}
		if (plugin.pluginSettings.debug) {
			System.out.println("Water is out of time!");
		}
		plugin.flowTimers.remove(this);
	}
}
