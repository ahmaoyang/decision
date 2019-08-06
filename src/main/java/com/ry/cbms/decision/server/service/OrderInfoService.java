package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.model.OrderDetail;
import com.ry.cbms.decision.server.model.ThrowInfo;
import com.ry.cbms.decision.server.vo.OrderDetailVo;
import com.ry.cbms.decision.server.vo.SingleEvalDataVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/11 10:25
 * @Description 订单信息模块
 */
public interface OrderInfoService {

    ThrowInfo getThrowHisSum(String account);

    List<ThrowInfo> throwOrder(String account, String flag, String startDate, String endDate);//抛单

    Map<String, Object> geAccDetail(String account, String userId, String curr, String startDate, String endDate);//账户明细

    Map<String, Object> geAccDetailList(String account, String userId, String curr, String startDate, String endDate, Integer offset, Integer limit);//账户明细列表

    Map<String, Object> getSpecifiedAccountDetail(String mt4Acc, String startDate, String endDate, String acctFromType, String flag);//根据Mt4 账号查询，出入金，转入，佣金出金 详情

    Map<Object, SingleEvalDataVo> SingleEval(String startDate, String endDate, String curr, String flag);//单量评估

    SingleEvalDataVo SingleEvalBalance(String startDate, String endDate, String curr);//单量评估


    Map<String, Object> getOrderDetails(String account, String orderFlag, String startDate, String endDate, Integer offset, Integer limit);//订单明细

    List<OrderDetail> loadOrderDetails(String account, String mt4Token);

    List<OrderDetailVo> getOrderRecordByAccount(String account);//根据账号查询订单记录

    List<OrderDetailVo> getOrderRecordByAccountAndDate(String account, String startDate, String endDate);//根据账号日期查询订单记录


}
