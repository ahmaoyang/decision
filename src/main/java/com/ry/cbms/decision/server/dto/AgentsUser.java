package com.ry.cbms.decision.server.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName AgentsUser
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/18 16:33
 * @Version 1.0
 **/
@Data
public class AgentsUser {
    @ApiModelProperty(value = "用户id")
    private String userid;
    @ApiModelProperty(value = "用户姓名")
    private String name;
}
