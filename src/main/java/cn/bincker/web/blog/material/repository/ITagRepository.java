package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.vo.TagVo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITagRepository extends JpaRepository<Tag, Long> {
    String TAG_VO = """
            new cn.bincker.web.blog.material.vo.TagVo(
                tag.id,
                tag.title,
                (select count(article.id) from Article article join article.tags tag1 on tag1.id = tag.id)
            )
            """;

    @Query("""
    select
    """ + TAG_VO + """
    from Tag tag
    """)
    List<TagVo> findAllVo();

    @Query("""
    select
    """ + TAG_VO + """
    from Tag tag
    where tag.id = :id
    """)
    Optional<TagVo> findVoById(@Param("id") Long id);

    @Query("""
    select
        count(article.id)
    from Article article
    join article.tags tag on tag.id = :id
    """)
    Long countArticleNum(@Param("id") Long id);

    @Query("from Tag t order by t.articleList.size desc")
    @EntityGraph("Tag.articleList")
    List<Tag> findAllOrderByArticleListSize();
}
