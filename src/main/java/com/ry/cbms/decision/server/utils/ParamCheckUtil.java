package com.ry.cbms.decision.server.utils;

/**
 * @Author maoYang
 * @Date 2019/6/26 11:15
 * @Description 多参数非空检验
 */
public class ParamCheckUtil {

    public static Boolean check(Object... args) {
        for (Object obj : args) {
            if (null == obj || "".equals (obj)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

}
