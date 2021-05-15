package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.material.entity.ArticleClass;
import cn.bincker.web.blog.material.service.vo.ArticleClassVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IArticleClassRepository extends JpaRepository<ArticleClass, Long>, JpaSpecificationExecutor<ArticleClass> {
    String ARTICLE_CLASS_VO_FIELDS = """
        new cn.bincker.web.blog.material.service.vo.ArticleClassVo(
        ac,
        count(child.id) as childrenNum
        )
            """;

    /**
     * 列出顶级节点
     */
    @Query("""
    select
    """ + ARTICLE_CLASS_VO_FIELDS + """
    from ArticleClass ac
    left join ac.children child
    where ac.parent.id is null
    group by ac.id
    order by ac.orderNum desc
    """)
    List<ArticleClassVo> listTopNode();

    /**
     * 获取子节点
     */
    @Query("""
    select
    """ + ARTICLE_CLASS_VO_FIELDS + """
    from ArticleClass ac
    left join ac.children child
    where ac.parent.id = :id
    group by ac.id
    order by ac.orderNum desc
    """)
    List<ArticleClassVo> findAllByParentId(@Param("id") Long id);

    @Query("""
    select
    """ + ARTICLE_CLASS_VO_FIELDS + """
    from ArticleClass ac
    left join ac.children child
    where ac.id = :id
    """)
    Optional<ArticleClassVo> findOneVo(Long id);

    @Query("""
    select
        count(ac.id)
    from ArticleClass ac
    where ac.parent.id = :id
    """)
    Long countChildren(Long id);
}
