package com.ry.cbms.decision.server.service.impl;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.dao.BondInfoDao;
import com.ry.cbms.decision.server.dao.CashInAndOutDao;
import com.ry.cbms.decision.server.dao.CommissionDao;
import com.ry.cbms.decision.server.dao.RetailDao;
import com.ry.cbms.decision.server.model.BondInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.service.BusinessAccountService;
import com.ry.cbms.decision.server.service.HomePageService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author maoYang
 * @Date 2019/6/18 10:35
 * @Description 首页相关实现类
 */
@Service
public class HomePageServiceImpl implements HomePageService {

    @Autowired
    private BusinessAccountService businessAccountService;

    @Autowired
    private ComUtil comUtil;
    @Autowired
    private CommissionDao commissionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CashInAndOutDao cashInAndOutDao;

    @Autowired
    private RetailDao retailDao;

    @Autowired
    private BondInfoDao bondInfoDao;


    @Override
    public List<Map<String, Object>> getTodayDetail(String currency, String startDate, String endDate, String accType) {
        List<Map<String, Object>> resultList;
        switch (accType) {
            case "accBroker": //经纪商账户
                resultList = businessAccountService.getBrokerAccountBalance (null, currency, startDate, endDate);//账户余额和展示数据
                break;
            case "accBond":     //保证金账户
                resultList = bondInfoDao.getBondByDay (startDate, endDate);
                break;
            case "accReturn":   //返佣账户
                resultList = commissionDao.selectCommByDay (startDate, endDate);
                break;
            case "accRetail":   //零售端账户
                resultList = retailDao.selectByDayUnit (startDate, endDate);
                resultList.forEach (it->{
                    if(null==it.get ("balance")){
                        it.put ("balance",0) ;
                    }
                });
                break;
            default:
                resultList = null;
        }
        return resultList;
    }
    /**
     * 获取首页数据
     *
     * @param currency 币种类型
     * @param flag     查询颗粒度标示
     * @return
     */
    @Override
    public Map<String, Object> getHomePageData(String currency, String flag) {
        Object homePageDataCache = redisTemplate.opsForValue ().get (RedisKeyGenerator.getHomePageData (currency, flag));
        Map<String, Object> resultMap = new HashMap<> ();
        if (null != homePageDataCache) {
            try {
                resultMap = (HashMap) homePageDataCache;
                return resultMap;
            } catch (Exception e) {
                throw new GlobalException ("getHomePageData转换成失败");
            }
        }
        Map<String, Object> bondMap = comUtil.getBondBalance ();
        Object riskBalance = bondMap.get ("riskBalance");
        Object swapBalance = bondMap.get ("swapBalance");
        if (null == swapBalance) {
            swapBalance = 0;
        }
        if (null == riskBalance) {
            riskBalance = 0;
        }
        resultMap.put ("bondBalance", new BigDecimal (riskBalance.toString ()).add (new BigDecimal (swapBalance.toString ())));//保证金余额
        BigDecimal sumComm = commissionDao.sumComm2 ();//总的佣金
        resultMap.put ("commBalance", sumComm);
        Object currRetailBalance = redisTemplate.opsForValue ().get (RedisKeyGenerator.getRetailBalance ());//零售端余额
        resultMap.put ("retailBalance", currRetailBalance);
        BigDecimal cashIn = cashInAndOutDao.getChannelCashInSum (currency);
        if (null == cashIn) {
            cashIn = new BigDecimal (0.0);
        }
        BigDecimal cashOut = cashInAndOutDao.getChannelCashOutSum (currency);
        if (null == cashOut) {
            cashOut = new BigDecimal (0.0);
        }
        resultMap.put ("businessBalance", cashIn.subtract (cashOut));//经纪商账户余额
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getHomePageData (currency, flag), resultMap, 10, TimeUnit.MINUTES);
        return resultMap;
    }


    /**
     * 查询账户展示数据细则
     *
     * @param currency 币种
     * @param flag     查询的颗粒度 （week,month）
     * @param accType  账户类别
     * @return
     */
    @Override
    public List<Map<String, Object>> geAccDataByFlag(String currency, String flag, String accType) {
        Object cacheList = redisTemplate.opsForValue ().get (RedisKeyGenerator.getHomePageDetails (currency, flag, accType));
        List<Map<String, Object>> resultList;
        if (null != cacheList) {
            resultList = (List<Map<String, Object>>) cacheList;
            return resultList;
        }
        if (Constants.ACC_BROKER.equals (accType)) {   //经济商账户
            resultList = businessAccountService.getBrokerAccountBalance (flag, currency, null, null);//账户余额和展示数据
        } else {
            Date currDate = DateUtil.getDayEnd ();//今天的日期时间
            String startDate = null;
            String endDate = null;
            if (Constants.FLAG_WEEK.equals (flag)) {
                startDate = DateUtil.parser (DateUtil.addOrReduceDay (currDate, -7));
                endDate = DateUtil.parser (currDate);
            }
            if (Constants.FLAG_MONTH.equals (flag)) {
                startDate = DateUtil.parser (DateUtil.addOrReduceDay (currDate, -30));
                endDate = DateUtil.parser (currDate);
            }
            switch (accType) {
                case "accBond":     //保证金账户
                    resultList = bondInfoDao.getByDay (startDate, endDate);
                    break;
                case "accReturn":   //返佣账户
                    resultList = commissionDao.selectCommByDay (startDate, endDate);
                    break;
                case "accRetail":   //零售端账户
                    resultList = retailDao.selectByDayUnit (startDate, endDate);
                    break;
                default:
                    throw new GlobalException ("请传入有效参数");
            }

        }
        if (null != resultList && resultList.size () > 0) {
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getHomePageDetails (currency, flag, accType), resultList, 5, TimeUnit.MINUTES);
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> geAccDataByDate(String currency, String startDate, String endDate, String accType) {
        List<Map<String, Object>> resultList ;
        resultList = switchFunction (accType, startDate, endDate, currency);
        resultList.sort ((a, b) -> {        //按日期时间降序
            Object timeA = a.get ("reqTime");
            Object timeB = b.get ("reqTime");
            if (!StringUtils.isEmpty (timeA) && !StringUtils.isEmpty (timeB)) {
                if (DateUtil.parse (timeB.toString ()).after (DateUtil.parse (timeA.toString ()))) {
                    return -1;
                } else if (DateUtil.parse (timeB.toString ()).after (DateUtil.parse (timeA.toString ()))) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        });
        return resultList;
    }

    private List switchFunction(String accType, String startDate, String endDate, String currency) {
        List<Map<String, Object>> resultList=new ArrayList<> ();
        if(StringUtils.isEmpty (startDate)){
            startDate="2018-07-01";
        }
        if(StringUtils.isEmpty (endDate)){
            endDate=DateUtil.getyyyyMMdd ();
        }
        switch (accType) {
            case "accBroker": //经纪商账户
                List<String> list = DateUtil.getDays (startDate, endDate);
                for(int i=0,len=list.size ();i<len;i++){
                    String it= list.get (i);
                    Map<String, Object> retMap=new HashMap<> ();
                    Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getBrokerBalanceRecord (currency), it);
                    if(null==cacheValue){
                        cacheValue=0;
                    }
                    retMap.put ("balance",cacheValue);
                    retMap.put ("reqTime",it);
                    resultList.add (retMap);
                }
                //resultList = businessAccountService.getBrokerAccountBalance (null, currency, startDate, endDate);//账户余额和展示数据
                break;
            case "accBond":     //保证金账户
                resultList = bondInfoDao.getBondByDay (startDate, endDate);
                break;
            case "accReturn":   //返佣账户
                resultList = commissionDao.selectCommByDay (startDate, endDate);
                break;
            case "accRetail":   //零售端账户
                resultList = retailDao.selectByDayUnit (startDate, endDate);
                resultList.forEach (it->{
                    if(null==it.get ("balance")){
                        it.put ("balance",0) ;
                    }
                });
                break;
            default:
                resultList = null;
        }
        return resultList;
    }
}
