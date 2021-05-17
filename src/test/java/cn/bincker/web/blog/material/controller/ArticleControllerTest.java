package cn.bincker.web.blog.material.controller;

import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.material.dto.ArticleDto;
import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.material.entity.ArticleClass;
import cn.bincker.web.blog.material.entity.Tag;
import cn.bincker.web.blog.material.repository.IArticleClassRepository;
import cn.bincker.web.blog.material.repository.IArticleRepository;
import cn.bincker.web.blog.material.repository.ITagRepository;
import cn.bincker.web.blog.material.vo.ArticleVo;
import cn.bincker.web.blog.netdisk.controller.NetDiskFileControllerTest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static cn.bincker.web.blog.base.constant.FieldsDescriptorConstant.FIELDS_PAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
@Transactional
class ArticleControllerTest {
    private MockMvc mockMvc;

    @Value("${system.base-path}")
    private String basePath;

    @Autowired
    private IArticleRepository articleRepository;
    @Autowired
    private ITagRepository tagRepository;
    @Autowired
    private IArticleClassRepository articleClassRepository;
    @Autowired
    private IBaseUserRepository baseUserRepository;
    @Autowired
    private INetDiskFileRepository netDiskFileRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach(WebApplicationContext applicationContext, RestDocumentationContextProvider documentationContextProvider){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(documentationContextProvider))
                .build();
    }

    @Test
    void getDetail() throws Exception{
        var tag = TagControllerTest.newTag("test-tag");
        tagRepository.save(tag);
        var cover = newCover();
        netDiskFileRepository.save(cover);
        var articleClass = ArticleClassControllerTest.newArticleClass("test article class", 0, true, null);
        articleClassRepository.save(articleClass);
        var article = newArticle("test article", "test article content", Collections.singleton(tag), cover, articleClass);
        articleRepository.save(article);
        mockMvc.perform(
                get(basePath + "/article/{id}", article.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        responseFields(getArticleDetailFieldDescriptors(""))
                ));
    }

    @Test
    void insertAndUpdate() throws Exception {
        var dto = new ArticleDto();
        dto.setTitle("test title");
        dto.setDescribe("describe content");
        dto.setIsOriginal(true);
        dto.setRecommend(true);
        dto.setIsPublic(true);
        dto.setTop(true);
        var cover = newCover();
        netDiskFileRepository.save(cover);
        var coverId = cover.getId();
        cover = new NetDiskFile();
        cover.setId(coverId);
        dto.setCover(cover);
        dto.setContent("content");
        var articleClass = ArticleClassControllerTest.newArticleClass("test-class", 0, true, null);
        articleClassRepository.save(articleClass);
        var articleClassId = articleClass.getId();
        articleClass = new ArticleClass();
        articleClass.setId(articleClassId);
        dto.setArticleClass(articleClass);
        var tags = new HashSet<Tag>();
        for (int i = 0; i < 2; i++) {
            var tag = TagControllerTest.newTag("tag" + i);
            tagRepository.save(tag);
            var tagId = tag.getId();
            tag = new Tag();
            tag.setId(tagId);
            tags.add(tag);
        }
        dto.setTags(tags);

        var insertResult = mockMvc.perform(
                post(basePath + "/article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8.name())
        )
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "{ClassName}/insert",
                        requestFields(REQUEST_FIELDS),
                        responseFields(getArticleDetailFieldDescriptors("").toArray(new FieldDescriptor[]{}))
                ))
                .andReturn();
        var responseArticle = objectMapper.readValue(insertResult.getResponse().getContentAsString(), ArticleVo.class);


        dto.setId(responseArticle.getId());
        dto.setTitle("changed title");
        mockMvc.perform(
                put(basePath + "/article")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8.name())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andDo(document(
                        "{ClassName}/update",
                        requestFields(fieldWithPath("id").type(JsonFieldType.NUMBER).description("修改用的id")).and(REQUEST_FIELDS),
                        responseFields(getArticleDetailFieldDescriptors(""))
                ));
    }

    @Test
    void del() throws Exception {
        var cover = newCover();
        netDiskFileRepository.save(cover);
        var articleClass = ArticleClassControllerTest.newArticleClass("test class", 0, true, null);
        articleClassRepository.save(articleClass);
        var article = newArticle("test title", "content", Collections.emptySet(), cover, articleClass);
        articleRepository.save(article);
        mockMvc.perform(
                delete(basePath + "/article/{id}", article.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/delete",
                        pathParameters(parameterWithName("id").description("删除对象id"))
                ));

        assertFalse(articleRepository.existsById(article.getId()));
    }

    @Test
    void pageAll() throws Exception {
        var articleClass = ArticleClassControllerTest.newArticleClass("test class", 0, true, null);
        articleClassRepository.save(articleClass);
        var tag = TagControllerTest.newTag("tag");
        tagRepository.save(tag);
        buildPageData(30, articleClass, Collections.singleton(tag));
        mockMvc.perform(
                get(basePath + "/article")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        responseFields(getArticleListFieldDescriptors("content[].")).and(FIELDS_PAGE)
                ));
    }

    @Test
    void pageByArticleClass() throws Exception {
        var articleClass = ArticleClassControllerTest.newArticleClass("test class", 0, true, null);
        articleClassRepository.save(articleClass);
        var tag = TagControllerTest.newTag("tag");
        tagRepository.save(tag);
        buildPageData(40, articleClass, Collections.singleton(tag));
        mockMvc.perform(
                get(basePath + "/article/search/article-class/{id}", articleClass.getId())
                        .param("page", "1")
                        .param("size", "20")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        requestParameters(parameterWithName("page").description("分页"), parameterWithName("size").description("分页数量")),
                        pathParameters(parameterWithName("id").description("文章分类id")),
                        responseFields(getArticleListFieldDescriptors("content[].")).and(FIELDS_PAGE)
                ));
    }

    @Test
    void pageByTag() throws Exception {
        var articleClass = ArticleClassControllerTest.newArticleClass("test class", 0, true, null);
        articleClassRepository.save(articleClass);
        var tag = TagControllerTest.newTag("tag");
        tagRepository.save(tag);
        buildPageData(20, articleClass, Collections.singleton(tag));
        mockMvc.perform(
                get(basePath + "/article/search/tag/{id}", tag.getId())
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        pathParameters(parameterWithName("id").description("标签id")),
                        responseFields(getArticleListFieldDescriptors("content[].")).and(FIELDS_PAGE)
                ));
    }

    @Test
    void pageByKeywords() throws Exception {
        var articleClass = ArticleClassControllerTest.newArticleClass("test class", 0, true, null);
        articleClassRepository.save(articleClass);
        var tag = TagControllerTest.newTag("tag");
        tagRepository.save(tag);
        buildPageData(20, articleClass, Collections.singleton(tag));
        mockMvc.perform(
                get(basePath + "/article/search/keywords")
                        .param("keywords", "title 1")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "{ClassName}/{methodName}",
                        requestParameters(parameterWithName("keywords").description("关键字")),
                        responseFields(getArticleListFieldDescriptors("content[].")).and(FIELDS_PAGE)
                ));
    }

    private void buildPageData(int size, ArticleClass articleClass, Set<Tag> tags){
        var cover = newCover();
        netDiskFileRepository.save(cover);
        for (int i = 0; i < size; i++) {
            var article = newArticle("test title " + i, "content" + i, tags, cover, articleClass);
            if(i < size - 3) {
                article.setTop(false);
                article.setRecommend(false);
            }
            if(i < size - 6) article.setRecommend(false);
            articleRepository.save(article);
        }
    }

    private static final FieldDescriptor[] REQUEST_FIELDS = new FieldDescriptor[]{
            fieldWithPath("title").type(JsonFieldType.STRING).description("标题"),
            fieldWithPath("recommend").type(JsonFieldType.BOOLEAN).description("是否推荐"),
            fieldWithPath("top").type(JsonFieldType.BOOLEAN).description("是否置顶"),
            fieldWithPath("isPublic").type(JsonFieldType.BOOLEAN).description("是否公开"),
            fieldWithPath("isOriginal").type(JsonFieldType.BOOLEAN).description("是否原创"),
            fieldWithPath("orderNum").type(JsonFieldType.NUMBER).optional().description("排序"),
            fieldWithPath("describe").type(JsonFieldType.STRING).description("摘要"),
            fieldWithPath("tags[].id").type(JsonFieldType.NUMBER).optional().description("标签id"),
            fieldWithPath("articleClass.id").type(JsonFieldType.NUMBER).description("分类id"),
            fieldWithPath("cover.id").type(JsonFieldType.NUMBER).description("封面id"),
            fieldWithPath("cover.isDirectory").ignored(),
            fieldWithPath("cover.parents").ignored(),
            fieldWithPath("cover.everyoneReadable").ignored(),
            fieldWithPath("cover.everyoneWritable").ignored(),
            fieldWithPath("cover.readableUserList").ignored(),
            fieldWithPath("cover.writableUserList").ignored(),
            fieldWithPath("content").type(JsonFieldType.STRING).description("正文"),
    };

    /**
     * 获取文章字段描述
     */
    public static List<FieldDescriptor> getArticleDetailFieldDescriptors(String prefix){
        var result = new ArrayList<FieldDescriptor>();
        result.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        result.add(fieldWithPath(prefix + "title").type(JsonFieldType.STRING).description("标题"));
        result.add(fieldWithPath(prefix + "recommend").type(JsonFieldType.BOOLEAN).optional().description("是否推荐"));
        result.add(fieldWithPath(prefix + "top").type(JsonFieldType.BOOLEAN).optional().description("是否置顶"));
        result.add(fieldWithPath(prefix + "describe").type(JsonFieldType.STRING).description("简述"));
        result.add(fieldWithPath(prefix + "content").type(JsonFieldType.STRING).description("文章内容"));
        result.add(fieldWithPath(prefix + "tags").type(JsonFieldType.ARRAY).optional().description("标签列表"));
        result.add(fieldWithPath(prefix + "tags[].id").type(JsonFieldType.NUMBER).optional().description("标签id"));
        result.add(fieldWithPath(prefix + "tags[].title").type(JsonFieldType.STRING).optional().description("标签标题"));
        result.addAll(NetDiskFileControllerTest.getNetDiskFileListItemFields(prefix + "cover."));
        result.add(fieldWithPath(prefix + "images").type(JsonFieldType.ARRAY).optional().description("图集"));
        result.addAll(ArticleClassControllerTest.getArticleClassFields(prefix + "articleClass."));
        result.add(fieldWithPath(prefix + "isOriginal").type(JsonFieldType.BOOLEAN).optional().description("是否原创"));
        result.add(fieldWithPath(prefix + "viewingNum").type(JsonFieldType.NUMBER).description("阅读量"));
        result.add(fieldWithPath(prefix + "agreedNum").type(JsonFieldType.NUMBER).description("点赞量"));
        result.add(fieldWithPath(prefix + "commentNum").type(JsonFieldType.NUMBER).description("评论量"));
        result.add(fieldWithPath(prefix + "forwardingNum").type(JsonFieldType.NUMBER).description("转发量"));
        return result;
    }

    /**
     * 获取文章字段描述
     */
    public static List<FieldDescriptor> getArticleListFieldDescriptors(String prefix){
        var result = new ArrayList<FieldDescriptor>();
        result.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        result.add(fieldWithPath(prefix + "title").type(JsonFieldType.STRING).description("标题"));
        result.add(fieldWithPath(prefix + "recommend").type(JsonFieldType.BOOLEAN).optional().description("是否推荐"));
        result.add(fieldWithPath(prefix + "top").type(JsonFieldType.BOOLEAN).optional().description("是否置顶"));
        result.add(fieldWithPath(prefix + "describe").type(JsonFieldType.STRING).description("简述"));
        result.add(fieldWithPath(prefix + "tags").type(JsonFieldType.ARRAY).optional().description("标签列表"));
        result.add(fieldWithPath(prefix + "tags[].id").type(JsonFieldType.NUMBER).optional().description("标签id"));
        result.add(fieldWithPath(prefix + "tags[].title").type(JsonFieldType.STRING).optional().description("标签标题"));
        result.addAll(NetDiskFileControllerTest.getNetDiskFileListItemFields(prefix + "cover."));
        result.add(fieldWithPath(prefix + "images").type(JsonFieldType.ARRAY).optional().description("图集"));
        result.addAll(ArticleClassControllerTest.getArticleClassFields(prefix + "articleClass."));
        result.add(fieldWithPath(prefix + "isOriginal").type(JsonFieldType.BOOLEAN).optional().description("是否原创"));
        result.add(fieldWithPath(prefix + "viewingNum").type(JsonFieldType.NUMBER).description("阅读量"));
        result.add(fieldWithPath(prefix + "agreedNum").type(JsonFieldType.NUMBER).description("点赞量"));
        result.add(fieldWithPath(prefix + "commentNum").type(JsonFieldType.NUMBER).description("评论量"));
        result.add(fieldWithPath(prefix + "forwardingNum").type(JsonFieldType.NUMBER).description("转发量"));
        return result;
    }

    private NetDiskFile newCover(){
        var user = baseUserRepository.findByUsername("admin").orElseThrow();
        var cover = new NetDiskFile();
        cover.setName("cover.jpg");
        cover.setIsDirectory(false);
        cover.setPath("");
        cover.setPossessor(user);
        cover.setCreatedUser(user);
        cover.setLastModifiedUser(user);
        return cover;
    }

    public static Article newArticle(String title, String content, Set<Tag> tags, NetDiskFile cover, ArticleClass articleClass){
        var article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setRecommend(true);
        article.setTop(true);
        article.setIsPublic(true);
        article.setOrderNum(0);
        article.setDescribe(content);
        article.setTags(tags);
        article.setCover(cover);
        article.setArticleClass(articleClass);
        article.setIsOriginal(true);
        article.setViewingNum((long) (Math.random() * 100));
        article.setAgreedNum((long) (Math.random() * 100));
        article.setCommentNum((long) (Math.random() * 100));
        article.setForwardingNum((long) (Math.random() * 100));
        return article;
    }
}