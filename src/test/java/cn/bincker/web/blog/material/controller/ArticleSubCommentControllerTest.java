package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.material.dto.ArticleCommentDto;
import cn.bincker.web.blog.material.dto.ArticleSubCommentDto;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;

import static cn.bincker.web.blog.base.constant.FieldsDescriptorConstant.FIELDS_PAGE;
import static org.junit.jupiter.api.Assertions.*;
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
class ArticleSubCommentControllerTest {
    private MockMvc mockMvc;
    @Value("${system.base-path}")
    private String basePath;
    @Autowired
    private IBaseUserRepository userRepository;
    @Autowired
    private IArticleRepository articleRepository;
    @Autowired
    private IArticleClassRepository articleClassRepository;
    @Autowired
    private ITagRepository tagRepository;
    @Autowired
    private INetDiskFileRepository netDiskFileRepository;
    @Autowired
    private IArticleCommentService articleCommentService;
    @Autowired
    private IArticleSubCommentService articleSubCommentService;
    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @WithUserDetails("admin")
    void comment() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("articleComment");
        articleCommentDto.setArticleId(article.getId());
        var articleCommentVo = articleCommentService.comment(articleCommentDto);

        var articleSubCommentDto = new ArticleSubCommentDto();
        articleSubCommentDto.setContent("article sub comment");
        articleSubCommentDto.setCommentId(articleCommentVo.getId());
        mockMvc.perform(
                post(basePath + "/sub-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(articleCommentDto))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        requestFields(ArticleCommentControllerTest.getArticleCommentDtoFields("")),
                        responseFields(ArticleCommentControllerTest.getArticleCommentVoFieldsDescriptor(""))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void del() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("articleComment");
        articleCommentDto.setArticleId(article.getId());
        var articleCommentVo = articleCommentService.comment(articleCommentDto);
        var articleSubCommentDto = new ArticleSubCommentDto();
        articleSubCommentDto.setContent("article sub comment");
        articleSubCommentDto.setCommentId(articleCommentVo.getId());
        var articleSubCommentVo = articleSubCommentService.comment(articleSubCommentDto);

        mockMvc.perform(
                delete(basePath + "/sub-comments/{id}", articleSubCommentVo.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("id").description("要删除的评论id"))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void toggleAgree() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("articleComment");
        articleCommentDto.setArticleId(article.getId());
        var articleCommentVo = articleCommentService.comment(articleCommentDto);
        var articleSubCommentDto = new ArticleSubCommentDto();
        articleSubCommentDto.setContent("article sub comment content");
        articleSubCommentDto.setCommentId(articleCommentVo.getId());
        var articleSubCommentVo = articleSubCommentService.comment(articleSubCommentDto);

        mockMvc.perform(
                post(basePath + "/sub-comments/{id}/toggle-agree", articleSubCommentVo.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("id").description("要删除的评论id")),
                        responseFields(fieldWithPath("value").type(JsonFieldType.BOOLEAN).description("切换后的状态"))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void toggleTread() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("articleComment");
        articleCommentDto.setArticleId(article.getId());
        var articleCommentVo = articleCommentService.comment(articleCommentDto);
        var articleSubCommentDto = new ArticleSubCommentDto();
        articleSubCommentDto.setContent("article sub comment");
        articleSubCommentDto.setCommentId(articleCommentVo.getId());
        var articleSubCommentVo = articleSubCommentService.comment(articleSubCommentDto);

        mockMvc.perform(
                post(basePath + "/sub-comments/{id}/toggle-tread", articleSubCommentVo.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("id").description("要删除的评论id")),
                        responseFields(fieldWithPath("value").type(JsonFieldType.BOOLEAN).description("切换后的状态"))
                ));
    }

    @Test
    @WithUserDetails("admin")
    void getPage() throws Exception {
        var article = newArticle();
        var articleCommentDto = new ArticleCommentDto();
        articleCommentDto.setContent("articleComment");
        articleCommentDto.setArticleId(article.getId());
        var articleCommentVo = articleCommentService.comment(articleCommentDto);
        for (int i = 0; i < 20; i++) {
            var articleSubCommentDto = new ArticleSubCommentDto();
            articleSubCommentDto.setContent("article sub comment " + i);
            articleSubCommentDto.setCommentId(articleCommentVo.getId());
            articleSubCommentService.comment(articleSubCommentDto);
        }

        mockMvc.perform(
                get(basePath + "/comments/{commentId}/sub-comments", articleCommentVo.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("commentId").description("上级评论id")),
                        responseFields(ArticleCommentControllerTest.getArticleCommentVoFieldsDescriptor("content[].")).and(FIELDS_PAGE)
                ));
    }
}
