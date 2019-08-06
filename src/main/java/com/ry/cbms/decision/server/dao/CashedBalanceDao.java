package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.model.CashedBalance;
import com.ry.cbms.decision.server.model.RetailBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author maoYang
 * @Date 2019/7/15 15:46
 * @Description 兑付余额
 */
@Mapper
public interface CashedBalanceDao {
    void save(@Param("cashedBalance") CashedBalance cashedBalance);

    List<CashedBalance> getBalanceByDay(@Param("currency")String currency,@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<CashedBalance> getBalanceByMonth(@Param("currency")String currency,@Param("startDate") String startDate, @Param("endDate") String endDate);

    CashedBalance getBalance(@Param("currency")String currency,@Param("startDate") String startDate, @Param("endDate") String endDate);


}
