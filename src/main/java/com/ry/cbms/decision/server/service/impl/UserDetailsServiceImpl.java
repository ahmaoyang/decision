package com.ry.cbms.decision.server.service.impl;

import java.util.List;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.model.Permission;
import com.ry.cbms.decision.server.model.SysUser;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.UserService;
import com.ry.cbms.decision.server.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ry.cbms.decision.server.dao.PermissionDao;
import com.ry.cbms.decision.server.dto.LoginUser;

/**
 * spring security登陆处理<br>
 * <p>
 * 密码校验
 *
 * @author maoyang
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;
    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Object userCache = redisTemplate.opsForValue ().get (username);
        if (null != userCache) {
            throw new AuthenticationCredentialsNotFoundException ("用户名不存在");
        }
        Object userLockCache = redisTemplate.opsForValue ().get (RedisKeyGenerator.getUserLock (username));
        Object userDisableCache = redisTemplate.opsForValue ().get (RedisKeyGenerator.getUserDisabled (username));
        if (null != userDisableCache) {
            throw new GlobalException ("用户已作废");
        }
        if (null != userLockCache) {
            throw new GlobalException ("用户被锁定,请联系管理员");
        }
        SysUser sysUser = userService.getUser (username);
        if (null == sysUser) {
            redisTemplate.opsForValue ().set (username, username);
            throw new AuthenticationCredentialsNotFoundException ("用户不存在");
        } else if (sysUser.getStatus () == SysUser.Status.LOCKED) {
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getUserLock (sysUser.getUsername ()), username);
            throw new LockedException ("用户被锁定,请联系管理员");
        } else if (sysUser.getStatus () == SysUser.Status.DISABLED) {
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getUserDisabled (sysUser.getUsername ()), username);
            throw new DisabledException ("用户已作废");
        }

        LoginUser loginUser = new LoginUser ();
        BeanUtils.copyProperties (sysUser, loginUser);
        List<Permission> permissions = permissionDao.listByUserId (sysUser.getId ());
        loginUser.setPermissions (permissions);

        return loginUser;
    }

}
