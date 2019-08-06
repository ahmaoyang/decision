package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/5/15 17:08
 * @Description 零售账户信息
 */
@Data
public class RetailAccount {

    private Integer id;
    private String createTime;
    private Date updateTime;
    private String account; // 账号

    private String userId;
    private int agentId;//用户或代理账号

    private BigDecimal lastRetailPrincipal;//上期零售端本金

    private BigDecimal principalIn;//本金入金

    private BigDecimal commissionToPrincipal;//佣金转本金

    private BigDecimal principalOut;//本金出金

    private BigDecimal netCashIn;//净入金

    private BigDecimal closeProfitAndLoss;//平仓盈亏

    private BigDecimal deduction;//扣款

    private BigDecimal otherFunds;//其他款项

    private BigDecimal currentRetailBalance;//当前零售端余额

    private BigDecimal openProfitAndLoss;//未平仓盈亏

    private BigDecimal currentRetailNet;//当前零售端净值

    private Integer order;//交易订单号


}
