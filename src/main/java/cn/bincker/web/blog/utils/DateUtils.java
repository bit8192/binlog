package cn.bincker.web.blog.utils;

import cn.bincker.web.blog.base.config.SystemProfile;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class DateUtils {
    @Getter
    private final DateFormat datetimeFormat;
    @Getter
    private final DateFormat dateFormat;
    @Getter
    private final DateFormat timeFormat;

    public DateUtils(SystemProfile systemProfile) {
        datetimeFormat = new SimpleDateFormat(systemProfile.getDatetimeFormat());
        dateFormat = new SimpleDateFormat(systemProfile.getDateFormat());
        timeFormat = new SimpleDateFormat(systemProfile.getTimeFormat());
    }

    public String todayStr(){
        return dateFormat.format(new Date());
    }

    public String nowStr(){
        return datetimeFormat.format(new Date());
    }

    public String nowTimeStr(){
        return timeFormat.format(new Date());
    }

    public static final Long INTERVAL_1_HOUR = 3600000L;
    public static final Long INTERVAL_8_HOUR = 3600000L * 8;
    public static final Long INTERVAL_1_DAY = INTERVAL_1_HOUR * 24;


    /**
     * 清除时间部分返回Date
     */
    public static Date clearTime(Date date){
        return clearTimeCalendar(date).getTime();
    }

    /**
     * 清除时间部分并返回Calendar
     */
    public static Calendar clearTimeCalendar(Date date){
        var calendar = Calendar.getInstance();
        calendar.setTime(date);
        var now = calendar.getTimeInMillis();
        if(calendar.get(Calendar.HOUR_OF_DAY) > 8){
            calendar.setTimeInMillis(now - now % INTERVAL_1_DAY - INTERVAL_8_HOUR);
        }else{
            calendar.setTimeInMillis(now - (now + INTERVAL_8_HOUR) % INTERVAL_1_DAY);
        }
        return calendar;
    }

    /**
     * 获取今天0点的时间
     */
    public static Date today(){
        return clearTime(new Date());
    }

    /**
     * 获取今天0点的Calendar
     */
    public static Calendar todayCalendar(){
        return clearTimeCalendar(new Date());
    }

    /**
     * 昨天
     */
    public static Date yesterday(){
        return new Date(today().getTime() - INTERVAL_1_DAY);
    }

    /**
     * 明天
     */
    public static Date tomorrow(){
        return new Date(today().getTime() + INTERVAL_1_DAY);
    }


    /**
     * 这周一
     */
    public static Date thisMonday(){
        return thisMondayCalendar().getTime();
    }

    /**
     * 这周一的Calendar
     */
    public static Calendar thisMondayCalendar(){
        var calendar = todayCalendar();
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        return calendar;
    }


    /**
     * 上周一
     */
    public static Date lastMonday(){
        return lastMondayCalendar().getTime();
    }

    /**
     * 上周一的Calendar
     */
    public static Calendar lastMondayCalendar(){
        var calendar = thisMondayCalendar();
        calendar.add(Calendar.WEEK_OF_MONTH, -1);
        return calendar;
    }


    /**
     * 下周一
     */
    public static Date nextMonday(){
        return nextMondayCalendar().getTime();
    }

    /**
     * 下周一的Calendar
     */
    public static Calendar nextMondayCalendar(){
        var calendar = thisMondayCalendar();
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        return calendar;
    }


    /**
     * 这个月一号
     */
    public static Date thisMonth(){
        return thisMonthCalendar().getTime();
    }

    /**
     * 这个月一号的Calendar
     */
    public static Calendar thisMonthCalendar(){
        var calendar = todayCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        return calendar;
    }


    /**
     * 上个月一号
     */
    public static Date lastMonth(){
        return lastMonthCalendar().getTime();
    }

    /**
     * 上个月一号的Calendar
     */
    public static Calendar lastMonthCalendar(){
        var calendar = thisMonthCalendar();
        calendar.add(Calendar.MONTH, -1);
        return calendar;
    }


    /**
     * 下个月一号
     */
    public static Date nextMonth(){
        return nextMonthCalendar().getTime();
    }

    /**
     * 下个月一号的Calendar
     */
    public static Calendar nextMonthCalendar(){
        var calendar = thisMonthCalendar();
        calendar.add(Calendar.MONTH, 1);
        return calendar;
    }


    /**
     * 今年第一天
     */
    public static Date thisYear(){
        return thisYearCalendar().getTime();
    }

    /**
     * 今年第一天的Calendar
     */
    public static Calendar thisYearCalendar(){
        var calendar = todayCalendar();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return calendar;
    }

    /**
     * 去年
     */
    public static Date lastYear() {
        var calendar = thisYearCalendar();
        calendar.add(Calendar.YEAR, -1);
        return calendar.getTime();
    }
}
