package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.AuthenticationTests;
import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.Message;
import cn.bincker.web.blog.base.repository.IMessageRepository;
import cn.bincker.web.blog.material.controller.ArticleClassControllerTest;
import cn.bincker.web.blog.material.controller.ArticleCommentControllerTest;
import cn.bincker.web.blog.material.controller.ArticleControllerTest;
import cn.bincker.web.blog.material.controller.TagControllerTest;
import cn.bincker.web.blog.material.repository.IArticleClassRepository;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.bincker.web.blog.base.constant.FieldsDescriptorConstant.FIELDS_PAGE;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class MessageControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private IMessageRepository messageRepository;
    @Autowired
    private UserAuditingListener userAuditingListener;
    @Value("${system.base-path}")
    private String basePath;
    @Autowired
    private ITagRepository tagRepository;
    @Autowired
    private INetDiskFileRepository netDiskFileRepository;
    @Autowired
    private IArticleClassRepository articleClassRepository;
    @Autowired
    private IArticleRepository articleRepository;

    @BeforeEach
    public void beforeEach(WebApplicationContext applicationContext, RestDocumentationContextProvider documentationContextProvider){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(documentationContextProvider))
                .build();
    }

    @Test
    @WithUserDetails("admin")
    public void unreadCount() throws Exception {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow();
        for (int i = 0; i < 10; i++) {
            var message = newMessage("test", null, currentUser, Message.Type.SYSTEM, null);
            messageRepository.save(message);
        }
        mockMvc.perform(
                get(basePath + "/messages/unread-count")
        )
                .andDo(print())
                .andExpect(status().isOk());
    }

    /*
    //懒得写了，好麻烦
    @Test
    @WithUserDetails("admin")
    public void getArticleCommentMessagePage() throws Exception{
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow();
        var tag = TagControllerTest.newTag("tag");
        var convert = new NetDiskFile();
        convert.setPossessor(currentUser);
        convert.setName("test-file");
        convert = netDiskFileRepository.save(convert);
        var articleClass = ArticleClassControllerTest.newArticleClass("test-class", null, true, null);
        articleClass = articleClassRepository.save(articleClass);
        tag = tagRepository.save(tag);
        var article = ArticleControllerTest.newArticle(
                "article title",
                "content",
                Collections.singleton(tag),
                convert,
                articleClass
        );
        article = articleRepository.save(article);
        for (int i = 0; i < 5; i++) {
//            ArticleCommentControllerTest.
            var message = newMessage("test" + i, null, currentUser, Message.Type.ARTICLE_COMMENT, article.getId());
            messageRepository.save(message);
        }
        mockMvc.perform(
                get(basePath + "/messages/article-comment")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        responseFields(FIELDS_PAGE).and(getMessageVoFields("content[]."))
                ));
    }*/

    @Test
    @WithUserDetails("admin")
    public void getReplyMessagePage() throws Exception{
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow();
    }

    public static List<FieldDescriptor> getMessageVoFields(String prefix){
        var result = new ArrayList<FieldDescriptor>();
        result.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        result.add(fieldWithPath(prefix + "content").type(JsonFieldType.STRING).description("消息内容"));
        result.add(fieldWithPath(prefix + "createdDate").type(JsonFieldType.STRING).description("创建日期"));
        result.add(fieldWithPath(prefix + "fromUser").type(JsonFieldType.OBJECT).optional().description("发送人"));
        result.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "fromUser."));
        result.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "toUser."));
        result.add(fieldWithPath(prefix + "read").type(JsonFieldType.BOOLEAN).description("是否已读"));
        result.add(fieldWithPath(prefix + "additionInfo").type(JsonFieldType.STRING).optional().description("附加信息, 如回复的评论内容、评论的文章标题等"));
        return result;
    }

    public static List<FieldDescriptor> getPrivateMessageSessionVoFields(String prefix){
        var result = new ArrayList<>(getMessageVoFields(prefix + "latestMessage."));
        result.add(fieldWithPath(prefix + "unreadMessageCount").type(JsonFieldType.NUMBER).description("未读消息数量"));
        return result;
    }

    public Message newMessage(String content, BaseUser fromUser, BaseUser toUser, Message.Type type, Long relevantId){
        var message = new Message();
        message.setContent(content);
        message.setToUser(toUser);
        message.setFromUser(fromUser);
        message.setIsRead(false);
        message.setType(type);
        message.setRelevantId(relevantId);
        return message;
    }
}
