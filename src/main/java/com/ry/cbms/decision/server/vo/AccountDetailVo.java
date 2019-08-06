package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/6/24 17:07
 * @Description 账户明细Vo
 */
@Data
@ApiModel(value = "账户明细")
public class AccountDetailVo implements Serializable {

    private static final long serialVersionUID = 7363353918096951799L;

    @ApiModelProperty(value = "mt4账户")
    private String account;
    @ApiModelProperty(value = "上期账户余额")
    private String preBalance;
    @ApiModelProperty(value = "实际入金")
    private String actualCashIn;
    @ApiModelProperty(value = "实际出金")
    private String actualCashOut;
    @ApiModelProperty(value = "实际佣金出金")
    private String actualCommOut;
    @ApiModelProperty(value = "佣金转本金")
    private String commToCash;
    @ApiModelProperty(value = "本期余额")
    private String currentBalance;

    @ApiModelProperty(value = "兑付余额")
    private String cashedBalance;

    @ApiModelProperty(value = "备注")
    private String remark;
}
