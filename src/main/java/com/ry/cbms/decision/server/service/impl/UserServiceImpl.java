package com.ry.cbms.decision.server.service.impl;

import java.util.Date;
import java.util.List;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.dto.UserDto;
import com.ry.cbms.decision.server.model.SysUser;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.vo.ResetPasswordVo;
import jdk.nashorn.internal.objects.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ry.cbms.decision.server.dao.UserDao;
import com.ry.cbms.decision.server.service.UserService;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger ("adminLogger");

    @Autowired
    private UserDao userDao;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SysUser saveUser(UserDto userDto) {
        SysUser user = userDto;
        if (StringUtils.isEmpty (user.getPassword ())) {
            user.setPassword (passwordEncoder.encode (Constants.DEFAULT_PWD));
        } else {
            user.setPassword (passwordEncoder.encode (user.getPassword ()));
        }
        user.setStatus (SysUser.Status.VALID);
        Date currDate = new Date ();
        user.setCreateTime (currDate);
        user.setUpdateTime (currDate);
        try {
            userDao.save (user);
        } catch (Exception e) {
            throw new GlobalException (CodeMsg.USER_INFO_DUPLICATE.getMsg () + ":" + e.toString ());
        }
        saveUserRoles (user.getId (), userDto.getRoleIds ());
        log.debug ("新增用户{}", user.getUsername ());
        return user;
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) { //用户添加角色
        if (roleIds != null) {
            userDao.deleteUserRole (userId);
            if (!CollectionUtils.isEmpty (roleIds)) {
                userDao.saveUserRoles (userId, roleIds);
            }
        }
    }

    @Override
    public SysUser getUser(String fuzz) {
        return userDao.getUser (fuzz);
    }


    @Override
    public SysUser getUserByUserNameAndPhoneAndEmail(String username, String phone, String email) {
        return userDao.getUserByUserNameAndPhoneAndEmail (username, phone, email);
    }

    @Override
    public List<SysUser> getUserByUserNameOrPhoneOrEmail(String username, String phone, String email) {
        return userDao.getUserByUserNameOrPhoneOrEmail (username, phone, email);
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        SysUser u = userDao.getById (userId);
        if (u == null) {
            throw new IllegalArgumentException ("用户不存在");
        }

        if (!passwordEncoder.matches (oldPassword, u.getPassword ())) {
            throw new IllegalArgumentException ("旧密码错误");
        }
        userDao.changePassword (u.getId (), passwordEncoder.encode (newPassword));
        log.debug ("修改{}的密码", userId);
    }

    @Override
    @Transactional
    public SysUser updateUser(UserDto userDto) {
        Long userId = userDto.getId ();
        if (StringUtils.isEmpty (userId) || userId.intValue () < Constants.MIN_ID) {
            throw new GlobalException ("id格式不正确");
        }
        userDto.setUpdateTime (new Date ());
        userDao.update (userDto);
        saveUserRoles (userDto.getId (), userDto.getRoleIds ());
        return userDto;
    }

    @Override
    public void deleteUser(String id) {
        userDao.deleteUser (id);
    }

    @Override
    public void resetPassword(ResetPasswordVo resetPasswordVo) {
        SysUser sysUser = userDao.getUserByUserNameAndPhoneAndEmail (resetPasswordVo.getUserName (), resetPasswordVo.getPhone (), resetPasswordVo.getEmail ());
        if (null == sysUser) {
            throw new GlobalException ("用户不存在");
        }
        userDao.changePassword (sysUser.getId (), passwordEncoder.encode (Constants.DEFAULT_PWD));
    }
}
