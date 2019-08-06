package com.ry.cbms.decision.server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/7/17 15:30
 * @Description 币种
 */
@Data
public class CurrencyVo implements Serializable {
    public static final Long serialVersionUID = 7363353918096951799L;

    private String toCurrency;//币种

    private String message;//币种中文名称

    private String currencySymbol;//币种标示

}
