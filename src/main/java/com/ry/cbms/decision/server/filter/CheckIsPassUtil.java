package com.ry.cbms.decision.server.filter;

import com.ry.cbms.decision.server.config.ApplicationProperties;

import javax.servlet.http.HttpServletRequest;

public class CheckIsPassUtil {

    public static boolean needFilter(HttpServletRequest request, ApplicationProperties applicationProperties){
        ApplicationProperties.Auth auth = applicationProperties.getAuth();
        if (null == auth){
            return true;
        }
        String withoutUrls = auth.getWithoutUrls();
        if (null == withoutUrls){
            return true;
        }
        String[] urls = withoutUrls.split(",");
        String requestURI = request.getRequestURI();
        for (int i = 0; i < urls.length; i++) {
            if (requestURI .startsWith(urls[i].trim())){
                return false;
            }
        }
        return true;
    }

}
