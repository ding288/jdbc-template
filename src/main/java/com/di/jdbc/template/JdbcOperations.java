package com.di.jdbc.template;

import java.util.List;

/**
 * @author di:
 * @date 创建时间：2016年10月22日 下午12:51:35
 * @version
 */
public abstract interface JdbcOperations {
	public abstract <T> List<T> queryForList(String sql, Class<T> resultClass);

	public abstract <T> T queryForObject(String sql, Class<T> resultClass);

	public abstract <T> T queryForSingleValue(String sql, Class<T> resultClass);
}
