package com.ry.cbms.decision.server.model;

import lombok.Data;

/**
 * @Author maoYang
 * @Date 2019/6/12 16:15
 * @Description 经济商账户信息
 */
@Data
public class BusinessAccountInfo {
    private String userApplyInAmount;//用户申请入金金额

    private String userPayActual;//用户实际支付

    private String toAccountActualAmount; //入金实际到账金额

    private String channelInCashFee;//入金通道手续费

    private String channelInCashActualFee;//入金通道实际手续费

    private String inCashBrokerFee;//入金经济商手续费

    private String userApplyOutAmount;//用户申请本金出金金额

    private String outActualCashAmount;//本金出金实际支出金额

    private String userCashOutActualAmount;//用户本金出金实际到账金额

    private String cashOutChannelFee;      //本金出金通道手续费

    private String cashOutBrokerFee;      //本金出金经纪商收取手续费

    private String userApplyCommOutAmount; //用户申请佣金出金金额

    private String commOutActualAmount;                        //佣金出金实际支出金额

    private String userCommOutActualToAccountAmount;//用户佣金出金实际到账金额

    private String commOutChannelFee;//佣金出金通道手续费

    private String commOutActualFee;//佣金出金通道实际手续费

    private String commOutBrokerFee;//佣金出金经纪商收取手续费

    private String totalChannelFee;//总通道手续费
    private String outPlatformActPayFee;//本金出金通道实际手续费
    private String totalBrokerFee;//总经济商手续费

    private String brokerActualNetIn;//经济商实际净收入


    private String dateTime;  //日期时间

    private String preCashBalance;//上期现金余额

    private String currentCashBalance;//本期现金余额

}
