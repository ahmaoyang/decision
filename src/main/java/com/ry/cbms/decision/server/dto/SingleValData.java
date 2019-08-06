package com.ry.cbms.decision.server.dto;

import java.util.Date;

/**
 * @ClassName Data
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/5 19:24
 * @Version 1.0
 **/
@lombok.Data
public class SingleValData {
    private String symbol;
    private Date time;
    private double bid;
    private double ask;
    private double low;
    private double high;
    private int direction;
    private int digits;
    private int spread;
    private Date modifyTime;
}
