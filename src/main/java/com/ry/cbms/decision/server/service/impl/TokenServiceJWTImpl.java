package com.ry.cbms.decision.server.service.impl;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.utils.UserUtil;
import com.ry.cbms.decision.server.service.SysLogService;
import com.ry.cbms.decision.server.service.TokenService;
import io.jsonwebtoken.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ry.cbms.decision.server.dto.LoginUser;
import com.ry.cbms.decision.server.dto.Token;

/**
 * token存到redis的实现类<br>
 * jwt实现的token
 *
 * @author maoyang
 */
@Primary
@Service
public class TokenServiceJWTImpl implements TokenService {

    private static final Logger log = LoggerFactory.getLogger("adminLogger");

    /**
     * token过期秒数
     */
    @Value("${token.expire.seconds}")
    private Integer expireSeconds;
    @Autowired
    private RedisTemplate<String, LoginUser> redisTemplate;
    @Autowired
    private SysLogService logService;
    /**
     * 私钥
     */
    @Value("${token.jwtSecret}")
    private String jwtSecret;

    private static Key KEY = null;
    private static final String LOGIN_USER_KEY = "LOGIN_USER_KEY";

    @Override
    public Token saveToken(LoginUser loginUser) {
//        LoginUser loginUser2 = UserUtil.getLoginUser();
//        if (null != loginUser2) {
//           deleteToken(createJWTToken(loginUser2));
//        }
        loginUser.setToken(UUID.randomUUID().toString());
        cacheLoginUser(loginUser);
        // 登陆日志
        logService.save(loginUser.getId(), "登陆", true, null);
        String jwtToken = createJWTToken(loginUser);

        return new Token(jwtToken, loginUser.getLoginTime());
    }

    /**
     * 生成jwt
     *
     * @param loginUser
     * @return
     */
    private String createJWTToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(LOGIN_USER_KEY, loginUser.getToken());// 放入一个随机字符串，通过该串可找到登陆用户

        String jwtToken = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS256, getKeyInstance())
                .compact();

        return jwtToken;
    }

    private void cacheLoginUser(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireSeconds * 1000);
        // 根据uuid将loginUser缓存
        redisTemplate.boundValueOps(getTokenKey(loginUser.getToken())).set(loginUser, expireSeconds, TimeUnit.SECONDS);
    }

    /**
     * 更新缓存的用户信息
     */
    @Override
    public void refresh(LoginUser loginUser) {
        cacheLoginUser(loginUser);
    }

    @Override
    public LoginUser getLoginUser(String jwtToken) {
        String uuid = getUUIDFromJWT(jwtToken);
        if (uuid != null) {
            return redisTemplate.boundValueOps(getTokenKey(uuid)).get();
        }
        return null;
    }

    @Override
    public boolean deleteToken(String jwtToken) {
        String uuid = getUUIDFromJWT(jwtToken);
        if (uuid != null) {
            String key = getTokenKey(uuid);
            LoginUser loginUser = redisTemplate.opsForValue().get(key);
            if (loginUser != null) {
                redisTemplate.delete(getTokenKey(loginUser.getToken()));
                redisTemplate.opsForValue ().set (RedisKeyGenerator.getLogOutStatus (jwtToken),loginUser,60,TimeUnit.MINUTES);

                // 退出日志
                logService.save(loginUser.getId(), "退出", true, null);
                return true;
            }
        }
        return false;
    }

    private String getTokenKey(String uuid) {
        return RedisKeyGenerator.getTOKEN () + uuid;
    }

    private Key getKeyInstance() {
        if (KEY == null) {
            synchronized (TokenServiceJWTImpl.class) {
                if (KEY == null) {// 双重锁
                    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtSecret);
                    KEY = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
                }
            }
        }

        return KEY;
    }
        private String getUUIDFromJWT(String jwtToken) {
            if ("null".equals(jwtToken) || StringUtils.isBlank(jwtToken)) {
                return null;
            }

            try {
                Map<String, Object> jwtClaims = null;
                try {
                    jwtClaims = Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(jwtToken).getBody();
                } catch (Exception e) {
                    //log.error("{}", e);
                    //throw  new GlobalException ("token无效");
                    return null;
                }
                return MapUtils.getString(jwtClaims, LOGIN_USER_KEY);
            } catch (ExpiredJwtException e) {
                log.error("{}已过期", jwtToken);
                throw  new GlobalException ("已过期"+jwtToken);
            } catch (Exception e) {
                log.error("{}", e);
            }
        return null;
    }
}
