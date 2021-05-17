package cn.bincker.web.blog.material.vo;

import cn.bincker.web.blog.material.entity.ArticleClass;
import lombok.Data;

@Data
public class ArticleClassVo {
    Long id;
    String title;
    Boolean visible;
    Integer orderNum;
    Long childrenNum;

    public ArticleClassVo() {
    }

    public ArticleClassVo(ArticleClass articleClass, Long childrenNum) {
        this.id = articleClass.getId();
        this.title = articleClass.getTitle();
        this.visible = articleClass.getVisible();
        this.orderNum = articleClass.getOrderNum();
        this.childrenNum = childrenNum;
    }
}
