package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "邮件")
public class Mail extends BaseEntity<Long> {

    private static final long serialVersionUID = 5613231124043303948L;

    @ApiModelProperty(value = "发送邮件用户id", required = true)
    private Long userId;
    @ApiModelProperty(value = "发送给用户的邮箱，多个用封号间隔(;)", required = true)
    private String toUsers;
    @ApiModelProperty("发送主题")
    private String subject;
    @ApiModelProperty("发送内容")
    private String content;
}
