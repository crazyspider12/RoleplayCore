package plugins.astro.roleplaycore;

import org.bukkit.ChatColor;

public class Race {
	private String name;
	private int maxAge;
	private ChatColor color;

	public Race(String name, int maxAge, String color) {
		this.name = name;
		this.maxAge = maxAge;
		this.color = stringToColor(color);
	}

	public String getName() {
		return name;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public ChatColor getColor() {
		return color;
	}

	public static ChatColor stringToColor(String input) {
		input = input.toLowerCase();
		switch(input) {
		case "0": case "&0": case "black":
			return ChatColor.BLACK;
		case "1": case "&1": case "dark blue":
			return ChatColor.DARK_BLUE;
		case "2": case "&2": case "dark green":
			return ChatColor.DARK_GREEN;
		case "3": case "&3": case "dark aqua":
			return ChatColor.DARK_AQUA;
		case "4": case "&4": case "dark red":
			return ChatColor.DARK_RED;
		case "5": case "&5": case "dark purple": case "dark magenta": case "dark pink":
			return ChatColor.DARK_PURPLE;
		case "6": case "&6": case "gold": case "dark yellow":
			return ChatColor.GOLD;
		case "7": case "&7": case "light gray": case "gray":
			return ChatColor.GRAY;
		case "8": case "&8": case "dark gray":
			return ChatColor.DARK_GRAY;
		case "9": case "&9": case "blue":
			return ChatColor.BLUE;
		case "a": case "&a": case "light green": case "green":
			return ChatColor.GREEN;
		case "b": case "&b": case "light aqua": case "aqua": case "cyan":
			return ChatColor.AQUA;
		case "c": case "&c": case "light red": case "red":
			return ChatColor.RED;
		case "d": case "&d": case "light purple": case "light magenta": case "light pink": case "magenta": case "purple": case "pink":
			return ChatColor.LIGHT_PURPLE;
		case "e": case "&e": case "light yellow": case "yellow":
			return ChatColor.YELLOW;
		case "f": case "&f": case "white":
			return ChatColor.WHITE;
		}
		return null;
	}
}
