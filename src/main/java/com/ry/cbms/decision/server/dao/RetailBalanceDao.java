package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.model.RetailBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author maoYang
 * @Date 2019/7/15 13:44
 * @Description 应付零售账户美元余额(也就是所有mt4 用户的总记录)
 */
@Mapper
public interface RetailBalanceDao {

    void save(RetailBalance retailBalance);

    List<RetailBalance> getBalanceByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<RetailBalance> getBalanceByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);

   RetailBalance getBalanceByCreateDate(@Param("createTime") String createTime);

    RetailBalance getBalance(@Param("startDate") String startDate, @Param("endDate") String endDate);


    List<RetailBalance>  getBalanceByCondition(@Param("startDate") String startDate, @Param("endDate") String endDate);

    void  update(RetailBalance retailBalance);


}
