package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/5/22 15:24
 * @Description TODO
 */
@Data
@ApiModel(value = "请求类")
public class BaseRequestVo implements Serializable {
    private static final long serialVersionUID = 7363353918096951799L;
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "冲突金额")
    private String conflictAmount;

    @ApiModelProperty(value = "对账结果")
    private String checkResult;
    @ApiModelProperty(value = "图片地址")
    private String imageUrl;

    @ApiModelProperty(value = "对账类型")
    private String checkKind;
}
