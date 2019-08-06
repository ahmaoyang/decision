package com.ry.cbms.decision.server.redis;
/**
 * @Author maoYang
 * @Date 2019/4/25
 * @Description
 */
public class AccessKey extends BasePrefix{

	private AccessKey( String prefix) {
		super(prefix);
	}
	public static AccessKey withPrifix() {
		return new AccessKey("access");
	}
}
