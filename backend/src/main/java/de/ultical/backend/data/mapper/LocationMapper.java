package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import de.ultical.backend.model.Location;

public interface LocationMapper extends BaseMapper<Location> {

    @Override
    @Select("SELECT id, version, latitude, longitude, city, street, zip_code AS zipCode, country, country_code AS countryCode, additional_info AS additionalInfo FROM LOCATION WHERE id=#{id}")
    Location get(int id);

    @Override
    @Select("SELECT id, version, latitude, longitude, city, street, zip_code AS zipCode, country, country_code AS countryCode, additional_info AS additionalInfo FROM LOCATION")
    List<Location> getAll();

    @Override
    @Insert("INSERT INTO LOCATION (latitude, longitude, city, street, zip_code, country, country_code, additional_info) VALUES (#{latitude, jdbcType=DOUBLE}, #{longitude, jdbcType=DOUBLE}, #{city, jdbcType=VARCHAR}, #{street, jdbcType=VARCHAR}, #{zipCode, jdbcType=INTEGER}, #{country, jdbcType=VARCHAR}, #{countryCode, jdbcType=VARCHAR}, #{additionalInfo, jdbcType=CLOB})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Location entity);

    @Override
    @Update("UPDATE LOCATION SET version=version+1, latitude=#{latitude, jdbcType=DOUBLE}, longitude=#{longitude, jdbcType=DOUBLE}, city=#{city, jdbcType=VARCHAR}, street=#{street, jdbcType=VARCHAR}, zip_code=#{zipCode, jdbcType=INTEGER}, country=#{country, jdbcType=VARCHAR}, country_code=#{countryCode, jdbcType=VARCHAR}, additional_info=#{additionalInfo, jdbcType=CLOB} WHERE version=#{version} AND id=#{id}")
    Integer update(Location entity);

    @Override
    @Delete("DELETE FROM LOCATION WHERE id=#{id}")
    void delete(Location entity);
}
