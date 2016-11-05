package com.di.jdbc.template;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import com.di.jdbc.util.ConnectionUtil;
import com.di.jdbc.util.ExampleUtil;
import com.di.jdbc.util.Pager;
import com.di.jdbc.util.PagerSqlUtil;
import com.di.jdbc.util.SqlUtil;

/**
 * @author di
 */
public class JdbcSimpleMapper extends JdbcMapper {
	Connection con = null;
	Statement st = null;

	public void beginTransaction() throws SQLException {
		con = ConnectionUtil.getConn(fileName);
		con.setAutoCommit(false);
	}

	public void commit() throws SQLException {
		if (st != null) {
			st.close();
			st = null;
		}
		con.commit();
		con.setAutoCommit(true);
		ConnectionUtil.returnConn(fileName, con);
		con = null;
	}

	public void rollback() throws SQLException {
		con.rollback();
		con.setAutoCommit(true);
		ConnectionUtil.returnConn(fileName, con);
		con = null;
	}

	private Connection getCon() {
		return con == null ? ConnectionUtil.getConn(fileName) : con;
	}

	public void executeSql(String sql) throws SQLException {
		con = getCon();
		st = con.createStatement();
		st.execute(sql);
	}

	public void updateObject(Object o) throws SQLException {
		con = getCon();
		st = con.createStatement();
		st.execute(SqlUtil.getUpdateSelecitiveSql(o));
	}

	public void insertObject(Object o) throws SQLException {
		con = getCon();
		st = con.createStatement();
		st.execute(SqlUtil.getInsertSelecitiveSql(o));
	}

	public <T> void insertObjects(List<T> os) {
		Connection conn = ConnectionUtil.getConn(fileName);
		Statement s = null;
		try {
			s = conn.createStatement();
			s.execute(SqlUtil.getInsertsSql(os));
		} catch (SQLException e) {
			System.out.println(SqlUtil.getInsertsSql(os));
			e.printStackTrace();
		} finally {
			if (s != null) {
				try {
					s.close();
					s = null;
					ConnectionUtil.returnConn(fileName, conn);
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public <T> void insertMillionObjects(List<T> os, int batchSize) {
		Connection conn = ConnectionUtil.getConn(fileName);
		Statement s = null;
		try {
			s = conn.createStatement();
			int offset = 0;
			while (offset < os.size()) {
				s.addBatch(SqlUtil.getInsertsSql(
						os.subList(offset, (offset + batchSize) > os.size() ? os.size() : (offset + batchSize))));
				offset += batchSize;
			}
			s.executeBatch();
		} catch (SQLException e) {
			System.out.println(SqlUtil.getInsertsSql(os));
			e.printStackTrace();
		} finally {
			if (s != null) {
				try {
					s.close();
					s = null;
					ConnectionUtil.returnConn(fileName, conn);
					conn = null;
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void deleteObject(Object o) throws SQLException {
		con = getCon();
		st = con.createStatement();
		st.execute(SqlUtil.getDeleteSql(o));
	}

	public <T> List<T> selectByExample(Object e, Class<T> t) {
		return queryForList(ExampleUtil.selectByExample(e, t), t);
	}

	public <T> long countByExample(Object e, Class<T> t) {
		return queryForSingleValue(ExampleUtil.countByExample(e, t), long.class);
	}

	public <T> void deleteByExample(Object e, Class<T> t) {
		execute(ExampleUtil.deleteByExample(e, t));
	}

	public <T> Pager<T> queryPager(String sql, int pageNum, int pageSize, Class<T> t) {
		Pager<T> p = new Pager<>();
		String sql0 = "select count(0) " + sql.substring(sql.indexOf("from"));
		p.setPageNum(pageNum);
		p.setPageSize(pageSize);
		p.setTotal(this.queryForSingleValue(sql0, long.class));
		p.setList(queryForList(PagerSqlUtil.getPageSql(sql, pageNum, pageSize, fileName), t));
		return p;
	}
}
