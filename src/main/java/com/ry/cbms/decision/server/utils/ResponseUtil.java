package com.ry.cbms.decision.server.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

/**
 * @author  maoyang
 */
public class ResponseUtil {

	public static void responseJson(HttpServletResponse response, int status, Object data) {
		try {
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "*");
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(status);

			response.getWriter().write(JSONObject.toJSONString(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
