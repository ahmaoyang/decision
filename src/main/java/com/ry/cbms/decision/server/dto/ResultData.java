package com.ry.cbms.decision.server.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ResultData
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/10 14:48
 * @Version 1.0
 **/
@Data
public class ResultData {
    private List<CharData> priceList;
    private Map TradeCount;
    private String type;
}
