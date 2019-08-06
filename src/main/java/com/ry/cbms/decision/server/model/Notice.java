package com.ry.cbms.decision.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("公告")
public class Notice extends BaseEntity<Long> {

	private static final long serialVersionUID = -4401913568806243090L;
@ApiModelProperty(value = "公告标题")
	private String title;
	@ApiModelProperty(value = "内容")
	private String content;
	@ApiModelProperty(value = "状态 0 草稿(未发布)，1:已经发布")
	private Integer status;
	public interface Status {
		int DRAFT = 0;
		int PUBLISH = 1;
	}

}
