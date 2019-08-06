package com.ry.cbms.decision.server.vo;

import java.io.Serializable;
import java.util.List;

import com.ry.cbms.decision.server.model.Notice;
import com.ry.cbms.decision.server.model.SysUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "通知")
@Data
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 7363353918096951799L;
    @ApiModelProperty(value = "通知")
    private Notice notice;
    @ApiModelProperty(value = "用户集合")
    private List<SysUser> users;

}
