package com.ry.cbms.decision.server.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.dao.AgentUserDao;
import com.ry.cbms.decision.server.dao.CashInAndOutDao;
import com.ry.cbms.decision.server.dao.CommissionDao;
import com.ry.cbms.decision.server.dao.RetailDao;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.vo.CurrencyVo;
import com.ry.cbms.decision.server.vo.SingleEvalDataVo;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author maoYang
 * @Date 2019/5/20 19:50
 * @Description 通用工具模块
 */
@Component
public class ComUtil {

    @Autowired
    private CommissionDao commissionDao;

    @Autowired
    private RetailDao retailDao;

    @Autowired
    private AgentUserDao agentUserDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CashInAndOutDao cashInAndOutDao;

    public Boolean isValidAcc(String userId) {
        Object cacheValue = redisTemplate.opsForHash ().get (RedisKeyGenerator.getAgentUserIdHash (), userId);
        if (null == cacheValue) {
            return false;
        }
        return true;
    }


    public void setLastRefreshTime(String endDate, String startDate, Map resultMap, String module) {
        String currDateTime;
        if (StringUtils.isEmpty (endDate) && StringUtils.isEmpty (startDate)) {
            currDateTime = DateUtil.parser (new Date ());
            if (null != resultMap) {
                resultMap.put ("lastRefreshTime", currDateTime);
            }
            redisTemplate.opsForValue ().set (RedisKeyGenerator.getLastRefreshTime (module), currDateTime);

        }
        Object cashValue = redisTemplate.opsForValue ().get (RedisKeyGenerator.getLastRefreshTime (module));
        if (!StringUtils.isEmpty (cashValue)) {
            if (null != resultMap) {
                resultMap.put ("lastRefreshTime", cashValue.toString ());
            }
        } else {
            if (null != resultMap) {
                resultMap.put ("lastRefreshTime", "");
            }
        }
    }

    /**
     * 邮箱格式验证
     *
     * @param emails
     * @return
     */
    public static Boolean checkEmail(List<String> emails) {
        //利用正则表达式验证邮箱是否符合邮箱的格式
        for (String email : emails) {
            if (!email.matches ("^\\w+@(\\w+\\.)+\\w+$")) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public static Boolean checkEmail(String email) {
        //利用正则表达式验证邮箱是否符合邮箱的格式
        if (!email.matches ("^\\w+@(\\w+\\.)+\\w+$")) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
     * 获取保证金余额（swap 和risk ）
     *
     * @return
     */
    public Map<String, Object> getBondBalance() {
        Object terToken = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTerminusToken ());//terminusToken
        if (null == terToken) {
            throw new GlobalException ("terToken is null");
        }
        Map<String, Object> resultMap = Maps.newHashMap ();//返回的体
        return resultMap;
    }

    public void doCycleForBond(Boolean isCurr) {
        Map<String, String> accMap = new HashMap ();
        accMap.put ("0", Constants.TERMIMUS_LOG1);
        accMap.put ("1", Constants.TERMIMUS_LOG2);
        for (int j = 0; j < 2; j++) {
            this.cycleBodyForBond (accMap, j, false, isCurr);
        }
    }

    public Map<String, Object> getForBondBalance() {
        Map<String, String> accMap = new HashMap ();
        Map<String, Object> retMap = new HashMap ();
        accMap.put ("0", Constants.TERMIMUS_LOG1);
        accMap.put ("1", Constants.TERMIMUS_LOG2);
        for (int j = 0; j < 2; j++) {
            retMap.putAll (this.cycleBodyForBond (accMap, j, true, false));
        }
        return retMap;
    }

    public Map<String, Object> cycleBodyForBond(Map<String, String> accMap, int j, Boolean flag, Boolean isCurr) {
        JSONObject response;//返回体
        Map<String, Object> retMap = new HashMap<> ();
        Object cacheTerminusToken = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTerminusToken ());
        String acc = accMap.get (String.valueOf (j));
        if (null != cacheTerminusToken) {
            String reqUrl = Constants.TERMINUS_SERVER_URL + "api/findHistoricCashBalance";
            BasicNameValuePair param1 = new BasicNameValuePair ("accountNo", acc);
            BasicNameValuePair param2 = new BasicNameValuePair ("date", (DateUtil.parserTo (DateUtil.getYesterDayStart ())));
            List<NameValuePair> nameValuePairList = new ArrayList<> ();
            nameValuePairList.add (param1);
            nameValuePairList.add (param2);
            response = HttpUtil2.doGet (reqUrl, cacheTerminusToken.toString (), nameValuePairList);
            if (null == response) {
                throw new GlobalException (CodeMsg.TERMINUS_DOWN.getMsg ());
            }
            if ("0".equals (response.getString ("code"))) { //请求成功
                JSONArray arr = response.getJSONArray ("data");
                if (null != arr && arr.size () > 0) {
                    JSONObject object = arr.getJSONObject (0);
                    if (flag) {
                        String balance = object.getString ("balance");
                        if (!StringUtils.isEmpty (balance)) {
                            retMap.put (acc, balance);
                        } else {
                            retMap.put (acc, "0");
                        }
                    } else {
                        if (isCurr) {
                            redisTemplate.opsForValue ().set (RedisKeyGenerator.getTermimusBalance (acc), object.getString ("balance"));
                        } else {
                            redisTemplate.opsForValue ().set (RedisKeyGenerator.getTermimusPreBalance (acc), object.getString ("balance"));
                        }
                    }
                }
            }
        } else {
            retMap.put (acc, "0");
        }
        return retMap;
    }

    /**
     * //全部币种类型
     *
     * @return
     */
    public List<CurrencyVo> getCurrencyKinds() {
        List<CurrencyVo> currencyKinds = cashInAndOutDao.currencyKinds (); //全部币种账户
        return currencyKinds;
    }


    public static Double DoubleValueOf(String arg) {
        if (StringUtils.isEmpty (arg)) {
            return 0.0;
        }
        return Double.valueOf (arg);
    }

    /**
     * 获取出入金手续费
     *
     * @return
     */
    public Map getCashInOrOutRate(String paymentType) {
        Map ratioMap = cashInAndOutDao.getCashInOrOutRate (paymentType, Constants.VALID);
        return ratioMap;
    }

    /**
     * 用分页查询出所有用户的Id
     *
     * @return
     */
    public List<String> getAllUserIds() {
        Integer limit = 1000;
        Integer countNum = agentUserDao.getAllAgentUserNum ();
        List<String> ids;
        List<String> allIds = new LinkedList<> ();
        Integer pageCount = (countNum + limit - 1) / limit;
        for (int i = 0; i < pageCount; i++) {
            Integer offset = i * limit;
            ids = agentUserDao.getAllAgentUser (offset, limit);
            allIds.addAll (ids);
            ids.clear ();
        }

        return allIds;
    }

    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (null == obj)
            return null;
        Map<String, Object> map = new HashMap ();
        BeanInfo beanInfo = Introspector.getBeanInfo (obj.getClass ());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors ();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName ();
            if (key.compareToIgnoreCase ("class") == 0) {
                continue;
            }
            Method getter = property.getReadMethod ();
            Object value = getter != null ? getter.invoke (obj) : null;
            map.put (key, value);
        }

        return map;
    }

    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null)
            return null;
        Object obj = beanClass.newInstance ();
        Field[] fields = obj.getClass ().getDeclaredFields ();
        for (Field field : fields) {
            int mod = field.getModifiers ();
            if (Modifier.isStatic (mod) || Modifier.isFinal (mod)) {
                continue;
            }
            field.setAccessible (true);
            field.set (obj, map.get (field.getName ()));
        }
        return obj;
    }

    /**
     * 获取不同币种的当前余额
     *
     * @param currency
     * @return
     */
    public Object getCurrentBalance(String currency, String userId) {
        BigDecimal accPreBalance;
        try {
            BigDecimal cashIn;
            BigDecimal cashOut;
            cashIn = cashInAndOutDao.getChannelCashInSumByCondition (null, currency, userId, null, null);
            cashOut = cashInAndOutDao.getChannelCashOutSumByCondition (null, currency, userId, null, null);
            if (null == cashIn) {
                cashIn = new BigDecimal (0);
            }
            if (null == cashOut) {
                cashOut = new BigDecimal (0);
            }
            accPreBalance = cashIn.subtract (cashOut);
        } catch (Exception e) {
            return 0;
        }
        return accPreBalance;
    }

    /**
     * 根据起止日期获取不同币种的余额
     *
     * @param currency
     * @return
     */
    public Object getMt4BalanceByTime(String currency, String mt4Acc, String startDate, String endDate) {
        BigDecimal accPreBalance;
        try {
            BigDecimal cashIn;
            BigDecimal cashOut;
            cashIn = cashInAndOutDao.getChannelCashInSumByCondition (mt4Acc, currency, null, startDate, endDate);
            cashOut = cashInAndOutDao.getChannelCashOutSumByCondition (mt4Acc, currency, null, startDate, endDate);
            if (null == cashIn) {
                cashIn = new BigDecimal (0);
            }
            if (null == cashOut) {
                cashOut = new BigDecimal (0);
            }
            accPreBalance = cashIn.subtract (cashOut);
        } catch (Exception e) {
            return 0;
        }
        return accPreBalance;
    }


    /**
     * 根据起止日期获取不同币种的余额
     *
     * @param currency
     * @return
     */
    public Object getBalanceByTime(String currency, String userId, String startDate, String endDate) {
        BigDecimal accPreBalance;
        try {
            BigDecimal cashIn;
            BigDecimal cashOut;
            cashIn = cashInAndOutDao.getChannelCashInSumByCondition (null, currency, userId, startDate, endDate);
            cashOut = cashInAndOutDao.getChannelCashOutSumByCondition (null, currency, userId, startDate, endDate);
            if (null == cashIn) {
                cashIn = new BigDecimal (0);
            }
            if (null == cashOut) {
                cashOut = new BigDecimal (0);
            }
            accPreBalance = cashIn.subtract (cashOut);
        } catch (Exception e) {
            return 0;
        }
        return accPreBalance;
    }

    /**
     * 根据mt4 账号 获取用户Id
     *
     * @param mt4Acc
     * @return
     */
    public String getUserIdByAcc(String mt4Acc) {
        String uid = agentUserDao.SelectUserIdByMt4Acc (mt4Acc);
        return uid;
    }

    public static String covSetToString(Set set) {
        List list = new ArrayList (set);
        return covListToString (list);
    }

    public static String covListToString(List list) {
        StringBuffer sb = new StringBuffer ();
        if (null != list && list.size () > 0) {
            int len = list.size ();
            for (int i = 0; i < len; i++) {
                sb.append (list.get (i));
                if (i < len - 1) {
                    sb.append (",");
                }
            }
        }
        return sb.toString ();
    }

    // map转换成list
    public static List mapTransitionList(Map map) {
        List list = new ArrayList ();
        Iterator iter = map.entrySet ().iterator (); // 获得map的Iterator
        while (iter.hasNext ()) {
            Map.Entry entry = (Map.Entry) iter.next ();
            //list.add(entry.getKey());
            list.add (entry.getValue ());
        }
        return list;
    }


    /**
     * 指定用户或代理下面所属全部账户 的用userIds
     *
     * @param userId
     * @return
     */
    public String getUserIds(String userId) {
        Set packUserIds = getUserIdSets (userId);
        String ids = ComUtil.covSetToString (packUserIds);
        return ids;
    }

    /**
     * 根据用户Id 获取下面用户的Id 集合
     *
     * @param userId
     * @return
     */
    public Set<String> getUserIdSets(String userId) {
        Set packUserIds = new HashSet ();
        Map retMap = commissionDao.selectStaffById (userId);
        if (null != retMap) {  //是员工
            List<Integer> ibIds = commissionDao.selectStaffIbs (userId);  //员工下的代理
            List<Integer> userIds = commissionDao.selectStaffRels (userId);  //员工下的直客
            for (Integer id : ibIds) {
                List<Map> ibMaps = commissionDao.selectIbUserByCondition (id.toString ());//每个会员下的所有儿孙代理
                for (Map ibMap : ibMaps) {
                    packUserIds.add (ibMap.get ("ibId"));
                }
            }
            packUserIds.add (userIds);
        } else {  //如果是代理
            List<Map> ibMaps = commissionDao.selectIbUserByCondition (userId);//代理下的所有儿孙代理
            for (Map ibMap : ibMaps) {
                packUserIds.add (ibMap.get ("ibId"));
                packUserIds.add (ibMap.get ("userId"));
            }
        }
        packUserIds.add (userId);
        return packUserIds;
    }


    /**
     * 根据 指定用户或代理下面所属全部账户
     *
     * @param userId
     * @return
     */
    public String getMt4AccountsByUserId(String userId) {
        String accounts = null;
        Set set = getMt4AccSetByUserId (userId);
        if (null != set && set.size () > 0) {
            accounts = covSetToString (set);
        }
        return accounts;
    }

    public Set getMt4AccSetByUserId(String userId) {
        String ids = getUserIds (userId);
        Set set = new HashSet ();
        if (!StringUtils.isEmpty (ids)) {
            List accountList = getAccList (ids);
            if (null != accountList && accountList.size () > 0) {
                set = new HashSet<> (accountList);
            }
        }
        return set;

    }

    public List getAccList(String userIds) {
        if (StringUtils.isEmpty (userIds)) {
            return null;
        }
        List accountList = retailDao.SelectMt4AccsByUserId (userIds);
        List removeList = new ArrayList ();
        accountList.forEach (it -> {
            if (StringUtils.isEmpty (it)) {
                removeList.add (it);
            }
        });
        accountList.removeAll (removeList);
        return accountList;
    }

    public static void setPageParam(PageTableRequest request, Integer offset, Integer
            limit, Map<String, Object> paramMap) {
        if (null == offset) {
            offset = 0;
        } else if (offset > 0) {
            offset = offset - 1;
        }
        if (null == limit) {
            limit = 10;
        }
        request.setLimit (limit);
        request.setOffset (offset * limit);
        request.setParams (paramMap);
    }


    public static void setPageParamMap(Map paramMap, String beginTime, String endTime) {
        paramMap.put ("beginTime", beginTime);
        paramMap.put ("endTime", endTime);
    }

    /**
     * 获取Mt4 账户余额
     *
     * @param mt4Acc
     * @return
     */
    public Double getMt4AccBalance(String mt4Acc) {
        String reqUrl = Constants.MT4_SERVER_URL + "user/userRecordsRequest";
        Object cacheToken = redisTemplate.opsForValue ().get (RedisKeyGenerator.getMT4Token ());
        Double balance = 0.0;
        BasicNameValuePair nameValuePair1 = new BasicNameValuePair ("logins", mt4Acc);
        List<NameValuePair> nameValuePairList = new ArrayList<> ();
        nameValuePairList.add (nameValuePair1);
        JSONObject res = HttpUtil2.doGet (reqUrl, cacheToken.toString (), nameValuePairList);
        if ("0".equals (res.getString ("code"))) {
            JSONArray dataArr = res.getJSONArray ("data");
            if (null != dataArr && dataArr.size () > 0) {
                JSONObject data = dataArr.getJSONObject (0);
                balance = data.getDouble ("balance");
            }
        }
        return balance;

    }

    public String getTerminusToken() {
        Object terminusToken = redisTemplate.opsForValue ().get (RedisKeyGenerator.getTerminusToken ());
        if (null != terminusToken) {
            return terminusToken.toString ();
        }
        return null;
    }

    public static boolean isDigit(String str) {
        boolean isDigit = false;//用来表示是否包含数字
        for (int i = 0; i < str.length (); i++) { //循环遍历字符串
            if (Character.isDigit (str.charAt (i))) {     //用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            }
        }
        if (isDigit) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否字母和数字
     *
     * @param str
     * @return
     */

    public static boolean isLetterDigit(String str) {
        boolean isDigit = false;//用来表示是否包含数字
        boolean isLetter = false;//用来表示是否包含字母
        for (int i = 0; i < str.length (); i++) { //循环遍历字符串
            if (Character.isDigit (str.charAt (i))) {     //用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            }
            if (Character.isLetter (str.charAt (i))) {   //用char包装类中的判断字母的方法判断每一个字符
                isLetter = true;
            }
        }
        if (isDigit || isLetter) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isLetter(String str) {
        boolean isLetter = false;//用来表示是否包含字母
        for (int i = 0; i < str.length (); i++) { //循环遍历字符串
            if (Character.isLetter (str.charAt (i))) {   //用char包装类中的判断字母的方法判断每一个字符
                isLetter = true;
            }
        }
        if (isLetter) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否是日期判断
     *
     * @param date
     * @return
     */
    public static boolean isDate(String date) {
        Pattern p = Pattern.compile ("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))?$");
        return p.matcher (date).matches ();
    }

    /**
     * 是否是时间判断
     *
     * @param
     * @return
     */
    public static boolean isTime(String time) {
        Pattern p = Pattern.compile ("((((0?[0-9])|([1][0-9])|([2][0-4]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
        return p.matcher (time).matches ();
    }


    /**
     * 类的初始化
     *
     * @param object
     * @return
     */
    public static Object initial(Object object) {
        Field[] fields = object.getClass ().getDeclaredFields ();
        for (Field field : fields) {
            field.setAccessible (true);
            Class<?> type = field.getType ();
            if ("int".equals (type.getName ())) {
                try {
                    field.setInt (object, 0);
                } catch (IllegalAccessException e) {
                }
            } else if ("double".equals (type.getName ())) {
                try {
                    field.setDouble (object, 0d);
                } catch (IllegalAccessException e) {
                }
            } else if ("float".equals (type.getName ())) {
                try {
                    field.setFloat (object, 0f);
                } catch (IllegalAccessException e) {
                }
            } else if ("long".equals (type.getName ())) {
                try {
                    field.setLong (object, 0L);
                } catch (IllegalAccessException e) {
                }
            } else if ("short".equals (type.getName ())) {
                try {
                    field.setShort (object, (short) 0);
                } catch (IllegalAccessException e) {
                }
            } else if ("boolean".equals (type.getName ())) {
                try {
                    field.setBoolean (object, false);
                } catch (IllegalAccessException e) {
                }
            } else if ("java.lang.String".equals (type.getName ())) {
                try {
                    field.set (object, "0");
                } catch (IllegalAccessException e) {
                }
            } else {
                try {
                    field.set (object, null);
                } catch (IllegalAccessException e) {
                }
            }

        }
        return object;
    }

    public  void orderByDateForHashMap(List<Map> collection) {
        collection.sort ((a, b) -> {
            try {
            Date dateA = DateUtil.parse (a.get ("createDate").toString ());
            Date dateB = DateUtil.parse (b.get ("createDate").toString ());
                if (dateA.before (dateB)) {
                    return 1;
                }
                if (dateA.before (dateB)) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                return 1;
            }
        });
    }

    public  void orderByDate(List<SingleEvalDataVo> collection) {
        collection.sort ((a, b) -> {
            Date dateA = DateUtil.parse (a.getCreateDate ());
            Date dateB = DateUtil.parse (b.getCreateDate ());
            try {
                if (dateA.before (dateB)) {
                    return 1;
                }
                if (dateA.before (dateB)) {
                    return -1;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                return 1;
            }
        });
    }

    /**
     * 计算佣金出金展示集合(mt4)
     *
     * @param startDate
     * @param endDate
     * @param mt4Acct
     * @return
     */
    public List<Map> getCommOutList(String startDate, String endDate, String mt4Acct) {
        List<String> ibIds = commissionDao.selectIbIdsByMt4Acc (startDate, endDate, mt4Acct); //查询当前用户的所有上级

        List<Map> mt4CommOutList = new ArrayList ();
        ibIds.forEach (ibid -> {
            List<Map> comOutList = commissionDao.getCommOutList (startDate, endDate, ibid);//出金记录
            for(int i=0,len=comOutList.size ();i<len;i++){
                Map comm=comOutList.get (i);
                String overDate=comm.get ("operaterTime").toString ();
                BigDecimal sumCommMt4 = commissionDao.sumCommByMt4(ibid, mt4Acct, null,overDate );
                BigDecimal sumCommNum = commissionDao.sumComm(ibid, null, overDate);
                if (null == sumCommMt4) {
                    sumCommMt4 = new BigDecimal (0.0);
                }
                if (null == sumCommNum) {
                    sumCommNum = new BigDecimal (0.0);
                }
                Double ratio = Double.parseDouble (sumCommMt4.toString ()) / Double.parseDouble (sumCommNum.toString ());//比率
                Object actOutAmount = comm.get ("actualToAcc");//佣金实际出金
                if (null == actOutAmount) {
                    actOutAmount = 0;
                }
                Double commOut = Double.valueOf (actOutAmount.toString ()) * ratio;
                Object moneyRmb = comm.get ("moneyRmb");//佣金实际出金
                if (null == moneyRmb) {
                    moneyRmb = 0;
                }

                Object platformPayFe = comm.get ("platformPayFe");
                if (null == platformPayFe) {
                    platformPayFe = 0;
                }
                Object exRate = comm.get ("exRate");
                if (null == exRate) {
                    exRate = 0;
                }
                Object payFee = comm.get ("payFee");
                if (null == payFee) {
                    payFee = 0;
                }
                Double moneyRmb2 = Double.valueOf (moneyRmb.toString ()) * ratio;
                Double actPayComm = moneyRmb2 * Double.valueOf (exRate.toString ()) - Double.valueOf (payFee.toString ()) - Double.valueOf (platformPayFe.toString ());
                comm.put ("actualToAcc", commOut);
                comm.put ("moneyRmb", moneyRmb2);
                comm.put ("actPay", actPayComm);
                mt4CommOutList.add (comm);
            }
        });
        return mt4CommOutList;
    }

    /**
     * 佣金出金
     *
     * @param userId 用户id
     */
    public BigDecimal commOutCal(String userId) {
        Map commIbMap = commissionDao.selectStaffCommByUserId (userId);
        Map commStaffMap = commissionDao.selectIbCommByUserId (userId);
        BigDecimal sumCommision = new BigDecimal (0.0);
        Object commIbComm = null;
        Object commStaffComm = null;
        if (null != commIbMap) {
            commIbComm = commIbMap.get ("sumCommission");
        }
        if (null != commStaffMap) {
            commStaffComm = commStaffMap.get ("sumCommission");
        }
        if (null == commIbComm) {
            commIbComm = 0;
        }
        if (null == commStaffComm) {
            commStaffComm = 0;
        }
        sumCommision.add (new BigDecimal (commIbComm.toString ())).add (new BigDecimal (commStaffComm.toString ()));
        return sumCommision;
    }

    /**
     * 计算每个的比率
     */
    private Map<String, Double> calCommRatio(Map commMap, BigDecimal sumCommision) {
        Double commTotal = new Double (sumCommision.toString ());
        commMap.forEach ((k, v) -> {
            Double valueComm = Double.valueOf (v.toString ());
            Double ratio = valueComm / commTotal;
            commMap.put (k, ratio); //获取每个人的佣金出金比例
        });
        return commMap;
    }

    public static void main(String[] args) {

    }

}

