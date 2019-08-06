package com.ry.cbms.decision.server.dao;


import com.ry.cbms.decision.server.model.RetailAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/3 13:43
 * @Description 零售端相关
 */
@Mapper
public interface RetailDao {
    List<RetailAccount> selectRetailInfoByCondition();

    void saveRetailInfo(@Param("retailAccounts") List retailAccounts);

    void deleteRetailInfo(@Param("todayDate") String todayDate); //今天日期

    RetailAccount select( @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<RetailAccount> selectByDay(@Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<RetailAccount> selectByMonth(@Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);

    RetailAccount getRetailHisSum(@Param("account") String account);


    List<String> SelectMt4AccsByUserId(@Param("userIds") String userIds);


    List<Map<String, Object>> selectByDayUnit(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
