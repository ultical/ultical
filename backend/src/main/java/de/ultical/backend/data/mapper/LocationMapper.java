package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Location;

public interface LocationMapper extends BaseMapper<Location> {

    final String SELECT_STMT = "SELECT id, version, title, is_main as main, latitude, longitude, city, street, zip_code AS zipCode, country, country_code AS countryCode, additional_info AS additionalInfo";

    // INSERT
    @Override
    @Insert("INSERT INTO LOCATION (title, is_main, latitude, longitude, city, street, zip_code, country, country_code, additional_info) VALUES (#{title, jdbcType=VARCHAR}, #{main, jdbcType=BOOLEAN}, #{latitude, jdbcType=DOUBLE}, #{longitude, jdbcType=DOUBLE}, #{city, jdbcType=VARCHAR}, #{street, jdbcType=VARCHAR}, #{zipCode, jdbcType=VARCHAR}, #{country, jdbcType=VARCHAR}, #{countryCode, jdbcType=VARCHAR}, #{additionalInfo, jdbcType=VARCHAR})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Location entity);

    @Insert("INSERT INTO EVENT_LOCATION (event, location) VALUES (#{eventId}, #{locationId})")
    Integer addToEvent(@Param("eventId") int eventId, @Param("locationId") int locationId);

    // UPDATE
    @Override
    @Update("UPDATE LOCATION SET version=version+1, title=#{title, jdbcType=VARCHAR}, is_main=#{main, jdbcType=BOOLEAN}, latitude=#{latitude, jdbcType=DOUBLE}, longitude=#{longitude, jdbcType=DOUBLE}, city=#{city, jdbcType=VARCHAR}, street=#{street, jdbcType=VARCHAR}, zip_code=#{zipCode, jdbcType=VARCHAR}, country=#{country, jdbcType=VARCHAR}, country_code=#{countryCode, jdbcType=VARCHAR}, additional_info=#{additionalInfo, jdbcType=CLOB} WHERE version=#{version} AND id=#{id}")
    Integer update(Location entity);

    // DELETE
    @Override
    @Delete("DELETE FROM LOCATION WHERE id=#{id}")
    void delete(Location entity);

    // SELECT
    @Override
    @Select({ SELECT_STMT, "FROM LOCATION WHERE id=#{id}" })
    Location get(int id);

    @Select({ SELECT_STMT,
            "FROM EVENT_LOCATION el LEFT JOIN LOCATION on el.location = LOCATION.id WHERE el.event=#{eventId}" })
    List<Location> getForEvent(int eventId);

    @Override
    @Select({ SELECT_STMT, "FROM LOCATION" })
    List<Location> getAll();

}
