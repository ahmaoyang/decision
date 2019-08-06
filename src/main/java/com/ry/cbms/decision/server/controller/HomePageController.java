package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.model.ThrowInfo;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.HomePageService;
import com.ry.cbms.decision.server.service.OrderInfoService;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.vo.SingleEvalDataVo;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author maoYang
 * @Date 2019/6/18 9:37
 * @Description 首页
 */
@Api(tags = "首页")
@RestController
@RequestMapping("/homePage")
public class HomePageController {
    @Autowired
    private HomePageService homePageService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ComUtil comUtil;

    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "首页负债收益数据", notes = ApiNote.getHomePageProfitAndLossData)
    @GetMapping("/getHomePageProfitAndLossData")
    public Result getHomePageProfitAndLossData(@ApiParam(value = "开始日期", required = true) @RequestParam(value = "startDate") String startDate,
                                               @ApiParam(value = "结束日期", required = true) @RequestParam("endDate") String endDate,
                                               @ApiParam(value = "币种", required = true) @RequestParam("currency") String currency,
                                               @ApiParam(value = "页数", required = true) @RequestParam(value = "offset") Integer offset,
                                               @ApiParam(value = "每页条数", required = true) @RequestParam(value = "limit", required = false) Integer limit) {
        if (!ComUtil.isDate (startDate) || !ComUtil.isDate (endDate)) {
            return Result.error ("请输入正确的日期格式");
        }
        if (offset > 0) {
            offset = offset - 1;
        }
        if (null == limit) {
            limit = 10;
        }
        offset = offset * limit;
        if (StringUtils.isEmpty (startDate)) {
            startDate = DateUtil.parserTo (DateUtil.getBeginDayOfMonth ());
        }
        // Object profitAndLossCollection = redisTemplate.opsForList ().range (RedisKeyGenerator.getLoadProfitAndLossCondition () + startDate + endDate + currency, offset, limit);
        List<SingleEvalDataVo> dataCollection = new ArrayList<> ();
        // Map<Object, SingleEvalDataVo> retMap = orderInfoService.SingleEval (startDate, endDate, currency, Constants.FLAG_DAY);
        List<String> dataList = DateUtil.getDays (startDate, endDate);
        dataList.forEach (it -> {
            Object cache = redisTemplate.opsForHash ().get (RedisKeyGenerator.getOrderEvaBalance (Constants.FLAG_DAY, currency), it);
            if (null != cache) {
                dataCollection.add ((SingleEvalDataVo) cache);

            }
        });
        List<String> totalDateList = DateUtil.getDays ("2018-07-01", endDate); //2018-07-01 作为开始时间
        List<SingleEvalDataVo> totalDataCollection = new ArrayList<> ();
        totalDateList.forEach (it -> {
            Object cache = redisTemplate.opsForHash ().get (RedisKeyGenerator.getOrderEvaBalance (Constants.FLAG_DAY, currency), it);
            if (null != cache) {
                totalDataCollection.add ((SingleEvalDataVo) cache);

            }
        });
        Map resultMap = new HashMap ();
        List profitList = new LinkedList ();
        List lossList = new LinkedList ();
        for (int i = 0, len = dataCollection.size (); i < len; i++) {
            Map profitMap = new HashMap (); //收益集合
            Map lossMap = new HashMap ();//负债集合
            SingleEvalDataVo singleEvalDataVo = null;
            try {
                singleEvalDataVo = dataCollection.get (i);
            } catch (Exception e) {
                continue;
            }
            profitMap.put ("createDate", singleEvalDataVo.getCreateDate ());
            profitMap.put ("cashedBalance", singleEvalDataVo.getCashedBalance ());
            profitMap.put ("throwTradeNum", singleEvalDataVo.getThrowTradeNum ());
            profitMap.put ("notThrowTradeNum", singleEvalDataVo.getNotThrowTradeNum ());
            profitMap.put ("totalTradeNum", singleEvalDataVo.getTotalTradeNum ());
            profitMap.put ("ratio", singleEvalDataVo.getRatio ());
            lossMap.put ("createDate", singleEvalDataVo.getCreateDate ());
            lossMap.put ("retailAccountRmbBalance", singleEvalDataVo.getRetailAccountRmbBalance ());
            lossMap.put ("retailAccountDollarBalance", singleEvalDataVo.getRetailAccountDollarBalance ());
            lossMap.put ("closedProfit", singleEvalDataVo.getClosedProfit ());
            lossMap.put ("commBalance", singleEvalDataVo.getCommBalance ());
            profitList.add (profitMap);
            lossList.add (lossMap);
        }
        Double cashedBalance = 0.0;
        Double throwTradeNum = 0.0;
        Double notThrowTradeNum = 0.0;
        Double totalTradeNum = 0.0;
        Double ratio = 0.0;
        Double retailAccountRmbBalance = 0.0;
        Double retailAccountDollarBalance = 0.0;
        Double closedProfit = 0.0;
        Double commBalance = 0.0;
        Map profitTotalMap = new HashMap ();
        Map lossTotalMap = new HashMap ();
        SingleEvalDataVo singleEvalDataVo;
        for (int j = 0, len = totalDataCollection.size (); j < len; j++) {
            singleEvalDataVo = totalDataCollection.get (j);
            cashedBalance += Double.valueOf (singleEvalDataVo.getCashedBalance ());
            throwTradeNum += Double.valueOf (singleEvalDataVo.getThrowTradeNum ());
            notThrowTradeNum += Double.valueOf (singleEvalDataVo.getNotThrowTradeNum ());
            totalTradeNum += Double.valueOf (singleEvalDataVo.getTotalTradeNum ());
            ratio += Double.valueOf (singleEvalDataVo.getRatio ());
            retailAccountRmbBalance += Double.valueOf (singleEvalDataVo.getRetailAccountRmbBalance ());
            retailAccountDollarBalance += Double.valueOf (singleEvalDataVo.getRetailAccountDollarBalance ());
            closedProfit += Double.valueOf (singleEvalDataVo.getClosedProfit ());
            commBalance += Double.valueOf (singleEvalDataVo.getCommBalance ());
        }
        profitTotalMap.put ("cashedBalanceTotal", cashedBalance);
        profitTotalMap.put ("throwTradeNumTotal", throwTradeNum);
        profitTotalMap.put ("notThrowTradeNumTotal", notThrowTradeNum);
        profitTotalMap.put ("totalTradeNumTotal", totalTradeNum);
        profitTotalMap.put ("ratioTotal", ratio);
        lossTotalMap.put ("retailAccountRmbBalanceTotal", retailAccountRmbBalance);
        lossTotalMap.put ("retailAccountDollarBalanceTotal", retailAccountDollarBalance);
        lossTotalMap.put ("closedProfitTotal", closedProfit);
        lossTotalMap.put ("commBalanceTotal", commBalance);
        profitList.add (0, profitTotalMap);
        lossList.add (0, lossTotalMap);
        PageTableRequest request = new PageTableRequest ();
        request.setOffset (offset);
        request.setLimit (limit);
        int lossListSize = lossList.size ();
        int profitListSize = profitList.size ();
        List showProfitList = new ArrayList ();
        List showLossList = new ArrayList ();
        for (int i = offset; i < offset+limit; i++) {
            showProfitList.add (profitList.get (i));
            showLossList.add (lossList.get (i));
        }

        resultMap.put ("profit", new PageTableHandler (request1 -> profitListSize, request2 -> showProfitList).handle (request));
        resultMap.put ("loss", new PageTableHandler (request1 -> lossListSize, request2 -> showLossList).handle (request));
        return Result.success (resultMap);
    }


    @ApiOperation(value = "首页最新刷新时间", notes = "lastRefreshTime:最新刷新时间")
    @GetMapping("/getHomePageLastRefreshTime")
    public Result getHomePageLastRefreshTime() {
        Object cashValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getLastRefreshTime (getClass ().getSimpleName () + Constants.GET_HOME_PAGR_DATA));
        Map resultMap = new HashMap ();
        if (!StringUtils.isEmpty (cashValue)) {
            resultMap.put ("lastRefreshTime", cashValue.toString ());
        } else {
            resultMap.put ("lastRefreshTime", "");
        }
        return Result.success (resultMap);
    }

    /**
     * 首页账户数据
     *
     * @param currency
     * @return
     */
    @GetMapping("/getHomePageData")
    @ApiOperation(value = "首页账户数据", notes = "返回结果：businessBalance:经济商账户余额，bondBalance保证金账户余额，retailBalance零售账户余额，commBalance 返佣账户余额")
    public Result getHomePageData(@ApiParam("币种(默认人民币)") @RequestParam(value = "currency", required = false) String currency) {
        if (StringUtils.isEmpty (currency)) {
            currency = Constants.CURRENCY_RMB;//默认人民币账户
        }
        Map resultMap = homePageService.getHomePageData (currency, Constants.FLAG_DAY);//默认展示人民币账户，近7天数据
        List dataList = new ArrayList ();
        Object businessBalance = resultMap.get ("businessBalance");//经纪商账户余额
        Object bondBalance = resultMap.get ("bondBalance");//保证金账户余额
        Object retailBalance = resultMap.get ("retailBalance");//零售端账户余额
        Object commBalance = resultMap.get ("commBalance");//返佣账户余额

        HashMap businessMap = new HashMap ();
        HashMap bondMap = new HashMap ();
        HashMap retailMap = new HashMap ();
        HashMap commMap = new HashMap ();
        businessMap.put ("name", "经纪商账户余额");
        businessMap.put ("balance", businessBalance);
        businessMap.put ("symbol", "¥");
        businessMap.put ("accType", "accBroker");

        bondMap.put ("name", "保证金账户余额");
        bondMap.put ("balance", bondBalance);
        bondMap.put ("symbol", "$");
        bondMap.put ("accType", "accBond");

        retailMap.put ("name", "零售账户余额账户余额");
        retailMap.put ("balance", retailBalance);
        retailMap.put ("symbol", "$");
        retailMap.put ("accType", "accRetail");

        commMap.put ("name", "返佣账户余额");
        commMap.put ("balance", commBalance);
        commMap.put ("symbol", "$");
        commMap.put ("accType", "accReturn");

        dataList.add (businessMap);
        dataList.add (bondMap);
        dataList.add (retailMap);
        dataList.add (commMap);
        comUtil.setLastRefreshTime (null, null, null, getClass ().getSimpleName () + Constants.GET_HOME_PAGR_DATA);
        return Result.success (dataList);
    }

    /**
     * 根据颗粒度查询账户数据
     *
     * @param currency
     * @param accType
     * @return
     */
    @GetMapping("/geAccDataByDate")
    @ApiOperation(value = "据颗粒度查询账户数据", notes = ApiNote.geAccDataByDate)
    public Result geAccDataByFlag(@ApiParam("币种(默认人民币)") @RequestParam(value = "currency", required = false) String currency,
                                  @ApiParam("账户类别 accBroker经济上账户，accBond保证金账户，accReturn 返佣账户，accRetail零售端账户") @RequestParam(value = "accType", required = false) String accType,
                                  @ApiParam(value = "开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                  @ApiParam(value = "结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        List retList;
        if (StringUtils.isEmpty (currency)) {
            currency = Constants.CURRENCY_RMB;//默认人民币
        }
        if (StringUtils.isEmpty (accType)) {
            accType = Constants.ACC_BROKER;//默认经纪商账户
        }
        Object cacheList = redisTemplate.opsForValue ().get (RedisKeyGenerator.getGeAccDataByDate (currency, accType, startDate, endDate));
        if (null != cacheList) {
            retList = (List) cacheList;
            return Result.success (retList);
        }
        retList = homePageService.geAccDataByDate (currency, startDate, endDate, accType);//默认展示人民币账户数据
        if (null != retList && retList.size () > 0) {
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getGeAccDataByDate (currency, accType, startDate, endDate), retList, 5, TimeUnit.MINUTES);
        }
        return Result.success (retList);
    }

}
