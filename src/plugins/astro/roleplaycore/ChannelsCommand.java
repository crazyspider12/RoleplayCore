package plugins.astro.roleplaycore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ChannelsCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public ChannelsCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("channels")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.channels.headerMessage")));
			for(Channel c : RoleplayCore.channels) {
				if(sender.hasPermission(c.getJoinPermission())) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.channels.listedChannel").replaceAll("%channel%", c.getInlineFormat())));
				}
			}
		}
		return true;
	}
}
