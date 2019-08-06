package com.ry.cbms.decision.server.controller;

import com.alibaba.fastjson.JSONArray;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.dao.CashInAndOutDao;
import com.ry.cbms.decision.server.dao.OrderInfoDao;
import com.ry.cbms.decision.server.model.OrderDetail;
import com.ry.cbms.decision.server.model.ThrowInfo;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.OrderInfoService;
import com.ry.cbms.decision.server.utils.ApiNote;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.vo.CurrencyVo;
import com.ry.cbms.decision.server.vo.OrderDetailVo;
import com.ry.cbms.decision.server.vo.SingleEvalDataVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * @Author maoYang
 * @Date 2019/5/24 15:07
 * @Description 订单信息相关
 */
@RestController
@RequestMapping("/orderInfo")
@Api(tags = "订单信息相关")
@Slf4j
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private ComUtil comUtil;

    @Autowired
    private CashInAndOutDao cashInAndOutDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderInfoDao orderInfoDao;


    @ApiOperation(value = "抛单历史", notes = ApiNote.throwOrder)
    @GetMapping("/getThrowHisSum")
    public Result getThrowHisSum(@ApiParam(value = "mt4账号") @RequestParam(value = "account", required = false) String account,
                                 @ApiParam(value = "用户userId") @RequestParam(value = "userId", required = false) String userId) {

        Object cashValue = null;
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {
            cashValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getThrowInfoHis (null));
        } else if (!StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {
            cashValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getThrowInfoHis (account));

        } else if (StringUtils.isEmpty (account) && !StringUtils.isEmpty (userId)) {
            account = comUtil.getMt4AccountsByUserId (userId);
            cashValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getThrowInfoHis (account));

        }
        return Result.success ((ThrowInfo) cashValue);
    }

    /**
     * 抛单信息
     *
     * @param account mt4 账户
     * @param userId  用户或代理Id
     * @return
     */
    @GetMapping("/throwOrder")
    @ApiOperation(value = "抛单信息", notes = ApiNote.throwOrder)
    public Result throwOrder(@ApiParam("mt4账户") @RequestParam(value = "account", required = false) String account,
                             @ApiParam("用户Id") @RequestParam(value = "userId", required = false) String userId,
                             @ApiParam("day 按天，week 按周 ，month 按月(默认查看天)") @RequestParam("flag") String flag,
                             @ApiParam("开始日期(默认查看本月)") @RequestParam(value = "startDate", required = false) String startDate,
                             @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        if (StringUtils.isEmpty (flag)) {
            flag = "day";//默认查看天
        }
        if (StringUtils.isEmpty (startDate)) {
            startDate = DateUtil.parserTo (DateUtil.getBeginDayOfMonth ());
        }
        if (StringUtils.isEmpty (endDate)) {
            endDate = DateUtil.getyyyyMMdd ();
        }
        if (StringUtils.isEmpty (account) && !StringUtils.isEmpty (userId)) {
            account = comUtil.getMt4AccountsByUserId (userId);
        }
        List<ThrowInfo> throwInfoList = orderInfoService.throwOrder (account, flag, startDate, endDate);
        return Result.success (throwInfoList);

    }

    /**
     * 单量评估
     *
     * @param flag      按天看，还是按月看标签
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param currency  账户类别
     * @return
     */
    @ApiOperation(value = "单量评估图像数据", notes = ApiNote.getOrderEva)
    @GetMapping("/getOrderEvaDataList")
    public Result getOrderEvaDataList(@ApiParam(value = "day 按天，month 按月", required = true) @RequestParam("flag") String flag,
                              @ApiParam(value = "开始日期(不传默认本月第一天)") @RequestParam(value = "startDate", required = false) String startDate,
                              @ApiParam(value = "结束日期") @RequestParam(value = "endDate", required = false) String endDate,
                              @ApiParam(value = "币种（账户）类别", required = true) @RequestParam("accType") String currency) {
        if (!("day".equals (flag) || "month".equals (flag))) {
            return Result.error ("请输入正确查询颗粒度(month或day)");
        }
        if (StringUtils.isEmpty (startDate)) {
            startDate = DateUtil.parserTo (DateUtil.getBeginDayOfMonth ());
            if (StringUtils.isEmpty (endDate)) {
                endDate = DateUtil.parserTo (new Date ());
            }
        } else {
            if (!(ComUtil.isDate (startDate))) {
                return Result.error ("请输入正确开始查询日期");
            }
            if (!(ComUtil.isDate (endDate))) {
                return Result.error ("请输入正确结束查询日期");
            }
        }
        List<SingleEvalDataVo> collection = new ArrayList<> ();
        if (Constants.FLAG_MONTH.equals (flag)) { //按月查询
            Date start = DateUtil.praiseString2Date (startDate);
            Date end = DateUtil.lastMonthDay (DateUtil.praiseString2Date (endDate));
            List<String> list = DateUtil.getDays (DateUtil.parserTo (start), DateUtil.parserTo (end));
            list.forEach (it -> {
                String year = it.substring (0, 4);
                String month = it.substring (6, 7);
                String dataMonth = DateUtil.getDateLastDay (year, (Integer.valueOf (month) - 1) + "");
                Date dateIt = DateUtil.parse (it);
                Date dateMonth = DateUtil.parse (dataMonth);
                if (dateIt.getTime () == dateMonth.getTime ()) {
                    Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getBrokerBalanceRecord (currency), dateMonth);
                    SingleEvalDataVo singleEvalDataVo=new SingleEvalDataVo ();
                    if(null==cacheValue){
                        cacheValue=0;
                    }
                    singleEvalDataVo.setBrokerBalance (cacheValue.toString ());
                    singleEvalDataVo.setCreateDate (dataMonth);
                    Object bondCacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getBondBalanceRecord (Constants.FLAG_DAY), dateMonth);
                    if(null==bondCacheValue){
                        bondCacheValue=0;
                    }
                    singleEvalDataVo.setBondBalance (bondCacheValue.toString ());
                        collection.add (singleEvalDataVo);
                }
            });

        }
        if (Constants.FLAG_DAY.equals (flag)) { //按天查询
            List<String> list = DateUtil.getDays (startDate, endDate);
            list.forEach (it -> {
                Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getBrokerBalanceRecord (currency), it);
                SingleEvalDataVo singleEvalDataVo=new SingleEvalDataVo ();
                if(null==cacheValue){
                    cacheValue=0;
                }
                singleEvalDataVo.setBrokerBalance (cacheValue.toString ());
                singleEvalDataVo.setCreateDate (it);
                Object bondCacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getBondBalanceRecord (Constants.FLAG_DAY), it);
                if(null!=bondCacheValue){
                    singleEvalDataVo.setBondBalance (bondCacheValue.toString ());
                }else{
                    singleEvalDataVo.setBondBalance ("0");

                }
                collection.add (singleEvalDataVo);
            });
        }
        if (Constants.FLAG_DAY.equals (flag)) {
            comUtil.orderByDate (collection);
        } else {
            orderByMonthDate (collection);
        }
        return Result.success (collection);

    }




    /**
     * 单量评估
     *
     * @param flag      按天看，还是按月看标签
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param currency  账户类别
     * @return
     */
    @ApiOperation(value = "单量评估", notes = ApiNote.getOrderEva)
    @GetMapping("/getOrderEva")
    public Result getOrderEva(@ApiParam(value = "day 按天，month 按月", required = true) @RequestParam("flag") String flag,
                              @ApiParam(value = "开始日期(不传默认本月第一天)") @RequestParam(value = "startDate", required = false) String startDate,
                              @ApiParam(value = "结束日期") @RequestParam(value = "endDate", required = false) String endDate,
                              @ApiParam(value = "币种（账户）类别", required = true) @RequestParam("accType") String currency) {
        if (!("day".equals (flag) || "month".equals (flag))) {
            return Result.error ("请输入正确查询颗粒度(month或day)");
        }
        if (StringUtils.isEmpty (startDate)) {
            startDate = DateUtil.parserTo (DateUtil.getBeginDayOfMonth ());
            if (StringUtils.isEmpty (endDate)) {
                endDate = DateUtil.parserTo (new Date ());
            }
        } else {
            if (!(ComUtil.isDate (startDate))) {
                return Result.error ("请输入正确开始查询日期");
            }
            if (!(ComUtil.isDate (endDate))) {
                return Result.error ("请输入正确结束查询日期");
            }
        }
        List<SingleEvalDataVo> collection = new ArrayList<> ();
        if (Constants.FLAG_MONTH.equals (flag)) { //按月查询
            Date start = DateUtil.praiseString2Date (startDate);
            Date end = DateUtil.lastMonthDay (DateUtil.praiseString2Date (endDate));
            List<String> list = DateUtil.getDays (DateUtil.parserTo (start), DateUtil.parserTo (end));
            list.forEach (it -> {
                String year = it.substring (0, 4);
                String month = it.substring (6, 7);
                String dataMonth = DateUtil.getDateLastDay (year, (Integer.valueOf (month) - 1) + "");
                Date dateIt = DateUtil.parse (it);
                Date dateMonth = DateUtil.parse (dataMonth);
                if (dateIt.getTime () == dateMonth.getTime ()) {
                    Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getOrderEvaBalance (Constants.FLAG_MONTH, currency), it);
                    if (null != cacheValue) {
                        collection.add ((SingleEvalDataVo) cacheValue);
                    }
                }
            });

        }
        if (Constants.FLAG_DAY.equals (flag)) { //按天查询
            List<String> list = DateUtil.getDays (startDate, endDate);
            list.forEach (it -> {
                Object cache = redisTemplate.opsForHash ().get (RedisKeyGenerator.getOrderEvaBalance (Constants.FLAG_DAY, currency), it);
                if (null != cache) {
                    collection.add ((SingleEvalDataVo) cache);

                }
            });
        }
        if (Constants.FLAG_DAY.equals (flag)) {
            comUtil.orderByDate (collection);
        } else {
            orderByMonthDate (collection);
        }
        return Result.success (collection);

    }

    private void orderByMonthDate(List<SingleEvalDataVo> collection) {
        collection.sort ((a, b) -> {
            Date dateA = DateUtil.praiseStringToDateYYYY_MM (a.getCreateDate ());
            Date dateB = DateUtil.praiseStringToDateYYYY_MM (b.getCreateDate ());

            if (dateA.after (dateB)) {
                return 1;
            }
            if (dateA.before (dateB)) {
                return -1;
            } else {
                return 0;
            }

        });
    }

    /**
     * 单量评估历史
     *
     * @param currency 账户类别
     * @return
     */
    @ApiOperation(value = "单量评估历史", notes = ApiNote.getOrderEva)
    @GetMapping("/getOrderEvaHis")
    public Result getOrderEva(@ApiParam(value = "币种（账户）类别", required = true) @RequestParam("accType") String currency) {

        Map<Object, SingleEvalDataVo> retMap = orderInfoService.SingleEval (null, null, currency, Constants.FLAG_DAY);
        List orderList = new ArrayList (retMap.values ());
        Map resultMap = new HashMap ();
        if (null != retMap) {
            orderByTimeDesc (orderList);
            resultMap = sumHis (orderList, currency);
        }
        return Result.success (resultMap);
    }

    private void orderByTimeDesc(List<SingleEvalDataVo> list) {
        list.sort ((a, b) -> {
            try {
                if (DateUtil.parse (a.getCreateDate ()).before (DateUtil.parse (b.getCreateDate ()))) {
                    return 1;
                } else if (DateUtil.parse (a.getCreateDate ()).after (DateUtil.parse (b.getCreateDate ()))) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                return 1;
            }
        });
    }

    private Map sumHis(List<SingleEvalDataVo> orderList, String currency) { //计算单量评估历史
        Map retMap = new HashMap ();
        Double retailAccountRmbBalance = 0.0;
        Double retailAccountDollarBalance = 0.0;
        String ratio;
        Double closedProfit = 0.0;
        Double commBalance = 0.0;
        Double cashedBalance = 0.0;
        Double throwTradeNum = 0.0;
        Double notThrowTradeNum = 0.0;
        Double totalTradeNum = 0.0;
        Object bondBalance1 = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTermimusBalance (Constants.TERMIMUS_LOG1));
        Object bondBalance2 = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTermimusBalance (Constants.TERMIMUS_LOG2));
        if (StringUtils.isEmpty (bondBalance1)) {
            bondBalance1 = "0";
        }
        if (StringUtils.isEmpty (bondBalance2)) {
            bondBalance2 = "0";
        }
        SingleEvalDataVo singleEvalDataVo1 = orderList.get (0);
        ratio = singleEvalDataVo1.getRatio ();
        for (int i = 0, len = orderList.size (); i < len; i++) {
            SingleEvalDataVo singleEvalDataVo = orderList.get (i);
            retailAccountDollarBalance += Double.valueOf (singleEvalDataVo.getRetailAccountDollarBalance ());
            closedProfit += Double.valueOf (singleEvalDataVo.getClosedProfit ());
            commBalance += Double.valueOf (singleEvalDataVo.getThrowTradeNum ());
            cashedBalance += Double.valueOf (singleEvalDataVo.getCashedBalance ());
            throwTradeNum += Double.valueOf (singleEvalDataVo.getThrowTradeNum ());
            notThrowTradeNum += Double.valueOf (singleEvalDataVo.getNotThrowTradeNum ());
            totalTradeNum += Double.valueOf (singleEvalDataVo.getTotalTradeNum ());
        }
        Object brokerCurrBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountBalance (currency));
        Double bondBalance = Double.valueOf (bondBalance1.toString ()) + Double.valueOf (bondBalance2.toString ());
        if(null==brokerCurrBalance){
            brokerCurrBalance=0;
        }
        retailAccountRmbBalance=Double.valueOf (brokerCurrBalance.toString ())-Double.valueOf (cashedBalance);
        retMap.put ("brokerBalance", brokerCurrBalance);
        retMap.put ("bondBalance", bondBalance);
        retMap.put ("retailAccountRmbBalance", retailAccountRmbBalance);
        retMap.put ("retailAccountDollarBalance", retailAccountDollarBalance);
        retMap.put ("ratio", ratio);
        retMap.put ("closedProfit", closedProfit);
        retMap.put ("commBalance", commBalance);
        retMap.put ("cashedBalance ", cashedBalance);

        retMap.put ("throwTradeNum", throwTradeNum);
        retMap.put ("notThrowTradeNum", notThrowTradeNum);
        retMap.put ("totalTradeNum", totalTradeNum);
        return retMap;
    }


    @ApiOperation(value = "经纪商账户余额信息", notes = ApiNote.getBrokerAccountInfo)
    @GetMapping("/getBrokerAccountInfo")
    public Result getBrokerAccountInfo() {
        List<CurrencyVo> currencyVoList = comUtil.getCurrencyKinds ();
        Map<String, Object> retMap = new HashMap<> ();
        List retList = new ArrayList ();
        currencyVoList.forEach (currencyVo -> {
            String currency = currencyVo.getToCurrency ();
            String currencyName = currencyVo.getMessage ();
            String currencyFlag = currencyVo.getCurrencySymbol ();
            if (!StringUtils.isEmpty (currency)) {
                Object bondBalance1 = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTermimusBalance (Constants.TERMIMUS_LOG1));
                Object bondBalance2 = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTermimusBalance (Constants.TERMIMUS_LOG2));
                if (StringUtils.isEmpty (bondBalance1)) {
                    bondBalance1 = "0";
                }
                if (StringUtils.isEmpty (bondBalance2)) {
                    bondBalance2 = "0";
                }
                Object accBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountBalance (currency));
                if (StringUtils.isEmpty (accBalance)) {
                    accBalance = "0";
                }
                Object accPreBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalance (currency));//经纪商账户上期余额
                if (StringUtils.isEmpty (accPreBalance)) {
                    accPreBalance = "0";
                }
                Object cashedBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCashedBalance (null, currency));//兑付余额
                if (StringUtils.isEmpty (cashedBalance)) {
                    cashedBalance = "0";
                }
                retMap.put ("currencyName", currencyName);//货币名称
                retMap.put ("currencyFlag", currencyFlag);//货币标识
                retMap.put ("currency", currency);//货币英文字符
                retMap.put ("brokerAccBalance", accBalance);//经济商账户余额
                retMap.put ("brokerPreAccBalance", accPreBalance);//经济商账户上期余额
                retMap.put ("cashedBalance", cashedBalance);
                retMap.put ("bondBalance", Double.valueOf (bondBalance1.toString ()) + Double.valueOf (bondBalance2.toString ()));
                retList.add (retMap);
            }
        });

        return Result.success (retList);
    }

    /**
     * 账户明细
     *
     * @param account   mt4 账户
     * @param userId    用户或代理Id
     * @param accType   账户类型
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return
     */
    @ApiOperation(value = "账户明细", notes = ApiNote.getAccountDetail_order)
    @GetMapping("/getAccountDetail")
    public Result getAccountDetail(@ApiParam("mt4账户") @RequestParam(value = "account", required = false) String account,
                                   @ApiParam("用户Id") @RequestParam(value = "userId", required = false) String userId,
                                   @ApiParam(value = "账户类别(币种)", required = true) @RequestParam(value = "accType") String accType,
                                   @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                   @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        Map resultMap;
        resultMap = orderInfoService.geAccDetail (account, userId, accType, startDate, endDate);
        Map retMap = orderInfoService.geAccDetailList (account, userId, accType, startDate, endDate, 0, 10);//取默认第一页10条记录
        Collection<Map> dataList = (Collection) retMap.get ("dataValues");

        if (null != dataList) {
            resultMap.put ("accList", dataList);
        }
        comUtil.setLastRefreshTime (endDate, startDate, resultMap, getClass ().getSimpleName () + "getAccountDetail");
        return Result.success (resultMap);

    }

    /**
     * 获取账户明细-指定时间筛选查询
     *
     * @param account
     * @param userId
     * @param accType
     * @param startDate
     * @param endDate
     * @param offset
     * @param limit
     * @return
     */
    @ApiOperation(value = "具体账户明细-指定时间筛选查询", notes = ApiNote.getAccountDataList)
    @GetMapping("/getAccountDataList")
    public Result getAccountDataList(@ApiParam("mt4账户") @RequestParam(value = "account", required = false) String account,
                                     @ApiParam("用户Id") @RequestParam(value = "userId", required = false) String userId,
                                     @ApiParam("账户类型（传币种类型）") @RequestParam(value = "accType", required = false) String accType,
                                     @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                     @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate,
                                     @ApiParam("页数(0表示第一页)") @RequestParam(value = "offset", required = false) Integer offset,
                                     @ApiParam("每页条数") @RequestParam(value = "limit", required = false) Integer limit) {
        PageTableRequest request = new PageTableRequest ();
        if (null == offset) {
            offset = 0;
        } else if (offset > 0) {
            offset = offset - 1;
        }
        if (null == limit) {
            limit = 10;
        }
        request.setOffset (offset * limit);
        request.setLimit (limit);
        Map resultMap = new HashMap ();
        Map retMap = orderInfoService.geAccDetailList (account, userId, accType, startDate, endDate, offset, limit);//取默认第一页10条记录
        Collection dataValues = (Collection) retMap.get ("dataValues");
        List list = new ArrayList (dataValues);
        Integer count = null == retMap.get ("count") ? 0 : (Integer) retMap.get ("count");
        resultMap.put ("accList", new PageTableHandler (request1 -> count, request2 -> list).handle (request));
        comUtil.setLastRefreshTime (endDate, startDate, resultMap, getClass ().getSimpleName () + "getAccountDataList");
        return Result.success (resultMap);

    }

    /**
     * 查询特定账号的 入金 ，出金，转入，佣金出金 详细记录
     *
     * @param account
     * @param startDate
     * @param endDate
     * @return
     */
    @ApiOperation(value = "询特定账号的 入金 ，出金，转入，佣金出金 详细记录", notes = ApiNote.getSpecifiedAccountDetail)
    @GetMapping("/getSpecifiedAccountDetail")
    public Result getSpecifiedAccountDetail(@ApiParam(value = "mt4账户", required = true) @RequestParam(value = "account") String account,
                                            @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                            @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate,
                                            @ApiParam("查询标识( 0 入金，1出金，2转入，3佣金出金(默认0))") @RequestParam(value = "flag", required = false) String flag) {
        if (StringUtils.isEmpty (flag)) {
            flag = "0";
        }
        if (!(flag.equals ("0") || flag.equals ("1") || flag.equals ("2") || flag.equals ("3"))) {
            return Result.error ("查询标识无效");
        }
        if (!StringUtils.isEmpty (endDate)) {
            endDate = DateUtil.parserTo (DateUtil.addOrReduceDay (DateUtil.parse (endDate), 1));
        }
        Map retMap = orderInfoService.getSpecifiedAccountDetail (account, startDate, endDate, Constants.ACC_FROM_TYPE_COMM, flag);
        return Result.success (retMap);
    }

    /**
     * 订单明细
     *
     * @param account   mt4 账户
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param orderFlag 排序标签
     * @param offset    页数* 每页条数
     * @param limit     每页条数
     * @return
     */
    @ApiOperation(value = "订单(明细)列表", notes = ApiNote.orderDetailList)
    @GetMapping("/orderDetailList")
    public Result getOrderDetails(@ApiParam("mt4账户") @RequestParam(value = "account", required = false) String account,
                                  @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                  @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate,
                                  @ApiParam("排序标示") @RequestParam(value = "orderFlag", required = false) String orderFlag,
                                  @ApiParam(value = "页数", required = true) @RequestParam(value = "offset") Integer offset,
                                  @ApiParam(value = "每页条数", required = true) @RequestParam(value = "limit", required = false) Integer limit) {

        Map resultMap;
        if (null == offset) {
            offset = 0;
        } else if (offset > 0) {
            offset = offset - 1;
        }
        if (null == limit) {
            limit = 10;
        }
        PageTableRequest request = new PageTableRequest ();
        request.setOffset (offset * limit);
        request.setLimit (limit);
        resultMap = orderInfoService.getOrderDetails (account, orderFlag, startDate, endDate, request.getOffset (), request.getLimit ());
        Integer count = (Integer) resultMap.get ("count");
        List<OrderDetail> orderDetailList = (List) resultMap.get ("orderDetailList");
        Map<String, Object> totalDetailData = (Map<String, Object>) resultMap.get ("totalDetailData");
        resultMap.clear ();
        comUtil.setLastRefreshTime (endDate, startDate, resultMap, getClass ().getSimpleName () + "orderDetailList");
        resultMap.put ("totalDetailData", totalDetailData);
        resultMap.put ("commInfos", new PageTableHandler (request1 -> count, request2 -> orderDetailList).handle (request));
        return Result.success (resultMap);
    }


    /**
     * 查询特定账号的订单记录
     *
     * @param account mt4账户
     * @return
     */
    @ApiOperation(value = "查询特定账号的订单记录", notes = ApiNote.getOrderRecordByAccount)
    @GetMapping("/getOrderRecordByAccount")
    public Result getOrderRecordByAccount(@ApiParam(value = "mt4账户", required = true) @RequestParam(value = "account") String account,
                                          @ApiParam("开始日期") @RequestParam(value = "startDate", required = false) String startDate,
                                          @ApiParam("结束日期") @RequestParam(value = "endDate", required = false) String endDate) {
        List<OrderDetailVo> orderDetailList = new ArrayList<> ();
        if (StringUtils.isEmpty (account)) {
            return Result.error ("账号不能为空");
        }
        if (!StringUtils.isEmpty (startDate)) {
            if (ComUtil.isDate (DateUtil.parser (DateUtil.stringParserToDate (startDate)))) {
                return Result.error ("日期格式不正确");
            }
        }
        if (!StringUtils.isEmpty (endDate)) {
            if (ComUtil.isDate (DateUtil.parser (DateUtil.stringParserToDate (endDate)))) {
                return Result.error ("日期格式不正确");
            }
        }
        Object orderObjectList;
        if (StringUtils.isEmpty (startDate) && StringUtils.isEmpty (endDate)) {
            orderObjectList = redisTemplate.opsForHash ().get (RedisKeyGenerator.getLoadOrderRecordByAccount (null, null), account);
        } else {
            orderObjectList = orderInfoService.getOrderRecordByAccountAndDate (account, startDate, endDate);
        }
        if (null != orderObjectList) {
            orderDetailList = (List) orderObjectList;
        }
        return Result.success (orderDetailList);
    }
}
