package com.di.jdbc.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.di.jdbc.connection.ConnectionPool;

/**
 * @author di
 */
public class ConnectionUtil {
	static Map<String, ConnectionPool> pools;
	static {
		if (pools == null) {
			pools = new HashMap<String, ConnectionPool>();
		}
	}

	public static void init(String fileName) {
		if (!pools.containsKey(fileName)) {
			ConnectionPool po = createConn(fileName);
			pools.put(fileName, po);
		}
	}

	public static ConnectionPool createConn(String fileName) {
		JdbcConfig c = new JdbcConfig(fileName);
		ConnectionPool cp = new ConnectionPool(c.getDriverClassName(), c.getUrl(), c.getUsername(), c.getPassword(),
				c.getInitPoolSize(), c.getMaxPoolSize());
		pools.put(fileName, cp);
		return cp;
	}

	public static Connection getConn(String fileName) {
		ConnectionPool po;
		if (pools.containsKey(fileName)) {
			po = pools.get(fileName);
		} else {
			po = createConn(fileName);
		}
		try {
			return po.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
