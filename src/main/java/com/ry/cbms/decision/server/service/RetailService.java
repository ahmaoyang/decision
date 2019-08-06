package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.model.RetailAccount;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/31 16:59
 * @Description 零售端相关
 */
public interface RetailService {

    Map<String,Object> getRetailInfo(String account,String userId,String timeFlag,String startDate,String endDate);//获取零售端本期和上期的相关余额

    List<RetailAccount> getAllRetailInfo();

    RetailAccount getHisRetailInfo(String  account);

    RetailAccount getAllRetailTodayInfo();

}
