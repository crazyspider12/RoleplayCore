package plugins.astro.roleplaycore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveChannelCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public LeaveChannelCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("leave")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length != 1) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
				} else {
					Channel channel = RoleplayCore.getChannelByName(args[0]);
					if(channel != null) {
						if(p.hasPermission(channel.getLeavePermission())) {
							plugin.leaveChannel(p, channel, ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.leave.success").replaceAll("%channel%", channel.getNickname())), false);
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.leave.no-permission")));
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
