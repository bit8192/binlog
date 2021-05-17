package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.material.entity.Tag;
import lombok.Data;

@Data
public class TagVo {
    private Long id;
    private String title;
    /**
     * 所涵盖文章数量
     */
    private Long articleNum;

    public TagVo() {
    }

    public TagVo(Tag tag, Long articleNum) {
        this.id = tag.getId();
        this.title = tag.getTitle();
        this.articleNum = articleNum;
    }

    public TagVo(Long id, String title, Long articleNum) {
        this.id = id;
        this.title = title;
        this.articleNum = articleNum;
    }
}
