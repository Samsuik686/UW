package com.jimi.uw_server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 标记该注解方法需要指定用户才可以访问
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Access {

	/**
	 * 提供一组TypeName，不属于组内用户类型的用户无法访问被标记的方法
	 */
	String[] value();
	
}
