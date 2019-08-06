package com.ry.cbms.decision.server.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author maoYang
 * @Date 2019/7/12 11:27
 * @Description 单量评估
 */
@Data
public class SingleEvalDataVo implements Serializable {

    public static final Long serialVersionUID = 7363353918096951799L;

    private String brokerBalance ;//经纪商余额

    private String bondBalance;//保证金余额

    private String retailAccountRmbBalance;//应付零售账户人民币余额

    private String retailAccountDollarBalance;//应付零售账户美元余额

    private String closedProfit;//已平仓盈亏

    private String commBalance;//应付佣金余额

    private String cashedBalance;//已兑付人民币余额

    private String throwTradeNum;//抛单交易量

    private String notThrowTradeNum;//非抛单交易量

    private String totalTradeNum;//总交易量

    private String ratio;//零售账户余额/保证金账户余额 比

    private String createDate;//日期


}
