package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.LeftMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ILeftMessageRepository extends JpaRepository<LeftMessage, Long> {
}
