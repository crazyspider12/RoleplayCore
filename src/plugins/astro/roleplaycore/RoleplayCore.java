package plugins.astro.roleplaycore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginAwareness;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.chat.Chat;

public class RoleplayCore extends JavaPlugin implements Listener {
	public static RoleplayCore instance;
	public static Logger logger = Logger.getLogger("Minecraft");
	public static List<Channel> channels = new ArrayList<Channel>();
	public static List<Channel> autojoinChannels = new ArrayList<Channel>();
	public static List<Race> races = new ArrayList<Race>();

	public File messagesFile = new File(getDataFolder() + "/messages.yml");
	public FileConfiguration messages;

	public static Pattern boldPattern = Pattern.compile("(?i)&L");
	public static Pattern italicsPattern = Pattern.compile("(?i)&O");
	public static Pattern strikethroughPattern = Pattern.compile("(?i)&M");
	public static Pattern underlinePattern = Pattern.compile("(?i)&N");
	public static Pattern randomPattern = Pattern.compile("(?i)&K");
	public static Pattern colorPattern = Pattern.compile("(?i)&[0-9A-F]");

	public static List<Cooldown> activeCooldowns = new ArrayList<Cooldown>();
	public static List<Player> countdowns = new ArrayList<Player>();

	private static String nmsver;
	private static boolean useOldMethods = false;

	private static Chat chat = null;

	private static HashMap<Player, List<String>> chatContinuations = new HashMap<Player, List<String>>();

	String url = "jdbc:sqlite:" + new File(getDataFolder(), "storage.db").getAbsolutePath();
	static Connection connection = null;

	public void onEnable() {
		getDataFolder().mkdirs();

		saveDefaultConfig();

		getMessages().options().copyDefaults(true);
		saveDefaultMessages();

		instance = this;
		for(String channel : getConfig().getConfigurationSection("channels").getKeys(false)) {
			Channel newChannel = new Channel(channel, getConfig().getBoolean("channels." + channel + ".auto-join"), getConfig().getString("channels." + channel + ".leave-perm"), getConfig().getString("channels." + channel + ".join-perm"), getConfig().getString("channels." + channel + ".view-perm"), getConfig().getString("channels." + channel + ".talk-perm"), strip(null, getConfig().getString("channels." + channel + ".nickname")), getConfig().getStringList("channels." + channel + ".aliases"), getConfig().getString("channels." + channel + ".nickname"), getConfig().getString("channels." + channel + ".channel-prefix"), getConfig().getString("channels." + channel + ".chat-format"), getConfig().getString("channels." + channel + ".emote-prefix"), getConfig().getInt("channels." + channel + ".range"), getConfig().getBoolean("channels." + channel + ".rank-prefixes"), getConfig().getBoolean("channels." + channel + ".emotes"), getConfig().getBoolean("channels." + channel + ".quotation-emotes"), getConfig().getInt("channels." + channel + ".cooldown"));
			channels.add(newChannel);
			if(newChannel.isAutoJoin()) {
				autojoinChannels.add(newChannel);
			}
		}

		for(String race : getConfig().getConfigurationSection("races").getKeys(false)) {
			Race newRace = new Race(race, getConfig().getInt("races." + race + ".maxAge"), getConfig().getString("races." + race + ".color"));
			races.add(newRace);
		}

		getCommand("card").setExecutor(new CardCommand(this));
		getCommand("join").setExecutor(new JoinChannelCommand(this));
		getCommand("leave").setExecutor(new LeaveChannelCommand(this));
		getCommand("channels").setExecutor(new ChannelsCommand(this));
		getCommand("races").setExecutor(new RacesCommand(this));
		getCommand("bird").setExecutor(new BirdCommand(this));
		getCommand("roll").setExecutor(new RollCommand(this));
		getCommand("countdown").setExecutor(new CountdownCommand(this));
		getCommand("emote").setExecutor(new EmoteCommand(this));
		getCommand("channel").setExecutor(new ChannelCommand(this));
		getCommand("character").setExecutor(new CharacterCommand(this));
		getCommand("roleplaycore").setExecutor(new RoleplayCoreCommand(this));

		for(Player p : Bukkit.getOnlinePlayers()) {
			AttributeInstance attribute = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			attribute.setBaseValue(getConfig().getDouble("playerHealth"));
		}

		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection(url);
		} catch(Exception e) {
			e.printStackTrace();
			logger.severe("No JDBC driver detected, could not establish connection to the SQL database! Shutting down.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		nmsver = Bukkit.getServer().getClass().getPackage().getName();
		nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

		if(nmsver.equalsIgnoreCase("v1_8_R1") || nmsver.startsWith("v1_7_")) {
			useOldMethods = true;
		}

		queryDB("CREATE TABLE IF NOT EXISTS EmoteData(Player VARCHAR(40), Color TEXT);");
		queryDB("CREATE TABLE IF NOT EXISTS ChannelData(Player VARCHAR(40), CurrentChannel TEXT, JoinedChannels TEXT);");
		queryDB("CREATE TABLE IF NOT EXISTS Cards(Player VARCHAR(40), CharacterName TEXT, Age INTEGER, Gender TEXT, Race TEXT, Description TEXT);");

		// Migrations
		try {
			ResultSet rs = queryDB("PRAGMA user_version;");
			int pragmaVersion = rs.getInt(1);
			if(pragmaVersion == 0) {
				queryDB("ALTER TABLE ChannelData ADD COLUMN BannedChannels TEXT;");
				queryDB("PRAGMA user_version = 1;");
				pragmaVersion = 1;
			}

			if(pragmaVersion == 1) {
				queryDB("ALTER TABLE EmoteData ADD COLUMN QuotationEmotes TEXT;");
				queryDB("PRAGMA user_version = 2;");
				pragmaVersion = 2;
			}
			
			if(pragmaVersion == 2) {
				queryDB("ALTER TABLE Cards ADD COLUMN CreationOrder INTEGER;");
				queryDB("UPDATE Cards SET CreationOrder = 1;");
				queryDB("ALTER TABLE Cards ADD COLUMN Contents TEXT;");
				queryDB("ALTER TABLE Cards ADD COLUMN ArmorContents TEXT;");
				queryDB("ALTER TABLE Cards ADD COLUMN Active INTEGER;");
				queryDB("UPDATE Cards SET Active = 1;");
				queryDB("PRAGMA user_version = 3;");
				pragmaVersion = 3;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				String currentChannel = null;
				List<Cooldown> toRemove = new ArrayList<Cooldown>();
				for(Cooldown c : activeCooldowns) {
					if(currentChannel == null) {
						try {
							ResultSet rs = queryDB("SELECT CurrentChannel FROM ChannelData WHERE Player='" + c.getPlayer().getUniqueId().toString() + "';");
							if(rs.next()) {
								currentChannel = rs.getString("CurrentChannel");
							}
						} catch(SQLException e) {
							e.printStackTrace();
						}
					}
					c.decrementSeconds();
					if(c.getSecondsLeft() <= 0) {
						if(currentChannel != null && currentChannel.equalsIgnoreCase(c.getChannel().getNickname())) {
							sendActionBar(c.getPlayer(), "");
						}
						toRemove.add(c);
					} else {
						if(currentChannel != null && currentChannel.equalsIgnoreCase(c.getChannel().getNickname())) {
							sendActionBar(c.getPlayer(), ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.generic.cooldown-left").replaceAll("%channel%", c.getChannel().getNickname()).replaceAll("%secondsLeft%", "" + c.getSecondsLeft()).replaceAll("%s%", c.getSecondsLeft() != 1 ? "s" : "")));
						}
					}
				}
				activeCooldowns.removeAll(toRemove);
			}
		}, 0, 20);

		Bukkit.getPluginManager().registerEvents(this, this);
		setupChat();
	}

	public void onDisable() {
		super.onDisable();
		try {
			connection.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static RoleplayCore getInstance() {
		return instance;
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
		chat = rsp.getProvider();
		return chat != null;
	}

	public static Chat getChat() {
		return chat;
	}

	public static ResultSet queryDB(final String query) {
		try {
			PreparedStatement statement = connection.prepareStatement(query);
			if(statement.execute()) {
				return statement.getResultSet();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
		for(Channel channel : channels) {
			List<String> aliases = channel.getAliases();
			if(e.getMessage().startsWith("/")) {
				String cmd = e.getMessage().toLowerCase().substring(1);
				if(cmd.contains(" ")) {
					cmd = cmd.split(" ", 2)[0];
				}
				if(cmd.equalsIgnoreCase(channel.getChannelName()) || cmd.equalsIgnoreCase(channel.getNickname()) || aliases.contains(cmd)) {
					e.setCancelled(true);
					try {
						ResultSet rs = queryDB("SELECT JoinedChannels,BannedChannels FROM ChannelData WHERE Player='" + e.getPlayer().getUniqueId().toString() + "';");
						if(rs.next()) {
							String bannedChannels = rs.getString("BannedChannels");
							String joinedChannels = rs.getString("JoinedChannels");
							if(bannedChannels != null && Arrays.asList(bannedChannels.split("\\|")).contains(channel.getNickname())) {
								e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.chat.currently-banned").replaceAll("%channel%", channel.getNickname())));
								return;
							}
							if(joinedChannels != null && Arrays.asList(joinedChannels.split("\\|")).contains(channel.getNickname())) {
								if(e.getMessage().contains(" ")) {
									sendMessageInChannel(e.getPlayer(), channel.getFormattedMessage(e.getPlayer(), e.getMessage().split(" ", 2)[1]), channel);
								} else if(e.getPlayer().hasPermission(channel.getJoinPermission())) {
									String result = joinChannel(e.getPlayer(), channel);
									if(result.equalsIgnoreCase("Joined")) {
										e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.join.joined" + (channel.getRange() > 0 ? "-range" : "-global")).replaceAll("%nickname%", channel.getInlineFormat()).replaceAll("%range%", "" + channel.getRange())));
									} else if(result.equalsIgnoreCase("Talking")) {
										e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.join.talking-in" + (channel.getRange() > 0 ? "-range" : "-global")).replaceAll("%nickname%", channel.getInlineFormat()).replaceAll("%range%", "" + channel.getRange())));
									}
								} else {
									e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.join.no-permission")));
								}
							} else {
								e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.join.not-in-channel").replaceAll("%nickname%", channel.getNickname())));
							}
						}
					} catch(SQLException ex) {
						ex.printStackTrace();
					}
					break;
				}
			}
		}
	}

	public static Channel getChannelByName(String name) {
		for(Channel channel : channels) {
			if(channel.getNickname().equalsIgnoreCase(name)) {
				return channel;
			}
		}
		return null;
	}

	public static Race getRaceByName(String name) {
		for(Race race : races) {
			if(race.getName().equalsIgnoreCase(name)) {
				return race;
			}
		}
		return null;
	}

	public void sendMessageInChannel(Player from, JsonMessage message, Channel channel) {
		if(!from.hasPermission(channel.getTalkPermission())) {
			from.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.generic.no-permission-to-talk").replaceAll("%channel%", channel.getNickname())));
			return;
		}
		if(channel.getCooldown() > 0 && !from.hasPermission("roleplaycore." + channel.getNickname() + ".cooldown-bypass")) {
			for(Cooldown c : activeCooldowns) {
				if(c.getPlayer() == from && c.getChannel() == channel) {
					from.sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.generic.active-cooldown").replaceAll("%channel%", channel.getNickname()).replaceAll("%secondsLeft%", "" + c.getSecondsLeft()).replaceAll("%s%", c.getSecondsLeft() != 1 ? "s" : "")));
					return;
				}
			}
			try {
				ResultSet rs = queryDB("SELECT CurrentChannel FROM ChannelData WHERE Player='" + from.getUniqueId().toString() + "';");
				if(rs.next()) {
					if(rs.getString("CurrentChannel").equalsIgnoreCase(channel.getNickname())) {
						sendActionBar(from, ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.generic.cooldown-left").replaceAll("%channel%", channel.getNickname()).replaceAll("%secondsLeft%", "" + channel.getCooldown()).replaceAll("%s%", channel.getCooldown() != 1 ? "s" : "")));
					}
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}

			activeCooldowns.add(new Cooldown(from, channel));
		}

		try {
			List<Player> nearby = null;
			if(channel.getRange() > 0) {
				List<Player> nearbyPlayers = new ArrayList<Player>();
				for(Entity e : from.getNearbyEntities(channel.getRange(), channel.getRange(), channel.getRange())) {
					if(e instanceof Player) {
						nearbyPlayers.add((Player) e);
					}
				}
				nearbyPlayers.add(from);
				nearby = nearbyPlayers;
			}
			List<Player> recipients = new ArrayList<Player>();
			ResultSet rs = queryDB("SELECT Player,JoinedChannels FROM ChannelData WHERE JoinedChannels LIKE '%" + channel.getNickname() + "%';");
			while(rs.next()) {
				if(Arrays.asList(rs.getString("JoinedChannels").split("\\|")).contains(channel.getNickname())) // Just making 100% sure.
				{
					OfflinePlayer potentialRecipient = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("Player")));
					if(potentialRecipient.isOnline() && (nearby == null || (nearby.contains(potentialRecipient)))) {
						recipients.add((Player) potentialRecipient);
					}
				}
			}

			for(Player p : Bukkit.getOnlinePlayers()) {
				if(!recipients.contains(p) && p.hasPermission(channel.getViewPermission()) && (nearby == null || (nearby.contains(p)))) {
					recipients.add(p);
				}
			}

			for(Player recipient : recipients) {
				message.send(recipient);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public String joinChannel(OfflinePlayer op, Channel channel, boolean forced, boolean setCurrent) {
		if(forced) {
			setCurrent = true;
		}
		try {
			ResultSet rs = queryDB("SELECT Player,JoinedChannels,BannedChannels FROM ChannelData WHERE Player='" + op.getUniqueId().toString() + "';");
			if(rs.next()) {
				String bannedChannels = rs.getString("BannedChannels");
				if(bannedChannels != null && Arrays.asList(bannedChannels.split("\\|")).contains(channel.getNickname())) {
					if(forced) {
						unbanFromChannel(op, channel);
					} else {
						if(op.isOnline()) {
							((Player) op).sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.chat.currently-banned").replaceAll("%channel%", channel.getNickname())));
						}
						return "Banned";
					}
				}
				String joinedChannels = rs.getString("JoinedChannels");
				if(joinedChannels != null && Arrays.asList(joinedChannels.split("\\|")).contains(channel.getNickname())) {
					queryDB("UPDATE ChannelData SET CurrentChannel='" + channel.getNickname() + "' WHERE Player='" + op.getUniqueId().toString() + "';");
					if(op.isOnline()) {
						sendActionBar((Player) op, "");
					}
					for(Cooldown c : activeCooldowns) {
						if(c.getChannel() == channel && op.getUniqueId().toString().equals(c.getPlayer().getUniqueId().toString())) {
							if(op.isOnline()) {
								sendActionBar((Player) op, ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.generic.cooldown-left").replaceAll("%channel%", channel.getNickname()).replaceAll("%secondsLeft%", "" + c.getSecondsLeft()).replaceAll("%s%", c.getSecondsLeft() != 1 ? "s" : "")));
							}
						}
					}
					return "Talking";
				} else {
					queryDB("UPDATE ChannelData SET JoinedChannels='" + joinedChannels + "|" + channel.getNickname() + "'" + (setCurrent ? (",CurrentChannel='" + channel.getNickname() + "'") : ("")) + " WHERE Player='" + op.getUniqueId().toString() + "';");
					return "Joined";
				}
			} else {
				queryDB("INSERT INTO ChannelData(Player," + (setCurrent ? "CurrentChannel," : "") + "JoinedChannels) VALUES('" + op.getUniqueId().toString() + "', " + (setCurrent ? "'" + channel.getNickname() + "', " : "") + "'" + channel.getNickname() + "');");
				if(op.isOnline()) {
					sendActionBar((Player) op, "");
				}
				for(Cooldown c : activeCooldowns) {
					if(c.getChannel() == channel && op.getUniqueId().toString().equals(c.getPlayer().getUniqueId().toString())) {
						if(op.isOnline()) {
							sendActionBar((Player) op, ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.generic.cooldown-left").replaceAll("%channel%", channel.getNickname()).replaceAll("%secondsLeft%", "" + c.getSecondsLeft()).replaceAll("%s%", c.getSecondsLeft() != 1 ? "s" : "")));
						}
					}
				}
				return "Talking";
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return "Nothing";
	}

	public String joinChannel(OfflinePlayer op, Channel channel) {
		return joinChannel(op, channel, false, true);
	}

	public boolean leaveChannel(OfflinePlayer op, Channel channel, String successMsg, boolean kicked) {
		try {
			ResultSet rs = queryDB("SELECT * FROM ChannelData WHERE Player='" + op.getUniqueId().toString() + "';");
			if(rs.next()) {
				List<String> joinedChannels = new ArrayList<String>();
				joinedChannels.addAll(Arrays.asList(rs.getString("JoinedChannels").split("\\|")));
				if(joinedChannels.contains(channel.getNickname())) {
					joinedChannels.remove(channel.getNickname());
					String newJoinedChannels = "";
					for(String joinedChannel : joinedChannels) {
						newJoinedChannels += joinedChannel + "|";
					}
					if(joinedChannels.size() > 0) {
						newJoinedChannels = newJoinedChannels.substring(0, newJoinedChannels.length() - 1);
					}
					queryDB("UPDATE ChannelData SET JoinedChannels='" + newJoinedChannels + "' WHERE Player='" + op.getUniqueId().toString() + "';");
					if(op.isOnline()) {
						((Player) op).sendMessage(successMsg);
					}
					if(rs.getString("CurrentChannel").equalsIgnoreCase(channel.getNickname())) {
						queryDB("UPDATE ChannelData SET CurrentChannel='" + (joinedChannels.size() > 0 ? getChannelByName(joinedChannels.get(joinedChannels.size() - 1)).getNickname() : "") + "' WHERE Player='" + op.getUniqueId().toString() + "';");
						if(joinedChannels.size() > 0) {
							Channel newChannel = getChannelByName(joinedChannels.get(joinedChannels.size() - 1));
							if(op.isOnline()) {
								((Player) op).sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString((kicked ? "messages.chat.placed-into-kick" : "messages.leave.placed-into-leave") + (newChannel.getRange() > 0 ? "-range" : "-global")).replaceAll("%oldChannel%", channel.getNickname()).replaceAll("%newChannel%", newChannel.getInlineFormat()).replaceAll("%range%", "" + newChannel.getRange())));
								sendActionBar((Player) op, "");
							}
							for(Cooldown c : activeCooldowns) {
								if(c.getChannel() == newChannel && op.getUniqueId().toString().equals(c.getPlayer().getUniqueId().toString())) {
									if(op.isOnline()) {
										sendActionBar((Player) op, ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.generic.cooldown-left").replaceAll("%channel%", newChannel.getNickname()).replaceAll("%secondsLeft%", "" + c.getSecondsLeft()).replaceAll("%s%", c.getSecondsLeft() != 1 ? "s" : "")));
									}
								}
							}
						}
					}
					return true;
				} else {
					if(op.isOnline()) {
						if(kicked) {
							return false;
						} else {
							((Player) op).sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.leave.not-in-channel")));
						}
					}
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean unbanFromChannel(OfflinePlayer op, Channel channel) {
		try {
			ResultSet rs = queryDB("SELECT * FROM ChannelData WHERE Player='" + op.getUniqueId().toString() + "';");
			if(rs.next()) {
				List<String> bannedChannels = new ArrayList<String>();
				bannedChannels.addAll(Arrays.asList(rs.getString("BannedChannels").split("\\|")));
				if(!bannedChannels.contains(channel.getNickname())) {
					return false;
				}
				bannedChannels.remove(channel.getNickname());
				String newBannedChannels = "";
				for(String bannedChannel : bannedChannels) {
					newBannedChannels += bannedChannel + "|";
				}
				if(bannedChannels.size() > 0) {
					newBannedChannels = newBannedChannels.substring(0, newBannedChannels.length() - 1);
				}
				queryDB("UPDATE ChannelData SET BannedChannels='" + newBannedChannels + "' WHERE Player='" + op.getUniqueId().toString() + "';");
				if(op.isOnline()) {
					((Player) op).sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.chat.unbanned").replaceAll("%channel%", channel.getNickname())));
				}
				return true;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean banFromChannel(OfflinePlayer op, Channel channel) {
		try {
			ResultSet rs = queryDB("SELECT * FROM ChannelData WHERE Player='" + op.getUniqueId().toString() + "';");
			if(rs.next()) {
				List<String> joinedChannels = new ArrayList<String>();
				List<String> bannedChannels = new ArrayList<String>();
				String joinedChannelsString = rs.getString("JoinedChannels");
				if(joinedChannelsString != null) {
					joinedChannels.addAll(Arrays.asList(joinedChannelsString.split("\\|")));
				}
				String bannedChannelsString = rs.getString("BannedChannels");
				if(bannedChannelsString != null) {
					bannedChannels.addAll(Arrays.asList(bannedChannelsString.split("\\|")));
				}
				if(bannedChannels.contains(channel.getNickname())) {
					return false;
				}
				bannedChannels.add(channel.getNickname());
				String newBannedChannels = "";
				for(String bannedChannel : bannedChannels) {
					newBannedChannels += bannedChannel + "|";
				}
				if(bannedChannels.size() > 0) {
					newBannedChannels = newBannedChannels.substring(0, newBannedChannels.length() - 1);
				}
				queryDB("UPDATE ChannelData SET BannedChannels='" + newBannedChannels + "' WHERE Player='" + op.getUniqueId().toString() + "';");
				if(joinedChannels.contains(channel.getNickname())) {
					joinedChannels.remove(channel.getNickname());
					String newJoinedChannels = "";
					for(String joinedChannel : joinedChannels) {
						newJoinedChannels += joinedChannel + "|";
					}
					if(joinedChannels.size() > 0) {
						newJoinedChannels = newJoinedChannels.substring(0, newJoinedChannels.length() - 1);
					}
					queryDB("UPDATE ChannelData SET JoinedChannels='" + newJoinedChannels + "' WHERE Player='" + op.getUniqueId().toString() + "';");
					if(rs.getString("CurrentChannel").equalsIgnoreCase(channel.getNickname())) {
						queryDB("UPDATE ChannelData SET CurrentChannel='" + (joinedChannels.size() > 0 ? getChannelByName(joinedChannels.get(joinedChannels.size() - 1)).getNickname() : "") + "' WHERE Player='" + op.getUniqueId().toString() + "';");
						if(op.isOnline()) {
							((Player) op).sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.chat.forcibly-talking-in" + (getChannelByName(joinedChannels.get(joinedChannels.size() - 1)).getRange() > 0 ? "-range" : "-global")).replaceAll("%nickname%", getChannelByName(joinedChannels.get(joinedChannels.size() - 1)).getInlineFormat()).replaceAll("%range%", "" + getChannelByName(joinedChannels.get(joinedChannels.size() - 1)).getRange())));
						}
					}
				}
			} else {
				queryDB("INSERT INTO ChannelData(Player, BannedChannels) VALUES('" + op.getUniqueId().toString() + "', '" + channel.getNickname() + "');");
			}
			if(op.isOnline()) {
				((Player) op).sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.chat.banned").replaceAll("%channel%", channel.getNickname())));
			}
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		AttributeInstance attribute = e.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
		attribute.setBaseValue(getConfig().getDouble("playerHealth"));

		if(autojoinChannels.size() >= 1) {
			try {
				ResultSet rs = queryDB("SELECT * FROM ChannelData WHERE Player='" + e.getPlayer().getUniqueId().toString() + "';");
				if(!rs.next()) {
					boolean hasCurrentChannel = false;
					for(Channel channel : autojoinChannels) {
						joinChannel(e.getPlayer(), channel, false, !hasCurrentChannel);
						hasCurrentChannel = true;
					}
				}
			} catch(SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		e.setCancelled(true);

		List<String> chatMessages = chatContinuations.get(e.getPlayer());
		if(chatMessages == null) {
			chatMessages = new ArrayList<String>();
		}
		if(chatMessages.size() < 2 && e.getMessage().endsWith("-")) {
			chatMessages.add(e.getMessage().substring(0, e.getMessage().length() - 1));
			chatContinuations.put(e.getPlayer(), chatMessages);
			return;
		} else if(chatMessages.size() == 2 || !e.getMessage().endsWith("-")) {
			chatMessages.add(e.getMessage());
			e.setMessage(String.join(" ", chatMessages));
			chatContinuations.put(e.getPlayer(), null);
		}

		try {
			ResultSet rs = queryDB("SELECT * FROM ChannelData WHERE Player='" + e.getPlayer().getUniqueId().toString() + "';");
			if(rs.next()) {
				String currentChannelName = rs.getString("CurrentChannel");
				boolean hasCurrentChannel = currentChannelName != null && !currentChannelName.equals("");
				if(hasCurrentChannel) {
					Channel currentChannel = getChannelByName(currentChannelName);
					sendMessageInChannel(e.getPlayer(), currentChannel.getFormattedMessage(e.getPlayer(), e.getMessage()), currentChannel);
				} else {
					e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getMessages().getString("messages.leave.not-in-any")));
				}
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onRightClickPlayerEvent(PlayerInteractEntityEvent e) {
		if(e.getPlayer().isSneaking() && e.getHand() == EquipmentSlot.HAND) {
			e.getPlayer().performCommand("card view " + e.getRightClicked().getName());
		}
	}

	public static String strip(Player p, String message) // This method is way ugly to me and I hate it with a passion.
	{
		boolean continueStrip = false;
		if(p == null || !p.hasPermission("roleplaycore.bold")) {
			continueStrip = message.contains("&l") || message.contains("&L");
			message = boldPattern.matcher(message).replaceAll("");
		}
		if(p == null || !p.hasPermission("roleplaycore.italics")) {
			continueStrip = message.contains("&o") || message.contains("&O");
			message = italicsPattern.matcher(message).replaceAll("");
		}
		if(p == null || !p.hasPermission("roleplaycore.strikethrough")) {
			continueStrip = message.contains("&m") || message.contains("&M");
			message = strikethroughPattern.matcher(message).replaceAll("");
		}
		if(p == null || !p.hasPermission("roleplaycore.underline")) {
			continueStrip = message.contains("&n") || message.contains("&N");
			message = underlinePattern.matcher(message).replaceAll("");
		}
		if(p == null || !p.hasPermission("roleplaycore.random")) {
			continueStrip = message.contains("&k") || message.contains("&K");
			message = randomPattern.matcher(message).replaceAll("");
		}
		if(p == null || !p.hasPermission("roleplaycore.colors")) {
			continueStrip = message.contains("&l") || message.contains("&L");
			if(!continueStrip) {
				for(char ch = 'a'; ch <= 'f'; ch++) {
					continueStrip = message.contains("&" + ch);
					if(continueStrip) {
						break;
					}
				}
			}
			if(!continueStrip) {
				for(char ch = 'A'; ch <= 'F'; ch++) {
					continueStrip = message.contains("&" + ch);
					if(continueStrip) {
						break;
					}
				}
			}
			if(!continueStrip) {
				for(int i = 0; i <= 9; i++) {
					continueStrip = message.contains("&" + i);
					if(continueStrip) {
						break;
					}
				}
			}
			message = colorPattern.matcher(message).replaceAll("");
		}
		return continueStrip ? strip(p, message) : message;
	}

	public FileConfiguration getMessages() {
		if(messages == null) {
			reloadMessages();
		}
		return messages;
	}

	@SuppressWarnings("deprecation")
	public void reloadMessages() {
		messages = YamlConfiguration.loadConfiguration(messagesFile);

		final InputStream defConfigStream = getResource("messages.yml");
		if(defConfigStream == null) {
			return;
		}

		final YamlConfiguration defConfig;
		if(getDescription().getAwareness().contains(PluginAwareness.Flags.UTF8)) {
			defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8));
		} else {
			final byte[] contents;
			defConfig = new YamlConfiguration();
			try {
				contents = ByteStreams.toByteArray(defConfigStream);
			} catch(final IOException e) {
				getLogger().log(Level.SEVERE, "Unexpected failure reading messages.yml", e);
				return;
			}

			final String text = new String(contents, Charset.defaultCharset());
			if(!text.equals(new String(contents, Charsets.UTF_8))) {
				getLogger().warning("Default system encoding may have misread messages.yml from plugin jar");
			}

			try {
				defConfig.loadFromString(text);
			} catch(final InvalidConfigurationException e) {
				getLogger().log(Level.SEVERE, "Cannot load configuration from jar", e);
			}
		}

		messages.setDefaults(defConfig);
	}

	public void saveMessages() {
		try {
			getMessages().save(messagesFile);
		} catch(IOException ex) {
			logger.log(Level.SEVERE, "Could not save messages to " + messagesFile, ex);
		}
	}

	public void saveDefaultMessages() {
		if(!messagesFile.exists()) {
			saveResource("messages.yml", false);
		}
	}

	public static void sendActionBar(Player player, String message) {
		if(!player.isOnline()) {
			return;
		}

		try {
			Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
			Object craftPlayer = craftPlayerClass.cast(player);
			Object packet;
			Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
			Class<?> packetClass = Class.forName("net.minecraft.server." + nmsver + ".Packet");
			if(useOldMethods) {
				Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
				Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				Method m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
				Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
				packet = packetPlayOutChatClass.getConstructor(new Class<?>[] { iChatBaseComponentClass, byte.class }).newInstance(cbc, (byte) 2);
			} else {
				Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
				Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
				try {
					Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + nmsver + ".ChatMessageType");
					Object[] chatMessageTypes = chatMessageTypeClass.getEnumConstants();
					Object chatMessageType = null;
					for(Object obj : chatMessageTypes) {
						if(obj.toString().equals("GAME_INFO")) {
							chatMessageType = obj;
						}
					}
					Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[] { String.class }).newInstance(message);
					packet = packetPlayOutChatClass.getConstructor(new Class<?>[] { iChatBaseComponentClass, chatMessageTypeClass }).newInstance(chatCompontentText, chatMessageType);
				} catch(ClassNotFoundException cnfe) {
					Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[] { String.class }).newInstance(message);
					packet = packetPlayOutChatClass.getConstructor(new Class<?>[] { iChatBaseComponentClass, byte.class }).newInstance(chatCompontentText, (byte) 2);
				}
			}
			Method craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
			Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
			Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
			Object playerConnection = playerConnectionField.get(craftPlayerHandle);
			Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
			sendPacketMethod.invoke(playerConnection, packet);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendActionBar(final Player player, final String message, int duration) {
		sendActionBar(player, message);

		if(duration >= 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					sendActionBar(player, "");
				}
			}.runTaskLater(instance, duration + 1);
		}

		while(duration > 40) {
			duration -= 40;
			new BukkitRunnable() {
				@Override
				public void run() {
					sendActionBar(player, message);
				}
			}.runTaskLater(instance, (long) duration);
		}
	}

	public static void sendActionBarToAllPlayers(String message) {
		sendActionBarToAllPlayers(message, -1);
	}

	public static void sendActionBarToAllPlayers(String message, int duration) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			sendActionBar(p, message, duration);
		}
	}

	public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			dataOutput.writeInt(items.length);

			for(int i = 0; i < items.length; i++) {
				dataOutput.writeObject(items[i]);
			}

			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch(Exception e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
	}

	public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			ItemStack[] items = new ItemStack[dataInput.readInt()];

			for(int i = 0; i < items.length; i++) {
				items[i] = (ItemStack) dataInput.readObject();
			}

			dataInput.close();
			return items;
		} catch(ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}

	public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
		String content = toBase64(playerInventory);
		String armor = itemStackArrayToBase64(playerInventory.getArmorContents());

		return new String[] { content, armor };
	}

	public static String toBase64(Inventory inventory) throws IllegalStateException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			dataOutput.writeObject(inventory.getType());

			for(int i = 0; i < inventory.getSize(); i++) {
				dataOutput.writeObject(inventory.getItem(i));
			}

			dataOutput.close();
			return Base64Coder.encodeLines(outputStream.toByteArray());
		} catch(Exception e) {
			throw new IllegalStateException("Unable to save item stacks.", e);
		}
	}

	public static Inventory fromBase64(String data) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			InventoryType type = (InventoryType) dataInput.readObject();
			Inventory inventory = Bukkit.getServer().createInventory(null, type);

			for(int i = 0; i < inventory.getSize(); i++) {
				inventory.setItem(i, (ItemStack) dataInput.readObject());
			}

			dataInput.close();
			return inventory;
		} catch(ClassNotFoundException e) {
			throw new IOException("Unable to decode class type.", e);
		}
	}
}
