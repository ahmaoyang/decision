package com.ry.cbms.decision.server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName TradeCountData
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/11 11:32
 * @Version 1.0
 **/
@Data
public class TradeCountData {
    private String type;
    private List<TradeAmount> tradeAmountList;
}
