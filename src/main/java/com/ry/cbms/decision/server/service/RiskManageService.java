package com.ry.cbms.decision.server.service;

import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.model.OrderEvaluationInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/4 17:43
 * @Description 风控管理
 */
public interface RiskManageService {

    Map<String, Object> accountEva(Integer offset, Integer limit, String flag); //账户评估

    JSONObject orderEva(String account, String userId, Integer offset, Integer limit); //订单评估

    List<OrderEvaluationInfo> getVarietyData(String variety, String orderType, String flag, String account, String  beginDate, String  overDate);

    List getByVariety(String account,String variety);


}
