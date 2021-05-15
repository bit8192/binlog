package cn.bincker.web.blog.utils;

import cn.bincker.web.blog.base.entity.SystemProfile;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
}
