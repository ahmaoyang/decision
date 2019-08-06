package com.ry.cbms.decision.server.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.dao.OrderInfoDao;
import com.ry.cbms.decision.server.model.ThrowInfo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * @Author maoYang
 * @Date 2019/7/4 13:15
 * @Description 抛单工具类
 */
@Slf4j
@Component
public class ThrowOrderUtil {

    @Autowired
    private OrderInfoDao orderInfoDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 加载抛单信息到本地
     *
     * @param login
     * @return
     */
    public List<ThrowInfo> loadThrowInfos(String login) {
        Date currDate = new Date ();
        if (log.isInfoEnabled ()) {
            log.info ("开始更新抛单信息{}", currDate);
        }
        List<ThrowInfo> throwInfoList = new LinkedList<> ();
        Long from = DateUtil.getBeginDayOfYesterday ().getTime () / 1000;//昨天开始时间
        Long to = DateUtil.getEndDayOfYesterDay ().getTime () / 1000;//昨天结束时间
        String reqUrl = Constants.MT4_SERVER_URL + "user/tradesUserHistory";
        BasicNameValuePair param1 = new BasicNameValuePair ("login", login);//登陆账号
        BasicNameValuePair param2 = new BasicNameValuePair ("from", from.toString ());//开始时间
        BasicNameValuePair param3 = new BasicNameValuePair ("to", to.toString ());//结束时间
        List<NameValuePair> nameValuePairList = new ArrayList<> ();
        nameValuePairList.add (param1);
        nameValuePairList.add (param2);
        nameValuePairList.add (param3);
        Object mt4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
        if (null == mt4Token) {
            return null;
        }
        JSONObject res;  //返回体
        res = HttpUtil2.doGet (reqUrl, mt4Token.toString (), nameValuePairList);
        if ("0".equals (res.getString ("code"))) { //请求成功
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getGetThrowFlag (DateUtil.getyyyyMMdd ()), true);
            JSONArray dataArr = res.getJSONArray ("data");
            if (null != dataArr && dataArr.size () > 0) {
                for (int i = 0, len = dataArr.size (); i < len; i++) {
                    ThrowInfo throwInfo = new ThrowInfo ();//抛单信息
                    initData (throwInfo);
                    JSONObject object = dataArr.getJSONObject (i);
                    dealThrowInfo (throwInfo, object, reqUrl, mt4Token.toString (), nameValuePairList, login);//抛单信息处理
                    calNetProfit (throwInfo);//计算净利润
                    throwInfoList.add (throwInfo);
                }
            }
        }
        return throwInfoList;
    }

    /**
     * 抛单数据初始化
     */
    private void initData(ThrowInfo throwInfo) {
        BigDecimal defaultData = new BigDecimal (0.0);
        throwInfo.setNetProfit (defaultData);
        throwInfo.setAmount (defaultData);
        throwInfo.setRiskThrowProfit (defaultData);
        throwInfo.setNotThrowProfit (defaultData);
        throwInfo.setRetailNotCloseProfit (defaultData);
        throwInfo.setThrowProfit (defaultData);
        throwInfo.setRetailNotCloseSettle (defaultData);
    }

    /**
     * 抛单信息处理
     *
     * @param throwInfo
     * @param object
     */
    private void dealThrowInfo(ThrowInfo throwInfo, JSONObject object, String reqUrl, String mt4Token, List<NameValuePair> nameValuePairList, String login) {
        String comment = object.getString ("comment");//备注里是账号和订单号
        Object clearAccOrderId = object.get ("order");
        if (!StringUtils.isEmpty (comment) && comment.contains ("_")) {
            Double profit = object.getDouble ("profit");//账号抛单利润
            String state = object.getString ("state");//状态
            Integer volume=object.getInteger ("volume "); //交易量
            Double orderProfit;//对应订单的利润
            Double commision;//原订单的commision
            int index = comment.indexOf ("_");
            String account = comment.substring (1, index);//获取订单的账号
            String orderNum = comment.substring (index + 1);//原订单号
            BasicNameValuePair param1 = new BasicNameValuePair ("login", account);//登陆账号
            nameValuePairList.add (param1);
            JSONObject res = HttpUtil2.doGet (reqUrl, mt4Token, nameValuePairList);//查询每个账号的信息
            if ("0".equals (res.getString ("code"))) { //请求成功
                JSONArray dataArr = res.getJSONArray ("data");
                if (null != dataArr && dataArr.size () > 0) {
                    JSONObject orderObj;
                    for (int j = 0, len = dataArr.size (); j < len; j++) {
                        orderObj = dataArr.getJSONObject (j);
                        Object order = orderObj.get ("order");//清算账户订单号
                        if (null != order) {
                            orderProfit = orderObj.getDouble ("profit");
                            commision = orderObj.getDouble ("commission"); //手续费
                            if (null == commision) {
                                commision = 0.0;
                            }
                            throwInfo.setCommission (new BigDecimal (commision));
                            throwInfo.setAccount (account);//抛单账号
                            throwInfo.setOrderId (orderNum);//抛单订单号
                            throwInfo.setClearAcc (login);
                            throwInfo.setVolume (volume);//抛单交易量
                            if (null != orderObj.getLong ("closeTime")) {
                                Date createTime = DateUtil.parse (DateUtil.timeStamp2Date (orderObj.getLong ("closeTime") * 1000, null));
                                throwInfo.setCreateTime (createTime);//发生时间
                            }
                            if (orderNum.equals (order.toString ())) { //抛单
                                Boolean flag = dealRiskThrowInfo (throwInfo, mt4Token, nameValuePairList, account, profit, orderProfit, commision, state);
                                if (!flag) {
                                    throwInfo.setThrowProfit (new BigDecimal (profit - orderProfit + commision)); //抛单盈亏
                                }
                            } else {   //非抛单
                                throwInfo.setNotThrowProfit (new BigDecimal (commision - orderProfit)); //非抛单盈亏
                            }
                            BigDecimal amount = throwInfo.getRiskThrowProfit ().add (throwInfo.getNotThrowProfit ()).add (throwInfo.getThrowProfit ());
                            throwInfo.setAmount (amount);//总额
                            if (null != clearAccOrderId) {
                                throwInfo.setClearAccOrderId (clearAccOrderId.toString ()); //清算账户订单号
                            }
                        }
                    }
                }

            }

        }

    }

    /**
     * 风控抛单信息处理
     *
     * @param throwInfo
     * @param mt4Token
     * @param nameValuePairList
     */
    private Boolean dealRiskThrowInfo(ThrowInfo throwInfo, String mt4Token, List<NameValuePair> nameValuePairList, String account, Double profit, Double orderProfit, Double commision, String state) {
        Boolean flag = false;
        String requestUrl = Constants.MT4_SERVER_URL + "/user/userRecordsRequest"; //指定用户数据请求
        BasicNameValuePair param = new BasicNameValuePair ("logins", account);//登陆账号
        nameValuePairList.clear ();
        nameValuePairList.add (param);
        JSONObject response = HttpUtil2.doGet (requestUrl, mt4Token, nameValuePairList);//获取账户详细信息
        if ("0".equals (response.getString ("code"))) { //请求成功
            JSONArray array = response.getJSONArray ("data");
            if (null != array && array.size () > 0) {
                JSONObject jsonObject = array.getJSONObject (0);
                if (null != jsonObject && Constants.RISKGROUP.equals (jsonObject.getString ("group"))) { // 说明风控订单
                    throwInfo.setRiskThrowProfit (new BigDecimal (profit - orderProfit + commision));//风控抛单盈亏
                    flag = true;
                } else {
                    Boolean isOk = dealRetailUnClose (jsonObject, state);
                    if (!isOk) {
                        throwInfo.setRetailNotCloseSettle (new BigDecimal (profit));//零售端未平仓抛单结算
                        throwInfo.setRetailNotCloseProfit (new BigDecimal (profit - orderProfit));//零售端未平仓抛单盈亏
                    }

                }
            }
        }
        return flag;
    }

    /**
     * 零售未平仓处理
     */
    private Boolean dealRetailUnClose(JSONObject jsonObject, String state) {
        String stateInfo = jsonObject.getString ("state");//客户定单平仓状态
        Boolean flag = false;
        if ((Constants.CLOSE_STATE.equals (state) || Constants.CLOSE_STATE_PART.equals (state))) { //客户经理平仓
            if (null != jsonObject && !(Constants.CLOSE_STATE.equals (stateInfo) || Constants.CLOSE_STATE_PART.equals (stateInfo))) { //客户订单未平仓
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 计算净利润
     */
    private void calNetProfit(ThrowInfo throwInfo) {
        if (!(null == throwInfo || null == throwInfo.getOrderId ())) {
            String orderId = throwInfo.getOrderId ();//抛单订单号
            BigDecimal comm = orderInfoDao.getThrowCommByOrderId (orderId);//抛单返佣金额
            if (null == comm) {
                comm = new BigDecimal (0.0);
            }
            throwInfo.setNetProfit (throwInfo.getAmount ().subtract (comm));//净利润
        }
    }
}
