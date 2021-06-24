package cn.bincker.web.blog.base.vo;

import lombok.Data;

@Data
public class ValueVo<T> {
    private T value;

    public ValueVo(T value) {
        this.value = value;
    }
}
