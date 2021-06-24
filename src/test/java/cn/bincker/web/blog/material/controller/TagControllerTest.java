package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.material.dto.TagPostDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
@Transactional
class TagControllerTest {
    private MockMvc mockMvc;
    @Value("${system.base-path}")
    private String basePath;
    @Autowired
    private ITagRepository tagRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach(
            WebApplicationContext applicationContext,
            RestDocumentationContextProvider documentationContextProvider
    ){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(documentationContextProvider))
                .build();
    }

    private FieldDescriptor[] getTagFields(String prefix, FieldDescriptor ...additional){
        var result = new FieldDescriptor[3 + additional.length];
        result[0] = fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id");
        result[1] = fieldWithPath(prefix + "title").type(JsonFieldType.STRING).description("标题");
        result[2] = fieldWithPath(prefix + "articleNum").type(JsonFieldType.NUMBER).description("articleNum");
        System.arraycopy(additional, 0, result, 3, additional.length);
        return result;
    }

    @Test
    void list() throws Exception {
        for (int i = 0; i < 10; i++) {
            var tag = new Tag();
            tag.setTitle("title" + i);
            tagRepository.save(tag);
        }
        mockMvc.perform(
                get(basePath + "/tags/all").contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("{ClassName}/{methodName}",
                                responseFields(getTagFields("[]."))
                        )
                );
    }

    @Test
    void hotList() throws Exception {
        for (int i = 0; i < 10; i++) {
            var tag = new Tag();
            tag.setTitle("test tag " + i);
            tagRepository.save(tag);
        }
        mockMvc.perform(
                get(basePath + "/tags/hot")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "{ClassName}/{methodName}",
                                responseFields(getTagFields("[]."))
                        )
                );
    }

    @Test
    void getItem() throws Exception {
        var tag = new Tag();
        tag.setTitle("标题");
        tagRepository.save(tag);
        mockMvc.perform(
                get(basePath + "/tags/{id}", tag.getId()).contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        responseFields(getTagFields("")),
                        pathParameters(parameterWithName("id").description("id"))
                ));
    }

    @Test
    void add() throws Exception {
        var tag = new TagPostDto();
        tag.setTitle("标题");
        mockMvc.perform(
                post(basePath + "/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tag))
        )
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andDo(document("{ClassName}/{methodName}",
                        requestFields(fieldWithPath("title").type(JsonFieldType.STRING).description("标题")),
                        responseFields(getTagFields(""))
                ));
    }

    @Test
    void del() throws Exception{
        var tag = new Tag();
        tag.setTitle("test title");
        tagRepository.save(tag);
        mockMvc.perform(
                delete(basePath + "/tags/{id}", tag.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}", pathParameters(
                        parameterWithName("id").description("id")
                )));
    }

    public static Tag newTag(String title){
        var tag = new Tag();
        tag.setTitle(title);
        return tag;
    }
}