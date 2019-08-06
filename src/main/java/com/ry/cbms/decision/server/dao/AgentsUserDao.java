package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.dto.AgentsUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component("AgentsUserDao")
public interface AgentsUserDao {

    List<AgentsUser>  getAgentsUserList(String dimUserName);

}
