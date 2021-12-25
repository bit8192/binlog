package cn.bincker.web.blog.password.entity;

import cn.bincker.web.blog.base.entity.AuditEntity;
import cn.bincker.web.blog.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "password_profile")
@Getter
@Setter
public class PasswordProfile extends AuditEntity {
    /**
     * 密码输入混淆
     * 这是一串自定义的字符串操作指令
     * 操作指令均是js中字符串的方法如sub、substr、substring、indexOf等等
     * 操作指令支持传参、支持嵌套
     * 多个操作以分号分隔，即“;”
     */
    @Column(columnDefinition = "text")
    private String inputConvert;
}
