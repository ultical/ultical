package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.model.Fee;

public interface FeeMapper extends BaseMapper<Fee> {

    final String SELECT_STMT = "SELECT fee_type, alternative_name, amount, currency FROM FEE";

    // INSERT
    @Override
    @Insert("INSERT INTO FEE (fee_type, alternative_name, amount, currency) VALUES (#{type},#{alternativeName},#{amount},#{currency})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Fee entity);

    // DELETE
    @Override
    @Delete("DELETE FROM FEE WHERE id=#{id}")
    void delete(int id);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "fee_type", property = "type"),
            @Result(column = "alternative_name", property = "alternativeName"),
            @Result(column = "amount", property = "amount"), @Result(column = "currency", property = "currency") })
    Fee get(int id);

    @Select({ SELECT_STMT, "LEFT JOIN FEE_EVENT fe ON fe.fee = FEE.id WHERE fe.event = #{eventId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "fee_type", property = "type"),
            @Result(column = "alternative_name", property = "alternativeName"),
            @Result(column = "amount", property = "amount"), @Result(column = "currency", property = "currency") })
    List<Fee> getForEvent(int eventId);

    @Select({ SELECT_STMT,
            "LEFT JOIN FEE_TOURNAMENT_EDITION fte ON fte.fee = FEE.id WHERE fte.tournament_edition = #{editionId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "fee_type", property = "type"),
            @Result(column = "alternative_name", property = "alternativeName"),
            @Result(column = "amount", property = "amount"), @Result(column = "currency", property = "currency") })
    List<Fee> getForTournamentEdition(int editionId);

}
