package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "数据字典")
public class Dict extends BaseEntity<Long> {

    private static final long serialVersionUID = -2431140186410912787L;
    @ApiModelProperty(value = "字典类别")
    private String type;
    @ApiModelProperty(value = "字典key")
    private String k;
    @ApiModelProperty(value = "字典value")
    private String val;

}
