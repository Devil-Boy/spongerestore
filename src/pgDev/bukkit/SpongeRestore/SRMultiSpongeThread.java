package pgDev.bukkit.SpongeRestore;

import java.util.LinkedList;

import org.bukkit.block.Block;

public class SRMultiSpongeThread implements Runnable {
	final SpongeRestore plugin;
	LinkedList<Block> enables;
	LinkedList<Block> disables;
	
	public SRMultiSpongeThread(LinkedList<Block> enableSponges, LinkedList<Block> disableSponges, final SpongeRestore pluginI) {
		plugin = pluginI;
		enables = enableSponges;
		disables = disableSponges;
	}
	
	public void run() {
		if (!enables.isEmpty()) {
			for (Block blk : enables) {
				plugin.enableSponge(blk);
			}
		}
		if (!disables.isEmpty()) {
			for (Block blk : disables) {
				plugin.disableSponge(blk);
			}
		}
	}

}
