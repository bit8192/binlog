package cn.bincker.web.blog.base.annotation;

import java.lang.annotation.*;

/**
 * 是否是接口控制器，用于添加路径前缀
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiController {
}
