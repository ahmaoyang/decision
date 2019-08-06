package com.ry.cbms.decision.server.redis;

/**
 * @author  maoyang
 * 缓存key  前缀
 */
public interface KeyPrefix {
		
	 int expireSeconds();
	
	 String getPrefix();
	
}
