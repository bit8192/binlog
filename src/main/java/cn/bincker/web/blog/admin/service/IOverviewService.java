package cn.bincker.web.blog.admin.service;

import cn.bincker.web.blog.admin.vo.BinlogStatusVo;
import cn.bincker.web.blog.base.vo.StringLongValueVo;

import java.util.Date;
import java.util.List;

public interface IOverviewService {
    BinlogStatusVo getBinlogStatus();

    List<StringLongValueVo> getVisitStatistics(Date start, Date end);

    List<StringLongValueVo> getAreaStatistics(Date start, Date end);

    List<StringLongValueVo> getPlatformStatistics(Date start, Date end);

    List<StringLongValueVo> getBrowserStatistics(Date start, Date end);
}
