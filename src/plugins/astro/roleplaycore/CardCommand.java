package plugins.astro.roleplaycore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CardCommand implements CommandExecutor {
	private RoleplayCore plugin;

	public CardCommand(RoleplayCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("card")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length == 0) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.header").replaceAll("%name%", "Your")));
					new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.name").replaceAll("%name%", RPUtils.getCharacterName(p)))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.name-hover"))).setClickAsSuggestCmd("/card name ").save().send(p);
					new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.age").replaceAll("%age%", (RPUtils.getAge(p) == -1 ? "UNSET" : "" + RPUtils.getAge(p))))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.age-hover"))).setClickAsSuggestCmd("/card age ").save().send(p);
					new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.gender").replaceAll("%gender%", RPUtils.getGender(p)))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.gender-hover"))).setClickAsSuggestCmd("/card gender ").save().send(p);
					new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.race").replaceAll("%raceColor%", RPUtils.getRace2(p) != null ? RPUtils.getRace2(p).getColor() + "" : "").replaceAll("%raceName%", RPUtils.getRace2(p) != null ? RPUtils.getRace2(p).getName() : RPUtils.getRace3(p) == null ? "UNSET" : RPUtils.getRace3(p)))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.race-hover"))).setClickAsSuggestCmd("/card race ").save().send(p);
					new JsonMessage().append(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.description").replaceAll("%description%", RPUtils.getDescription(p)))).setHoverAsTooltip(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.description-hover"))).setClickAsSuggestCmd("/card description ").save().send(p);
				} else if(args.length >= 2) {
					if(args[0].equalsIgnoreCase("name")) {
						if(p.hasPermission("roleplaycore.card.name")) {
							String name = "";
							for(int i = 1; i < args.length; i++) {
								name += args[i] + " ";
							}
							name = name.substring(0, name.length() - 1);
							RPUtils.setCharacterName(p, name);
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "name").replaceAll("%new%", name)));
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
						}
					} else if(args[0].equalsIgnoreCase("age")) {
						try {
							int age = Integer.parseInt(args[1]);
							if(age > 0 || p.hasPermission("roleplaycore.card.age")) {
								Race currentRace = RPUtils.getRace2(p);
								if(currentRace != null || p.hasPermission("roleplaycore.card.age")) {
									if(p.hasPermission("roleplaycore.card.age") || age <= currentRace.getMaxAge()) {
										RPUtils.setAge(p, age);
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "age").replaceAll("%new%", "" + age)));
									} else {
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.max-age").replaceAll("%maxAge%", "" + currentRace.getMaxAge())));
									}
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.choose-race-first")));
								}
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.invalid-age")));
							}
						} catch(NumberFormatException e) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.invalid-age")));
						}
					} else if(args[0].equalsIgnoreCase("gender")) {
						if(p.hasPermission("roleplaycore.card.gender")) {
							String gender = "";
							for(int i = 1; i < args.length; i++) {
								gender += args[i] + " ";
							}
							gender = gender.substring(0, gender.length() - 1);
							RPUtils.setGender(p, gender);
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "gender").replaceAll("%new%", gender)));
						} else {
							if(args[1].equalsIgnoreCase("m") || args[1].equalsIgnoreCase("male")) {
								RPUtils.setGender(p, "Male");
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "gender").replaceAll("%new%", "Male")));
							} else if(args[1].equalsIgnoreCase("f") || args[1].equalsIgnoreCase("female")) {
								RPUtils.setGender(p, "Female");
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "gender").replaceAll("%new%", "Female")));
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.invalid-gender")));
							}
						}
					} else if(args[0].equalsIgnoreCase("race")) {
						Race race = RoleplayCore.getRaceByName(args[1]);
						if(race != null || p.hasPermission("roleplaycore.card.race")) {
							String raceString = "";
							if(p.hasPermission("roleplaycore.card.race")) {
								for(int i = 1; i < args.length; i++) {
									raceString += args[i] + " ";
								}
								raceString = raceString.substring(0, raceString.length() - 1);
								RPUtils.setRace(p, raceString);
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "race").replaceAll("%new%", raceString)));
							} else {
								if(p.hasPermission("roleplaycore.races.*") || p.hasPermission("roleplaycore.races." + race.getName().toLowerCase())) {
									RPUtils.setRace(p, race, false);
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "race").replaceAll("%new%", race.getName())));
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.races.no-permission").replaceAll("%race%", race.getName())));
								}
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.invalid-race")));
						}
					} else if(args[0].equalsIgnoreCase("desc") || args[0].equalsIgnoreCase("description")) {
						if(p.hasPermission("roleplaycore.card.desc")) {
							String description = "";
							for(int i = 1; i < args.length; i++) {
								description += args[i] + " ";
							}
							description = description.substring(0, description.length() - 1);
							RPUtils.setDescription(p, description);
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set").replaceAll("%category%", "description").replaceAll("%new%", description)));
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
						}
					} else if(args[0].equalsIgnoreCase("view")) {
						@SuppressWarnings("deprecation")
						OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
						if(target.hasPlayedBefore() || target.isOnline()) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.header").replaceAll("%name%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s"))));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.name").replaceAll("%name%", RPUtils.getCharacterName(target))));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.age").replaceAll("%age%", (RPUtils.getAge(target) == -1 ? "UNSET" : "" + RPUtils.getAge(target)))));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.gender").replaceAll("%gender%", RPUtils.getGender(target))));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.race").replaceAll("%raceColor%", RPUtils.getRace2(target) != null ? RPUtils.getRace2(target).getColor() + "" : "").replaceAll("%raceName%", RPUtils.getRace2(target) != null ? RPUtils.getRace2(target).getName() : RPUtils.getRace3(target) == null ? "UNSET" : RPUtils.getRace3(target))));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.description").replaceAll("%description%", RPUtils.getDescription(target))));
						}
					} else if(args[0].equalsIgnoreCase("edit")) {
						if(p.hasPermission("roleplaycore.card.edit")) {
							if(args.length >= 4) {
								@SuppressWarnings("deprecation")
								OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
								if(target.hasPlayedBefore() || target.isOnline()) {
									if(args[2].equalsIgnoreCase("name")) {
										String name = "";
										for(int i = 3; i < args.length; i++) {
											name += args[i] + " ";
										}
										name = name.substring(0, name.length() - 1);
										RPUtils.setCharacterName(target, name);
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "name").replaceAll("%new%", name)));
										if(target.isOnline()) {
											((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.edited").replaceAll("%category%", "name")));
										}
									} else if(args[2].equalsIgnoreCase("age")) {
										try {
											int age = Integer.parseInt(args[3]);
											RPUtils.setAge(target, age);

											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "age").replaceAll("%new%", "" + age)));
											if(target.isOnline()) {
												((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.edited").replaceAll("%category%", "age")));
											}
										} catch(NumberFormatException e) {
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.invalid-age")));
										}
									} else if(args[2].equalsIgnoreCase("gender")) {
										if(args[3].equalsIgnoreCase("m") || args[3].equalsIgnoreCase("male")) {
											RPUtils.setGender(target, "Male");
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "gender").replaceAll("%new%", "Male")));
											if(target.isOnline()) {
												((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.edited").replaceAll("%category%", "gender")));
											}
										} else if(args[3].equalsIgnoreCase("f") || args[3].equalsIgnoreCase("female")) {
											RPUtils.setGender(target, "Female");
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "gender").replaceAll("%new%", "Female")));
											if(target.isOnline()) {
												((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.edited").replaceAll("%category%", "gender")));
											}
										} else {
											String gender = "";
											for(int i = 3; i < args.length; i++) {
												gender += args[i] + " ";
											}
											gender = gender.substring(0, gender.length() - 1);
											RPUtils.setGender(target, gender);
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "gender").replaceAll("%new%", gender)));
											if(target.isOnline()) {
												((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.edited").replaceAll("%category%", "gender")));
											}
										}
									} else if(args[2].equalsIgnoreCase("race")) {
										String race = "";
										for(int i = 3; i < args.length; i++) {
											race += args[i] + " ";
										}
										race = race.substring(0, race.length() - 1);
										if(RoleplayCore.getRaceByName(race) != null) {
											RPUtils.setRace(target, RoleplayCore.getRaceByName(race), true);
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "race").replaceAll("%new%", RoleplayCore.getRaceByName(race).getName())));
										} else {
											RPUtils.setRace(target, race);
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "race").replaceAll("%new%", race)));
										}
										if(target.isOnline()) {
											((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.edited").replaceAll("%category%", "race")));
										}
									} else if(args[2].equalsIgnoreCase("desc") || args[2].equalsIgnoreCase("description")) {
										String description = "";
										for(int i = 3; i < args.length; i++) {
											description += args[i] + " ";
										}
										description = description.substring(0, description.length() - 1);
										RPUtils.setDescription(target, description);
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.successfully-set-other").replaceAll("%person%", target.getName() + "'" + (target.getName().toLowerCase().endsWith("s") ? "" : "s")).replaceAll("%category%", "description").replaceAll("%new%", description)));
										if(target.isOnline()) {
											((Player) target).sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.edited").replaceAll("%category%", "description")));
										}
									}
								} else {
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.target-not-found")));
								}
							} else {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.invalid-arguments-edit")));
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.no-permission")));
						}
					}
				} else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.generic.invalid-arguments")));
				}
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessages().getString("messages.card.console-error")));
			}
		}
		return true;
	}
}
