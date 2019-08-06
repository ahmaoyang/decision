package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.model.Permission;

public interface PermissionService {

	void save(Permission permission);

	void update(Permission permission);

	void delete(Long id);
}
