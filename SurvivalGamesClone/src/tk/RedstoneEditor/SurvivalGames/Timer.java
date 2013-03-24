package tk.RedstoneEditor.SurvivalGames;

import org.bukkit.ChatColor;

public class Timer implements Runnable {

	private SurvivalGames plugin;

	public Timer(SurvivalGames instance) {
		this.plugin = instance;
	}

	public void run() {
		if (plugin.lobby) {
			if (plugin.time >= 60 && plugin.time % 60 == 0) {
				plugin.chatManager.timeMsg = 
						ChatColor.GOLD + Integer.toString(plugin.time / 60)
								+ " minutes until pre-game";
			} else if (plugin.time > 0 && plugin.time <= 60
					&& plugin.time % 10 == 0) {
				plugin.chatManager.timeMsg = 
						ChatColor.GOLD + Integer.toString(plugin.time)
								+ " seconds until  pre-game";
			} else if (plugin.time > 0 && plugin.time <= 10) {
				plugin.chatManager.timeMsg = 
						ChatColor.GOLD + Integer.toString(plugin.time)
								+ " seconds until  pre-game";
			} else if (plugin.time == 0) {
				plugin.initGames();
			}
			if (plugin.time > 0) {
				plugin.time--;
			}
		}

	}
}