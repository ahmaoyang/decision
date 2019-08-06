package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.dao.AgentsUserDao;
import com.ry.cbms.decision.server.dto.AgentsUser;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.ComUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author maoYang
 * @Date 2019/6/20 16:09
 * @Description 公共接口
 */
@RestController
@RequestMapping("/com")
@Api(tags = "公共接口")
public class ComController {
    @Autowired
    private ComUtil comUtil;

    @Autowired
    private AgentsUserDao agentsUserDao;

    /**
     * 获取币种类型
     *
     * @return
     */
    @ApiOperation(value = "获取所有币种类型", notes = ApiNote.getCurrency)
    @GetMapping("/getCurrency")
    public Result getCurrency() {
        List currencyKinds = comUtil.getCurrencyKinds ();
        return Result.success (currencyKinds);
    }

    @ApiOperation(value = "模糊搜索获取精确姓名和用户id", notes = "name 姓名，userid 用户id")
    @GetMapping("/getUserList")
    @ResponseBody
    public Result getUserInfo(@RequestParam String dimUserName) {
        List<AgentsUser> useridList = agentsUserDao.getAgentsUserList (dimUserName);
        return Result.success (useridList);
    }


}
