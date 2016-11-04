package com.di.jdbc.template;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import com.di.jdbc.template.annotation.Column;
import com.di.jdbc.template.annotation.Id;
import com.di.jdbc.template.annotation.IgnoreInsert;
import com.di.jdbc.template.annotation.JoinColumn;
import com.di.jdbc.template.annotation.ManyToOne;
import com.di.jdbc.template.annotation.OneToMany;
import com.di.jdbc.template.annotation.Sql;
import com.di.jdbc.template.annotation.Sqls;
import com.di.jdbc.template.annotation.Table;
import com.di.jdbc.template.annotation.Transient;
import com.di.jdbc.util.ConnectionUtil;
import com.di.jdbc.util.ResultSetUtil;
import com.di.jdbc.util.SqlUtil;

/**
 * @author di
 */
public class JdbcMapper implements JdbcOperations {
	private String fileName = "jdbc.properties";

	public JdbcMapper() {
		this.init(fileName);
	}

	public JdbcMapper(String fileName) {
		this.fileName = fileName;
		this.init(fileName);
	}

	public void init(String fileName) {
		ConnectionUtil.init(fileName);
	}

	@Override
	public List<HashMap<String, Object>> queryForMap(String sql) {
		List<HashMap<String, Object>> res = Collections.emptyList();
		Connection c = ConnectionUtil.getConn(fileName);
		Statement st = null;
		ResultSet rs = null;
		try {
			st = c.createStatement();
			rs = st.executeQuery(sql);
			res = ResultSetUtil.resultSetToMapList(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (st != null) {
					st.close();
					st = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	@Override
	public <T> List<T> queryForList(String sql, Class<T> resultClass) {
		List<T> list = new ArrayList<>();
		Connection c = ConnectionUtil.getConn(fileName);
		Statement st = null;
		ResultSet rs = null;
		try {
			st = c.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				T obj = resultClass.newInstance();
				Field[] fs = obj.getClass().getDeclaredFields();
				for (Field f : fs) {
					f.setAccessible(true);
					String column = f.getName();
					if (f.isAnnotationPresent(Column.class)) {
						column = f.getAnnotation(Column.class).name();
					}
					SqlUtil.setFieldValue(obj, f, rs, column);
				}
				list.add(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (st != null) {
					st.close();
					st = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public <T> T queryForObject(String sql, Class<T> resultClass) {
		List<T> list = queryForList(sql, resultClass);
		return list.isEmpty() ? null : list.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T queryForSingleValue(String sql, Class<T> resultClass) {
		Connection c = ConnectionUtil.getConn(fileName);
		Statement st = null;
		ResultSet rs = null;
		try {
			st = c.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				return (T) SqlUtil.getResultSetTypeByClassType(resultClass, rs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (st != null) {
					st.close();
					st = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public boolean executeUpdate(String sql) {
		int i = 0;
		Connection c = ConnectionUtil.getConn(fileName);
		Statement st = null;
		ResultSet rs = null;
		try {
			st = c.createStatement();
			i = st.executeUpdate(sql);
		} catch (Exception e) {
			System.err.println("sql: " + sql);
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (st != null) {
					st.close();
					st = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return i > 0;
	}

	@Override
	public boolean executeInsert(String sql) {
		boolean b = false;
		Connection c = ConnectionUtil.getConn(fileName);
		Statement st = null;
		ResultSet rs = null;
		try {
			st = c.createStatement();
			b = st.execute(sql);
		} catch (Exception e) {
			System.err.println("sql: " + sql);
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (st != null) {
					st.close();
					st = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return b;
	}

	@Override
	public boolean updateSelective(Object o) {
		return executeUpdate(SqlUtil.getUpdateSelecitiveSql(o));
	}

	@Override
	public boolean insertSelective(Object o) {
		Connection c = ConnectionUtil.getConn(fileName);
		Statement st = null;
		ResultSet rs = null;
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
		boolean b = false;
		try {
			st = c.createStatement();
			b = st.execute(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			rs = st.getGeneratedKeys();
			if (rs.next()) {
				SqlUtil.setFieldValue(o, idField, rs);
			}
		} catch (IllegalArgumentException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (st != null) {
					st.close();
					st = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return b;
	}

	@Override
	public <T> List<T> prepareQuery(String sql, Class<T> resultClass, Object[] params) {
		List<T> list = new ArrayList<>();
		Connection c = ConnectionUtil.getConn(fileName);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = c.prepareStatement(sql);
			if (params != null) {
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i + 1, params[i]);
				}
			}
			rs = ps.executeQuery();
			list = SqlUtil.resultSetToList(resultClass, rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
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

	@Override
	public void prepareInsert(Object o) {
		Connection c = ConnectionUtil.getConn(fileName);
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(SqlUtil.getPrepareInsertSelecitiveSql(o));
			SqlUtil.setPrepareParams(o, ps);
			ps.execute();
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
					ps = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void prepareUpdate(Object o) {
		Connection c = ConnectionUtil.getConn(fileName);
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(SqlUtil.getPrepareUpdateSelecitiveSql(o));
			SqlUtil.setPrepareUpdateParams(o, ps);
			ps.execute();
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
					ps = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void prepareExecute(String sql, Object[] args) {
		Connection c = ConnectionUtil.getConn(fileName);
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject(i + 1, args[i]);
			}
			ps.execute();
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
					ps = null;
				}
				ConnectionUtil.returnConn(fileName, c);
				c = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
