package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author maoYang
 * @Date 2019/5/15 17:18
 * @Description 经济商账户出入金表
 */
@Data
public class BrokerCustomerAccessGold extends BaseEntity<Integer>{

    private int  account;

    private int  typeId;

    private int agentId;

    private BigDecimal previousBalance;


}
