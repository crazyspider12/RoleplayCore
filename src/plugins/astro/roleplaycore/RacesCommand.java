package plugins.astro.roleplaycore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RacesCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public RacesCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("races")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.races.headerMessage")));
			for(Race r : RoleplayCore.races) {
				if(sender.hasPermission("roleplaycore.races." + r.getName()) || sender.hasPermission("roleplaycore.races.*")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.races.listedRace").replaceAll("%raceColor%", r.getColor() + "").replaceAll("%raceName%", r.getName())));
				}
			}
		}
		return true;
	}
}
