package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.model.BusinessAccountInfo;


import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/12 16:11
 * @Description 经济商账户
 */
public interface BusinessAccountService {

    List getBrokerAccountInfos(String account, String userId, String flag, String startDate, String endDate, String currency);

    BusinessAccountInfo getBrokerAccountTypeDetails(String account, String userId, String startDate, String endDate, String currency);

    List<Map<String, Object>> getBrokerAccountBalance(String flag, String currency, String startDate, String endDate);//查询经纪商账户余额


    Map<String, BusinessAccountInfo> getAccDataByCondition(String account, String flag, String startDate, String endDate, String currency, String userId);


   BusinessAccountInfo  getBusinessAccInfoHis(String currency,String mt4Acc);

    BusinessAccountInfo  getBusinessAccInfoHisForUserId(String currency,String userId);


}
