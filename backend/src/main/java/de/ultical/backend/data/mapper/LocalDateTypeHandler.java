/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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