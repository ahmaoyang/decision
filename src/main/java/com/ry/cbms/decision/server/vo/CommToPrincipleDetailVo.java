package com.ry.cbms.decision.server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/7/17 14:37
 * @Description TODO
 */
@Data
public class CommToPrincipleDetailVo implements Serializable {
    public static final Long serialVersionUID = 7363353918096951799L;
    private String id;

    private String mtAcctTo;

    private String tranMoney;

}
