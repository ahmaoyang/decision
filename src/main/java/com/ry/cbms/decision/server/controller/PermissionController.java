package com.ry.cbms.decision.server.controller;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.Permission;
import com.ry.cbms.decision.server.service.PermissionService;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.ParamCheckUtil;
import com.ry.cbms.decision.server.utils.UserUtil;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.annotation.LogAnnotation;
import com.ry.cbms.decision.server.dao.PermissionDao;
import com.ry.cbms.decision.server.dto.LoginUser;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 权限相关接口
 *
 * @author maoyang
 */
@Api(tags = "权限")
@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private PermissionService permissionService;

    @ApiOperation(value = "当前登录用户拥有的权限")
    @GetMapping("/current")
    public Result permissionsCurrent() {
        LoginUser loginUser = UserUtil.getLoginUser ();
        if (null == loginUser) {
            return Result.error (CodeMsg.NOT_LOGIN);
        }
        List<Permission> list = loginUser.getPermissions ();
        JSONArray array = new JSONArray ();
        setPermissionsTree (0L, list, array, false);//树结构
        return Result.success (array);
    }

    /**
     * 设置子元素
     *
     * @param p
     * @param permissions
     */
    private void setChild(Permission p, List<Permission> permissions) {
        List<Permission> child = permissions.parallelStream ().filter (a -> a.getParentId ().equals (p.getId ())).collect (Collectors.toList ());
        p.setChild (child);
        if (!CollectionUtils.isEmpty (child)) {
            child.parallelStream ().forEach (c -> {
                //递归设置子元素，多级菜单支持
                setChild (c, permissions);
            });
        }
    }

//	private void setChild(List<Permission> permissions) {
//		permissions.parallelStream().forEach(per -> {
//			List<Permission> child = permissions.stream().filter(p -> p.getParentId().equals(per.getId()))
//					.collect(Collectors.toList());
//			per.setChild(child);
//		});
//	}

    /**
     * 菜单列表
     *
     * @param pId
     * @param permissionsAll
     * @param list
     */
    private void setPermissionsList(Long pId, List<Permission> permissionsAll, List<Permission> list) {
        for (Permission per : permissionsAll) {
            if (per.getParentId ().equals (pId)) {
                list.add (per);
                if (permissionsAll.stream ().filter (p -> p.getParentId ().equals (per.getId ())).findAny () != null) {
                    setPermissionsList (per.getId (), permissionsAll, list);
                }
            }
        }
    }

    @GetMapping("/permissionsList")
    @ApiOperation(value = "菜单列表")
    //@PreAuthorize("hasAuthority('sys:menu:query')")
    public Result permissionsList() {
        List<Permission> permissionsAll = permissionDao.listAll ();

        List<Permission> list = Lists.newArrayList ();
        setPermissionsList (0L, permissionsAll, list);

        return Result.success (list);
    }

    @GetMapping("/all")
    @ApiOperation(value = "所有菜单")
    //@PreAuthorize("hasAuthority('sys:menu:query')")
    public Result permissionsAll() {
        List<Permission> permissionsAll = permissionDao.listAll ();
        JSONArray array = new JSONArray ();
        setPermissionsTree (0L, permissionsAll, array, false);
        return Result.success (array);
    }

    @GetMapping("/parents")
    @ApiOperation(value = "一级菜单")
    // @PreAuthorize("hasAuthority('sys:menu:query')")
    public Result parentMenu() {
        List<Permission> parents = permissionDao.listParents ();
        return Result.success (parents);
    }

    /**
     * 菜单树
     *
     * @param pId
     * @param permissionsAll
     * @param array
     */
    private void setPermissionsTree(Long pId, List<Permission> permissionsAll, JSONArray array, Boolean flag) {
        if (null != permissionsAll && permissionsAll.size () > 0) {
            for (Permission per : permissionsAll) {
                if (per.getParentId ().equals (pId)) {
                    String string = JSONObject.toJSONString (per);
                    JSONObject parent = (JSONObject) JSONObject.parse (string);
                    if (null != array && null != parent) {
                        array.add (parent);
                    }
                    if (permissionsAll.stream ().filter (p -> p.getParentId ().equals (per.getId ())).findAny () != null) {
                        JSONArray child = new JSONArray ();
                        parent.put ("child", child);
                        if (flag) {
                            if (null != child && child.size () > 0) {
                                setPermissionsTree (per.getId (), null, child, flag);
                            } else {
                                setPermissionsTree (per.getId (), permissionsAll, null, flag);
                            }
                        } else {
                            setPermissionsTree (per.getId (), permissionsAll, child, flag);
                        }
                    }
                }
            }
        }
    }

    @GetMapping("/listByRoleId")
    @ApiOperation(value = "根据角色id获取权限")
    //@PreAuthorize("hasAnyAuthority('sys:menu:query','sys:role:query')")
    public Result listByRoleId(@ApiParam("角色Id") @RequestParam("roleId") Long roleId) {
        if (StringUtils.isEmpty (roleId) || roleId < Constants.MIN_ID) {
            return Result.error ("输入Id不正确");
        }
        List<Permission> permissionList = permissionDao.listByRoleId (roleId);
        return Result.success (permissionList);
    }

    @LogAnnotation
    @PostMapping
    @ApiOperation(value = "新增菜单")
    // @PreAuthorize("hasAuthority('sys:menu:add')")
    public Result save(@RequestBody Permission permission) {
        if (null == permission) {
            return Result.error ("参数不能为空");
        }
        if (StringUtils.isEmpty (permission.getId ()) || permission.getId () < Constants.MIN_ID) {
            return Result.error ("参数id格式不正确");
        }
        if (ParamCheckUtil.check (permission.getHref (), permission.getName ())) {
            return Result.error ("菜单名称或地址不能为空");
        }
        Date currDate = new Date ();
        permission.setCreateTime (currDate);
        permission.setUpdateTime (currDate);
        try {
            permissionDao.save (permission);
        } catch (Exception e) {
            Result.error (e.toString ());
        }
        return Result.success ();
    }

    @GetMapping("/getPermissionById")
    @ApiOperation(value = "根据菜单id获取菜单")
    //@PreAuthorize("hasAuthority('sys:menu:query')")
    public Result getPermissionById(@ApiParam(value = "菜单Id", required = true) @RequestParam("id") Long id) {
        if (ParamCheckUtil.check (id) || id < 1) {
            return Result.error ("id格式不正确");
        }
        return Result.success (permissionDao.getById (id));
    }

    @LogAnnotation
    @PostMapping("/update")
    @ApiOperation(value = "修改菜单")
    // @PreAuthorize("hasAuthority('sys:menu:add')")
    public Result update(@RequestBody List<Permission> permissions) {
        if (null == permissions) {
            return Result.error ("参数不能为空");
        }
        try {
            permissions.forEach (permission -> {
                permissionService.update (permission);
            });
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        return Result.success ();
    }

    /**
     * 校验权限
     *
     * @return
     */
    @GetMapping("/owns")
    @ApiOperation(value = "校验当前用户的权限")
    public Result ownsPermission() {
        List<Permission> permissions = UserUtil.getLoginUser ().getPermissions ();
        if (CollectionUtils.isEmpty (permissions)) {
            return Result.success (Collections.emptySet ());
        }

        return Result.success (permissions.parallelStream ().filter (p -> !StringUtils.isEmpty (p.getPermission ()))
                .map (Permission::getPermission).collect (Collectors.toSet ()));
    }

    @LogAnnotation
    @DeleteMapping("/deleteById")
    @ApiOperation(value = "删除菜单")
    // @PreAuthorize("hasAuthority('sys:menu:del')")
    public Result deleteById(@ApiParam(value = "菜单Id", required = true) @RequestParam(value = "id") Long id) {
        if (ParamCheckUtil.check (id) || id < 1) {
            return Result.error ("id格式不正确");
        }
        try {
            permissionService.delete (id);
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        return Result.success ();
    }
}
