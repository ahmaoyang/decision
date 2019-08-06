package com.ry.cbms.decision.server.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SingleFormData
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/11 10:33
 * @Version 1.0
 **/
@Data
public class SingleCharData {
    private String type;
    private List<CharData> priceList;
}
