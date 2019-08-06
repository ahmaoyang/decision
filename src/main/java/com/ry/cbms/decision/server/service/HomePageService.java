package com.ry.cbms.decision.server.service;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/18 10:32
 * @Description 首页
 */
public interface HomePageService {

    Map<String, Object> getHomePageData(String currency, String flag);

    List<Map<String, Object>> geAccDataByFlag(String currency, String flag, String accType);

    List<Map<String, Object>> geAccDataByDate(String currency, String startDate,String endDate, String accType);

    List<Map<String, Object>> getTodayDetail(String currency, String startDate,String endDate, String accType); //获取今日账户明细


}
