package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "角色类")
public class Role extends BaseEntity<Long> {

    private static final long serialVersionUID = -3802292814767103648L;

    @ApiModelProperty(value = "角色名称",required = true)
    private String name;

    @ApiModelProperty(value = "角色描述")
    private String description;

}
