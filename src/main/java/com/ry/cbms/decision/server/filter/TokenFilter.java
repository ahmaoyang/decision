package com.ry.cbms.decision.server.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ry.cbms.decision.server.config.ApplicationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ry.cbms.decision.server.dto.LoginUser;
import com.ry.cbms.decision.server.service.TokenService;

/**
 * Token过滤器
 *
 * @author maoyang
 */
@Component
public class TokenFilter extends OncePerRequestFilter {

    public static final String TOKEN_KEY = "token";

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private ApplicationProperties applicationProperties;

    private static final Long MINUTES_10 = 10 * 60 * 1000L;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
//        if (!CheckIsPassUtil.needFilter(request, applicationProperties)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        String token = getToken(request);
//        if (null == token) {
//            WriteResponseUtil.writeNotLoginMessageToResponse(response);
//            return;
//        }
        if (StringUtils.isNotBlank(token)) {
            LoginUser loginUser = tokenService.getLoginUser(token);
            if (loginUser != null) {
                loginUser = checkLoginTime(loginUser);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser,
                        null, loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 校验时间<br>
     * 过期时间与当前时间对比，临近过期10分钟内的话，自动刷新缓存
     *
     * @param loginUser
     * @return
     */
    private LoginUser checkLoginTime(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MINUTES_10) {
            String token = loginUser.getToken();
            loginUser = (LoginUser) userDetailsService.loadUserByUsername(loginUser.getUsername());
            loginUser.setToken(token);
            tokenService.refresh(loginUser);
        }
        return loginUser;
    }

    /**
     * 根据参数或者header获取token
     *
     * @param request
     * @return
     */
    public static String getToken(HttpServletRequest request) {
        String token = request.getParameter(TOKEN_KEY);
        if (StringUtils.isBlank(token)) {
            token = request.getHeader(TOKEN_KEY);
        }

        return token;
    }

}
