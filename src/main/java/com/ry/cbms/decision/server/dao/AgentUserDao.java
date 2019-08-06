package com.ry.cbms.decision.server.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @Author maoYang
 * @Date 2019/6/10 9:33
 * @Description TODO
 */
@Mapper
public interface AgentUserDao {

    @Select("SELECT loginid FROM cbms.agents_login where userState=1 limit #{offset},#{limit}")
    List<String> getAllAgentUser(@Param("offset") Integer offset, @Param("limit") Integer limit);//获取所有已认证代理和直客Id


    @Select("SELECT count(*) FROM cbms.agents_login where userState=1")
    Integer getAllAgentUserNum();//获取所有已认证代理和直客数量

    @Select("select distinct(userid) from cbms.flow_mt4acct where mtAcct =#{mt4Acc}")
    String SelectUserIdByMt4Acc(@Param("mt4Acc") String mt4Acc);


}
