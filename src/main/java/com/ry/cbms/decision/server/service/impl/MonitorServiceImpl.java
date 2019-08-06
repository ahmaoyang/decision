package com.ry.cbms.decision.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.dto.*;
import com.ry.cbms.decision.server.dao.MonitorDao;
import com.ry.cbms.decision.server.service.MonitorService;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.HttpUtil2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName MonitorServiceImpl
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/5 17:56
 * @Version 1.0
 **/
@Service
@Slf4j
public class MonitorServiceImpl implements MonitorService {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    MonitorDao monitorDao;


    /**
     * @return java.util.List<org.apache.poi.ss.formula.functions.T>
     * @Author XTH
     * @Description //TODO 获取所有的交易品种
     * @Date 2019/7/5 18:00
     * @Param []
     **/
    @Override
    public List<String> getAllVal() {
        try {
            Object terToken = redisTemplate.opsForValue ().get (Constants.PREFIX + Constants.MT4_TOKEN);
            if (null == terToken) {
                throw new GlobalException ("MT4 token为空");
            } else {
                //通过小龙接口获取所有交易品种
                JSONObject response = HttpUtil2.doGet (Constants.MT4_SERVER_URL+"user/symbolList", terToken.toString (), null);
                //log.info("获取到的所有品种响应参数为：" + response);
                List<String> resultList = new ArrayList<> ();
                String allValStr = response.toJSONString ();
                AllVal allVal = JSON.parseObject (allValStr, AllVal.class);
                if ("0".equals (allVal.getCode ())) {
                    List<AllValData> dataList = allVal.getData ();
                    for (int i = 0; i < dataList.size (); i++) {
                        resultList.add (dataList.get (i).getSymbol ());
                        //log.info(dataList.get(i).getSymbol());
                    }
                    return resultList;
                } else {
                    throw new GlobalException ("MT4接口获取参数失败");
                }
            }
        } catch (GlobalException e) {
            log.info (e.getMessage ());
        }
        return new ArrayList<> ();
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @Author XTH
     * @Description //TODO 获取所有品种对应的实时价格
     * @Date 2019/7/5 18:54
     * @Param [symbolList]
     **/
    @Override
    public Map<String, Object> getValPrice(List<String> symbolList, int pageNum, int pageSize) {
        // monitorService.getSinglePrice(symbolList);
        try {
            Page<SimpleValData> pages = PageHelper.startPage (pageNum, pageSize);//默认展示第一页的7行数据
            List<SimpleValData> valsPrice = monitorDao.getValsPrice (symbolList);
            log.info ("从库中查到的品种价格集合为：" + valsPrice);
            log.info ("总页数" + pages.getPageSize ());
            log.info ("总条数" + pages.getTotal ());
            Map<String, Object> resultMap = new HashMap<> ();
            resultMap.put ("valsPrice", valsPrice);
            Pages pageInfo = new Pages ();
            pageInfo.setTotalAmount (String.valueOf (pages.getTotal ()));
            pageInfo.setTotalPage (String.valueOf (pages.getPages ()));
            resultMap.put ("pageInfo", pageInfo);
            return resultMap;
        } catch (Exception e) {
            log.error (e.getMessage ());
        }
        return new HashMap<> ();
    }

    @Override
    public Map<String, Object> getSingleVal(String symbol) {
        try {
            Object terToken = redisTemplate.opsForValue ().get (Constants.PREFIX + Constants.MT4_TOKEN);
            List<CharData> singlePrice = monitorDao.getSinglePrice (symbol);
            List<String> slCount = new ArrayList<> ();
            //log.info("交易品种" + symbol + "从库中查到近6小时数据为：" + singlePrice);
            if (null == terToken) {
                throw new GlobalException ("MT4 token为空");
            } else {
                //通过MT4接口获取交易品种止损价的持仓订单数量
                JSONObject response = HttpUtil2.doGet (Constants.MT4_SERVER_URL+"user/openList", terToken.toString (), null);
                String responseStr = response.toJSONString ();
                Trade trade = JSON.parseObject (responseStr, Trade.class);
                if ("0".equals (trade.getCode ())) {
                    List<TradeCount> dataList = trade.getData ();
                    for (int i = 0; i < dataList.size (); i++) {
                        if (symbol.equals (dataList.get (i).getSymbol ())) {
                            dataList.get (i).getSl ();//当前品种的止损价格
                            slCount.add (String.valueOf (dataList.get (i).getSl ()));
                        }
                    }
                }
                Map<String, Object> resultMap = new HashMap<> ();
                resultMap.put ("price", singlePrice);
                resultMap.put ("trade", slCount);
                return resultMap;
            }
        } catch (GlobalException e) {
            log.error (e.getMessage ());
        }
        return null;
    }

    @Override
    public String accordPush(String type) {
        Map<String, Object> singleVal = getSingleVal (type);
        //System.out.println("接受到的品种类型为："+type);
        List<CharData> valPrice = (List) singleVal.get ("price");
        SinglePriceData singlePriceData = new SinglePriceData ();
        singlePriceData.setType ("singlePrice");
        singlePriceData.setSinglePrice (valPrice);
        Result<SinglePriceData> success1 = Result.success (singlePriceData);
        String result = JSON.toJSONString (success1);
        if (valPrice.isEmpty () || valPrice == null) {
            return "";
        } else {
            return result;
        }
    }

    /**
     * @return java.lang.String
     * @Author XTH
     * @Description //TODO
     * @Date 2019/7/15 13:59
     * @Param [type]
     **/
    @Override
    public String accordPushTrade(String type) {
        try {
            Map<String, Integer> tradeMap = new HashMap (16);
            tradeMap.put ("11.23", 3);
            tradeMap.put ("100.33", 3);
            List<TradeAmount> tradeList = new ArrayList<> ();
            Map<String, Object> singleVal = getSingleVal (type);
            List<String> tradeCount = (List) singleVal.get ("trade");
            for (String price : tradeCount) {
                Integer i = 1;
                if (tradeMap.get (price) != null) {
                    i = tradeMap.get (price) + 1;
                }
                if (!"0".equals (price)) {
                    tradeMap.put (price, i);
                }
            }
            Iterator<Map.Entry<String, Integer>> iterator = tradeMap.entrySet ().iterator ();
            while (iterator.hasNext ()) {
                Map.Entry<String, Integer> next = iterator.next ();
                TradeAmount tradeAmount = new TradeAmount ();
                tradeAmount.setCount (next.getValue ().toString ());
                tradeAmount.setPrice (next.getKey ());
                tradeList.add (tradeAmount);
            }
            String result = "";
            if (tradeMap.size () > 0) {
                TradeCountData tradeCountData = new TradeCountData ();
                tradeCountData.setTradeAmountList (tradeList);
                tradeCountData.setType ("tradeCount");
                Result<TradeCountData> success = Result.success (tradeCountData);
                result = JSON.toJSONString (success);//要主动推送的数据2
                log.info ("定时发送交易品种价格交易单数的推送完成");
            }
            return result;
        } catch (Exception e) {
            log.error (e.getMessage ());
        }
        return "";
    }


}
