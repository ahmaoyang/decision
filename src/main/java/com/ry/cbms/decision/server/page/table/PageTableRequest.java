package com.ry.cbms.decision.server.page.table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 分页查询参数
 *
 * @author maoyang
 */
@ApiModel("分页查询")
@Data
public class PageTableRequest implements Serializable {

    private static final long serialVersionUID = 7328071045193618467L;

    @ApiModelProperty(value = "分业查询的开始数 ",required = true)
    private Integer offset;
    @ApiModelProperty(value = "每页显示条数",required = true)
    private Integer limit;

    @ApiModelProperty(value = "查询参数")
    private Map<String, Object> params;

}
