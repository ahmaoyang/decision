package com.ry.cbms.decision.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.OrderEvaluationInfo;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.service.RiskManageService;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/4 16:41
 * @Description 风控相关
 */
@Api(tags = "风控相关")
@RestController
@RequestMapping("/riskManage")
public class RiskManageController {

    @Autowired
    private RiskManageService riskManageService;
    @Autowired
    private ComUtil comUtil;

    /**
     * 根据品种查询品种下的不同类别的
     */
    @GetMapping("/getByVariety")
    @ApiOperation(value = "根据品种查询品种下的不同类别的", notes = ApiNote.getRiskOrderEva)
    public Result getOrderEva(@ApiParam("用户id") @RequestParam(value = "userId", required = false) String userId,
                              @ApiParam("mt4账户") @RequestParam(value = "mt4Acc", required = false) String mt4Acc,
                              @ApiParam("品种") @RequestParam("variety") String variety) {
        String account = null;
        if (!StringUtils.isEmpty (userId) && StringUtils.isEmpty (mt4Acc)) {
            account = comUtil.getMt4AccountsByUserId (userId);
        }
        if (StringUtils.isEmpty (userId) && !StringUtils.isEmpty (mt4Acc)) {
            account = mt4Acc;
        }
        List retList = riskManageService.getByVariety (account, variety);
        return Result.success (retList);
    }

    /**
     * 选定的品种名称 查询图线数据
     *
     * @param variety
     * @param orderType
     * @param flag
     * @param account
     * @param beginDate
     * @param overDate
     * @return
     */
    @GetMapping("/getVarietyData")
    @ApiOperation(value = "特定的品种名称 查询图线数据", notes = ApiNote.getRiskOrderEva)
    public Result getVarietyData(@ApiParam("品种") @RequestParam("variety") String variety,
                                 @ApiParam(value = "订单类型") @RequestParam(value = "orderType", required = false) String orderType,
                                 @ApiParam("day 按天，hour 按小时") @RequestParam("flag") String flag,
                                 @ApiParam("mt4账户") @RequestParam("account") String account,
                                 @ApiParam(value = "开始日期") @RequestParam(value = "beginDate", required = false) String beginDate,
                                 @ApiParam(value = "结束日期") @RequestParam(value = "overDate", required = false) String overDate) {

        if (StringUtils.isEmpty (variety)) {
            return Result.error ("品种不能为空");
        }
        if (StringUtils.isEmpty (flag)) {
            flag = "day";//默认按天查
        }
        if (StringUtils.isEmpty (beginDate)) {
            beginDate = DateUtil.parser (DateUtil.getBeginDayOfMonth ());
        }
        if(!StringUtils.isEmpty (overDate)){
            overDate=DateUtil.parserTo (DateUtil.addOrReduceDay (DateUtil.parse (overDate),1));
        }
        List<OrderEvaluationInfo> results = riskManageService.getVarietyData (variety, orderType, flag, account, beginDate, overDate);
        return Result.success (results);

    }

    /**
     * 账户评估
     *
     * @param flag
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping("/getAccEva")
    @ApiOperation(value = "账户评估", notes = ApiNote.getAccEva)
    public Result getAccEva(@ApiParam(value = "标示(0是默认排序),1 优先看异常风酬比账户，2 优先看僵尸账户，3 优先看异常杠杆账户") @RequestParam(value = "flag", required = false) String flag,
                            @ApiParam(value = "页数*每页显示条数", required = true) @RequestParam("offset") Integer offset,
                            @ApiParam(value = "每页显示条数", required = true) @RequestParam("limit") Integer limit) {
        Integer flagNum = 0;
        if (StringUtils.isEmpty (flag)) {
            return Result.error ("flag 不正确");
        }
        if (!StringUtils.isEmpty (flag)) {
            try {
                flagNum = Integer.parseInt (flag);
            } catch (NumberFormatException e) {
                return Result.error ("flag 不正确");
            }
        }
        if (flagNum > 3) {
            return Result.error ("flag 不正确");
        }
        Map resultMap;
        PageTableRequest request = new PageTableRequest ();
        ComUtil.setPageParam (request, offset, limit, null);
        resultMap = riskManageService.accountEva (offset, limit, flag);
        List accInfos = (List) resultMap.get ("accInfos");//查询的账户评估信息
        return Result.success (new PageTableHandler (request1 -> (Integer) resultMap.get ("count"), request2 -> accInfos).handle (request));
    }


    /**
     * 订单评估
     *
     * @param account
     * @param userId
     * @return
     */
    @GetMapping("/getOrderEva")
    @ApiOperation(value = "订单评估", notes = ApiNote.getRiskOrderEva)
    public Result getOrderEva(@ApiParam("mt4账户") @RequestParam(value = "account", required = false) String account,
                              @ApiParam("用户id") @RequestParam(value = "userId", required = false) String userId,
                              @ApiParam(value = "页数*每页显示条数", required = true) @RequestParam(value = "offset") Integer offset,
                              @ApiParam(value = "每页显示条数", required = true) @RequestParam("limit") Integer limit) {
        PageTableRequest request = new PageTableRequest ();
        ComUtil.setPageParam (request, offset, limit, null);
        JSONObject ret = riskManageService.orderEva (account, userId, request.getOffset (), request.getLimit ());
        List infos = ret.getJSONArray ("orderEvaluationInfoList");
        Integer count = ret.getInteger ("totalCount");
        return Result.success (new PageTableHandler (request1 -> count, request2 -> infos).handle (request));


    }
}
