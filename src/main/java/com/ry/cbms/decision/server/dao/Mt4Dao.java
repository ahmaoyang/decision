package com.ry.cbms.decision.server.dao;

import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.model.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/7/28 16:23
 * @Description TODO
 */
@Mapper
public interface Mt4Dao {

    @Select("SELECT openDate  FROM cbms.flow_mt4acct where mtAcct=#{mt4Acc} ")
    Date getByMt4Acc(String mt4Acc);


    @Select("SELECT lever FROM cbms.flow_mt4acct where mtAcct=#{mt4Account } ")
    String getLeverByMt4Acc(String mt4Account );

}
