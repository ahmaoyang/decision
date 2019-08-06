package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.ClearAccountInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.RetailService;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/31 16:53
 * @Description 零售端相关
 */
@RestController
@RequestMapping("/retail")
@Api(tags = "零售端相关")
public class RetailController {
    @Autowired
    private RetailService retailService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ComUtil comUtil;

    /**
     * 获取零售端账户信息
     *
     * @param account   指定的Mt4 账户
     * @param userId    用户或代理的Id
     * @param timeFlag  日周月标签
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return
     */
    @ApiOperation(value = "获取零售端账户信息", notes = ApiNote.getRetailInfo)
    @GetMapping("/getRetailInfo")
    public Result getRetailInfo(@ApiParam("mt4账户") @RequestParam(value = "account", required = false) String account,
                                @ApiParam("用户Id") @RequestParam(value = "userId", required = false) String userId,
                                @ApiParam("day 按天，month 按月 默认按天查询") @RequestParam(value = "timeFlag", defaultValue = "day") String timeFlag,
                                @ApiParam("开始日期(开始日期和结束日期默认系统当前日期)") @RequestParam(value = "startDate", required = false) String startDate,
                                @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        if (StringUtils.isEmpty (startDate)) {
            startDate = DateUtil.getyyyyMMdd ();
        }
        if (StringUtils.isEmpty (endDate)) {
            endDate = DateUtil.getyyyyMMdd ();
        }
        Map<String, Object> resultMap = retailService.getRetailInfo (account, userId, timeFlag, startDate, endDate);
        return Result.success (resultMap);
    }

    @ApiOperation(value = "零售端历史累计", notes = ApiNote.getRetailInfo)
    @GetMapping("/getRetailAccHisInfo")
    public Result getRetailAccHisInfo(@ApiParam(value = "mt4账号") @RequestParam(value = "account", required = false) String account,
                                      @ApiParam(value = "用户userId") @RequestParam(value = "userId", required = false) String userId) {
        Object clearAccountInfo = null;
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {
            clearAccountInfo = redisTemplate.opsForValue ().get (RedisKeyGenerator.getRetailHisSum (null));
        } else if (!StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {
            clearAccountInfo = redisTemplate.opsForValue ().get (RedisKeyGenerator.getRetailHisSum (account));

        } else if (StringUtils.isEmpty (account) && !StringUtils.isEmpty (userId)) {
            account = comUtil.getMt4AccountsByUserId (userId);
            clearAccountInfo = redisTemplate.opsForValue ().get (RedisKeyGenerator.getRetailHisSum (account));

        }
        if (null != clearAccountInfo) {
            return Result.success (clearAccountInfo);
        }
        return Result.success ();
    }

    @ApiOperation(value = "零售端今日数据", notes = ApiNote.getRetailInfo)
    @GetMapping("/getRetailAccTodayInfo")
    public Result getRetailAccTodayInfo() {
        return Result.success (retailService.getAllRetailTodayInfo ());
    }
}
