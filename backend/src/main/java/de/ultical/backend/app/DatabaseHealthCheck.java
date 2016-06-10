package de.ultical.backend.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import javax.sql.DataSource;

import com.codahale.metrics.health.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck {

	private final DataSource dataSource;
	
	public DatabaseHealthCheck(DataSource ds) {
		this.dataSource = Objects.requireNonNull(ds);
	}
	
	@Override
	protected Result check() throws Exception {
		if (dataSource == null) {
			return Result.unhealthy("No Config present!");
		}
		Result result = null;
		try (Connection connection = this.dataSource.getConnection()){
			Statement stmt = connection.createStatement();
			ResultSet queryResult = stmt.executeQuery("SELECT 1");
			if (queryResult != null) {
				result = Result.healthy();
			} else {
				result = Result.unhealthy("Query did not provide a result");
			}
			
		} catch (SQLException e) {
			result = Result.unhealthy(e);
		}
		
		return result;
	}

}
