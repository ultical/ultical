/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
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

    public static final String SELECT_STMT = "SELECT id, version, first_name as firstName, last_name as lastName, email, gender, birth_date as birthDate, dfv_number as dfvNumber, is_registered, club FROM PLAYER p LEFT JOIN DFV_PLAYER ON p.id = DFV_PLAYER.player_id LEFT JOIN UNREGISTERED_PLAYER ON p.id = UNREGISTERED_PLAYER.player_id";

    // INSERT
    @Override
    @InsertProvider(type = PlayerInsertProvider.class, method = "getInsertSql")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Player entity);

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
            @Result(column = "club", property = "club", javaType = Club.class, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    Player get(int id);

    @Override
    @Select(SELECT_STMT)
    @TypeDiscriminator(column = "is_registered", javaType = Boolean.class, jdbcType = JdbcType.BOOLEAN, cases = {
            @Case(type = DfvPlayer.class, value = "true"), @Case(type = UnregisteredPlayer.class, value = "false") })
    @Results({
            @Result(column = "club", property = "club", javaType = Club.class, one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get") ) })
    List<Player> getAll();
}
