package cn.bincker.web.blog.base.specification;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Message;
import org.springframework.data.jpa.domain.Specification;

public class MessageSpecification {
    public static Specification<Message> toUser(BaseUser baseUser){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("toUser"), baseUser);
    }

    public static Specification<Message> typeLike(String type){
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("type").as(String.class), "%" + type + "%");
    }

    public static Specification<Message> type(Message.Type type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
    }
}
