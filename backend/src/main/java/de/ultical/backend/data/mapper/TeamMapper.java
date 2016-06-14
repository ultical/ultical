package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Team;
import de.ultical.backend.model.User;

public interface TeamMapper extends BaseMapper<Team> {

    // INSERT
    @Override
    @Insert("INSERT INTO TEAM (name, description, founding_date, location, emails, url, contact_email, twitter_name, facebook_url, club) VALUES (#{name, jdbcType=VARCHAR}, #{description, jdbcType=VARCHAR}, #{foundingDate, jdbcType=DATE}, #{location.id, jdbcType=INTEGER}, #{emails, jdbcType=VARCHAR}, #{url, jdbcType=VARCHAR}, #{contactEmail, jdbcType=VARCHAR}, #{twitterName, jdbcType=VARCHAR}, #{facebookUrl, jdbcType=VARCHAR}, #{club.id, jdbcType=INTEGER} )")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Team team);

    @Insert("INSERT INTO TEAM_ULTICAL_USERS (team, admin) VALUES (#{team.id}, #{admin.id})")
    void addAdmin(@Param("team") Team team, @Param("admin") User admin);

    // UPDATE
    @Override
    @Update("UPDATE TEAM SET version=version+1, name=#{name}, description=#{description, jdbcType=VARCHAR}, founding_date=#{foundingDate, jdbcType=INTEGER}, location=#{location.id, jdbcType=INTEGER}, emails=#{emails, jdbcType=VARCHAR}, url=#{url, jdbcType=VARCHAR}, contact_email=#{contactEmail, jdbcType=VARCHAR}, twitter_name=#{twitterName, jdbcType=VARCHAR}, facebook_url=#{facebookUrl, jdbcType=VARCHAR}, club=#{club.id, jdbcType=INTEGER} WHERE version=#{version} AND id=#{id}")
    Integer update(Team t);

    // DELETE
    @Override
    @Delete("DELETE FROM TEAM WHERE id=#{team.id}")
    void delete(Team team);

    @Delete("DELETE FROM TEAM_ULTICAL_USERS WHERE team = #{team.id} AND admin = #{admin.id}")
    void removeAdmin(@Param("team") Team team, @Param("admin") User admin);

    @Delete("DELETE FROM TEAM_ULTICAL_USERS WHERE team = #{team.id}")
    void removeAllAdmins(@Param("team") Team team);

    // SELECT
    @Select("SELECT id, version, name, founding_date, location, url, contact_email, twitter_name, facebook_url, club FROM TEAM")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "url", property = "url"), @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "twitter_name", property = "twitterName"),
            @Result(column = "facebook_url", property = "facebookUrl"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get")) })
    List<Team> getBasics();

    @Select({ "SELECT id, version, name, founding_date, location, url, contact_email, twitter_name, facebook_url, club",
            "FROM TEAM t", "LEFT JOIN TEAM_ULTICAL_USERS tuc ON tuc.team = t.id", "WHERE tuc.admin = #{userId}" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "name", property = "name"), @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "url", property = "url"), @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "twitter_name", property = "twitterName"),
            @Result(column = "facebook_url", property = "facebookUrl"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get")) })
    List<Team> getBasicsByUser(int userId);

    @Override
    @Select("SELECT * FROM TEAM WHERE id=#{id}")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "emails", property = "emails"), @Result(column = "url", property = "url"),
            @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "twitter_name", property = "twitterName"),
            @Result(column = "facebook_url", property = "facebookUrl"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get")),
            @Result(column = "id", property = "rosters", many = @Many(select = "de.ultical.backend.data.mapper.RosterMapper.getForTeam")),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam")) })
    Team get(int id);

    @Select("SELECT * FROM TEAM WHERE id=#{id}")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "emails", property = "emails"), @Result(column = "url", property = "url"),
            @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "twitter_name", property = "twitterName"),
            @Result(column = "facebook_url", property = "facebookUrl"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get")),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam")) })
    Team getForTeamRegistration(int id);

    @Select("SELECT * FROM TEAM WHERE id=#{id}")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "emails", property = "emails"), @Result(column = "url", property = "url"),
            @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "twitter_name", property = "twitterName"),
            @Result(column = "facebook_url", property = "facebookUrl"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get")),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam")) })
    Team getForRoster(int id);

    @Override
    @Select("SELECT * FROM TEAM")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "emails", property = "emails"), @Result(column = "url", property = "url"),
            @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "twitter_name", property = "twitterName"),
            @Result(column = "facebook_url", property = "facebookUrl"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get")),
            @Result(column = "id", property = "rosters", many = @Many(select = "de.ultical.backend.data.mapper.RosterMapper.getForTeam")),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam")) })
    List<Team> getAll();

    // returns all teams the corresponding user is admin of
    @Select("SELECT * FROM TEAM t LEFT JOIN TEAM_ULTICAL_USERS tuc ON tuc.team = t.id WHERE tuc.admin = #{userId}")
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "description", property = "description"), @Result(column = "name", property = "name"),
            @Result(column = "founding_date", property = "foundingDate"),
            @Result(column = "emails", property = "emails"), @Result(column = "url", property = "url"),
            @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "twitter_name", property = "twitterName"),
            @Result(column = "facebook_url", property = "facebookUrl"),
            @Result(column = "club", property = "club", one = @One(select = "de.ultical.backend.data.mapper.ClubMapper.get")),
            @Result(column = "location", property = "location", one = @One(select = "de.ultical.backend.data.mapper.LocationMapper.get")),
            @Result(column = "id", property = "rosters", many = @Many(select = "de.ultical.backend.data.mapper.RosterMapper.getForTeam")),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam")) })
    List<Team> getByUser(int userId);

    // returns a team with the given name
    @Select("SELECT id FROM TEAM WHERE name = #{teamName}")
    @Results({ @Result(column = "id", property = "id") })
    Team getByName(String teamName);

    // get all teams that participate in the specified division of the specified
    // edition with the specified participation status
    @Select({ "<script>", "SELECT * FROM TEAM t", "LEFT JOIN ROSTER r ON r.team = t.id",
            "LEFT JOIN TEAM_REGISTRATION tr ON tr.roster = r.id",
            "LEFT JOIN DIVISION_REGISTRATION dr ON tr.division_registration = dr.id",
            "LEFT JOIN TOURNAMENT_EDITION te ON te.id = dr.tournament_edition", "WHERE tr.status IN ",
            "<foreach item='status' collection='statusList' open='(' separator=',' close=')'>", "#{status}",
            "</foreach>", "AND dr.id IN ",
            "<foreach item='division' collection='divisionList' open='(' separator=',' close=')'>", "#{division.id}",
            "</foreach>", "AND te.id = #{editionId}", "</script>" })
    @Results({ @Result(column = "id", property = "id"), @Result(column = "version", property = "version"),
            @Result(column = "emails", property = "emails"), @Result(column = "url", property = "url"),
            @Result(column = "contact_email", property = "contactEmail"),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForTeam")) })
    List<Team> getByEditionDivisionStatus(@Param("editionId") Integer editionId,
            @Param("divisionList") List<Integer> divisionList, @Param("statusList") List<String> statusList);
}
