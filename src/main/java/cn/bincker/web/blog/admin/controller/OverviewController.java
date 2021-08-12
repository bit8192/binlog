package cn.bincker.web.blog.admin.controller;

import cn.bincker.web.blog.admin.service.IOverviewService;
import cn.bincker.web.blog.admin.vo.BinlogStatusVo;
import cn.bincker.web.blog.base.exception.BadRequestException;
import cn.bincker.web.blog.base.vo.StringLongValueVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("${binlog.base-path}/admin/overview")
public class OverviewController {
    private final IOverviewService overviewService;

    public OverviewController(IOverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("status")
    public BinlogStatusVo getBinlogStatus(){
        return overviewService.getBinlogStatus();
    }

    @GetMapping("visit-statistics")
    public List<StringLongValueVo> getVisitStatistics(Date start, Date end){
        if(start == null || end == null) throw new BadRequestException("参数错误");
        return overviewService.getVisitStatistics(start, end);
    }

    @GetMapping("area-statistics")
    public List<StringLongValueVo> getAreaStatistics(Date start, Date end){
        if(start == null || end == null) throw new BadRequestException("参数错误");
        return overviewService.getAreaStatistics(start, end);
    }

    @GetMapping("platform-statistics")
    public List<StringLongValueVo> getPlatformStatistics(Date start, Date end){
        if(start == null || end == null) throw new BadRequestException("参数错误");
        return overviewService.getPlatformStatistics(start, end);
    }

    @GetMapping("browser-statistics")
    public List<StringLongValueVo> getBrowserStatistics(Date start, Date end){
        if(start == null || end == null) throw new BadRequestException("参数错误");
        return overviewService.getBrowserStatistics(start, end);
    }
}
