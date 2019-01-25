package plugins.astro.roleplaycore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class RollCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public RollCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("roll")) {
			if(sender.hasPermission("roleplaycore.roll")) {
				if(sender instanceof Player) {
					Player p = (Player) sender;
					if(args.length < 2) {
						try {
							int max = Integer.parseInt(args.length == 0 ? "20" : args[0]);
							int roll = new Random().nextInt(max) + 1;
							List<Player> nearbyPlayers = new ArrayList<Player>();
							for(Entity e : p.getNearbyEntities(32, 32, 32)) {
								if(e instanceof Player) {
									nearbyPlayers.add((Player) e);
								}
							}
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.roll.rollMessage").replaceAll("%charactername%", RPUtils.getCharacterName(p)).replaceAll("%name%", p.getDisplayName()).replaceAll("%roll%", roll + "").replaceAll("%max%", max + "")));
							for(Player n : nearbyPlayers) {
								n.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.roll.rollMessage").replaceAll("%charactername%", RPUtils.getCharacterName(p)).replaceAll("%name%", p.getDisplayName()).replaceAll("%roll%", roll + "").replaceAll("%max%", max + "")));
							}
						} catch(NumberFormatException e) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-number")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.roll.console-error")));
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
			}
		}
		return true;
	}
}
