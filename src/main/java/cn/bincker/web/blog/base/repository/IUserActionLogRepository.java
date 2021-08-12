package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.UserActionLog;
import cn.bincker.web.blog.base.event.UserActionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Date;

public interface IUserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    Long countByActionInAndCreatedDateBetween(Collection<UserActionEvent.ActionEnum> actions, Date start, Date end);

    Long countByActionIn(Collection<UserActionEvent.ActionEnum> actions);
}
