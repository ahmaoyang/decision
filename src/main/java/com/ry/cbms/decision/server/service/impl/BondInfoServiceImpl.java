package com.ry.cbms.decision.server.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.dao.BondInfoDao;
import com.ry.cbms.decision.server.dao.ClearAccountInfoDao;
import com.ry.cbms.decision.server.model.BondInfo;
import com.ry.cbms.decision.server.model.ClearAccountInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.BondInfoService;
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

import java.util.*;

/**
 * @Author maoYang
 * @Date 2019/5/30 18:52
 * @Description 保证金服务层
 */
@Service
@Slf4j
public class BondInfoServiceImpl implements BondInfoService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ComUtil comUtil;

    @Autowired
    private BondInfoDao bondInfoDao;
    @Autowired
    private ClearAccountInfoDao clearAccountInfoDao;

    @Override
    public BondInfo getBondInfoByCreateTime(String createTime) {
        BondInfo bondInfo = null;
        try {
            bondInfo = bondInfoDao.getByCreateTime (createTime);
        } catch (Exception e) {
            log.error ("{}", e);
        }
        return bondInfo;
    }

    /**
     * 保证金账户展示 上期 本期的余额和净值
     *
     * @return
     */
    @Override
    public Map<String, Object> getBondInfo() {
        Map<String, Object> resultMap = Maps.newHashMap ();//返回的体
//        Object riskPreBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTermimusPreBalance (Constants.TERMIMUS_LOG1));// risk组上期保证金余额
//        Object swapPreBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTermimusPreBalance (Constants.TERMIMUS_LOG2));//swap组的上期保证余额
//        if (null == riskPreBalance) {
//            riskPreBalance = 0;
//        }
//        if (null == swapPreBalance) {
//            swapPreBalance = 0;
//        }
//        resultMap.put (Constants.TERMIMUS_LOG1 + "pre", riskPreBalance);//Hej001上期保证金余额
//        resultMap.put (Constants.TERMIMUS_LOG2 + "pre", swapPreBalance);//Hej411上期保证金余额
        Map<String, Object> bondMap;
        bondMap = comUtil.getForBondBalance ();//获取本期余额
        Object hej411Balance = bondMap.get ("HEJ411");
        Object hej411pre = resultMap.get ("HEJ411pre");
        Object hej001pre = resultMap.get ("HEJ001pre");
        Object hej00Balance = bondMap.get ("HEJ001");
        bondMap.put ("totalPre", String.valueOf (setNullZero (hej411pre) + setNullZero (hej001pre)));
        bondMap.put ("total", String.valueOf (setNullZero (hej411Balance) + setNullZero (hej00Balance)));
        resultMap.putAll (bondMap);
        return resultMap;
    }

    private Double setNullZero(Object obj) {
        if (StringUtils.isEmpty (obj)) {
            return 0.0;
        } else {
            return Double.valueOf (obj.toString ());
        }
    }

    /**
     * 按天，按月查看保证金账户相关信息
     *
     * @param flag      ：day,month 按天，按月
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<BondInfo> getBonAccData(String flag, String startDate, String endDate) {
        List bondInfoList;
        switch (flag) {
            case "day":
                log.info ("开始按天保证金账户查询");
                bondInfoList = bondInfoDao.getByDay (startDate, endDate);
                break;
            case "month":
                log.info ("开始按月保证金账户查询");
                endDate = DateUtil.parserTo ((DateUtil.lastMonthDay (DateUtil.stringParserToDate (endDate))));
                bondInfoList = bondInfoDao.getByMonth (startDate, endDate);
                break;
            default:
                throw new GlobalException ("请输入有效的查询单元");
        }
        return bondInfoList;
    }
    /**
     * 调用terminus 获取保证接口
     *
     * @param dateTime(yyyy-MM-dd) 这种格式
     * @return
     */
    public void loadRemoteBondInfo(String dateTime) {
        String terminusToken = comUtil.getTerminusToken ();
        if (null == terminusToken) {
            throw new GlobalException ("terminus token 为空");
        }
        String url = Constants.TERMINUS_SERVER_URL + "api/findHistoricCashMovements";
        List<NameValuePair> nameValuePairList = new ArrayList<> ();
        BasicNameValuePair param = new BasicNameValuePair ("date", dateTime);
        nameValuePairList.add (param);
        JSONObject resObj = HttpUtil2.doGet (url, terminusToken, nameValuePairList);
        if (null != resObj) {
            String code = resObj.getString ("code");
            if ("0".equals (code)) { //请求成功
                JSONArray dataArr = resObj.getJSONArray ("data");
                if (null != dataArr && dataArr.size () > 0) {
                    List<BondInfo> bondInfos = new ArrayList<> ();
                    BondInfo bondInfo1 = new BondInfo ();
                    Double cashIn = 0.0;
                    Double cashOut = 0.0;
                    for (int i = 0, len = dataArr.size (); i < len; i++) {
                        JSONObject dataObj = dataArr.getJSONObject (i);
                        String type = dataObj.getString ("type");
                        Double quality = dataObj.getDoubleValue ("quantity");
                        if (null != type) {
                            if (Constants.terminusDeposit.equalsIgnoreCase (type)) { //入金
                                cashIn += quality;
                            }
                            if (Constants.terminusWithdrawal.equalsIgnoreCase (type)) { //出金
                                cashOut += quality;
                            }
                            if (Constants.terminusTransFor.equals (type)) { //转账
                                if (Double.valueOf (quality) > 0) { //入金
                                    cashIn += quality;
                                }
                                if (Double.valueOf (quality) > 0) { //出金
                                    cashOut += quality;
                                }
                            }
                        }
                    }
                    cashOut = Math.abs (cashOut);
                    bondInfo1.setCashIn (cashIn.toString ());
                    bondInfo1.setCashOut (cashOut.toString ());
                    bondInfo1.setBridgeFee ("0");
                    bondInfo1.setLiquidityTransactionFee ("0");
                    bondInfo1.setOthers ("0");
                    bondInfo1.setClosedProfitAndLoss ("0");
                    if (null != dateTime) {
                        bondInfo1.setCreateTime (dateTime); //日期
                    }
                    BondInfo bondInfo = bondInfoDao.getByCreateDate (dateTime);
                    if (null != bondInfo) {
                        bondInfo.setCashIn (bondInfo1.getCashIn ());
                        bondInfo.setCashOut (bondInfo1.getCashOut ());
                        bondInfoDao.updateBondInfo (bondInfo);
                    } else {
                        bondInfos.add (bondInfo1);
                    }
                    if (null != bondInfos && bondInfos.size () > 0) {
                        bondInfoDao.save (bondInfos);
                    }
                }
                if (null == dataArr && dataArr.size () == 0) {
                    BondInfo bondInfo2 = bondInfoDao.getByCreateDate (dateTime);
                    if (null == bondInfo2) {
                        BondInfo bondInfo = new BondInfo ();
                        List<BondInfo> bondInfos = new ArrayList<> ();
                        bondInfo.setCashIn ("0");
                        bondInfo.setCashOut ("0");
                        bondInfo.setCreateTime (dateTime);
                        bondInfo.setBridgeFee ("0");
                        bondInfo.setLiquidityTransactionFee ("0");
                        bondInfo.setOthers ("0");
                        bondInfo.setClosedProfitAndLoss ("0");
                        bondInfos.add (bondInfo);
                        bondInfoDao.save (bondInfos);
                    }
                }
            }
        }
    }

    /**
     * 保存保证金
     *
     * @param bondInfo
     */
    @Override
    public void updateBondInfo(BondInfo bondInfo) {
        if (null != bondInfo) {
            try {
                bondInfoDao.updateBondInfo (bondInfo);
            } catch (Exception e) {
                throw new GlobalException (e.toString ());
            }
        }
    }

    @Override
    public void save(List bondInfos) {
        if (null != bondInfos) {
            try {
                bondInfoDao.save (bondInfos);
            } catch (Exception e) {
                throw new GlobalException (e.toString ());
            }
        }
    }

    @Override
    public void delete(String todayDate) {

    }

    @Override
    public void saveClearAccInfo(ClearAccountInfo info) {
        clearAccountInfoDao.save (info);
    }


    @Override
    public void updateClearAccInfo(ClearAccountInfo info) {
        info.setUpdateTime (DateUtil.getyyyyMMdd ());
        clearAccountInfoDao.update (info);
    }

    @Override
    public ClearAccountInfo getClearAccountInfoByCreateTime(String createTime) {
        ClearAccountInfo clearAccountInfo = clearAccountInfoDao.getClearAccountInfoByCreateTime (createTime);
        return clearAccountInfo;
    }

    @Override
    public List getClearAccData(String flag, String startDate, String endDate) {
        List<ClearAccountInfo> clearInfoList;
        switch (flag) {
            case "day":
                log.info ("开始按天清算账户查询");
                clearInfoList = clearAccountInfoDao.getByDay (startDate, endDate);
                break;
            case "month":
                log.info ("开始按月清算账户查询");
                clearInfoList = clearAccountInfoDao.getByMonth (startDate, endDate);
                break;
            default:
                throw new GlobalException ("请输入有效的查询单元");
        }
        return clearInfoList;
    }

    /**
     * 保证金历史累计
     *
     * @return
     */
    @Override
    public BondInfo getHisBondInfo() {
        BondInfo bondInfo = bondInfoDao.getHisBondInfo ();
        if (null != bondInfo) {
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getBondInfoHis (), bondInfo);
        }
        return null;
    }

    @Override
    public ClearAccountInfo getHisClearAccountInfo() {
        ClearAccountInfo clearAccountInfo = clearAccountInfoDao.getHisClearAccountInfo ();
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getClearAccountInfoHis (), clearAccountInfo);
        return null;
    }

    @Override
    public BondInfo getDayUnitInfo(String startDate, String endDate) {
        return bondInfoDao.getDayUnitInfo (startDate, endDate);
    }
}
