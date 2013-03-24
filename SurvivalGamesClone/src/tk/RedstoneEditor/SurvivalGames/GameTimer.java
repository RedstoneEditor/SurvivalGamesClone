package tk.RedstoneEditor.SurvivalGames;

import org.bukkit.ChatColor;

public class GameTimer implements Runnable{
	private SurvivalGames plugin;
	public GameTimer(SurvivalGames plugin) {
		this.plugin = plugin;
	}
	@Override
	public void run() {
		if (plugin.inGame) {
			if (plugin.gameTime >= 60 && plugin.gameTime % 60 == 0) {
				plugin.chatManager.timeMsg =
						ChatColor.GOLD + Integer.toString(plugin.gameTime / 60)
								+ " minutes left";
			} else if (plugin.gameTime > 0 && plugin.gameTime <= 60
					&& plugin.gameTime % 10 == 0) {
				plugin.chatManager.timeMsg =
						ChatColor.GOLD + Integer.toString(plugin.gameTime / 10)
								+ " seconds left";
			} else if (plugin.gameTime > 0 && plugin.gameTime <= 10) {
				plugin.chatManager.timeMsg =
						ChatColor.GOLD + Integer.toString(plugin.gameTime)
								+ " seconds left";
			}
			if (plugin.gameTime == 0 || !plugin.canStart) {
				plugin.stop();
			}
			if (plugin.gameTime > 0)
				plugin.gameTime--;
		}
		
	}

}
