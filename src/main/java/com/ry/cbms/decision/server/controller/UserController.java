package com.ry.cbms.decision.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.access.AccessLimit;
import com.ry.cbms.decision.server.dto.UserDto;
import com.ry.cbms.decision.server.model.SysUser;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.service.UserService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.ParamCheckUtil;
import com.ry.cbms.decision.server.utils.UserUtil;
import com.ry.cbms.decision.server.vo.BaseRequestVo;
import com.ry.cbms.decision.server.vo.ModifyPassWordVo;
import com.ry.cbms.decision.server.vo.ResetPasswordVo;
import io.swagger.annotations.ApiParam;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.ry.cbms.decision.server.annotation.LogAnnotation;
import com.ry.cbms.decision.server.dao.UserDao;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 用户相关接口
 *
 * @author maoyang
 */
@Api(tags = "用户")
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger ("adminLogger");

    @Autowired
    private UserService userService;
    @Autowired
    private UserDao userDao;

    @LogAnnotation
    @PostMapping("/saveUser")
    @ApiOperation(value = "新增用户", notes = "新增用户(用户名，邮箱，手机号不能为空)")
    //@PreAuthorize("hasAuthority('sys:user:add')")
    public Result saveUser(@RequestBody UserDto userDto) {
        if (StringUtils.isEmpty (userDto.getUsername ())) {
            return Result.error ("用户名不能为空");
        }
        if (StringUtils.isEmpty (userDto.getEmail ())) {
            return Result.error ("邮箱不能为空");
        }
        if (StringUtils.isEmpty (userDto.getPhone ())) {
            return Result.error ("手机号不能为空");
        }
        String userName = userDto.getUsername ();
        List<SysUser> uList = userService.getUserByUserNameOrPhoneOrEmail (userName, userDto.getPhone (), userDto.getEmail ());
        if (null != uList && uList.size () > 0) {
            return Result.error ("用户名或邮箱或手机号已存在");
        }
        try {
            userService.saveUser (userDto);
        } catch (Exception e) {
            return Result.error (e.toString ());
        }
        return Result.success ();
    }

    @LogAnnotation
    @PostMapping("/updateUser")
    @ApiOperation(value = "修改用户")
    //@PreAuthorize("hasAuthority('sys:user:add')")
    public Result updateUser(@RequestBody UserDto userDto) {
        String userName = userDto.getUsername ();
        String email = userDto.getEmail ();
        String phone = userDto.getPhone ();
        Boolean flag = ParamCheckUtil.check (userName, email, phone);

        if (flag) {
            return Result.error ("用户名或邮箱或手机号不能为空");
        }
        List roleIds = userDto.getRoleIds ();
        if (null == roleIds|| roleIds.size ()==0) {  //修改用户判断（非修改角色）
            List<SysUser> uList = userService.getUserByUserNameOrPhoneOrEmail (userName, userDto.getPhone (), userDto.getEmail ());
            if (null != uList && uList.size () > 0) {
                return Result.error ("用户名或邮箱或手机号已存在");
            }
        }
        try {
            userService.updateUser (userDto);
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        return Result.success ();
    }

    @LogAnnotation
    @DeleteMapping("/deleteUser/{id}")
    @ApiOperation(value = "删除用户")
    //@PreAuthorize("hasAuthority('sys:user:add')")
    public Result deleteUser(@PathVariable String id) {
        if (Long.valueOf (id) < Constants.MIN_ID) {
            return Result.error ("参数格式不正确");
        }
        try {
            userService.deleteUser (id);
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        return Result.success ();
    }

    @LogAnnotation
    @PostMapping("/updateHeadImgUrl")
    @ApiOperation(value = "当前用户修改头像")
    public Result updateHeadImgUrl(@ApiParam(value = "用户头像", required = true) @RequestParam(value = "headImgUrl") String headImgUrl) {
        if (ParamCheckUtil.check (headImgUrl)) {
            return Result.error ("参数不能为空");
        }
        SysUser user = UserUtil.getLoginUser ();
        if (null == user) {
            return Result.error ("用户未登录");
        }
        UserDto userDto = new UserDto ();
        BeanUtils.copyProperties (user, userDto);
        userDto.setHeadImgUrl (headImgUrl);
        try {
            userService.updateUser (userDto);
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        if (log.isInfoEnabled ()) {
            log.info ("{}修改了头像", user.getUsername ());
        }
        return Result.success ();
    }

    @LogAnnotation
    @PostMapping("/changePassword")
    @ApiOperation(value = "修改密码")
    //@PreAuthorize("hasAuthority('sys:user:password')")
    public Result changePassword(@RequestBody ModifyPassWordVo modifyPassWordVo) {
        String userId = modifyPassWordVo.getUserId ();
        String oldPassword = modifyPassWordVo.getOldPassword ();
        String newPassword = modifyPassWordVo.getNewPassword ();
        Boolean flag = ParamCheckUtil.check (userId, oldPassword, newPassword);
        if (flag) {
            return Result.error ("必传参数不能为空");
        }
        if (oldPassword.equals (newPassword)) {
            return Result.error ("新密码不能和老密码相同");
        }
        if (newPassword.length () < 6) {
            return Result.error ("新密码长度不能少于6位");
        }
        try {
            userService.changePassword (userId, oldPassword, newPassword);
        } catch (Exception e) {
            return Result.error (e.getMessage ());
        }
        return Result.success (CodeMsg.SUCCESS);
    }


    @LogAnnotation
    @PostMapping("/resetPassword")
    @ApiOperation(value = "重置密码")
    //@PreAuthorize("hasAuthority('sys:user:password')")
    public Result resetPassword(@RequestBody ResetPasswordVo resetPasswordVo) {
        if (null == resetPasswordVo) {
            return Result.error ("参数不能为空");
        }
        String email = resetPasswordVo.getEmail ();
        Boolean flag = ParamCheckUtil.check (email, resetPasswordVo.getPhone (), resetPasswordVo.getUserName ());
        if (flag) {
            return Result.error ("必传参数不能为空");
        }
        if (!ComUtil.checkEmail (email)) {
            return Result.error ("邮箱格式不正确");
        }
        try {
            userService.resetPassword (resetPasswordVo);
        } catch (Exception e) {
            return Result.error (e.getMessage ());
        }
        return Result.success (CodeMsg.SUCCESS);
    }

    @GetMapping("/listUsers")
    @ApiOperation(value = "用户列表")
    //@PreAuthorize("hasAuthority('sys:user:query')")
    public Result listUsers(@ApiParam(value = "分业查询的开始数", required = true) @RequestParam(value = "offset") Integer offset,
                            @ApiParam(value = "每页显示条数", required = true) @RequestParam(value = "limit") Integer limit,
                            @ApiParam(value = "名称") @RequestParam(value = "username", required = false) String username,
                            @ApiParam(value = "职位名称") @RequestParam(value = "nickname", required = false) String nickname,
                            @ApiParam(value = "用户状态，0作废 1正常，2锁定") @RequestParam(value = "status", required = false) String status,
                            @ApiParam(value = "邮箱") @RequestParam(value = "email", required = false) String email,
                            @ApiParam(value = "手机") @RequestParam(value = "phone", required = false) String phone) {
        PageTableRequest request = new PageTableRequest ();
        Map<String, Object> paramMap = new HashMap<> ();
        paramMap.put ("username", username);
        paramMap.put ("nickname", nickname);
        paramMap.put ("status", status);
        paramMap.put ("email", email);
        paramMap.put ("phone", phone);
        ComUtil.setPageParam (request, offset, limit, paramMap);
        List<SysUser> list = userDao.list (request.getParams (), request.getOffset (), request.getLimit ());
        Integer count = userDao.count (request.getParams ());
        return Result.success (new PageTableHandler (request1 -> count, request2 -> list).handle (request));
    }

    @ApiOperation(value = "当前登录用户")
    @GetMapping("/current")
    public Result currentUser() {
        return Result.success (UserUtil.getLoginUser ());
    }

    @ApiOperation(value = "根据用户id获取用户")
    @GetMapping("/getUserById")
    //@PreAuthorize("hasAuthority('sys:user:query')")
    //@AccessLimit
    public Result getUserById(@RequestParam(value = "id") String id) {
        if (ParamCheckUtil.check (id)) {
            return Result.error ("参数不能为空");
        }
        try {
            if (Long.valueOf (id) < Constants.MIN_ID) {
                return Result.error ("参数格式不正确");
            }
        } catch (NumberFormatException e) {
            return Result.error ("参数格式不正确");
        }
        return Result.success (userDao.getById (id));
    }


    @ApiOperation(value = "根据条件查询获取用户(邮箱，手机，中文名)")
    @GetMapping("/getUserByCondition")
    //@PreAuthorize("hasAuthority('sys:user:query')")
    //@AccessLimit
    public Result getUserByCondition(@ApiParam(value = "邮箱") @RequestParam(value = "email", required = false) String email,
                                     @ApiParam(value = "手机") @RequestParam(value = "phone", required = false) String phone,
                                     @ApiParam(value = "姓名") @RequestParam(value = "name", required = false) String name) {
        if (StringUtils.isEmpty (email)) {
            email = null;
        }
        if (StringUtils.isEmpty (phone)) {
            phone = null;
        }
        if (StringUtils.isEmpty (name)) {
            name = null;
        }
        return Result.success (userDao.getUserByCondition (email, phone, name));
    }
}
