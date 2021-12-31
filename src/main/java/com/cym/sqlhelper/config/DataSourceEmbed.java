package com.cym.sqlhelper.config;

import javax.sql.DataSource;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import com.cym.config.HomeConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class DataSourceEmbed {
	@Inject
	HomeConfig homeConfig;
	@Inject("${database.type}")
	String databaseType;
	@Inject("${database.url}")
	String url;
	@Inject("${database.username}")
	String username;
	@Inject("${database.password}")
	String password;

	DataSource dataSource;

	@Init(index = 10)
	public void init() {
		if (databaseType.equalsIgnoreCase("sqlite") || databaseType.equalsIgnoreCase("h2")) {
			String dbPath = homeConfig.home + "h2";
			HikariConfig dbConfig = new HikariConfig();
			dbConfig.setJdbcUrl(("jdbc:h2:" + dbPath));
			dbConfig.setUsername("sa");
			dbConfig.setPassword("");
			dbConfig.setMaximumPoolSize(1);
			dataSource = new HikariDataSource(dbConfig);
		} else if (databaseType.equalsIgnoreCase("mysql")) {
			HikariConfig dbConfig = new HikariConfig();
			dbConfig.setJdbcUrl(url);
			dbConfig.setUsername(username);
			dbConfig.setPassword(password);
			dbConfig.setMaximumPoolSize(1);
			dataSource = new HikariDataSource(dbConfig);
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

}
