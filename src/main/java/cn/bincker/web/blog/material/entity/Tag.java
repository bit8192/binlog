package cn.bincker.web.blog.material.entity;

import cn.bincker.web.blog.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Tag extends BaseEntity {
    private String title;
}
