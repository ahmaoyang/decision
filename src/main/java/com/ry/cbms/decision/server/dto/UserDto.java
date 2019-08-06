package com.ry.cbms.decision.server.dto;

import java.util.List;

import com.ry.cbms.decision.server.model.SysUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户数据对象")
public class UserDto extends SysUser {

    private static final long serialVersionUID = -184009306207076712L;
    @ApiModelProperty(value = "角色")
    private List<Long> roleIds;


}
