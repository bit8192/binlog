package cn.bincker.web.blog.base.controller;

import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.exception.NotFoundException;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.base.dto.UploadFileDto;
import cn.bincker.web.blog.base.service.ISystemFileFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
class UploadControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private IUploadFileRepository uploadFileRepository;
    @Autowired
    private ISystemFileFactory systemFileFactory;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeTest(
            WebApplicationContext context,
            RestDocumentationContextProvider restDocumentationContextProvider
    ){
            mockMvc = MockMvcBuilders.webAppContextSetup(context)
                    .apply(documentationConfiguration(restDocumentationContextProvider))
                    .build();
    }

    @Test
    @Transactional
    void upload() throws Exception {
        var result = mockMvc.perform(
                fileUpload("/files")
                .file(new MockMultipartFile("filename", "test-file.txt", MediaType.TEXT_PLAIN_VALUE, "<<upload-content>>".getBytes(StandardCharsets.UTF_8)))
                .param("isPublic", "true")
        )
                .andExpect(status().isOk())
                .andDo(document("files"))
                .andReturn();
        var responseVo = ((UploadFileDto[])objectMapper.readValue(result.getResponse().getContentAsString(), objectMapper.getTypeFactory().constructArrayType(UploadFileDto.class)))[0];
        var uploadFile = uploadFileRepository.findById(responseVo.getId()).orElseThrow(NotFoundException::new);
        assertTrue(systemFileFactory.fromPath(uploadFile.getPath()).delete());
    }

    @Test
    @Transactional
    void get() throws Exception {
        var uploadFile = new UploadFile();
        uploadFile.setName("test-file.txt");
        uploadFile.setPath("public" + File.separator + uploadFile.getName());
        uploadFile.setIsPublic(true);
        uploadFile.setSize(0);
        uploadFile.setSha256("");
        uploadFile.setMediaType("");
        uploadFileRepository.save(uploadFile);
        var file = systemFileFactory.fromPath(uploadFile.getPath());
        try(var in = new ByteArrayInputStream("<<content>>".getBytes(StandardCharsets.UTF_8)); var out = file.getOutputStream()){
            in.transferTo(out);
        }
        try {
            mockMvc.perform(
                    RestDocumentationRequestBuilders.get("/files/{id}", uploadFile.getId())
            )
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andDo(document("files"));
        }finally {
            assertTrue(file.delete());
        }
    }
}
