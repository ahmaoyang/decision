package com.ry.cbms.decision.server.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @Author maoYang
 * @Date 2019/5/6 11:08
 * @Description
 */
public class WriteResponseUtil {

    private static final Logger logger = LoggerFactory.getLogger(WriteResponseUtil.class);

    public static void writeNotLoginMessageToResponse(HttpServletResponse response){
        try {
            AuthError authError = new AuthError();
            authError.setStatus(Constants.NOT_LOGIN);
            authError.setTitle(Constants.NOT_LOGIN_MESSAGE);
            String content = JSONObject.toJSONString(authError);
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(content);
        }catch (Exception e){
            if (logger.isErrorEnabled()){
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void render(HttpServletResponse response, CodeMsg cm) throws Exception {
        AuthError authError = new AuthError();
        authError.setStatus(cm.getCode());
        authError.setTitle(cm.getMsg());
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(authError);
        out.write(str.getBytes("UTF-8"));
        if (null != out) {
            out.flush();
            out.close();
        }
    }

    static class AuthError{
        private int status;
        private String title;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

}
