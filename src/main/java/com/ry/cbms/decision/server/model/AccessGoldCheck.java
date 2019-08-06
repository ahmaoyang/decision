package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/5/15 17:20
 * @Description 出入金对账表
 */
@Data
@ApiModel(value = "出入金对账")
public class AccessGoldCheck extends BaseEntity<Integer> {
    @ApiModelProperty(value = "支付通道")
    private String payChannel;

    @ApiModelProperty(value = "交易单号")

    private String tradeCode;

    @ApiModelProperty(value = "系统流水号")

    private String serialNum;
    @ApiModelProperty(value = "交易金额")

    private BigDecimal tradeAmount;
    @ApiModelProperty(value = "通道手续费")

    private BigDecimal channelFees;
    @ApiModelProperty(value = "通道到账")

    private BigDecimal actualArrival;
    @ApiModelProperty(value = "支付时间")

    private String  payTime;
    @ApiModelProperty(value = "对账结果")

    private Integer checkResult;
    @ApiModelProperty(value = "备注")

    private String remark;
    @ApiModelProperty(value = "问题金额")

    private BigDecimal remarkMoney;
    @ApiModelProperty(value = "实际通道费用")

    private BigDecimal actualChannelFees;
    @ApiModelProperty(value = "图片地址")

    private String imageUrl;
    @ApiModelProperty(value = "对账类别 1 入金对账，2出金对账")
    private String checkKind;//对账类别（1 入金对账，2出金对账）


}
