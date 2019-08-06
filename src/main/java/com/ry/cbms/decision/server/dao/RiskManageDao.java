package com.ry.cbms.decision.server.dao;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/4 17:21
 * @Description 风控管理
 */
@Mapper
public interface RiskManageDao {
    @Select("SELECT 0 as ratio, a.mtAcct, a.openDate ,a.accType,tureMt ,(SELECT closeTime FROM cbms.detail_closeprice where mt4Acct=a.mtAcct order by closeTime  limit 1)  as lastCloseTime FROM cbms.flow_mt4acct a limit #{offset},#{limit}")
    List<JSONObject> getAccInfo(@Param("offset") Integer offset, @Param("limit") Integer limit);//查询mt4 账户信息

    @Select("SELECT Count(*) FROM cbms.flow_mt4acct")
    Integer getAccInfoCount();//查询mt4 账户信息总数

    @Select("SELECT * FROM cbms.flow_mt4acct where mtAcct=#{mtAcct}")
    Map getAccInfoByMt4(@Param ("mtAcct")String mtAcct);//查询mt4 账户信息总数


}
