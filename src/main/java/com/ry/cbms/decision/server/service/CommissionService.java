package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.vo.CommissionVo;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/28 10:06
 * @Description 返佣信息相关
 */
public interface CommissionService {

    Map<String, Object> getCommissionInfos(String account, String userId, String startDate, String endDate, Integer offset, Integer limit);

    Map<String, List> getCommInOutDetails(String userId, String startDate, String endDate);//根据用户账号查询出入金细则


    CommissionVo getTodayCommissionInfo(String startDate, String endDate);

}
