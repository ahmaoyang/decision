package com.ry.cbms.decision.server.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/25 15:30
 * @Description 转账相关
 */
@Mapper
public interface TransferDao {

    List<Map> getTransferByMt4Acc(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc, @Param("acctFromType") String acctFromType);//转账记录
}
