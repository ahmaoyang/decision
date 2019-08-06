package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/7/15 15:43
 * @Description 已兑付余额
 */
@Data
public class CashedBalance implements Serializable {
    private static final long serialVersionUID = -3802292814767103648L;
    private Long id;

    private String cashedBalance;//兑付余额

    private Object createTime;

    private String currency;//币种


}
