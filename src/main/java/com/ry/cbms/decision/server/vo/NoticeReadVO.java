package com.ry.cbms.decision.server.vo;

import java.util.Date;

import com.ry.cbms.decision.server.model.Notice;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "通知读取")
@Data
public class NoticeReadVO extends Notice {

    private static final long serialVersionUID = -3842182350180882396L;

    @ApiModelProperty(value = "用户Id")
    private Long userId;

    @ApiModelProperty(value = "读取时间")
    private Date readTime;

    @ApiModelProperty(value = "是否已读")
    private Boolean isRead;

}
