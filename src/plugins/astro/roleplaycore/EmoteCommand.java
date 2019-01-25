package plugins.astro.roleplaycore;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EmoteCommand implements Listener, CommandExecutor {
	private RoleplayCore plugin;

	public EmoteCommand(RoleplayCore plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("emote")) {
			if(sender instanceof Player) {
				openGUI((Player) sender);
			}
		}
		return true;
	}

	public void openGUI(Player p) {
		Inventory inv = Bukkit.createInventory(new EmoteHolder(), 18, "Emotes Menu");
		inv.setItem(0, getNamedItem("&8Black", new ItemStack(Material.BLACK_WOOL, 1)));
		inv.setItem(1, getNamedItem("&1Dark Blue", new ItemStack(Material.LAPIS_BLOCK, 1)));
		inv.setItem(2, getNamedItem("&2Dark Green", new ItemStack(Material.GREEN_WOOL, 1)));
		inv.setItem(3, getNamedItem("&3Dark Aqua", new ItemStack(Material.CYAN_WOOL, 1)));
		inv.setItem(4, getNamedItem("&4Dark Red", new ItemStack(Material.REDSTONE_BLOCK)));
		inv.setItem(5, getNamedItem("&5Dark Purple", new ItemStack(Material.MAGENTA_WOOL, 1)));
		inv.setItem(6, getNamedItem("&6Gold", new ItemStack(Material.ORANGE_WOOL, 1)));
		inv.setItem(7, getNamedItem("&7Light Gray", new ItemStack(Material.LIGHT_GRAY_WOOL, 1)));
		inv.setItem(8, getNamedItem("&8Dark Gray", new ItemStack(Material.GRAY_WOOL, 1)));
		inv.setItem(10, getNamedItem("&9Blue", new ItemStack(Material.BLUE_WOOL, 1)));
		inv.setItem(11, getNamedItem("&aGreen", new ItemStack(Material.LIME_WOOL, 1)));
		inv.setItem(12, getNamedItem("&bAqua", new ItemStack(Material.LIGHT_BLUE_WOOL, 1)));
		inv.setItem(13, getNamedItem("&cRed", new ItemStack(Material.RED_WOOL, 1)));
		inv.setItem(14, getNamedItem("&dPurple", new ItemStack(Material.PINK_WOOL, 1)));
		inv.setItem(15, getNamedItem("&eYellow", new ItemStack(Material.YELLOW_WOOL, 1)));
		inv.setItem(16, getNamedItem("&fWhite", new ItemStack(Material.WHITE_WOOL, 1)));
		inv.setItem(17, getNamedItem("&cDisable quotation emotes", new ItemStack(Material.PAPER, 1)));
		try {
			ResultSet rs = RoleplayCore.queryDB("SELECT QuotationEmotes FROM EmoteData WHERE Player='" + p.getUniqueId().toString() + "';");
			if(rs.next()) {
				String quotationEmotes = rs.getString("QuotationEmotes");
				if(quotationEmotes != null && quotationEmotes.equalsIgnoreCase("OFF")) {
					inv.setItem(17, getNamedItem("&aEnable quotation emotes", new ItemStack(Material.MAP, 1)));
				}
			}
		} catch(SQLException ex) {
			ex.printStackTrace();
		}

		p.openInventory(inv);
	}

	public ItemStack getNamedItem(String name, ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		item.setItemMeta(meta);
		return item;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getInventory() != null && e.getInventory().getHolder() instanceof EmoteHolder) {
			if((e.getSlot() >= 0 && e.getSlot() <= 8) || (e.getSlot() >= 10 && e.getSlot() <= 16)) {
				String color = e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName();
				try {
					ResultSet rs = RoleplayCore.queryDB("SELECT * FROM EmoteData WHERE Player='" + e.getWhoClicked().getUniqueId().toString() + "';");
					if(rs.next()) {
						RoleplayCore.queryDB("UPDATE EmoteData SET Color='" + (color.endsWith("Black") ? "&0" : "&" + color.substring(1, 2)) + "' WHERE Player='" + e.getWhoClicked().getUniqueId().toString() + "'");
					} else {
						RoleplayCore.queryDB("INSERT INTO EmoteData(Player, Color) VALUES('" + e.getWhoClicked().getUniqueId().toString() + "', '" + (color.endsWith("Black") ? "&0" : "&" + color.substring(1, 2)) + "');");
					}
					e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.emotes.selected-color").replaceAll("%color%", color)));
				} catch(SQLException ex) {
					ex.printStackTrace();
				}
			} else if(e.getSlot() == 17) {
				try {
					ResultSet rs = RoleplayCore.queryDB("SELECT QuotationEmotes FROM EmoteData WHERE Player='" + e.getWhoClicked().getUniqueId().toString() + "';");
					if(rs.next()) {
						String quotationEmotes = rs.getString("QuotationEmotes");
						RoleplayCore.queryDB("UPDATE EmoteData SET QuotationEmotes='" + (quotationEmotes != null && quotationEmotes.equalsIgnoreCase("OFF") ? "ON" : "OFF") + "' WHERE Player='" + e.getWhoClicked().getUniqueId().toString() + "'");
					} else {
						RoleplayCore.queryDB("INSERT INTO EmoteData(Player, QuotationEmotes) VALUES('" + e.getWhoClicked().getUniqueId().toString() + "', 'OFF');");
					}
				} catch(SQLException ex) {
					ex.printStackTrace();
				}
			}
			e.getWhoClicked().closeInventory();
			e.setCancelled(true);
		}
	}
}

class EmoteHolder implements InventoryHolder {
	@Override
	public Inventory getInventory() {
		return null;
	}
}
