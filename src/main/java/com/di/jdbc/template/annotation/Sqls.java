package com.di.jdbc.template.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
* @author  di: 
* @date 创建时间：2016年10月22日 下午2:53:44 
* @version
*/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sqls {
	Sql[] sqls();
}
