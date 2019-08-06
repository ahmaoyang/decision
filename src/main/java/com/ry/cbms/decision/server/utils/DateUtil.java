package com.ry.cbms.decision.server.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author maoyang
 * @Date 2019/5/05
 */
public class DateUtil {
    //获取当天的开始时间
    public static Date getDayBegin() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.set (Calendar.HOUR_OF_DAY, 0);
        cal.set (Calendar.MINUTE, 0);
        cal.set (Calendar.SECOND, 0);
        cal.set (Calendar.MILLISECOND, 0);
        return cal.getTime ();
    }


    //获取当前日期是第几月
    public static Integer getMonth(Date date) {
        int month = date.getMonth ();
        return month;
    }

    //获取当天的结束时间
    public static Date getDayEnd() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.set (Calendar.HOUR_OF_DAY, 23);
        cal.set (Calendar.MINUTE, 59);
        cal.set (Calendar.SECOND, 59);
        return cal.getTime ();
    }

    //获取昨天的开始时间
    public static Date getBeginDayOfYesterday() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.setTime (getDayBegin ());
        cal.add (Calendar.DAY_OF_MONTH, -1);
        return cal.getTime ();
    }

    //获取昨天的结束时间
    public static Date getEndDayOfYesterDay() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.setTime (getDayEnd ());
        cal.add (Calendar.DAY_OF_MONTH, -1);
        return cal.getTime ();
    }

    //获取明天的开始时间
    public static Date getBeginDayOfTomorrow() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.setTime (getDayBegin ());
        cal.add (Calendar.DAY_OF_MONTH, 1);

        return cal.getTime ();
    }

    //获取明天的结束时间
    public static Date getEndDayOfTomorrow() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.setTime (getDayEnd ());
        cal.add (Calendar.DAY_OF_MONTH, 1);
        return cal.getTime ();
    }

    //获取本周的开始时间
    public static Date getBeginDayOfWeek() {
        Date date = new Date ();
        if (date == null) {
            return null;
        }
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = Calendar.getInstance ();
        cal.setTime (date);
        int dayofweek = cal.get (Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add (Calendar.DATE, 2 - dayofweek);
        return getDayStartTime (cal.getTime ());
    }

    //获取本周的结束时间
    public static Date getEndDayOfWeek() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = Calendar.getInstance ();
        cal.setTime (getBeginDayOfWeek ());
        cal.add (Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime ();
        return getDayEndTime (weekEndSta);
    }

    //获取本月的开始时间
    public static Date getBeginDayOfMonth() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar calendar = Calendar.getInstance ();
        calendar.set (getNowYear (), getNowMonth () - 1, 1);
        return getDayStartTime (calendar.getTime ());
    }

    //获取本月的结束时间
    public static Date getEndDayOfMonth() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar calendar = Calendar.getInstance ();
        calendar.set (getNowYear (), getNowMonth () - 1, 1);
        int day = calendar.getActualMaximum (5);
        calendar.set (getNowYear (), getNowMonth () - 1, day);
        return getDayEndTime (calendar.getTime ());
    }

    //获取本年的开始时间
    public static Date getBeginDayOfYear() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = Calendar.getInstance ();
        cal.set (Calendar.YEAR, getNowYear ());
        // cal.set
        cal.set (Calendar.MONTH, Calendar.JANUARY);
        cal.set (Calendar.DATE, 1);

        return getDayStartTime (cal.getTime ());
    }

    //获取本年的结束时间
    public static Date getEndDayOfYear() {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = Calendar.getInstance ();
        cal.set (Calendar.YEAR, getNowYear ());
        cal.set (Calendar.MONTH, Calendar.DECEMBER);
        cal.set (Calendar.DATE, 31);
        return getDayEndTime (cal.getTime ());
    }

    //获取某个日期的开始时间
    public static Timestamp getDayStartTime(Date d) {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar calendar = Calendar.getInstance ();
        if (null != d) calendar.setTime (d);
        calendar.set (calendar.get (Calendar.YEAR), calendar.get (Calendar.MONTH), calendar.get (Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set (Calendar.MILLISECOND, 0);
        return new Timestamp (calendar.getTimeInMillis ());
    }

    //获取某个日期的结束时间
    public static Timestamp getDayEndTime(Date d) {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar calendar = Calendar.getInstance ();
        if (null != d) calendar.setTime (d);
        calendar.set (calendar.get (Calendar.YEAR), calendar.get (Calendar.MONTH), calendar.get (Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set (Calendar.MILLISECOND, 999);
        return new Timestamp (calendar.getTimeInMillis ());
    }

    //获取今年是哪一年
    public static Integer getNowYear() {
        Date date = new Date ();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance ();
        gc.setTime (date);
        return Integer.valueOf (gc.get (1));
    }

    //获取本月是哪一月
    public static int getNowMonth() {
        Date date = new Date ();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance ();
        gc.setTime (date);
        return gc.get (2) + 1;
    }

    //两个日期相减得到的天数
    public static int getDiffDays(Date beginDate, Date endDate) {

        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException ("getDiffDays param is null!");
        }

        long diff = (endDate.getTime () - beginDate.getTime ())
                / (1000 * 60 * 60 * 24);

        int days = new Long (diff).intValue ();

        return days;
    }

    //两个日期相减得到的毫秒数
    public static long dateDiff(Date beginDate, Date endDate) {
        long date1ms = beginDate.getTime ();
        long date2ms = endDate.getTime ();
        return date2ms - date1ms;
    }

    //获取两个日期中的最大日期
    public static Date max(Date beginDate, Date endDate) {
        if (beginDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return beginDate;
        }
        if (beginDate.after (endDate)) {
            return beginDate;
        }
        return endDate;
    }

    //获取两个日期中的最小日期
    public static Date min(Date beginDate, Date endDate) {
        if (beginDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return beginDate;
        }
        if (beginDate.after (endDate)) {
            return endDate;
        }
        return beginDate;
    }

    //返回某月该季度的第一个月
    public static Date getFirstSeasonDate(Date date) {
        final int[] SEASON = {1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4};
        Calendar cal = Calendar.getInstance ();
        cal.setTime (date);
        int sean = SEASON[cal.get (Calendar.MONTH)];
        cal.set (Calendar.MONTH, sean * 3 - 3);
        return cal.getTime ();
    }

    //返回某个日期下几天的日期
    public static Date getNextDay(Date date, int i) {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.setTime (date);
        cal.set (Calendar.DATE, cal.get (Calendar.DATE) + i);
        return cal.getTime ();
    }

    //返回某个日期前几天的日期
    public static Date getFrontDay(Date date, int i) {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar cal = new GregorianCalendar ();
        cal.setTime (date);
        cal.set (Calendar.DATE, cal.get (Calendar.DATE) - i);
        return cal.getTime ();
    }

    //获取某年某月到某年某月按天的切片日期集合（间隔天数的日期集合）
    public static List getTimeList(int beginYear, int beginMonth, int endYear,
                                   int endMonth, int k) {
        List list = new ArrayList ();
        if (beginYear == endYear) {
            for (int j = beginMonth; j <= endMonth; j++) {
                list.add (getTimeList (beginYear, j, k));

            }
        } else {
            {
                for (int j = beginMonth; j < 12; j++) {
                    list.add (getTimeList (beginYear, j, k));
                }

                for (int i = beginYear + 1; i < endYear; i++) {
                    for (int j = 0; j < 12; j++) {
                        list.add (getTimeList (i, j, k));
                    }
                }
                for (int j = 0; j <= endMonth; j++) {
                    list.add (getTimeList (endYear, j, k));
                }
            }
        }
        return list;
    }

    //获取某年某月按天切片日期集合（某个月间隔多少天的日期集合）
    public static List getTimeList(int beginYear, int beginMonth, int k) {
        List list = new ArrayList ();
        Calendar begincal = new GregorianCalendar (beginYear, beginMonth, 1);
        int max = begincal.getActualMaximum (Calendar.DATE);
        for (int i = 1; i < max; i = i + k) {
            list.add (begincal.getTime ());
            begincal.add (Calendar.DATE, k);
        }
        begincal = new GregorianCalendar (beginYear, beginMonth, max);
        list.add (begincal.getTime ());
        return list;
    }


    public static Date parser(String time) {
        if (time == null || time.equals ("")) return null;
        try {
            Date date = DateUtils.parseDate (time, "yyyy-MM-dd");
            return date;
        } catch (ParseException e) {
            throw new RuntimeException ("日期转换失败");
        }
    }

    public static String parserTo(String time) {
        if (time == null || time.equals ("")) return null;
        return time.substring (0, 10);
    }

    public static Date stringParserToDate(String time) {
        if (time == null || time.equals ("")) return null;
        Date date = null;
        try {
            date = new SimpleDateFormat ("yyyy-MM-dd").parse (time);
        } catch (ParseException e) {
            e.printStackTrace ();
        }
        return date;

    }

    public static String parser(Date time) {
        return new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format (time);
    }
    public static String parserTo(Date time) {
        return new SimpleDateFormat ("yyyy-MM-dd").format (time);
    }
    public static String parserTo01(Date time) {
        return new SimpleDateFormat ("yyyy-MM-01").format (time);
    }


    public static String getyyyyMMdd() {
        return new SimpleDateFormat ("yyyy-MM-dd").format (new Date ());
    }

    public static String getyyyyMM() {
        return new SimpleDateFormat ("yyyy-MM-00").format (new Date ());
    }

    public static String getyyyy() {
        return new SimpleDateFormat ("yyyy").format (new Date ());
    }
    /**
     * 把日期往后增加一天.整数往后推,负数往前移动
     *
     * @param from
     * @param day
     * @return
     */
    public static Date addOrReduceDay(Date from, Integer day) {
        Calendar calendar = new GregorianCalendar ();
        calendar.setTime (from);
        calendar.add (calendar.DATE, day);//把日期往后增加一天.整数往后推,负数往前移动
        Date date = calendar.getTime ();   //这个时间就是日期往后推一天的结果
        return date;
    }


    public static Date removeHHmmss(Date date) {
        date.setHours (0);
        date.setMinutes (0);
        date.setSeconds (0);
        return date;
    }

    public static Date firstMonthDay(Date date) {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime (date);
        calendar.set (Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = calendar.getTime ();
        return firstDayOfMonth;
    }

    public static Date lastMonthDay(Date date) {
        TimeZone.setDefault (TimeZone.getTimeZone ("GMT+8"));
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime (date);
        calendar.add (Calendar.MONTH, 1);
        calendar.add (Calendar.DAY_OF_MONTH, -1);
        Date lastDayOfMonth = calendar.getTime ();
        return lastDayOfMonth;
    }

    public static Integer getReduceDay(Date end, Date start) {
        if (end.getTime () < start.getTime ()) {
            throw new IllegalArgumentException ("结束时间不可小于开始时间");
        }
        long m = end.getTime () - start.getTime ();
        Double dayd = m / (double) (1000 * 60 * 60 * 24);
        return dayd.intValue ();
    }

    public static Date getNowDayEnd() {
        Date date = removeHHmmss (addOrReduceDay (new Date (), 1));
        date.setTime (date.getTime () - 1);
        return date;
    }

    public static Date getNowDayStart() {
        return removeHHmmss (new Date ());
    }

    public static Date getYesterDayStart() {
        Date date = new Date ();
        date = addOrReduceDay (date, -1);
        date = removeHHmmss (date);
        return date;
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(Long seconds, String format) {
        if (seconds == null || seconds.equals ("null")) {
            return "";
        }
        if (format == null || format.isEmpty ()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat (format);

        return sdf.format (new Date (seconds));
    }

    /**
     * 获取当前时间的整点小时时间
     *
     * @return
     */
    public static String getCurrHourTime() {
        Calendar ca = Calendar.getInstance ();
        ca.set (Calendar.MINUTE, 0);
        ca.set (Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        return sdf.format (ca.getTime ());
    }

    /**
     * 获取当前时间的整点小时时间戳
     *
     * @return
     */
    public static Long getCurrHourTimeStamp() {
        Calendar ca = Calendar.getInstance ();
        ca.set (Calendar.MINUTE, 0);
        ca.set (Calendar.SECOND, 0);
        return ca.getTime ().getTime ();
    }

    /**
     * 获取当前时间的上一个整点时间
     *
     * @param
     */
    public static String getLastHourTime() {

        Calendar ca = Calendar.getInstance ();
        ca.set (Calendar.MINUTE, 0);
        ca.set (Calendar.SECOND, 0);
        ca.set (Calendar.HOUR_OF_DAY, ca.get (Calendar.HOUR_OF_DAY) - 1);
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        return sdf.format (ca.getTime ());
    }

    /**
     * 获取指定日期下一个小时的时间
     *
     * @param
     */
    public static Date getNextHourTime(Date date) {

        Calendar ca = Calendar.getInstance ();
        ca.setTime (date);
        ca.set (Calendar.MINUTE, 0);
        ca.set (Calendar.SECOND, 0);
        ca.set (Calendar.HOUR_OF_DAY, ca.get (Calendar.HOUR_OF_DAY) + 1);

        return ca.getTime ();
    }


    public static Date addOrReduceHourTime(Date date,Integer n) {

        Calendar ca = Calendar.getInstance ();
        ca.setTime (date);
        ca.set (Calendar.MINUTE, 0);
        ca.set (Calendar.SECOND, 0);
        ca.set (Calendar.HOUR_OF_DAY, ca.get (Calendar.HOUR_OF_DAY) + n);

        return ca.getTime ();
    }

    public static Date praiseStringToDateYYYY_MM(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM");
        Date parse = null;
        try {
            parse = sdf.parse (date);
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return parse;
    }


    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     * @return
     */
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition (0);
        Date strtodate = formatter.parse (strDate, pos);
        return strtodate;
    }

    /**
     * 获取指定时间的小时
     *
     * @param
     * @throws
     */
    public static Integer getHourFromDate(Date date) {
        Calendar calendar = Calendar.getInstance ();
        calendar.setTime (date);
        return calendar.get (Calendar.HOUR_OF_DAY);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式时间转化为date
     */
    public static Date praiseStringToDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        Date parse = null;
        try {
            parse = sdf.parse (date);
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return parse;
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式时间转化为date
     */
    public static Date praiseString2Date(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
        Date parse = null;
        try {
            parse = sdf.parse (date);
        } catch (Exception e) {
            e.printStackTrace ();
        }
        return parse;
    }

    /**
     * 组合日期和小时
     *
     * @param
     */
    public static Date combineDateAndHour(Integer hour, Date date) {
        Calendar ca = Calendar.getInstance ();
        ca.setTime (date);
        ca.set (Calendar.MINUTE, 0);
        ca.set (Calendar.SECOND, 0);
        ca.set (Calendar.HOUR_OF_DAY, hour);
        return ca.getTime ();
    }

    private static final String date_format = "yyyy-MM-dd";
    private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<> ();

    public static DateFormat getDateFormat() {
        DateFormat df = threadLocal.get ();
        if (df == null) {
            df = new SimpleDateFormat (date_format);
            threadLocal.set (df);
        }
        return df;
    }

    public static String formatDate(Date date) throws ParseException {
        return getDateFormat ().format (date);
    }

    public static Date parse(String strDate) {
        try {
            return getDateFormat ().parse (strDate);
        } catch (ParseException e) {
           //
        }
        return null;
    }

    /**
     * java 获取两个日期间的日期
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }
    public static String getDateLastDay(String year, String month) {

        //year="2018" month="2"
        Calendar calendar = Calendar.getInstance();
        // 设置时间,当前时间不用设置
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month));

        // System.out.println(calendar.getTime());

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd ");
        return format.format(calendar.getTime());
    }

    public static void main(String[] args) throws Exception {
        System.out.println (getDateLastDay("2018","3"));
    }
}
