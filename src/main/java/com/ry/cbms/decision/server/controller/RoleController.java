package com.ry.cbms.decision.server.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.Role;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.page.table.PageTableResponse;
import com.ry.cbms.decision.server.service.RoleService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.ParamCheckUtil;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.ry.cbms.decision.server.annotation.LogAnnotation;
import com.ry.cbms.decision.server.dao.RoleDao;
import com.ry.cbms.decision.server.dto.RoleDto;
import com.google.common.collect.Maps;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 角色相关接口
 *
 * @author maoyang
 */
@Api(tags = "角色")
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleDao roleDao;

    @LogAnnotation
    @PostMapping("/saveRole")
    @ApiOperation(value = "保存(修改)角色")
    //@PreAuthorize("hasAuthority('sys:role:add')")
    public Result saveRole(@RequestBody RoleDto roleDto) {
        if (null == roleDto) {
            return Result.error ("参数不能为空");
        }
        if (StringUtils.isEmpty (roleDto.getName ())) {
            return Result.error ("角色名称不能为空");
        }
        try {
            roleService.saveRole (roleDto);
        } catch (Exception e) {
            return Result.error (e.getMessage ());
        }
        return Result.success ();
    }

    @GetMapping("/listRoles")
    @ApiOperation(value = "角色列表")
    //@PreAuthorize("hasAuthority('sys:role:query')")
    public Result listRoles(@ApiParam(value = "分业查询的开始数", required = true) @RequestParam(value = "offset") Integer offset,
                            @ApiParam(value = "每页显示条数", required = true) @RequestParam(value = "limit") Integer limit,
                            @ApiParam(value = "角色名称") @RequestParam(value = "name",required = false) String name) {
        PageTableRequest request = new PageTableRequest ();
        Map<String,Object> paramMap=new HashMap<> ();
        paramMap.put ("name",name);
        ComUtil.setPageParam (request, offset, limit,paramMap);
        return Result.success (new PageTableHandler (request1 -> roleDao.count (request1.getParams ()), request12 -> {
            List<Role> list = roleDao.list (request12.getParams (), request12.getOffset (), request12.getLimit ());
            return list;
        }).handle (request));
    }

    @GetMapping("/getById")
    @ApiOperation(value = "根据id获取角色")
    //@PreAuthorize("hasAuthority('sys:role:query')")
    public Result getById(@ApiParam(value = "角色Id", required = true) @RequestParam("id") Long id) {
        if (ParamCheckUtil.check (id) || id < 1) {
            return Result.error ("id格式不正确");
        }
        return Result.success (roleDao.getById (id));
    }

    @GetMapping("/all")
    @ApiOperation(value = "所有角色")
    //@PreAuthorize("hasAnyAuthority('sys:user:query','sys:role:query')")
    public Result roles() {
        return Result.success (roleDao.list (Maps.newHashMap (), null, null));
    }

    @GetMapping("/roles")
    @ApiOperation(value = "根据用户id获取拥有的角色")
    //@PreAuthorize("hasAnyAuthority('sys:user:query','sys:role:query')")
    public Result roles(@ApiParam(value = "用户id", required = true) @RequestParam("userId") Long userId) {
        if (ParamCheckUtil.check (userId) || userId < 1) {
            return Result.error ("id格式不正确");
        }
        return Result.success (roleDao.listByUserId (userId));
    }

    @LogAnnotation
    @DeleteMapping("/deleteById")
    @ApiOperation(value = "删除角色")
    //@PreAuthorize("hasAuthority('sys:role:del')")
    public Result deleteById(@ApiParam(value = "角色Id", required = true) @RequestParam("id") Long id) {
        if (ParamCheckUtil.check (id) || id < Constants.MIN_ID) {
            return Result.error ("id格式不正确");
        }
        try {
            roleService.deleteRole (id);
        } catch (Exception e) {
            return Result.error (e.toString ());
        }
        return Result.success ();
    }
}
