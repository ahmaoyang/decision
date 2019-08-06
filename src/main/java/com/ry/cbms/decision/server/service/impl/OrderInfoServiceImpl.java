package com.ry.cbms.decision.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.dao.*;
import com.ry.cbms.decision.server.model.*;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.OrderInfoService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.utils.HttpUtil2;
import com.ry.cbms.decision.server.vo.OrderDetailVo;
import com.ry.cbms.decision.server.vo.SingleEvalDataVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author maoYang
 * @Date 2019/6/11 10:28
 * @Description 订单信息模块服务
 */
@Service
@Slf4j
public class OrderInfoServiceImpl implements OrderInfoService {

    @Autowired
    private OrderInfoDao orderInfoDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ComUtil comUtil;

    @Autowired
    private CashInAndOutDao cashInAndOutDao;

    @Autowired
    private TransferDao transferDao;

    @Autowired
    private BondInfoDao bondInfoDao;

    @Autowired
    private RetailBalanceDao retailBalanceDao;

    @Autowired
    private CashedBalanceDao cashedBalanceDao;

    @Autowired
    private RetailDao retailDao;

    @Autowired
    private CommissionDao commissionDao;


    @Override
    public ThrowInfo getThrowHisSum(String account) {
        ThrowInfo throwInfo = orderInfoDao.getThrowInfoHisSum (account);
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getThrowInfoHis (account), throwInfo);
        return null;
    }

    /**
     * 根据Mt4 账号查询，出入金，转入，佣金出金 详情
     *
     * @param mt4Acc mt4账号
     * @return
     */
    @Override
    public Map<String, Object> getSpecifiedAccountDetail(String mt4Acc, String startDate, String endDate, String acctFromType, String flag) {
        List<Map> retList = null;
        String acctType;
        switch (flag) {
            case "0":
                retList = cashInAndOutDao.getCashInByMt4Acc (startDate, endDate, mt4Acc);//入金

                break;
            case "1":
                acctType = Constants.ACCTYPE_CASH;//本金出金
                retList = cashInAndOutDao.getCashOutByMt4Acc (startDate, endDate, mt4Acc, acctType);
                break;

            case "2":
                retList = transferDao.getTransferByMt4Acc (startDate, endDate, mt4Acc, acctFromType); //转账
                break;
            case "3":
                //acctType = Constants.ACCTYPE_COMM;//佣金出金
                retList = comUtil.getCommOutList (startDate, endDate, mt4Acc);
                break;
            default:
                throw new GlobalException ("请传入有效参数");
        }
        Map<String, Object> retMap = new HashMap<> ();
        retMap.put ("retList", retList);
        return retMap;
    }

    /**
     * 查询账户明细展示数据
     *
     * @param mt4Acc
     * @param userId
     * @param curr
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public Map<String, Object> geAccDetailList(String mt4Acc, String userId, String curr, String startDate, String endDate, Integer offset, Integer limit) {
        List<Map> inList;
        List<Map> outList;
        HashMap retMap = new HashMap ();
        Integer count = 0; //总条数
        Map<Object, Map> dealMap = new HashMap ();
        if (!StringUtils.isEmpty (userId) && StringUtils.isEmpty (mt4Acc)) { //指定用户或代理
            mt4Acc = comUtil.getMt4AccountsByUserId (userId);
            if (StringUtils.isEmpty (mt4Acc)) {
                retMap.put ("dataValues", null);
                return retMap;
            }
        }
        if (!StringUtils.isEmpty (endDate)) {
            Date tmpDate = DateUtil.addOrReduceDay (DateUtil.parse (endDate), 1);
            endDate = DateUtil.parserTo (tmpDate);

        }
        count = cashInAndOutDao.getCashOutGroupByMt4AccTotalCount (startDate, endDate, mt4Acc);
        inList = cashInAndOutDao.getCashInGroupByMt4Acc (startDate, endDate, mt4Acc, offset, limit); //入金数据
        outList = cashInAndOutDao.getCashOutGroupByMt4Acc (startDate, endDate, mt4Acc, offset, limit);//出金数据
        for (Map map : outList) {
            Object acc = map.get ("mt4Acct"); //mt4账号
            Object cashOut = map.get ("cashOut");//申请出金
            if (null == cashOut) {
                cashOut = 0;
            }
            List<Map> commMapList = comUtil.getCommOutList (startDate, endDate, acc.toString ());
            Double sumActualToAcc = 0.0;
            for (int i = 0, len = commMapList.size (); i < len; i++) {
                Map comMap = commMapList.get (i);
                Object actComOut = comMap.get ("actualToAcc"); //这个取得是实际佣金支出（每条）
                if (null == actComOut) {
                    actComOut = 0;
                }
                sumActualToAcc += Double.valueOf (actComOut.toString ());
            }
            BigDecimal actualOut = new BigDecimal (cashOut.toString ());//实际出金
            map.put ("actualCashOut", actualOut);//实际本金出金
            map.put ("actualCommOut", sumActualToAcc);//实际佣金出金
            setOrderDealData (acc.toString (), curr, map);
            dealMap.put (acc, map);
        }
        for (Map map : inList) {
            Object mt4Acct = map.get ("mt4Acct");
            List<Map> commMapList = comUtil.getCommOutList (startDate, endDate, mt4Acct.toString ());
            Double sumActualToAcc = 0.0;
            for (int i = 0, len = commMapList.size (); i < len; i++) {
                Map comMap = commMapList.get (i);
                Object actComOut = comMap.get ("actualToAcc"); //这个取得是实际佣金支出（每条）
                if (null == actComOut) {
                    actComOut = 0;
                }
                sumActualToAcc += Double.valueOf (actComOut.toString ());
            }
            if (dealMap.containsKey (mt4Acct)) { //如果包含
                Map dataMap = dealMap.get (mt4Acct);
                dataMap.put ("cashIn", map.get ("cashIn")); //入金实际到账
                sumActualToAcc += Double.valueOf (dataMap.get ("actualCommOut").toString ());
                Double currBalance = Double.valueOf (map.get ("cashIn").toString ()) - Double.valueOf (dataMap.get ("actualCashOut").toString ()) - Double.valueOf (dataMap.get ("actualCommOut").toString ());
                dataMap.put ("currBalance", currBalance); //本期余额
            } else { //如果不包含
                setOrderDealData (mt4Acc, curr, map);
                map.put ("actualCommOut", 0);//实际佣金出金
                map.put ("actualCashOut", 0);//实际本金出金
                map.put ("currBalance", map.get ("cashIn"));//当前余额
                dealMap.put (mt4Acct, map);
                count = count + 1;
            }
            map.put ("actualCommOut", sumActualToAcc);//实际佣金出金
        }
        Collection dataValues = dealMap.values ();
        retMap.put ("dataValues", dataValues);
        retMap.put ("count", count);
        return retMap;
    }


    /**
     * 处理订单数据
     *
     * @param mt4Acc
     * @param curr
     * @param map
     */
    private void setOrderDealData(String mt4Acc, String curr, Map map) {
        Object cashBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCashedBalance (mt4Acc, curr));//兑付余额
        Object currBalanceObj = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountCurrBalanceForMt4 (curr, mt4Acc)); //本期余额
        Object accPreBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalanceForMt4 (curr, mt4Acc));//上期余额
        if (null == accPreBalance) {
            accPreBalance = 0;
        }
        if (null == currBalanceObj) {
            currBalanceObj = 0;
        }
        if (null == cashBalance) {
            cashBalance = 0;
        }
        map.put ("accPreBalance", accPreBalance);
        map.put ("currBalance", currBalanceObj);
        map.put ("cashBalance", cashBalance);
    }

    /**
     * 订单信息-获取账户明细
     *
     * @param account
     * @param userId
     * @param curr
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public Map<String, Object> geAccDetail(String account, String userId, String curr, String startDate, String endDate) {
        BigDecimal preBalance = new BigDecimal (0);
        BigDecimal currentBalance = new BigDecimal (0);
        BigDecimal cashBalance = new BigDecimal (0);//兑付余额
        Map<String, Object> resultMap = new ConcurrentHashMap<> ();
        Object accPreBalance;
        Object currBalanceObj;
        Object cashBa = null;
        if (StringUtils.isEmpty (account) && StringUtils.isEmpty (userId)) {  //全部账户查询
            cashBa = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCashedBalance (null, curr));
            if (null != cashBa) {
                cashBalance = (BigDecimal) cashBa;
            }
            accPreBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalance (curr));
            if (null != accPreBalance) {
                preBalance = (BigDecimal) accPreBalance;//币种总的上期余额
            }
            currBalanceObj = comUtil.getCurrentBalance (curr, null);
            if (null != currBalanceObj) {
                currentBalance = new BigDecimal (currBalanceObj.toString ());//本期余额
            }
        } else if (!StringUtils.isEmpty (account)) { //指定账户
            String uid = comUtil.getUserIdByAcc (account);
            accPreBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalanceForP (curr, uid));
            cashBa = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCashedBalance (account, curr));
            if (null != cashBa) {
                cashBalance = (BigDecimal) cashBa;
            }
            if (null != accPreBalance) {
                preBalance = (BigDecimal) accPreBalance;//币种总的上期余额
            }
            currBalanceObj = comUtil.getCurrentBalance (curr, uid);
            if (null != currBalanceObj) {
                currentBalance = (BigDecimal) currBalanceObj;//本期余额
            }
        } else if (!StringUtils.isEmpty (userId)) { //指定用户或代理下面所属全部账户
            List ids = new ArrayList<> (comUtil.getUserIdSets (userId));
            String uIds = comUtil.getUserIds (userId);
            List accountList = comUtil.getAccList (uIds);
            currentBalance = (BigDecimal) comUtil.getCurrentBalance (curr, uIds);//本期余额
            for (int i = 0, len = ids.size (); i < len; i++) {
                String id = ids.get (i).toString ();
                Object preB = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAccountPreBalanceForP (curr, id));
                Object acc;
                try {
                    acc = accountList.get (i);
                } catch (Exception e) {
                    continue;
                }
                if (null != accountList && null != acc) {
                    cashBa = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCashedBalance (accountList.get (i).toString (), curr));
                }
                if (null != preB) {
                    preBalance = preBalance.add (new BigDecimal (preB.toString ()));//币种总的上期余额
                }
                if (null != cashBa) {
                    cashBalance.add (new BigDecimal (cashBa.toString ()));
                }
            }
        }
        resultMap.put ("preBalance", preBalance);
        resultMap.put ("currentBalance", currentBalance);
        resultMap.put ("cashBalance", cashBalance); //兑付余额
        return resultMap;
    }

    /**
     * 抛单
     *
     * @param account
     * @param flag
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<ThrowInfo> throwOrder(String account, String flag, String startDate, String endDate) {
        List<ThrowInfo> throwInfos;
        switch (flag) {
            case "day":
                throwInfos = orderInfoDao.getThrowInfoDataByDay (account, startDate, endDate);
                break;
            case "week":
                throwInfos = orderInfoDao.getThrowInfoDataByWeek (account, startDate, endDate);
                break;

            case "month":
                throwInfos = orderInfoDao.getThrowInfoDataByMonth (account, startDate, endDate);
                break;
            default:
                throw new GlobalException ("请传入有效参数");

        }
        return throwInfos;
    }

    /**
     * 单量评估
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param currency  币种
     * @param flag      展示 颗粒度 day，month  分别表示天，月
     * @return
     */
    @Override
    public Map<Object, SingleEvalDataVo> SingleEval(String startDate, String endDate, String currency, String flag) {
        Map<Object, SingleEvalDataVo> dataMap = dealSingleEvalData (flag, startDate, endDate, currency);
        return dataMap;
    }


    @Override
    public SingleEvalDataVo SingleEvalBalance(String startDate, String endDate, String curr) {
        SingleEvalDataVo dataInfo = dealSingleEvalDataBalance (startDate, endDate, curr);
        return dataInfo;
    }


    private SingleEvalDataVo dealSingleEvalDataBalance(String startDate, String endDate, String currency) {
        Map cashInMap;//入金
        Map cashOutMap;//出金
        RetailBalance retailBalance;//零售端(美元余额)记录
        RetailAccount retailAccount;//零售端交易记录(已经平仓的)
        CashedBalance cashedBalance;//已兑付余额
        ThrowInfo throwInfo;//抛单
        ThrowInfo totalThrowInfo;//抛单+非抛单
        BigDecimal comm;//佣金生成
        BigDecimal commOut;//佣金出金
        cashInMap = cashInAndOutDao.getChannelCashIn (null, currency, null, startDate, endDate);
        cashOutMap = cashInAndOutDao.getChannelCashOut (null, currency, null, startDate, endDate);
        Object bondCacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getBondBalanceRecord (Constants.FLAG_DAY), endDate);

        retailBalance = retailBalanceDao.getBalance (startDate, endDate);
        comm = commissionDao.sumCommByUserIdAndTime (null, startDate, endDate);
        cashedBalance = cashedBalanceDao.getBalance (currency, startDate, endDate);//兑付余额
        throwInfo = orderInfoDao.getThrowInfoVolume (startDate, endDate);
        totalThrowInfo = orderInfoDao.getTotalThrowInfoVolume (startDate, endDate);
        retailAccount = retailDao.select (startDate, endDate);
        commOut = commissionDao.sumCommOutByUserIdAndTime (null, startDate, endDate);
        Object cashIn = 0;
        Object cashOut = 0;
        if (null != cashInMap) {
            cashIn = cashInMap.get ("cashIn"); //入金实际到账
            if (null == cashIn) {
                cashIn = 0;
            }
        }
        if (null != cashOutMap) {
            cashOut = cashOutMap.get ("cashOut"); //出金
            if (null == cashOut) {
                cashOut = 0;
            }
        }
        if (null == bondCacheValue) {
            bondCacheValue = 0;
        }
        String cashedBalanceNum=null;
        if (null != cashedBalance) {
            cashedBalanceNum = cashedBalance.getCashedBalance ();
        }

        if (null == cashedBalanceNum) {
            cashedBalanceNum = "0";
        }
        Double brokerBalance = Double.valueOf (cashIn.toString ()) - Double.valueOf (cashOut.toString ());
        BigDecimal closeProfit=new BigDecimal (0.0);
        if(null!=retailAccount){
            closeProfit  = retailAccount.getCloseProfitAndLoss ();
        }

        if (null == closeProfit) {
            closeProfit = new BigDecimal (0.0);
        }
        if (null == comm) {
            comm = new BigDecimal (0.0);
        }
        if (null == commOut) {
            commOut = new BigDecimal (0.0);
        }
        Integer totalVolume = totalThrowInfo.getVolume ();
        if (null == totalVolume) {
            totalVolume = 0;
        }
        Integer throwVolume = throwInfo.getVolume ();
        if (null == throwVolume) {
            throwVolume = 0;
        }
        Double retailBalanceNum=0.0;
        if(null!=retailBalance){
            retailBalanceNum= retailBalance.getRetailBalance ();
        }
        if(null==retailBalanceNum){
            retailBalanceNum=0.0;
        }

        Integer notThrowVolume = (totalVolume - throwVolume);

        Double commPay = Double.valueOf (comm.toString ()) - Double.valueOf (commOut.toString ());

        SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
        ComUtil.initial (singleEvalDataVo);
        singleEvalDataVo.setBrokerBalance (brokerBalance.toString ());//经纪商余额
        singleEvalDataVo.setCreateDate (endDate); //创建是日期
        singleEvalDataVo.setBondBalance (bondCacheValue.toString ());

        singleEvalDataVo.setRetailAccountRmbBalance ((brokerBalance - Double.valueOf (cashedBalanceNum)) + "");
        singleEvalDataVo.setRetailAccountDollarBalance (retailBalanceNum.toString ());
        singleEvalDataVo.setClosedProfit (closeProfit.toString ());
        singleEvalDataVo.setCommBalance (commPay.toString ());
        singleEvalDataVo.setCashedBalance (cashedBalanceNum);
        singleEvalDataVo.setTotalTradeNum (totalVolume.toString ());
        singleEvalDataVo.setThrowTradeNum (throwVolume.toString ());
        singleEvalDataVo.setNotThrowTradeNum (notThrowVolume.toString ());
        return singleEvalDataVo;
    }


    /**
     * 单量评估数据处理
     *
     * @param flag
     * @param startDate
     * @param endDate
     * @param currency
     */
    private Map<Object, SingleEvalDataVo> dealSingleEvalData(String flag, String startDate, String endDate, String currency) {
        Map<Object, SingleEvalDataVo> singleEvalDataMap = new HashMap<> ();//返回数据
        List<Map> cashInList;//入金
        List<Map> cashOutList;//出金
        List<Map<String, Object>> bondInfoList;//保证金
        List<RetailBalance> retailBalanceList;//零售端(美元余额)记录
        List<RetailAccount> retailAccountList;//零售端交易记录(已经平仓的)
        List<CashedBalance> cashedBalanceList;//已兑付余额
        List<ThrowInfo> throwInfoListList;//抛单
        List<ThrowInfo> totalThrowInfoListList;//抛单+非抛单

        List<Map<String, Object>> commList;//佣金生成
        List<Map> commOutList;//佣金出金

        switch (flag) {
            case "day":
                cashInList = cashInAndOutDao.getChannelCashInByDay (null, currency, null, startDate, endDate);
                cashOutList = cashInAndOutDao.getChannelCashOutByDay (null, currency, null, startDate, endDate);
                bondInfoList = bondInfoDao.getBondBalanceByDay (startDate, endDate);

                retailBalanceList = retailBalanceDao.getBalanceByCondition (startDate, endDate);
                commList = commissionDao.selectCommByDay (startDate, endDate);
                cashedBalanceList = cashedBalanceDao.getBalanceByDay (currency, startDate, endDate);//兑付余额
                throwInfoListList = orderInfoDao.getThrowInfoVolumeByDay (startDate, endDate);
                totalThrowInfoListList = orderInfoDao.getTotalThrowInfoVolumeByDay (startDate, endDate);
                retailAccountList = retailDao.selectByDay (null, startDate, endDate);
                commOutList = cashInAndOutDao.getChannelCommOutByDay (null, currency, null, startDate, endDate);
                break;
            case "month":
                cashInList = cashInAndOutDao.getChannelCashInByMonth (null, currency, null, startDate, endDate);
                cashOutList = cashInAndOutDao.getChannelCashOutByMonth (null, currency, null, startDate, endDate);
                bondInfoList = bondInfoDao.getBondBalanceByMonth (startDate, endDate);
                retailBalanceList = retailBalanceDao.getBalanceByMonth (startDate, endDate);
                cashedBalanceList = cashedBalanceDao.getBalanceByMonth (currency, startDate, endDate);
                throwInfoListList = orderInfoDao.getThrowInfoVolumeByMonth (startDate, endDate);
                totalThrowInfoListList = orderInfoDao.getTotalThrowInfoVolumeByMonth (startDate, endDate);
                retailAccountList = retailDao.selectByMonth (null, startDate, endDate);
                commList = commissionDao.selectCommByMonth (startDate, endDate);
                commOutList = cashInAndOutDao.getChannelCommOutByMonth (null, currency, null, startDate, endDate);
                break;
            default:
                throw new GlobalException ("请传入有效参数");
        }

        cashInList.forEach (cashInMap -> {
            Object cashIn = cashInMap.get ("cashIn"); //入金实际到账
            if (null == cashIn) {
                cashIn = 0;
            }
            Object reqTime = cashInMap.get ("reqTime");
            if (null != reqTime) {
                SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                ComUtil.initial (singleEvalDataVo);
                singleEvalDataVo.setBrokerBalance (cashIn.toString ());//经纪商余额
                singleEvalDataVo.setCreateDate (reqTime.toString ()); //创建是日期

                singleEvalDataMap.put (reqTime, singleEvalDataVo);
            }
        });
        cashOutList.forEach (cashOutMap -> {  //出金遍历
            Object cashOut = cashOutMap.get ("cashOut");//申请出金金额
            if (null == cashOut) {
                cashOut = 0;
            }
            Object reqTime = cashOutMap.get ("reqTime");
            if (null != reqTime) {
                if (singleEvalDataMap.containsKey (reqTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (reqTime);
                    if (null != singleEvalData) {
                        String brokerBalance = singleEvalData.getBrokerBalance ();
                        Double breakBalanceDouble = 0.0;
                        if (null != brokerBalance) {
                            breakBalanceDouble += (Double.valueOf (brokerBalance) - Double.valueOf (String.valueOf (cashOut)));
                            singleEvalData.setBrokerBalance (breakBalanceDouble.toString ());//经济商余额
                            Double retailBalance = Double.valueOf (breakBalanceDouble);
                            singleEvalData.setRetailAccountRmbBalance (retailBalance.toString ()); //应付零售端XX币余额
                            singleEvalDataMap.put (reqTime, singleEvalData);

                        }
                    }
                } else {
                    Double brokerBalance = Double.valueOf (0.0) - Double.valueOf (cashOut.toString ());
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setBrokerBalance (brokerBalance.toString ());
                    singleEvalDataVo.setCreateDate (reqTime.toString ());
                    singleEvalDataMap.put (reqTime, singleEvalDataVo);
                }
            }
        });
        if (null != bondInfoList && bondInfoList.size () > 0) {
            bondInfoList.forEach (bondInfoMap -> { //设置保证金
                Object cashBalance = bondInfoMap.get ("cashBalance");  //保证金余额
                if (null == cashBalance) {
                    cashBalance = 0;
                }
                Object reqTime = bondInfoMap.get ("reqTime");
                if (null != cashBalance && null != reqTime) {
                    if (singleEvalDataMap.containsKey (reqTime)) {
                        SingleEvalDataVo singleEvalData = singleEvalDataMap.get (reqTime);
                        if (null != singleEvalData) {
                            singleEvalData.setBondBalance (cashBalance.toString ());
                            singleEvalDataMap.put (reqTime, singleEvalData);
                        }
                    } else {
                        SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                        ComUtil.initial (singleEvalDataVo);
                        singleEvalDataVo.setBrokerBalance ("0");
                        singleEvalDataVo.setCreateDate (reqTime.toString ());
                        singleEvalDataVo.setBondBalance (cashBalance.toString ());
                        singleEvalDataMap.put (reqTime, singleEvalDataVo);
                    }
                }
            });
        }
        retailBalanceList.forEach (retailBalance -> { //遍历零售端美元余额
            Double balance = retailBalance.getRetailBalance ();
            if (null == balance) {
                balance = 0.0;
            }
            Object createTime = retailBalance.getCreateTime ();
            if (null != balance && null != createTime) {
                if (singleEvalDataMap.containsKey (createTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (createTime);
                    if (null != singleEvalData) {
                        singleEvalData.setRetailAccountDollarBalance (balance.toString ()); //应付零售账户美元余额
                        singleEvalDataMap.put (createTime, singleEvalData);
                    }
                } else {
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setRetailAccountDollarBalance (balance.toString ());//应付零售账户美元余额
                    singleEvalDataVo.setCreateDate (createTime.toString ());
                    singleEvalDataMap.put (createTime, singleEvalDataVo);

                }
            }
        });
        cashedBalanceList.forEach (cashedBalance -> {
            String balance = cashedBalance.getCashedBalance ();//已兑付(XX)币余额
            if (null == balance) {
                balance = "0.0";
            }
            Object createTime = cashedBalance.getCreateTime ();
            if (null != balance && null != createTime) {
                if (singleEvalDataMap.containsKey (createTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (createTime);
                    if (null != singleEvalData) {
                        singleEvalData.setCashedBalance (balance);
                        Object retailBalance = singleEvalData.getRetailAccountRmbBalance ();
                        if (null == retailBalance) {
                            retailBalance = 0;
                        }

                        String brokerBalance = singleEvalData.getBrokerBalance ();
                        if (null == brokerBalance) {
                            brokerBalance = "0";
                        }
                        if (null == balance) {
                            balance = "0";
                        }
                        retailBalance = Double.valueOf ( brokerBalance) - Double.valueOf (balance);
                        singleEvalData.setRetailAccountRmbBalance (retailBalance.toString ());
                        singleEvalDataMap.put (createTime, singleEvalData);
                    }
                } else {
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setCashedBalance (balance);//已兑付XX币余额
                    singleEvalDataVo.setCreateDate (createTime.toString ());
                    singleEvalDataMap.put (createTime, singleEvalDataVo);

                }
            }
        });
        retailAccountList.forEach (retailAccount -> {
            BigDecimal closeProfitAndLoss = retailAccount.getCloseProfitAndLoss ();//已平仓盈亏
            Object createTime = retailAccount.getCreateTime ();//日期
            if (null == closeProfitAndLoss) {
                closeProfitAndLoss = new BigDecimal (0);
            }
            if (null != closeProfitAndLoss && null != createTime) {
                if (singleEvalDataMap.containsKey (createTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (createTime);
                    if (null != singleEvalData) {
                        singleEvalData.setClosedProfit (closeProfitAndLoss.toString ());
                        singleEvalDataMap.put (createTime, singleEvalData);
                    }
                } else {
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setBrokerBalance ("0");
                    singleEvalDataVo.setCreateDate (createTime.toString ());
                    singleEvalDataVo.setClosedProfit (closeProfitAndLoss.toString ());
                    singleEvalDataMap.put (createTime, singleEvalDataVo);

                }
            }

        });
        commList.forEach (commission -> {
            Object balance = commission.get ("balance");//佣金生成
            Object createTime = commission.get ("reqTime");//日期
            if (null == balance) {
                balance = new BigDecimal (0);
            }
            if (null != balance && null != createTime) {
                if (singleEvalDataMap.containsKey (createTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (createTime);
                    if (null != singleEvalData) {
                        singleEvalData.setCommBalance (balance.toString ());
                        singleEvalDataMap.put (createTime, singleEvalData);
                    }
                } else {
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setBrokerBalance ("0");
                    singleEvalDataVo.setCreateDate (createTime.toString ());
                    singleEvalDataVo.setCommBalance (balance.toString ());
                    singleEvalDataMap.put (createTime, singleEvalDataVo);

                }
            }

        });
        commOutList.forEach (commOut -> {
            Object balance = commOut.get ("commOut");//佣金生成
            Object createTime = commOut.get ("reqTime");//日期
            if (null == balance) {
                balance = new BigDecimal (0);
            }
            if (null != balance && null != createTime) {
                if (singleEvalDataMap.containsKey (createTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (createTime);
                    if (null != singleEvalData) {
                        String commBalance = singleEvalData.getCommBalance ();
                        if (null == commBalance) {
                            commBalance = "0";
                        }
                        Double balanceComm = Double.valueOf (commBalance) - Double.valueOf (balance.toString ());
                        singleEvalData.setCommBalance (balanceComm.toString ());
                        singleEvalDataMap.put (createTime, singleEvalData);
                    }
                } else {
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setBrokerBalance ("0");
                    singleEvalDataVo.setCreateDate (createTime.toString ());
                    Double balanceComm = -Double.valueOf (balance.toString ());
                    singleEvalDataVo.setCommBalance (balanceComm.toString ());
                    singleEvalDataMap.put (createTime, singleEvalDataVo);

                }
            }

        });
        throwInfoListList.forEach (throwInfo -> {
            Integer volume = throwInfo.getVolume ();//抛单交易量
            Object createTime = throwInfo.getCreateTime ();
            if (null == volume) {
                volume = 0;
            }
            if (null != volume && null != createTime) {
                if (singleEvalDataMap.containsKey (createTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (createTime);
                    if (null != singleEvalData) {
                        singleEvalData.setThrowTradeNum (volume.toString ()); //抛单交易量
                        singleEvalDataMap.put (createTime, singleEvalData);
                    }
                } else {
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setBrokerBalance ("0");
                    singleEvalDataVo.setCreateDate (createTime.toString ());
                    singleEvalDataVo.setThrowTradeNum (volume.toString ());
                    singleEvalDataMap.put (createTime, singleEvalDataVo);

                }
            }

        });
        totalThrowInfoListList.forEach (totalThrowInfo -> {
            Integer volume = totalThrowInfo.getVolume ();//抛单交易量
            Object createTime = totalThrowInfo.getCreateTime ();
            if (null == volume) {
                volume = 0;
            }
            if (null != volume && null != createTime) {
                if (singleEvalDataMap.containsKey (createTime)) {
                    SingleEvalDataVo singleEvalData = singleEvalDataMap.get (createTime);
                    if (null != singleEvalData) {
                        singleEvalData.setTotalTradeNum (volume.toString ()); //总抛单交易量
                        String throwVolume = singleEvalData.getThrowTradeNum ();
                        if (null == throwVolume) {
                            throwVolume = "0";
                        }
                        Integer notThrowTradeNum = volume - Integer.valueOf (throwVolume); //非抛单交易量
                        singleEvalData.setNotThrowTradeNum (notThrowTradeNum.toString ());
                        singleEvalDataMap.put (createTime, singleEvalData);
                    }
                } else {
                    SingleEvalDataVo singleEvalDataVo = new SingleEvalDataVo ();
                    ComUtil.initial (singleEvalDataVo);
                    singleEvalDataVo.setBrokerBalance ("0");
                    singleEvalDataVo.setCreateDate (createTime.toString ());
                    singleEvalDataVo.setTotalTradeNum (volume.toString ());
                    singleEvalDataVo.setNotThrowTradeNum ("0");
                    singleEvalDataVo.setThrowTradeNum (volume.toString ());
                    singleEvalDataMap.put (createTime, singleEvalDataVo);

                }
            }

        });
        return singleEvalDataMap;
    }


    /**
     * 订单明细
     *
     * @param account
     * @param orderFlag
     * @param startDate
     * @param endDate
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public Map<String, Object> getOrderDetails(String account, String orderFlag, String startDate, String endDate, Integer offset, Integer limit) {
        String accountFlag = account;
        List orderDetailList = new ArrayList<> ();
        if (StringUtils.isEmpty (account)) {
            Object cacheList = redisTemplate.opsForValue ().get (RedisKeyGenerator.getAllOrderDetails ());
            if (null != cacheList) {
                orderDetailList = (List<OrderDetail>) cacheList;
            }
        } else {
            orderDetailList = orderInfoDao.getOrderDetailByCondition (account, orderFlag, startDate, endDate, offset, limit);
        }
        Integer count = orderInfoDao.getOrderDetailCount (account, orderFlag, startDate, endDate);
        Object orderDetailMap;
        if (StringUtils.isEmpty (accountFlag)) {
            orderDetailMap = redisTemplate.opsForValue ().get (RedisKeyGenerator.getOrderDetailAcc ("all"));
            HashMap paramMap = new HashMap ();
            if (null != orderDetailMap) {
                paramMap = (HashMap) orderDetailMap;
                // ((HashMap) orderDetailMap).clear ();
            }
            Object brokerProfit = paramMap.get ("brokerProfitTotal");
            Object feeTotal = paramMap.get ("feeTotal");
            Object customerOrderProfitTotal = paramMap.get ("customerOrderProfitTotal");
            Object clearAccProfitTotal = paramMap.get ("clearAccProfitTotal");
            if (StringUtils.isEmpty (brokerProfit)) {
                brokerProfit = 0;
            }
            if (StringUtils.isEmpty (feeTotal)) {
                feeTotal = 0;
            }
            if (StringUtils.isEmpty (customerOrderProfitTotal)) {
                customerOrderProfitTotal = 0;
            }
            if (StringUtils.isEmpty (clearAccProfitTotal)) {
                clearAccProfitTotal = 0;
            }
            paramMap.put ("brokerProfit", brokerProfit);
            paramMap.put ("fee", feeTotal);
            paramMap.put ("customerOrderProfit", customerOrderProfitTotal);
            paramMap.put ("clearAccProfit", clearAccProfitTotal);
            orderDetailMap = paramMap;
        } else {
            orderDetailMap = redisTemplate.opsForValue ().get (RedisKeyGenerator.getOrderDetailAcc (account));
        }
        Map<String, Object> totalDetailMap = null;
        if (null != orderDetailMap) {
            totalDetailMap = (HashMap) orderDetailMap;
        }
        checkAccProblem (orderDetailList);
        Map<String, Object> retMap = new HashMap<> ();
        retMap.put ("count", count);
        retMap.put ("orderDetailList", orderDetailList);
        retMap.put ("totalDetailData", totalDetailMap);
        return retMap;
    }

    private void checkAccProblem(List orderDetailList) {
        List<HashMap> orderMapList = new ArrayList (orderDetailList);
        if (null != orderMapList && orderMapList.size () > 0) {
            orderMapList.forEach (
                    orderDetail -> {
                        Object acc = orderDetail.get ("account");
                        if (null != acc) {
                            if (!StringUtils.isEmpty (acc)) {
                                Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getHaveProOrderAcc (), acc);
                                if (null != cacheValue) {
                                    orderDetail.put ("isRed", true);
                                } else {
                                    orderDetail.put ("isRed", false);
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 加载订单明细
     *
     * @param account 账号
     * @return
     */
    public List<OrderDetail> loadOrderDetails(String account, String mt4Token) {  ///
        List<NameValuePair> nameValuePairList = new ArrayList<> ();
        List<OrderDetail> orderDetailList = new ArrayList<> ();
        String currTimeMillis = String.valueOf (DateUtil.addOrReduceHourTime (DateUtil.addOrReduceDay (new Date (), 1), -6).getTime () / 1000);
        String from = "0";
        Object lastLoadTime = redisTemplate.opsForValue ().get (RedisKeyGenerator.getLoadOrderDetail (RedisKeyGenerator.getLoadOrderDetailLastTime ()));
        if (null != lastLoadTime) {
            from = Long.valueOf (lastLoadTime.toString ()) - 24 * 60 * 60 + "";
        }
        String requestUrl = Constants.MT4_SERVER_URL + "user/tradesUserHistory";
        BasicNameValuePair loginParam = new BasicNameValuePair ("login", account);//登陆账号
        BasicNameValuePair fromParam = new BasicNameValuePair ("from", from);//登陆账号
        BasicNameValuePair toParam = new BasicNameValuePair ("to", currTimeMillis);//查询截止时间
        nameValuePairList.add (loginParam);
        nameValuePairList.add (fromParam);
        nameValuePairList.add (toParam);
        JSONObject res = HttpUtil2.doGet (requestUrl, mt4Token, nameValuePairList);//查询每个账号的信息
        if ("0".equals (res.getString ("code"))) { //请求成功
            JSONArray dataArr = res.getJSONArray ("data");
            if (null != dataArr && dataArr.size () > 0) {
                JSONObject orderObject;
                for (int k = 0, len = dataArr.size (); k < len; k++) {
                    OrderDetail orderDetail = new OrderDetail ();
                    orderObject = dataArr.getJSONObject (k);
                    if (null == orderObject) {
                        continue;
                    }
                    Integer cmd = orderObject.getInteger ("cmd");
                    if (!"0".equals (cmd.toString ()) && !"1".equals (cmd.toString ())) {
                        continue;
                    }
                    Double profit = orderObject.getDouble ("profit");//订单盈亏
                    Double commission = orderObject.getDouble ("commission");//手续费
                    String orderId = orderObject.getString ("order");//订单号
                    BigDecimal comm = orderInfoDao.getThrowCommByOrderId (orderId);//定单返佣金额
                    ThrowInfo throwInfo = orderInfoDao.getByOrderId (orderId);
                    if (null == profit) {
                        profit = 0.0;
                    }
                    if (null == comm) {
                        comm = new BigDecimal (0);
                    }
                    if (null == commission) {
                        commission = 0.0;
                    }
                    Object throwProfit = 0;
                    String clearAccOrderId = null;
                    if (null != throwInfo) {
                        throwProfit = throwInfo.getThrowProfit ();
                        if (null == throwProfit) {
                            throwProfit = 0;
                        }
                        clearAccOrderId = throwInfo.getClearAccOrderId ();
                    }
                    Date createTime = DateUtil.parse (DateUtil.timeStamp2Date (orderObject.getLong ("closeTime") * 1000, null));
                    orderDetail.setCreateTime (createTime);
                    orderDetail.setAccount (account);
                    orderDetail.setCustomerOrder (orderId);
                    orderDetail.setCustomerOrderProfit (profit.toString ());
                    orderDetail.setClearAccProfit (throwProfit.toString ());
                    orderDetail.setClearAccOrderNo (clearAccOrderId);
                    orderDetail.setFee (commission.toString ());
                    orderDetail.setReturnComm (comm.toString ());
                    String brokerProfit = new BigDecimal (throwProfit.toString ()).subtract (new BigDecimal (profit)).toString ();
                    orderDetail.setBrokerProfit (String.format ("%.2f", Double.valueOf (brokerProfit)));
                    orderDetailList.add (orderDetail);
                }
            }
        }
        return orderDetailList;
    }

    /**
     * 根据mt4 账号查询订单记录
     *
     * @param account
     * @return
     */
    @Override
    public List<OrderDetailVo> getOrderRecordByAccount(String account) {
        List<OrderDetailVo> orderDetailRecords = orderInfoDao.getOrderRecordByAccount (account);
        orderDetailRecords.forEach (orderDetail -> {
            String cusProfit = orderDetail.getCustomerOrderProfit ();
            String clearAccProfit = orderDetail.getClearAccProfit ();
            String brokerProfit = orderDetail.getBrokerProfit ();
            Boolean flag = this.calProblemOrder (cusProfit, clearAccProfit, brokerProfit);
            orderDetail.setProblemOrder (flag);
        });
        return orderDetailRecords;
    }

    @Override
    public List<OrderDetailVo> getOrderRecordByAccountAndDate(String account, String startDate, String endDate) {

        List<OrderDetailVo> orderDetailRecords = orderInfoDao.getOrderRecordByAccountAndDate (account, startDate, endDate);
        return orderDetailRecords;
    }

    /**
     * 问题订单筛选
     *
     * @param cusProfit      客户订单盈亏
     * @param clearAccProfit 清算账户盈亏
     * @param brokerProfit   经纪商盈亏
     * @return
     */
    private Boolean calProblemOrder(String cusProfit, String clearAccProfit, String brokerProfit) {
        if (StringUtils.isEmpty (cusProfit)) {
            cusProfit = "0";
        }
        if (StringUtils.isEmpty (clearAccProfit)) {
            clearAccProfit = "0";
        }
        if (StringUtils.isEmpty (brokerProfit)) {
            brokerProfit = "0";
        }
        if (Double.valueOf (cusProfit) > Double.valueOf (clearAccProfit)) {
            return Boolean.TRUE;

        }
        return Boolean.FALSE;
    }
}
