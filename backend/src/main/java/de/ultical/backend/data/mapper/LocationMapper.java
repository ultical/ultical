package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import de.ultical.backend.model.Location;

public interface LocationMapper extends BaseMapper<Location> {

	@Override
	@Select("SELECT id, version, latitude, longitude, city, street, zip AS zipCode, country, additional_info AS additionalInfo FROM LOCATION WHERE id=#{id}")
	Location get(int id);

	@Override
	@Select("SELECT id, version, latitude, longitude, city, street, zip AS zipCode, country, additional_info AS additionalInfo FROM LOCATION")
	List<Location> getAll();

	@Override
	@Insert("INSERT INTO LOCATION (latitude, longitude, city, street, zip, country, additional_info) VALUES (#{latitude}, #{longitude}, #{city}, #{street}, #{zipCode}, #{country}, #{additionalInfo})")
	@Options(keyProperty = "id", useGeneratedKeys = true)
	Integer insert(Location entity);

	@Override
	@Update({
			"UPDATE LOCATION SET version=version+1, latitude=#{latitude}, longitude=#{longitude}, city=#{city}, street=#{street}, zip=#{zipCode},",
			"country=#{country}, additional_info=#{additionalInfo}", "WHERE version=#{version} AND id=#{id}" })
	Integer update(Location entity);
	
	@Override
	@Delete("DELETE FROM LOCATION WHERE id=#{id}")
	void delete(Location entity);
}
