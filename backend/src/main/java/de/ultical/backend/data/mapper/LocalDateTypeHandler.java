package de.ultical.backend.data.mapper;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class LocalDateTypeHandler implements TypeHandler<LocalDate> {

    public LocalDateTypeHandler() {
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            StringBuilder result = new StringBuilder(10);
            result.append(parameter.getYear()).append('-').append(parameter.getMonthValue()).append('-')
                    .append(parameter.getDayOfMonth());
            ps.setString(i, result.toString());
        } else {
            ps.setNull(i, Types.DATE);
        }
    }

    @Override
    public LocalDate getResult(ResultSet rs, String columnName) throws SQLException {
        final Date result = rs.getDate(columnName);
        LocalDate returnValue = this.convertSqlToLocalDate(result);
        return returnValue;
    }

    private LocalDate convertSqlToLocalDate(final Date result) {
        LocalDate returnValue = null;
        if (result != null) {
            returnValue = result.toLocalDate();
        }
        return returnValue;
    }

    @Override
    public LocalDate getResult(ResultSet rs, int columnIndex) throws SQLException {
        final Date sqlDate = rs.getDate(columnIndex);
        return this.convertSqlToLocalDate(sqlDate);
    }

    @Override
    public LocalDate getResult(CallableStatement cs, int columnIndex) throws SQLException {
        final Object o = cs.getObject(columnIndex);
        if (o instanceof Date) {
            return this.convertSqlToLocalDate((Date) o);
        }
        return null;
    }
}
