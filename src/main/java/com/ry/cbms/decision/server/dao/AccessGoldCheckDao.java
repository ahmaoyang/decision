package com.ry.cbms.decision.server.dao;

import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.model.AccessGoldCheck;
import com.ry.cbms.decision.server.model.FileInfo;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/20 9:30
 * @Description 出入金对账
 */
@Mapper
public interface AccessGoldCheckDao {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into access_gold_check(payChannel, tradeCode, serialNum, tradeAmount, channelFees,actualChannelFees，actualArrival,payTime,checkResult,remark,remarkMoney) values(#{payChannel}, #{tradeCode}, #{serialNum}, #{tradeAmount}, #{channelFees}, #{actualArrival}, #{payTime}, #{checkResult}, #{remark},#{remarkMoney}, #{actualChannelFees})")
    int save(AccessGoldCheck accessGoldCheck);

    int saveAccessGoldChecks(@Param("checkList") List checkList);

    int updateBatch(List<AccessGoldCheck> accessGoldChecks);

    int updateChannelCheckBatch(@Param("channelChecks") List<Map> channelChecks);

    int update(AccessGoldCheck accessGoldCheck);

    int updateCashIn(@Param("params") Map<String, Object> params);

    int updateCashOut(@Param("params") Map<String, Object> params);

    @Select("select * from  access_gold_check t  where t.id = #{id}")
    AccessGoldCheck findById(Integer id);

    @Select("select * from  access_gold_check t  where t.serialNum = #{serialNum} and t.checkKind=#{checkKind}")
    AccessGoldCheck findBySerialNum(String serialNum, String checkKind);

    Integer getChannelCashInCount(@Param("params") Map<String, Object> params);

    Integer getChannelCashOutCount(@Param("params") Map<String, Object> params);


    Integer getCheckBillCount(@Param("params") Map<String, Object> params);


    AccessGoldCheck selectChecksBySerialNum(@Param("checkKind") String checkKind, @Param("id") String id);

    @Select("select * from cbms.flow_deposit t where operaterTime between #{startTime} and #{endTime} and mt4State=1 and state=1")
    List<Map> getChannelCashIns(@Param("startTime") String startTime, @Param("endTime") String endTime);//对账查询用到

    List<Map> getChannelCashOuts(@Param("startTime") String startTime, @Param("endTime") String endTime);//对账查询用到

    List<Map> getChannelCashInList(@Param("params") Map<String, Object> params, @Param("offset") Integer offset, @Param("limit") Integer limit);

    List<Map> getChannelCashOutList(@Param("params") Map<String, Object> params, @Param("offset") Integer offset, @Param("limit") Integer limit);


    @Select("select d.userid as userId,d.moneyUsd as cashIn,d.operaterTime as createTime,payFee,platformPayFee ,mt4Acct from cbms.flow_deposit d  where  mt4State=1 and state=1")
    List<Map> getAllDepositRecord();//查询所有的入金记录

    @Select("select w.moneyUsd as cashOut,acctType,w.operaterTime createTime,mt4Acct from cbms.flow_withdraw w where  mt4State=1 and state=1 and userid=#{userId} and w.operaterTime between #{startTime} and #{endTime}")
    List<Map> getAllWithdrawRecord(@Param("userId") String userId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);//查询所有的出金记录

    @Select("select mt4Acct,userid as userId ,closeTime,profit from cbms.detail_closeprice where userId=#{userId} and closeTime between #{startTime} and #{endTime}")
    List<Map> getAllDetailCloseRecord(@Param("userId") String userId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);//查询所有平仓记录

    @Select("select userid as userId, mtAcct,allBalance,requestTime from cbms.flow_dispute where userId=#{userId} and requestTime between #{startTime} and #{endTime}")
    List<Map> getAllDetailDisputeRecord(@Param("userId") String userId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);//查询所有争议金额记录

    @Select("select userid as userId, mtAcct,compensate,requestTime  from cbms.flow_compensate where userId=#{userId} and requestTime between #{startTime} and #{endTime}")
    List<Map> getAllDetailCompensateRecord(@Param("userId") String userId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);//查询所有赔付金额记录

    @Select("select mt4Acct,profit,volume from cbms.detail_closeprice  where mt4Acct=#{mt4Acc}")
    List<Map> getDetailCloseRecordProfitByAcc(@Param("mt4Acc") String mt4Acc);//查询平仓收益记录


    @Select("select mt4Acct,volume,closeTime from cbms.detail_closeprice ")
    List<JSONObject> getDetailCloseRecords();//查询所有用户平仓收益记录

    @Select("select closeTime  from cbms.detail_closeprice  where mt4Acct=#{mt4Acc} order by closeTime desc limit 1 ")
    String getLatestCloseTime(@Param("mt4Acc") String mt4Acc);

    @Select("SELECT volume,closeTime FROM cbms.detail_closeprice where mt4Acct=#{acc} order by closeTime desc limit 1 ")
    JSONObject getLastCloseRecordTime(@Param("acc") String acc);//查询用户最近平仓时间和交易量


    @Select("select closeTime  from cbms.detail_closeprice  where mt4Acct=#{mt4Acc} order by closeTime ")
    List<Date> getAllClose(@Param("mt4Acc") String mt4Acc);//查询所有的的记录

    @Select("SELECT mt4Acct,DATE(createtime) createtime,COUNT(*)orderNum FROM cbms.detail_closeprice WHERE DATE_SUB(#{closeTime}, INTERVAL 20 DAY) <= DATE(createtime) and mt4Acct=#{mt4Acc}")
    Map getTwentyDaysOrderNum(@Param("mt4Acc") String mt4Acc,@Param("closeTime") Date closeTime);


    @Select("SELECT mt4Acct,DATE(createtime) createtime,sum(volume)volumeNum FROM cbms.detail_closeprice WHERE DATE_SUB(#{closeTime}, INTERVAL 20 DAY) <= DATE(createtime) and mt4Acct=#{mt4Acc}")
    Map getTwentyDaysVolumeNum(@Param("mt4Acc") String mt4Acc,@Param("closeTime") Date closeTime);
}
