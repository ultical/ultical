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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDateTimeHandler implements TypeHandler<LocalDateTime> {

	private final static Logger LOGGER = LoggerFactory.getLogger(LocalDateTimeHandler.class);

	@Override
	public void setParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType)
			throws SQLException {
		if (ps != null) {
			if (parameter != null) {
				switch (jdbcType) {

				case DATE:
					Date date = Date.valueOf(parameter.toLocalDate());
					ps.setDate(i, date);
					break;
				case VARCHAR:
					String timeStampString = parameter.toString();
					ps.setString(i, timeStampString);
					break;
				case TIMESTAMP:
				default:
					Timestamp timeStamp = Timestamp.valueOf(parameter);
					ps.setTimestamp(i, timeStamp);
					break;
				}
			} else {
				ps.setTimestamp(i, null);
			}
		}
	}

	@Override
	public LocalDateTime getResult(ResultSet rs, String columnName) throws SQLException {
		Object o = rs.getObject(columnName);
		LocalDateTime result = convertToLocalDateTime(o);
		return result;
	}

	private LocalDateTime convertToLocalDateTime(Object o) {
		LocalDateTime result = null;
		if (o instanceof Timestamp) {
			result = ((Timestamp) o).toLocalDateTime();
		} else if (o instanceof Date) {
			result = LocalDateTime.of(((Date) o).toLocalDate(), LocalTime.MIDNIGHT);
		} else if (o != null) {
			// we did not find a proper type thus we try it brute-force over the
			// object's toString()
			try {
				result = LocalDateTime.parse(o.toString());
			} catch (DateTimeParseException dtpe) {
				// NOP result remains null in this case.
				LOGGER.warn(
						String.format("failed to convert object(%s) to a proper LocalDateTime instance", o.toString()),
						dtpe);
			}
		}
		return result;
	}

	@Override
	public LocalDateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
		Object o = rs.getObject(columnIndex);
		LocalDateTime result = this.convertToLocalDateTime(o);
		return result;
	}

	@Override
	public LocalDateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
		Object o = cs.getObject(columnIndex);
		final LocalDateTime result = this.convertToLocalDateTime(o);
		return result;
	}

}
