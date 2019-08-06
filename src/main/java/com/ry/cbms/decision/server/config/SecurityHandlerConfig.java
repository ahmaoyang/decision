package com.ry.cbms.decision.server.config;
import com.ry.cbms.decision.server.dto.ResponseInfo;
import com.ry.cbms.decision.server.filter.TokenFilter;
import com.ry.cbms.decision.server.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.ry.cbms.decision.server.dto.LoginUser;
import com.ry.cbms.decision.server.dto.Token;
import com.ry.cbms.decision.server.service.TokenService;

/**
 * spring security处理器
 *
 * @author maoyang
 */
@Configuration
public class SecurityHandlerConfig {

    @Autowired
    private TokenService tokenService;

    /**
     * 登陆成功，返回Token
     *
     * @return
     */
    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            Token token = tokenService.saveToken(loginUser);
            ResponseUtil.responseJson(response, HttpStatus.OK.value(), token);
        };
    }

    /**
     * 登陆失败
     *
     * @return
     */
    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String msg = null;
            if (exception instanceof BadCredentialsException) {
                msg = "密码错误";
            } else {
                msg = exception.getMessage();
            }
            ResponseInfo info = new ResponseInfo(HttpStatus.UNAUTHORIZED.value() + "", msg);
            ResponseUtil.responseJson(response, HttpStatus.UNAUTHORIZED.value(), info);
        };

    }

    /**
     * 未登录，返回401
     *
     * @return
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            ResponseInfo info = new ResponseInfo(HttpStatus.UNAUTHORIZED.value() + "", "请先登录");
            ResponseUtil.responseJson(response, HttpStatus.UNAUTHORIZED.value(), info);
        };
    }

    /**
     * 退出处理
     *
     * @return
     */
    @Bean
    public LogoutSuccessHandler logoutSussHandler() {
        return (request, response, authentication) -> {
            ResponseInfo info = new ResponseInfo(HttpStatus.OK.value() + "", "退出成功");

            String token = TokenFilter.getToken(request);
            tokenService.deleteToken(token);

            ResponseUtil.responseJson(response, HttpStatus.OK.value(), info);
        };

    }

}
