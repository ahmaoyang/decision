package com.ry.cbms.decision.server.dto;

import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * @ClassName Trade
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/10 11:14
 * @Version 1.0
 **/
@Data
public class Trade {
    private String code;
    private String msg;
    private String ver;
    private List<TradeCount> data;
}
