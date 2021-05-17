package cn.bincker.web.blog.base.entity.converter;

import com.fasterxml.jackson.databind.JavaType;

public class LongArrayConverter extends JsonConverter{
    @Override
    JavaType getTargetType() {
        return objectMapper.getTypeFactory().constructArrayType(Long.class);
    }
}
