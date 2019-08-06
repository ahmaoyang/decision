package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.utils.ComUtil;

import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ry.cbms.decision.server.dao.SysLogsDao;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "日志")
@RestController
@RequestMapping("/logs")
public class SysLogsController {

    @Autowired
    private SysLogsDao sysLogsDao;

    @GetMapping
    // @PreAuthorize("hasAuthority('sys:log:query')")
    @ApiOperation(value = "日志列表")
    public Result list(@ApiParam(value = "分业查询的开始数",required = true) @RequestParam(value = "offset") Integer offset,
                       @ApiParam(value = "每页显示条数",required = true) @RequestParam(value = "limit") Integer limit,
                       @ApiParam(value = "开始时间",required = true) @RequestParam(value = "beginTime") String beginTime,
                       @ApiParam(value = "结束时间",required = true) @RequestParam(value = "endTime") String endTime,
                       @ApiParam(value = "flag 1 表示操作成，0表示操作失败的,不传值查询所有") @RequestParam(value = "flag",required = false) String flag) {
        PageTableRequest request = new PageTableRequest ();
        Map<String,Object> paramMap=new HashMap<> ();
        paramMap.put ("beginTime",beginTime);
        paramMap.put ("endTime",endTime);
        paramMap.put ("flag",flag);
        ComUtil.setPageParam (request,offset,limit,paramMap);
        return Result.success (new PageTableHandler (request12 -> sysLogsDao.count (request12.getParams ()), request1 -> sysLogsDao.list (request1.getParams (), request1.getOffset (), request1.getLimit ())).handle (request));
    }

}
