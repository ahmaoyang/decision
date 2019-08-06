package com.ry.cbms.decision.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "响应消息")

public class ResponseInfo implements Serializable {

    private static final long serialVersionUID = -4417715614021482064L;
    @ApiModelProperty(value = "响应吗")
    private String code;
    @ApiModelProperty(value = "响应消息")
    private String message;

    public ResponseInfo(String code, String message) {
        super ();
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
