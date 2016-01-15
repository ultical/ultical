package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.Club;
import de.ultical.backend.model.DfvPlayer;

public interface DfvPlayerMapper extends BaseMapper<DfvPlayer> {

    final String SELECT_STMT = "SELECT id, version, dfv_number as dfvNumber, first_name as firstName, last_name as lastName, gender, birth_date as birthDate, club FROM PLAYER INNER JOIN DFV_PLAYER ON PLAYER.id = DFV_PLAYER.player_id";

    // INSERT
    @Override
    @Insert("INSERT INTO DFV_PLAYER (player_id, dfv_number, birth_date, club) VALUES (#{id},#{dfvNumber, jdbcType=VARCHAR},#{birthDate},#{club.id, jdbcType=INTEGER})")
    Integer insert(DfvPlayer entity);

    // UPDATE
    @Override
    @Update("UPDATE DFV_PLAYER SET dfv_number = #{dfvNumber}, birth_date = #{birthDate}, club = #{club.id, jdbcType=INTEGER} WHERE player_id = #{id}")
    Integer update(DfvPlayer entity);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "WHERE id = #{pk} AND is_registered=true" })
    @Results({
            @Result(column = "club", property = "club", javaType = Club.class, jdbcType = JdbcType.BIGINT, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    DfvPlayer get(int pk);

    @Override
    @Select(SELECT_STMT)
    @Results({
            @Result(column = "club", property = "club", javaType = Club.class, jdbcType = JdbcType.BIGINT, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    List<DfvPlayer> getAll();

    @Select({ SELECT_STMT, "WHERE dfv_number = #{dfvNumber} AND is_registered=true" })
    @Results({
            @Result(column = "club", property = "club", javaType = Club.class, jdbcType = JdbcType.BIGINT, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    DfvPlayer getByDfvNumber(int dfvNumber);
}
