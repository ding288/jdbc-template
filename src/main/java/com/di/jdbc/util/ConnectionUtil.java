package com.di.jdbc.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.di.jdbc.connection.ConnectionPool;

/**
 * @author di
 */
public class ConnectionUtil {
	static int INTERVAL = 30;
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
		startRepair();
	}

	public static void startRepair() {
		if (!TimeoutThread.run.get()) {
			ScheduledExecutorService es=new ScheduledThreadPoolExecutor(1);
			es.scheduleAtFixedRate(new Runnable() {				
				@Override
				public void run() {
					repairTimeout();
				}
			},5, INTERVAL,TimeUnit.SECONDS);
		}
	}

	public static void stopRepair() {
		TimeoutThread.run.set(false);	
	}

	public static void repairTimeout() {
		for (String key : pools.keySet()) {
			ConnectionPool cp = pools.get(key);
			try {
				cp.refreshConnections();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static ConnectionPool createConn(String fileName) {
		JdbcConfig c = new JdbcConfig(fileName);
		ConnectionPool cp = new ConnectionPool(c.getDriverClassName(), c.getUrl(), c.getUsername(), c.getPassword(),
				c.getInitPoolSize(), c.getMaxPoolSize());
		try {
			cp.createPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
		pools.put(fileName, cp);
		return cp;
	}
	public static void returnConn(String fileName,Connection conn){
		ConnectionPool po=pools.get(fileName);
		po.returnConnection(conn);
	}
	public static Connection getConn(String fileName) {
		ConnectionPool po;
		if (pools.containsKey(fileName)) {
			po = pools.get(fileName);
		} else {
			po = createConn(fileName);
			init(fileName);
		}
		try {
			return po.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}
