package cn.bincker.web.blog.material.specification;

import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.entity.Tag;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class ArticleSpecification {
    public static Specification<Article> keyWords(String keywords){
        return (root, query, criteriaBuilder) -> {
            if(!StringUtils.hasText(keywords)) return null;
            var pattern = "%" + keywords + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("title"), pattern),
                    criteriaBuilder.like(root.get("describe"), pattern),
                    criteriaBuilder.like(root.get("content"), pattern)
            );
        };
    }

    public static Specification<Article> articleClass(Long id){
        return (root, query, criteriaBuilder) -> {
            if(id == null) return null;
            return criteriaBuilder.equal(root.get("articleClass").get("id"), id);
        };
    }

    public static Specification<Article> tagIds(Long[] ids){
        return (root, query, criteriaBuilder) -> {
            if(ids == null || ids.length < 1) return null;
            var tagIdsSubQuery = query.subquery(Long.class);
            var tagRoot = tagIdsSubQuery.from(Tag.class);
            tagIdsSubQuery.select(tagRoot.get("id"));
            tagIdsSubQuery.where(criteriaBuilder.equal(tagRoot.join("articleList").get("id"),root.get("id")));
            var predicate = criteriaBuilder.literal(ids[0]).in(tagIdsSubQuery);
            for (int i = 1; i < ids.length; i++) {
                predicate = criteriaBuilder.and(
                        criteriaBuilder.literal(ids[i]).in(tagIdsSubQuery)
                );
            }
            return predicate;
        };
    }

    public static Specification<Article> isPublic(){
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isPublic"));
    }

    public static Specification<Article> publicOrUser(BaseUser user){
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.isTrue(root.get("isPublic")),
                criteriaBuilder.equal(root.get("createdUser"), user)
        );
    }
}
