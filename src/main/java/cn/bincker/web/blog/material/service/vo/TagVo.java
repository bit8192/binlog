package cn.bincker.web.blog.material.service.vo;

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

    public TagVo(Long id, String title, Long articleNum) {
        this.id = id;
        this.title = title;
        this.articleNum = articleNum;
    }
}
