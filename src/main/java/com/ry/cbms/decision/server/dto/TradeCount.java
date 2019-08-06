package com.ry.cbms.decision.server.dto;

import lombok.Data;

/**
 * @ClassName TradeCount
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/10 11:12
 * @Version 1.0
 **/
@Data
public class TradeCount {
    private int order;
    private int login;
    private String symbol;
    private int digits;
    private int cmd;
    private int volume;
    private long openTime;
    private int state;
    private String reason;
    private double openPrice;
    private int sl;
    private int tp;
    private int closeTime;
    private int expiration;
    private int commission;
    private int commissionAgent;
    private double closePrice;
    private double profit;
    private int taxes;
    private int magic;
    private String comment;
    private long timestamp;
    private double storage;
    private int intReason;
}
