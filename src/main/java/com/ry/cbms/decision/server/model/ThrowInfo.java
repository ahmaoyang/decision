package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/5/15 17:02
 * @Description 抛单信息
 */
@Data
public class ThrowInfo {
    private Integer id;
    private Date updateTime;
    private BigDecimal throwProfit;//抛单盈亏

    private BigDecimal notThrowProfit;//非抛单盈亏

    private BigDecimal riskThrowProfit;//风控抛单盈亏

    private BigDecimal amount;//总额

    private BigDecimal netProfit;//净利润

    private BigDecimal retailNotCloseSettle;//零售未平仓结算

    private BigDecimal retailNotCloseProfit;//零售未平仓抛单盈亏

    private String account;//用户mt4账号

    private int agentId;

    private Object createTime;//创建时间

    private String orderId;//抛单订单号

    private BigDecimal commission;//手续费

    private String clearAccOrderId;//清算账户订单号

    private String clearAcc;//清算账户号


    private Integer volume;//交易量


}
