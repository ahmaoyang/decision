package com.ry.cbms.decision.server.redis;

import com.ry.cbms.decision.server.utils.Constants;

/**
 * @Author maoYang
 * @Date 2019/4/25
 * @Description
 */
public abstract class BasePrefix implements KeyPrefix{
	
	private int expireSeconds;//过期时间
	
	private String prefix;//前缀
	
	public BasePrefix(String prefix) {//0代表永不过期
		this(0, prefix);
	}
	
	public BasePrefix(int expireSeconds, String prefix) {
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	
	public int expireSeconds() {//默认0代表永不过期
		return expireSeconds;
	}

	public String getPrefix() {
		String className = Constants.PREFIX+getClass().getSimpleName();
		return className+":" + prefix;
	}

}
