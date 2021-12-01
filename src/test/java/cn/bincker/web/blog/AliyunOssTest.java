package cn.bincker.web.blog;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.Bucket;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.io.EmptyInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AliyunOssTest {
    @Autowired
    private OSS oss;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listBuckets() {
        for (Bucket listBucket : oss.listBuckets()) {
            System.out.println(listBucket.getName());
        }
    }

    @Test
    void listFile() {
        for (var file : oss.listObjects("bincker", "").getObjectSummaries()) {
            System.out.println(file.getKey());
        }
    }

    @Test
    void getFileInfo() throws JsonProcessingException {
        var ossFile = oss.getObject("bincker", "game/red2_yuri_1002.rar");
        System.out.println(ossFile.getKey());
        System.out.println(objectMapper.writeValueAsString(ossFile.getObjectMetadata().getRawMetadata()));
    }

    @Test
    void mkdirs() {
        oss.putObject("bincker", "test/", EmptyInputStream.INSTANCE);
    }

    @Test
    void exist() {
        System.out.println(oss.doesObjectExist("bincker", "test"));
    }
}
