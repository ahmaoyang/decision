package com.ry.cbms.decision.server.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SinglePriceData
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/11 14:03
 * @Version 1.0
 **/
@Data
public class SinglePriceData {
    private String type;
    private List<CharData> singlePrice;
}
