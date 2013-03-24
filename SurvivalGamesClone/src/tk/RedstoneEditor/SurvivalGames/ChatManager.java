package tk.RedstoneEditor.SurvivalGames;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatManager implements Runnable {
	private SurvivalGames plugin;

	public ChatManager(SurvivalGames plugin) {
		this.plugin = plugin;
	}

	public String[] chat = { " ", " ", " ", " ", " " };
	private String line = ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH
			+ StringUtils.repeat(" ", 80);
	public String timeMsg = "";
	public void update() {
		Player[] players = Bukkit.getOnlinePlayers();
		for (Player p : players) {
			if (plugin.lobby) {
				p.sendMessage(clearChat());
				p.sendMessage("");
				p.sendMessage("");
				p.sendMessage("");
				p.sendMessage(timeMsg);
				p.sendMessage(line);
				p.sendMessage(getChat());
			} else if(plugin.pre){
				p.sendMessage(clearChat());
				p.sendMessage("");
				p.sendMessage("");
				p.sendMessage("");
				p.sendMessage(timeMsg);
				p.sendMessage(line);
				p.sendMessage(getChat());
			} else if (plugin.inGame) {
				p.sendMessage(clearChat());
				p.sendMessage("");
				p.sendMessage("");
				p.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.BLUE
						+ plugin.tracker.kills.get(p.getName())
						+ ChatColor.RED + " | " + ChatColor.YELLOW
						+ "Bounty: " + ChatColor.BLUE
						+ plugin.tracker.bounty.get(p.getName()));
				p.sendMessage(timeMsg);
				p.sendMessage(line);
				p.sendMessage(getChat());
			}
		}
	}

	public void sendChat(String s) {
		String[] split = new String[55];
		if (s.length() <= 55) {
			chat[4] = chat[3];
			chat[3] = chat[2];
			chat[2] = chat[1];
			chat[1] = chat[0];
			chat[0] = s;
		} else if (s.length() > 55) {
			split = plugin.split(s, 55);
			chat[4] = chat[2];
			chat[3] = chat[1];
			chat[2] = chat[0];
			chat[1] = split[0];
			chat[0] = split[1];
		} else if (s.length() > 55) {
			String[] split2 = plugin.split(split[1], 55);
			chat[4] = chat[1];
			chat[3] = chat[0];
			chat[2] = split[0];
			chat[1] = split2[0];
			chat[0] = split2[1];
		}
		update();
	}

	public void run() {
		update();
	}

	public String clearChat() {
		String m = "";
		for (int c = 0; c < 20; c++) {
			m = m + " \n ";
		}

		return m;
	}

	public String getChat() {
		String ret = "";
		ret += (chat[4] != null ? chat[4] + "\n" : "\n");
		ret += (chat[3] != null ? chat[3] + "\n" : "\n");
		ret += (chat[2] != null ? chat[2] + "\n" : "\n");
		ret += (chat[1] != null ? chat[1] + "\n" : "\n");
		ret += (chat[0] != null ? chat[0] + "\n" : " ");
		return ret;
	}

}
