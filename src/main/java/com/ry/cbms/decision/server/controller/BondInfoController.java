package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.BondInfo;
import com.ry.cbms.decision.server.model.ClearAccountInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.BondInfoService;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.vo.BondInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author maoYang
 * @Date 2019/5/30 18:50
 * @Description 保证金相关(包含清算账户)
 */
@Api(tags = "保证金相关")
@RestController
@RequestMapping("/bond")
public class BondInfoController {
    @Autowired
    private BondInfoService bondInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "保证金账户历史累计", notes = ApiNote.getBondInfo)
    @GetMapping("/getBondHisInfo")
    public Result getBondHisInfo() {
        Object bondInfo;
        try {
            bondInfo = redisTemplate.opsForValue ().get (RedisKeyGenerator.getBondInfoHis ());
        } catch (Exception e) {
            bondInfo = bondInfoService.getHisBondInfo ();
        }
        return Result.success (bondInfo);
    }

    @ApiOperation(value = "清算账户历史累计", notes = "rollovers:总隔夜利息，rolloversFor6002：6002账户隔夜利息，rolloversFor6000：6000账户隔夜利息")
    @GetMapping("/getClearAccHisInfo")
    public Result getClearAccHisInfo() {
        Object clearAccountInfo;
        try {
            clearAccountInfo = redisTemplate.opsForValue ().get (RedisKeyGenerator.getClearAccountInfoHis ());
        } catch (Exception e) {
            clearAccountInfo = bondInfoService.getHisClearAccountInfo ();
            return Result.success (clearAccountInfo);
        }
        return Result.success (clearAccountInfo);

    }

    @ApiOperation(value = "保证金账户信息", notes = ApiNote.getBondInfo)
    @GetMapping("/getBondInfo")
    public Result getBondInfo(@ApiParam(value = "时间颗粒度(day 按天，month 按月)", required = true) @RequestParam(value = "flag") String flag,
                              @ApiParam(value = "开始日期", required = true) @RequestParam(value = "startDate") String startDate,
                              @ApiParam(value = "结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        Map<String, Object> retMap = new HashMap<> ();
        List<BondInfo> dataList = bondInfoService.getBonAccData (flag, startDate, endDate);
       // Collections.reverse (dataList);
        retMap.put ("accData",dataList);
        return Result.success (retMap);
    }


    @ApiOperation(value = "保证金账户今日明细", notes = ApiNote.getBondInfo)
    @GetMapping("/getTodayBondInfo")
    public Result getTodayBondInfo() {
        String startDate = DateUtil.parser (DateUtil.getDayBegin ());
        String endDate = DateUtil.parser (DateUtil.getDayEnd ());
        BondInfo data = bondInfoService.getDayUnitInfo (startDate, endDate);
        return Result.success (data);
    }


    @ApiOperation(value = "保证金账户头", notes = "HEJ411:HEJ411本期净值 ,HEJ001:HEJ001本期净值,HEJ411pre:HEJ411上期余额 ,HEJ001pre:HEJ001上期余额,total:总的本期净值,totalPre:总的上期余额,accData：保证金账户数据集合")
    @GetMapping("/getBondInfoHead")
    public Result getBondInfoHead() {
        Object cashValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getBondInfoHead ());
        if (null != cashValue) {
            return Result.success ((List) cashValue);
        }
        Map<String, Object> retMapTotal = new HashMap<> ();
        Map<String, Object> retMap411 = new HashMap<> ();
        Map<String, Object> retMap001 = new HashMap<> ();
        Map<String, Object> bondInfoMap = bondInfoService.getBondInfo ();
        List<Map> retList = dealHead (retMapTotal, retMap411, retMap001, bondInfoMap);
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getBondInfoHead (), retList, 2, TimeUnit.MINUTES);
        return Result.success (retList);
    }

    private List dealHead(Map<String, Object> retMapTotal, Map<String, Object> retMap411, Map<String, Object> retMap001, Map<String, Object> bondInfoMap) {
        List<Map> retList = new ArrayList<> ();
        Object currBalanceTotal = bondInfoMap.get ("total");
        if (null == currBalanceTotal) {
            currBalanceTotal = 0;
        }
        retMapTotal.put ("currBalance", 0);
        retMapTotal.put ("accountName", "保证金账户");
        retMapTotal.put ("preBalance", currBalanceTotal);
        Object currBalance411 = bondInfoMap.get ("HEJ411");
        if (null == currBalance411) {
            currBalance411 = 0;
        }
        retMap411.put ("currBalance", 0);
        retMap411.put ("preBalance", currBalance411);
        retMap411.put ("accountName", "账户411");
        Object currBalance001 = bondInfoMap.get ("HEJ001");
        if (null == currBalance001) {
            currBalance001 = 0;
        }
        retMap001.put ("currBalance", 0);
        retMap001.put ("preBalance", currBalance001);
        retMap001.put ("accountName", "账户001");

        retList.add (retMap001);
        retList.add (retMap411);
        retList.add (retMapTotal);
        return retList;
    }

    @ApiOperation(value = "清算账户信息", notes = "返回字段说明：rollovers:总隔夜利息，rolloversFor6002：6002账户隔夜利息，rolloversFor6000：6000账户隔夜利息，，createTime：创建时间")
    @GetMapping("/getClearAccInfo")
    public Result getClearAccInfo(@ApiParam(value = "时间颗粒度(day 按天，month 按月)", required = true) @RequestParam(value = "flag") String flag,
                                  @ApiParam(value = "开始日期", required = true) @RequestParam(value = "startDate") String startDate,
                                  @ApiParam(value = "结束日期") @RequestParam(value = "endDate", required = false) String endDate) {

        List dataList = bondInfoService.getClearAccData (flag, startDate, endDate);
        return Result.success (dataList);
    }


    @ApiOperation(value = "更新保证金账户信息")
    @PostMapping("/updateBondInfo")
    public Result updateBondInfo(@RequestBody BondInfo bondInfo) {

        try {
            bondInfoService.updateBondInfo (bondInfo);
        } catch (Exception e) {
            return Result.error ("保存失败");
        }
        return Result.success ("保存成功");
    }

    @ApiOperation(value = "查询保证金账户信息")
    @GetMapping("/getBondInfoByCreateTime")
    public Result getBondInfoByCreateTime(@RequestParam("createTime") String createTime) {
        if (StringUtils.isEmpty (createTime)) {
            return Result.error ("参数不能为空");
        }
        BondInfo bondInfo;
        try {
            bondInfo = bondInfoService.getBondInfoByCreateTime (createTime);
        } catch (Exception e) {
            return Result.error ("失败" + e.toString ());
        }
        return Result.success (bondInfo);
    }


    @ApiOperation(value = "保存保证金账户信息")
    @PostMapping("/saveBondInfo")
    public Result saveBondInfo(@RequestBody BondInfo bondInfo) {
        if (null == bondInfo) {
            return Result.error ("参数不能为空");
        }
        String createTime = bondInfo.getCreateTime ();
        if (StringUtils.isEmpty (createTime)) {
            return Result.error ("日期不能为空");
        }
        try {
            List bondInfos = new ArrayList ();
            bondInfo.setCreateTime (createTime);
            bondInfos.add (bondInfo);
            bondInfoService.save (bondInfos);
        } catch (Exception e) {
            return Result.error ("保存失败");
        }
        return Result.success ("保存成功");
    }


    @ApiOperation(value = "保存清算账户信息")
    @PostMapping("/saveClearAccInfo")
    public Result saveClearAccInfo(@RequestBody ClearAccountInfo clearAccountInfo) {
        if (null == clearAccountInfo.getCreateTime ()) {
            return Result.error ("创建时间不能为空");
        }
        try {
            bondInfoService.saveClearAccInfo (clearAccountInfo);
        } catch (Exception e) {
            return Result.error ("保存失败" + e.toString ());
        }
        return Result.success ("保存成功");
    }

    @ApiOperation(value = "查询清算账户信息")
    @GetMapping("/getClearInfoByCreateTime")
    public Result getClearInfoByCreateTime(@RequestParam("createTime") String createTime) {
        if (StringUtils.isEmpty (createTime)) {
            return Result.error ("参数不能为空");
        }
        ClearAccountInfo clearAccountInfo;
        try {
            clearAccountInfo = bondInfoService.getClearAccountInfoByCreateTime (createTime);
        } catch (Exception e) {
            return Result.error ("保存失败" + e.toString ());
        }
        return Result.success (clearAccountInfo);
    }

    @ApiOperation(value = "更新清算账户信息")
    @PostMapping("/updateClearAccInfo")
    public Result updateClearAccInfo(@RequestBody ClearAccountInfo clearAccountInfo) {
        Integer accountInfoId = clearAccountInfo.getId ();
        if (StringUtils.isEmpty (accountInfoId) || accountInfoId.intValue () < Constants.MIN_ID) {
            return Result.error ("id不可为空或格式错误");
        }
        try {
            bondInfoService.updateClearAccInfo (clearAccountInfo);
        } catch (Exception e) {
            return Result.error ("更新清算账户信息失败" + e.toString ());
        }
        return Result.success ("更新成功");
    }
}
