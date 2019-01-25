package plugins.astro.roleplaycore;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CountdownCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public CountdownCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("countdown")) {
			if(sender.hasPermission("roleplaycore.countdown")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(args.length == 0) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.too-little-arguments")));
					} else {
						if(RoleplayCore.countdowns.contains(p)) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.already-counting")));
							return true;
						}
						try {
							int time = Integer.parseInt(args[0]);
							if(time > plugin.getConfig().getInt("max-countdown")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.exceeds-max").replaceAll("%max%", "" + plugin.getConfig().getInt("max-countdown"))));
								return true;
							}
							List<Player> nearbyPlayers = new ArrayList<Player>();
							for(Entity e : p.getNearbyEntities(32, 32, 32)) {
								if(e instanceof Player) {
									nearbyPlayers.add((Player) e);
								}
							}
							RoleplayCore.countdowns.add(p);
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.started").replaceAll("%name%", p.getName()).replaceAll("%time%", "" + time)));
							for(Player n : nearbyPlayers) {
								n.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.started").replaceAll("%name%", p.getName()).replaceAll("%time%", "" + time)));
							}
							new BukkitRunnable() {
								int secondsLeft = time;

								@Override
								public void run() {
									secondsLeft--;
									if(secondsLeft > 0) {
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.decrement").replaceAll("%timeLeft%", "" + secondsLeft)));
										for(Player n : nearbyPlayers) {
											n.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.decrement").replaceAll("%timeLeft%", "" + secondsLeft)));
										}
									} else {
										if(secondsLeft == 0) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.finished")));
											for(Player n : nearbyPlayers) {
												n.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.finished")));
											}
										}
										RoleplayCore.countdowns.remove(p);
										this.cancel();
									}
								}
							}.runTaskTimer(plugin, 20L, 20L);
						} catch(Exception e) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-number")));
						}
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.countdown.console-error")));
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
			}
		}
		return true;
	}
}
