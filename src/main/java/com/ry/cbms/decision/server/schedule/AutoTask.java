package com.ry.cbms.decision.server.schedule;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.dao.*;
import com.ry.cbms.decision.server.model.*;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.*;
import com.ry.cbms.decision.server.utils.*;
import com.ry.cbms.decision.server.vo.CurrencyVo;
import com.ry.cbms.decision.server.vo.OrderDetailVo;
import com.ry.cbms.decision.server.vo.SingleEvalDataVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author maoYang
 * @Date 2019/5/28 16:15
 * @Description:自动执行任务
 */
@Component
@Slf4j
public class AutoTask {
    private static List<String> accTypes = new ArrayList<> ();
    @Autowired
    private CommissionDao commissionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Mt4LogInUtil mt4LogInUtil;

    @Autowired
    private RetailService retailService;

    @Autowired
    private RetailDao retailDao;

    @Autowired
    private AccessGoldCheckDao accessGoldCheckDao;

    @Autowired
    private OrderEvaluationInfoDao orderEvaluationInfoDao;

    @Autowired
    private CashInAndOutDao cashInAndOutDao;

    @Autowired
    private ComUtil comUtil;

    @Autowired
    private HomePageService homePageService;

    @Autowired
    private ThrowOrderUtil throwOrderUtil;

    @Autowired
    private OrderInfoDao orderInfoDao;

    @Autowired
    private BondInfoService bondInfoService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private CashedBalanceDao cashedBalanceDao;

    @Autowired
    private BusinessAccountService businessAccountService;

    @Autowired
    private RetailBalanceDao retailBalanceDao;


    static {   //加载账户类型
        accTypes.add (Constants.ACC_BROKER);//经纪商账户
        accTypes.add (Constants.ACC_BOND);//保证金账户
        accTypes.add (Constants.ACC_RETAIL);//零售端账户
        accTypes.add (Constants.ACC_RETURN);//返佣账户
    }

    /**
     * 跟新所有用户的上期佣金余额
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void refreshPreBalance() {
        List<Map> list = commissionDao.selectAllAccs ();
        Double totalSum = 0.0;
        if (null != list && list.size () > 0) {
            for (Map map : list) {
                Object userId = map.get ("userid");
                Object allBalance = map.get ("allBalance");
                if (null == allBalance) {
                    allBalance = 0;
                }
                totalSum += Double.valueOf (allBalance.toString ());
                if (null != userId) {
                    redisTemplate.opsForValue ().set (RedisKeyGenerator.getCommPreBalance (userId.toString ()), allBalance.toString ());//将每个用户的上期余额保存
                }
            }
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getCommPreBalance (), Double.valueOf (totalSum.toString ()));//总的上期佣金余额
        }

    }

    /**
     * 刷新保证金余额(两种保证金余额 5分钟一次)
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void autoRefreshBond() {
        comUtil.doCycleForBond (true);

    }


    /**
     * 获取每天的保证金余额
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void getBondBalance() {
        Object cacheTerminusToken = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTerminusToken ());
        if (null != cacheTerminusToken) {
            List<String> accList = new ArrayList<> ();
            accList.add (Constants.TERMIMUS_LOG1);
            accList.add (Constants.TERMIMUS_LOG2);
            cycleBody (cacheTerminusToken.toString (), accList);
        }
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void loadBrokerBalance() {  //经纪商现金余额
        List list2 = DateUtil.getTimeList (2018, 7, DateUtil.getNowYear (), DateUtil.getNowMonth (), 1);
        list2.forEach (it -> {
            List arr = (List) it;
            arr.forEach (time -> {
                String startDate = DateUtil.parserTo ((Date) time);
                List<CurrencyVo> currencys = comUtil.getCurrencyKinds ();
                currencys.forEach (currencyVo -> {
                    String currency = currencyVo.getToCurrency ();
                    BigDecimal cashInSum = cashInAndOutDao.getChannelCashInSumBalance (currency, startDate);
                    BigDecimal cashOutSum = cashInAndOutDao.getChannelCashOutSumBalance (currency, startDate);
                    if (null == cashInSum) {
                        cashInSum = new BigDecimal (0.0);
                    }
                    if (null == cashOutSum) {
                        cashOutSum = new BigDecimal (0.0);
                    }
                    Double balance = Double.valueOf (Double.valueOf (cashInSum.toString ()) - Double.valueOf (cashOutSum.toString ()));
                    redisTemplate.opsForHash ().put (RedisKeyGenerator.getBrokerBalanceRecord (currency), startDate, balance);
                });
            });
        });
    }


    private void cycleBody(String cacheTerminusToken, List<String> accList) {
        List list2 = DateUtil.getTimeList (2018, 7, DateUtil.getNowYear (), DateUtil.getNowMonth (), 1);
        list2.forEach (it -> {
            List arr = (List) it;
            arr.forEach (time -> {
                String dataMonth = DateUtil.parserTo01 ((Date) time);
                String dateDay = DateUtil.parserTo ((Date) time);
                Double balance = 0.0;
                //Object cacheDayValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getBondBalanceRecord (Constants.FLAG_DAY), dateDay); //按天的保证金余额
                for (int i = 0, len = accList.size (); i < len; i++) {
                    String acc = accList.get (i);
                    String bondBalance = requireBody (acc, dateDay, cacheTerminusToken);
                    if (null == bondBalance) {
                        bondBalance = "0";
                    }
                    balance += Double.valueOf (bondBalance);
                }
                redisTemplate.opsForHash ().put (RedisKeyGenerator.getBondBalanceRecord (Constants.FLAG_DAY), dateDay, balance); //按天的保证金余额
            });
        });

    }

    private String requireBody(String acc, String endDateMonth, String cacheTerminusToken) {
        JSONObject responseBody;//返回体
        String balance = null;
        Map<String, Object> retMap = new HashMap<> ();
        String url = Constants.TERMINUS_SERVER_URL + "api/findHistoricCashBalance";
        BasicNameValuePair accParam = new BasicNameValuePair ("accountNo", acc);
        BasicNameValuePair dateParam = new BasicNameValuePair ("date", endDateMonth);
        List<NameValuePair> nameValuePairList = new ArrayList<> ();
        nameValuePairList.add (accParam);
        nameValuePairList.add (dateParam);
        responseBody = HttpUtil2.doGet (url, cacheTerminusToken, nameValuePairList);
        if (null == responseBody) {
            throw new GlobalException (CodeMsg.TERMINUS_DOWN.getMsg ());
        }
        if ("0".equals (responseBody.getString ("code"))) { //请求成功
            JSONArray jsonArr = responseBody.getJSONArray ("data");
            if (null != jsonArr && jsonArr.size () > 0) {
                JSONObject object = jsonArr.getJSONObject (0);
                balance = object.getString ("balance");

            }
        }
        return balance;
    }

    /**
     * 刷新上期保证金余额(两种保证金余额)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoRefreshPreBond() {
        comUtil.doCycleForBond (false);

    }

    @Scheduled(cron = "0 */20 * * * ?")
    public void autoLoginTerminus() {
        mt4LogInUtil.terminusLogIn ();
    }

    /**
     * 更新上期零售端本金
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoRefreshRetailPreBalance() {
        mt4LogInUtil.setRetailInfo (Boolean.FALSE);
    }

    /**
     * 5分钟一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoRefreshRetailNet() {
        mt4LogInUtil.setRetailInfo (Boolean.TRUE);
    }

    /**
     * 更新零售端信息（10分钟一次）
     */
    //@Scheduled(cron = "0 0/1 * * * ?")
    public void autoRefreshRetailInfo() {
        List<RetailAccount> retailAccounts = retailService.getAllRetailInfo ();
        if (null != retailAccounts && retailAccounts.size () > 0) {
            String todayDate = DateUtil.getyyyyMMdd ();
            retailDao.deleteRetailInfo (todayDate);
            retailDao.saveRetailInfo (retailAccounts); //以后。。
        }
    }

    /**
     * 计算任意连续20天交易量达到20的人(5秒一次)
     */
    @Scheduled(cron = "*/50 * * * * ?")
    public void calTradeCount() {
        Object allAccList = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserAccList ()); //所有账号
        if (null != allAccList) {
            List<String> accList = (List<String>) allAccList;
            for (String mt4Acc : accList) {
                Object object = redisTemplate.opsForValue ().get (RedisKeyGenerator.getForTrade (mt4Acc));
                if (null != object) {
                    continue;
                }
                List<Date> closeDates = accessGoldCheckDao.getAllClose (mt4Acc); //某个账号的所有平仓记录时间
                if (null != closeDates && closeDates.size () > 0) {
                    for (int i = 0, len = closeDates.size (); i < len; i++) {
                        Date closeTime = closeDates.get (i);
                        Object cacheObject = redisTemplate.opsForValue ().get (RedisKeyGenerator.getForTwentyTrade (mt4Acc));
                        if (null != cacheObject) {
                            continue;
                        }
                        Map orderMap = accessGoldCheckDao.getTwentyDaysOrderNum (mt4Acc, closeTime);
                        if (null != orderMap) {
                            Object orderNum = orderMap.get ("orderNum"); //当前日期计算的20天内的交易单数
                            if (null != orderNum && Integer.valueOf (orderNum.toString ()) >= 20) {
                                redisTemplate.opsForValue ().set (RedisKeyGenerator.getForTwentyTrade (mt4Acc), true);
                            }
                        }
                        Map volumeMap = accessGoldCheckDao.getTwentyDaysVolumeNum (mt4Acc, closeTime);

                        if (null != volumeMap) {
                            Object volumeNum = volumeMap.get ("volumeNum"); //当前日期计算的20天内的交易量
                            if (null != volumeNum && Double.valueOf (volumeNum.toString ()) >= 20) {
                                redisTemplate.opsForValue ().set (RedisKeyGenerator.getForTwentyTrade (mt4Acc), true);
                            }
                        }
                    }

                }

            }
        }


    }

    /**
     * 每10分钟加载一次 ，agent 用户id 加载到缓存
     */
    @Scheduled(cron = "0 */3 * * * ?")
    public List<String> getAllUserIds() {
        List<String> allIds = comUtil.getAllUserIds ();
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getAgentUserIds (), allIds);
        for (int i = 0, len = allIds.size (); i < len; i++) {
            String id = allIds.get (i);
            redisTemplate.opsForHash ().put (RedisKeyGenerator.getAgentUserIdHash (), id, id);
        }
        return allIds;
    }

    /**
     * 每10分钟加载一次 ，agent 用户mt4Acc加载到缓存
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void loadAllUserAccount() {
        List<String> allIds = comUtil.getAllUserIds (); //用户id集合
        String userIds = ComUtil.covListToString (allIds);
        List<String> accList = comUtil.getAccList (userIds);//mt4账户集合
        List<String> list2 = new ArrayList<> ();
        list2.add (null);
        accList.removeAll (list2);
        if (null != accList) {
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getAgentUserAccList (), accList);
        }
    }


    /**
     * 加载持仓订单信息（2分钟一次）
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    public void loadOrder() {
        if (log.isInfoEnabled ()) {
            log.info ("加载持仓订单信息落库{}", DateUtil.getyyyyMMdd ());
        }
        try {
            orderEvaluationInfoDao.deleteAll ();
        } catch (Exception e) {
            log.error ("{}", e);
        }
        Double digital = 0.01;
        String reqUrl = Constants.MT4_SERVER_URL + "user/openList";
        Object mt4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
        JSONObject res = null; //获取持仓订单信息
        try {
            res = HttpUtil2.doGet (reqUrl, mt4Token.toString (), null);
        } catch (Exception e) {
            log.error ("mt4宕机", e);
        }
        List<OrderEvaluationInfo> orderEvaluationInfoList = new ArrayList<> ();
        if ("0".equals (res.getString ("code"))) {
            JSONArray dateArr = res.getJSONArray ("data");
            if (null != dateArr && dateArr.size () > 0) {
                for (int i = 0, len = dateArr.size (); i < len; i++) {
                    JSONObject object = dateArr.getJSONObject (i);
                    Long openTime = object.getLong ("openTime") * 1000;
                    String createTime = DateUtil.timeStamp2Date (openTime, null);
                    Object symbolObj = object.get ("symbol");
                    OrderEvaluationInfo orderEvaluationInfo = new OrderEvaluationInfo ();
                    if (null != symbolObj) {
                        String symbol = symbolObj.toString ();
                        if (symbol.contains (".")) {
                            String varity;
                            try {
                                varity = symbol.split (".")[0];
                            } catch (Exception e) {
                                continue;
                            }
                            orderEvaluationInfo.setOrderVariety (varity);
                            String type;
                            try {
                                type = symbol.split (".")[1];
                            } catch (Exception e) {
                                type = null;
                            }
                            orderEvaluationInfo.setOrderType (type);

                        } else {
                            orderEvaluationInfo.setOrderVariety (symbol);
                            orderEvaluationInfo.setOrderType (null);
                        }
                        if (0 == object.getInteger ("cmd").intValue ()) { //多单
                            orderEvaluationInfo.setEmptyHands ("0");//多单将空单值设为0
                            if (null != object.getInteger ("volume")) {
                                orderEvaluationInfo.setMultipleHands (Double.valueOf (object.getInteger ("volume").intValue () * digital) + "");
                            } else {
                                orderEvaluationInfo.setMultipleHands ("0");
                            }
                        }
                        if (1 == object.getInteger ("cmd").intValue ()) { //空单
                            orderEvaluationInfo.setMultipleHands ("0");//空单将多单值设为0
                            if (null != object.getInteger ("volume")) {
                                orderEvaluationInfo.setEmptyHands (Double.valueOf (object.getInteger ("volume").intValue () * 0.01).toString ());
                            } else {
                                orderEvaluationInfo.setEmptyHands ("0");
                            }
                        }
                        orderEvaluationInfo.setNotCloseProfitsAndLoss (object.getBigDecimal ("profit"));
                        orderEvaluationInfo.setCreateTime (createTime);
                        orderEvaluationInfo.setMt4Acc (object.getInteger ("login"));
                        orderEvaluationInfo.setNetPosition ("0");//不落库默认取0
                        orderEvaluationInfo.setTradeNum (0);//不落库默认取0
                        orderEvaluationInfo.setTradeTotalHands ("0");//不落库默认取0
                        orderEvaluationInfoList.add (orderEvaluationInfo);
                    }
                }

                orderEvaluationInfoDao.save (orderEvaluationInfoList);


                //更新判断
                orderEvaluationInfoList.clear ();

            }
        }
    }

    private void calBalance() {
        List<CurrencyVo> currencyKinds = cashInAndOutDao.currencyKinds ();
        BigDecimal accBalance;
        BigDecimal cashInTotal;
        BigDecimal cashOutTotal;
        for (CurrencyVo currency : currencyKinds) {
            String toCurrency = currency.getToCurrency ();
            cashInTotal = cashInAndOutDao.getChannelCashInSum (toCurrency);
            cashOutTotal = cashInAndOutDao.getChannelCashOutSum (toCurrency);

            if (null == cashInTotal) {
                cashInTotal = new BigDecimal (0);
            }
            if (null == cashOutTotal) {
                cashOutTotal = new BigDecimal (0);
            }
            accBalance = cashInTotal.subtract (cashOutTotal);
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getAccountBalance (toCurrency), accBalance);//不同币种总的现金余额

        }
    }

    /**
     * 更新出入金现金余额(3分钟一次)
     */
    @Scheduled(cron = "0 0/3 * * * ?")
    private void refreshInOutBalance() {
        calBalance ();
    }

    /**
     * 更新出入金上期现金余额（每天 0点）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshPreInOutBalance() {
        refreshInOutBalanceForUserId (Boolean.FALSE);
    }

    /**
     * 更新出入金本期现金余额（）
     */
    @Scheduled(cron = "0 */3 * * * ?")
    public void refreshCurInOutBalance() {
        refreshInOutBalanceForUserId (Boolean.FALSE);
    }


    private void refreshInOutBalanceForUserId(Boolean flag) {  //true 本期。false 上期
        List<CurrencyVo> currencyKinds = cashInAndOutDao.currencyKinds ();
        BigDecimal accPreBalance;
        BigDecimal cashIn;
        BigDecimal cashOut;
        for (CurrencyVo curr : currencyKinds) {
            String toCurrency = curr.getToCurrency ();
            cashIn = cashInAndOutDao.getChannelCashInSum (toCurrency);
            cashOut = cashInAndOutDao.getChannelCashOutSum (toCurrency);
            if (null == cashIn) {
                cashIn = new BigDecimal (0);
            }
            if (null == cashOut) {
                cashOut = new BigDecimal (0);
            }
            accPreBalance = cashIn.subtract (cashOut);
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getAccountPreBalance (toCurrency), accPreBalance);//不同币种总的现金余额
            List<String> userIds = getAllUserIds ();
            if (null != userIds && userIds.size () > 0) {
                userIds.forEach (it -> {
                    BigDecimal cashInForP = cashInAndOutDao.getChannelCashInSumByCondition (null, toCurrency, it, null, null);
                    BigDecimal cashOutForP = cashInAndOutDao.getChannelCashOutSumByCondition (null, toCurrency, it, null, null);
                    if (null == cashInForP) {
                        cashInForP = new BigDecimal (0);
                    }
                    if (null == cashOutForP) {
                        cashOutForP = new BigDecimal (0);
                    }
                    if (flag) {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getAccountCurBalance (toCurrency, it), cashInForP.subtract (cashOutForP));//不同币种每个UserId的现金余额
                    }
                    if (!flag) {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getAccountPreBalanceForP (toCurrency, it), cashInForP.subtract (cashOutForP));//不同币种每个UserId的现金余额
                    }


                });
            }
        }
    }

    /**
     * 更新用户实际到账出入金上期现金余额（每天 0点）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshPreInOutBalanceForAcc() {
        dealInOutBalanceForAcc (Boolean.FALSE);
    }


    /**
     * 更新用户实际到账出入金上期现金余额（4 分钟一次）
     */
    @Scheduled(cron = "0 */4 * * * ?")
    public void refreshCurrInOutBalanceForAcc() {
        dealInOutBalanceForAcc (Boolean.TRUE);
    }

    private void dealInOutBalanceForAcc(Boolean flag) {  //true 为本期 ，false 为上期
        List<CurrencyVo> currencyKinds = cashInAndOutDao.currencyKinds ();
        List<String> mt4AccList = new ArrayList ();
        for (CurrencyVo curr : currencyKinds) {
            String currency = curr.getToCurrency ();
            Object accList = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserAccList ());
            if (null != accList) {
                mt4AccList = (List) accList;
            }
            if (null != mt4AccList && mt4AccList.size () > 0) {
                mt4AccList.forEach (mt4Acc -> {
                    BigDecimal cashOutSum = cashInAndOutDao.getChannelCashOutSumByCondition (mt4Acc, currency, null, null, null);
                    BigDecimal cashInSum = cashInAndOutDao.getChannelCashInSumByCondition (mt4Acc, currency, null, null, null);
                    if (null == cashInSum) {
                        cashInSum = new BigDecimal (0.0);
                    }
                    if (null == cashOutSum) {
                        cashOutSum = new BigDecimal (0.0);
                    }
                    if (flag) {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getAccountCurrBalanceForMt4 (currency, mt4Acc), Double.valueOf (cashInSum.toString ()) - Double.valueOf (cashOutSum.toString ()));//不同币种每个mt4Acc的现金余额
                    }
                    if (!flag) {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getAccountPreBalanceForMt4 (currency, mt4Acc), Double.valueOf (cashInSum.toString ()) - Double.valueOf (cashOutSum.toString ()));//不同币种每个mt4Acc的现金余额
                    }
                });
            }
        }
    }

    /**
     * 更新出入金现金余额(5分钟一次)
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void loadHomePageData() {
        List<CurrencyVo> kinds = comUtil.getCurrencyKinds ();
        kinds.forEach (curr -> { //遍历每个币种
            String toCurrency = curr.getToCurrency ();
            homePageService.getHomePageData (toCurrency, Constants.FLAG_DAY);
            this.loadHomePageDetails (toCurrency);

        });
    }

    private void loadHomePageDetails(String curr) {
        accTypes.forEach (accType -> {
            homePageService.geAccDataByFlag (curr, Constants.FLAG_WEEK, accType); //7天的
            homePageService.geAccDataByFlag (curr, Constants.FLAG_MONTH, accType);//近一个月30天
            homePageService.geAccDataByFlag (curr, null, accType);//全部的
        });

    }

    /**
     * 落库更新应付零售账户美元余额(10分钟一次（记录每天的数据）)
     */
    @Scheduled(cron = "0 */2 * * * ?")
    private void loadRetailAccountDollarBalance() {
        String reqUrl = Constants.MT4_SERVER_URL + "user/users";
        Object mt4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
        JSONObject res;  //返回体
        String nowYearAndMonth = DateUtil.getyyyyMM ();
        String nowYear = DateUtil.getyyyy ();
        res = HttpUtil2.doGet (reqUrl, mt4Token.toString (), null);
        if ("0".equals (res.getString ("code"))) { //请求成功
            JSONArray dataArr = res.getJSONArray ("data");
            if (null != dataArr && dataArr.size () > 0) {
                Double totalBalance = 0.0;//应付零售账户美元余额：
                for (int i = 0, size = dataArr.size (); i < size; i++) {
                    JSONObject dataObject = dataArr.getJSONObject (i);
                    if (null != dataObject) {
                        Integer login = dataObject.getInteger ("login");
                        if (null != login) {
                            String loginStr = login.toString ();
                            if (loginStr.length () >= 2 && "23".equals (loginStr.substring (0, 2))) {  //获取开头是23 的所有账号
                                totalBalance += dataObject.getDouble ("balance");
                            }
                        }
                    }
                }

                redisTemplate.opsForHash ().put (RedisKeyGenerator.getRetailAccountDollarBalanceHash (nowYearAndMonth), DateUtil.getyyyyMMdd (), totalBalance);  //每天的数据

                String createTime = DateUtil.getyyyyMMdd ();
                RetailBalance balance = retailBalanceDao.getBalanceByCreateDate (createTime);
                if (null == balance) {
                    RetailBalance retailBalance = new RetailBalance ();
                    retailBalance.setRetailBalance (totalBalance);
                    retailBalance.setCreateTime (createTime);
                    retailBalanceDao.save (retailBalance);
                } else {
                    balance.setRetailBalance (totalBalance);
                    retailBalanceDao.update (balance);
                }
            }
        }
        Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getRetailAccountDollarBalanceHash (nowYear), nowYearAndMonth);
        if (null == cacheValue) {
            cacheValue = 0;
        }
        Double cacheValueDouble = Double.valueOf (cacheValue.toString ());
        Set keys = redisTemplate.opsForHash ().keys (nowYearAndMonth);
        Iterator iterator = keys.iterator ();
        while (iterator.hasNext ()) {
            Object it = iterator.next ();
            Object value = redisTemplate.opsForHash ().get (RedisKeyGenerator.getRetailAccountDollarBalanceHash (nowYearAndMonth), it);
            cacheValueDouble += Double.valueOf (value.toString ());
        }
        redisTemplate.opsForHash ().put (RedisKeyGenerator.getRetailAccountDollarBalanceHash (DateUtil.getyyyy ()), DateUtil.getyyyyMM (), cacheValueDouble); //每月的数据
    }


    /**
     * 更新总的（所有币种）兑付余额（0点跟新今天的）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    // @Scheduled(cron = "0 0/2 * * * ?")
    private void loadTotalCashedBalance() {
        calCashedBalance (true);
    }

    /**
     * 更新用户兑付余额（上午8点到下午19点 10分钟一次）
     */
    @Scheduled(cron = "0 0/10 8-19 * * ?")
    private void loadCashedBalance() {
        calCashedBalance (false);
    }

    private void calCashedBalance(Boolean flag) {
        if (log.isInfoEnabled ()) {
            log.info ("更新用户兑付余额{}", DateUtil.getyyyyMMdd ());
        }
        List<CurrencyVo> currencyKinds = comUtil.getCurrencyKinds (); //币种类型
        Long currStamp = System.currentTimeMillis () / 1000;
        for (CurrencyVo currencyVo : currencyKinds) {
            String kind = currencyVo.getToCurrency ();
            Object ids = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserIds ());
            Object mt4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
            List<String> idList = new ArrayList<> ();
            if (null != ids) {
                idList = (List) ids;  //所有用户的userId
            }
            String userIds = ComUtil.covListToString (idList);
            List<String> accountList = retailDao.SelectMt4AccsByUserId (userIds);
            String reqUrl = Constants.MT4_SERVER_URL + "user/tradesUserHistory";
            BigDecimal totalBalance = new BigDecimal (0.0);//不同币种对应的总的兑付余额
            for (String acc : accountList) {
                // String userId = comUtil.getUserIdByAcc (acc);
                BigDecimal balance = new BigDecimal (0.0);
                BigDecimal accBalance = new BigDecimal (0.0); //当天有出金并且余额在10美金一下的账户余额
                BasicNameValuePair param1 = new BasicNameValuePair ("login", acc);
                BasicNameValuePair param2 = new BasicNameValuePair ("from", "0");
                BasicNameValuePair param3 = new BasicNameValuePair ("to", currStamp.toString ());
                List<NameValuePair> nameValuePairList = new ArrayList<> ();
                nameValuePairList.add (param1);
                nameValuePairList.add (param2);
                nameValuePairList.add (param3);
                JSONObject res;  //返回体
                res = HttpUtil2.doGet (reqUrl, mt4Token.toString (), nameValuePairList);
                if ("0".equals (res.getString ("code"))) { //请求成功
                    JSONArray dataArr = res.getJSONArray ("data");
                    if (null != dataArr && dataArr.size () > 0) {
                        for (int i = 0, len = dataArr.size (); i < len; i++) {
                            JSONObject data = dataArr.getJSONObject (i);
                            String comment = data.getString ("comment"); //备注信息
                            if (calExplodeFlag (comment)) { //确认为爆仓
                                Long closeTime = data.getLong ("closeTime"); //爆仓时间
                                balance = balance.add ((BigDecimal) comUtil.getMt4BalanceByTime (kind, acc, null, DateUtil.timeStamp2Date (closeTime, null))); //对应账户余额
                            }
                        }
                    }
                }
                String currency = kind;
                List<String> reqTimes = cashInAndOutDao.getCashOutByCondition (currency, acc); //用户出金记录的时间
                for (String time : reqTimes) {
                    BigDecimal accBalanceTmp = (BigDecimal) comUtil.getMt4BalanceByTime (kind, acc, null, time);
                    if (accBalanceTmp.compareTo (new BigDecimal (10)) < 0) {

                        accBalance = accBalance.add (accBalanceTmp); //对应账户余额);
                    }
                }
                balance = balance.add (accBalance);//客户爆仓时对应到经纪商的虚拟人民币账户余额+当天有出金并且余额在10美金一下的账户余额
                BigDecimal commBalance = commissionDao.selectCommBalance (acc);
                if (null == commBalance || commBalance.intValue () == 0) { //佣金出完
                    redisTemplate.opsForValue ().set (RedisKeyGenerator.getCashedBalance (acc, kind), balance);  //保存每个用户在爆仓下不同币种账户兑付
                }
                totalBalance = totalBalance.add (balance);
            }
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getCashedBalance (null, kind), totalBalance);  //不同币种下总的兑付余额
            if (null != flag && flag) {
                CashedBalance cashedBalance = new CashedBalance ();
                cashedBalance.setCashedBalance (totalBalance.toString ());
                cashedBalance.setCurrency (kind);
                cashedBalance.setCreateTime (DateUtil.getNowDayStart ().toString ());
                cashedBalanceDao.save (cashedBalance);
            }
        }
    }

    /**
     * 计算是否爆仓
     *
     * @return
     */
    private Boolean calExplodeFlag(String comment) {
        if (!StringUtils.isEmpty (comment)) {
            if (!comment.contains ("so")) {
                return Boolean.FALSE;
            }
            String[] arr = comment.split ("/");
            if (arr.length < 3) {
                return Boolean.FALSE;
            }
            String arr1 = arr[1];
            String arr2 = arr[2];
            if (Double.valueOf (arr1) - Double.valueOf (arr2) == 0) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 更新抛单信息（每小时更新昨天的数据更新1次（成功后不更新））
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    private void loadThrowInfo() {
        Object cacheValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getGetThrowFlag (DateUtil.getyyyyMMdd ()));
        if (null == cacheValue) {
            List<ThrowInfo> infos = new LinkedList<> ();
            List<ThrowInfo> throwInfoList1 = throwOrderUtil.loadThrowInfos (Constants.LOG1);
            List<ThrowInfo> throwInfoList2 = throwOrderUtil.loadThrowInfos (Constants.LOG2);
            if (null != throwInfoList1 && throwInfoList1.size () > 0) {
                infos.addAll (throwInfoList1);
            }
            if (null != throwInfoList2 && throwInfoList2.size () > 0) {
                infos.addAll (throwInfoList2);
            }
            if (null != infos && infos.size () > 0) {
                orderInfoDao.save (infos);
            }
        }
    }


    /**
     * 加载负债收益总量数据
     */
    @Scheduled(cron = "0 */3 * * * ?")
    private void loadProfitAndLossTotalData() {
        List<CurrencyVo> currencyKinds = comUtil.getCurrencyKinds (); //币种类型
        currencyKinds.forEach (currencyVo -> {
            String currency = currencyVo.getToCurrency ();
            if (null != currency) {
                Map<Object, SingleEvalDataVo> totalMap = orderInfoService.SingleEval (null, null, currency, Constants.FLAG_DAY);
                List<SingleEvalDataVo> totalCollection = ComUtil.mapTransitionList (totalMap);
                redisTemplate.opsForHash ().put (RedisKeyGenerator.getLoadProfitAndLossTotal (), currency, totalCollection);
            }
        });

    }

    /**
     * 加载特定账号订单记录(3分钟一次)
     */
    @Scheduled(cron = "0 */3 * * * ?")
    private void loadOrderRecordByAccount() {
        Object accObj = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserAccList ());//所有账户的账号
        if (null != accObj) {
            List<String> accList = (ArrayList) accObj;
            accList.forEach (it -> {
                List<OrderDetailVo> orderDetailList = orderInfoService.getOrderRecordByAccount (it);
                orderDetailList.forEach (orderDetailVo -> {
                    Boolean isProblem = orderDetailVo.getProblemOrder ();
                    if (null != isProblem && isProblem) {
                        redisTemplate.opsForHash ().put (RedisKeyGenerator.getHaveProOrderAcc (), it, true);
                    }
                });
                redisTemplate.opsForHash ().put (RedisKeyGenerator.getLoadOrderRecordByAccount (null, null), it, orderDetailList);

            });
        }

    }

    /**
     * 加载客户订单(三分钟一次)
     */
    @Scheduled(cron = "0 */3 * * * ?")
    private void loadCustomOrder() {
        Object accObj = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserAccList ());//所有账户的账号
        Object mt4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
        if (null != accObj) {
            List<OrderDetail> orderDetailList;
            List<String> accList = (ArrayList) accObj;
            for (int i = 0, len = accList.size (); i < len; i++) {
                String account = accList.get (i);
                orderDetailList = orderInfoService.loadOrderDetails (account, mt4Token.toString ());
                orderDetailList.forEach (it -> {
                    String customerOrder = it.getCustomerOrder ();
                    orderInfoDao.deleteOrderDetail (customerOrder);
                });
                if (null != orderDetailList && orderDetailList.size () > 0) {
                    orderInfoDao.saveOrderDetail (orderDetailList);
                }
            }
            String currTimeMillis = String.valueOf (DateUtil.addOrReduceHourTime (DateUtil.addOrReduceDay (new Date (), 1), -6).getTime () / 1000);
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getLoadOrderDetail (RedisKeyGenerator.getLoadOrderDetailLastTime ()), currTimeMillis);
        }
    }

    /**
     * 加载客户订单总量数据(2分钟一次)
     */
    @Scheduled(cron = "0 0/2 * * * ?")
    private void loadOrderDetailSum() {
        Object acc = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserAccList ());//所有账户的账号
        List<String> accList = (ArrayList) acc;
        Double customerOrderProfitTotal = 0.0;
        Double clearAccProfitTotal = 0.0;
        Double feeTotal = 0.0;
        Double brokerProfitTotal = 0.0;
        Map<String, Object> detailMap = new HashMap<> ();
        List allOrderDetails = new ArrayList ();
        if (null != accList && accList.size () > 0) {
            for (int i = 0, len = accList.size (); i < len; i++) {
                String account = accList.get (i);
                if (!StringUtils.isEmpty (account)) {
                    detailMap = orderInfoDao.getOrderDetailSum (account);
                    if (null != detailMap) {
                        String customerOrderProfit = detailMap.get ("customerOrderProfit").toString ();
                        String clearAccProfit = detailMap.get ("clearAccProfit").toString ();
                        String fee = detailMap.get ("fee").toString ();
                        String brokerProfit = detailMap.get ("brokerProfit").toString ();
                        if (null != customerOrderProfit) {
                            customerOrderProfitTotal += Double.valueOf (customerOrderProfit);
                        }
                        if (null != clearAccProfit) {
                            clearAccProfitTotal += Double.valueOf (clearAccProfit);
                        }
                        if (null != fee) {
                            feeTotal += Double.valueOf (fee);
                        }
                        if (null != brokerProfit) {
                            brokerProfitTotal += Double.valueOf (brokerProfit);
                        }
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getOrderDetailAcc (account), detailMap, 5, TimeUnit.MINUTES);
                        detailMap.put ("account", account);
                        allOrderDetails.add (detailMap);
                    }
                }

            }
        }
        detailMap = new HashMap<> ();
        detailMap.put ("brokerProfitTotal", brokerProfitTotal.toString ());
        detailMap.put ("feeTotal", feeTotal.toString ());
        detailMap.put ("clearAccProfitTotal", clearAccProfitTotal.toString ());
        detailMap.put ("customerOrderProfitTotal", customerOrderProfitTotal.toString ());
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getOrderDetailAcc ("all"), detailMap);
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getAllOrderDetails (), allOrderDetails);

    }

    /**
     * 加载历史记录总计(3分钟一次)
     */
    @Scheduled(cron = "0 */4 * * * ?")
    private void loadHisBrokerData() {
        Object acc = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserAccList ());//所有账户的账号
        List<String> accList = (ArrayList) acc;
        accList.forEach (account -> {
            if (null != account) {
                List<CurrencyVo> kinds = comUtil.getCurrencyKinds ();
                kinds.forEach (curr -> { //遍历每个币种
                    String toCurrency = curr.getToCurrency ();
                    BusinessAccountInfo info = businessAccountService.getBusinessAccInfoHis (toCurrency, account);
                    if (null != info) {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getBrokenAccHis (toCurrency, account), info);
                    }
                });
            }
        });
        List<CurrencyVo> kinds = comUtil.getCurrencyKinds ();
        kinds.forEach (curr -> { //遍历每个币种
            String toCurrency = curr.getToCurrency ();
            BusinessAccountInfo info = businessAccountService.getBusinessAccInfoHis (toCurrency, null);
            if (null != info) {
                redisTemplate.opsForValue ().set (RedisKeyGenerator.getBrokenAccHis (toCurrency, null), info);
            }
        });


    }

    /**
     * 加载历史记录总计(3分钟一次)
     */
    @Scheduled(cron = "0 */3 * * * ?")
    private void loadHisBrokerDataForUserId() {
        Object ids = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserIds ());//所有账户的账号
        List<String> idList = (List) ids;
        idList.forEach (id -> {
            if (null != id) {
                List<CurrencyVo> kinds = comUtil.getCurrencyKinds ();
                kinds.forEach (curr -> { //遍历每个币种
                    String toCurrency = curr.getToCurrency ();
                    BusinessAccountInfo info = businessAccountService.getBusinessAccInfoHisForUserId (toCurrency, id);
                    if (null != info) {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getBrokenUserIdHis (toCurrency, id), info);
                    }
                });
            }
        });
    }


    /**
     * 加载历史记录总计(3分钟一次)
     */
    @Scheduled(cron = "0 */3 * * * ?")
    private void loadHisData() {
        Object acc = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAgentUserAccList ());//所有账户的账号
        List<String> accList = (ArrayList) acc;
        accList.forEach (account -> {
            if (null != account) {
                retailService.getHisRetailInfo (account);
                orderInfoService.getThrowHisSum (account);
                List<CurrencyVo> kinds = comUtil.getCurrencyKinds ();
                kinds.forEach (curr -> { //遍历每个币种
                    String toCurrency = curr.getToCurrency ();
                    BusinessAccountInfo info = businessAccountService.getBusinessAccInfoHis (toCurrency, account);
                    if (null != info) {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getBrokenAccHis (toCurrency, account), info);
                    }
                });
            }
        });
        retailService.getHisRetailInfo (null); //总的
        orderInfoService.getThrowHisSum (null);//总的
        bondInfoService.getHisClearAccountInfo ();
        bondInfoService.getHisBondInfo ();

    }

    /**
     * 更新保证金（terminus）信息()
     */
    @Scheduled(cron = "0 */20 * * * ?")
    private void loadRemoteBondInfo() {
        Object cacheValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getLoadRemoteBondInfoFlag ());
        if (null != cacheValue) {
            bondInfoService.loadRemoteBondInfo (DateUtil.parserTo (DateUtil.getyyyyMMdd ()));
        } else {
            List list = DateUtil.getTimeList (2018, 7, DateUtil.getNowYear (), DateUtil.getNowMonth (), 1);
            list.forEach (it -> {
                List arr = (List) it;
                arr.forEach (time -> {
                    bondInfoService.loadRemoteBondInfo (DateUtil.parserTo ((Date) time));
                });
            });
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getLoadRemoteBondInfoFlag (), "loadRemoteBondInfo");
        }

    }

    @Scheduled(cron = "0 */5 * * * ?") //5分钟更新一次
    private void updateBrokerAndBondBalance() {
        List list = DateUtil.getTimeList (2018, 7, DateUtil.getNowYear (), DateUtil.getNowMonth (), 1);
        list.forEach (it -> {
            List arr = (List) it;
            arr.forEach (time -> {
                String dataDay = DateUtil.parserTo ((Date) time);
                List<CurrencyVo> kinds = comUtil.getCurrencyKinds ();
                kinds.forEach (currencyVo -> {
                    String currency = currencyVo.getToCurrency ();
                    SingleEvalDataVo orderInfo = orderInfoService.SingleEvalBalance ("2018-07-01", dataDay, currency);
                    redisTemplate.opsForHash ().put (RedisKeyGenerator.getOrderEvaBalance (Constants.FLAG_DAY, currency), dataDay, orderInfo);

                });
            });
        });
        List list2 = DateUtil.getTimeList (2018, 7, DateUtil.getNowYear (), DateUtil.getNowMonth (), 30);
        list2.forEach (it -> {
            List arr = (List) it;
            arr.forEach (time -> {
                String dataMonth = DateUtil.parserTo01 ((Date) time);
                String endDateMonth = DateUtil.parserTo (DateUtil.lastMonthDay (DateUtil.praiseString2Date (dataMonth)));

                List<CurrencyVo> kinds = comUtil.getCurrencyKinds ();
                kinds.forEach (currencyVo -> {
                    String currency = currencyVo.getToCurrency ();
                    Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getOrderEvaBalance (Constants.FLAG_MONTH, currency), endDateMonth);
                    if (null == cacheValue) {
                        SingleEvalDataVo orderInfo = orderInfoService.SingleEvalBalance ("2018-07-01", endDateMonth, currency);
                        redisTemplate.opsForHash ().put (RedisKeyGenerator.getOrderEvaBalance (Constants.FLAG_MONTH, currency), endDateMonth, orderInfo);
                    }
                });

            });
        });
    }
}
