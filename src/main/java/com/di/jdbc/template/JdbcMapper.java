package com.di.jdbc.template;

import java.util.HashMap;
import java.util.List;

import com.di.jdbc.util.ConnectionUtil;

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
		return null;
	}

	@Override
	public <T> List<T> queryForList(String sql, Class<T> resultClass) {
		return null;
	}

	@Override
	public <T> T queryForObject(String sql, Class<T> resultClass) {
		return null;
	}

	@Override
	public <T> T queryForSingleValue(String sql, Class<T> resultClass) {
		return null;
	}

	@Override
	public boolean executeUpdate(String sql) {
		return false;
	}

	@Override
	public boolean executeInsert(String sql) {
		return false;
	}

	@Override
	public boolean updateSelective(Object o) {
		return false;
	}

	@Override
	public boolean insertSelective(Object o) {
		return false;
	}

	@Override
	public <T> List<T> prepareQuery(String sql, Class<T> resultClass, Object[] params) {
		return null;
	}

	@Override
	public <T> List<T> prepareNamedQuery(String sqlName, Class<T> resultClass, Object[] params) {
		return null;
	}

	@Override
	public void prepareInsert(Object o) {
	}

	@Override
	public void prepareUpdate(Object o) {
	}

	@Override
	public void prepareExecute(String sql, Object[] args) {
	}

}
