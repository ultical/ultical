package de.ultical.backend.app;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.glassfish.hk2.api.Factory;

import io.dropwizard.lifecycle.Managed;

public class MyBatisManager implements Managed, Factory<SqlSession> {

	private SqlSessionFactory sessionFactory;
	private final DataSource dataSource;

	public MyBatisManager(final DataSource ds) {
		this.dataSource = Objects.requireNonNull(ds);
	}

	@Override
	public void start() throws Exception {
	    try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
		Environment iBatisEnv = new Environment("production", new JdbcTransactionFactory(), dataSource);
		XMLConfigBuilder builder = new XMLConfigBuilder(reader, "production");
		
		Configuration iBatisConfig = builder.parse();
		iBatisConfig.setEnvironment(iBatisEnv);
		
		this.sessionFactory = new SqlSessionFactoryBuilder().build(iBatisConfig);
	    } catch (IOException e) {
		throw new RuntimeException(e.getMessage());
	    }
	}

	@Override
	public void stop() throws Exception {
		// NOP we don't have to do anything here :)
	}

	@Override
	public SqlSession provide() {
		return this.sessionFactory.openSession();
	}

	@Override
	public void dispose(SqlSession instance) {
		instance.close();

	}

}
