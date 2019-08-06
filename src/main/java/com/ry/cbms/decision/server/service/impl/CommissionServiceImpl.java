package com.ry.cbms.decision.server.service.impl;

import com.google.common.collect.Maps;
import com.ry.cbms.decision.server.dao.CommissionDao;
import com.ry.cbms.decision.server.vo.CommissionVo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.CommissionService;
import com.ry.cbms.decision.server.utils.ComUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author maoYang
 * @Date 2019/5/28 10:08
 * @Description 返佣服务
 */
@Service
public class CommissionServiceImpl implements CommissionService {

    @Autowired
    private CommissionDao commissionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ComUtil comUtil;

    /**
     * 根据用户账号查询出入金细则
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, List> getCommInOutDetails(String userId, String startDate, String endDate) {
        Map<String, List> map = Maps.newHashMap ();
        List commOuts = commissionDao.selectCommOutDetail (userId, startDate, endDate);//用户佣金出金细则
        List commToPrinciples = commissionDao.selectCommToPrincipleDetail (userId, startDate, endDate);//用户佣金转本金细则
        map.put ("commOuts", commOuts);
        map.put ("commToPrinciples", commToPrinciples);
        return map;
    }

    /**
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public CommissionVo getTodayCommissionInfo(String startDate, String endDate) {
        //全部账户和指定账户
        Object preBalance;//上期佣金余额
        BigDecimal sumCommHis = new BigDecimal (0.0); //历史生成佣金
        BigDecimal sumCommOutHis = new BigDecimal (0.0);//历史佣金出金
        BigDecimal sumCommToPrincipalHis = new BigDecimal (0.0);//历史佣金转本金
        CommissionVo labelCommissionVo = new CommissionVo ();
        BigDecimal sumCommOutFee = commissionDao.sumCommOutFeeByUserIdAndTime (null, startDate, endDate);//通道手续费
        preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCommPreBalance ());// 上期总的佣金余额
        BigDecimal sumCurrComm = commissionDao.sumCommByUserIdAndTime (null, startDate, endDate); //ib的
        BigDecimal sumCurrComm2 = commissionDao.sumStaffCommByUserIdAndTime (null, startDate, endDate);//员工的
        if (null == sumCurrComm) {
            sumCurrComm = new BigDecimal (0);
        }
        if (null == sumCurrComm2) {
            sumCurrComm2 = new BigDecimal (0);
        }
        sumCurrComm = sumCurrComm2.add (sumCurrComm);

        if (null == preBalance) {
            preBalance = 0;
        }
        if (null == sumCommOutFee) {
            sumCommOutFee = new BigDecimal (0);
        }
        if (null == sumCurrComm) {
            sumCurrComm = new BigDecimal (0);
        }
        labelCommissionVo.setChannelFee (sumCommOutFee.toString ());
        labelCommissionVo.setPreviousBalance (preBalance.toString ());
        labelCommissionVo.setCurrentBalance (sumCurrComm.toString ());
        sumCommInfo (null, labelCommissionVo, sumCommHis, sumCommOutHis, sumCommToPrincipalHis, startDate, endDate);
        return labelCommissionVo;
    }

    /**
     * 返佣信息查询
     *
     * @param account
     * @param userId
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public Map<String, Object> getCommissionInfos(String account, String userId, String startDate, String endDate, Integer offset, Integer limit) {
        Map<String, Object> resultMap = new ConcurrentHashMap<> ();
        List<CommissionVo> infoList = new ArrayList ();
        List<Map> commissions;
        CommissionVo labelCommissionVo = new CommissionVo ();
        Integer totalCount;
        Object preBalance;//上期佣金余额
        if (StringUtils.isEmpty (userId)) { //全部账户和指定账户
            if (StringUtils.isEmpty (account)) {  //全部账户
                preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCommPreBalance ());// 上期总的佣金余额
            } else {
                String uid = comUtil.getUserIdByAcc (account);
                preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCommPreBalance (uid));
            }
            if (null == preBalance) {
                preBalance = 0;
            }
            labelCommissionVo.setPreviousBalance (preBalance.toString ());
            String ids = comUtil.getUserIdByAcc (account);
            commissions = commissionDao.selectAllAccByUserId (ids, offset, limit);
            cycleBody (infoList, commissions, startDate, endDate, labelCommissionVo);
            totalCount = commissionDao.selectAllAccNumByUserId (ids);
        } else {                        //指定员工或代理下面账户信息
            String ids = comUtil.getUserIds (userId);
            commissions = commissionDao.selectAllAccByUserId (ids, offset, limit);
            cycleBody (infoList, commissions, startDate, endDate, labelCommissionVo);
            totalCount = commissionDao.selectAllAccNumByUserId (ids);
        }
        resultMap.put ("infoList", infoList);
        resultMap.put ("totalCount", totalCount == null ? 0 : totalCount);
        resultMap.put ("labelCommissionVo", labelCommissionVo);
        return resultMap;
    }


    private void cycleBody(List<CommissionVo> commissionVoLists, List<Map> comList, String startDate, String endDate, CommissionVo labelCommissionVo) {
        BigDecimal sumCommHis = new BigDecimal (0.0); //历史生成佣金
        BigDecimal sumCommOutHis = new BigDecimal (0.0);//历史佣金出金
        BigDecimal sumCommToPrincipalHis = new BigDecimal (0.0);//历史佣金转本金
        BigDecimal sumCurrComm = new BigDecimal (0.0);//本期佣金总
        for (Map map : comList) { //遍历钱包账户信息
            CommissionVo comm = new CommissionVo ();
            Object userIdObj = map.get ("userid");
            if (null != userIdObj) {
                sumCommInfo (userIdObj.toString (), labelCommissionVo, sumCommHis, sumCommOutHis, sumCommToPrincipalHis, null, null);
                comm.setUserId (userIdObj.toString ());
                BigDecimal sumComm1 = commissionDao.sumCommByUserIdAndTime (userIdObj.toString (), startDate, endDate);//获取用户生成的佣金
                BigDecimal sumComm2 = commissionDao.sumStaffCommByUserIdAndTime (userIdObj.toString (), startDate, endDate);//获取用户生成的佣金

                BigDecimal sumCommOut = commissionDao.sumCommOutByUserIdAndTime (userIdObj.toString (), startDate, endDate);//用户的佣金出金量
                BigDecimal sumCommOutFee = commissionDao.sumCommOutFeeByUserIdAndTime (userIdObj.toString (), startDate, endDate);//通道手续费
                BigDecimal sumCommToPrincipal = commissionDao.sumCommToPrincipalByUserIdAndTime (userIdObj.toString (), startDate, endDate);//佣金转本金
                if (null == sumComm1) {
                    sumComm1 = BigDecimal.valueOf (0);
                }
                if (null == sumComm2) {
                    sumComm2 = BigDecimal.valueOf (0);
                }
                BigDecimal sumComm = sumComm1.add (sumComm2);
                if (null == sumCommOut) {
                    sumCommOut = BigDecimal.valueOf (0);
                }
                if (null == sumCommOutFee) {
                    sumCommOutFee = BigDecimal.valueOf (0);
                }
                if (null == sumCommToPrincipal) {
                    sumCommToPrincipal = BigDecimal.valueOf (0);
                }
                comm.setGenCommission (sumComm.toString ());//佣金生成
                comm.setOutCommission (sumCommOut.toString ());//佣金出金
                comm.setChannelFee (sumCommOutFee.toString ());//通道手续费
                comm.setCommissionToPrinciple (sumCommToPrincipal.toString ());//
            }
            String userId = userIdObj.toString ();
            Object acctId = map.get ("acctid");
            if (null != acctId) {
                comm.setAccount (acctId.toString ());
            }
            Object registerTime = commissionDao.selectRegisterById (userId);
            if (null != registerTime) {
                comm.setRegisterTime (registerTime.toString ());//用户注册时间
            }
            Object preBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getCommPreBalance (userIdObj.toString ()));
            if (null == preBalance) {
                preBalance = 0;
            }
            comm.setPreviousBalance (preBalance.toString ());//上期余额
            Object currBalance = map.get ("allBalance");
            if (null == currBalance) {
                currBalance = 0;
            }
            comm.setCurrentBalance (currBalance.toString ());//本期佣金余额
            sumCurrComm.add (BigDecimal.valueOf (Double.valueOf (currBalance.toString ())));
            if (null != commissionVoLists) {
                commissionVoLists.add (comm);
            }
        }
        labelCommissionVo.setCurrentBalance (sumCurrComm.toString ());
    }

    /**
     * 计算全部账户和总的账户的佣金生成
     */
    private void sumCommInfo(String userId, CommissionVo labelCommissionVo, BigDecimal sumCommHis, BigDecimal sumCommOutHis, BigDecimal sumCommToPrincipalHis, String startDate, String endDate) {
        BigDecimal genComm = commissionDao.sumCommByUserIdAndTime (userId, startDate, endDate);//获取用户生成的佣金
        BigDecimal commOut = commissionDao.sumCommOutByUserIdAndTime (userId, startDate, endDate);//用户的佣金出金量
        BigDecimal commToPrincipal = commissionDao.sumCommToPrincipalByUserIdAndTime (userId, startDate, endDate);//佣金转本金
        BigDecimal sumCommActOutFee = commissionDao.sumCommOutActFeeByUserIdAndTime (userId, startDate, endDate);//通道实际手续费
        BigDecimal sumBrokerFee = commissionDao.sumCommOutBrokerFeeByUserIdAndTime (userId, startDate, endDate);//经纪商手续费
        if (null == genComm) {
            sumCommHis.add (new BigDecimal (0.0));
        }
        if (null != commOut) {
            sumCommOutHis.add (new BigDecimal (0.0));
        }
        if (null != commToPrincipal) {
            sumCommToPrincipalHis.add (new BigDecimal (0.0));
        }
        if (null == sumCommActOutFee) {
            sumCommActOutFee = BigDecimal.valueOf (0);
        }
        if (null == sumBrokerFee) {
            sumBrokerFee = BigDecimal.valueOf (0);
        }
        labelCommissionVo.setChannelActFee (sumCommActOutFee.toString ());//通道实际手续费
        labelCommissionVo.setBrokerFee (sumBrokerFee.toString ());
        labelCommissionVo.setOutCommission (sumCommOutHis.toString ());
        labelCommissionVo.setCommissionToPrinciple (sumCommToPrincipalHis.toString ());
        labelCommissionVo.setGenCommission (sumCommHis.toString ());
        labelCommissionVo.setBrokerFee (sumBrokerFee.toString ());

    }


}
