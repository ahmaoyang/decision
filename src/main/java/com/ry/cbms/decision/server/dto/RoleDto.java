package com.ry.cbms.decision.server.dto;

import java.util.List;

import com.ry.cbms.decision.server.model.Role;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "角色类")
public class RoleDto extends Role {

    private static final long serialVersionUID = 4218495592167610193L;
    @ApiModelProperty(value = "权限Id")
    private List<Long> permissionIds;

}
