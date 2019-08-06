package com.ry.cbms.decision.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.dao.AccessGoldCheckDao;
import com.ry.cbms.decision.server.dao.Mt4Dao;
import com.ry.cbms.decision.server.dao.OrderEvaluationInfoDao;
import com.ry.cbms.decision.server.dao.RiskManageDao;
import com.ry.cbms.decision.server.model.Mail;
import com.ry.cbms.decision.server.model.OrderEvaluationInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.MailService;
import com.ry.cbms.decision.server.service.RiskManageService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.utils.HttpUtil2;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author maoYang
 * @Date 2019/6/4 17:46
 * @Description 风控管理
 */
@Service
@Slf4j
public class RiskManageServiceImpl implements RiskManageService {
    @Autowired
    private RiskManageDao riskManageDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AccessGoldCheckDao accessGoldCheckDao;

    @Autowired
    private OrderEvaluationInfoDao orderEvaluationInfoDao;

    @Autowired
    private ComUtil comUtil;

    @Autowired
    private Mt4Dao mt4Dao;

    @Autowired
    private MailService mailService;

    @Override
    public List getByVariety(String account, String variety) {
        List<OrderEvaluationInfo> orderEvaluationInfoList = orderEvaluationInfoDao.getOrderByCondition (variety, account);
        handOrderEval (orderEvaluationInfoList);
        return orderEvaluationInfoList;
    }

    /**
     * 选定品种查询历史数据
     *
     * @param variety   品种
     * @param orderType 订单类别
     * @param flag      展示时间颗粒度标签
     * @param account   mt4账户号
     * @return
     */
    @Override
    public List<OrderEvaluationInfo> getVarietyData(String variety, String orderType, String flag, String account, String beginDate, String overDate) {
        List<OrderEvaluationInfo> orderEvaluationInfoList;
        String startDate;
        if (StringUtils.isEmpty (beginDate)) {
            startDate = DateUtil.parser (DateUtil.getDayBegin ());
        } else {
            startDate = beginDate;
        }
        String endDate;
        if (StringUtils.isEmpty (overDate)) {
            endDate = DateUtil.parser (DateUtil.getDayEnd ());
        } else {
            endDate = overDate;
        }
        switch (flag) {
            case "day":
                orderEvaluationInfoList = orderEvaluationInfoDao.getOrderDataByDay (variety, orderType, account, startDate, endDate);
                break;
            case "week":
                orderEvaluationInfoList = orderEvaluationInfoDao.getOrderDataByHour (variety, orderType, account, startDate, startDate);
                break;

            case "hour":
                orderEvaluationInfoList = orderEvaluationInfoDao.getOrderDataByHour (variety, orderType, account, startDate, endDate);
                break;
            case "all":
                orderEvaluationInfoList = orderEvaluationInfoDao.getOrderDataByWeek (variety, orderType, account, startDate, endDate);
                break;
            default:
                throw new GlobalException ("请传入有效参数");

        }
        handOrderEval (orderEvaluationInfoList);
        return orderEvaluationInfoList;
    }

    private void handOrderEval(List<OrderEvaluationInfo> orderEvaluationInfoList) {
        for (int i = 0, len = orderEvaluationInfoList.size (); i < len; i++) {
            OrderEvaluationInfo info = orderEvaluationInfoList.get (i);
            String emptyHands = info.getEmptyHands ();
            String multipleHands = info.getMultipleHands ();
            if (null == emptyHands) {
                emptyHands = "0";
            }
            if (null == multipleHands) {
                multipleHands = "0";
            }
            Double multi = Double.valueOf (multipleHands);
            Double empty = Double.valueOf (emptyHands);
            Double tradeTotalHands = multi + empty;
            Double net = multi - empty;
            info.setTradeTotalHands (String.format ("%.2f", tradeTotalHands));
            info.setMultipleHands (String.format ("%.2f", multi));
            info.setEmptyHands (String.format ("%.2f", empty));
            info.setNetPosition (String.format ("%.2f", net));
            info.setId (i);
        }
    }

    /**
     * 订单评估
     *
     * @param account
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public JSONObject orderEva(String account, String userId, Integer offset, Integer limit) {
        List<OrderEvaluationInfo> orderEvaluationInfoList = new ArrayList<> ();
        Integer totalCount = 0;
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {  //全部账户
            orderEvaluationInfoList = orderEvaluationInfoDao.getAllOrderEvaluationInfo (offset, limit, null);
            totalCount = orderEvaluationInfoDao.getAllOrderEvaluationCount (null);

        } else if (!StringUtils.isEmpty (account)) {  //指定账户
            orderEvaluationInfoList = orderEvaluationInfoDao.getAllOrderEvaluationInfo (offset, limit, account);
            totalCount = orderEvaluationInfoDao.getAllOrderEvaluationCount (account);

        } else if (!StringUtils.isEmpty (userId)) {//指定用户或代理下面所属全部账户
            account = comUtil.getMt4AccountsByUserId (userId);
            if (null != account) {
                totalCount = orderEvaluationInfoDao.getAllOrderEvaluationCount (account);
                orderEvaluationInfoList = orderEvaluationInfoDao.getAllOrderEvaluationInfo (offset, limit, account);
            }

        }
        JSONObject result = new JSONObject ();
        result.put ("totalCount", totalCount);
        calNetPosition (orderEvaluationInfoList);
        dealAccuracy (orderEvaluationInfoList);
        result.put ("orderEvaluationInfoList", orderEvaluationInfoList);
        return result;
    }

    private void dealAccuracy(List<OrderEvaluationInfo> orderEvaluationInfoList) {
        orderEvaluationInfoList.forEach (it -> {
            String multipleHands = it.getMultipleHands ();
            String emptyHands = it.getEmptyHands ();
            String netPosition = it.getNetPosition ();
            it.setEmptyHands (String.format ("%.2f", Double.valueOf (emptyHands)));
            it.setNetPosition (String.format ("%.2f", Double.valueOf (netPosition)));
            it.setMultipleHands (String.format ("%.2f", Double.valueOf (multipleHands)));
        });
    }

    /**
     * 计算净头寸
     */
    private void calNetPosition(List<OrderEvaluationInfo> orderEvaluationInfoList) {
        for (OrderEvaluationInfo info : orderEvaluationInfoList) {
            String emptyHands = info.getEmptyHands ();
            String multiplehands = info.getMultipleHands ();
            if (null == emptyHands) {
                emptyHands = "0";
            }
            if (null == multiplehands) {
                multiplehands = "0";
            }
            Double tradCount = Double.valueOf (emptyHands) + Double.valueOf (multiplehands);//交易总手数
            Double netPosition = Double.valueOf (multiplehands) - Double.valueOf (emptyHands);
            info.setTradeTotalHands (String.format ("%.2f", tradCount));
            //info.setTradeNum (tradCount / 100); //交易单数
            info.setNetPosition (netPosition.toString ());
        }
    }

    /**
     * 计算风酬比和订单平均交易量
     *
     * @param mt4Account
     */
    private Map<String, Object> calRiskRatioAndAvg(String mt4Account) {
        Map<String, Object> retMap = Maps.newHashMap ();
        String ratio = null;
        Double volume = 0.0;//交易量
        int countPro = 0;
        int countLoss = 0;
        String mt4Acc = mt4Account;
        List<Map> closeRecords = accessGoldCheckDao.getDetailCloseRecordProfitByAcc (mt4Acc);
        if (null == closeRecords || closeRecords.size () == 0) {
            ratio = "0";
            volume = 0.0;
            retMap.put ("ratio", ratio);
            retMap.put ("volume", volume);
            return retMap;
        }
        BigDecimal totalProfit = new BigDecimal (0.0);//盈利的
        BigDecimal totalLoss = new BigDecimal (0.0); //亏的
        for (Map map : closeRecords) {
            Object profit = map.get ("profit");
            if (null != profit && Double.valueOf (profit.toString ()) > 0) {
                totalProfit = totalProfit.add (new BigDecimal (profit.toString ()));
                countPro++;
            }
            if (null != profit && Double.valueOf (profit.toString ()) < 0) {
                totalLoss = totalLoss.add (new BigDecimal (profit.toString ()));
                countLoss++;
            }
            if (null != map.get ("volume ")) {
                volume += (Double) map.get ("volume");
            }
        }
        int len = closeRecords.size ();
        BigDecimal ratioPro = new BigDecimal (0.0);
        BigDecimal ratioLoss = new BigDecimal (0.0);
        if (totalProfit.intValue () > 0) {
            ratioPro = totalProfit.divide (new BigDecimal (countPro), 2, BigDecimal.ROUND_HALF_UP);
        }
        if (totalLoss.intValue () < 0) {
            ratioLoss = totalLoss.divide (new BigDecimal (countLoss), 2, BigDecimal.ROUND_HALF_UP);
        }
        try {
            ratio = ratioPro.divide (ratioLoss, 2, BigDecimal.ROUND_DOWN).toString ();
        } catch (Exception e) {
            ratio = "0";
        }
        volume = volume / len;
        retMap.put ("ratio", ratio);
        retMap.put ("volume", volume);
        return retMap;
    }

    /**
     * 账户评估
     *
     * @param offset
     * @param limit
     * @param flag
     * @return
     */
    @Override
    public Map<String, Object> accountEva(Integer offset, Integer limit, String flag) {
        Map retMap;
        List<JSONObject> accInfos = riskManageDao.getAccInfo (offset, limit);
        List removeList = new ArrayList<> ();
        Integer count = riskManageDao.getAccInfoCount ();
        StringBuffer mt4AccSb = new StringBuffer ();
        for (int i = 0, len = accInfos.size (); i < len; i++) {
            Object mt4Acc = accInfos.get (i).get ("mtAcct");
            if (null == mt4Acc) {
                removeList.add (accInfos.get (i));
                continue;
            }
            if (null != mt4Acc) {
                mt4AccSb.append (mt4Acc.toString ());
            }
            if (i < len - 1) {
                mt4AccSb.append (",");//获取本页查询的账号
            }
        }
        accInfos.removeAll (removeList);
        String reqUrl = Constants.MT4_SERVER_URL + "user/userRecordsRequest";
        Object mt4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
        List<NameValuePair> nameValuePairList = new ArrayList ();
        BasicNameValuePair param1 = new BasicNameValuePair ("logins", mt4AccSb.toString ());
        nameValuePairList.add (param1);
        JSONObject response;
        try {
            response = HttpUtil2.doGet (reqUrl, mt4Token.toString (), nameValuePairList);
        } catch (Exception e) {
            throw new GlobalException ("mt4 服务宕机");
        }
        nameValuePairList.clear ();
        if (null != response && "0".equals (response.getString ("code"))) {
            JSONArray dataArr = response.getJSONArray ("data");
            List<JSONObject> dataList = new ArrayList (dataArr);
            Map levelMap = dealLevel (dataList);//发邮件

            Map<String, String> dataMap = dealDataList (dataList);
            if (null != dataList && dataList.size () > 0) {
                Double fc = 0.0;//方差
                for (int i = 0, len = accInfos.size (); i < len; i++) {
                    Map accInfo = accInfos.get (i);
                    if (null == accInfo.get ("mtAcct")) {
                        continue;
                    }
                    accInfo.put ("corpse", false);//默认非僵尸用户
                    String mt4Account = accInfo.get ("mtAcct").toString ();//获取mt4 账号
                    accInfo.put ("isLevelPro", levelMap.get (mt4Account));
                    String lever = mt4Dao.getLeverByMt4Acc (mt4Account);
                    accInfo.put ("leverage", lever);
                    try {
                        String balance = dataMap.get (mt4Account);
                        if (log.isInfoEnabled ()) {
                            log.info ("balance{},i{},mt4Account {}", balance, i, mt4Account);
                        }
                        accInfo.put ("balance", balance); //获取账户余额
                    } catch (Exception e) {
                        accInfo.put ("balance", 0);
                        accInfo.put ("ratio", 0);//用户的风酬比
                        accInfo.put ("fc", 0);
                        accInfo.put ("isRed", false);
                        continue;
                    }
                    Map<String, Object> avgMap = this.calRiskRatioAndAvg (mt4Account);// 计算风酬比和订单平均交易量
                    String ratio = avgMap.get ("ratio").toString ();
                    Double avgVolume = (Double) avgMap.get ("volume");//平均交易单量
                    List<Map> closeRecords = accessGoldCheckDao.getDetailCloseRecordProfitByAcc (mt4Account);//查询平仓收益记录
                    for (Map map : closeRecords) {
                        String vv = ((BigDecimal) map.get ("volume")).subtract (new BigDecimal (avgVolume)).toString ();
                        fc += Math.pow (Double.valueOf (vv), 2);//计算方差
                    }
                    String reqUrl2 = Constants.MT4_SERVER_URL + "openNoticeTrader";
                    BasicNameValuePair pair = new BasicNameValuePair ("login", mt4Account);
                    BasicNameValuePair pair2 = new BasicNameValuePair ("serverId", Constants.Mt4ServerId);
                    nameValuePairList.add (pair);
                    nameValuePairList.add (pair2);
                    JSONObject req = HttpUtil2.doGet (reqUrl2, mt4Token.toString (), nameValuePairList);
                    if (null != req && "-12".equals (req.getString ("code"))) {  //用户持仓为空
                        String lastCloseTime = accessGoldCheckDao.getLatestCloseTime (mt4Account);//获取用户最新平仓时间
                        Date openTime = mt4Dao.getByMt4Acc (mt4Account);//用户开户时间
                        if (null != lastCloseTime) {
                            accInfo.put ("lastCloseTime",(DateUtil.praiseStringToDate (lastCloseTime)));
                            if (DateUtil.addOrReduceDay (DateUtil.praiseStringToDate (lastCloseTime), 42).before (new Date ())) {
                                accInfo.put ("corpse", true);//僵尸用户
                            }
                        } else {
                            if (null != openTime) {
                                if (DateUtil.addOrReduceDay (openTime, 42).before (new Date ())) {  //开户距离现在时间段没交易也是僵尸
                                    accInfo.put ("corpse", true);//僵尸用户
                                }
                            }
                        }
                    }
                    String ratioNum=null;
                    ratio=String.valueOf (Math.abs (Double.valueOf (ratio)));
                    if(ratio.contains (".") && ratio.length ()>=5){
                        String[] arr=ratio.split (".");
                         ratioNum = arr[0]+arr[1].substring (0,2);
                    }else{
                        ratioNum=ratio;
                    }
                    accInfo.put ("ratio", Math.abs (Double.valueOf (ratioNum)));//用户的风酬比
                    fc = Math.sqrt (fc);
                    accInfo.put ("fc", fc);//方差
                    accInfo.put ("isRed", calRatioColor (ratio, fc, mt4Account));
                }

            } else {
                for (int i = 0, len = accInfos.size (); i < len; i++) { //没有设为0
                    Map accInfo = accInfos.get (i);
                    accInfo.put ("balance", 0);
                    accInfo.put ("ratio", 0);//用户的风酬比
                    accInfo.put ("fc", 0);
                    accInfo.put ("isRed", false);
                    accInfo.put ("corpse", false);
                }
            }
        }
        this.orderAccInfos (accInfos, flag);
        retMap = this.clearEmpTyMt4 (accInfos, count);
        return retMap;
    }

    /**
     * 计算异常杠杆
     */
    private Map dealLevel(List<JSONObject> dataArr) {
        Map levelMap = new HashMap ();
        dataArr.forEach (it -> {
            Double balance = it.getDouble ("balance");//余额
            Integer leverage = it.getInteger ("leverage");//杠杆
            Boolean isPro = false;
            if (null == balance) {
                balance = 0.0;
            }
            String mt4Acct = it.getString ("login"); //账号
            Map retMap = riskManageDao.getAccInfoByMt4 (mt4Acct);
            if (null != retMap) {
                Object accType = retMap.get ("accType");
                if (null != accType) {
                    if ("1".equals (accType.toString ())) { //STP
                        if (balance > 0 && balance < 19999) {
                            if (leverage > 400) {
                                isPro = true;
                            }
                        }

                        if (balance > 20000 && balance < 49999) {
                            if (leverage > 300) {
                                isPro = true;
                            }
                        }

                        if (balance > 50000 && balance < 99999) {
                            if (leverage > 100) {
                                isPro = true;
                            }
                        }
                    }
                    if ("2".equals (accType.toString ())) {//ECN
                        if (balance > 0 && balance < 49999) {
                            if (leverage > 300) {
                                isPro = true;
                            }
                        }

                        if (balance > 50000 && balance < 99999) {
                            if (leverage > 200) {
                                isPro = true;
                            }
                        }

                        if (balance >= 100000) {
                            if (leverage > 100) {
                                isPro = true;
                            }
                        }
                    }
                }
            }
            if (isPro) {
//                Mail mail = new Mail ();
//                mail.setToUsers ("936524643@qq.com");
//                mail.setContent ("hellow");
//                mail.setSubject ("异常杠杆");
//                List toUser = new ArrayList ();
//                toUser.add ("936524643@qq.com");
//                mail.setUserId (39L);
//                mailService.save (mail, toUser);
            }
            levelMap.put (mt4Acct, isPro);
        });
        return levelMap;
    }

    private Map<String, String> dealDataList(List<JSONObject> dataArr) {
        Map retMap = new HashMap ();
        dataArr.forEach (data -> {
            retMap.put (data.getString ("login"), data.getString ("balance"));
        });
        return retMap;
    }

    /**
     * 清除mt4 账号为空的数据
     */
    private Map clearEmpTyMt4(List<JSONObject> accInfos, Integer count) {
        Map retMap = new HashMap ();
        List<JSONObject> rmList = new ArrayList<> ();
        accInfos.forEach (it -> {
            String mt4Acc = it.getString ("mtAcct");
            if (StringUtils.isEmpty (mt4Acc)) {
                rmList.add (it);
            }
        });
        int size = 0;
        if (null != rmList && rmList.size () > 0) {
            accInfos.removeAll (rmList);
            size = rmList.size ();

        }
        count = count - size;
        retMap.put ("accInfos", accInfos);
        retMap.put ("count", count);
        return retMap;
    }

    /**
     * 计算风酬比是否变红色
     *
     * @param ratio 风酬比
     * @param fc    方差
     */
    private Boolean calRatioColor(String ratio, Double fc, String mt4Account) {
        if (null == ratio) {
            ratio = "0.0";
        }
        Double ratioNum = Math.abs (Double.valueOf (ratio));
        Object cacheObject = redisTemplate.opsForValue ().get (RedisKeyGenerator.getForTwentyTrade (mt4Account));
        if (ratioNum <= 1.5 && Math.abs (fc) <= 1.5) {
            if (null != cacheObject) {
                return true;
            }
        }
        return false;
    }

    private void orderAccInfos(List<JSONObject> accInfos, String flag) {
        switch (flag) { //默认排序
            case "0":
                accInfos.sort ((a, b) -> {
                    if (b.getBigDecimal ("ratio").compareTo (a.getBigDecimal ("ratio")) < 0) {
                        return -1;
                    } else if (b.getBigDecimal ("ratio").compareTo (a.getBigDecimal ("ratio")) == 0) {
                        return 0;
                    } else {
                        return 1;
                    }

                });
                break;
            case "1":
                accInfos.sort ((a, b) -> {
                    try {
                        if (true == b.getBoolean ("isRed") && false == a.getBoolean ("isRed")) {
                            return 1;
                        } else if (true == b.getBoolean ("isRed") && true == a.getBoolean ("isRed")) {
                            return 0;
                        } else {
                            return -1;
                        }
                    } catch (Exception e) {
                        return -1;
                    }
                });
                break;
            case "2":
                accInfos.sort ((a, b) -> {
                    try {
                        if (true == b.getBoolean ("corpse") && false == a.getBoolean ("corpse")) {
                            return 1;
                        } else if (true == b.getBoolean ("corpse") && true == a.getBoolean ("corpse")) {
                            return 0;
                        } else {
                            return -1;
                        }
                    } catch (Exception e) {
                        return -1;
                    }
                });
                break;
            case "3":   //异常杠杆 排序(优先查看)
                accInfos.sort ((a, b) -> {
                    try {
                        if (true == b.getBoolean ("isLevelPro") && false == a.getBoolean ("isLevelPro")) {
                            return 1;
                        } else if (true == b.getBoolean ("isLevelPro") && true == a.getBoolean ("isLevelPro")) {
                            return 0;
                        } else {
                            return -1;
                        }
                    } catch (Exception e) {
                        return -1;
                    }
                });
                break;
            default:
                throw new GlobalException ("请传入有效参数");
        }
    }

}
