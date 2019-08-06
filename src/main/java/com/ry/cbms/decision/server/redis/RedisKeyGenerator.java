package com.ry.cbms.decision.server.redis;

import com.ry.cbms.decision.server.utils.Constants;

/**
 * Created by maoYang on 2019/4/30
 * 对redis的namespace和key的定义
 */
public class RedisKeyGenerator {

    public static final String LOAD_PROFIT_AND_LOSS_TOTAL = "loadProfitAndLossTotalData";//总的负债收益

    public static final String BOND_BALANCE_RECORD = "bondBalanceRecord:";//保证金账户余额

    public static final String BROKER_BALANCE_RECORD = "brokerBalanceRecord:";//经纪商账户余额

    public static final String LOAD_PROFIT_AND_LOSS_CONDITION = "loadProfitAndLossCondition:";//负债收益(条件)

    public static final String LOAD_REMOTE_BOND_INFO_FLAG = "loadRemoteBondInfoFlag";//更新保证金
    public static final String TOKEN = "tokens:";
    private static final String loadOrderDetail = "loadOrderDetail:"; //加载订单信息
    private static final String loadOrderDetailLastTime = "loadOrderDetailLastTime "; //加载订单信息上次时间
    private static final String retailAccountDollarBalanceHash = "retailAccountDollarBalance:";
    private static final String forTwoWeekTrade = "forTwoWeekTradeTrue";//完成连续两周交易
    private static final String geAccDataByDate = "geAccDataByDate";
    private static final String forTrade = "forTrade";//连续交易标志
    private static final String throwInfoHis = "throwInfoHis";
    private static final String brokenAccHis = "brokenAccHis ";//经济商账户历史记录
    private static final String brokenUserIdHis = "brokenUserIdHis";//经济商账户历史记录

    private static final String bondInfoHis = "bondInfoHis";

    private static final String clearAccountInfoHis = "clearAccountInfoHis";
    private static final String retailHisSum = "retailHisSum";
    private static final String lastRefreshTime = "lastRefreshTime:"; //上次刷新时间(要和模块相结合)

    private static final String BondInfoHead = "bondInfo";
    private static final String USER_DISABLED = "userDisable";

    private static final String USER_LOCK = "userLOCK";

    private static final String PREFIX = Constants.PREFIX;

    private static final String COMM_PRE_BALANCE = "commPreviousBalance:";//佣金上期余额

    private static final String TERMINUS_TOKEN = "terminusToken";

    private static final String TERMIMUS_PRE_BALANCE = "terminusPreBalance:"; //terminus 保证金上期余额


    private static final String TERMIMUS_BALANCE = "terminusBalance:"; //terminus 保证金余额

    private static final String PRE_RETAIL_BALANCE = "preRetailBalance:";//上期零售段余额

    private static final String RETAIL_BALANCE = "retailBalance:";//本期零售端余额

    private static final String HAVE_PRO_ORDER_ACC = "haveProblemOrderACC";//有问题订单的账号


    private static final String CUR_EQUITY = "currentEquity:";//本期零售端净值

    private static final String AGENT_USER_IDS = "AgentUser:agentUserIds";//所有投资人Id集合


    private static final String AGENT_USER_ID_HASH = "AgentUser:agentUserHash";//所有投资人Id hash


    private static final String USER_TRADE_FLAG = "userTradeFlag";//用户连续十四天交易达标标签

    private static final String ACCOUNT_PRE_BALANCE = "accountPreBalance";//账户上期余额

    private static final String ACCOUNT_CUR_BALANCE = "accountCurBalance";//账户本期余额

    private static final String ACCOUNT_PRE_BALANCE_FOR_MT4 = "accountPreBalanceForMt4";//账户上期余额(mt4)


    private static final String ACCOUNT_CURR_BALANCE_FOR_MT4 = "accountCurBalanceForMt4";//账户上期余额(mt4)

    private static final String ACCOUNT_BALANCE = "accountBalance";//账户余额


    private static final String HOME_PAGE_DATA = "homePageData:";//首页数据

    private static final String HOME_PAGE_DETAILS = "homePageDetails:";//首页图像展示数据

    private static final String EXPLODE = "explode:"; //爆仓

    private static final String CASHED_BALANCE = "cashedBalance:";//兑付余额

    private static final String LOG_OUT_STATUS = "logoutStatus：";//登出状态

    public static final String GET_THROW_FLAG = "throwFlag";//本地获取到抛单信息标识

    public static final String LOAD_ORDER_RECORD_BY_ACCOUNT = "loadOrderRecordByAccount:";//特定账号的订单记录

    private static final String AGENT_USER_ACCLIST = "AgentUser:agentUserAccList";//所有投资人Id集合

    private static final String ORDER_DETAIL_ACC = "OrderDetailAcc:";//所有投资人Id集合

    private static final String ALL_ORDER_DETAILS = "allOrderDetails";


    private static final String OrderEvaBalance = "OrderEvaBalance:";



    public static String getUserDisabled(String flag) {
        return PREFIX + USER_DISABLED + flag;
    }

    public static String getUserLock(String flag) {
        return PREFIX + USER_LOCK + flag;
    }

    public static String getCommPreBalance(String userId) {
        return PREFIX + COMM_PRE_BALANCE + "_" + userId;
    }

    public static String getCommPreBalance() {
        return PREFIX + COMM_PRE_BALANCE;
    }

    public static String getTerminusToken() {
        return PREFIX + TERMINUS_TOKEN;
    }

    public static String getMT4Token() {
        return Constants.PREFIX + Constants.MT4_TOKEN;
    }

    public static String getTermimusPreBalance(String group) {
        return PREFIX + TERMIMUS_PRE_BALANCE + group;
    }

    public static String getTermimusBalance(String group) {
        return PREFIX + TERMIMUS_BALANCE + group;
    }

    public static String getPreRetailBalance(String acc) {
        return PREFIX + PRE_RETAIL_BALANCE + acc;
    }

    public static String getPreRetailBalance() {
        return PREFIX + PRE_RETAIL_BALANCE;
    }

    public static String getRetailBalance(String acc) {
        return PREFIX + RETAIL_BALANCE + acc;
    }

    public static String getRetailBalance() {
        return PREFIX + RETAIL_BALANCE;
    }

    public static String getCurEquity(String acc) {
        return PREFIX + CUR_EQUITY + acc;
    }

    public static String getCurEquity() {
        return PREFIX + CUR_EQUITY;
    }

    public static String getAgentUserIds() {
        return PREFIX + AGENT_USER_IDS;
    }

    public static String getAgentUserAccList() {
        return PREFIX + AGENT_USER_ACCLIST;
    }

    public static String getUserTradeFlag(String userId) {
        return PREFIX + USER_TRADE_FLAG + ":" + userId;
    }

    public static String getAccountPreBalance(String currency) {
        return PREFIX + ACCOUNT_PRE_BALANCE + ":" + currency;
    }

    public static String getAccountBalance(String currency) {
        return PREFIX + ACCOUNT_BALANCE + ":" + currency;
    }

    public static String getAccountPreBalanceForP(String currency, String userId) {
        return PREFIX + ACCOUNT_PRE_BALANCE + ":" + currency + ":" + userId;
    }

    public static String getHomePageData(String currency, String flag) {
        return PREFIX + HOME_PAGE_DATA + currency + flag;
    }

    public static String getHomePageDetails(String currency, String flag, String accType) {
        return PREFIX + HOME_PAGE_DETAILS + currency + flag + accType;
    }

    public static String getExplode11(String mt4Acc, String currency) { //mt4账号，币种
        return PREFIX + EXPLODE + currency + mt4Acc;
    }

    public static String getCashedBalance(String mt4Acc, String currency) {
        return PREFIX + CASHED_BALANCE + currency + mt4Acc;
    }

    public static String getGetThrowFlag(String dataFlag) {
        return PREFIX + GET_THROW_FLAG + dataFlag;
    }

    public static String getOrderDetailAcc(String acc) {
        return PREFIX + ORDER_DETAIL_ACC + acc;
    }

    public static String getBondInfoHis() {
        return PREFIX + bondInfoHis;
    }

    public static String getClearAccountInfoHis() {
        return PREFIX + clearAccountInfoHis;
    }

    public static String getRetailHisSum(String account) {
        return PREFIX + retailHisSum + account;
    }

    public static String getLastRefreshTime(String module) {
        return PREFIX + lastRefreshTime + module;
    }

    public static String getBondInfoHead() {
        return PREFIX + BondInfoHead;
    }

    public static String getThrowInfoHis(String account) {
        return PREFIX + throwInfoHis + account;
    }

    public static String getCashedBalance() {
        return CASHED_BALANCE;
    }

    public static String getBrokenAccHis(String currency, String account) {
        return PREFIX + brokenAccHis + currency + account;
    }

    public static String getForTwentyTrade(String id) {
        return PREFIX + forTwoWeekTrade + id;
    }

    public static String getForTrade(String id) {
        return PREFIX + forTrade + id;
    }

    public static String getGeAccDataByDate(String currency, String accType, String startDate, String endDate) {
        return PREFIX + geAccDataByDate + currency + accType + startDate + endDate;
    }

    public static String getAgentUserIdHash() {
        return PREFIX + AGENT_USER_ID_HASH;
    }

    public static String getRetailAccountDollarBalanceHash(String flag) {
        return PREFIX + retailAccountDollarBalanceHash + flag;
    }

    public static String getLogOutStatus(String token) {
        return PREFIX + LOG_OUT_STATUS + token;
    }

    public static String getLoadOrderDetail(String flag) {
        return PREFIX + loadOrderDetail + flag;
    }

    public static String getLoadOrderDetailLastTime() {
        return loadOrderDetailLastTime;
    }

    public static String getTOKEN() {
        return PREFIX + TOKEN;
    }

    public static String getLoadOrderRecordByAccount(String startDate, String endDate) {
        return PREFIX + LOAD_ORDER_RECORD_BY_ACCOUNT + startDate + endDate;
    }

    public static String getHaveProOrderAcc() {
        return PREFIX + HAVE_PRO_ORDER_ACC;
    }

    public static String getAllOrderDetails() {
        return PREFIX + ALL_ORDER_DETAILS;
    }

    public static String getLoadProfitAndLossTotal() {
        return PREFIX + LOAD_PROFIT_AND_LOSS_TOTAL;
    }

    public static String getLoadProfitAndLossCondition(String flag) {
        return PREFIX + LOAD_PROFIT_AND_LOSS_CONDITION+flag;
    }

    public static String getBrokenUserIdHis(String currency, String userId) {
        return PREFIX + brokenUserIdHis + currency + "userId" + userId;
    }

    public static String getLoadRemoteBondInfoFlag() {
        return PREFIX + LOAD_REMOTE_BOND_INFO_FLAG;
    }

    public static String getAccountPreBalanceForMt4(String currency, String mt4Acc) {
        return PREFIX + ACCOUNT_PRE_BALANCE_FOR_MT4 + "currency" + currency + "mt4Acc" + mt4Acc;
    }
    public static String getAccountCurrBalanceForMt4(String currency, String mt4Acc) {
        return PREFIX + ACCOUNT_CURR_BALANCE_FOR_MT4 + "currency" + currency + "mt4Acc" + mt4Acc;
    }

    public static String getAccountCurBalance(String currency, String UserId) {
        return PREFIX + ACCOUNT_CUR_BALANCE+"currency" + currency + "UserId" + UserId;
    }

    public static String getOrderEvaBalance(String flag,String currency) {
        return PREFIX + OrderEvaBalance+flag+":"+currency;
    }

    public static String getBondBalanceRecord(String dateFlag) {
        return PREFIX +BOND_BALANCE_RECORD+dateFlag;
    }

    public static String getBrokerBalanceRecord(String currency) {
        return PREFIX +BROKER_BALANCE_RECORD+currency;
    }
}
