package com.ry.cbms.decision.server.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ResultSymbols
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/10 17:25
 * @Version 1.0
 **/
@Data
public class ResultSymbols {
    private List<SimpleValData> symbolList;
    private Pages pageInfo;
    private String type;
}
