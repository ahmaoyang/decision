package com.ry.cbms.decision.server.utils;

/**
 * @Author maoYang
 * @Date 2019/5/6 11:08
 * @Description 常量工具
 */
public class Constants {
    //public static final String checkModulePath = "E:/developWorksp/decision/src/main/resources/static/入金对账单模版.xlsx";//对账单模版地址

    public static final String checkModulePath="/usr/local/decision/src/main/resources/static/入金对账单模版.xlsx";//对账单模版地址(测试服)
    public static final int MIN_ID = 1;
    public static final String VALID = "1";
    public static final String PREFIX = "decision:";
    public static final String MT4_TOKEN = "mt4_token";
    public static final String DEFAULT_PWD = "abc123";//默认用户密码
    public static final int NOT_LOGIN = 401;
    public static final String localUrl="http://localhost:8081";
    public static final String NOT_LOGIN_MESSAGE = "未登录";
    public static final String SYSTEM_ADMIN_ROLE = "_admin_role"; // 系统管理员
    public static final String SECURITY_ADMIN_ROLE = "_security_role"; // 系统安全员
    public static final String AUDIT_ADMIN_ROLE = "_audit_role"; // 系统审计员

    public static final String HEART_BEAT = "heartBeat";
    public static final Integer check_Bill_SUCC = 1; // 对账成功

    public static final Integer check_Bill_FAIL = 0; //对账失败

    public static final Integer check_Bill_INVALID = 3;//对账单作废

    public static final Integer check_Bill_Channel_Pro = 4;//通道问题

    public static final Integer check_Bill_Cash_Out_More = 5;//出金金额系统多出


    public static final Integer check_Bill_Cash_Out_Channel_More= 6;//出金账单通道多出

    public static final Integer check_Bill_Bill_More = 7;//出金账单系统出

    public static final Integer check_Bill_Special= 8;//特殊用户金额



    public static final String rCheckKind = "1";//对账单类别 - 入金

    public static final String cCheckKind = "2";//对账单类别 - 出金

    //public static final String MT4_SERVER_URL = "http://192.168.5.240:8081/mt4/";

    public static final String MT4_SINGLE_TRADECOUNT_URL = "http://192.168.50.37:8081/mt4/user/openList";//获取所有持仓订单交易数量


    public static final String MT4_ALL_TRADEVAL_URL = "http://192.168.50.37:8081/mt4/user/symbolList";//获取所有交易品种的地址


    //public static final String MT4_SERVER_URL = "http://192.168.50.37:8081/mt4/";

    public static final String MT4_SERVER_URL = "http://192.168.50.37:8081/mt4/";

    public static final String TERMINUS_SERVER_URL = "http://192.168.50.66:8082/terminus/";

    public static final String TERMIMUS_LOG1 = "HEJ001";//Terminus 登陆账号1
    public static final String TERMIMUS_LOG2 = "HEJ411";//Terminus 登陆账号2


    public static final String ONLINE_SERVER_URL = "";
    public static final String GET_HOME_PAGR_DATA="getHomePageData";
    public static final String Mt4LoginServer = "192.168.5.118:9901";
    //public static final String MT4LoginServerReal = "120.78.138.122:443";
    public static final String MT4LoginServerReal = "120.78.138.122:443";

    public static final String Mt4LoginUserNameReal = "18001";

    public static final String Mt4LoginPasswordReal = "qqq1234";

    public static final String Mt4LoginUserName = "3";

    //public static final String Mt4LoginUserName = "5";

    public static final String Mt4LoginPassword = "abc123";

    public static final String Mt4ServerId = "1";

    public static final String RiskGroupName = "Risk";//证金的组
    public static final String SwapRiskGroupName = "Swap Risk";//保证金的组

    public static final String FLAG_DAY = "day";

    public static final String FLAG_MONTH = "month";

    public static final String FLAG_WEEK = "week"; //近七天

    public static final String FLAG_ALL = "all"; //全部

    public static final String wactchFlagOne = "1";//优先看异常风酬比账户

    public static final String wactchFlagTwo = "2";//优先看异常僵尸账户

    public static final String wactchFlagThree = "3";//优先看异常杠杆账户

    public static final String CURRENCY_RMB = "CNY"; //人民币种

    public static final String ACC_BROKER = "accBroker";//经济商账户

    public static final String ACC_BOND = "accBond";//保证金账户

    public static final String ACC_RETURN = "accReturn";//返佣账户

    public static final String ACC_RETAIL = "accRetail";//零售端账户

    public static final String EXPLOSION = "explosion";//爆仓备注

    public static final String ACCTYPE_COMM = "2";//佣金出金账户类型

    public static final String ACCTYPE_CASH = "1";//本金出金账户类型
    public static final String ACC_FROM_TYPE_COMM = "2";//佣金钱包转账

    public static final String LOG1 = "6000";//查询抛单信息的登录账号

    public static final String LOG2 = "6002";//查询抛单信息的登录账号

    public static final String RISKGROUP = "Cover";//风控自段组

    public static final String CLOSE_STATE = "3"; //平仓

    public static final String CLOSE_STATE_PART = "4";//部分平仓

    public static final String terminusDeposit = "Deposit";//入金

    public static final String terminusWithdrawal = "Withdrawal";//出金

    public static final String terminusTransFor = "Transfer";//出金

    public static final String ACC_TYPE_TWO = "2";
}
