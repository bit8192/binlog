package cn.bincker.web.blog.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class RequestUtilsTest {
    private MockMvc mockMvc;

    @BeforeEach
    public void beforeEach(
            WebApplicationContext applicationContext,
            RestDocumentationContextProvider documentationContextProvider
    ){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(documentationContextProvider))
                .build();
    }

    @Test
    void contextPathTest() throws Exception {
        HttpServletRequest request = mockMvc.perform(get("/")).andReturn().getRequest();
        assertEquals(RequestUtils.getRequestBaseUrl(request), "http://localhost:8080");
    }
}