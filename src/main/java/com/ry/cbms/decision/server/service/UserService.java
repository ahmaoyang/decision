package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.dto.UserDto;
import com.ry.cbms.decision.server.model.SysUser;
import com.ry.cbms.decision.server.vo.ResetPasswordVo;

import java.util.List;

public interface UserService {

    SysUser saveUser(UserDto userDto);

    SysUser updateUser(UserDto userDto);

    SysUser getUser(String fuzz);

    void deleteUser(String id);

    void resetPassword(ResetPasswordVo resetPasswordVo);

    SysUser getUserByUserNameAndPhoneAndEmail(String username, String phone, String email);

    List<SysUser> getUserByUserNameOrPhoneOrEmail(String username, String phone, String email);

    void changePassword(String userId, String oldPassword, String newPassword);

}
