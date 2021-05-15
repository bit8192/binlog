package cn.bincker.web.blog.netdisk.service.impl;

import cn.bincker.web.blog.base.entity.AuthorizationUser;
import cn.bincker.web.blog.base.entity.BaseUser;
import cn.bincker.web.blog.base.entity.UploadFile;
import cn.bincker.web.blog.base.repository.IUploadFileRepository;
import cn.bincker.web.blog.base.service.IBaseUserService;
import cn.bincker.web.blog.netdisk.entity.NetDiskFile;
import cn.bincker.web.blog.netdisk.repository.INetDiskFileRepository;
import cn.bincker.web.blog.netdisk.service.INetDiskFileService;
import cn.bincker.web.blog.netdisk.service.dto.NetDiskFilePostDto;
import cn.bincker.web.blog.netdisk.service.dto.NetDiskFilePutDto;
import cn.bincker.web.blog.utils.SystemResourceUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class NetDiskFileServiceImplTest {
    @Autowired
    private INetDiskFileService netDiskFileService;
    @Autowired
    private INetDiskFileRepository netDiskFileRepository;
    @Autowired
    private IBaseUserService userService;
    @Autowired
    private SystemResourceUtils systemResourceUtils;
    @Autowired
    private IUploadFileRepository uploadFileRepository;

    @Test
    @WithUserDetails("admin")
    void add() {
        var dto = new NetDiskFilePostDto();
        dto.setName("test");
        dto.setEveryoneReadable(false);
        dto.setEveryoneWritable(false);
        var netDiskFileVo = netDiskFileService.add(dto);
        assertEquals(netDiskFileVo.getName(), dto.getName());
        var netDiskFile = netDiskFileRepository.findById(netDiskFileVo.getId());
        assertTrue(netDiskFile.isPresent());
        var file = new File(netDiskFile.get().getPath());
        assertTrue(file.exists());
        assertTrue(file.delete());
    }

    @Test
    @WithUserDetails("admin")
    void del() throws IOException {
        var user = ((AuthorizationUser)userService.loadUserByUsername("admin")).getBaseUser();
        var parent = createDirectory("top", user, null);
        var allFiles = new ArrayList<NetDiskFile>();
        allFiles.add(parent);
        for (int i = 0; i < 3; i++) {
//            随机创建子目录
            var randomDirectoryNum = (int)(Math.random() * 5) + 1;
            var directories = new NetDiskFile[randomDirectoryNum];
            for (int j = 0; j < randomDirectoryNum; j++) {
                directories[j] = createDirectory("sub-directory-" + i + "-" + j, user, parent);
            }
            allFiles.addAll(Arrays.asList(directories));
//            随机创建子文件
            var randomFileNum = (int)(Math.random() * 3);
            for (int j = 0; j < randomFileNum; j++) {
                var netDiskFile = createEmptyFile("sub-file-" + i + "-" + j + ".txt", user, parent);
                allFiles.add(netDiskFile);
            }
            var index = (int) (Math.random() * randomDirectoryNum);
            parent = directories[index];
        }
        netDiskFileService.delete(allFiles.get(0).getId());
        for (NetDiskFile allFile : allFiles) {
            assertFalse(netDiskFileRepository.existsById(allFile.getId()));
            if(!allFile.getIsDirectory()) assertFalse(uploadFileRepository.existsById(allFile.getUploadFile().getId()));
            assertFalse(new File(allFile.getPath()).exists());
        }
    }

    @Test
    @WithUserDetails("admin")
    void save() throws IOException {
        var user = ((AuthorizationUser)userService.loadUserByUsername("admin")).getBaseUser();
        var topDir = createDirectory("topDir", user, null);
        var subDir = createDirectory("subDir", user, topDir);
        var targetNetDiskFile = createEmptyFile("test.txt", user, null);

        //先最底层目录同时修改文件名
        var dto = new NetDiskFilePutDto();
        dto.setId(targetNetDiskFile.getId());
        dto.setName("changed-text.txt");
        dto.setParentId(subDir.getId());
        var result = netDiskFileService.save(dto);
        assertEquals(result.getName(), dto.getName());
        assertTrue(new File(subDir.getPath() + File.separator + dto.getName()).exists());
        var uploadFile = uploadFileRepository.getOne(targetNetDiskFile.getUploadFile().getId());
        assertEquals(uploadFile.getName(), dto.getName());
        assertTrue(new File(uploadFile.getPath()).exists());

        //然后移动到上级目录
        dto.setParentId(topDir.getId());
        netDiskFileService.save(dto);
        assertTrue(new File(topDir.getPath() + File.separator + dto.getName()).exists());
        uploadFile = uploadFileRepository.getOne(uploadFile.getId());
        assertTrue(new File(uploadFile.getPath()).exists());

        //然后移回根目录
        dto.setParentId(null);
        netDiskFileService.save(dto);
        assertTrue(new File(systemResourceUtils.getUploadPath(user.getUsername()), dto.getName()).exists());
        uploadFile = uploadFileRepository.getOne(uploadFile.getId());
        assertTrue(new File(uploadFile.getPath()).exists());

        //然后删除测试目录
        assertTrue(new File(targetNetDiskFile.getPath()).delete());
        assertTrue(new File(subDir.getPath()).delete());
        assertTrue(new File(topDir.getPath()).delete());
    }

    @Test
    @WithUserDetails("admin")
    void listRoot() throws IOException {
        var user = userService.getByUsername("admin");
        var firstDir = createDirectory("first", user, null);
        var secondDir = createDirectory("second", user, null);
        var thirdDir = createDirectory("third", user, null);
        var firstDirSubDir = createDirectory("first-first", user, firstDir);
        var secondDirFile = createEmptyFile("test.txt", user, secondDir);

        var result = netDiskFileService.listCurrentUserRoot();
        assertEquals(result.size(), 3);
        assertTrue(result.stream().anyMatch(item->item.getName().equals(firstDir.getName())));
        assertTrue(result.stream().anyMatch(item->item.getName().equals(secondDir.getName())));
        assertTrue(result.stream().anyMatch(item->item.getName().equals(thirdDir.getName())));

        assertTrue(new File(secondDirFile.getPath()).delete());
        assertTrue(new File(firstDirSubDir.getPath()).delete());
        assertTrue(new File(thirdDir.getPath()).delete());
        assertTrue(new File(secondDir.getPath()).delete());
        assertTrue(new File(firstDir.getPath()).delete());
    }

    @Test
    @WithUserDetails("admin")
    void listChildren() throws IOException {
        var user = userService.getByUsername("admin");
        var topDir = createDirectory("top", user, null);
        var subDir = createDirectory("subDir", user, topDir);
        var subFile = createEmptyFile("test.txt", user, topDir);

        var result = netDiskFileService.listChildren(topDir.getId());
        assertEquals(result.size(), 2);
        assertTrue(result.stream().anyMatch(item->item.getName().equals(subDir.getName())));
        assertTrue(result.stream().anyMatch(item->item.getName().equals(subFile.getName()) && !item.getIsDirectory()));

        assertTrue(new File(subFile.getPath()).delete());
        assertTrue(new File(subDir.getPath()).delete());
        assertTrue(new File(topDir.getPath()).delete());
    }

    /**
     * 创建目录
     */
    private NetDiskFile createDirectory(String name, BaseUser user, NetDiskFile parent) {
        var result = new NetDiskFile();
        result.setName(name);
        if(parent == null){
            result.setPath(systemResourceUtils.getUploadPath(user.getUsername() + File.separator + result.getName()).getPath());
        }else{
            result.setPath(parent.getPath() + File.separator + result.getName());
            result.setParent(parent);
        }
        result.setPossessor(user);
        netDiskFileRepository.save(result);
        var file = new File(result.getPath());
        if(!file.exists()) assertTrue(file.mkdir());
        return result;
    }

    /**
     * 创建空文件
     */
    private NetDiskFile createEmptyFile(String name, BaseUser user, NetDiskFile parent) throws IOException {
        var result = new NetDiskFile();
        var uploadFile = new UploadFile();
        result.setIsDirectory(false);
        result.setName(name);
        uploadFile.setName(result.getName());
        if(parent == null){
            result.setPath(systemResourceUtils.getUploadPath(user.getUsername()).getPath() + File.separator + result.getName());
        }else{
            result.setPath(parent.getPath() + File.separator + result.getName());
            result.setParent(parent);
        }
        uploadFile.setPath(result.getPath());
        uploadFile.setSha256("");
        result.setPossessor(user);
        result.setUploadFile(uploadFile);
        netDiskFileRepository.save(result);
        uploadFileRepository.save(uploadFile);
        var file = new File(result.getPath());
        if(!file.exists()) assertTrue(file.createNewFile());
        return result;
    }
}