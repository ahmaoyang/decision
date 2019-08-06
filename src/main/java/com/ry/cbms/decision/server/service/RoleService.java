package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.dto.RoleDto;

public interface RoleService {

	void saveRole(RoleDto roleDto);

	void deleteRole(Long id);
}
