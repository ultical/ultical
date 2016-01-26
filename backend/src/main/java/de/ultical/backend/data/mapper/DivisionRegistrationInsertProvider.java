package de.ultical.backend.data.mapper;

import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.jdbc.SQL;

import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationPlayers;
import de.ultical.backend.model.TournamentEdition;

public class DivisionRegistrationInsertProvider {

    public static String getInsertSql(final Map<String, Object> params) {
        DivisionRegistration reg = (DivisionRegistration) Objects.requireNonNull(params.get("reg"));
        TournamentEdition edition = (TournamentEdition) Objects.requireNonNull(params.get("edition"));
        Objects.requireNonNull(edition);
        final boolean is_player_registration = reg instanceof DivisionRegistrationPlayers;
        SQL sql = new SQL();
        sql.INSERT_INTO("DIVISION_REGISTRATION");
        sql.VALUES("tournament_edition", String.valueOf(edition.getId()));
        sql.VALUES("division_type", "'" + reg.getDivisionType().name() + "'");
        sql.VALUES("division_age", "'" + reg.getDivisionAge().name() + "'");
        sql.VALUES("number_of_spots", String.valueOf(reg.getNumberSpots()));
        sql.VALUES("is_player_registration", Boolean.toString(is_player_registration));
        sql.VALUES("division_identifier", "'" + reg.getDivisionIdentifier() + "'");

        return sql.toString();
    }
}
