package cn.bincker.web.blog.material.service.impl;

import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.material.service.ITagService;
import cn.bincker.web.blog.material.dto.TagPostDto;
import cn.bincker.web.blog.material.vo.TagVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class TagServiceImplTest {
    private MockMvc mockMvc;
    @Autowired
    private ITagService tagService;
    @Autowired
    private ITagRepository tagRepository;

    @BeforeEach
    void beforeEach(
            WebApplicationContext applicationContext,
            RestDocumentationContextProvider documentationContextProvider
    ){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(documentationContextProvider))
                .build();
    }

    @Test
    void listAll() {
        tagService.listAll();
    }

    @Test
    @Transactional
    void findVoById() {
        var tag = new Tag();
        tag.setTitle("test");
        tagRepository.save(tag);
        Optional<TagVo> tagOptional = tagService.findById(tag.getId());
        assertTrue(tagOptional.isPresent());
    }

    @Test
    @Transactional
    void add(){
        var tag = new TagPostDto();
        tag.setTitle("test tag");
        var tagVo = tagService.add(tag);
        assertEquals(tagVo.getTitle(), tag.getTitle());
        var result = tagRepository.getOne(tagVo.getId());
        assertEquals(tag.getTitle(), result.getTitle());
    }
}