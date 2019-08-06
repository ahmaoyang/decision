package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/6/10 14:16
 * @Description 订单评估
 */
@Data
public class OrderEvaluationInfo {
    private static final long serialVersionUID = 6180869216498363919L;
    private Integer id;
    private String  createTime;
    private Date updateTime;
    private Integer mt4Acc;
    private String orderVariety;

    private String orderType;

    private Integer tradeNum;

    private String tradeTotalHands;

    private String multipleHands;

    private String emptyHands;

    private String netPosition;

    private BigDecimal notCloseProfitsAndLoss;




}
