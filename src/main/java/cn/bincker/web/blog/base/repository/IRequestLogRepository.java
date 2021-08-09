package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IRequestLogRepository extends JpaRepository<RequestLog, Long> {
}
