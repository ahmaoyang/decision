package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author maoYang
 * @Date 2019/6/28 10:57
 * @Description 修改密码Vo
 */
@ApiModel("修改密码")
@Data
public class ModifyPassWordVo {
    @ApiModelProperty(value = "用户Id", required = true,example ="22")
    private String userId;

    @ApiModelProperty(value = "旧密码", required = true,example = "abc123")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true,example = "16888888")
    private String newPassword;
}
