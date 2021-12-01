package cn.bincker.web.blog.base.config;

import lombok.Data;

@Data
public class AliyunOssProperties {
    /**
     * 阿里云OSS Bucket所在地域对应的Endpoint
     */
    private String endpoint;

    /**
     * 阿里云OSS accessKeyId
     */
    private String accessKeyId;

    /**
     * 阿里云OSS accessKeySecret
     */
    private String accessKeySecret;

    /**
     * 阿里云OSS BucketName
     */
    private String bucketName;

    /**
     * 根存储路径
     */
    private String location = "";
}
