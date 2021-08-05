package cn.bincker.web.blog.base.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ICommentReplyRepositoryTest {
    @Autowired
    private ICommentReplyRepository commentReplyRepository;

    @Test
    void test() {
        commentReplyRepository.countAllByCommentIds(List.of(1L));
    }
}
