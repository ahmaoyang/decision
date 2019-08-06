package com.ry.cbms.decision.server.access;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @Author maoYang
 * @Date 2019/4/26 16:01
 * @Description 限制流(设定的时间段内设置最大的访问次数)
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {
    int seconds() default 10;

    int maxCount() default 3;

    boolean needLogin() default true;
}
