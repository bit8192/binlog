package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ArticleClass extends BaseEntity {
    private String title;

    private Boolean visible;

    private Integer orderNum;

    @ManyToOne
    private ArticleClass parent;

    @OneToMany(mappedBy = "parent")
    private List<ArticleClass> children;
}
