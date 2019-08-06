package com.ry.cbms.decision.server.dto;

import lombok.Data;

/**
 * @ClassName AllValData
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/8 14:06
 * @Version 1.0
 **/
@Data
public class AllValData {
    private String symbol;
    private int trade;
    private int swapRollover3days;
    private String description;
    private String currency;
    private int digits;
    private long contractSize;
    private double swapLong;
    private double swapShort;
    private String marginCurrency;
}
