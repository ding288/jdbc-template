package com.di.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author di:
 * @date 创建时间：2016年10月24日 下午7:28:51
 * @version
 */
public class ConnectionPoolUtil {
	static boolean run = true;
	static Vector<Pool> pools;
	static String url;
	static String user;
	static String password;
	static int initPoolSize = 2;
	static int minPoolSize = 1;
	static int maxPoolSize = 10;
	private static String driverClassName;

	public static void init() {
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				timeoutDetection();
			}
		}, 5, 10, TimeUnit.SECONDS);
		// 隔5秒后开始执行任务，并且在上一次任务开始后隔10秒再执行一次；
	}

	public static void timeoutDetection() {
		System.out.println("start to timeout detection.");
		for (Pool p : pools) {
			if (p.status) {
				try {
					if (!p.getCon().isValid(3)) {
						reConnection(p);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void reConnection(Pool p) {
		try {
			p.getCon().close();
			Connection con = newConnetion();
			if (con != null) {
				p.setCon(con);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static Connection newConnetion() {
		try {
			Class.forName(driverClassName);
			Connection con = DriverManager.getConnection(url, user, password);
			return con;
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	synchronized Connection get() {
		for (Pool p : pools) {
			if (p.status) {
				p.setStatus(false);
				return p.getCon();
			}
		}
		throw new RuntimeException("获取连接失败");
	}

	synchronized void release(Connection con) {
		for (Pool p : pools) {
			if (p.getCon().equals(con)) {
				p.setStatus(true);
				break;
			}
		}
	}

	class Pool {
		Connection con;
		boolean status;

		public Connection getCon() {
			return con;
		}

		public void setCon(Connection con) {
			this.con = con;
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}
	}
}
