package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/7/15 13:42
 * @Description 零售端总的余额
 */
@Data
public class RetailBalance implements Serializable {
    private static final long serialVersionUID = -3802292814767103648L;

    private Long id;

    private Double retailBalance;

    private String createTime;


}
