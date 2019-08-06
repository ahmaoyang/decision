package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author maoYang
 * @Date 2019/5/15 17:13
 * @Description 佣金
 */
@Data
public class Commission extends BaseEntity<Integer> {

    private BigDecimal lastCommisionBalance;

    private BigDecimal commisionGen;

    private BigDecimal commissionToPrincipal;

    private BigDecimal commisionOut;

    private BigDecimal currentCommBanlance;

    private int account;

    private int agentId;


}
