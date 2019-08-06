package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.model.BondInfo;
import com.ry.cbms.decision.server.model.ClearAccountInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/7/8 13:47
 * @Description 保证金相关
 */
@Mapper
public interface BondInfoDao {

    BondInfo getByCreateDate(@Param("todayDate") String todayDate);

    BondInfo getByCreateTime(@Param("createTime") String createTime);

    void save(@Param("bondInfos") List bondInfos);

    BondInfo getDayUnitInfo(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Map<String, Object>> getByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Map<String, Object>> getByDayTimeCondition(@Param("startDate") String startDate, @Param("endDate") String endDate);

    Map getBondSum(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select(" SELECT sum(cashIn)cashIn,sum(cashOut)cashOut,sum(liquidityTransactionFee)liquidityTransactionFee,sum(bridgeFee) bridgeFee,sum(closedProfitAndLoss)closedProfitAndLoss ,sum(others)others FROM decision.bond_Info")
    BondInfo getHisBondInfo();

    List<BondInfo> getByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Map<String, Object>> getBondBalanceByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Map<String, Object>> getBondBalanceByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);

    void updateBondInfo(BondInfo bondInfo);

    List<Map<String, Object>> getBondByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);


}
