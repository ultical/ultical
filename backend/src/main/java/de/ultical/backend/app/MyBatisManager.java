package de.ultical.backend.app;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.glassfish.hk2.api.Factory;

import io.dropwizard.lifecycle.Managed;

public class MyBatisManager implements Managed, Factory<SqlSession> {

	private SqlSessionFactory sessionFactory;
	
	@Override
	public void start() throws Exception {
		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader("mybatis-config.xml");
			this.sessionFactory = new SqlSessionFactoryBuilder().build(reader, "production");
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void stop() throws Exception {
		//NOP we don't have to do anything here :)
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
