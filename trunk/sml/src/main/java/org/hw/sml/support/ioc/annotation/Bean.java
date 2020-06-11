package org.hw.sml.support.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
	public static final String SCOPE_SINGLETON = "singleton";
	public static final String SCOPE_PROTOTYPE = "prototype";

	String value() default "";

	String scope() default SCOPE_SINGLETON;// [singleton,prototype]

	String profile() default "";

	int order() default 0;

	String prefix() default "";

	/**
	 * 用于config定义bean
	 */
	String initMethod() default "";

	/**
	 * 用于config定义bean
	 */
	String destoryMethod() default "";
	
	boolean conditionalOnMissingBean() default false;

	String[] conditionalOnExistsVals() default {};

	String[] conditionalOnExistsBeans() default {};

	String[] conditionOnMatchVals() default {};

	String[] conditionOnExistsPkgs() default {};

	String conditionOnMatchValMissingTrue() default "";
}
