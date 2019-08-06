package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author maoYang
 * @Date 2019/7/27 12:33
 * @Description TODO
 */
@Data
public class LogInVo {
    @ApiModelProperty("登录邮箱或手机号")
    private String username; //用户
    @ApiModelProperty("密码")
    private String password;//密码
}
