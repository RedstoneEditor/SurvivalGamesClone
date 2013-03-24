package tk.RedstoneEditor.SurvivalGames;

import org.bukkit.ChatColor;

public class PreTimer implements Runnable {

	private SurvivalGames plugin;

	public PreTimer(SurvivalGames instance) {
		this.plugin = instance;
	}

	public void run() {
		if (plugin.pre) {
			if (plugin.preTime >= 60 && plugin.preTime % 60 == 0) {
				plugin.chatManager.timeMsg = 
						ChatColor.GOLD + Integer.toString(plugin.preTime / 60)
								+ " minutes until game starts";
				plugin.log.info("Pre Timer Started...");
			} else if (plugin.preTime > 0 && plugin.preTime <= 60
					&& plugin.preTime % 10 == 0) {
				plugin.chatManager.timeMsg = 
						ChatColor.GOLD + Integer.toString(plugin.preTime)
								+ " seconds until game starts";
				plugin.log.info("Pre Timer Started...");
			} else if (plugin.preTime > 0 && plugin.preTime <= 10) {
				plugin.chatManager.timeMsg = 
						ChatColor.GOLD + Integer.toString(plugin.preTime)
								+ " seconds until game starts";
				plugin.log.info("Pre Timer Started...");
			} else if (plugin.preTime == 0) {
				plugin.start();
			}
			if (plugin.preTime > 0) {
				plugin.preTime--;
			}
		}

	}
}