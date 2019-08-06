package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.CommissionService;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.vo.CommissionVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/28 9:37
 * @Description 返佣相关
 */
@Api(tags = "返佣相关")
@RestController
@RequestMapping("/commission")
@Slf4j
public class CommissionController {
    @Autowired
    private CommissionService commissionService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param account   账户号
     * @param userId    用户或代理Id
     * @param startDate 开始日期
     * @param endDate   截止日期
     * @param offset    分页开始的量
     * @param limit
     * @return
     */
    @GetMapping("/getCommissionInfo")
    @ApiOperation(value = "返佣账户信息", notes = ApiNote.getCommissionInfo)
    public Result getCommissionInfo(@ApiParam("mt4账号") @RequestParam(value = "account", required = false) String account,
                                    @ApiParam("用户Id") @RequestParam(value = "userId", required = false) String userId,
                                    @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                    @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate,
                                    @ApiParam("页数(1表示第一页)") @RequestParam(value = "offset", required = false) Integer offset,
                                    @ApiParam("每页条数") @RequestParam(value = "limit", required = false) Integer limit) {
        PageTableRequest request = new PageTableRequest ();
        if (null == offset) {
            offset = 0;
        } else {
            offset = offset - 1;
        }
        if (null == limit) {
            limit = 10;
        }
        request.setOffset (offset * limit);
        request.setLimit (limit);
        Map resultMap = new HashMap ();
        String currDateTime;
        try {
            if (StringUtils.isEmpty (endDate) && StringUtils.isEmpty (startDate)) {
                currDateTime = DateUtil.parser (new Date ());
                resultMap.put ("lastRefreshTime", currDateTime);
                redisTemplate.opsForValue ().set (RedisKeyGenerator.getLastRefreshTime (getClass ().getSimpleName ()), currDateTime);
            }
            resultMap = commissionService.getCommissionInfos (account, userId, startDate, endDate, offset, limit);
            List infoList = (List) resultMap.get ("infoList");
            Integer totalCount = (Integer) resultMap.get ("totalCount");
            resultMap.remove ("infoList");
            resultMap.remove ("totalCount");
            Object cashValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getLastRefreshTime (getClass ().getSimpleName ()));
            if (!StringUtils.isEmpty (cashValue)) {
                resultMap.put ("lastRefreshTime", cashValue.toString ());
            } else {
                resultMap.put ("lastRefreshTime", "");
            }
            resultMap.put ("commInfos", new PageTableHandler (request1 -> totalCount, request2 -> infoList).handle (request));
        } catch (Exception e) {
            log.error ("{}", e);
            return Result.error ();
        }
        return Result.success (resultMap);


    }

    @GetMapping("/getCommInOutDetails")
    @ApiOperation(value = "返佣账户特定账号信息", notes = ApiNote.getCommInOutDetails)
    public Result getCommInOutDetails(@ApiParam("用户Id") @RequestParam(value = "userId") String userId,
                                      @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                      @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate) {

        Map resultMap = commissionService.getCommInOutDetails (userId, startDate, endDate);
        return Result.success (resultMap);

    }

    @GetMapping("/getCommTodayInfo")
    @ApiOperation(value = "返佣账户今日明细", notes = ApiNote.getCommInOutDetails)
    public Result getCommTodayInfo() {
        String startDate = DateUtil.parser (DateUtil.getDayBegin ());
        String endDate = DateUtil.parser (DateUtil.getDayEnd ());
        CommissionVo commissionVo = commissionService.getTodayCommissionInfo (startDate, endDate);
        return Result.success (commissionVo);

    }
}
