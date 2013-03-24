package tk.RedstoneEditor.SurvivalGames;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class SurvivalGames extends JavaPlugin implements Listener {
	int GameTimerID = 0;
	public Tracker tracker;
	ArrayList<String> players = new ArrayList<String>();
	ArrayList<String> alive = new ArrayList<String>();
	public MapManager mapManager;
	public boolean lobby = true;
	public boolean inGame = false;
	public Timer timer;
	public PreTimer preTimer;
	public BukkitScheduler scheduler;
	public PluginManager pluginManager;
	public Server server;
	Logger log;
	//test
	public int time = 120;
	public boolean canStart;
	public int TimerID;
	public int ChatID;
	public ChatManager chatManager;
	Connection conn;
	Statement stat;
	public boolean pre;
	public File pluginDirectory = new File("plugins" + File.separator
			+ "SurvivalGames");
	public File mapsDirectory = new File("plugins" + File.separator
			+ "SurvivalGames" + File.separator + "maps");
	public int gameTime = 120;
	public FileConfiguration config;
	private World world;
	private boolean worldLoaded;
	private boolean firstLoad;
	private String currentWorld;
	private int PreTimerID = 0;
	public int preTime = 10;
	private java.util.Timer restartTimer;

	@Override
	public void onDisable() {
		scheduler.cancelTasks(this);
	}

	@Override
	public void onEnable() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		config = getConfig();
		this.tracker = new Tracker();
		this.chatManager = new ChatManager(this);
		this.timer = new Timer(this);
		this.preTimer = new PreTimer(this);
		this.mapManager = new MapManager();
		this.server = getServer();
		this.scheduler = server.getScheduler();
		this.pluginManager = server.getPluginManager();
		registerHooks();
		this.log = getLogger();
		this.TimerID = scheduler
				.scheduleSyncRepeatingTask(this, timer, 0L, 20L);
		this.ChatID = scheduler.scheduleSyncRepeatingTask(this, chatManager,
				0L, 20L);
		try {
			this.conn = DriverManager
					.getConnection("jdbc:sqlite:plugins/SurvivalGames/SurvivalGames.db");
			stat = this.conn.createStatement();
			stat.execute("CREATE TABLE IF NOT EXISTS `players` (`name` text, `score` int(11));");
			stat.execute("CREATE TABLE IF NOT EXISTS `spawns` (`id` text, `x` double(11), `y` double(11), `z` double(11), `world` text);");
			stat.execute("CREATE TABLE IF NOT EXISTS `chests` (`id` text, `group` text, `x` int(11), `y` int(11), `z` int(11), `world` text);");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (!this.pluginDirectory.exists()) {
			this.pluginDirectory.mkdir();
		}
		if (!this.mapsDirectory.exists()) {
			this.mapsDirectory.mkdir();
		}
		mapManager.loadList();
		loadMap();
	}

	public void loadMap() {
		log.info(mapManager.getMapList().toString());
		if (mapManager.getMapList().size() > 0) {
			Random random = new Random();
			int r = random.nextInt(mapManager.getMapList().size());
			String map = (String) mapManager.getMapList().get(r);
			while (map == currentWorld) {
				r = random.nextInt(mapManager.getMapList().size());
				map = (String) mapManager.getMapList().get(r);
			}
			File mapFile = new File(map);
			File mapBackup = new File("plugins/SurvivalGames/backups/" + map);
			File playerDir = new File(map + "/players");
			File uid = new File(map + "/uid.dat");
			log.info(map);
			if (mapBackup.exists()) {
				if (mapFile.exists())
					mapFile.delete();
				try {
					FileUtils.copyDirectory(mapBackup, new File(map));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (playerDir.exists()) {
					try {
						FileUtils.deleteDirectory(playerDir);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (uid.exists())
					uid.delete();
			} else {
				log.warning("No back up found, map will load as is.");
			}
			if (mapManager.getMapList().size() > 1)
				mapManager.changeMap(map);
		} else {
			log.warning("No maps on the map cycle, not changing map.");
		}
	}

	public int getLines(String table, String column, String value) {
		int row = 0;
		try {
			ResultSet rSet = stat.executeQuery("SELECT * FROM `" + table
					+ "` WHERE `" + column + "`='" + value + "';");
			while (rSet.next()) {
				row++;
			}
			rSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return row;
	}

	public void spawnPlayers() {
		ArrayList<Location> spawns = new ArrayList<Location>();
		try {
			int lines = getLines("spawns", "world", currentWorld);
			if (lines > 0) {
				if (getAlivePlayers().size() > lines) {
					log.warning("More players than spawns! Some players will be teleported to the same spawn!");
				}
				ResultSet rs = stat
						.executeQuery("SELECT `x`,`y`,`z` FROM `spawns` WHERE `world`='"
								+ currentWorld + "';");
				while (rs.next()) {
					spawns.add(new Location(server.getWorld(currentWorld), rs
							.getDouble(1), rs.getDouble(2), rs.getDouble(3)));
				}
				int i = 0;
				int ii = players.size();
				for (int k = 0; i < ii; i++) {
					Player player = ((server.getPlayer(players.get(i))));
					if (k < lines)
						k++;
					else {
						k = 0;
					}
					player.teleport((Location) spawns.get(k));
					player.setHealth(player.getMaxHealth());
					player.setFoodLevel(20);
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
				}
				rs.close();
			} else {
				log.warning("No spawns added, leaving players where they are.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int j = 0;
		for (int jj = alive.size(); j < jj; j++)
			server.getPlayer(alive.get(j)).getInventory().clear();
	}

	private ArrayList<String> getAlivePlayers() {
		// TODO Auto-generated method stub
		return null;
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		event.setCancelled(true);
		String s = event.getPlayer().getName() + ": " + event.getMessage();
		if (event.getPlayer().hasPermission("sg.color")) {
			s = StringUtils.replace(s, "&0", "" + ChatColor.BLACK);
			s = StringUtils.replace(s, "&1", "" + ChatColor.DARK_BLUE);
			s = StringUtils.replace(s, "&2", "" + ChatColor.DARK_GREEN);
			s = StringUtils.replace(s, "&3", "" + ChatColor.DARK_AQUA);
			s = StringUtils.replace(s, "&4", "" + ChatColor.DARK_RED);
			s = StringUtils.replace(s, "&5", "" + ChatColor.DARK_PURPLE);
			s = StringUtils.replace(s, "&6", "" + ChatColor.GOLD);
			s = StringUtils.replace(s, "&7", "" + ChatColor.GRAY);
			s = StringUtils.replace(s, "&8", "" + ChatColor.DARK_GRAY);
			s = StringUtils.replace(s, "&9", "" + ChatColor.BLUE);
			s = StringUtils.replace(s, "&a", "" + ChatColor.GREEN);
			s = StringUtils.replace(s, "&b", "" + ChatColor.AQUA);
			s = StringUtils.replace(s, "&c", "" + ChatColor.RED);
			s = StringUtils.replace(s, "&d", "" + ChatColor.LIGHT_PURPLE);
			s = StringUtils.replace(s, "&e", "" + ChatColor.YELLOW);
			s = StringUtils.replace(s, "&f", "" + ChatColor.WHITE);
			s = StringUtils.replace(s, "&m", "" + ChatColor.STRIKETHROUGH);
			s = StringUtils.replace(s, "&l", "" + ChatColor.BOLD);
			s = StringUtils.replace(s, "&k", "" + ChatColor.MAGIC);
			s = StringUtils.replace(s, "&n", "" + ChatColor.UNDERLINE);
			s = StringUtils.replace(s, "&o", "" + ChatColor.ITALIC);
			s = StringUtils.replace(s, "&r", "" + ChatColor.RESET);
		}
		chatManager.sendChat(s);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		canStart = Bukkit.getOnlinePlayers().length >= 1;
		if (isPlayerNew(event.getPlayer().getName())) {
			try {
				stat.execute("INSERT INTO `players` VALUES ('"
						+ event.getPlayer().getName() + "', 0);");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} else {
			try {
				ResultSet rs = stat
						.executeQuery("SELECT `score` FROM `players` WHERE `name`='"
								+ event.getPlayer().getName() + "';");
				tracker.bounty.put(event.getPlayer().getName(), rs.getInt(1));
				rs.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		players.add(event.getPlayer().getName());
		tracker.kills.put(event.getPlayer().getName(), 0);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		canStart = Bukkit.getOnlinePlayers().length >= 2 - 1;
		try {
			stat.execute("UPDATE `players` SET `score`='"
					+ tracker.bounty.get(event.getPlayer().getName())
					+ "' WHERE `name`='" + event.getPlayer().getName() + "';");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		players.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		if (((e.getWorld().getName().endsWith("_the_end") ? 0 : 1) & (e
				.getWorld().getName().endsWith("_nether") ? 0 : 1)) != 0) {
			world = e.getWorld();
			this.worldLoaded = true;
			this.firstLoad = false;
			currentWorld = world.getName();
		}
	}

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event) {
		if (inGame)
			if (event.getEntityType() == EntityType.PLAYER) {
				Player p = (Player) event.getEntity();
				chatManager.sendChat(ChatColor.YELLOW + p.getName()
						+ ChatColor.BLUE + " was killed by " + ChatColor.YELLOW
						+ p.getKiller().getName());
				tracker.addKill(p.getKiller().getName());
				tracker.addBounty(p.getKiller().getName(), 5);
				tracker.subBounty(p.getName(), 5);
			}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!inGame && !lobby) {
			event.getPlayer().teleport(event.getPlayer());
		}
	}

	public void registerHooks() {
		pluginManager.registerEvents(this, this);
	}

	public String[] split(String src, int len) {
		String[] result = new String[(int) Math.ceil((double) src.length()
				/ (double) len)];
		for (int i = 0; i < result.length; i++)
			result[i] = src.substring(i * len,
					Math.min(src.length(), (i + 1) * len));
		return result;
	}

	public void start() {
		pre = false;
		inGame = true;
		chatManager.timeMsg = (ChatColor.GOLD + "Starting Game...");
		scheduler.cancelTask(PreTimerID);
		GameTimerID = scheduler.scheduleSyncRepeatingTask(this, new GameTimer(
				this), 0L, 20L);
	}

	public void stop() {
		inGame = false;
		lobby = true;
		chatManager.timeMsg = ChatColor.GOLD + "GAME OVER!";
		scheduler.cancelTask(GameTimerID);
		restartServer();
	}

	public void restartServer() {
		server.broadcastMessage(ChatColor.RED + "Server restarting!");
		restartTimer = new java.util.Timer();
		restartTimer.schedule(new TimerTask() {
			public void run() {
				while (alive.size() > 0) {
					server.getPlayer(alive.get(0)).kickPlayer(
							"Server restarting.");
				}
				server.dispatchCommand(server.getConsoleSender(), "save-all");
				server.dispatchCommand(server.getConsoleSender(), "stop");
			}
		}, 10 * 1000L);
	}

	public boolean isPlayerNew(String name) {
		try {
			ResultSet rs = stat
					.executeQuery("SELECT * FROM `players` WHERE `name`='"
							+ name + "';");
			if (!rs.next()) {
				return true;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void initGames() {
		alive = players;
		players.clear();
		lobby = false;
		pre = true;
		scheduler.cancelTask(TimerID);
		spawnPlayers();
		PreTimerID = scheduler.scheduleSyncRepeatingTask(this, preTimer, 0L,
				20L);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (args[0].equalsIgnoreCase("addspawn")) {
			if (args.length == 2) {
				if (sender instanceof Player) {
					if (sender instanceof Player) {
						double x = ((Player) sender).getLocation().getX();
						double y = ((Player) sender).getLocation().getY();
						double z = ((Player) sender).getLocation().getZ();
						if (((isInDatabase(
								new String[] { "x", "y", "z" },
								"spawns",
								new String[] { Double.toString(x),
										Double.toString(y), Double.toString(z) }) ? 0
								: 1) & (sharesValue("spawns", "id", "world",
								args[1], currentWorld) ? 0 : 1)) != 0) {
							try {
								stat.execute("INSERT INTO `spawns` VALUES ('"
										+ args[1] + "', " + x + ", " + y + ", "
										+ z + ", '" + currentWorld + "');");
							} catch (SQLException e) {
								e.printStackTrace();
							}
							sender.sendMessage(ChatColor.RED + "Spawn '" + args[1]
									+ "' added.");
						} else {
							((Player) sender).sendMessage(ChatColor.RED
									+ "That spawn is already registered!");
						}
					} else {
						((Player) sender).sendMessage(ChatColor.RED
								+ "You do not have permission to use this command.");
					}
				} else
					log.warning("You must be in the game to use this command!");
			} else {
				sender.sendMessage("Usage: /lsgm addspawn <id>");
			}
		}
		return false;
	}

	public boolean isInDatabase(String[] columns, String table,
			Object[] searchItem) {
		ArrayList<Object> in = new ArrayList<Object>();
		int i = 0;
		for (int ii = searchItem.length; i < ii; i++) {
			try {
				ResultSet rs = stat.executeQuery("SELECT * FROM `" + table
						+ "` WHERE `" + columns[i] + "`='" + searchItem[i]
						+ "';");
				if (rs.next()) {
					in.add(searchItem[i]);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (in.size() == searchItem.length) {
			return true;
		}
		return false;
	}
	
	public boolean sharesValue(String table, String column1, String column2,
			String value1, String value2) {
		try {
			ResultSet rs = stat.executeQuery("SELECT * FROM `" + table
					+ "` WHERE `" + column1 + "`='" + value1 + "' AND `"
					+ column2 + "`='" + value2 + "';");
			if (rs.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
