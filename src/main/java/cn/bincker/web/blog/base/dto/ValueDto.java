package cn.bincker.web.blog.base.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ValueDto<T> {
    @NotEmpty
    private T value;

    public ValueDto() {
    }

    public ValueDto(T value) {
        this.value = value;
    }
}
