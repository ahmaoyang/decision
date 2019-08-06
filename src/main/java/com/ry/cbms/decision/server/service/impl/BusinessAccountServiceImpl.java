package com.ry.cbms.decision.server.service.impl;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.dao.AgentUserDao;
import com.ry.cbms.decision.server.dao.CashInAndOutDao;
import com.ry.cbms.decision.server.model.BusinessAccountInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.BusinessAccountService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.vo.CurrencyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author maoYang
 * @Date 2019/6/12 17:01
 * @Description 经济商账户相关
 */
@Service
@Slf4j
public class BusinessAccountServiceImpl implements BusinessAccountService {

    @Autowired
    private CashInAndOutDao cashInAndOutDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AgentUserDao agentUserDao;

    @Autowired
    private ComUtil comUtil;

    /**
     * 查询经纪商账户余额记录
     *
     * @param flag      颗粒度
     * @param currency  币种
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    @Override
    public List<Map<String, Object>> getBrokerAccountBalance(String flag, String currency, String startDate, String endDate) {
        Date currDate = DateUtil.getDayEnd ();//今天的日期时间
        List<Map<String, Object>> retList = new ArrayList<> ();
        if (StringUtils.isEmpty (currency)) {
            currency = Constants.CURRENCY_RMB; //默认人民币账户
        }
        if (Constants.FLAG_WEEK.equals (flag)) {
            startDate = DateUtil.parser (DateUtil.addOrReduceDay (currDate, -7));
            endDate = DateUtil.parser (currDate);

        }
        if (Constants.FLAG_MONTH.equals (flag)) { //近一月
            startDate = DateUtil.parser (DateUtil.addOrReduceDay (currDate, -30));
            endDate = DateUtil.parser (currDate);
        }
        Map<String, BusinessAccountInfo> infoMap = this.getAccData (null, Constants.FLAG_DAY, startDate, endDate, currency, null, Boolean.TRUE);//key 是日期 value 是数据  按天查询
        for (Map.Entry<String, BusinessAccountInfo> entry : infoMap.entrySet ()) {
            Map<String, Object> dataMap = new HashMap<> ();
            BusinessAccountInfo info = entry.getValue ();
            String accountActualAmount = info.getToAccountActualAmount ();
            String cashOutActualAmount = info.getUserCashOutActualAmount ();
            if (null == accountActualAmount) {
                accountActualAmount = "0";
            }
            if (null == cashOutActualAmount) {
                cashOutActualAmount = "0";
            }
            BigDecimal cashIn = new BigDecimal (accountActualAmount);
            BigDecimal cashOut = new BigDecimal (cashOutActualAmount);
            dataMap.put ("reqTime", entry.getKey ());
            dataMap.put ("balance", cashIn.subtract (cashOut));
            retList.add (dataMap);
        }
        return retList;
    }

    @Override
    public List getBrokerAccountInfos(String account, String userId, String flag, String startDate, String endDate, String currency) {
        List retList = new ArrayList ();//返回数据集合
        Map<String, Object> dataMap = new ConcurrentHashMap<> ();//数据集合
        List<CurrencyVo> currencyKinds = comUtil.getCurrencyKinds ();//全部币种
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) { //全部账户
            currencyKinds.forEach (it -> {
                String toCurrency = it.getToCurrency ();
                String message = it.getMessage ();
                String symbol = it.getCurrencySymbol ();
                Object preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalance (toCurrency));
                if (null == preBalance) {
                    preBalance = 0;
                }
                dataMap.put ("preCashBalance", preBalance);//上期现金余额
                Map<String, Object> accountData = this.dealAccountData (it.getToCurrency (), account);
                if (null != accountData) {
                    dataMap.putAll (accountData);
                }
                dataMap.put ("currency", toCurrency);
                dataMap.put ("message", message);
                dataMap.put ("symbol", symbol);
                retList.add (dataMap);
            });
        } else if (!StringUtils.isEmpty (account)) { //指定账户
            currencyKinds.forEach (it -> {
                String toCurrency = it.getToCurrency ();
                String message = it.getMessage ();
                String symbol = it.getCurrencySymbol ();
                Object preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalanceForMt4 (toCurrency, account));
                if (null == preBalance) {
                    preBalance = 0;
                }
                dataMap.put ("preCashBalance", preBalance);//上期现金余额
                Map<String, Object> accountData = this.dealAccountData (toCurrency, account);
                dataMap.putAll (accountData);
                dataMap.put ("currency", toCurrency);
                dataMap.put ("symbol", symbol);
                dataMap.put ("message", message);
                retList.add (dataMap);
            });
        } else if (!StringUtils.isEmpty (userId)) {  //指定用户或代理下面所属全部账户
            currencyKinds.forEach (it -> {
                String toCurrency = it.getToCurrency ();
                String message = it.getMessage ();
                String symbol = it.getCurrencySymbol ();
                BigDecimal sumPre = new BigDecimal (0.0);
               // Set<String> userIds = comUtil.getUserIdSets (userId);
                String accounts = comUtil.getMt4AccountsByUserId (userId);
               String [] accountCollection= accounts.split (",");
                for (String mt4Acc: accountCollection) {
                    Object cacheValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalanceForMt4 (toCurrency, mt4Acc));
                    if (null != cacheValue) {
                        sumPre.add ((BigDecimal) cacheValue);
                    }
                }
                dataMap.put ("preCashBalance", sumPre);//上期现金余额
                Map<String, Object> accountData = this.dealAccountData (toCurrency, accounts);
                if (null != accountData) {
                    dataMap.putAll (accountData);
                }
                dataMap.put ("currency", toCurrency);
                dataMap.put ("symbol", symbol);
                dataMap.put ("message", message);
                retList.add (dataMap);
            });

        }
        return retList;
    }

    @Override
    public Map<String, BusinessAccountInfo> getAccDataByCondition(String account, String flag, String startDate, String endDate, String currency, String userId) {
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) { //全部账户
            userId = ComUtil.covListToString (comUtil.getAllUserIds ());
        } else if (!StringUtils.isEmpty (userId)) {  //指定用户或代理下面所属全部账户
            userId = comUtil.getUserIds (userId);
        }
        if (!StringUtils.isEmpty (endDate)) {
            if (Constants.FLAG_DAY.equals (flag)) {
                endDate = DateUtil.parserTo (DateUtil.addOrReduceDay (DateUtil.parse (endDate), 1));
            }
            if (Constants.FLAG_MONTH.equals (flag)) {
                endDate = DateUtil.parserTo (DateUtil.addOrReduceDay (DateUtil.lastMonthDay (DateUtil.stringParserToDate (endDate)), 1));
            }
        }
        Map infoMap = this.getAccData (account, flag, startDate, endDate, currency, userId, null);
        return infoMap;
    }


    /**
     * 经纪商账户历史记录
     *
     * @param currency
     * @return
     */
    @Override
    public BusinessAccountInfo getBusinessAccInfoHis(String currency, String mt4Acc) {
        return getSum (currency, null, mt4Acc);
    }

    @Override
    public BusinessAccountInfo getBusinessAccInfoHisForUserId(String currency, String userId) {
        return getSum (currency, userId, null);
    }

    private BusinessAccountInfo getSum(String currency, String userId, String mt4Acc) {
        Map<String, BusinessAccountInfo> infoMap = this.getAccData (mt4Acc, Constants.FLAG_ALL, null, null, currency, userId, null);
        Double userApplyInAmount = 0.0;//用户申请入金金额

        Double preCashBalance = 0.0;//上期现金余额

        Double userApplyOutAmount = 0.0;//用户申请本金出金金额

        Double outActualCashAmount = 0.0;//本金出金实际支出金额
        Double userCommOutActualToAccountAmount = 0.0;//用户佣金出金实际到账金额

        Double commOutChannelFee = 0.0;//佣金出金通道手续费

        Double commOutActualFee = 0.0;//佣金出金通道实际手续费

        Double commOutBrokerFee = 0.0;//佣金出金经纪商收取手续费
        Double userPayActual = 0.0;//用户实际支付

        Double toAccountActualAmount = 0.0; //入金实际到账金额

        Double cashOutChannelFee = 0.0;      //本金出金通道手续费

        Double cashOutBrokerFee = 0.0;      //本金出金经纪商收取手续费

        Double userApplyCommOutAmount = 0.0; //用户申请佣金出金金额

        Double commOutActualAmount = 0.0;                        //佣金出金实际支出金额

        Double channelInCashFee = 0.0;//入金通道手续费

        Double channelInCashActualFee = 0.0;//入金通道实际手续费

        Double inCashBrokerFee = 0.0;//入金经济商手续费

        Double totalChannelFee = 0.0;//总通道手续费
        Double totalBrokerFee = 0.0;//总经济商手续费

        Double brokerActualNetIn = 0.0;//经济商实际净收入
        Double userCashOutActualAmount = 0.0;//用户本金出金实际到账金额
        Double currentCashBalance = 0.0;//本期现金余额
        BusinessAccountInfo businessAccountInfo = new BusinessAccountInfo ();
        if (null != infoMap) {
            for (String key : infoMap.keySet ()) {
                BusinessAccountInfo info = infoMap.get (key);
                userApplyInAmount += ComUtil.DoubleValueOf (null == info.getUserApplyInAmount () ? "0" : info.getUserApplyInAmount ());
                userPayActual += ComUtil.DoubleValueOf (null == info.getUserPayActual () ? "0" : info.getUserPayActual ());
                toAccountActualAmount += ComUtil.DoubleValueOf (info.getToAccountActualAmount ());
                channelInCashFee += ComUtil.DoubleValueOf (info.getChannelInCashFee ());
                channelInCashActualFee += ComUtil.DoubleValueOf (info.getChannelInCashActualFee ());
                inCashBrokerFee += ComUtil.DoubleValueOf (info.getInCashBrokerFee ());
                userApplyOutAmount += ComUtil.DoubleValueOf (info.getUserApplyOutAmount ());
                outActualCashAmount += ComUtil.DoubleValueOf (info.getOutActualCashAmount ());
                userCashOutActualAmount += ComUtil.DoubleValueOf (info.getUserCashOutActualAmount ());
                cashOutChannelFee += ComUtil.DoubleValueOf (info.getCashOutChannelFee ());

                cashOutBrokerFee += ComUtil.DoubleValueOf (info.getCashOutBrokerFee ());      //本金出金经纪商收取手续费

                userApplyCommOutAmount += ComUtil.DoubleValueOf (info.getUserApplyCommOutAmount ()); //用户申请佣金出金金额

                commOutActualAmount += ComUtil.DoubleValueOf (info.getCommOutActualAmount ());                        //佣金出金实际支出金额

                userCommOutActualToAccountAmount += ComUtil.DoubleValueOf (info.getUserCommOutActualToAccountAmount ());//用户佣金出金实际到账金额

                commOutChannelFee += ComUtil.DoubleValueOf (info.getCommOutChannelFee ());//佣金出金通道手续费

                commOutActualFee += ComUtil.DoubleValueOf (info.getCommOutActualFee ());//佣金出金通道实际手续费

                commOutBrokerFee += ComUtil.DoubleValueOf (info.getCommOutBrokerFee ());//佣金出金经纪商收取手续费

                totalChannelFee += ComUtil.DoubleValueOf (info.getTotalChannelFee ());//总通道手续费

                totalBrokerFee += ComUtil.DoubleValueOf (info.getTotalBrokerFee ());//总经济商手续费

                brokerActualNetIn += ComUtil.DoubleValueOf (info.getBrokerActualNetIn ());//经济商实际净收入

                preCashBalance += ComUtil.DoubleValueOf (info.getPreCashBalance ());//上期现金余额

                currentCashBalance += ComUtil.DoubleValueOf (info.getCurrentCashBalance ());//本期现金余额
            }
            businessAccountInfo.setUserApplyInAmount (userApplyInAmount.toString ());

            businessAccountInfo.setUserPayActual (userPayActual.toString ());

            businessAccountInfo.setToAccountActualAmount (toAccountActualAmount.toString ());

            businessAccountInfo.setInCashBrokerFee (inCashBrokerFee.toString ());

            businessAccountInfo.setUserApplyOutAmount (userApplyOutAmount.toString ());

            businessAccountInfo.setOutActualCashAmount (outActualCashAmount.toString ());

            businessAccountInfo.setUserCashOutActualAmount (userCashOutActualAmount.toString ());

            businessAccountInfo.setCashOutChannelFee (cashOutChannelFee.toString ());

            businessAccountInfo.setCashOutBrokerFee (cashOutBrokerFee.toString ());

            businessAccountInfo.setUserApplyCommOutAmount (userApplyCommOutAmount.toString ());

            businessAccountInfo.setCommOutActualAmount (commOutActualAmount.toString ());                       //佣金出金实际支出金额

            businessAccountInfo.setUserCommOutActualToAccountAmount (userCommOutActualToAccountAmount.toString ());

            businessAccountInfo.setCommOutChannelFee (commOutChannelFee.toString ());

            businessAccountInfo.setCommOutActualFee (commOutActualFee.toString ());

            businessAccountInfo.setCommOutBrokerFee (commOutBrokerFee.toString ());

            businessAccountInfo.setTotalChannelFee (totalChannelFee.toString ());

            businessAccountInfo.setTotalBrokerFee (totalBrokerFee.toString ());

            businessAccountInfo.setBrokerActualNetIn (brokerActualNetIn.toString ());

            businessAccountInfo.setPreCashBalance (preCashBalance.toString ());

            businessAccountInfo.setCurrentCashBalance (currentCashBalance.toString ());

            businessAccountInfo.setChannelInCashActualFee (channelInCashActualFee.toString ());
            businessAccountInfo.setChannelInCashFee (channelInCashFee.toString ());
        }
        return businessAccountInfo;
    }

    /**
     * 账户类别明细
     *
     * @param account
     * @param userId
     * @param startDate
     * @param endDate
     * @param currency
     * @return
     */
    @Override
    public BusinessAccountInfo getBrokerAccountTypeDetails(String account, String userId, String startDate, String endDate, String currency) {
        Map<String, BusinessAccountInfo> dataMap = new HashMap<> ();
        Object currBalance = null;
        Object preBalance = null;
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {
            dataMap = getAccData (null, Constants.FLAG_DAY, startDate, endDate, currency, null, null);          //全部账户
            currBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountBalance (currency));//本期现金余额
            preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalance (currency));

        } else if (!StringUtils.isEmpty (account)) { //指定账户
            dataMap = getAccData (account, Constants.FLAG_DAY, startDate, endDate, currency, null, null);
            currBalance = dealAccountData (currency, account).get ("currBalance");//本期现金余额
            preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalanceForMt4 (currency, account));

        } else if (!StringUtils.isEmpty (userId)) {  //指定用户或代理下面所属全部账户
            BigDecimal totalPre = new BigDecimal (0.0);
            BigDecimal totalCurr = new BigDecimal (0.0);
            Set<String> userIds = comUtil.getUserIdSets (userId);
            for (String id : userIds) {
                Object cacheValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalanceForP (currency, id));
                if (null != cacheValue) {
                    totalPre.add ((BigDecimal) cacheValue);
                }
            }

            Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getAgentUserIdHash (), userId);
            if (null == cacheValue) {
                throw new GlobalException ("用户id不存在");
            }
            userId = comUtil.getUserIds (userId);
             String accounts = comUtil.getMt4AccountsByUserId (userId);
            Map<String, Object> accountData = this.dealAccountData (currency, accounts);

            preBalance = totalPre;
            currBalance = accountData.get ("currBalance");
            dataMap = getAccData (null, Constants.FLAG_DAY, startDate, endDate, currency, userId, null);
        }
        BusinessAccountInfo businessAccountInfo = new BusinessAccountInfo ();
        String startDate2 = DateUtil.parserTo (DateUtil.getDayBegin ());
        if (null != dataMap.get (startDate2)) {
            businessAccountInfo = dataMap.get (startDate2);
        }
        businessAccountInfo.setCurrentCashBalance (null == currBalance ? "0" : currBalance.toString ());
        if (null == preBalance) {
            preBalance = 0;
        }
        businessAccountInfo.setPreCashBalance (preBalance.toString ());
        return businessAccountInfo;

    }

    /**
     * 根据条件查询账户信息
     *
     * @param flag（查询颗粒度标示 ：按天，按月）
     * @param startDate
     * @param endDate
     * @param currency     币种
     * @param userId       用户的Id(多个用户Id逗号分割)
     * @return
     */
    private Map<String, BusinessAccountInfo> getAccData(String mt4Acc, String flag, String startDate, String endDate, String currency, String userId, Boolean brokerFlag) {  //不是broker 传false
        if (StringUtils.isEmpty (userId)) {
            userId = null;
        }
        List<Map> cashInList;  //入金
        List<Map> cashOutList;//出金
        List<Map> commOutList;//佣金出金
        Map<String, BusinessAccountInfo> businessAccountInfos = new ConcurrentHashMap<> ();

        if (StringUtils.isEmpty (currency)) { //默认查询人民币账户
            currency = Constants.CURRENCY_RMB;
        }
        switch (flag) {
            case "day":
                cashInList = cashInAndOutDao.getChannelCashInByDay (mt4Acc, currency, userId, startDate, endDate);
                cashOutList = cashInAndOutDao.getChannelCashOutByDay (mt4Acc, currency, userId, startDate, endDate);
                commOutList = cashInAndOutDao.getChannelCommOutByDay (mt4Acc, currency, userId, startDate, endDate);
                break;
            case "month":
                cashInList = cashInAndOutDao.getChannelCashInByMonth (mt4Acc, currency, userId, startDate, endDate);
                cashOutList = cashInAndOutDao.getChannelCashOutByMonth (mt4Acc, currency, userId, startDate, endDate);
                commOutList = cashInAndOutDao.getChannelCommOutByMonth (mt4Acc, currency, userId, startDate, endDate);
                break;
            case "all":   //展示所有的数据
                cashInList = cashInAndOutDao.getChannelCashInByDay (mt4Acc, currency, userId, null, null);
                cashOutList = cashInAndOutDao.getChannelCashOutByDay (mt4Acc, currency, userId, null, null);
                commOutList = cashInAndOutDao.getChannelCommOutByDay (mt4Acc, currency, userId, null, null);
                break;
            default:
                throw new GlobalException ("请传入有效参数");
        }
        if (null == brokerFlag || !brokerFlag) {
            this.cycleBodyForInData (businessAccountInfos, cashInList);
            this.cycleBodyForOutData (businessAccountInfos, cashOutList, Boolean.FALSE);
            this.cycleBodyForOutData (businessAccountInfos, commOutList, Boolean.TRUE);
            this.cycleBodyForTotalFee (businessAccountInfos);
        } else {
            this.cycleBodyForInData (businessAccountInfos, cashInList);
            this.cycleBodyForOutData (businessAccountInfos, cashOutList, Boolean.FALSE);
        }
        return businessAccountInfos;
    }

    /**
     * 入金数据处理
     *
     * @param businessAccountInfos
     * @param cashInList
     */
    private void cycleBodyForInData(Map<String, BusinessAccountInfo> businessAccountInfos, List<Map> cashInList) {
        cashInList.forEach (it -> { //遍历入金集合
            BusinessAccountInfo businessAccountInfo = new BusinessAccountInfo ();
            Object platformActPayFee = it.get ("platformActPayFee");//通道实际手续费‘
            if (null == platformActPayFee) {
                platformActPayFee = 0;
            }
            Object platformPayFee = it.get ("platformPayFee");//通道手续费
            if (null == platformPayFee) {
                platformPayFee = 0;
            }
            Object payFee = it.get ("payFee");//经济商手续费
            if (null == payFee) {
                payFee = 0;
            }
            Object cashIn = it.get ("cashIn");//用户申请入金金额
            if (null == cashIn) {
                cashIn = 0;
            }
            Object userPayActual = it.get ("userPayActual");
            if (null == userPayActual) {
                userPayActual = 0;
            }
            Object toAccountActualAmount = it.get ("toAccountActualAmount");
            if (null == toAccountActualAmount) {
                toAccountActualAmount = 0;
            }

            String dateTime = it.get ("reqTime").toString ();
            businessAccountInfo.setDateTime (dateTime); //显示日期
            businessAccountInfo.setUserApplyInAmount (cashIn.toString ()); //用户申请入金金额
            businessAccountInfo.setUserPayActual (userPayActual.toString ());//用户实际支付
            businessAccountInfo.setToAccountActualAmount (toAccountActualAmount.toString ()); //用户入金实际到账
            businessAccountInfo.setChannelInCashFee (platformPayFee.toString ());//入金通道手续费

            businessAccountInfo.setChannelInCashActualFee (platformActPayFee.toString ()); //通道实际手续费
            businessAccountInfo.setInCashBrokerFee (payFee.toString ());//入金经济商手续费
            businessAccountInfos.put (dateTime, businessAccountInfo);
        });
    }

    /**
     * 出金数据处理
     *
     * @param businessAccountInfos
     * @param cashOutList
     * @param flag                 true 为佣金出金，false 为本金出金
     */
    private void cycleBodyForOutData(Map<String, BusinessAccountInfo> businessAccountInfos, List<Map> cashOutList, Boolean flag) {
        cashOutList.forEach (out -> {  //遍历出金集合

            String dateTime = out.get ("reqTime").toString (); //日期
            Object outPlatformActPayFee = out.get ("platformActPayFee");//通道实际手续费
            if (null == outPlatformActPayFee) {
                outPlatformActPayFee = 0;
            }

            Object actOutAmount = out.get ("userCashOutActualAmount");//出金实际到账

            BigDecimal hk = new BigDecimal (0);//new BigDecimal (outPlatformPayFee.toString ()).subtract (new BigDecimal (outPlatformActPayFee.toString ())); //手续费回扣
            Object outPayFee = out.get ("payFee");//经济商手续费
            if (null == outPayFee) {
                outPayFee = 0;
            }
            Object cashOut = out.get ("cashOut");//用户申请出金金额
            if (null == cashOut) {
                cashOut = 0;
            }
            if (null == actOutAmount) {
                actOutAmount = 0;
            }
            Object outPlatformPayFee = out.get ("platformPayFee");//通道手续费
            if (null == outPlatformPayFee) {
                outPlatformPayFee = 0;
            }
            if (businessAccountInfos.containsKey (dateTime)) {
                BusinessAccountInfo businessAccountInfo = businessAccountInfos.get (dateTime);
                this.setDataInfo (businessAccountInfo, actOutAmount, cashOut, outPayFee, outPlatformPayFee, outPlatformActPayFee, hk, flag);
            } else {
                BusinessAccountInfo businessAccountInfo = new BusinessAccountInfo ();
                businessAccountInfo.setDateTime (dateTime);
                this.setDataInfo (businessAccountInfo, actOutAmount, cashOut, outPayFee, outPlatformPayFee, outPlatformActPayFee, hk, flag);
                businessAccountInfo.setDateTime (dateTime);
                businessAccountInfos.put (dateTime, businessAccountInfo);
            }

        });
    }

    /**
     * 总的费用信息处理
     */
    private void cycleBodyForTotalFee(Map<String, BusinessAccountInfo> businessAccountInfos) {
        businessAccountInfos.values ().forEach (it -> {
            BusinessAccountInfo businessAccountInfo = it;
            String channelInCashFee = businessAccountInfo.getChannelInCashFee ();
            String cashOutChannelFee = businessAccountInfo.getCashOutChannelFee ();
            String commOutChannelFee = businessAccountInfo.getCommOutChannelFee ();
            String commOutBrokerFee = businessAccountInfo.getCommOutBrokerFee ();
            String inCashBrokerFee = businessAccountInfo.getInCashBrokerFee ();
            String cashOutBrokerFee = businessAccountInfo.getCashOutBrokerFee ();
            String toAccountActualAmount = businessAccountInfo.getToAccountActualAmount ();
            String commOutActualAmount = businessAccountInfo.getCommOutActualAmount ();
            String userCashOutActualAmount = businessAccountInfo.getUserCashOutActualAmount ();
            String outActualCashAmount = businessAccountInfo.getOutActualCashAmount ();
            if (StringUtils.isEmpty (userCashOutActualAmount)) {
                userCashOutActualAmount = "0";
            }
            if (StringUtils.isEmpty (commOutActualAmount)) {
                commOutActualAmount = "0";
            }
            if (StringUtils.isEmpty (inCashBrokerFee)) {
                inCashBrokerFee = "0";
            }
            if (StringUtils.isEmpty (commOutChannelFee)) {
                commOutChannelFee = "0";
            }
            if (StringUtils.isEmpty (cashOutChannelFee)) {
                cashOutChannelFee = "0";
            }
            if (StringUtils.isEmpty (channelInCashFee)) {
                channelInCashFee = "0";
            }
            if (StringUtils.isEmpty (cashOutBrokerFee)) {
                cashOutBrokerFee = "0";
            }
            if (StringUtils.isEmpty (inCashBrokerFee)) {
                inCashBrokerFee = "0";
            }
            if (StringUtils.isEmpty (commOutBrokerFee)) {
                commOutBrokerFee = "0";
            }
            if (null == toAccountActualAmount) {
                toAccountActualAmount = "0";
            }
            if (null == commOutActualAmount) {
                commOutActualAmount = "0";
            }
            if (null == userCashOutActualAmount) {
                userCashOutActualAmount = "0";
            }
            if (null == outActualCashAmount) {
                outActualCashAmount = "0";
            }
            it.setDateTime (businessAccountInfo.getDateTime ());
            it.setTotalChannelFee (new BigDecimal (channelInCashFee).add (new BigDecimal (cashOutChannelFee)).add (new BigDecimal (commOutChannelFee)).toString ());//总通道手续费
            it.setTotalBrokerFee (new BigDecimal (commOutBrokerFee).add (new BigDecimal (inCashBrokerFee)).add (new BigDecimal (cashOutBrokerFee)).toString ());//总经济商手续费
            it.setBrokerActualNetIn (new BigDecimal (toAccountActualAmount).subtract (new BigDecimal (commOutActualAmount)).subtract (new BigDecimal (outActualCashAmount)).toString ());//经济商实际净收入
        });

    }

    private void setDataInfo(BusinessAccountInfo businessAccountInfo, Object actOutAmount, Object cashOut, Object outPayFee, Object outPlatformPayFee, Object outPlatformActPayFee, BigDecimal hk, Boolean flag) {
        Double actOut = Double.valueOf (actOutAmount.toString ());
        Double outPlatformFee = Double.valueOf (outPlatformActPayFee.toString ());
        Double act;
        act = actOut + outPlatformFee;
        if (!flag) { //本金出金操作
            businessAccountInfo.setUserApplyOutAmount (cashOut.toString ());//用户申请出金
            businessAccountInfo.setOutActualCashAmount (act.toString ());//实际出金
            businessAccountInfo.setUserCashOutActualAmount (actOutAmount.toString ());//实际到账
            businessAccountInfo.setCashOutChannelFee (outPlatformPayFee.toString ());//本金出金通道手续费
            businessAccountInfo.setOutPlatformActPayFee (outPlatformActPayFee.toString ());//本金出金通道实际手续费
            businessAccountInfo.setCashOutBrokerFee (outPayFee.toString ());//本金出金经纪商手续费
        } else if (flag) { //佣金出金操作
            businessAccountInfo.setUserApplyCommOutAmount (cashOut.toString ());//用户申请佣金出金
            businessAccountInfo.setCommOutActualAmount (act.toString ());//佣金实际出金
            businessAccountInfo.setUserCommOutActualToAccountAmount (actOutAmount.toString ());//实际到账
            businessAccountInfo.setCommOutChannelFee (outPlatformPayFee.toString ());//佣金出金通道手续费
            businessAccountInfo.setCommOutActualFee (outPlatformActPayFee.toString ());//佣金出金通道实际手续费
            businessAccountInfo.setCommOutBrokerFee (outPayFee.toString ());//佣金出金经纪商手续费
        }//commOutActualAmount

    }

    private Map dealAccountData(String currency, String account) {
        Map dataObj = new HashMap ();
        dataObj.put ("commActOut", "0");
        dataObj.put ("cashActIn", "0");
        dataObj.put ("cashActOut", "0");
        dataObj.put ("currBalance", "0");
        Map retMap;
        Object cashIn = 0;
        Object cashOut = 0;
        Object commActOut = 0;
        if (StringUtils.isEmpty (account)) {
            retMap = cashInAndOutDao.getAccountData (currency, null);
        } else {
            retMap = cashInAndOutDao.getAccountData (currency, account);
        }
        if (null != retMap) {
            dataObj = retMap;
            cashIn = retMap.get ("cashActIn");
            if (null == cashIn) {
                cashIn = 0;
            }
            cashOut = retMap.get ("cashActOut");
            if (null == cashOut) {
                cashOut = 0;
            }
            commActOut = retMap.get ("commActOut");
            if (null == commActOut) {
                commActOut = 0;
            }
        }
        Double currBalance = Double.valueOf (cashIn.toString ()) - Double.valueOf (cashOut.toString ()) - Double.valueOf (commActOut.toString ());
        dataObj.put ("currBalance", String.format ("%.2f", currBalance));

        return dataObj;
    }


}
