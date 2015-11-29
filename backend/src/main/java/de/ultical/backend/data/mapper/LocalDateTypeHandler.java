package de.ultical.backend.data.mapper;

import java.sql.*;
import java.time.LocalDate;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class LocalDateTypeHandler implements TypeHandler<LocalDate> {

	public LocalDateTypeHandler() {
	}
	
	@Override
	public void setParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType)
			throws SQLException {
		StringBuilder result = new StringBuilder(10);
		result.append(parameter.getYear()).append('-').append(parameter.getMonthValue()).append('-')
				.append(parameter.getDayOfMonth());
		ps.setString(i, result.toString());
	}

	@Override
	public LocalDate getResult(ResultSet rs, String columnName) throws SQLException {
		final Date result = rs.getDate(columnName);
		LocalDate returnValue = convertSqlToLocalDate(result);
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
		return convertSqlToLocalDate(sqlDate);
	}

	@Override
	public LocalDate getResult(CallableStatement cs, int columnIndex) throws SQLException {
		final Object o = cs.getObject(columnIndex);
		if (o instanceof Date) {
			return convertSqlToLocalDate((Date)o);
		}
		return null;
	}

}