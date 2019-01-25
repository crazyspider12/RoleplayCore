package plugins.astro.roleplaycore;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class RoleplayCoreCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public RoleplayCoreCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("roleplaycore") || cmd.getName().equalsIgnoreCase("rpcore")) {
			if(sender.hasPermission("roleplaycore.reload") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				plugin.reloadConfig();
				plugin.reloadMessages();
				RoleplayCore.channels.clear();
				RoleplayCore.autojoinChannels.clear();
				for(String channel : plugin.getConfig().getConfigurationSection("channels").getKeys(false)) {
					Channel newChannel = new Channel(channel, plugin.getConfig().getBoolean("channels." + channel + ".auto-join"), plugin.getConfig().getString("channels." + channel + ".leave-perm"), plugin.getConfig().getString("channels." + channel + ".join-perm"), plugin.getConfig().getString("channels." + channel + ".view-perm"), plugin.getConfig().getString("channels." + channel + ".talk-perm"), RoleplayCore.strip(null, plugin.getConfig().getString("channels." + channel + ".nickname")), plugin.getConfig().getStringList("channels." + channel + ".aliases"), plugin.getConfig().getString("channels." + channel + ".nickname"), plugin.getConfig().getString("channels." + channel + ".channel-prefix"), plugin.getConfig().getString("channels." + channel + ".chat-format"), plugin.getConfig().getString("channels." + channel + ".emote-prefix"), plugin.getConfig().getInt("channels." + channel + ".range"), plugin.getConfig().getBoolean("channels." + channel + ".rank-prefixes"), plugin.getConfig().getBoolean("channels." + channel + ".emotes"), plugin.getConfig().getBoolean("channels." + channel + ".quotation-emotes"), plugin.getConfig().getInt("channels." + channel + ".cooldown"));
					RoleplayCore.channels.add(newChannel);
					if(newChannel.isAutoJoin()) {
						RoleplayCore.autojoinChannels.add(newChannel);
					}
				}
				RoleplayCore.races.clear();
				for(String race : plugin.getConfig().getConfigurationSection("races").getKeys(false)) {
					Race newRace = new Race(race, plugin.getConfig().getInt("races." + race + ".maxAge"), plugin.getConfig().getString("races." + race + ".color"));
					RoleplayCore.races.add(newRace);
				}

				for(Player p : Bukkit.getOnlinePlayers()) {
					AttributeInstance attribute = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
					attribute.setBaseValue(plugin.getConfig().getDouble("playerHealth"));
				}
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.reloaded")));
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
			}
		}
		return true;
	}
}
