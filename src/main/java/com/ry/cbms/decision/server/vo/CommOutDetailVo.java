package com.ry.cbms.decision.server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/7/17 14:31
 * @Description 佣金出金Vo
 */
@Data
public class CommOutDetailVo implements Serializable {
    public static final Long serialVersionUID = 7363353918096951799L;

    private String id;
    private String  userid;
    private String  mt4Acct;
    private String moneyUsd;
    private String exRate;
    private String moneyRmb;
    private String payFee;
    private String platformPayFee;
    private String platformActPayFee;
}
