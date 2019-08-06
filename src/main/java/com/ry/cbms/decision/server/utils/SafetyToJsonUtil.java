package com.ry.cbms.decision.server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import org.springframework.util.StringUtils;

/**
 * @Author maoYang
 * @Date 2019/4/29 17:29
 * @Description 对象转string 保留null 字段
 */
public class SafetyToJsonUtil {

    public static String toJsonString(Object object) {
        String jsString = null;
        if (object != null) {
            try {
                jsString = JSON.toJSONString(object, SerializerFeature.WriteMapNullValue);
            } catch (Exception e) {
                e.toString();
            }
        }
        return jsString;
    }

    public static JSONObject toJsonObject(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new GlobalException("转换内容不能为空");
        }
        JSONObject jsonObject;
        jsonObject = JSON.parseObject(str);
        return jsonObject;
    }
}
