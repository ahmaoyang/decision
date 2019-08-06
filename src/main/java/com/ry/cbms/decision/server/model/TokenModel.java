package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.util.Date;
@Data
public class TokenModel extends BaseEntity<String> {

	private static final long serialVersionUID = 4566334160572911795L;

	/**
	 * 过期时间
	 */
	private Date expireTime;
	/**
	 * LoginUser的json串
	 */
	private String val;

}
