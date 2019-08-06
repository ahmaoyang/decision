package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/7/5 17:47
 * @Description 保证金
 */
@Data
@ApiModel("保证金")
public class BondInfo  {
    private static final long serialVersionUID = 6180869216498363919L;

    private Integer id;
    private String  createTime;
    private String  updateTime;
    private String cashIn;//保证金入金

    private String cashOut;//保证金出金
    @ApiModelProperty(value = "流动性交易手续费")
    private String liquidityTransactionFee;//流动性交易手续费

    @ApiModelProperty(value = "桥相关费用")
    private String bridgeFee;//桥相关费用

    @ApiModelProperty(value = "已平仓盈亏")
    private String closedProfitAndLoss;//已平仓盈亏

    @ApiModelProperty(value = "其他")
    private String others;//其他

}
