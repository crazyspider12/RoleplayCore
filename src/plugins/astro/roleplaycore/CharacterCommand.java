package plugins.astro.roleplaycore;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class CharacterCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public CharacterCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("character")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length == 0) {
					try {
						ResultSet rsActive = RoleplayCore.queryDB("SELECT CharacterName,CreationOrder FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
						while(rsActive.next()) {
							String characterName = rsActive.getString("CharacterName");
							if(characterName == null) { characterName = "UNSET"; };
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.character-slot-info-active").replaceAll("%name%", characterName).replaceAll("%order%", "" + rsActive.getInt("CreationOrder"))));
						}

						List<String> otherCharacters = new ArrayList<String>();
						ResultSet rsInactive = RoleplayCore.queryDB("SELECT CharacterName,CreationOrder FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=0;");
						while(rsInactive.next()) {
							String characterName = rsInactive.getString("CharacterName");
							if(characterName == null) { characterName = "UNSET"; };
							otherCharacters.add(plugin.getMessages().getString("messages.character.character-slot-info").replaceAll("%name%", characterName).replaceAll("%order%", "" + rsInactive.getInt("CreationOrder")));
						}
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.character-help-prompt")));
						if(!otherCharacters.isEmpty()) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.other-characters")));
							for(String s : otherCharacters) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
							}
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				} else if(args.length == 1) {
					if(args[0].equalsIgnoreCase("new")) {
						try {
							int highestCreationOrder = 0;
							ResultSet rs = RoleplayCore.queryDB("SELECT CreationOrder FROM Cards WHERE Player='" + p.getUniqueId().toString() + "';");
							while(rs.next()) {
								int creationOrder = rs.getInt("CreationOrder");
								highestCreationOrder = creationOrder > highestCreationOrder ? creationOrder : highestCreationOrder;
							}
							if(highestCreationOrder + 1 <= getHighestPermissibleSlotCount(p)) {
								RoleplayCore.queryDB("INSERT INTO Cards(Player, CreationOrder, Active) VALUES('" + p.getUniqueId().toString() + "', " + (highestCreationOrder + 1) + ", 0);");
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.successfully-created").replaceAll("%order%", "" + (highestCreationOrder + 1))));
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.max-slot-count")));
							}
						} catch(SQLException e) {
							e.printStackTrace();
						}
					} else if(args[0].equalsIgnoreCase("help")) {
						for(String s : plugin.getMessages().getStringList("messages.character.help-text"))  {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
						}
					}
				} else if(args.length == 2 && args[0].equalsIgnoreCase("slot")) {
					try {
						int slotNumber = Integer.parseInt(args[1]);

						if(slotNumber <= getHighestPermissibleSlotCount(p)) {
							ResultSet rsCheck = RoleplayCore.queryDB("SELECT * FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND CreationOrder=" + slotNumber + ";");
							if(rsCheck.next()) {
								String[] serializedInventory = RoleplayCore.playerInventoryToBase64(p.getInventory());
								RoleplayCore.queryDB("UPDATE Cards SET Contents='" + serializedInventory[0] + "' WHERE Player='" + p.getUniqueId().toString() + "' AND Active = 1;");
								RoleplayCore.queryDB("UPDATE Cards SET ArmorContents='" + serializedInventory[1] + "' WHERE Player='" + p.getUniqueId().toString() + "' AND Active = 1;");
								RoleplayCore.queryDB("UPDATE Cards SET Active = 0 WHERE Player='" + p.getUniqueId().toString() + "';");
								RoleplayCore.queryDB("UPDATE Cards SET Active = 1 WHERE Player='" + p.getUniqueId().toString() + "' AND CreationOrder=" + slotNumber + ";");

								ResultSet rs = RoleplayCore.queryDB("SELECT Contents,ArmorContents FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active = 1;");
								if(rs.next()) {
									p.getInventory().clear();
									String contentsString = rs.getString("Contents");
									String armorContentsString = rs.getString("ArmorContents");
									if(contentsString != null) {
										p.getInventory().setContents(RoleplayCore.fromBase64(contentsString).getContents());
									}
									if(armorContentsString != null) {
										p.getInventory().setArmorContents(RoleplayCore.itemStackArrayFromBase64(armorContentsString));
									}
								}
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.successfully-switched-to-slot").replaceAll("%order%", "" + slotNumber)));
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.no-character-with-count")));
							}
						}
					} catch(NumberFormatException e) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-number")));
					} catch(IllegalArgumentException e) {
						e.printStackTrace();
					} catch(IOException e) {
						e.printStackTrace();
					} catch(SQLException e) {
						e.printStackTrace();
					}
				} else {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
				} 
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.character.console-error")));
			}
		}
		return true;
	}

	public int getHighestPermissibleSlotCount(Player p) {
		if(p.isOp() || p.hasPermission("*")) {
			return 2147483647;
		}
		int highestSlotPermissible = 0;
		for(PermissionAttachmentInfo perm : p.getEffectivePermissions()) {
			if(perm.getPermission().toLowerCase().startsWith("roleplaycore.character.slot.")) {
				try {
					int permissedSlotNumber = Integer.parseInt(perm.getPermission().split("\\.")[3]);
					if(permissedSlotNumber > highestSlotPermissible) {
						highestSlotPermissible = permissedSlotNumber;
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		return highestSlotPermissible;
	}
}