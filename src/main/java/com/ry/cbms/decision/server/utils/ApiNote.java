package com.ry.cbms.decision.server.utils;

/**
 * @Author maoYang
 * @Date 2019/7/17 10:21
 * @Description TODO
 */
public class ApiNote {

    public static final String getHomePageProfitAndLossData = " \"totalTradeNumTotal\": 82,总交易历史累计交易量\n" +
            "        \"notThrowTradeNumTotal\": 30,非抛单历史累计交易量\n" +
            "        \"throwTradeNumTotal\": 52,抛单历史累计交易量\n" +
            "        \"cashedBalanceTotal\": 0,历史累计已兑付余额\n" +
            "        \"ratioTotal\": 0 历史累计零售端余额/保证金余额\n" +
            "\"throwTradeNum\": \"0\",抛单交易量\n" +
            "        \"notThrowTradeNum\": \"0\",非抛单交易量\n" +
            "        \"cashedBalance\": \"0\",已兑付余额\n" +
            "        \"totalTradeNum\": \"0\",总交易量\n" +
            "        \"ratio\": \"0\" 零售端余额/保证金余额" +
            "\"retailAccountRmbBalanceTotal\": 0,应付零售端人民币历史余额 \n" +
            "        \"retailAccountDollarBalanceTotal\": 0,应付零售端美元历史余额\n" +
            "        \"closedProfitTotal\": 0,应付已平仓历史盈亏\n" +
            "        \"commBalanceTotal\": 0 应付佣金历史余额\n" +
            "\"retailAccountRmbBalance\": \"0\",应付零售端人民币余额\n" +
            "        \"retailAccountDollarBalance\": \"0\",应付零售端美元余额\n" +
            "        \"closedProfit\": \"0\",应付已平仓盈亏\n" +
            "        \"commBalance\": \"0\" 应付佣金余额";

    public static final String login = "" +
            "登录方式：x-www-form-urlencoded  " +
            "{\n" +
            "  \"code\": 0, 0表示成功，非0失败\n" +
            "  \"msg\": \"success\",\n" +
            "  \"data\": {\n" +
            "    \"loginTime\": 1564199761930,时间戳\n" +
            "    \"token\": \"eyJhbGciOiJIUzI1NiJ9.eyJMT0dJTl9VU0VSX0tFWSI6IjU0Njc3OWI1LTAxZDItNGNjOC04ZTczLWMwNmMxZmUyM2M3NCJ9.-i-SMXZ2Op_AvzazRmmmMZsUY_2Sp_OkLNwxzkeidOQ\" token\n" +
            "  }\n" +
            "}";

    public static final String logout = "" +
            "登出方式：x-www-form-urlencoded" +
            "{\n" +
            "  \"code\": 0, 表示成功，非0表示是失败\n" +
            "  \"msg\": \"success\",\n" +
            "  \"data\": \"登出成功\"\n" +
            "}";
    public static final String findChannelCashIn = "\"platformActPayFee\": 0.03,通道实际手续费\n" +
            "            \"payFee\": 0.003,经纪商手续费\n" +
            "            \"moneyUsd\": 8000,金额(出金,入金)\n" +
            "            \"moneyRmb\": 54940.8,用户实际支付\n" +
            "            \"exRate\": 6.8676,汇率\n" +
            "            \"id\": 235,流水\n" +
            "            \"remarks\":备注\n" +
            "            \"actAmount\": 235,实际支付(到账)\n" +
            "            \"checkResult\":对账状态(对账状态,0-失败，1-成功，2-未对账，3-作废,4-通道(多出)问题，5-出金金额系统多出 6 出金账单\n" +
            "通道多出  7 .出金账单系统多出)\n" +

            "   \"platformPayFee\": 0.03,通道手续费\n" +
            "            \"operaterTime\":支付时间 \"2019-06-25 20:21:56\"" +
            "" +
            "\n第二行：\n" +
            "  \n   String payChannel;支付通道\n" +
            "\n" +
            "\n" +
            "     String tradeCode;交易单号\n" +
            "\n" +
            "\n" +
            "     String serialNum;系统流水号\n" +
            "\n" +
            "     BigDecimal tradeAmount;交易金额\n" +
            "  \n" +
            "\n" +
            "     BigDecimal channelFees;通道手续费\n" +
            "\n" +
            "     BigDecimal actualArrival;通道到账\n" +
            "  \n" +
            "\n" +
            "     String  payTime;支付时间\n" +
            " \n" +
            "\n" +
            "     Integer checkResult;对账结果\n" +
            "\n" +
            "     String remark;备注\n" +
            "\n" +
            "     BigDecimal remarkMoney;问题金额\n" +
            "\n" +
            "     BigDecimal actualChannelFees;实际通道费用\n" +
            "\n" +
            "     String imageUrl;图片地址\n" +
            "\n" +
            "     String checkKind;//对账类别（1 入金对账，2出金对账）";
    public static final String getCurrency = " \"data\": [\n" +
            "    {\n" +
            "      \"toCurrency\": \"CNY\",币种英文名称\n" +
            "      \"message\": \"人民币\",\n" +
            "      \"currencySymbol\": \"￥\"\n" +
            "    }\n" +
            "  ]";
    public static final String getSpecifiedAccountDetail = "\"platformActPayFee\": 0.03,通道实际手续费\n" +
            "        \"orderNo\": \"173295697775296512\",流水号\n" +
            "        \"payFee\": 0.003,经济商手续费（入金，出金,佣金出金都是这个字段标识）\n" +
            "        \"mt4Acct\": \"503\",mt4账号\n" +
            "        \"moneyRmb\": 54940.8,金额（入金，出金，佣金出金都是这个字段标识）\n" +
            "        \"exRate\": 6.8676,汇率\n" +
            "        \"userid\": 50228,用户id\n" +
            "        \"platformPayFee\": 通道手续费\n" +
            "        \" actualToAcc\":实际(到账)\n" +
            "        \" actPay\":\n" +


            " 转账： id:流水, mtAcctFrom:转出账户,mtAcctTo:转入账户 ,tranMoney 佣金转本金金额";

    public static final String throwOrder = "{\n" +
            "  \"code\": 0,\n" +
            "  \"msg\": \"success\",\n" +
            "  \"data\": [\n" +
            "    {\n" +
            "      \"id\": null,\n" +
            "      \"createTime\": 创建时间,\n" +
            "      \"updateTime\": null,\n" +
            "      \"throwProfit\": 0,抛单盈亏\n" +
            "      \"notThrowProfit\": 28.7,非抛单盈亏\n" +
            "      \"riskThrowProfit\": 0,风控抛单盈亏\n" +
            "      \"amount\": 28.7,总额\n" +
            "      \"netProfit\": 28.7,净利润\n" +
            "      \"retailNotCloseSettle\": 0,零售未平仓抛单结算\n" +
            "      \"retailNotCloseProfit\": 0,零售未平仓抛单盈亏\n" +
            "      \"account\": null,\n" +
            "      \"agentId\": 0,\n" +
            "      \"orderId\": null,\n" +
            "      \"commission\": null,\n" +
            "      \"clearAccOrderId\": null,\n" +
            "      \"clearAcc\": null\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    public static final String getOrderEva = "   按月传参 传每个月的第一天 例如：2019 五月到九月 2019-05-01  到 2019-09-01 " +
            "" +
            "" +
            "" +
            "" +
            "" +
            "{\n" +
            "  \"code\": 0,\n" +
            "  \"msg\": \"success\",\n" +
            "  \"data\": {\n" +
            "      \"brokerBalance\": null,经纪商现金余额\n" +
            "      \"bondBalance\": \"0.0\",保证金账户余额\n" +
            "      \"retailAccountRmbBalance\": \"0\",应付零售账户人民币余额\n" +
            "      \"retailAccountDollarBalance\": \"0\",应付零售账户美元余额\n" +
            "      \"closedProfit\": \"0\",应付/应收已平仓盈亏\n" +
            "      \"commBalance\": \"0\",应付佣金余额\n" +
            "      \"cashedBalance\": \"0\",已兑付人民币余额\n" +
            "      \"throwTradeNum\": \"0\",抛单交易量\n" +
            "      \"notThrowTradeNum\": \"0\",非抛单交易量\n" +
            "      \"totalTradeNum\": \"0\",总交易量\n" +
            "      \"ratio\": \"0\",零售账户余额/保证金账户\n" +
            "      \"createDate\": \"2019-05-01\"\n" +
            "    }\n" +
            "}";


    public static final String getCommTodayInfo = " \"previousBalance\": \"1.0881901456385686E9\",上期佣金余额\n" +
            "    \"currentBalance\": \"0\",本期佣金余额\n" +
            "    \"genCommission\": \"0\",历史佣金生成\n" +
            "    \"outCommission\": \"0\",历史佣金出金\n" +
            "    \"channelFee\": null,通道手续费\n" +
            "     \"channelActFee\": null,通道实际手续费\n" +
            "    \"registerTime\": null,\n" +
            "    \"brokerFee\": null,经纪商手续费\n" +
            "    \"commissionToPrinciple\": \"0\",历史佣金转本金";


    public static final String getCommInOutDetails = "{\n" +
            "  \"code\": 0,\n" +
            "  \"msg\": \"success\",\n" +
            "  \"data\": {\n" +
            "    \"commToPrinciples\": 佣金转本金数据 [\n" +
            "      {\n" +
            "        \"acctFromType\": \"2\",\n" +
            "        \"netValue\": 10000,\n" +
            "        \"endtime\": \"2019-06-28 20:30:31\",\n" +
            "        \"mtAcctTo\": \"304\",转入账户\n" +
            "        \"tureMtTo\": \"backup\",\n" +
            "        \"userName\": \"李艳杰\",\n" +
            "        \"userid\": 1013,\n" +
            "        \"mtServerFrom\": \"真实盘服务器\",\n" +
            "        \"mtServerTo\": \"真实盘服务器\",\n" +
            "        \"tranTime\": \"2019-06-28 20:30:31\",\n" +
            "        \"showMtFrom\": \"mt4组4\",\n" +
            "        \"mtAcctFrom\": \"303\",\n" +
            "        \"balance\": 10000,\n" +
            "        \"showMtTo\": \"mt4组5\",\n" +
            "        \"tureMtFrom\": \"APITest01\",\n" +
            "        \"deposit\": 10000,\n" +
            "        \"id\": 1, 流水号\n" +
            "        \"state\": \"1\",\n" +
            "        \"tranMoney\": 3000.55，转本金金额\n" +
            "      }\n" +
            "    ],\n" +
            "    \"commOuts\": [\n" + "出金数据" +
            "      {\n" +
            "        \"userid\": 1006,\n" +
            "        \"exRate\": 0,出金汇率\n" +
            "        \"operaterTime\": \"2019-06-28 15:51:10\",\n" +
            "        \"mt4State\": \"1\",\n" +
            "        \"mt4Acct\": \"1006002019062818\",账号\n" +
            "        \"moneyUsd\": 50,出金金额\n" +
            "        \"moneyRmb\": 50,人民币出金金额\n" +
            "        \"payFee\": 50,经纪商手续费\n" +
            "        \"platformPayFee\": 50,渠道手续费用\n" +
            "        \"platformActPayFee\": 50,渠道实际手续费用\n" +
            "        \"id\": 1,流水号\n" +

            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    public static final String getRetailInfo = "retailAccountList: 中字段说明 [：String account; // 账号\n" +
            "\n" +
            "      String userId;\n" +
            "      int agentId;//用户或代理账号\n" +
            "\n" +
            "      BigDecimal lastRetailPrincipal;//上期零售端本金\n" +
            "\n" +
            "      BigDecimal principalIn;//本金入金\n" +
            "\n" +
            "      BigDecimal commissionToPrincipal;//佣金转本金\n" +
            "\n" +
            "      BigDecimal principalOut;//本金出金\n" +
            "\n" +
            "      BigDecimal netCashIn;//净入金\n" +
            "\n" +
            "      BigDecimal closeProfitAndLoss;//平仓盈亏\n" +
            "\n" +
            "      BigDecimal deduction;//扣款\n" +
            "\n" +
            "      BigDecimal otherFunds;//其他款项\n" +
            "\n" +
            "      BigDecimal currentRetailBalance;//当前零售端余额\n" +
            "\n" +
            "      BigDecimal openProfitAndLoss;//未平仓盈亏\n" +
            "\n" +
            "      BigDecimal currentRetailNet;//当前零售端净值" +
            "}" +
            "]" +
            "\n" +
            "currRetailNet：本期零售端净值，preRetailBalance：上期零售端余额，currRetailBalance:当前零售端余额\"";


    public static final String getAccountInfo = "账户是不同币种的账户{CNY：人民币(账户)，GBP：英镑,THAILAND:泰国铢,USD:美元,VND:越南盾}," +
            "cashActIn:入金实际到账金额\n" +
            "commActOut:佣金出金实际支出金额\n" +
            "cashActOut:本金出金实际支出金额\n" +
            "currBalance:本期现金余额\n" +
            "message:币种中文名称\n" +
            "currency:币种英文名称\n" +
            "symbol:币种图标\n" +
            "preCashBalance:上期现金余额\n" +
            "CNYinfo: {\n" +
            "   userApplyInAmount;//用户申请入金金额\n" +
            "\n" +
            "     userPayActual;//用户实际支付\n" +
            "\n" +
            "    toAccountActualAmount; //入金实际到账金额\n" +
            "\n" +
            "    channelInCashFee;//入金通道手续费\n" +
            "\n" +
            "     channelInCashActualFee;//入金通道实际手续费\n" +
            "\n" +
            "     inCashBrokerFee;//入金经济商手续费\n" +
            "\n" +
            "     userApplyOutAmount;//用户申请本金出金金额\n" +
            "\n" +
            "     outActualCashAmount;//本金出金实际支出金额\n" +
            "\n" +
            "     userCashOutActualAmount;//用户本金出金实际到账金额\n" +
            "\n" +
            "     cashOutChannelFee;      //本金出金通道手续费\n" +
            "\n" +
            "     cashOutBrokerFee;      //本金出金经纪商收取手续费\n" +
            "\n" +
            "     userApplyCommOutAmount; //用户申请佣金出金金额\n" +
            "\n" +
            "     commOutActualAmount;                        //佣金出金实际支出金额\n" +
            "\n" +
            "     userCommOutActualToAccountAmount;//用户佣金出金实际到账金额\n" +
            "\n" +
            "     commOutChannelFee;//佣金出金通道手续费\n" +
            "\n" +
            "     commOutActualFee;//佣金出金通道实际手续费\n" +
            "\n" +
            "     commOutBrokerFee;//佣金出金经纪商收取手续费\n" +
            "\n" +
            "     totalChannelFee;//总通道手续费\n" +
            "\n" +
            "     totalBrokerFee;//总经济商手续费\n" +
            "\n" +
            "     brokerActualNetIn;//经济商实际净收入\n" +
            "\n" +
            "\n" +
            "     dateTime;  //日期时间\n" +
            "\n" +
            "     preCashBalance;//上期现金余额\n" +
            "\n" +
            "     currentCashBalance;//本期现金余额";

    public static final String getAccountDetail = "账户是不同币种的账户{CNY：人民币(账户)，GBP：英镑,THAILAND:泰国铢,USD:美元,VND:越南盾}" +
            "   userApplyInAmount;//用户申请入金金额\n" +
            "\n" +
            "  +   outPlatformActPayFee;//本金出金通道实际手续费\n" +
            "     userPayActual;//用户实际支付\n" +
            "\n" +
            "    toAccountActualAmount; //入金实际到账金额\n" +
            "\n" +
            "    channelInCashFee;//入金通道手续费\n" +
            "\n" +
            "     channelInCashActualFee;//入金通道实际手续费\n" +
            "\n" +
            "     inCashBrokerFee;//入金经济商手续费\n" +
            "\n" +
            "     userApplyOutAmount;//用户申请本金出金金额\n" +
            "\n" +
            "     outActualCashAmount;//本金出金实际支出金额\n" +
            "\n" +
            "     userCashOutActualAmount;//用户本金出金实际到账金额\n" +
            "\n" +
            "     cashOutChannelFee;      //本金出金通道手续费\n" +
            "\n" +
            "     cashOutBrokerFee;      //本金出金经纪商收取手续费\n" +
            "\n" +
            "     userApplyCommOutAmount; //用户申请佣金出金金额\n" +
            "\n" +
            "     commOutActualAmount;                        //佣金出金实际支出金额\n" +
            "\n" +
            "     userCommOutActualToAccountAmount;//用户佣金出金实际到账金额\n" +
            "\n" +
            "     commOutChannelFee;//佣金出金通道手续费\n" +
            "\n" +
            "     commOutActualFee;//佣金出金通道实际手续费\n" +
            "\n" +
            "     commOutBrokerFee;//佣金出金经纪商收取手续费\n" +
            "\n" +
            "     totalChannelFee;//总通道手续费\n" +
            "\n" +
            "     totalBrokerFee;//总经济商手续费\n" +
            "\n" +
            "     brokerActualNetIn;//经济商实际净收入\n" +
            "\n" +
            "\n" +
            "     dateTime;  //日期时间\n" +
            "\n" +
            "     preCashBalance;//上期现金余额\n" +
            "\n" +
            "     currentCashBalance;//本期现金余额";


    public static final String getCommissionInfo = "\"previousBalance\": \"26793.00\",上期余额\n" +
            "          \"currentBalance\": \"0.00\",本期余额\n" +
            "          \"genCommission\": \"0\",佣金生成\n" +
            "          \"outCommission\": \"0\",佣金出金\n" +
            "          \"channelFee\": \"0\",通道手续费\n" +
            "          \"registerTime\": \"2019-05-28 15:47:22.0\",注册时间\n" +
            "          \"commissionToPrinciple\": \"0\",佣金转本金\n" +
            "          \"account\": \"1001002019052815\",账号\n" +
            "          \"userId\": \"1001\"用户id\n" +
            "          \"lastRefreshTimeuserId\":最新刷新时间 \"\"\n" +
            "        ";

    public static final String getBrokerAccountInfo = "  {\n" +
            "      \"currencyName\": \"人民币\",币种名称\n" +
            "      \"currency\": \"CNY\",英文名称\n" +
            "      \"currencyFlag\": \"Y\",币种标示\n" +
            "      \"bondBalance\": 41794.25,保证金余额\n" +
            "      \"brokerAccBalance\": 经济商账户余额\n" +
            "      \"cashedBalance\": 兑付余额余额\n" +
            "      \"brokerPreAccBalance\": 经纪商上期余额\n" +
            "    }";


    public static final String getAccEva = "{\n" +
            "        \"tureMt\": \"APITest01\",所在组\n" +
            "        \"lastCloseTime\": \"2019-07-01 17:36:54\",最近平仓时间\n" +
            "        \"mtAcct\": \"502\",账号\n" +
            "        \"openDate\": \"2019-07-01 22:08:48\",开户日期\n" +
            "        \"accType\": \"1\",\n" +
            "        \"balance\": \"1\",余额\n" +
            "        \"ratio\": 0 风酬比\n" +
            "        \"leverage\": 0 杠杆\n" +
            "        \"isLevelPro\": 0 杠杆标红（true 是，false 不是）\n" +
            "        \"isRed\": true 风酬比那个数字变红色 ,false 不变\n" +

            "        \"corpse\": false 是否僵尸用户(true 是，false 不是)\n" +

            "      }";

    public static final String getRiskOrderEva = " {\n" +
            "        \"orderType\": null,\n" +
            "        \"orderVariety\": \"AUDUSD\",订单品种\n" +
            "        \"createTime\": null,创建时间\n" +
            "        \"emptyHands\": 200, 空单手数\n" +
            "        \"multipleHands\": 400,多单手数\n" +
            "        \"notCloseProfitsAndLoss\": 0.2,未平仓盈亏\n" +
            "        \"tradeNum\": 6,交易单数\n" +
            "        \"tradeTotalHands\": 600,交易总手数\n" +
            "        \"updateTime\": null,\n" +
            "        \"id\": null,\n" +
            "        \"netPosition\": 200,净头寸\n" +
            "        \"mt4Acc\": null mt4 账号\n" +
            "      }";


    public static final String getAccountDetail_order = "参数说明：\" +\n" +
            "            \"\\n\" + \"返回案例：\" +\n" +
            "            \"{\\n\" +\n" +
            "            \"  \\\"code\\\": 0,\\n\" +\n" +
            "            \"  \\\"msg\\\": \\\"success\\\",\\n\" +\n" +
            "            \"  \\\"data\\\": {\\n\" +\n" +
            "            \"    \\\"currentBalance\\\": 0,本期余额\\n\" +\n" +
            "    \\\"lastRefreshTime:\\\": 0,最近刷新时间\\n\" +\n" +
            "            \"    \\\"cashBalance\\\": 0,兑付余额\\n\" +\n" +
            "            \"    \\\"preBalance\\\": 0,上期余额\\n\" +\n" +
            "\n" +
            "            \"    \\\"accList\\\": [\\n\" + \"//\" + \"    \\\"accList\\\":不同账号的集合展示\\n\" +\n" +
            "            \"      {\\n\" +\n" +
            "            \"        \\\"actualCommOut\\\": 0,实际佣金支出\\n\" +\n" +
            "            \"        \\\"cashIn\\\": 13705.567,入金实际到账\\n\" +\n" +
            "            \"        \\\"accPreBalance\\\": 0,上期余额\\n\" +\n" +
            "            \"        \\\"mt4Acct\\\": \\\"301\\\",账号\\n\" +\n" +
            "            \"        \\\"cashBalance\\\": 0,兑付余额\\n\" +\n" +
            "            \"        \\\"currBalance\\\": 0,本期余额\\n\" +\n" +
            "            \"        \\\"actualCashOut\\\": 0，实际出金支出\\n\" +\n" +
            "            \"      }\\n\" +\n" +
            "            \"    ]\\n\" +\n" +
            "            \"  }\\n\" +\n" +
            "            \"}";

    public static final String getBondInfo = "" +
            "\"accData\": [\n" +
            "      {\n" +
            "        \"id\": 25,\n" +
            "        \"createTime\": \"2019-05-01 00:00:00\",日期时间\n" +
            "        \"updateTime\": \"2019-07-09 11:19:03\",\n" +
            "        \"cashIn\": \"5000.0\",入金\n" +
            "        \"cashOut\": \"5000.0\",出金\n" +
            "        \"liquidityTransactionFee\": \"111\",流动性交易手续费\n" +
            "        \"bridgeFee\": \"444\",桥相关费用\n" +
            "        \"closedProfitAndLoss\": \"111\",已平仓盈亏\n" +
            "        \"others\": \"111\"其他\n" +
            "      }";


    public static final String getAccountDataList = "{\n" +
            "  \"code\": 0,\n" +
            "  \"msg\": \"success\",\n" +
            "  \"data\": {\n" +
            "    \"accList\": {\n" +
            "      \"recordsTotal\": 总的记录条数,\n" +
            "      \"pageCount\": 总页数,\n" +
            "lastRefreshTime:\\\": 0,最近刷新时间\\n\" +\n" +
            "      \"data\": [\n" +
            "        {\n" +
            "          \"actualCommOut\": 佣金实际支出,\n" +
            "          \"cashIn\":入金实际到账,\n" +
            "          \"accPreBalance\": 账户上期余额,\n" +
            "          \"mt4Acct\": \"mt4账号\",\n" +
            "          \"cashBalance\":兑付余额 ,\n" +
            "          \"currBalance\": 当前余额,\n" +
            "          \"actualCashOut\": 实际本金出金\n" +
            "        }" +
            " ]\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String orderDetailList = "返回结果：" +
            "\n" + "" +
            "{\n" +
            "  \"code\": 0,\n" +
            "  \"msg\": \"success\",\n" +
            "  \"data\": {\n" +
            "    \"commInfos\": {\n" +
            "      \"recordsTotal\": 1,总记录数\n" +
            "      \"recordsFiltered\": 1,\n" +
            "      \"pageCount\": 1,总页数\n" +
            "      \"data\": [\n" +
            "        {\n" +
            "          \"id\": null,\n" +
            "          \"account\": \"300\",账号\n" +
            "          \"isRed\": \"true\",是否红色 true false 不是\n" +
            "          \"customerOrderProfit\": \"11\",客户订单盈亏\n" +
            "          \"clearAccProfit\": \"3\",清算账户盈亏\n" +
            "          \"clearAccOrderNo\": null,清算账户订单号\n" +
            "          \"fee\": \"3\",手续费\n" +
            "          \"brokerProfit\": \"3\",经济商盈亏\n" +
            "          \"createTime\": null,创建时间\n" +
            "          \"returnComm\": null,返佣\n" +
            "          \"customerOrder\": null，客户订单号\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "" + "lastRefreshTime：最新刷新时间" +
            "    \"totalDetailData\": {\n" + "//总的详细数据(头部显示)" +
            "      \"brokerProfitTotal\": \"3.0\",经济商盈亏\n" +
            "      \"feeTotal\": \"3.0\",手续费\n" +
            "      \"clearAccProfitTotal\": \"3.0\",清算账户盈亏\n" +
            "      \"customerOrderProfitTotal\": \"11.0\",客户订单盈亏\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String geAccDataByDate = "{\n" +
            "      \"balance\": 75537.998,余额\n" +
            "      \"reqTime\": \"2019-07-02\"日期时间\n" +
            "    }";

    public static final String getOrderRecordByAccount = "\"返回结果：\" +\n" +
            "            \"{\\n\" +\n" +
            "            \"  \\\"code\\\": 0,\\n\" +\n" +
            "            \"  \\\"msg\\\": \\\"success\\\",\\n\" +\n" +
            "            \"  \\\"data\\\": [\\n\" +\n" +
            "            \"    {\\n\" +\n" +
            "            \"      \\\"account\\\": \\\"300\\\",\\n\" +\n" +
            "            \"      \\\"customerOrderProfit\\\": \\\"9\\\",\\n\" +\n" +
            "            \"      \\\"clearAccProfit\\\": \\\"1\\\",\\n\" +\n" +
            "            \"      \\\"fee\\\": \\\"1\\\",\\n\" +\n" +
            "            \"      \\\"brokerProfit\\\": \\\"1\\\",\\n\" +\n" +
            "            \"      \\\"returnComm\\\": \\\"1\\\",\\n\" +\n" +
            "            \"      \\\"customerOrder\\\": \\\"11\\\",\\n\" +\n" +
            "            \"      \\\"clearAccOrderNo\\\": \\\"1\\\",\\n\" +\n" +
            "            \"      \\\"problemOrder\\\": false\\n\" +\n" +
            "            \"    },\\n\" +\n" +
            "            \"    {\\n\" +\n" +
            "            \"      \\\"account\\\": \\\"300\\\",\\n\" +\n" +
            "            \"      \\\"customerOrderProfit\\\": \\\"2\\\",\\n\" +\n" +
            "            \"      \\\"clearAccProfit\\\": \\\"2\\\",\\n\" +\n" +
            "            \"      \\\"fee\\\": \\\"2\\\",\\n\" +\n" +
            "            \"      \\\"brokerProfit\\\": \\\"2\\\",\\n\" +\n" +
            "            \"      \\\"returnComm\\\": \\\"1\\\",\\n\" +\n" +
            "            \"      \\\"customerOrder\\\": \\\"2\\\",\\n\" +\n" +
            "            \"      \\\"clearAccOrderNo\\\": \\\"2\\\",\\n\" +\n" +
            "            \"      \\\"problemOrder\\\": false\\n\" +\n" +
            "            \"    }\\n\" +\n" +
            "            \"  ]\\n\" +\n" +
            "            \"}\" +\n" +
            "            \"account:账号，customerOrderProfit：客户订单盈亏，clearAccProfit：清算账户盈亏,fee:手续费,brokerProfit:经纪商盈亏，returnComm：返佣，customerOrder：客户订单号，清算账户订单号：clearAccOrderNo，problemOrder：问题订单（true:是，false:不是）";
}
