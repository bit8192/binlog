package cn.bincker.web.blog;

import cn.bincker.web.blog.security.machine.ChineseVerifyCode;
import cn.bincker.web.blog.security.machine.IVerifyCode;
import cn.bincker.web.blog.security.machine.VerifyQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
public class AuthenticationTests {
    private MockMvc mock;
    @Value("${system.base-path}")
    private String basePath;

    @BeforeEach
    void beforeTest(
            WebApplicationContext context,
            RestDocumentationContextProvider restDocumentationContextProvider
    ){
        mock = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentationContextProvider))
                .apply(springSecurity())
                .build();
    }

    @Test
    void authentication() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MvcResult result = mock.perform(
                get(basePath + "/verify-code")
                .session(session)
        )
                .andExpect(status().isOk())
                .andReturn();

        VerifyQuestion<?> verifyQuestion = (VerifyQuestion<?>) Objects.requireNonNull(result.getRequest().getSession()).getAttribute(IVerifyCode.SESSION_ATTRIBUTE_ANSWER);
        ChineseVerifyCode.ChineseVerifyCodeAnswer answer = (ChineseVerifyCode.ChineseVerifyCodeAnswer) verifyQuestion.getAnswer();
        System.out.println(verifyQuestion);

        MockHttpServletRequestBuilder requestBuilder =
                post(basePath + "/authorize")
                        .param("username", "admin")
                        .param("password", "123456")
                .session(session);
        List<Pair<String, Point>> points = answer.getPoints();
        for (int i = 0, pointsSize = points.size(); i < pointsSize; i++) {
            Pair<String, Point> p = points.get(i);
            requestBuilder.param("point" + i, p.getSecond().x + "," + p.getSecond().y);
        }

        mock.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    public static Collection<FieldDescriptor> getBaseUserVoFields(String prefix){
        var result = new ArrayList<FieldDescriptor>();
        result.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        result.add(fieldWithPath(prefix + "username").type(JsonFieldType.STRING).description("用户名"));
        result.add(fieldWithPath(prefix + "nickname").type(JsonFieldType.NUMBER).optional().description("昵称"));
        result.add(fieldWithPath(prefix + "headImg").type(JsonFieldType.STRING).optional().description("头像"));
        result.add(fieldWithPath(prefix + "isBlogger").type(JsonFieldType.BOOLEAN).optional().description("是否是博主"));
        result.add(fieldWithPath(prefix + "isAdmin").type(JsonFieldType.BOOLEAN).optional().description("是否是管理员"));
        return result;
    }
}
