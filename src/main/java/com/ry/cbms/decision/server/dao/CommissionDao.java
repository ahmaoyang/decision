package com.ry.cbms.decision.server.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/28 10:15
 * @Description 返佣
 */
@Mapper
public interface CommissionDao {

    List<Map> selectCommOutDetail(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//出金流水

    List<Map> selectCommToPrincipleDetail(@Param("userId") String serId, @Param("startDate") String startDate, @Param("endDate") String endDate);//出金流水


    List<Map> allCommision(@Param("userid") String userid, @Param("offset") Integer offset, @Param("limit") Integer limit);

    Integer getAllAccNum(@Param("userid") String userid);

    Map selectAcctid(@Param("userId") String userId);


    @Select("SELECT registertime FROM cbms.agents_users where userid=#{userId}")
    String selectRegisterById(String userId);

    @Select("SELECT mt4Acct, sum(sumCommission)sumCommission FROM cbms.report_ib_commission where userid=#{userId}")
    Map selectIbCommByUserId(@Param("userId") String userId);

    @Select("SELECT mt4Acct, sum(sumCommission)sumCommission FROM cbms.report_staff_commission where userid=#{userId}")
    Map selectStaffCommByUserId(@Param("userId") String userId);


    @Select("select * from cbms.detail_closeprice_commission where commUserid=#{accUserId}")
    List<Map> selectByCommUserId(@Param("accUserId") String accUserId);

    List<Map> selectCommisionRecord(@Param("ibid") String ibid, @Param("mt4Acct") String mt4Acct);

    BigDecimal sumComm(@Param("ibid") String ibid, @Param("startDate") String startDate, @Param("overDate") String overDate);

    BigDecimal sumCommByMt4(@Param("ibid") String ibid, @Param("mt4Acct") String mt4Acct, @Param("startDate") String startDate, @Param("overDate") String overDate);

    List<String> selectIbIdsByMt4Acc(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acct") String mt4Acct);


    List<Map> getCommOutList(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("ibid") String ibid );

    @Select("select userid,allBalance from cbms.cbms_account ")
    List<Map> selectAllAccs();

    Integer selectAllAccNumByUserId(@Param("ids") String ids);

    List<Map> selectAllAccByUserId(@Param("ids") String ids, @Param("offset") Integer offset, @Param("limit") Integer limit);

//    @Select("select sum(sumCommission) from cbms.report_ib_commission where  userid=#{userId}")
//    BigDecimal sumCommByUserId(@Param("userId") String userId);//根据用户id 查询拥金生成

    @Select("select sum(sumCommission) from cbms.report_ib_commission")
    BigDecimal sumComm2();//查询总的佣金

    BigDecimal sumCommByUserIdAndTime(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//根据用户id 查询时间段内拥金生成

    BigDecimal sumStaffCommByUserIdAndTime(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//根据statff查询


    BigDecimal sumCommOutByUserIdAndTime(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//根据用户id 和时间范围 查询拥金的出金

//    @Select("select sum(moneyUsd) from cbms.flow_withdraw where  userid=#{userId} and state=1 and mt4State=1")
//    BigDecimal sumCommOutByUserId(@Param("userId") String userId);//根据用户id 查询拥金总的出金

    @Select("select sum(moneyUsd) from cbms.flow_withdraw where  userid=#{userId} and state=1 and mt4State=1")
    BigDecimal sumCommOutFeeByUserId(@Param("userId") String userId);//佣金出金手续费

    BigDecimal sumCommOutFeeByUserIdAndTime(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//佣金出金手续费

    BigDecimal sumCommOutActFeeByUserIdAndTime(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//佣金出金实际手续费


    BigDecimal sumCommOutBrokerFeeByUserIdAndTime(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//佣金出金实际手续费


    @Select("select sum(tranMoney) from cbms.flow_transfer where  userid=#{userId} and acctFromType=2 and state=1")
    BigDecimal sumCommToPrincipalByUserId(@Param("userId") String userId);//佣金转本金


    BigDecimal sumCommToPrincipalByUserIdAndTime(@Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//佣金转本金


    @Select("select * from cbms.report_ib_commission where ibid=#{userId} or userid=#{userId}")
    List<Map> getCommRepByCondition(@Param("userId") String userId);//指定用户或代理下面所属全部账户的信息

    @Select("SELECT * FROM cbms.staff_info where staffid =#{staffId}")
    Map selectStaffById(String staffId);//根据员工Id 查询员工信息

    @Select("SELECT * FROM cbms.agents_users where agents_users =#{staffId}")
    Map selectAgentById(String userId);//根据Id 查询代理信息

    @Select("select ibid as ibId, userid as userId from cbms.user_ib_rel where ibid in (SELECT userid FROM cbms.user_ib_rel where ibid=#{ibId}) or ibid=#{ibId} and isAgent=1")
    List<Map> selectIbUserByCondition(String ibId);//根据代理Id 查询代理下级代理

    @Select("SELECT ibid FROM cbms.ib_staff_rel r where r.staffid =#{staffId}")
    List<Integer> selectStaffIbs(String staffId);//查询会员下的代理

    @Select("SELECT userid FROM cbms.user_staff_rel r where r.staffid =#{staffId}")
    List<Integer> selectStaffRels(String staffId);//查询会员下的代理

    List<Map<String, Object>> selectCommByDay(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Map<String, Object>> selectCommByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);


    @Select("SELECT allBalance FROM  cbms.cbms_account where acctType =1 and acctid=#{mt4Acc}")
    BigDecimal selectCommBalance(@Param("mt4Acc") String mt4Acc);//查询佣金余额


}
