package cn.bincker.web.blog.base.repository;

import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.vo.PrivateMessageSession;
import cn.bincker.web.blog.base.vo.UnreadMessageCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.OrderBy;
import java.util.List;

public interface IMessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {
    @Query("""
    select msg.type as type, count(msg.id) as count
    from Message msg
    where msg.toUser.id = :uid and msg.isRead = false
    group by msg.type
    """)
    List<UnreadMessageCount> queryUnreadCount(Long uid);

    @Query("""
       select
         msg as latestMessage,
         (select count(m.id) from Message m where m.toUser.id = :uid and m.fromUser = msg.fromUser and m.isRead = false) as unreadMessageCount
       from Message msg
       where msg.toUser.id = :uid and msg.type = 'PRIVATE_MESSAGE'
       group by msg.fromUser
       order by msg.createdDate desc
    """)
    List<PrivateMessageSession> findAllPrivateMessageSessionByToUserId(Long uid);

    @OrderBy("createdDate desc")
    Page<Message> findAllByToUserIdAndFromUserId(Long toUserId, Long fromUserId, Pageable pageable);

    @Modifying
    @Query("update Message msg set msg.isRead = true where msg.id in (:ids)")
    void setRead(Long... ids);
}
