package com.ry.cbms.decision.server.vo;

import com.ry.cbms.decision.server.model.Commission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/5/28 15:22
 * @Description 佣金Vo
 */
@Data
@ApiModel(value = "佣金")
public class CommissionVo implements Serializable {

    private static final long serialVersionUID = 7363353918096951799L;
    @ApiModelProperty(value = "上期余额")
    private String previousBalance;//上期余额

    @ApiModelProperty(value = "本期余额")
    private String currentBalance;//本期余额

    @ApiModelProperty(value = "佣金生成")
    private String genCommission;//佣金生成

    @ApiModelProperty(value = "佣金出金")
    private String outCommission;//佣金出金

    @ApiModelProperty(value = "通道手续费")
    private String channelFee;//通道手续费

    @ApiModelProperty(value = "通道实际手续费")
    private String channelActFee;//通道实际手续费

    @ApiModelProperty(value = "经纪商手续费手续费")
    private String brokerFee;//经纪商手续费手续费

    @ApiModelProperty(value = "注册时间")
    private String registerTime;//注册时间

    @ApiModelProperty(value = "佣金转本金")
    private String commissionToPrinciple;//佣金转本金

    @ApiModelProperty(value = "mt4账号")
    private String account;

    @ApiModelProperty(value = "用户Id")
    private String userId;//用户Id


}
