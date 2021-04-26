package cn.bincker.web.blog.base.entity.converter;

import cn.bincker.web.blog.base.exception.SystemException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;

public abstract class JsonConverter implements AttributeConverter<Object, String> {
    protected final ObjectMapper objectMapper;

    public JsonConverter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String convertToDatabaseColumn(Object o) {
        if(o == null) return null;
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new SystemException("转换数据到json失败 \ttarget=" + o.toString(), e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String s) {
        JavaType javaType = getTargetType();
        if(s == null) return null;
        try {
            return objectMapper.readValue(s, javaType);
        } catch (JsonProcessingException e) {
            throw new SystemException("读取json数据失败 \tjson=" + s + "\tjavaType=" + javaType, e);
        }
    }

    abstract JavaType getTargetType();
}
