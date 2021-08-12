package cn.bincker.web.blog.utils;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void today() {
        var calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(DateUtils.today(), calendar.getTime());
    }

    @Test
    void yesterday() {
        var calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(DateUtils.yesterday(), calendar.getTime());
    }

    @Test
    void tomorrow() {
        var calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(DateUtils.tomorrow(), calendar.getTime());
    }

    @Test
    void thisMonday() {
        var calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_WEEK, 2);
        calendar.set(Calendar.WEEK_OF_MONTH, 1);
        calendar.add(Calendar.WEEK_OF_MONTH, -30);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
    }

    @Test
    void clearTime() {
        var calendar = Calendar.getInstance();
        var dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < 23; i++) {
            calendar.set(Calendar.HOUR_OF_DAY, i);
            var tmpCalendar = Calendar.getInstance();
            tmpCalendar.setTime(DateUtils.clearTime(calendar.getTime()));
            assertEquals(tmpCalendar.get(Calendar.DAY_OF_MONTH), dayOfMonth);
        }
    }

    @Test
    void thisYear() {
        var calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.today());
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
    }
}
