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

    final String SELECT_STMT = "SELECT id, version, fee_type, other_name, amount, currency FROM FEE";

    // INSERT
    @Override
    @Insert("INSERT INTO FEE (fee_type, other_name, amount, currency, event, tournament_edition) VALUES (#{type},#{otherName},#{amount},#{currency},#{event.id, jdbcType=INTEGER},#{tournamentEdition.id, jdbcType=INTEGER})")
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
            @Result(column = "fee_type", property = "type"), @Result(column = "other_name", property = "otherName"),
            @Result(column = "amount", property = "amount"), @Result(column = "currency", property = "currency") })
    Fee get(int id);

    @Select({ SELECT_STMT, "WHERE event = #{eventId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "fee_type", property = "type"), @Result(column = "other_name", property = "otherName"),
            @Result(column = "amount", property = "amount"), @Result(column = "currency", property = "currency") })
    List<Fee> getForEvent(int eventId);

    @Select({ SELECT_STMT, "WHERE tournament_edition = #{editionId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "fee_type", property = "type"), @Result(column = "other_name", property = "otherName"),
            @Result(column = "amount", property = "amount"), @Result(column = "currency", property = "currency") })
    List<Fee> getForTournamentEdition(int editionId);

}
