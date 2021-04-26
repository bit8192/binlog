package cn.bincker.web.blog.base.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class IndexControllerTest {
    private MockMvc mock;
    @Value("${spring.data.rest.base-path}")
    private String basePath;

    @BeforeEach
    public void beforeEach(WebApplicationContext applicationContext, RestDocumentationContextProvider contextProvider){
        mock = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(MockMvcRestDocumentation.documentationConfiguration(contextProvider))
                .build();
    }

    @Test
    public void test() throws Exception {
        mock.perform(
                post("/test")
                .param("test", "hello world")
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void apiList() throws Exception {
        mock.perform(
                get(basePath)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}