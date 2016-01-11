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
    @Insert("INSERT INTO LOCATION (latitude, longitude, city, street, zip_code, country, country_code, additional_info) VALUES (#{latitude}, #{longitude}, #{city}, #{street}, #{zipCode}, #{country}, #{countryCode}, #{additionalInfo})")
    @Options(keyProperty = "id", useGeneratedKeys = true)
    Integer insert(Location entity);

    @Override
    @Update("UPDATE LOCATION SET version=version+1, latitude=#{latitude}, longitude=#{longitude}, city=#{city}, street=#{street}, zip_code=#{zipCode, jdbcType=INTEGER}, country=#{country}, country_code=#{countryCode}, additional_info=#{additionalInfo} WHERE version=#{version} AND id=#{id}")
    Integer update(Location entity);

    @Override
    @Delete("DELETE FROM LOCATION WHERE id=#{id}")
    void delete(Location entity);
}
