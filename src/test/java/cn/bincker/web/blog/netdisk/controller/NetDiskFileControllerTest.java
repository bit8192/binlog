package cn.bincker.web.blog.netdisk.controller;

import cn.bincker.web.blog.AuthenticationTests;
import cn.bincker.web.blog.base.UserAuditingListener;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.repository.IBaseUserRepository;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.netdisk.config.properties.NetDiskFileSystemProperties;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.service.ISystemFileFactory;
import cn.bincker.web.blog.netdisk.dto.NetDiskFileDto;
import cn.bincker.web.blog.netdisk.vo.NetDiskFileVo;
import cn.bincker.web.blog.utils.CommonUtils;
import cn.bincker.web.blog.utils.SystemResourceUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
@Transactional
public class NetDiskFileControllerTest {
    private MockMvc mockMvc;

    @Value("${system.base-path}")
    private String basePath;

    @Autowired
    private INetDiskFileRepository netDiskFileRepository;
    @Autowired
    private ISystemFileFactory systemFileFactory;
    @Autowired
    private SystemResourceUtils systemResourceUtils;
    @Autowired
    private NetDiskFileSystemProperties netDiskFileSystemProperties;
    @Autowired
    private IUploadFileRepository uploadFileRepository;
    @Autowired
    private UserAuditingListener userAuditingListener;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IBaseUserRepository baseUserRepository;

    @BeforeEach
    public void beforeEach(
            WebApplicationContext applicationContext,
            RestDocumentationContextProvider documentationContextProvider
    ){
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(documentationContextProvider))
                .build();
    }

    @Test
    @WithUserDetails("admin")
    void getItem() throws Exception {
        var createUser = baseUserRepository.findByUsername("admin").orElseThrow();
        for (int i = 0; i < 10; i++) {
            var user = new BaseUser();
            user.setUsername("testuser" + i);
            user.setEncodedPasswd("");
            baseUserRepository.save(user);
        }
        var testFile = createFile("test.txt", "<<content>>", createUser, null);
        var allUserList = baseUserRepository.findAll();
        testFile.setReadableUserList(new HashSet<>(allUserList));
        testFile.setWritableUserList(new HashSet<>(allUserList));
        netDiskFileRepository.save(testFile);

        try {
            mockMvc.perform(
                    get(basePath + "/net-disk-files/{id}", testFile.getId()).contentType(MediaType.APPLICATION_JSON)
            )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(document(
                            "{ClassName}/{methodName}",
                            pathParameters(parameterWithName("id").description("id")),
                            responseFields(getNetDiskFileFields("").toArray(new FieldDescriptor[]{}))
                                    .and(
                                            fieldWithPath("readable").type(JsonFieldType.BOOLEAN).description("是否可读"),
                                            fieldWithPath("writable").type(JsonFieldType.BOOLEAN).description("是否可写")
                                    )
                    ));
        }finally {
            assertTrue(systemFileFactory.fromNetDiskFile(testFile).delete());
        }
    }

    @Test
    @WithUserDetails("admin")
    void listChildren() throws Exception {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow();
        var firstDir = createDirectory("first", currentUser, null);
        var secondDir = createDirectory("second", currentUser, null);
        var fileInFirstDir = createFile("first-file.txt", "hello world", currentUser, firstDir);
        var directoryInFirstDir = createDirectory("directory", currentUser, firstDir);

        try {
            mockMvc.perform(
                    get(basePath + "/net-disk-files")
            )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(document("{ClassName}/listRoot",
                            responseFields(getNetDiskFileListItemFields("[].").toArray(new FieldDescriptor[]{}))
                    ));

            mockMvc.perform(
                    get(basePath + "/net-disk-files").param("id", firstDir.getId().toString())
            )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(document("{ClassName}/listChildren",
                            responseFields(getNetDiskFileListItemFields("[].").toArray(new FieldDescriptor[]{}))
                    ));
        }finally {
            assertTrue(systemFileFactory.fromNetDiskFile(directoryInFirstDir).delete());
            assertTrue(systemFileFactory.fromNetDiskFile(fileInFirstDir).delete());
            assertTrue(systemFileFactory.fromNetDiskFile(secondDir).delete());
            assertTrue(systemFileFactory.fromNetDiskFile(firstDir).delete());
        }
    }

    @Test
    @WithUserDetails("admin")
    void getParents() throws Exception {
        var currentUser = userAuditingListener.getCurrentAuditor().orElseThrow();
        var topDir = createDirectory("top", currentUser, null);
        var secondDir = createDirectory("second", currentUser, topDir);
        var thirdDir = createDirectory("directory", currentUser, secondDir);
        var file = createFile("first-file.txt", "hello world", currentUser, thirdDir);
        try{
            mockMvc.perform(
                    get(basePath + "/net-disk-files/{id}/parents", file.getId())
            )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(document("{ClassName}/{methodName}",
                            pathParameters(parameterWithName("id").description("id")),
                            responseFields(getNetDiskFileListItemFields("[].").toArray(new FieldDescriptor[]{}))
                    ));
        }finally {
            assertTrue(systemFileFactory.fromNetDiskFile(file).delete());
            assertTrue(systemFileFactory.fromNetDiskFile(thirdDir).delete());
            assertTrue(systemFileFactory.fromNetDiskFile(secondDir).delete());
            assertTrue(systemFileFactory.fromNetDiskFile(topDir).delete());
        }
    }

    @Test
    @WithUserDetails("admin")
    void createDirectory() throws Exception {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow();
        var netDiskFileDto = new NetDiskFileDto();
        netDiskFileDto.setName("test");
        netDiskFileDto.setEveryoneReadable(true);
        netDiskFileDto.setEveryoneWritable(true);

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        var createDirectoryRequestFields = new FieldDescriptor[]{
                fieldWithPath("name").type(JsonFieldType.STRING).description("目录名称"),
                fieldWithPath("parentId").type(JsonFieldType.NUMBER).optional().description("父级目录[可选]"),
                fieldWithPath("everyoneReadable").type(JsonFieldType.BOOLEAN).optional().description("任何人可读(只有所有者可以分配权限)"),
                fieldWithPath("everyoneWritable").type(JsonFieldType.BOOLEAN).optional().description("任何人可写"),
                fieldWithPath("readableUserList").type(JsonFieldType.ARRAY).optional().description("可读用户列表"),
                fieldWithPath("writableUserList").type(JsonFieldType.ARRAY).optional().description("可写用户列表")
        };

        var result = mockMvc.perform(
                post(basePath + "/net-disk-files/directories").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(netDiskFileDto))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/createDirectoryInRoot",
                        requestFields(createDirectoryRequestFields),
                        responseFields(getNetDiskFileFields("").toArray(new FieldDescriptor[]{}))
                ))
                .andReturn();
        var responseVo = objectMapper.readValue(result.getResponse().getContentAsString(), NetDiskFileVo.class);
        var topDir = systemFileFactory.fromPath(systemResourceUtils.getUploadPath(user.getUsername() + File.separator + netDiskFileDto.getName()).getPath());
//        确保创建成功
        System.out.println(topDir.getPath());
        assertTrue(topDir.exists());

        netDiskFileDto.setName("sub-dir");
        netDiskFileDto.setParentId(responseVo.getId());
        mockMvc.perform(
                post(basePath + "/net-disk-files/directories").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(netDiskFileDto))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/createDirectoryInParent",
                        requestFields(createDirectoryRequestFields),
                        responseFields(getNetDiskFileFields("").toArray(new FieldDescriptor[]{}))
                ));
        var subDir = systemFileFactory.fromPath(topDir.getPath() + File.separator + netDiskFileDto.getName());
//        确保创建成功
        assertTrue(subDir.exists());

        //删除
        assertTrue(subDir.delete());
        assertTrue(topDir.delete());
    }

    @Test
    @WithUserDetails("admin")
    void uploadFile() throws Exception{
        var dto = new NetDiskFileDto();
        dto.setEveryoneReadable(false);
        dto.setEveryoneWritable(false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        var result = mockMvc.perform(
                fileUpload(basePath + "/net-disk-files/files")
                        .file(new MockMultipartFile("test.txt", "test.txt", MediaType.TEXT_PLAIN_VALUE, "<<file-content>>".getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("fileInfo", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(dto).getBytes(StandardCharsets.UTF_8)))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        requestPartFields("fileInfo",
                                fieldWithPath("parentId").type(JsonFieldType.NUMBER).optional().description("上级目录id[可选]"),
                                fieldWithPath("everyoneReadable").type(JsonFieldType.BOOLEAN).optional().description("任何人可读, 默认为假[可选]"),
                                fieldWithPath("everyoneWritable").type(JsonFieldType.BOOLEAN).optional().description("任何人可写，默认为假[可选]"),
                                fieldWithPath("readableUserList").type(JsonFieldType.ARRAY).optional().description("可读用户id列表[可选]"),
                                fieldWithPath("writableUserList").type(JsonFieldType.ARRAY).optional().description("可写用户id列表[可选]")
                        ),
                        responseFields(getNetDiskFileFields("[].").toArray(new FieldDescriptor[]{}))
                ))
                .andReturn();
        var typeFactory = objectMapper.getTypeFactory();
        var resultObject = (NetDiskFileVo[]) objectMapper.readValue(
                result.getResponse().getContentAsString(),
                typeFactory.constructArrayType(NetDiskFileVo.class)
        );
        var target = netDiskFileRepository.getOne(resultObject[0].getId());
        var targetFile = systemFileFactory.fromNetDiskFile(target);
        assertTrue(targetFile.exists());
        assertTrue(targetFile.delete());
    }

    @Test
    @WithUserDetails("admin")
    void download() throws Exception {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow();
        var file = createFile("test.txt", "hello world", user, null);
        mockMvc.perform(
                get(basePath + "/net-disk-files/{id}/file", file.getId())
        )
                .andDo(print())
                .andExpect(status().isOk());
        assertTrue(systemFileFactory.fromNetDiskFile(file).delete());
    }

    @Test
    @WithUserDetails("admin")
    void del() throws Exception {
        var user = userAuditingListener.getCurrentAuditor().orElseThrow();
        var file = createFile("test.txt", "hello world", user, null);
        mockMvc.perform(
                delete(basePath + "/net-disk-files/{id}", file.getId())
        )
                .andDo(print())
                .andExpect(status().isOk());
        assertFalse(systemFileFactory.fromNetDiskFile(file).exists());
    }

    public static Collection<FieldDescriptor> getNetDiskFileFields(String prefix){
        var result = new ArrayList<FieldDescriptor>();
        result.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        result.add(fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("名称"));
        result.add(fieldWithPath(prefix + "isDirectory").type(JsonFieldType.BOOLEAN).description("是否是目录"));
        result.add(fieldWithPath(prefix + "size").optional().type(JsonFieldType.NUMBER).description("大小，目录为0"));
        result.add(fieldWithPath(prefix + "createdDate").type(JsonFieldType.STRING).description("创建日期"));
        result.add(fieldWithPath(prefix + "lastModifiedDate").type(JsonFieldType.STRING).description("最后修改日期"));
        result.add(fieldWithPath(prefix + "possessor").type(JsonFieldType.OBJECT).description("所有人"));
        result.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "possessor."));
        result.add(fieldWithPath(prefix + "createdUser").type(JsonFieldType.OBJECT).description("创建人"));
        result.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "createdUser."));
        result.add(fieldWithPath(prefix + "lastModifiedUser").type(JsonFieldType.OBJECT).description("最后修改人"));
        result.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "lastModifiedUser."));
        result.add(fieldWithPath(prefix + "childrenNum").type(JsonFieldType.NUMBER).description("子节点数量"));
        result.add(fieldWithPath(prefix + "everyoneReadable").type(JsonFieldType.BOOLEAN).optional().description("任何人可读（所有者可见）"));
        result.add(fieldWithPath(prefix + "everyoneWritable").type(JsonFieldType.BOOLEAN).optional().description("任何人可写（所有者可见）"));
        result.add(fieldWithPath(prefix + "readableUserList").type(JsonFieldType.ARRAY).optional().description("可读用户列表"));
        result.add(fieldWithPath(prefix + "writableUserList").type(JsonFieldType.ARRAY).optional().description("可写用户列表"));
        result.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "readableUserList[]."));
        result.addAll(AuthenticationTests.getBaseUserVoFields(prefix + "writableUserList[]."));
        return result;
    }

    public static Collection<FieldDescriptor> getNetDiskFileListItemFields(String prefix){
        var result = new ArrayList<FieldDescriptor>();
        result.add(fieldWithPath(prefix + "id").type(JsonFieldType.NUMBER).description("id"));
        result.add(fieldWithPath(prefix + "name").type(JsonFieldType.STRING).description("名称"));
        result.add(fieldWithPath(prefix + "isDirectory").type(JsonFieldType.BOOLEAN).description("是否是目录"));
        result.add(fieldWithPath(prefix + "size").optional().type(JsonFieldType.NUMBER).description("大小，目录为0"));
        result.add(fieldWithPath(prefix + "createdDate").type(JsonFieldType.STRING).description("创建日期"));
        result.add(fieldWithPath(prefix + "lastModifiedDate").type(JsonFieldType.STRING).description("最后修改日期"));
        return result;
    }

    /**
     * 创建目录
     */
    private NetDiskFile createDirectory(String name, BaseUser user, NetDiskFile parent){
        var netDiskFile = new NetDiskFile();
        netDiskFile.setName(name);
        netDiskFile.setIsDirectory(true);
        if(parent != null){
            netDiskFile.setPath(parent.getPath() + File.separator + netDiskFile.getName());
            netDiskFile.setParent(parent);
            setParent(netDiskFile, parent);
        }else{
            netDiskFile.setPath(systemResourceUtils.getUploadPath(user.getUsername()).getPath() + File.separator + netDiskFile.getName());
        }
        netDiskFile.setPossessor(user);
        var file = systemFileFactory.fromNetDiskFile(netDiskFile);
        assertTrue(file.exists() || file.mkdir());
        return netDiskFileRepository.save(netDiskFile);
    }

    /**
     * 写出文件
     */
    private NetDiskFile createFile(String name, String content, BaseUser user, NetDiskFile parent){
        var uploadFile = new UploadFile();
        var netDiskFile = new NetDiskFile();
        uploadFile.setName(name);
        netDiskFile.setName(name);
        netDiskFile.setIsDirectory(false);
        uploadFile.setStorageLocation(netDiskFileSystemProperties.getType());
        if(parent != null){
            uploadFile.setPath(parent.getPath() + File.separator + name);
            netDiskFile.setPath(uploadFile.getPath());
            netDiskFile.setParent(parent);
            setParent(netDiskFile, parent);
        }else{
            uploadFile.setPath(systemResourceUtils.getUploadPath(user.getUsername()).getPath() + File.separator + name);
            netDiskFile.setPath(uploadFile.getPath());
        }
        uploadFile.setMediaType("");
        uploadFile.setSize(content.length());
        uploadFile.setSha256("");
        uploadFile.setSuffix(CommonUtils.getStringSuffix(name, "."));
        uploadFile.setIsPublic(false);
        netDiskFile.setUploadFile(uploadFile);
        netDiskFile.setPossessor(user);
        var file = systemFileFactory.fromNetDiskFile(netDiskFile);
        try(
                var in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                var out = file.getOutputStream()
        ){
            in.transferTo(out);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        uploadFileRepository.save(uploadFile);
        return netDiskFileRepository.save(netDiskFile);
    }


    /**
     * 设置父级
     */
    private void setParent(NetDiskFile netDiskFile, NetDiskFile parent) {
        var parents = new Long[parent.getParents().length + 1];
        if(parents.length > 1) System.arraycopy(parent.getParents(), 0, parents, 0, parent.getParents().length);
        parents[parents.length - 1] = parent.getId();
        netDiskFile.setParents(parents);
    }
}