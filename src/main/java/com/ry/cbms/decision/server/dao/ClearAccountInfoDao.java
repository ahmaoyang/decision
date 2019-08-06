package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.model.BondInfo;
import com.ry.cbms.decision.server.model.ClearAccountInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @Author maoYang
 * @Date 2019/7/8 14:47
 * @Description 清算账户
 */
@Mapper
public interface ClearAccountInfoDao {
    void save(@Param("info") ClearAccountInfo info);

    void update(@Param("info") ClearAccountInfo info);

    ClearAccountInfo getClearAccountInfoByCreateTime(@Param("createTime") String createTime);


    @Select("SELECT sum(rolloversFor6002)rolloversFor6002,sum(rolloversFor6000)rolloversFor6000,sum(rolloversFor6002+rolloversFor6002)rollovers FROM decision.clear_account_info")
    ClearAccountInfo  getHisClearAccountInfo();
    List<ClearAccountInfo> getByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);


    List<ClearAccountInfo> getByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
