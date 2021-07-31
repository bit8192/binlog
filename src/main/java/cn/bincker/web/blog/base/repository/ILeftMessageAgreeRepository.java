package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.LeftMessageAgree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ILeftMessageAgreeRepository extends JpaRepository<LeftMessageAgree, Long> {
    List<LeftMessageAgree> findAllByCreatedUserAndMessageIdIn(BaseUser user, Iterable<Long> leftMessageIds);

    Optional<LeftMessageAgree> findByCreatedUserAndMessageId(BaseUser user, Long id);

    Long countByMessageId(Long id);
}
