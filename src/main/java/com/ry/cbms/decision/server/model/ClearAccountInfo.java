package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/5/15 17:16
 * @Description 清算账户
 */
@Data
@ApiModel("清算账户")
public class ClearAccountInfo  {
    private Integer  id;
    private String  createTime;
    private String  updateTime;

    @ApiModelProperty(value = "隔夜利息(总)")
    private BigDecimal rollovers;//隔夜利息

    @ApiModelProperty(value = "隔夜利息(6000)", required = true)
    private BigDecimal rolloversFor6000;

    @ApiModelProperty(value = "隔夜利息(6002)", required = true)
    private BigDecimal rolloversFor6002;



}
