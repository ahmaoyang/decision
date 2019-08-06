package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/7/9 16:52
 * @Description 订单明细
 */
@ApiModel("订单明细")
@Data
public class OrderDetailVo implements Serializable {

    public static final Long serialVersionUID = 7363353918096951799L;

    @ApiModelProperty("mt4账号")
    private String account;

    @ApiModelProperty("客户订单盈亏")
    private String customerOrderProfit;

    @ApiModelProperty("清算账户盈亏")
    private String clearAccProfit;

    @ApiModelProperty("手续费")
    private String fee;

    @ApiModelProperty("经纪商盈亏")
    private String brokerProfit;//经济商盈亏

    @ApiModelProperty("返佣")
    private String returnComm;//返佣

    @ApiModelProperty("客户订单号")
    private String customerOrder;//客户订单号

    @ApiModelProperty("清算账户订单号")
    private String clearAccOrderNo;//清算账户订单号

    private Boolean problemOrder;//问题订单

}
