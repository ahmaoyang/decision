package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.BusinessAccountInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.BusinessAccountService;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.vo.CurrencyVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Author maoYang
 * @Date 2019/5/17 13:50
 * @Description 经济商账户相关接口
 */
@Api(tags = "经济商账户")
@RestController
@RequestMapping("/business")
@Slf4j
public class BusinessAccountController {

    @Autowired
    private BusinessAccountService businessAccountService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ComUtil comUtil;

    /**
     * @param account mt4账户
     * @param userId  指定用户或代理userId
     * @return
     */
    @ApiOperation(value = "查询经济商账户信息", notes = ApiNote.getAccountInfo)
    @GetMapping("/getAccountInfo")
    public Result getAccountInfo(@ApiParam("mt4账号") @RequestParam(value = "account", required = false) String account,
                                 @ApiParam("用户id") @RequestParam(value = "userId", required = false) String userId,
                                 @ApiParam(value = "day 按天，month 按月，all 所有") @RequestParam(value = "flag", required = false) String flag,
                                 @ApiParam(value = "币种") @RequestParam(value = "currency", required = false) String currency,
                                 @ApiParam(value = "开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                 @ApiParam(value = "结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        List retList;
        try {
            retList = businessAccountService.getBrokerAccountInfos (account, userId, flag, startDate, endDate, currency);
        } catch (Exception e) {
            if (log.isErrorEnabled ()) {
                log.error ("{}", e);
            }
            return Result.error (e.toString ());
        }
        return Result.success (retList);
    }

    @ApiOperation(value = "查询经济商账户信息明细", notes = ApiNote.getAccountDetail)
    @GetMapping("/getAccountInfoDetails")
    public Result getAccountInfoDetails(@ApiParam("mt4账户") @RequestParam(value = "account", required = false) String account,
                                        @ApiParam("用户Id") @RequestParam(value = "userId", required = false) String userId,
                                        @ApiParam(value = "币种", required = true) @RequestParam(value = "currency") String currency,
                                        @ApiParam(value = "开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                        @ApiParam(value = "结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        BusinessAccountInfo info;
        try {
            if(StringUtils.isEmpty (startDate)&& StringUtils.isEmpty (endDate)){
                startDate = DateUtil.parser (DateUtil.getDayBegin ());
                endDate = DateUtil.parser (DateUtil.getEndDayOfTomorrow ());
            }

            info = businessAccountService.getBrokerAccountTypeDetails (account, userId, startDate, endDate, currency);
        } catch (Exception e) {
            log.error ("{}", e);
            return Result.error (e.getMessage ());
        }
        return Result.success (info);
    }

    @ApiOperation(value = "经济商账户今日明细(只需要传币种参数)", notes = ApiNote.getAccountDetail)
    @GetMapping("/getAccountTodayInfo")
    public Result getAccountTodayInfo(CurrencyVo currencyVo) {
        BusinessAccountInfo info;
        String startDate = DateUtil.parser (DateUtil.getDayBegin ());
        String endDate = DateUtil.parser (DateUtil.getEndDayOfTomorrow ());
        String currency = currencyVo.getToCurrency ();
        if (StringUtils.isEmpty (currency)) {
            return Result.error ("币种不能为空");
        }
        try {
            info = businessAccountService.getBrokerAccountTypeDetails (null, null, startDate, endDate, currency);
        } catch (Exception e) {
            log.error ("{}", e);
            return Result.error (e.getMessage ());
        }
        return Result.success (info);
    }


    @ApiOperation(value = "查询经济商账户历史", notes = ApiNote.getAccountDetail)
    @GetMapping("/getAccountInfoHis")
    public Result getAccountInfoHis(@ApiParam(value = "币种", required = true) @RequestParam(value = "currency") String currency,
                                    @ApiParam(value = "mt4账号") @RequestParam(value = "account", required = false) String account,
                                    @ApiParam(value = "用户userId") @RequestParam(value = "userId", required = false) String userId) {

        Object cacheValue = null;
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {
            cacheValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getBrokenAccHis (currency, null));
        } else if (!StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {
            cacheValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getBrokenAccHis (currency, account));
        } else if (StringUtils.isEmpty (account) && !StringUtils.isEmpty (userId)) {
            cacheValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getBrokenUserIdHis (currency, userId));
        }
        if (null != cacheValue) {
            return Result.success ((BusinessAccountInfo) cacheValue);
        }
        return Result.success (new BusinessAccountInfo ());
    }

    @ApiOperation(value = "查询经济商账户信息记录", notes = ApiNote.getAccountDetail)
    @GetMapping("/getAccountInfoRecords")
    public Result getAccountInfoRecords(@ApiParam("mt4账号") @RequestParam(value = "account", required = false) String account,
                                        @ApiParam("用户id") @RequestParam(value = "userId", required = false) String userId,
                                        @ApiParam(value = "day 按天，month 按月", required = true) @RequestParam(value = "flag") String flag,
                                        @ApiParam(value = "币种") @RequestParam(value = "currency", required = false) String currency,
                                        @ApiParam(value = "开始日期", required = true) @RequestParam(value = "startDate") String startDate,
                                        @ApiParam(value = "结束日期", required = true) @RequestParam(value = "endDate") String endDate) {
        Map<String, BusinessAccountInfo> infoMap;
        try {
            infoMap = businessAccountService.getAccDataByCondition (account, flag, startDate, endDate, currency, userId);
        } catch (Exception e) {
            infoMap = null;
        }
        if (null == infoMap) {
            return Result.success ();
        }
        Collection collection = infoMap.values ();
        List<BusinessAccountInfo> showList = new ArrayList<> (collection);
        log.info ("{}", collection);
        orderByDateTime(showList,flag);
        return Result.success (showList);
    }

    private void orderByDateTime(List<BusinessAccountInfo> showList,String flag) {
        if(Constants.FLAG_MONTH.equals (flag)){
            showList.sort ((a,b)->{
                Date dateA=DateUtil.praiseStringToDateYYYY_MM (a.getDateTime ());
                Date dateB=DateUtil.praiseStringToDateYYYY_MM(b.getDateTime ());

                if(dateA.after (dateB)){
                    return -1;
                }else if(dateA.before (dateB)){
                    return 1;
                }else{
                    return 0;
                }
            });
        }
      else{
            showList.sort ((a,b)->{
                Date dateA=DateUtil.parse (a.getDateTime ());
                Date dateB=DateUtil.parse(b.getDateTime ());

                if(dateA.after (dateB)){
                    return -1;
                }else if(dateA.before (dateB)){
                    return 1;
                }else{
                    return 0;
                }
            });
        }

    }
}
