package de.ultical.backend.data.mapper;

import org.apache.ibatis.jdbc.SQL;

import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Player;

public class PlayerInsertProvider {

	public String getInsertSql(final Player p) {
		SQL sql = new SQL();
		sql.INSERT_INTO("PLAYER").VALUES("first_name", "'"+p.getFirstName()+"'").VALUES("last_name", "'"+p.getLastName()+"'")
				.VALUES("gender", "'"+p.getGender().name()+"'")
				.VALUES("is_registered", Boolean.valueOf(p instanceof DfvPlayer).toString());

		// VALUES("first_name, last_name, gender, is_registered",
		// String.format("%s, %s, %s, %b",
		// p.getFirstName(), p.getLastName(), p.getGender().toString(), p
		// instanceof DfvPlayer));
		return sql.toString();
	}
}
