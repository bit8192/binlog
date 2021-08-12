package cn.bincker.web.blog.admin.vo;

import lombok.Data;

/**
 * Binlog的状态
 */
@Data
public class BinlogStatusVo {

//    访问量
    private Long totalVisitNum;
    private Long todayVisitNum;
    private Long thisWeekVisitNum;
    private Long thisMonthVisitNum;
    private Long thisYearVisitNum;

    private Long yesterdayVisitNum;
    private Long lastWeekVisitNum;
    private Long lastMonthVisitNum;
    private Long lastYearVisitNum;


//    请求量
    private Long totalRequestNum;
    private Long todayRequestNum;
    private Long thisWeekRequestNum;
    private Long thisMonthRequestNum;
    private Long thisYearRequestNum;

    private Long yesterdayRequestNum;
    private Long lastWeekRequestNum;
    private Long lastMonthRequestNum;
    private Long lastYearRequestNum;


//    用户量
    private Long totalUserNum;
    private Long todayAddUserNum;
    private Long thisWeekAddUserNum;
    private Long thisMonthAddUserNum;
    private Long thisYearAddUserNum;

    private Long yesterdayAddUserNum;
    private Long lastWeekAddUserNum;
    private Long lastMonthAddUserNum;
    private Long lastYearAddUserNum;


//    登录用户量
    private Long totalLoginNum;
    private Long todayLoginNum;
    private Long thisWeekLoginNum;
    private Long thisMonthLoginNum;
    private Long thisYearLoginNum;

    private Long yesterdayLoginNum;
    private Long lastWeekLoginNum;
    private Long lastMonthLoginNum;
    private Long lastYearLoginNum;
}
