package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.model.BondInfo;
import com.ry.cbms.decision.server.model.ClearAccountInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/30 18:51
 * @Description 保证金相关
 */
public interface BondInfoService {
    BondInfo getDayUnitInfo( String startDate,String endDate);

    BondInfo getBondInfoByCreateTime( String createTime);


    void save(List bondInfos);

    void delete(String todayDate);


    Map<String, Object> getBondInfo();

    List getBonAccData(String flag, String startDate, String endDate);

    void loadRemoteBondInfo(String dateTime);

    void updateBondInfo(BondInfo bondInfo);

    void saveClearAccInfo(ClearAccountInfo clearAccountInfo);

    void updateClearAccInfo(ClearAccountInfo clearAccountInfo);

    BondInfo getHisBondInfo();//保证金历史累计

    ClearAccountInfo getHisClearAccountInfo();//清算账户历史累计

    ClearAccountInfo getClearAccountInfoByCreateTime( String createTime);


    List getClearAccData(String flag, String startDate, String endDate);
}
