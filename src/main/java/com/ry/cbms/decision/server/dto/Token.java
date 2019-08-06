package com.ry.cbms.decision.server.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Restful方式登陆token
 *
 * @author maoyang
 * 2019年5月4日
 */

@Data
public class Token implements Serializable {

    private static final long serialVersionUID = 6314027741784310221L;
    @ApiModelProperty(value = "token")
    private String token;
    @ApiModelProperty(value = "登陆时间戳（毫秒）")
    private Long loginTime;

    public Token(String token, Long loginTime) {
        super ();
        this.token = token;
        this.loginTime = loginTime;
    }
}
