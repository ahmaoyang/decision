package com.ry.cbms.decision.server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/7/17 10:09
 * @Description TODO
 */
@Data
public class OrderCashVo implements Serializable {
    public static final Long serialVersionUID = 7363353918096951799L;

    private String orderNo;
    private String mt4Acct;
    private String userid;
    private String moneyRmb;
    private String exRate;
    private String floatingRate;
    private String payFee;
    private String platformPayFee;
    private String platformActPayFee;
}
