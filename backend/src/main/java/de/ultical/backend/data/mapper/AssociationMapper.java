package de.ultical.backend.data.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import de.ultical.backend.model.Association;
import de.ultical.backend.model.TournamentFormat;
import de.ultical.backend.model.User;

public interface AssociationMapper extends BaseMapper<Association> {

    // INSERT
    @Override
    @Insert("INSERT INTO ASSOCIATION (id, name, acronym, contact) VALUES (#{id, jdbcType=INTEGER},#{name, jdbcType=VARCHAR},#{acronym, jdbcType=VARCHAR},#{contact.id, jdbcType=INTEGER})")
    Integer insert(Association entity);

    @Insert("INSERT INTO ASSOCIATION_ULTICAL_USERS (association, admin) VALUES (#{association.id},#{user.id})")
    public Integer insertAdmin(@Param("association") TournamentFormat association, @Param("user") User user);

    // UPDATE
    @Override
    @Update("UPDATE ASSOCIATION SET name=#{name, jdbcType=VARCHAR}, acronym=#{acronym, jdbcType=VARCHAR}, contact=#{contact.id, jdbcType=INTEGER} WHERE id=#{id}")
    Integer update(Association entity);

    @Update("UPDATE ASSOCIATION SET name=#{name, jdbcType=VARCHAR} WHERE id=#{id}")
    Integer updateBasics(Association entity);

    // SELECT
    @Override
    @Select({ "SELECT id, name, acronym, contact FROM ASSOCIATION", "WHERE id=#{id}" })
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForAssociation") ),
            @Result(column = "contact", property = "contact", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ) })
    Association get(@Param("id") int id);

    @Override
    @Select("SELECT id, name, contact FROM ASSOCIATION")
    @Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, javaType = Integer.class),
            @Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "acronym", property = "acronym", jdbcType = JdbcType.VARCHAR, javaType = String.class),
            @Result(column = "id", property = "admins", many = @Many(select = "de.ultical.backend.data.mapper.UserMapper.getAdminsForAssociation") ),
            @Result(column = "contact", property = "contact", one = @One(select = "de.ultical.backend.data.mapper.ContactMapper.get") ) })
    List<Association> getAll();

    @Select({ "SELECT id FROM ASSOCIATION" })
    Set<Integer> getAllIds();
}
