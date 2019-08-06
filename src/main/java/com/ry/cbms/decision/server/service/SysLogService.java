package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.model.SysLogs;

/**
 * 日志service
 * 
 * @author maoyang
 *
 *         2019年5月19日
 */
public interface SysLogService {

	void save(SysLogs sysLogs);

	void save(Long userId, String module, Boolean flag, String remark);

	void deleteLogs();
}
