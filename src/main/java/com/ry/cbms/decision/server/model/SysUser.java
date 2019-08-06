package com.ry.cbms.decision.server.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "SysUser", description = "用户对象")
public class SysUser extends BaseEntity<Long> {

    private static final long serialVersionUID = -6525908145032868837L;
    @ApiModelProperty(value = "姓名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "头像")
    private String headImgUrl;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "生日")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @ApiModelProperty(value = "性别")
    private Integer sex;

    @ApiModelProperty(value = "用户状态")
    private Integer status;

    @ApiModelProperty(value = "用户介绍")
    private String intro;

    @ApiModelProperty(value = "最新登陆日期")
    private Date lastLogin;

    @ApiModelProperty(value = "登陆IP")
    private String loginIP;

    @ApiModelProperty(value = "登陆设备")
    private String loginDevice;
    @ApiModelProperty(value = "昵称")
    private String nickname;

    public interface Status {
        int DISABLED = 0;
        int VALID = 1;
        int LOCKED = 2;
    }

}
