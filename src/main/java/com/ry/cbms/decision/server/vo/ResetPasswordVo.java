package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/7/18 18:54
 * @Description 重置密码
 */
@Data
@ApiModel("重置密码")
public class ResetPasswordVo implements Serializable {

    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("手机")
    private String phone;
    @ApiModelProperty("姓名")
    private String userName;

}
