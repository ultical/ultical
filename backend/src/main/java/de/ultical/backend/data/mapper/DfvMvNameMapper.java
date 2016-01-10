package de.ultical.backend.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import de.ultical.backend.api.transferClasses.DfvMvName;

public interface DfvMvNameMapper {

    final String SELECT_STMT = "SELECT dfv_number as dfvNumber, first_name as firstName, last_name as lastName, dse from DFV_MV_NAME";

    // INSERT
    @Insert("INSERT INTO DFV_MV_NAME (dfv_number, first_name, last_name, dse) VALUES (#{dfvNumber},#{firstName},#{lastName},#{dse})")
    @Options(flushCache = true)
    Integer insert(DfvMvName entity);

    // DELETE
    @Delete("DELETE FROM DFV_MV_NAME WHERE 1=1")
    @Options(flushCache = true)
    void deleteAll();

    // SELECT
    @Select({ SELECT_STMT, "WHERE dfv_number = #{pk}" })
    DfvMvName get(int pk);

    @Select(SELECT_STMT)
    List<DfvMvName> getAll();

    @Select({ SELECT_STMT, "WHERE first_name = #{firstname} AND last_name = #{lastname}" })
    List<DfvMvName> getByName(@Param("firstname") String firstname, @Param("lastname") String lastname);

    @Select({ SELECT_STMT, "WHERE dse=1 AND CONCAT(first_name, ' ', last_name) LIKE #{namePart}" })
    List<DfvMvName> find(final String namePart);

}
