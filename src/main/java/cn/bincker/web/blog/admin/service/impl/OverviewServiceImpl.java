package cn.bincker.web.blog.admin.service.impl;

import cn.bincker.web.blog.admin.service.IOverviewService;
import cn.bincker.web.blog.admin.vo.BinlogStatusVo;
import cn.bincker.web.blog.base.event.UserActionEvent;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.repository.IRequestLogRepository;
import cn.bincker.web.blog.base.repository.IUserActionLogRepository;
import cn.bincker.web.blog.base.vo.StringLongValueVo;
import cn.bincker.web.blog.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OverviewServiceImpl implements IOverviewService {
    private final IRequestLogRepository requestLogRepository;
    private final IBaseUserRepository userRepository;
    private final IUserActionLogRepository userActionLogRepository;

    public OverviewServiceImpl(IRequestLogRepository requestLogRepository, IBaseUserRepository userRepository, IUserActionLogRepository userActionLogRepository) {
        this.requestLogRepository = requestLogRepository;
        this.userRepository = userRepository;
        this.userActionLogRepository = userActionLogRepository;
    }

    @Override
    public BinlogStatusVo getBinlogStatus() {
        var tomorrow = DateUtils.tomorrow();
        var today = DateUtils.today();
        var thisMonday = DateUtils.thisMonday();
        var thisMonth = DateUtils.thisMonth();
        var thisYear = DateUtils.thisYear();
        var yesterday = DateUtils.yesterday();
        var lastMonday = DateUtils.lastMonday();
        var lastMonth = DateUtils.lastMonth();
        var lastYear = DateUtils.lastYear();

        var vo = new BinlogStatusVo();
        vo.setTotalVisitNum(requestLogRepository.countBySessionId());
        vo.setTodayVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(today, tomorrow));
        vo.setThisWeekVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(thisMonday, tomorrow));
        vo.setThisMonthVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(thisMonth, tomorrow));
        vo.setThisYearVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(thisYear, tomorrow));
        vo.setYesterdayVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(yesterday, today));
        vo.setLastWeekVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(lastMonday, thisMonday));
        vo.setLastMonthVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(lastMonth, thisMonth));
        vo.setLastYearVisitNum(requestLogRepository.countSessionIdAndCreatedDateBetween(lastYear, thisYear));

        vo.setTotalRequestNum(requestLogRepository.count());
        vo.setTodayRequestNum(requestLogRepository.countByCreatedDateBetween(today, tomorrow));
        vo.setThisWeekRequestNum(requestLogRepository.countByCreatedDateBetween(thisMonday, tomorrow));
        vo.setThisMonthRequestNum(requestLogRepository.countByCreatedDateBetween(thisMonth, tomorrow));
        vo.setThisYearRequestNum(requestLogRepository.countByCreatedDateBetween(thisYear, tomorrow));
        vo.setYesterdayRequestNum(requestLogRepository.countByCreatedDateBetween(yesterday, today));
        vo.setLastWeekRequestNum(requestLogRepository.countByCreatedDateBetween(lastMonday, thisMonday));
        vo.setLastMonthRequestNum(requestLogRepository.countByCreatedDateBetween(lastMonth, thisMonth));
        vo.setLastYearRequestNum(requestLogRepository.countByCreatedDateBetween(lastYear, thisYear));

        vo.setTotalUserNum(userRepository.count());
        vo.setTodayAddUserNum(userRepository.countByCreatedDateBetween(today, tomorrow));
        vo.setThisWeekAddUserNum(userRepository.countByCreatedDateBetween(thisMonday, tomorrow));
        vo.setThisMonthAddUserNum(userRepository.countByCreatedDateBetween(thisMonth, tomorrow));
        vo.setThisYearAddUserNum(userRepository.countByCreatedDateBetween(thisYear, tomorrow));
        vo.setYesterdayAddUserNum(userRepository.countByCreatedDateBetween(yesterday, today));
        vo.setLastWeekAddUserNum(userRepository.countByCreatedDateBetween(lastMonday, thisMonday));
        vo.setLastMonthAddUserNum(userRepository.countByCreatedDateBetween(lastMonth, thisMonth));
        vo.setLastYearAddUserNum(userRepository.countByCreatedDateBetween(lastYear, thisYear));

        var loginActions = List.of(
                UserActionEvent.ActionEnum.LOGIN_PASSWORD,
                UserActionEvent.ActionEnum.LOGIN_GITHUB,
                UserActionEvent.ActionEnum.LOGIN_QQ,
                UserActionEvent.ActionEnum.LOGIN_WECHAT,
                UserActionEvent.ActionEnum.LOGIN_PHONE
        );
        vo.setTotalLoginNum(userActionLogRepository.countByActionIn(loginActions));
        vo.setTodayLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, today, tomorrow));
        vo.setThisWeekLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, thisMonday, tomorrow));
        vo.setThisMonthLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, thisMonth, tomorrow));
        vo.setThisYearLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, thisYear, tomorrow));
        vo.setYesterdayLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, yesterday, today));
        vo.setLastWeekLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, lastMonday, thisMonday));
        vo.setLastMonthLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, lastMonth, thisMonth));
        vo.setLastYearLoginNum(userActionLogRepository.countByActionInAndCreatedDateBetween(loginActions, lastYear, thisYear));
        return vo;
    }

    @Override
    public List<StringLongValueVo> getVisitStatistics(Date start, Date end) {
        return requestLogRepository.countAllBySessionId(start, end);
    }

    @Override
    public List<StringLongValueVo> getAreaStatistics(Date start, Date end) {
        return requestLogRepository.countAllByAddress(start, end);
    }

    @Override
    public List<StringLongValueVo> getPlatformStatistics(Date start, Date end) {
        return requestLogRepository.countAllByPlatform(start, end);
    }

    @Override
    public List<StringLongValueVo> getBrowserStatistics(Date start, Date end) {
        return requestLogRepository.countAllByBrowserStatistics(start, end);
    }
}
