package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.api.transferClasses.DfvMvName;

public interface DfvMvNameMapper {

	final String SELECT_STMT = "SELECT dfv_number as dfvnr, first_name as vorname, last_name as nachname, dse from DFV_MV_NAME";

	@Select({ SELECT_STMT, "WHERE dfv_number = #{pk}" })
	DfvMvName get(int pk);

	@Select(SELECT_STMT)
	List<DfvMvName> getAll();

	@Insert("INSERT INTO DFV_MV_NAME (dfv_number, first_name, last_name, dse) VALUES (#{dfvnr},#{vorname},#{nachname},#{dse})")
	@Options(flushCache = true)
	Integer insert(DfvMvName entity);

	@Delete("DELETE FROM DFV_MV_NAME WHERE 1=1")
	@Options(flushCache = true)
	void deleteAll();

}
