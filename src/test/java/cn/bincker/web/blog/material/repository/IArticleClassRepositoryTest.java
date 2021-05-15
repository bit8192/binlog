package cn.bincker.web.blog.material.repository;

import cn.bincker.web.blog.material.service.vo.ArticleClassVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IArticleClassRepositoryTest {
    @Autowired
    private IArticleClassRepository repository;

    @Test
    void listTopNode() {
        List<ArticleClassVo> result = repository.listTopNode();
        assertNotNull(result);
        for (ArticleClassVo vo : result) {
            System.out.println("id: " + vo.getId());
            System.out.println("childrenNum: " + vo.getChildrenNum());
        }
    }
}