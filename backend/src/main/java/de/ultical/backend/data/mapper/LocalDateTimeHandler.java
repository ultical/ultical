package de.ultical.backend.data.mapper;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
        LocalDateTime result = this.convertToLocalDateTime(o);
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
