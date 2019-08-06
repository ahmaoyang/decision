package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "菜单")
public class Permission extends BaseEntity<Long> {

    private static final long serialVersionUID = 6180869216498363919L;
    @ApiModelProperty(value = "父类菜单Id")
    private Long parentId;
    @ApiModelProperty(value = "菜单名称",required = true)
    private String name;
    @ApiModelProperty(value = "菜单Url",required = true)

    private String href;
    @ApiModelProperty(value = "菜单类型 1 是1类菜单，2 是功能菜单",required = true)
    private Integer type;
    @ApiModelProperty(value = "菜单权限")
    private String permission;
    @ApiModelProperty(value = "菜单排序")

    private Integer sort;
    @ApiModelProperty(value = "子菜单")
    private List<Permission> child;

}
