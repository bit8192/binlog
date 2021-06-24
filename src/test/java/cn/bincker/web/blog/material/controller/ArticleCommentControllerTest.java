package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.AuthenticationTests;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.repository.IArticleClassRepository;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.material.service.IArticleCommentService;
import cn.bincker.web.blog.material.service.IArticleSubCommentService;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static cn.bincker.web.blog.base.constant.FieldsDescriptorConstant.FIELDS_PAGE;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
@Transactional
class ArticleCommentControllerTest {
    private MockMvc mockMvc;

    @Value("${system.base-path}")
    private String basePath;

    @Autowired
    private IArticleClassRepository articleClassRepository;
    @Autowired
    private IBaseUserRepository userRepository;
    @Autowired
    private ITagRepository tagRepository;
    @Autowired
    private INetDiskFileRepository netDiskFileRepository;
    @Autowired
    private IArticleRepository articleRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IArticleCommentService articleCommentService;
    @Autowired
    private IArticleSubCommentService articleSubCommentService;

    @BeforeEach
    void beforeEach(WebApplicationContext applicationContext, RestDocumentationContextProvider documentationContextProvider){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(documentationContextProvider))
                .build();
    }

    /**
     * 构建一个测试用的文章
     */
    private Article newArticle(){
        var user = userRepository.findByUsername("admin").orElseThrow();
        var articleClass = ArticleClassControllerTest.newArticleClass("testClass", 0, true, null);
        articleClassRepository.save(articleClass);

        var tag = TagControllerTest.newTag("tag-a");
        tagRepository.save(tag);
        var tags = new HashSet<Tag>();
        tags.add(tag);

        var cover = new NetDiskFile();
        cover.setName("cover.jpg");
        cover.setIsDirectory(false);
        cover.setPath("");
        cover.setPossessor(user);
        cover.setCreatedUser(user);
        cover.setLastModifiedUser(user);
        netDiskFileRepository.save(cover);

        var article = ArticleControllerTest.newArticle("testArticle", "test content", tags, cover, articleClass);
        articleRepository.save(article);
        return article;
    }

    /**
     * 获取评论字段
     */
    public static List<FieldDescriptor> getArticleCommentVoFieldsDescriptor(String prefix){
        var fields = new ArrayList<FieldDescriptor>();
        fields.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        fields.add(fieldWithPath(prefix + "content").type(JsonFieldType.STRING).description("内容"));
        fields.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "createdUser."));
        fields.add(fieldWithPath(prefix + "createdDate").type(JsonFieldType.STRING).description("评论时间"));
        fields.add(fieldWithPath(prefix + "agreeNum").type(JsonFieldType.NUMBER).description("获赞数量"));
        fields.add(fieldWithPath(prefix + "treadNum").type(JsonFieldType.NUMBER).description("获踩数量"));
        fields.add(fieldWithPath(prefix + "isAgreed").type(JsonFieldType.BOOLEAN).optional().description("是否已点赞"));
        fields.add(fieldWithPath(prefix + "isTrod").type(JsonFieldType.BOOLEAN).optional().description("是否已点踩"));
        return fields;
    }

    /**
     * 获取评论列表字段
     */
    public static List<FieldDescriptor> getArticleCommentListVoFieldsDescriptor(String prefix){
        var fields = getArticleCommentVoFieldsDescriptor(prefix);
        fields.add(fieldWithPath(prefix + "replies").type(JsonFieldType.ARRAY).description("子评论"));
        fields.addAll(getArticleCommentVoFieldsDescriptor(prefix + "replies[]."));
        fields.add(fieldWithPath(prefix + "repliesNum").type(JsonFieldType.NUMBER).description("下级评论总数"));
        return fields;
    }

    @Test
    @WithUserDetails("admin")
    void comment() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("very good");

        mockMvc.perform(
                post(basePath + "/article/{articleId}/comment",article.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleCommentDto))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("articleId").description("文章id")),
                        requestFields(fieldWithPath("content").type(JsonFieldType.STRING).description("评论内容")),
                        responseFields(getArticleCommentVoFieldsDescriptor(""))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void del() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("nice");
        var vo = articleCommentService.comment(article.getId(), articleCommentDto);

        mockMvc.perform(
                delete(basePath + "/article/{articleId}/comment/{id}", article.getId(), vo.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("articleId").description("文章id"), parameterWithName("id").description("评论id"))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void toggleAgree() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("nice");
        var vo = articleCommentService.comment(article.getId(), articleCommentDto);

        mockMvc.perform(
                post(basePath + "/article/{articleId}/comment/{id}/toggle-agree", article.getId(), vo.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("articleId").description("文章id"), parameterWithName("id").description("评论id")),
                        responseFields(fieldWithPath("value").type(JsonFieldType.BOOLEAN).description("切换后的状态"))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void toggleTread() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("nice");
        var vo = articleCommentService.comment(article.getId(), articleCommentDto);

        mockMvc.perform(
                post(basePath + "/article/{articleId}/comment/{id}/toggle-tread", article.getId(), vo.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("articleId").description("文章id"), parameterWithName("id").description("评论id")),
                        responseFields(fieldWithPath("value").type(JsonFieldType.BOOLEAN).description("切换后的状态"))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void getPage() throws Exception {
        var article = newArticle();
        //创建评论
        for (int i = 0; i < 20; i++) {
            var articleCommentDto = new ArticleCommentDto();
            articleCommentDto.setContent("comment-" + i);
            var vo = articleCommentService.comment(article.getId(), articleCommentDto);
            for (int j = 0, subCommentNum = (int)(Math.random() * 10 % 4); j < subCommentNum; j++) {
                articleCommentDto.setContent("sub-comment-" + j);
                articleSubCommentService.comment(vo.getId(), articleCommentDto);
            }
        }

        mockMvc.perform(
                get(basePath + "/article/{articleId}/comment", article.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("articleId").description("文章id")),
                        responseFields(getArticleCommentListVoFieldsDescriptor("content[].")).and(FIELDS_PAGE)
                ));
    }
}