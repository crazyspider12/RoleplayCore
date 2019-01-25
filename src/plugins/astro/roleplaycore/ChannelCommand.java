package plugins.astro.roleplaycore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class ChannelCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public ChannelCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("channel")) {
			if(args.length == 3) {
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
				if(target == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.target-not-found")));
					return true;
				}
				Channel channel = RoleplayCore.getChannelByName(args[2]);
				if(channel == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.channel-not-found")));
					return true;
				}

				if(args[0].equalsIgnoreCase("ban")) {
					if(sender.hasPermission("roleplaycore.channel.ban")) {
						if(!plugin.banFromChannel(target, channel)) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.already-banned").replaceAll("%channel%", channel.getNickname())));
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.channel-banned").replaceAll("%player%", target.getName()).replaceAll("%channel%", channel.getNickname())));
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
					}
				} else if(args[0].equalsIgnoreCase("unban") || args[0].equalsIgnoreCase("pardon")) {
					if(sender.hasPermission("roleplaycore.channel.unban")) {
						if(!plugin.unbanFromChannel(target, channel)) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.not-banned").replaceAll("%channel%", channel.getNickname())));
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.channel-unbanned").replaceAll("%player%", target.getName()).replaceAll("%channel%", channel.getNickname())));
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
					}
				} else if(args[0].equalsIgnoreCase("kick")) {
					if(sender.hasPermission("roleplaycore.channel.kick")) {
						if(!plugin.leaveChannel(target, channel, ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.kicked").replaceAll("%channel%", channel.getNickname())), true)) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.not-in-channel").replaceAll("%channel%", channel.getNickname())));
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.channel-kicked").replaceAll("%player%", target.getName()).replaceAll("%channel%", channel.getNickname())));
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
					}
				} else if(args[0].equalsIgnoreCase("forcejoin")) {
					if(sender.hasPermission("roleplaycore.channel.forcejoin")) {
						if(plugin.joinChannel(target, channel, true, true).equalsIgnoreCase("Joined")) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.channel-forcejoined").replaceAll("%player%", target.getName()).replaceAll("%channel%", channel.getNickname())));
							if(target.isOnline()) {
								((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.chat.forcibly-talking-in" + (channel.getRange() > 0 ? "-range" : "-global")).replaceAll("%nickname%", channel.getInlineFormat()).replaceAll("%range%", "" + channel.getRange())));
							}
						}
					} else {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
			}
		}
		return true;
	}
}
