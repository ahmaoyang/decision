package com.ry.cbms.decision.server.access;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.filter.WriteResponseUtil;
import com.ry.cbms.decision.server.model.SysUser;
import com.ry.cbms.decision.server.redis.AccessKey;
import com.ry.cbms.decision.server.service.UserService;
import com.ry.cbms.decision.server.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @Author maoYang
 * @Date 2019/4/25 13:51
 * @Description 用户服务层接口拦截器
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            SysUser user = UserUtil.getLoginUser();
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if (needLogin) {
                if (null == user) {
                    WriteResponseUtil.writeNotLoginMessageToResponse(response);
                    return false;
                }
                key += "_" + user.getId();
            }
            String ak = AccessKey.withPrifix().getPrefix();
            Integer count = (Integer)redisTemplate.opsForValue().get(ak+key);
            if (count == null) {
                redisTemplate.opsForValue().set(ak+key, 1,Long.valueOf(seconds),TimeUnit.SECONDS);
            } else if (count < maxCount) {
                redisTemplate.opsForValue().increment(ak+key,1);
            } else {
                WriteResponseUtil.render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
            if(!needLogin){
                if (null != user) {
                    WriteResponseUtil.render(response, CodeMsg.ON_LOGIN);
                    return false;
                }
            }
        }
        return true;
    }


}
