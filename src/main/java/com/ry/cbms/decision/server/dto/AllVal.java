package com.ry.cbms.decision.server.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @ClassName AllVal
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/8 13:55
 * @Version 1.0
 **/
@Slf4j
@Data
public class AllVal {
    private String msg;
    private String ver;
    private String code;
    private List<com.ry.cbms.decision.server.dto.AllValData> data;
}
