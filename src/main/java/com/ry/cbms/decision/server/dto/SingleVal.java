package com.ry.cbms.decision.server.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SingleVal
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/8 15:00
 * @Version 1.0
 **/
@Data
public class SingleVal {
    private String code;
    private String msg;
    private String ver;
    private SingleValData data;
}
