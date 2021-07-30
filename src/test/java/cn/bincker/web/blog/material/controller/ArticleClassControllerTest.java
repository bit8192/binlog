package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.material.entity.ArticleClass;
import cn.bincker.web.blog.material.repository.IArticleClassRepository;
import cn.bincker.web.blog.material.dto.ArticleClassPostDto;
import cn.bincker.web.blog.material.vo.ArticleClassVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
public class ArticleClassControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private IArticleClassRepository articleClassRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${binlog.base-path}")
    private String basePath;

    @BeforeEach
    void beforeEach(
            WebApplicationContext webApplicationContext,
            RestDocumentationContextProvider restDocumentationContextProvider
    ){
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentationContextProvider))
                .build();
    }

    public static Collection<FieldDescriptor> getArticleClassFields(String prefix){
        var result = new ArrayList<FieldDescriptor>();
        result.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        result.add(fieldWithPath(prefix + "title").type(JsonFieldType.STRING).description("标题"));
        result.add(fieldWithPath(prefix + "orderNum").type(JsonFieldType.NUMBER).optional().description("排序"));
        result.add(fieldWithPath(prefix + "visible").type(JsonFieldType.BOOLEAN).optional().description("是否可见"));
        result.add(fieldWithPath(prefix + "childrenNum").type(JsonFieldType.NUMBER).optional().description("子节点数量"));
        return result;
    }

    @Test
    void getItem() throws Exception {
        mockMvc.perform(
                get(basePath + "/article-classes/{id}", 7)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("{ClassName}/{methodName}",
                                pathParameters(parameterWithName("id").description("分类id")),
                                responseFields(getArticleClassFields("").toArray(new FieldDescriptor[]{}))
                        )
                );
    }

    @Test
    void findAllByParentId() throws Exception {
        mockMvc.perform(
                get(basePath + "/article-classes/search/parent")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        responseFields(getArticleClassFields("[].").toArray(new FieldDescriptor[]{}))
                ))
                .andReturn();
    }

    @Test
    @Transactional
    void addItem() throws Exception {
        ArticleClassPostDto data = new ArticleClassPostDto();
        data.setTitle("test title");
        data.setVisible(true);
        data.setOrderNum(33);
        MvcResult addParentResult = mockMvc.perform(
                post(basePath + "/article-classes").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(data))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value(data.getTitle()))
                .andExpect(jsonPath("visible").value(data.getVisible()))
                .andExpect(jsonPath("orderNum").value(data.getOrderNum()))
                .andReturn();
        ArticleClassVo parent = objectMapper.readValue(addParentResult.getResponse().getContentAsString(), ArticleClassVo.class);
        data.setTitle("sub item title");
        data.setVisible(false);
        data.setOrderNum(44);
        data.setParentId(parent.getId());
        MvcResult addChildrenResult = mockMvc.perform(
                post(basePath + "/article-classes").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(data))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        responseFields(getArticleClassFields("").toArray(new FieldDescriptor[]{})),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("标题"),
                                fieldWithPath("visible").type(JsonFieldType.BOOLEAN).description("是否可见"),
                                fieldWithPath("orderNum").type(JsonFieldType.NUMBER).optional().description("排序（可选）"),
                                fieldWithPath("parentId").type(JsonFieldType.NUMBER).optional().description("父节点id（可选）")
                        )
                ))
                .andReturn();
        ArticleClassVo child = objectMapper.readValue(addChildrenResult.getResponse().getContentAsString(), ArticleClassVo.class);
        ArticleClass articleClass = articleClassRepository.getOne(child.getId());
        assertEquals(articleClass.getParent().getId(), parent.getId());
    }

    public static ArticleClass newArticleClass(String title, Integer orderNum, Boolean visible, ArticleClass parent){
        var articleClass = new ArticleClass();
        articleClass.setTitle(title);
        articleClass.setOrderNum(orderNum);
        articleClass.setVisible(visible);
        articleClass.setParent(parent);
        return articleClass;
    }
}
