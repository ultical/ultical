package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.model.RosterPlayer;

public interface RosterPlayerMapper {

    // SELECT
    @Select("SELECT player, DATE(date_added) as date FROM ROSTER_PLAYERS WHERE roster=#{rosterId}")
    @Results({ @Result(column = "date", property = "dateAdded"),
            @Result(column = "player", property = "player", one = @One(select = "de.ultical.backend.data.mapper.PlayerMapper.get") ) })
    List<RosterPlayer> getByRoster(int rosterId);

    // DELETE
    @Delete("DELETE FROM ROSTER_PLAYERS WHERE roster=#{rosterId} AND player=#{playerId}")
    void deletePlayer(@Param("playerId") int playerId, @Param("rosterId") int rosterId);

}