package com.ry.cbms.decision.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.dao.AccessGoldCheckDao;
import com.ry.cbms.decision.server.dao.RetailDao;
import com.ry.cbms.decision.server.model.RetailAccount;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.RetailService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import com.ry.cbms.decision.server.utils.HttpUtil2;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author maoYang
 * @Date 2019/5/31 17:01
 * @Description 零售端服务实现类
 */
@Service
public class RetailServiceImpl implements RetailService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AccessGoldCheckDao accessGoldCheckDao;

    @Autowired
    private RetailDao retailDao;

    @Autowired
    private ComUtil comUtil;

    @Override
    public Map<String, Object> getRetailInfo(String account, String userId, String timeFlag, String startDate, String endDate) {
        JSONObject retailInfo = new JSONObject ();
        Object preRetailBalance;//上期零售端余额
        Object currRetailBalance;//本期零售端余额
        Object currRetailNet;//本期零售端净值]
        List<RetailAccount> retailAccountList;
        if (StringUtils.isEmpty (account)) {
            preRetailBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getPreRetailBalance ());
            currRetailBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getRetailBalance ());
            currRetailNet = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCurEquity ());
        } else {
            preRetailBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getPreRetailBalance (account));
            currRetailBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getRetailBalance (account));
            currRetailNet = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCurEquity (account));
        }

        retailInfo.put ("preRetailBalance", null == preRetailBalance ? 0 : preRetailBalance);
        retailInfo.put ("currRetailBalance", null == currRetailBalance ? 0 : currRetailBalance);
        retailInfo.put ("currRetailNet", null == currRetailNet ? 0 : currRetailBalance);
        if (!StringUtils.isEmpty (userId)) {
            String userIds = comUtil.getUserIds (userId);
            if (null != userIds) {
                List accountList = retailDao.SelectMt4AccsByUserId (userIds);
                if (null != accountList && accountList.size () > 0) {
                    Set set = new HashSet<> (accountList);
                    account = ComUtil.covSetToString (set);
                }
            }
        }
        retailAccountList = getRetailInfoByTimeFlag (account, timeFlag, startDate, endDate);
        retailInfo.put ("retailAccountList", retailAccountList);
        return retailInfo;
    }


    private List<RetailAccount> getRetailInfoByTimeFlag(String account, String timeFlag, String startDate, String endDate) {
        List<RetailAccount> retailAccountList;
        switch (timeFlag) {
            case "day":
                retailAccountList = retailDao.selectByDay (account, startDate, endDate);
                return retailAccountList;
            case "month":
                retailAccountList = retailDao.selectByMonth (account, startDate, endDate);
                return retailAccountList;
            default:
                return null;
        }
    }

    /**
     * 将所有零售端相关信息同步落库
     */
    @Override
    public List<RetailAccount> getAllRetailInfo() {
        List<RetailAccount> retailAccountList = dealClose ();
        for (RetailAccount retailAccount : retailAccountList) {
            String mt4Acc = retailAccount.getAccount ();
            if (!StringUtils.isEmpty (mt4Acc)) {
                Object mt4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
                if (null == mt4Token) {
                    return null;
                }
                String reqUrl = Constants.MT4_SERVER_URL + "openNoticeTrader";
                List<NameValuePair> nameValuePairList = new ArrayList<> ();
                BasicNameValuePair param1 = new BasicNameValuePair ("login", mt4Acc);
                BasicNameValuePair param2 = new BasicNameValuePair ("serverId ", Constants.Mt4ServerId);
                nameValuePairList.add (param1);
                nameValuePairList.add (param2);
                JSONObject response = HttpUtil2.doGet (reqUrl, mt4Token.toString (), nameValuePairList);
                String code = null;
                try {
                    code = response.getString ("code");
                } catch (Exception e) {
                    throw new GlobalException ("mt4服务已宕机");
                }
                if ("0".equals (code)) {
                    JSONArray orders = response.getJSONArray ("data");
                    if (null != orders && orders.size () > 0) {
                        Double profit = 0.0;
                        Double otherFund = 0.0;
                        for (int i = 0, len = orders.size (); i < len; i++) {
                            JSONObject object = orders.getJSONObject (i);
                            if (null != object.getDouble ("profit")) {
                                if ("6".equals (object.getString ("cmd"))) {
                                    Object comment = object.getString ("comment");
                                    if (null != comment && comment.toString ().contains ("backup")) {
                                        otherFund += object.getDouble ("profit");
                                    }
                                } else {
                                    profit = +object.getDouble ("profit");
                                }
                            }
                        }
                        retailAccount.setOpenProfitAndLoss (new BigDecimal (profit));//未平仓盈亏
                        retailAccount.setOtherFunds (new BigDecimal (otherFund));//其他款项（异常情况）
                    }
                }
            }
        }
        return retailAccountList;
    }

    /**
     * 入金组合操作
     *
     * @return
     */
    public List<RetailAccount> dealRetailDeposit() {
        List<Map> depositRecords = accessGoldCheckDao.getAllDepositRecord ();//所有入金记录
        List<RetailAccount> retailAccountList = new ArrayList<> ();//零售端账户记录
        Map<String, RetailAccount> retailMap = Maps.newHashMap ();
        Double payFee = 0.0;//经济商手续费
        Double platformPayFee = 0.0;//通道手续费
        if (null != depositRecords && depositRecords.size () > 0) {
            for (Map retMap : depositRecords) {
                Object mt4Acct = retMap.get ("mt4Acct");
                try {
                    if (null != mt4Acct) {
                        RetailAccount retailAccount = new RetailAccount ();
                        Object mapKey = mt4Acct + DateUtil.formatDate (DateUtil.parse (retMap.get ("createTime").toString ()));
                        if (retailMap.containsKey (mapKey.toString ())) { //如果集合中包含
                            retailAccount = retailMap.get (mapKey.toString ());
                            retailAccountList.remove (retailAccount);
                            retailAccount.setPrincipalIn (retailAccount.getPrincipalIn ().add (new BigDecimal (retMap.get ("cashIn").toString ())));//入金相加
                            retailAccount.setNetCashIn (retailAccount.getNetCashIn ().add (new BigDecimal (Double.valueOf (retMap.get ("cashIn").toString ()) - payFee - platformPayFee)));//手续费相加
                            retailMap.put (mt4Acct + DateUtil.formatDate (DateUtil.parse (retMap.get ("createTime").toString ())), retailAccount);
                        } else {
                            retailAccount.setAccount (mt4Acct.toString ());
                            if (null != retMap.get ("cashIn")) {
                                retailAccount.setPrincipalIn (new BigDecimal (retMap.get ("cashIn").toString ()));//入金金额
                            }
                            if (null != retMap.get ("createTime")) {
                                retailAccount.setCreateTime (retMap.get ("createTime").toString ()); //记录创建时间
                            }
                            if (null != retMap.get ("payFee")) { //经济商手续费
                                payFee = Double.valueOf (retMap.get ("payFee").toString ());
                            }
                            if (null != retMap.get ("platformPayFee")) { //渠道手续费
                                platformPayFee = Double.valueOf (retMap.get ("platformPayFee").toString ());
                            }
                            retailAccount.setNetCashIn (new BigDecimal (Double.valueOf (retMap.get ("cashIn").toString ()) - payFee - platformPayFee));//入金净值

                            retailAccount.setAccount (mt4Acct.toString ()); //mt4账号

                            retailMap.put (mapKey.toString (), retailAccount);
                        }
                        retailAccountList.add (retailAccount);//添加到集合

                    }
                } catch (Exception e) {
                    continue;
                }

            }
        }
        return retailAccountList;
    }

    /**
     * 出金组合操作
     *
     * @return
     */
    public List<RetailAccount> dealDepositWithdraw() {
        List<RetailAccount> retailAccountList = dealRetailDeposit ();
        for (RetailAccount retailAccount : retailAccountList) {
            List<Map> depositRecords = accessGoldCheckDao.getAllWithdrawRecord (retailAccount.getUserId (), DateUtil.getDayStartTime (DateUtil.parse (retailAccount.getCreateTime ())), DateUtil.getDayEndTime (DateUtil.parse (retailAccount.getCreateTime ())));//出金记录
            BigDecimal cashOut = new BigDecimal (0.0);
            BigDecimal commToCash = new BigDecimal (0.0);
            for (Map depositMap : depositRecords) {
                if (retailAccount.getAccount ().equals (depositMap.get ("mt4Acct").toString ())) {
                    cashOut.add (new BigDecimal (depositMap.get ("cashOut").toString ()));
                }
                if ("2".equals (depositMap.get ("acctType"))) {  //佣金转成本金
                    commToCash.add (new BigDecimal (depositMap.get ("cashOut").toString ()));
                }
            }
            retailAccount.setPrincipalOut (cashOut);
            retailAccount.setCommissionToPrincipal (commToCash);
        }
        return retailAccountList;
    }

    /**
     * 组合平仓数据
     *
     * @return
     */
    public List<RetailAccount> dealClose() {

        List<RetailAccount> retailAccountList = dealDepositWithdraw ();
        for (RetailAccount retailAccount : retailAccountList) {
            BigDecimal closedProfit = new BigDecimal (0.0);//已平仓盈亏
            BigDecimal deduction = new BigDecimal (0.0);//赔付金额
            List<Map> closeRecords = accessGoldCheckDao.getAllDetailCloseRecord (retailAccount.getUserId (), DateUtil.getDayStartTime (DateUtil.parse (retailAccount.getCreateTime ())), DateUtil.getDayEndTime (DateUtil.parse (retailAccount.getCreateTime ())));

            List<Map> disputeRecords = accessGoldCheckDao.getAllDetailDisputeRecord (retailAccount.getUserId (), DateUtil.getDayStartTime (DateUtil.parse (retailAccount.getCreateTime ())), DateUtil.getDayEndTime (DateUtil.parse (retailAccount.getCreateTime ())));//争议金额

            List<Map> compensateRecords = accessGoldCheckDao.getAllDetailCompensateRecord (retailAccount.getUserId (), DateUtil.getDayStartTime (DateUtil.parse (retailAccount.getCreateTime ())), DateUtil.getDayEndTime (DateUtil.parse (retailAccount.getCreateTime ())));//争议金额
            if (null != closeRecords && closeRecords.size () > 0) {
                for (Map depositMap : closeRecords) {
                    if (retailAccount.getAccount ().equals (depositMap.get ("mt4Acct").toString ())) {
                        closedProfit.add (new BigDecimal (depositMap.get ("profit").toString ()));
                    }

                }
            }
            if (null != disputeRecords && disputeRecords.size () > 0) {
                for (Map disputeMap : disputeRecords) {
                    if (retailAccount.getAccount ().equals (disputeMap.get ("mt4Acct").toString ())) {
                        deduction.add (new BigDecimal (disputeMap.get ("allBalance").toString ()));
                    }

                }
            }

            if (null != compensateRecords && compensateRecords.size () > 0) {
                for (Map compensate : compensateRecords) {
                    if (retailAccount.getAccount ().equals (compensate.get ("mt4Acct").toString ())) {
                        deduction.add (new BigDecimal (compensate.get ("compensate").toString ()));
                    }

                }
            }
            retailAccount.setCloseProfitAndLoss (closedProfit);
            retailAccount.setDeduction (deduction);
        }
        return retailAccountList;
    }

    @Override
    public RetailAccount getHisRetailInfo(String account) {
        RetailAccount retailAccount = retailDao.getRetailHisSum (account);
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getRetailHisSum (account), retailAccount, 5, TimeUnit.MINUTES);
        return null;
    }

    /**
     * 查询今天零售段数据
     *
     * @return
     */
    @Override
    public RetailAccount getAllRetailTodayInfo() {
        String startDate = DateUtil.parser (DateUtil.getDayBegin ());
        String endDate = DateUtil.parser (DateUtil.getDayEnd ());

        List<RetailAccount> retailAccounts = retailDao.selectByDay (null, startDate, endDate);
        if (null != retailAccounts && retailAccounts.size () > 0) {
            return retailAccounts.get (0);
        }
        return null;
    }
}
