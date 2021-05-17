package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.material.entity.Article;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileListVo;
import lombok.Data;

import java.util.Collection;
import java.util.stream.Collectors;

@Data
public class ArticleVo {
    private Long id;
    private String title;
    private Boolean recommend;
    private Boolean top;
    private String describe;
    private String content;
    private Collection<TagVo> tags;
    private NetDiskFileListVo cover;
    private String[] images;
    private ArticleClassVo articleClass;
    private String className;
    private Boolean isOriginal;
    private Long viewingNum;
    private Long agreedNum;
    private Long commentNum;
    private Long forwardingNum;

    public ArticleVo() {
    }

    public ArticleVo(Article article) {
        id = article.getId();
        title = article.getTitle();
        recommend = article.getRecommend();
        top = article.getTop();
        describe = article.getDescribe();
        content = article.getContent();
        tags = article.getTags().stream().map(t->new TagVo(t, null)).collect(Collectors.toSet());
        cover = new NetDiskFileListVo(article.getCover(), null);
        images = article.getImages();
        articleClass = new ArticleClassVo(article.getArticleClass(), null);
        isOriginal = article.getIsOriginal();
        viewingNum = article.getViewingNum();
        agreedNum = article.getAgreedNum();
        commentNum = article.getCommentNum();
        forwardingNum = article.getForwardingNum();
    }
}
