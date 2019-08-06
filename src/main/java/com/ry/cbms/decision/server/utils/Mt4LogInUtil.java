package com.ry.cbms.decision.server.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.vo.Mt4Vo;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
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
 * @Date 2019/5/24 16:28
 * @Description MT4 账户登录工具(Terminus 登陆也在这里)
 */
@Slf4j
@Service
public class Mt4LogInUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Boolean logInMT4(Mt4Vo mt4Vo) {
        if (null == mt4Vo) {
            throw new GlobalException ("登陆参数不能为空！");
        }
        Map<String, Object> paramMap;
        String response;
        try {
            paramMap = ComUtil.objectToMap (mt4Vo);
        } catch (Exception e) {
            throw new GlobalException ("转换异常" + e);
        }

        try {
            response = HttpUtil.httpPostWithForm (Constants.MT4_SERVER_URL + "login", paramMap);
            JSONObject jsonObject = SafetyToJsonUtil.toJsonObject (response);
            if ("0".equals (jsonObject.get ("code"))) {
                String mt4Token = jsonObject.getString ("data");
                if (!StringUtils.isEmpty (mt4Token)) {
                    redisTemplate.opsForValue ().set (Constants.PREFIX + Constants.MT4_TOKEN, mt4Token);//将Mt4Token 保存到缓存
                }
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            log.error ("Mt4登陆出错{}", e);
            return Boolean.FALSE;
        }
    }

    /**
     * 涮新mt4心跳数据
     *
     * @return
     */
    public void refreshHeartBeat() {
        if (log.isInfoEnabled ()) {
            log.info ("开始刷新mt4心跳数据{}", DateUtil.parser (new Date ()));
        }
        Object tokenCache = redisTemplate.opsForValue ().get (Constants.PREFIX + Constants.MT4_TOKEN);
        if (null == tokenCache) {
            throw new GlobalException ("mt4token为空");
        }
        try {
            HttpUtil2.doGet (Constants.MT4_SERVER_URL + "/ping", tokenCache.toString (), null);
        } catch (Exception e) {
            if (log.isErrorEnabled ()) {
                log.error ("MT4心跳连接失败{}", e);
            }
        }
        if (log.isInfoEnabled ()) {
            log.info ("刷新mt4心跳数据结束{}", DateUtil.parser (new Date ()));
        }
    }

    public void terminusLogIn() {
        if (log.isInfoEnabled ()) {
            log.info ("开始登陆terminus{}", DateUtil.parser (new Date ()));
        }
        String loginUrl = Constants.TERMINUS_SERVER_URL + "api/login";
        JSONObject param = new JSONObject ();
        param.put ("password", "");
        param.put ("username", "");
        String response = HttpUtil.httpPostForString (loginUrl, param.toJSONString (), null);
        if (!StringUtils.isEmpty (response)) {
            JSONObject res = JSON.parseObject (response);
            if ("0".equals (res.get ("code"))) { //登录成功
                Object token = res.get ("data");
                if (null != token) {
                    redisTemplate.opsForValue ().set (RedisKeyGenerator.getTerminusToken (), token.toString ());//保存terminus 登录的token
                }
            }
        }
        if (log.isInfoEnabled ()) {
            log.info ("结束登陆terminus{}", DateUtil.parser (new Date ()));
        }
    }

    public void setRetailInfo(Boolean flag) {  //flag 为false涮新上期本金 ，true 刷新本期净值
        if (log.isInfoEnabled ()) {
            log.info ("开始更新零售端信息{}", DateUtil.parser (new Date ()));
        }
        String reqUrl = Constants.MT4_SERVER_URL + "user/users";
        Object mT4Token = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
        if (null == mT4Token) {
            if (log.isInfoEnabled ()) {
                log.info ("Mt4Token is null");
                throw new GlobalException ("Mt4Token is null");
            }
        }
        JSONObject response = HttpUtil2.doGet (reqUrl, mT4Token.toString (), null);
        if (null != response) {
            Double totalBalance = 0.0;//所有用户的零售端余额
            Double totalRetailNet = 0.0;//零售端总的净值
            if ("0".equals (response.getString ("code"))) { //请求成功
                JSONArray jsonArray = response.getJSONArray ("data");
                int arrLen = jsonArray.size ();
                for (int i = 0; i < arrLen; i++) {
                    JSONObject mt4AccInfo = jsonArray.getJSONObject (i);
                    String acc = mt4AccInfo.getString ("login");
                    Double balance = mt4AccInfo.getDouble ("balance");
                    totalBalance += balance;
                    if (Boolean.TRUE == flag) {
                        //下面计算零售端净值
                        totalRetailNet += dealRetailNet (acc, mT4Token.toString ());
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getRetailBalance (acc), balance);//每个人的本期零售端余额

                    } else {
                        redisTemplate.opsForValue ().set (RedisKeyGenerator.getPreRetailBalance (acc), balance);//每个人的上期零售端余额
                    }
                }
                if (Boolean.FALSE == flag) {
                    redisTemplate.opsForValue ().set (RedisKeyGenerator.getPreRetailBalance (), totalBalance);//所有用户的上期零售端余额
                } else {
                    redisTemplate.opsForValue ().set (RedisKeyGenerator.getRetailBalance (), totalBalance);//所有用户的本期零售端余额
                    redisTemplate.opsForValue ().set (RedisKeyGenerator.getCurEquity (), totalRetailNet);//所有用户的本期零售端净值
                }
            }
        }
        if (log.isInfoEnabled ()) {
            log.info ("跟新零售端信息完成{}", DateUtil.parser (new Date ()));
        }

    }

    /**
     * 计算零售端净值和余额
     *
     * @param account
     * @param mt4Token
     */
    public Double dealRetailNet(String account, String mt4Token) {
        String url = Constants.MT4_SERVER_URL + "user/marginLevelManager";
        List<NameValuePair> nameValuePairList = new ArrayList<> ();
        BasicNameValuePair param1 = new BasicNameValuePair ("login", account);
        nameValuePairList.add (param1);
        JSONObject resObj = HttpUtil2.doGet (url, mt4Token, nameValuePairList);
        Double equity = resObj.getDouble ("equity ");
        if (null == equity) {
            equity = 0.0;
        }
        redisTemplate.opsForValue ().set (RedisKeyGenerator.getCurEquity (account), equity);//本期零售端净值
        return equity;
    }
}
