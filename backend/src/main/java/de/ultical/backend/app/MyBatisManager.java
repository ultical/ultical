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
package de.ultical.backend.app;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
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
		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader("mybatis-config.xml");
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
