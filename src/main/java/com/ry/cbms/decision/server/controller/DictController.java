package com.ry.cbms.decision.server.controller;


import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.Dict;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.ParamCheckUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.ry.cbms.decision.server.dao.DictDao;

import io.swagger.annotations.ApiOperation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "数据字典")
@RestController
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private DictDao dictDao;

    //@PreAuthorize("hasAuthority('dict:add')")
    @PostMapping
    @ApiOperation(value = "保存")
    public Result save(@RequestBody Dict dict) {
        if (ParamCheckUtil.check (dict.getK (), dict.getVal ())) {
            return Result.error ("参数不能为空");
        }
        Dict d = dictDao.getByTypeAndK (dict.getType (), dict.getK ());
        if (d != null) {
            return Result.error ("类型和key已存在");
        }
        dict.setCreateTime (new Date ());
        dict.setUpdateTime (new Date ());
        dictDao.save (dict);
        return Result.success (dict);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取")
    public Result get(@PathVariable Long id) {
        if (StringUtils.isEmpty (id) || id < 1) {
            return Result.error ("id不能为空或不为正数");
        }
        return Result.success (dictDao.getById (id));
    }

    //@PreAuthorize("hasAuthority('dict:add')")
    @PutMapping
    @ApiOperation(value = "修改")
    public Result update(@RequestBody Dict dict) {
        if (ParamCheckUtil.check (dict.getK (), dict.getVal ())) {
            return Result.error ("参数不能为空");
        }
        try {
            dictDao.update (dict);
        } catch (Exception e) {
            return Result.error (e.toString ());
        }
        return Result.success (dict);
    }

    //@PreAuthorize("hasAuthority('dict:query')")
    @GetMapping("/list")
    @ApiOperation(value = "列表")
    public Result list(@ApiParam(value = "分业查询的开始数", required = true) @RequestParam(value = "offset") Integer offset,
                       @ApiParam(value = "每页显示条数", required = true) @RequestParam(value = "limit") Integer limit,
                       @ApiParam(value = "id") @RequestParam(value = "id", required = false) String id,
                       @ApiParam(value = "类别") @RequestParam(value = "type",required = false) String  type,
                       @ApiParam(value = "key") @RequestParam(value = "k", required = false) String k,
                       @ApiParam(value = "value") @RequestParam(value = "val", required = false) String val ) {
        PageTableRequest request = new PageTableRequest ();
        Map<String,Object> paramMap=new HashMap<> ();
        paramMap.put ("id",id);
        paramMap.put ("type",type);
        paramMap.put ("k",k);
        paramMap.put ("val",val);
        ComUtil.setPageParam (request, offset, limit,paramMap);
        return Result.success (new PageTableHandler (request1 -> dictDao.count (request1.getParams ()), request12 -> dictDao.list (request12.getParams (), request12.getOffset (), request12.getLimit ())).handle (request));
    }

    //@PreAuthorize("hasAuthority('dict:del')")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除")
    public Result delete(@PathVariable Long id) {
        if (StringUtils.isEmpty (id) || id < 1) {
            return Result.error ("id不能为空或不为正数");
        }
        try {
            dictDao.delete (id);
        } catch (Exception e) {
            return Result.error ();
        }
        return Result.success (CodeMsg.SUCCESS);
    }

    @ApiOperation(value = "根据字典类型查询")
    @GetMapping("/listByType")
    public Result listByType(@ApiParam("字典类型") @RequestParam("type") String type) {
        if (StringUtils.isEmpty (type)) {
            return Result.error ("字典类型不能为空");
        }
        return Result.success (dictDao.listByType (type));
    }
}
