package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author maoYang
 * @Date 2019/7/27 12:42
 * @Description 登出
 */
@Data
@ApiModel("登出")
public class LogoutVo {
    @ApiModelProperty("token")
    private String token;
}
