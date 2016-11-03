package com.di.jdbc.template;

import java.util.HashMap;
import java.util.List;

/**
 * @author di:
 * @date 创建时间：2016年10月22日 下午12:51:35
 * @version
 */
public abstract interface JdbcOperations {
	public List<HashMap<String, Object>> queryForMap(String sql);

	public <T> List<T> queryForList(String sql, Class<T> resultClass);

	public <T> T queryForObject(String sql, Class<T> resultClass);

	public <T> T queryForSingleValue(String sql, Class<T> resultClass);

	public boolean executeUpdate(String sql);

	public boolean executeInsert(String sql);

	public boolean updateSelective(Object o);

	public boolean insertSelective(Object o);

	public <T> List<T> prepareQuery(String sql, Class<T> resultClass, Object[] params);

	public <T> List<T> prepareNamedQuery(String sqlName, Class<T> resultClass, Object[] params);

	public void prepareInsert(Object o);

	public void prepareUpdate(Object o);

	public void prepareExecute(String sql, Object[] args);

}
