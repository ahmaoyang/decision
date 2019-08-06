package com.ry.cbms.decision.server.vo;

import com.ry.cbms.decision.server.model.BondInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author maoYang
 * @Date 2019/7/16 13:18
 * @Description 保证金账户返回
 */
@ApiModel(value = "保证金账户返回")
@Data
public class BondInfoVo implements Serializable {
    private static final long serialVersionUID = 7363353918096951799L;

    @ApiModelProperty(value = "总的本期净值",example = "0")
    private String total;
    @ApiModelProperty(value = "总的上期余额",example = "0")
    private String totalPre;


    @ApiModelProperty(value = "账户001本期净值",example = "0")
    private String HEJ001;
    @ApiModelProperty(value = "账户00上期余额",example = "0")
    private String HEJ001pre;


    @ApiModelProperty(value = "账户411本期净值",example = "0")
    private String HEJ411;
    @ApiModelProperty(value = "账户411上期余额",example = "0")
    private String HEJ411pre;

    @ApiModelProperty(value = "展示图形的数据")
    private List<BondInfo> accData;

}
