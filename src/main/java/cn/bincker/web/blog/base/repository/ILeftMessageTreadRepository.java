package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.LeftMessageTread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ILeftMessageTreadRepository extends JpaRepository<LeftMessageTread, Long> {
    List<LeftMessageTread> findAllByCreatedUserAndMessageIdIn(BaseUser user, Iterable<Long> leftMessageIds);

    Optional<LeftMessageTread> findByCreatedUserAndMessageId(BaseUser user, Long id);

    Long countByMessageId(Long id);
}
