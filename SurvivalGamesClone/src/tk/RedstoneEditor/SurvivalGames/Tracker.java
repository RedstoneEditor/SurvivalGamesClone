package tk.RedstoneEditor.SurvivalGames;

import java.util.HashMap;

public class Tracker {
	public HashMap<String, Integer> kills = new HashMap<String, Integer>();
	public HashMap<String, Integer> bounty = new HashMap<String, Integer>();

	public void addKill(String name) {
		int before = 0;
		if (kills.containsKey(name)) {
			before = kills.get(name);
			kills.remove(name);
		}
		kills.put(name, before + 1);
	}
	
	public void addBounty(String name, int amount) {
		int before = 0;
		if (bounty.containsKey(name)) {
			before = bounty.get(name);
			bounty.remove(name);
		}
		bounty.put(name, before + amount);
	}
	
	public void subBounty(String name, int amount) {
		int before = 0;
		if (bounty.containsKey(name)) {
			before = bounty.get(name);
			bounty.remove(name);
		}
		bounty.put(name, before - amount);
	}

}
