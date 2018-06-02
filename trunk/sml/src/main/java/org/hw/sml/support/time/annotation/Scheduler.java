package org.hw.sml.support.time.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scheduler {
    String value();
    boolean triggerdCondition() default true;
    String triggerdConditionElp() default "";
    public static String min1="min1";
}
