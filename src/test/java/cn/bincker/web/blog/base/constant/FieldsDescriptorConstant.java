package cn.bincker.web.blog.base.constant;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class FieldsDescriptorConstant {
    public static final FieldDescriptor[] FIELDS_PAGE = new FieldDescriptor[]{
            fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("是否最后一页"),
            fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("总页数"),
            fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("总条数"),
            fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("是否是第一页"),
            fieldWithPath("number").type(JsonFieldType.NUMBER).description("当前页"),
            fieldWithPath("size").type(JsonFieldType.NUMBER).description("当前分页数量"),
            fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER).description("当前条"),
    };
}
