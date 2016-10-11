package de.ultical.backend.utils.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.jvm.DerbyConnection;
import liquibase.logging.LogLevel;
import liquibase.resource.ClassLoaderResourceAccessor;

public class PrepareDBRule implements TestRule {

	private static final String DEFAULT_MYBATIS_ENV = "inmemory-test";
	private static final String DEFAULT_MYBATIS_CONFIG = "mybatis-config.xml";
	private static final String DEFAULT_CHANGELOG = "database/db.changelog-1.0.xml";
	private SqlSessionFactory sessionFactory;
	private final String changeLogFile;
	private final String myBatisConfigFile;
	private final String myBatisEnvironment;
	private SqlSession session;

	public PrepareDBRule() {
		this(DEFAULT_CHANGELOG, DEFAULT_MYBATIS_CONFIG, DEFAULT_MYBATIS_ENV);
	}

	public PrepareDBRule(final String changeLog, final String myBatisConf, final String myBatisEnv) {
		this.changeLogFile = Objects.requireNonNull(changeLog);
		this.myBatisConfigFile = Objects.requireNonNull(myBatisConf);
		this.myBatisEnvironment = Objects.requireNonNull(myBatisEnv);
	}

	private class PrepareDBStatement extends Statement {

		final Statement chainedStatement;

		public PrepareDBStatement(final Statement orig) {
			this.chainedStatement = Objects.requireNonNull(orig);
		}

		@Override
		public void evaluate() throws Throwable {
			Liquibase liquibase = null;
			try {
				DriverManager.registerDriver(new EmbeddedDriver());
				Connection dbCon = DriverManager.getConnection("jdbc:derby:memory:test;create=true");
				final DerbyDatabase derbyDatabase = new DerbyDatabase();
				final DatabaseConnection liquibaseConnection = new DerbyConnection(dbCon);
				derbyDatabase.setConnection(liquibaseConnection);
				liquibase = new Liquibase(PrepareDBRule.this.changeLogFile, new ClassLoaderResourceAccessor(),
						derbyDatabase);
				liquibase.getLog().setLogLevel(LogLevel.WARNING);
				liquibase.update((Contexts) null);
			} finally {

			}

			/*
			 * setting up mybatis using the dbconnection defined above.
			 */
			PrepareDBRule.this.sessionFactory = new SqlSessionFactoryBuilder().build(
					Resources.getResourceAsReader(PrepareDBRule.this.myBatisConfigFile),
					PrepareDBRule.this.myBatisEnvironment);

			this.chainedStatement.evaluate();

			try {
				/*
				 * we drop the db again in order to clean up for other tests running in the same vm.
				 */
				DriverManager.getConnection("jdbc:derby:memory:test;drop=true");
			} catch (SQLException dropEx) {
				/*
				 * derby per specification throws an SQLExcpetion upon success of the drop-operation
				 * At least the SQLState is known and hence we can mask this exception.
				 */
				if (!"08006".equals(dropEx.getSQLState())) {
					throw dropEx;
				}
			}
		}
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new PrepareDBStatement(base);
	}

	public SqlSession getSession() {
		if (session == null) {
			session = this.sessionFactory.openSession();
		}
		return session;
	}

	public void closeSession() {
		if (session != null) {
			session.close();
			session = null;
		}
	}
}
