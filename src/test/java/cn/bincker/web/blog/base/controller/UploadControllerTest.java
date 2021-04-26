package cn.bincker.web.blog.base.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class UploadControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    public void beforeTest(
            WebApplicationContext context,
            RestDocumentationContextProvider restDocumentationContextProvider
    ){
            mockMvc = MockMvcBuilders.webAppContextSetup(context)
                    .apply(documentationConfiguration(restDocumentationContextProvider))
                    .build();
    }

    @Test
    void upload() throws Exception {
        mockMvc.perform(
                fileUpload("/api/files")
                .file("filename", "hello world".getBytes(StandardCharsets.UTF_8))
                .param("isPublic", "true")
        )
                .andExpect(status().isOk())
                .andDo(document("files"));
    }

    @Test
    void get() throws Exception {
        mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/files/6")
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("files"));
    }
}