package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.material.entity.Article;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ArchiveVo {
    private Long id;
    private String title;
    private Date createdDate;

    public ArchiveVo(Article article){
        this.id = article.getId();
        this.title = article.getTitle();
        this.createdDate = article.getCreatedDate();
    }
}
