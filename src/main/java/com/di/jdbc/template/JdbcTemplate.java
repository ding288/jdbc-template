package com.di.jdbc.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import com.di.jdbc.template.annotation.Column;
import com.di.jdbc.template.annotation.Id;
import com.di.jdbc.template.annotation.IgnoreInsert;
import com.di.jdbc.template.annotation.IgnoreUpdate;
import com.di.jdbc.template.annotation.JoinColumn;
import com.di.jdbc.template.annotation.ManyToOne;
import com.di.jdbc.template.annotation.OneToMany;
import com.di.jdbc.template.annotation.Sql;
import com.di.jdbc.template.annotation.Sqls;
import com.di.jdbc.template.annotation.Table;
import com.di.jdbc.template.annotation.Transient;
import com.di.jdbc.util.SqlUtil;

/**
 * @author di
 */
public class JdbcTemplate implements JdbcOperations {
	private String driverClassName;
	private String username;
	private String password;
	private String url;
	private String fileName;
	private Connection con;
	private ResultSet res;
	private PreparedStatement pst;
	private Statement st;
	private long initTime;

	public JdbcTemplate() {
		fileName = "jdbc.properties";
		this.init();
	}

	public JdbcTemplate(String fileName) {
		this.fileName = fileName;
		this.init();
	}

	private void init() {
		Properties prop = new Properties();
		String path = "";
		try {
			path = JdbcTemplate.class.getClass().getResource("/").getPath() + fileName;
		} catch (NullPointerException nu) {
			try {
				path = Thread.currentThread().getContextClassLoader().getResource("").toURI().getPath();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			path = path + fileName;
		}
		try {
			prop.load(new FileInputStream(new File(path)));
		} catch (FileNotFoundException e) {
			System.err.println(path + " not found");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		driverClassName = prop.getProperty("driverClassName");
		url = prop.getProperty("url");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		if (driverClassName == null)
			System.err.println("driverClassName is null or not found");
		if (url == null)
			System.err.println("url is null or not found");
		if (username == null)
			System.err.println("username is null or not found");
		if (password == null)
			System.err.println("password is null or not found");
		try {
			Class.forName(driverClassName);
			DriverManager.setLoginTimeout(60);
			initTime = new Date().getTime();
			con = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	private void reInit() {
		closeAll();
		try {
			con = DriverManager.getConnection(url, username, password);
			initTime = new Date().getTime();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeResultSetAndStatement() {
		try {
			if (res != null && !res.isClosed())
				res.close();
			if (pst != null && !pst.isClosed())
				pst.close();
			if (st != null && !st.isClosed())
				st.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeAll() {
		try {
			if (res != null) {
				res.close();
				res = null;
			}
			if (pst != null) {
				pst.close();
				pst = null;
			}
			if (st != null) {
				st.close();
				st = null;
			}
			if (con != null) {
				con.close();
				con = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closePreviousStatement() {
		assertTimeout();
		if (con != null) {
			closeResultSetAndStatement();
		} else {
			init();
		}
	}

	private void assertTimeout() {
		int preid = (int) ((new Date().getTime() - initTime) / 1000);
		if (preid >= 60) {
			this.reInit();
		}
	}

	/**
	 * After calling this method, you must call another method to close the
	 * result set
	 * 
	 * @AnotherMethod closeResultSetAndStatement() or closeAll()
	 */
	@Deprecated
	public ResultSet createQuery(String sql) {
		closePreviousStatement();
		try {
			closePreviousStatement();
			st = con.createStatement();
			res = st.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * After calling this method, you must call another method to close the
	 * result set
	 * 
	 * @AnotherMethod closeResultSetAndStatement() or closeAll()
	 */
	@Deprecated
	public ResultSet createPrepareQuery(String sql, Object... args) {
		closePreviousStatement();
		try {
			closePreviousStatement();
			pst = con.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				pst.setObject(i + 1, args[i]);
			}
			res = pst.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 返回linkmap包装的结果
	 * 
	 * @param sql
	 * @return
	 */
	public List<HashMap<String, Object>> queryForMap(String sql) {
		List<HashMap<String, Object>> list = new ArrayList<>();
		try {
			closePreviousStatement();
			st = con.createStatement();
			res = st.executeQuery(sql);
			while (res.next()) {
				HashMap<String, Object> m = new HashMap<String, Object>();
				ResultSetMetaData rsmd = res.getMetaData();
				int columnCount = rsmd.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					String colName = rsmd.getColumnLabel(i);
					if (colName == null) {
						colName = rsmd.getColumnName(i);
					}
					m.put(colName, res.getObject(i));
				}
				list.add(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSetAndStatement();
		}
		return list;
	}

	/**
	 * 返回map包装的结果
	 * 
	 * @param sql
	 * @return
	 */
	public List<LinkedHashMap<String, Object>> queryForLinkedMap(String sql) {
		List<LinkedHashMap<String, Object>> list = new ArrayList<>();
		try {
			closePreviousStatement();
			st = con.createStatement();
			res = st.executeQuery(sql);
			while (res.next()) {
				LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
				ResultSetMetaData rsmd = res.getMetaData();
				int columnCount = rsmd.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					String colName = rsmd.getColumnLabel(i);
					if (colName == null) {
						colName = rsmd.getColumnName(i);
					}
					m.put(colName, res.getObject(i));
				}
				list.add(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSetAndStatement();
		}
		return list;
	}

	/**
	 * 查询多条记录
	 */
	@Override
	public <T> List<T> queryForList(String sql, Class<T> resultClass) {
		List<T> list = new ArrayList<>();
		try {
			closePreviousStatement();
			st = con.createStatement();
			res = st.executeQuery(sql);
			while (res.next()) {
				T obj = resultClass.newInstance();
				Field[] fs = obj.getClass().getDeclaredFields();
				for (Field f : fs) {
					f.setAccessible(true);
					String column = f.getName();
					if (f.isAnnotationPresent(Column.class)) {
						column = f.getAnnotation(Column.class).name();
					}
					SqlUtil.setFieldValue(obj, f, res, column);
				}
				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSetAndStatement();
		}
		return list;
	}

	/**
	 * 查询一条记录
	 */
	@Override
	public <T> T queryForObject(String sql, Class<T> resultClass) {
		List<T> list = queryForList(sql, resultClass);
		return list.size() > 0 ? list.get(0) : null;
	}

	/**
	 * 返回一行一列的结果
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T queryForSingleValue(String sql, Class<T> resultClass) {
		try {
			closePreviousStatement();
			st = con.createStatement();
			res = st.executeQuery(sql);
			while (res.next()) {
				return (T) SqlUtil.getResultSetTypeByClassType(resultClass, res);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 执行更新sql
	 * 
	 * @param sql
	 * @return
	 */
	public boolean executeUpdate(String sql) {
		int i = 0;
		try {
			closePreviousStatement();
			st = con.createStatement();
			i = st.executeUpdate(sql);
		} catch (Exception e) {
			System.err.println("sql: " + sql);
			e.printStackTrace();
		} finally {
			closeResultSetAndStatement();
		}
		return i > 0;
	}

	/**
	 * 同个事物内执行sql
	 * 
	 * @param sqls
	 * @return
	 */
	public boolean execute(List<String> sqls) {
		boolean b = true;
		try {
			con.setAutoCommit(false);
			st = con.createStatement();
		} catch (SQLException e2) {
			e2.printStackTrace();
		}
		try {
			for (String sql : sqls) {
				st.execute(sql);
			}
			con.commit();
		} catch (SQLException e) {
			b = false;
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		closePreviousStatement();
		return b;
	}

	public void executeBatch(List<String> sqls) {
		try {
			st = con.createStatement();
			for (String s : sqls) {
				st.addBatch(s);
			}
			st.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 执行一条sql语句
	 * 
	 * @param sql
	 * @return
	 */
	public boolean executeInsert(String sql) {
		boolean b = false;
		try {
			closePreviousStatement();
			st = con.createStatement();
			b = st.execute(sql);
		} catch (Exception e) {
			System.err.println("sql: " + sql);
			e.printStackTrace();
		} finally {
			closeResultSetAndStatement();
		}
		return b;
	}

	/**
	 * 插入一条记录，返回自动生成的主键
	 * 
	 * @param sql
	 * @return
	 */
	public Object insertWithGeneratedKey(String sql) {
		Object id = null;
		try {
			closePreviousStatement();
			st = con.createStatement();
			st.execute(sql, Statement.RETURN_GENERATED_KEYS);
			res = st.getGeneratedKeys();
			if (res.next()) {
				id = res.getObject(1);
			}
		} catch (Exception e) {
			System.err.println("sql: " + sql);
			e.printStackTrace();
		} finally {
			closeResultSetAndStatement();
		}
		return id;
	}

	public ResultSet insertWithGeneratedKeyRes(String sql) {
		try {
			closePreviousStatement();
			st = con.createStatement();
			st.execute(sql, Statement.RETURN_GENERATED_KEYS);
			res = st.getGeneratedKeys();
		} catch (Exception e) {
			System.err.println("sql: " + sql);
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 更新一条记录
	 * 
	 * @param o实体类实例
	 * @return
	 */
	public boolean update(Object o) {
		Field fs[] = o.getClass().getDeclaredFields();
		String tabName;
		if (o.getClass().isAnnotationPresent(Table.class)) {
			tabName = o.getClass().getAnnotation(Table.class).name();
		} else {
			tabName = o.getClass().getSimpleName();
		}
		StringBuilder sql = new StringBuilder("update ").append(tabName).append(" set ");
		String idName = "id";
		Object idValue = null;
		for (Field f : fs) {
			f.setAccessible(true);
			try {
				if (f.isAnnotationPresent(Id.class)) {
					idValue = f.get(o);
					if (f.isAnnotationPresent(Column.class)) {
						idName = f.getAnnotation(Column.class).name();
					} else {
						idName = f.getName();
					}
				} else if (f.isAnnotationPresent(JoinColumn.class) && f.isAnnotationPresent(ManyToOne.class)) {
				} else if (f.isAnnotationPresent(OneToMany.class)) {
				} else if (f.isAnnotationPresent(Transient.class) || f.isAnnotationPresent(IgnoreUpdate.class)) {
				} else if (f.isAnnotationPresent(Column.class)) {
					sql.append(f.getAnnotation(Column.class).name()).append("=").append(SqlUtil.setSqlValue(o, f))
							.append(",");
				} else {
					sql.append(f.getName()).append("=").append(SqlUtil.setSqlValue(o, f)).append(",");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		String sq = sql.toString();
		sql = new StringBuilder(sq.substring(0, sq.lastIndexOf(",")));
		sql.append(" where ").append(idName).append("='").append(idValue).append("'");
		executeUpdate(sql.toString());
		closeResultSetAndStatement();
		return true;
	}

	/**
	 * 可选择的更新一条记录
	 * 
	 * @param o实体类实例
	 * @return
	 */
	public boolean updateSelective(Object o) {
		Field fs[] = o.getClass().getDeclaredFields();
		String tabName;
		if (o.getClass().isAnnotationPresent(Table.class)) {
			tabName = o.getClass().getAnnotation(Table.class).name();
		} else {
			tabName = o.getClass().getSimpleName();
		}
		StringBuilder sql = new StringBuilder("update ").append(tabName).append(" set ");
		String idName = "id";
		Object idValue = null;
		for (Field f : fs) {
			f.setAccessible(true);
			try {
				if (f.get(o) == null) {
					continue;
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			try {
				if (f.isAnnotationPresent(Id.class)) {
					idValue = f.get(o);
					if (f.isAnnotationPresent(Column.class)) {
						idName = f.getAnnotation(Column.class).name();
					} else {
						idName = f.getName();
					}
				} else if (f.isAnnotationPresent(JoinColumn.class) && f.isAnnotationPresent(ManyToOne.class)) {
				} else if (f.isAnnotationPresent(OneToMany.class)) {
				} else if (f.isAnnotationPresent(Transient.class) || f.isAnnotationPresent(IgnoreUpdate.class)) {
				} else if (f.isAnnotationPresent(Column.class)) {
					sql.append(f.getAnnotation(Column.class).name()).append("=").append(SqlUtil.setSqlValue(o, f))
							.append(",");
				} else {
					sql.append(f.getName()).append("=").append(SqlUtil.setSqlValue(o, f)).append(",");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		String sq = sql.toString();
		sql = new StringBuilder(sq.substring(0, sq.lastIndexOf(",")));
		sql.append(" where ").append(idName).append("='").append(idValue).append("'");
		boolean b = executeUpdate(sql.toString());
		closeResultSetAndStatement();
		return b;
	}

	/**
	 * 可选择的插入一条记录，忽略值为空的字段
	 * 
	 * @param o实体类实例
	 * @return成功或者失败
	 */
	public boolean insertSelective(Object o) {
		Field fs[] = o.getClass().getDeclaredFields();
		String tabName;
		if (o.getClass().isAnnotationPresent(Table.class)) {
			tabName = o.getClass().getAnnotation(Table.class).name();
		} else {
			tabName = o.getClass().getSimpleName();
		}
		StringBuilder sql = new StringBuilder("insert into ").append(tabName).append(" (");
		StringBuilder s1 = new StringBuilder();
		Field idField = null;
		for (Field f : fs) {
			f.setAccessible(true);
			try {
				if (f.isAnnotationPresent(Id.class)) {
					idField = f;
				}
				if (f.get(o) == null) {
					continue;
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			try {
				if (f.isAnnotationPresent(JoinColumn.class) && f.isAnnotationPresent(ManyToOne.class)) {
				} else if (f.isAnnotationPresent(OneToMany.class)) {
				} else if (f.isAnnotationPresent(Transient.class) || f.isAnnotationPresent(IgnoreInsert.class)) {
				} else if (f.isAnnotationPresent(Column.class)) {
					s1.append(f.getAnnotation(Column.class).name()).append(",");
				} else {
					s1.append(f.getName()).append(",");
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		String sq = s1.toString();
		sql.append(sq.substring(0, sq.lastIndexOf(","))).append(")values(");
		for (Field f : fs) {
			f.setAccessible(true);
			try {
				if (f.get(o) == null) {
					continue;
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
			try {
				if (f.isAnnotationPresent(JoinColumn.class) && f.isAnnotationPresent(ManyToOne.class)) {
				} else if (f.isAnnotationPresent(OneToMany.class)) {
				} else if (f.isAnnotationPresent(Transient.class) || f.isAnnotationPresent(IgnoreInsert.class)) {
				} else if (f.isAnnotationPresent(Column.class)) {
					sql.append(SqlUtil.setSqlValue(o, f)).append(",");
				} else {
					sql.append(SqlUtil.setSqlValue(o, f)).append(",");
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		sq = sql.toString();
		sql = new StringBuilder(sq.substring(0, sq.lastIndexOf(",")));
		sql.append(")");
		boolean b = executeInsert(sql.toString());
		res = insertWithGeneratedKeyRes(sql.toString());
		try {
			if (res.next()) {
				SqlUtil.setFieldValue(o, idField, res);
			}
		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		}
		closeResultSetAndStatement();
		return b;
	}

	/**
	 * 执行PreparedStatement查询多条记录
	 * 
	 * @param sql查询语句
	 * @param resultClass结果类
	 * @param params参数
	 * @return
	 */
	public <T> List<T> prepareQuery(String sql, Class<T> resultClass, Object[] params) {
		List<T> list = new ArrayList<>();
		try {
			pst = con.prepareStatement(sql);
			for (int i = 0; i < params.length; i++) {
				pst.setObject(i + 1, params[i]);
			}
			res = pst.executeQuery();
			list = SqlUtil.resultSetToList(resultClass, res);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeResultSetAndStatement();
		return list;
	}

	/**
	 * 执行PreparedStatement查询多条记录，命名查询
	 * 
	 * @param sqlName查询名字
	 * @param resultClass结果类
	 * @param params参数
	 * @return
	 */
	public <T> List<T> prepareNamedQuery(String sqlName, Class<T> resultClass, Object[] params) {
		String s0 = null;
		List<T> list = new ArrayList<>();
		if (resultClass.isAnnotationPresent(Sqls.class)) {
			for (Sql sql : resultClass.getAnnotation(Sqls.class).sqls()) {
				if (sql.name().equals(sqlName)) {
					s0 = sql.value();
				}
			}
			if (s0 == null) {
				System.err.println(sqlName + " not found.");
				return null;
			}
			list = prepareQuery(s0, resultClass, params);
		}
		return list;
	}

	/**
	 * 执行对象插入，使用PreparedStatement执行
	 * 
	 * @param o表实体对象实例
	 */
	public void prepareInsert(Object o) {
		try {
			pst = con.prepareStatement(SqlUtil.getPrepareInsertSelecitiveSql(o));
			SqlUtil.setPrepareParams(o, pst);
			pst.execute();
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		closeResultSetAndStatement();
	}

	/**
	 * 执行对象更新，使用PreparedStatement执行
	 * 
	 * @param o表实体对象实例
	 */
	public void prepareUpdate(Object o) {
		try {
			pst = con.prepareStatement(SqlUtil.getPrepareUpdateSelecitiveSql(o));
			SqlUtil.setPrepareUpdateParams(o, pst);
			pst.execute();
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		closeResultSetAndStatement();
	}

	/**
	 * 使用PreparedStatement执行sql
	 * 
	 * @param sql
	 *            语句
	 * @param args
	 *            参数
	 */
	public void prepareExecute(String sql, Object[] args) {
		try {
			pst = con.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				pst.setObject(i + 1, args[i]);
			}
			pst.execute();
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		closePreviousStatement();
	}

	public void prepareBatchExecute(String sql, List<Object[]> args) {
		try {
			pst = con.prepareStatement(sql);
			for (int i = 0; i < args.size(); i++) {
				for (int j = 0; j < args.get(i).length; j++) {
					pst.setObject(j + 1, args.get(i)[j]);
				}
				pst.addBatch();
			}
			pst.execute();
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		closePreviousStatement();
	}
}
