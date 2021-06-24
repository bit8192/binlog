package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NamedEntityGraph(name = "Tag.articleList", attributeNodes = {@NamedAttributeNode("articleList")})
public class Tag extends BaseEntity {
    @Column(unique = true)
    private String title;

    @ManyToMany(mappedBy = "tags")
    private List<Article> articleList = new ArrayList<>();
}
