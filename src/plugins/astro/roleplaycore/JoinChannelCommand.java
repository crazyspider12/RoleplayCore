package plugins.astro.roleplaycore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class JoinChannelCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public JoinChannelCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("join")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length != 1) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
				} else {
					Channel channel = RoleplayCore.getChannelByName(args[0]);
					if(channel != null) {
						if(p.hasPermission(channel.getJoinPermission())) {
							String result = plugin.joinChannel(p, channel);
							if(result.equalsIgnoreCase("Joined")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.join.joined" + (channel.getRange() > 0 ? "-range" : "-global")).replaceAll("%nickname%", channel.getInlineFormat()).replaceAll("%range%", "" + channel.getRange())));
							} else if(result.equalsIgnoreCase("Talking")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.join.talking-in" + (channel.getRange() > 0 ? "-range" : "-global")).replaceAll("%nickname%", channel.getInlineFormat()).replaceAll("%range%", "" + channel.getRange())));
							}
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.join.no-permission")));
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.channel-not-found")));
					}
				}
			}
		}
		return true;
	}
}
