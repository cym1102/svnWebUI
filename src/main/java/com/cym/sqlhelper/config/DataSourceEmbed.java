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

	HikariDataSource dataSource;

	@Init
	public void init() {
		if (databaseType.equalsIgnoreCase("sqlite") || databaseType.equalsIgnoreCase("h2")) {
			
			// 建立新的sqlite数据源
			HikariConfig dbConfig = new HikariConfig();
			dbConfig.setJdbcUrl(("jdbc:sqlite:" + homeConfig.home + "sqlite.db"));
			dbConfig.setUsername("");
			dbConfig.setPassword("");
			dbConfig.setMaximumPoolSize(1);
			dbConfig.setDriverClassName("org.sqlite.JDBC");
			dataSource = new HikariDataSource(dbConfig);
		} else if (databaseType.equalsIgnoreCase("mysql")) {
			HikariConfig dbConfig = new HikariConfig();
			dbConfig.setJdbcUrl(url);
			dbConfig.setUsername(username);
			dbConfig.setPassword(password);
			dbConfig.setMaximumPoolSize(1);
			dbConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
			dataSource = new HikariDataSource(dbConfig);
		}
	}

	public HikariDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(HikariDataSource dataSource) {
		this.dataSource = dataSource;
	}

}
