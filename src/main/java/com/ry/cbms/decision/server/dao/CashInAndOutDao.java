package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.vo.CurrencyVo;
import com.ry.cbms.decision.server.vo.OrderCashVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/12 19:02
 * @Description 出金和入金
 */
@Mapper
public interface CashInAndOutDao {

    @Select("select sum(t.actInAmount)cashIn from cbms.flow_deposit t where  currency=#{currency} and mt4State=1 and state=1")
    BigDecimal getChannelCashInSum(@Param("currency") String currency);//总入金

    @Select("select sum(t.actOutAmount)cashOut from cbms.flow_withdraw t where currency=#{currency} and mt4State=1 and state=1")
    BigDecimal getChannelCashOutSum(@Param("currency") String currency);//总出金


    @Select("select toCurrency,currencySymbol,message from cbms.cbms_currency where isValid =1")
    List<CurrencyVo> currencyKinds();

    @Select("SELECT * FROM cbms.currency_site where paymentType=#{paymentType} and isValid=#{isValid}")
    Map getCashInOrOutRate(@Param("paymentType") String paymentType, @Param("isValid") String isValid);//获取出入金汇率(paymentType 1 入金 ，2 出金)


    Map<String, Object> getAccountData(@Param("currency") String currency, @Param("account") String account);

    BigDecimal getChannelCashInSumByCondition(@Param("mt4Acc") String mt4Acc, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//截止查询时间总入金

    BigDecimal getChannelCashOutSumByCondition(@Param("mt4Acc") String mt4Acc, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//截止查询时间总出金


    List<Map> getChannelCashInByDay(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//按天查询

    Map getChannelCashIn(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);


    Map getChannelCashOut(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);////按天查询


    List<Map> getChannelCashOutByDay(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);////按天查询


    List<Map> getChannelCashInByMonth(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//按月查询

    List<Map> getChannelCashOutByMonth(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);//按月查询


    List<Map> getChannelCommOutByMonth(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<Map> getChannelCommOutByDay(@Param("mt4Acct") String mt4Acct, @Param("currency") String currency, @Param("userId") String userId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    List<String> getCashOutByCondition(@Param("currency") String currency, @Param("mt4Acc") String mt4Acc);//查询当天出金记录

    List<Map> getCashOutGroupByMt4Acc(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc, @Param("offset") Integer offset, @Param("limit") Integer limit);

    List getCashOutGroupByMt4AccTotals(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc);

    List<Map> getCashInGroupByMt4Acc(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc, @Param("offset") Integer offset, @Param("limit") Integer limit);


    Integer getCashOutGroupByMt4AccTotalCount(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc);

    Integer getCashInGroupByMt4AccTotalCount(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc);

    List<Map> getCashOutByMt4Acc(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc, @Param("acctType") String acctType);

    List<Map> getCashInByMt4Acc(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("mt4Acc") String mt4Acc);


    @Select("select sum(t.actInAmount)cashIn from cbms.flow_deposit t where  currency=#{currency} and mt4State=1 and state=1 and  #{startDate}>=operaterTime")
    BigDecimal getChannelCashInSumBalance(@Param("currency") String currency,@Param("startDate") String startDate);

    @Select("select sum(t.actOutAmount)cashOut from cbms.flow_withdraw t where currency=#{currency} and mt4State=1 and state=1 and  #{startDate}>=operaterTime")
    BigDecimal getChannelCashOutSumBalance(@Param("currency") String currency,@Param("startDate") String startDate);



}
