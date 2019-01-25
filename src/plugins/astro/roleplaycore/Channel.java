package plugins.astro.roleplaycore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Channel {
	private String channelName;
	private boolean autoJoin;
	private String leavePermission;
	private String joinPermission;
	private String viewPermission;
	private String talkPermission;
	private String nickname;
	private List<String> aliases;
	private String inlineFormat;
	private String channelPrefix;
	private String chatFormat;
	private String emotePrefix;
	private int range;
	private boolean rankPrefixes;
	private boolean emotes;
	private boolean quotationEmotes;
	private int cooldown;

	public static Pattern characternameJsonPattern = Pattern.compile("(.*)(%charactername%)(.*)");
	public static Pattern nameJsonPattern = Pattern.compile("(.*)(%name%)(.*)");

	public Channel(String channelName, boolean autoJoin, String leavePermission, String joinPermission, String viewPermission, String talkPermission, String nickname, List<String> aliases, String inlineFormat, String channelPrefix, String chatFormat, String emotePrefix, int range, boolean rankPrefixes, boolean emotes, boolean quotationEmotes, int cooldown) {
		this.channelName = channelName;
		this.autoJoin = autoJoin;
		this.leavePermission = leavePermission;
		this.joinPermission = joinPermission;
		this.viewPermission = viewPermission;
		this.talkPermission = talkPermission;
		this.nickname = nickname;
		this.aliases = aliases;
		this.inlineFormat = inlineFormat;
		this.channelPrefix = channelPrefix;
		this.chatFormat = chatFormat;
		this.emotePrefix = emotePrefix;
		this.range = range;
		this.rankPrefixes = rankPrefixes;
		this.emotes = emotes;
		this.quotationEmotes = quotationEmotes;
		this.cooldown = cooldown;
	}

	public String getChannelName() {
		return channelName;
	}

	public boolean isAutoJoin() {
		return autoJoin;
	}

	public String getLeavePermission() {
		return leavePermission;
	}

	public String getJoinPermission() {
		return joinPermission;
	}

	public String getViewPermission() {
		return viewPermission;
	}

	public String getTalkPermission() {
		return talkPermission;
	}

	public String getNickname() {
		return nickname;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public String getInlineFormat() {
		return inlineFormat;
	}

	public String getChannelPrefix() {
		return channelPrefix;
	}

	public String getChatFormat() {
		return chatFormat;
	}

	public String getEmotePrefix() {
		return emotePrefix;
	}

	public int getRange() {
		return range;
	}

	public boolean usesRankPrefixes() {
		return rankPrefixes;
	}

	public boolean areEmotesAllowed() {
		return emotes;
	}

	public boolean areQuotationEmotesAllowed() {
		return quotationEmotes;
	}

	public int getCooldown() {
		return cooldown;
	}

	public JsonMessage getFormattedMessage(Player p, String message) {
		if(message.startsWith("*") && !message.equals("*") && emotes) {
			message = RoleplayCore.strip(p, message);
			try {
				String color = "&e";
				ResultSet rs = RoleplayCore.queryDB("SELECT * FROM EmoteData WHERE Player='" + p.getUniqueId().toString() + "';");
				if(rs.next()) {
					color = rs.getString("Color");
				}
				if(quotationEmotes) {
					message = message.replaceAll("(\\\"{1})(.+?)(\\\"{1})", "&f\"$2\"" + color);
				}
				message = message.substring(1);
				return new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', emotePrefix)).save().append(ChatColor.translateAlternateColorCodes('&', color + (RPUtils.getCharacterName(p).equals("UNSET") ? p.getName() : RPUtils.getCharacterName(p)))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', RoleplayCore.getInstance().getMessages().getString("messages.card.in-chat").replaceAll("%player%", p.getName() + "'" + (p.getName().toLowerCase().endsWith("s") ? "" : "s")))).setClickAsExecuteCmd("/card view " + p.getName()).save().append(" ").save().append(ChatColor.translateAlternateColorCodes('&', color + message)).save();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}

		if(hasQuotes(message) && quotationEmotes) {
			try {
				String color = "&e";
				boolean quotationEmotes = true;
				ResultSet rs = RoleplayCore.queryDB("SELECT * FROM EmoteData WHERE Player='" + p.getUniqueId().toString() + "';");
				if(rs.next()) {
					color = rs.getString("Color");
					if(color == null) {
						color = "&e";
					}
					String quotationEmotesString = rs.getString("QuotationEmotes");
					quotationEmotes = (quotationEmotesString != null && rs.getString("QuotationEmotes").equalsIgnoreCase("OFF")) ? false : true;
				}
				if(quotationEmotes) {
					message = color + RoleplayCore.strip(p, message).replaceAll("(\\\"{1})(.+?)(\\\"{1})", "&f\"$2\"" + color);
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}

		if(chatFormat.contains("%charactername%")) {
			String left = characternameJsonPattern.matcher(chatFormat.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$1");
			String right = characternameJsonPattern.matcher(chatFormat.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$3");
			// To any readers: Prepare for complete nonsense.
			if(left.contains("%name%")) {
				String leftLeft = nameJsonPattern.matcher(left.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$1");
				String[] colors = leftLeft.split("\\&(?!l)");
				String leftRight = nameJsonPattern.matcher(left.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$3");
				return new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', leftLeft)).save().append(ChatColor.translateAlternateColorCodes('&', (colors.length != 0 ? "&" + colors[colors.length - 1].substring(0, 1 + (colors[colors.length - 1].contains("&l") ? 2 : 0)) : "") + (rankPrefixes ? RoleplayCore.getChat().getPlayerPrefix(p) : "") + p.getName())).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', RoleplayCore.getInstance().getMessages().getString("messages.card.in-chat").replaceAll("%player%", p.getName() + "'" + (p.getName().toLowerCase().endsWith("s") ? "" : "s")))).setClickAsExecuteCmd("/card view " + p.getName()).save().append(ChatColor.translateAlternateColorCodes('&', leftRight)).save().append(ChatColor.translateAlternateColorCodes('&', (rankPrefixes ? RoleplayCore.getChat().getPlayerPrefix(p) : "") + RPUtils.getCharacterName(p))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', RoleplayCore.getInstance().getMessages().getString("messages.card.in-chat").replaceAll("%player%", p.getName() + "'" + (p.getName().toLowerCase().endsWith("s") ? "" : "s")))).setClickAsExecuteCmd("/card view " + p.getName()).save().append(ChatColor.translateAlternateColorCodes('&', right)).save();
			} else if(right.contains("%name%")) {
				String rightLeft = nameJsonPattern.matcher(right.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$1");
				String[] colors = rightLeft.split("\\&(?!l)");
				String rightRight = nameJsonPattern.matcher(right.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$3");
				return new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', left)).save().append(ChatColor.translateAlternateColorCodes('&', (rankPrefixes ? RoleplayCore.getChat().getPlayerPrefix(p) : "") + RPUtils.getCharacterName(p))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', RoleplayCore.getInstance().getMessages().getString("messages.card.in-chat").replaceAll("%player%", p.getName() + "'" + (p.getName().toLowerCase().endsWith("s") ? "" : "s")))).setClickAsExecuteCmd("/card view " + p.getName()).save().append(ChatColor.translateAlternateColorCodes('&', rightLeft)).save().append(ChatColor.translateAlternateColorCodes('&', (colors.length != 0 ? "&" + colors[colors.length - 1].substring(0, 1 + (colors[colors.length - 1].contains("&l") ? 2 : 0)) : "") + (rankPrefixes ? RoleplayCore.getChat().getPlayerPrefix(p) : "") + p.getName())).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', RoleplayCore.getInstance().getMessages().getString("messages.card.in-chat").replaceAll("%player%", p.getName() + "'" + (p.getName().toLowerCase().endsWith("s") ? "" : "s")))).setClickAsExecuteCmd("/card view " + p.getName()).save().append(ChatColor.translateAlternateColorCodes('&', rightRight)).save();
			} else {
				String[] colors = left.split("\\&(?!l)");
				return new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', left)).save().append(ChatColor.translateAlternateColorCodes('&', (colors.length != 0 ? "&" + colors[colors.length - 1].substring(0, 1 + (colors[colors.length - 1].contains("&l") ? 2 : 0)) : "") + (rankPrefixes ? RoleplayCore.getChat().getPlayerPrefix(p) : "") + RPUtils.getCharacterName(p))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', RoleplayCore.getInstance().getMessages().getString("messages.card.in-chat").replaceAll("%player%", p.getName() + "'" + (p.getName().toLowerCase().endsWith("s") ? "" : "s")))).setClickAsExecuteCmd("/card view " + p.getName()).save().append(ChatColor.translateAlternateColorCodes('&', right)).save();
			}
		} else if(chatFormat.contains("%name%")) {
			String left = nameJsonPattern.matcher(chatFormat.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$1");
			String[] colors = left.split("\\&(?!l)");
			String right = nameJsonPattern.matcher(chatFormat.replace("%channelprefix%", channelPrefix).replace("%message%", message)).replaceAll("$3");
			return new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', left)).save().append(ChatColor.translateAlternateColorCodes('&', (colors.length != 0 ? "&" + colors[colors.length - 1].substring(0, 1 + (colors[colors.length - 1].contains("&l") ? 2 : 0)) : "") + (rankPrefixes ? RoleplayCore.getChat().getPlayerPrefix(p) : "") + p.getName())).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', RoleplayCore.getInstance().getMessages().getString("messages.card.in-chat").replaceAll("%player%", p.getName() + "'" + (p.getName().toLowerCase().endsWith("s") ? "" : "s")))).setClickAsExecuteCmd("/card view " + p.getName()).save().append(ChatColor.translateAlternateColorCodes('&', right)).save();
		}
		return new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', chatFormat.replace("%channelprefix%", channelPrefix).replace("%message%", message))).save(); // This shouldn't ever run.
	}

	private boolean hasQuotes(String input) {
		int quotationMarkCount = 0;
		for(char ch : input.toCharArray()) {
			if(ch == '"') {
				quotationMarkCount++;
				if(quotationMarkCount >= 2) {
					return true;
				}
			}
		}
		return false;
	}
}