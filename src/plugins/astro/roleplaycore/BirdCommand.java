package plugins.astro.roleplaycore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BirdCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public BirdCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("bird")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length < 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.too-little-arguments")));
				} else {
					Player target = Bukkit.getPlayer(args[0]);
					if(target != null) {
						if(p.getWorld() == target.getWorld()) {
							String message = "";
							for(int i = 1; i < args.length; i++) {
								message += args[i] + " ";
							}
							message = message.substring(0, message.length() - 1);
							final String finalMessage = message;
							Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
								@Override
								public void run() {
									if(target.isOnline()) {
										target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.bird.messageReceived")));
										target.sendMessage(ChatColor.translateAlternateColorCodes('&', finalMessage));
									} else {
										if(p.isOnline()) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.bird.targetWentOffline")));
										}
									}
								}
							}, (long) (p.getLocation().distance(target.getLocation()) / plugin.getConfig().getInt("bird.blocksPerSecond") * 20));
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.bird.targetIsInAnotherDimension")));
						}
					} else {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.target-not-found")));
					}
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.bird.console-error")));
			}
		}
		return true;
	}
}
