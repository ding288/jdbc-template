package com.di.jdbc.template;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import com.di.jdbc.util.ConnectionUtil;
import com.di.jdbc.util.ExampleUtil;
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

	public void deleteObject(Object o) throws SQLException {
		con = getCon();
		st = con.createStatement();
		st.execute(SqlUtil.getDeleteSql(o));
	}

	public <T> List<T> selectByExample(Object e,Class<T> t) {
		return queryForList(ExampleUtil.selectByExample(e,t), t);
	}

	void countByExample() {}

	void deleteByExample(){}
}
