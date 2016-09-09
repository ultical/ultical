package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.Club;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.UnregisteredPlayer;

public interface PlayerMapper extends BaseMapper<Player> {

    public static final String SELECT_STMT = "SELECT id, version, first_name as firstName, last_name as lastName, email, gender, birth_date as birthDate, dfv_number as dfvNumber, is_registered, club, eligible_until as eligibleUntil, last_modified as lastModified FROM PLAYER p LEFT JOIN DFV_PLAYER ON p.id = DFV_PLAYER.player_id LEFT JOIN UNREGISTERED_PLAYER ON p.id = UNREGISTERED_PLAYER.player_id";

    // INSERT
    @Insert({ "INSERT INTO PLAYER (first_name, last_name, gender, is_registered)",
            "VALUES (#{player.firstName, jdbcType=VARCHAR}, #{player.lastName, jdbcType=VARCHAR}, #{player.gender, jdbcType=VARCHAR}, #{isRegistered, jdbcType=BOOLEAN})" })
    @Options(keyProperty = "player.id", useGeneratedKeys = true)
    Integer insertPlayer(@Param("player") Player entity, @Param("isRegistered") boolean isRegistered);

    // UPDATE
    @Override
    @Update({
            "UPDATE PLAYER SET version=version+1, first_name=#{firstName, jdbcType=VARCHAR}, last_name=#{lastName, jdbcType=VARCHAR},",
            "gender=#{gender} WHERE id=#{id} AND version=#{version}" })
    Integer update(Player entity);

    // DELETE
    @Override
    @Delete("DELETE FROM PLAYER WHERE id=#{id}")
    void delete(Player entity);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "WHERE id=#{id}" })
    @TypeDiscriminator(column = "is_registered", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN, cases = {
            @Case(type = DfvPlayer.class, value = "true"), @Case(type = UnregisteredPlayer.class, value = "false") })
    @Results({
            @Result(column = "club", property = "club", javaType = Club.class, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")) })
    Player get(int id);

    @Override
    @Select(SELECT_STMT)
    @TypeDiscriminator(column = "is_registered", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN, cases = {
            @Case(type = DfvPlayer.class, value = "true"), @Case(type = UnregisteredPlayer.class, value = "false") })
    @Results({
            @Result(column = "club", property = "club", javaType = Club.class, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")) })
    List<Player> getAll();
}
