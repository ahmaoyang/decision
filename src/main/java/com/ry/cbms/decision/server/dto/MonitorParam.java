package com.ry.cbms.decision.server.dto;

import lombok.Data;

/**
 * @ClassName MonitorParam
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/9 14:24
 * @Version 1.0
 **/
@Data
public class MonitorParam {
    private String type;
    private int pageSize;
    private int pageNum;
    private String symbol;
}
