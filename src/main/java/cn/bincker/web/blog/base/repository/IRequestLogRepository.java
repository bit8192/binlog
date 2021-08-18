package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.RequestLog;
import cn.bincker.web.blog.base.vo.StringLongValueVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface IRequestLogRepository extends JpaRepository<RequestLog, Long>, JpaSpecificationExecutor<RequestLog> {
    @Query("select count(distinct log.sessionId) from RequestLog log where log.createdDate > :start and log.createdDate < :end")
    Long countSessionIdAndCreatedDateBetween(Date start, Date end);

    Long countByCreatedDateBetween(Date start, Date end);

    @Query("select count(distinct log.sessionId) from RequestLog log")
    Long countBySessionId();

    @Query("select DATE_FORMAT(log.createdDate, '%Y-%m-%d') as key, count(distinct log.sessionId) as value from RequestLog log where log.createdDate > :start and log.createdDate < :end group by key")
    List<StringLongValueVo> countAllBySessionId(Date start, Date end);

    @Query("select log.address as key, count(distinct log.ip) as value from RequestLog log where log.createdDate > :start and log.createdDate < :end group by log.address")
    List<StringLongValueVo> countAllByAddress(Date start, Date end);

    @Query("""
        select
               case
                   when locate('Linux',log.userAgent)>0 then
                        'Linux'
                   when locate('Android',log.userAgent)>0 then
                       'Android'
                   when locate('Windows',log.userAgent)>0 then
                       'Windows'
                   when locate('IOS',log.userAgent)>0 then
                       'IOS'
                   else 'Other'
                   end as key,
               count(distinct log.sessionId) as value
        from RequestLog log
        where
            log.createdDate > :start
            and log.createdDate < :end
        group by key
    """)
    List<StringLongValueVo> countAllByPlatform(Date start, Date end);

    @Query("""
        select
               case
                   when locate('Edg',log.userAgent)>0 then
                       'Edge'
                   when locate('OPR',log.userAgent)>0 then
                       'Opera'
                   when locate('Vivaldi',log.userAgent)>0 then
                       'Vivaldi'
                   when locate('Chrome',log.userAgent)>0 then
                       'Chrome'
                   when locate('Firefox',log.userAgent)>0 then
                        'Firefox'
                   when locate('Safari',log.userAgent)>0 then
                       'Safari'
                   when locate('Opera',log.userAgent)>0 then
                       'Opera'
                   when locate('MSIE 6',log.userAgent)>0 then
                       'IE6'
                   when locate('MSIE 7',log.userAgent)>0 then
                       'IE7'
                   when locate('MSIE 8',log.userAgent)>0 then
                       'IE8'
                   when locate('MSIE 9',log.userAgent)>0 then
                       'IE9'
                   when locate('MSIE 10',log.userAgent)>0 then
                       'IE10'
                   when locate('Trident/7',log.userAgent)>0 then
                       'IE11'
                   else 'Other'
                   end as key,
               count(distinct log.sessionId) as value
        from RequestLog log
        where
            log.createdDate > :start
            and log.createdDate < :end
        group by key
    """)
    List<StringLongValueVo> countAllByBrowserStatistics(Date start, Date end);
}
