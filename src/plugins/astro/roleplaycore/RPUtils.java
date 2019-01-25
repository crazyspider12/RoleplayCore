package plugins.astro.roleplaycore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.OfflinePlayer;

public class RPUtils {
	public static String getCharacterName(OfflinePlayer target) {
		ResultSet rs = RoleplayCore.queryDB("SELECT CharacterName FROM Cards WHERE Player='" + target.getUniqueId().toString() + "' AND Active=1;");
		try {
			if(rs.next()) {
				String returnedName = rs.getString("CharacterName");
				return returnedName == null ? "UNSET" : returnedName;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "UNSET";
	}

	public static int getAge(OfflinePlayer p) {
		ResultSet rs = RoleplayCore.queryDB("SELECT Age FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
		try {
			if(rs.next()) {
				int returnedAge = rs.getInt("Age");
				return returnedAge <= 0 ? -1 : returnedAge;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static String getGender(OfflinePlayer p) {
		ResultSet rs = RoleplayCore.queryDB("SELECT Gender FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
		try {
			if(rs.next()) {
				String returnedGender = rs.getString("Gender");
				return returnedGender == null ? "UNSET" : returnedGender;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "UNSET";
	}

	public static Race getRace2(OfflinePlayer p) {
		String race = getRace3(p);
		return race == null ? null : RoleplayCore.getRaceByName(race);
	}

	public static String getRace3(OfflinePlayer p) {
		ResultSet rs = RoleplayCore.queryDB("SELECT Race FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
		try {
			if(rs.next()) {
				return rs.getString("Race");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getDescription(OfflinePlayer p) {
		ResultSet rs = RoleplayCore.queryDB("SELECT Description FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
		try {
			if(rs.next()) {
				String returnedDescription = rs.getString("Description");
				return returnedDescription == null ? "UNSET" : returnedDescription;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "UNSET";
	}

	public static void setCharacterName(OfflinePlayer p, String name) {
		ResultSet rs = RoleplayCore.queryDB("SELECT * FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
		try {
			PreparedStatement statement;
			if(rs.next()) {
				statement = RoleplayCore.connection.prepareStatement("UPDATE Cards SET CharacterName=? WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			} else {
				statement = RoleplayCore.connection.prepareStatement("INSERT INTO Cards(Player, CharacterName, CreationOrder, Active) VALUES('" + p.getUniqueId().toString() + "', ?, 1, 1);");
			}
			statement.setString(1, name);
			statement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setAge(OfflinePlayer p, int age) {
		try {
			ResultSet rs = RoleplayCore.queryDB("SELECT * FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			PreparedStatement statement;
			if(rs.next()) {
				statement = RoleplayCore.connection.prepareStatement("UPDATE Cards SET Age=? WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			} else {
				statement = RoleplayCore.connection.prepareStatement("INSERT INTO Cards(Player, Age, CreationOrder, Active) VALUES('" + p.getUniqueId().toString() + "', ?, 1, 1);");
			}
			statement.setInt(1, age);
			statement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setGender(OfflinePlayer p, String gender) {
		try {
			ResultSet rs = RoleplayCore.queryDB("SELECT * FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			PreparedStatement statement;
			if(rs.next()) {
				statement = RoleplayCore.connection.prepareStatement("UPDATE Cards SET Gender=? WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			} else {
				statement = RoleplayCore.connection.prepareStatement("INSERT INTO Cards(Player, Gender, CreationOrder, Active) VALUES('" + p.getUniqueId().toString() + "', ?, 1, 1);");
			}
			statement.setString(1, gender);
			statement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setRace(OfflinePlayer p, Race race, boolean bypass) {
		try {
			ResultSet rs = RoleplayCore.queryDB("SELECT * FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			PreparedStatement statement;
			if(rs.next()) {
				statement = RoleplayCore.connection.prepareStatement("UPDATE Cards SET Race=? WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			} else {
				statement = RoleplayCore.connection.prepareStatement("INSERT INTO Cards(Player, Race, CreationOrder, Active) VALUES('" + p.getUniqueId().toString() + "', ?, 1, 1);");
			}
			statement.setString(1, race.getName());
			statement.executeUpdate();

			if(!bypass && getAge(p) > race.getMaxAge()) {
				setAge(p, -1);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setRace(OfflinePlayer p, String race) {
		try {
			ResultSet rs = RoleplayCore.queryDB("SELECT * FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			PreparedStatement statement;
			if(rs.next()) {
				statement = RoleplayCore.connection.prepareStatement("UPDATE Cards SET Race=? WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			} else {
				statement = RoleplayCore.connection.prepareStatement("INSERT INTO Cards(Player, Race, CreationOrder, Active) VALUES('" + p.getUniqueId().toString() + "', ?, 1, 1);");
			}
			statement.setString(1, race);
			statement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static void setDescription(OfflinePlayer p, String description) {
		ResultSet rs = RoleplayCore.queryDB("SELECT * FROM Cards WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
		try {
			PreparedStatement statement;
			if(rs.next()) {
				statement = RoleplayCore.connection.prepareStatement("UPDATE Cards SET Description=? WHERE Player='" + p.getUniqueId().toString() + "' AND Active=1;");
			} else {
				statement = RoleplayCore.connection.prepareStatement("INSERT INTO Cards(Player, Description, CreationOrder, Active) VALUES('" + p.getUniqueId().toString() + "', ?, 1, 1);");
			}
			statement.setString(1, description);
			statement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
