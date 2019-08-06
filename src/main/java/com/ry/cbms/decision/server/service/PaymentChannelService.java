package com.ry.cbms.decision.server.service;


import com.ry.cbms.decision.server.Msg.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/21 13:44
 * @Description 支付通道相关
 */
public interface PaymentChannelService {

    Integer getChannelCashCount(Map<String, Object> params);

    List getChannelCashList(Map<String, Object> params, Integer offset, Integer limit);//出入金对账集合

    Map<String,Object> checkBill(String startDate ,String endDate,String checkKind);//对账操作

    Result uploadCheckBill(MultipartFile file);

    void dealProbCashBill(String id,String remark,String conflictAmount,String imageUrl,String checkResult,String checkKind);//入金问题账单处理
}
