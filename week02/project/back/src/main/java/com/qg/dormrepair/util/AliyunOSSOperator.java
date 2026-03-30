package com.qg.dormrepair.util;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.qg.dormrepair.config.AliyunOSSProperties;
import com.qg.dormrepair.constants.MessageConstant;
import com.qg.dormrepair.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS文件上传工具类
 * 提供图片/文件上传到阿里云OSS的通用功能，自动生成文件名、目录、返回访问地址
 *
 * @author qg
 * @date 2026-03-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunOSSOperator {

    /**
     * 阿里云OSS配置属性
     */
    private final AliyunOSSProperties aliyunOSSProperties;

    /**
     * 上传文件到阿里云OSS
     *
     * @param content 文件字节数组
     * @param originalFilename 原始文件名
     * @return 上传后文件的访问URL
     * @throws Exception 上传过程中出现异常直接抛出
     */
    public String upload(byte[] content, String originalFilename) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String region = aliyunOSSProperties.getRegion();
        String bucketName = aliyunOSSProperties.getBucketName();

        // 生成上传目录：按日期归类  yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        // 生成唯一文件名：UUID + 后缀名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + getFileExtension(originalFilename);
        // OSS中的完整文件路径
        String objectName = dir + "/" + newFileName;

        OSS ossClient = null;
        try {
            // 从环境变量获取AK/SK凭证
            EnvironmentVariableCredentialsProvider credentialsProvider =
                    CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

            // OSS客户端配置
            ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

            // 创建OSS客户端
            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(clientBuilderConfiguration)
                    .region(region)
                    .build();

            // 执行上传
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
            log.info("图片上传至OSS成功，文件名：{}", objectName);

            // 拼接返回可访问的URL
            return endpoint.split("://")[0] + "://" + bucketName + "." + endpoint.split("://")[1] + "/" + objectName;

        } catch (OSSException e) {
            log.error(MessageConstant.OSS_SERVICE_ERROR+"，上传失败：{}", e.getMessage());
            throw new BusinessException(500,MessageConstant.OSS_SERVICE_ERROR + e.getErrorMessage());
        } catch (Exception e) {
            log.error(MessageConstant.IMAGE_UPLOAD_FAILED+"：{}", e.getMessage());
            throw new BusinessException(500,MessageConstant.IMAGE_UPLOAD_FAILED + e.getMessage());
        } finally {
            // 关闭OSS客户端，释放资源
            if (ossClient != null) {
                try {
                    ossClient.shutdown();
                    log.debug("OSS客户端已正常关闭");
                } catch (Exception e) {
                    log.warn("关闭OSS客户端失败：{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 获取文件扩展名（如 .png、.jpg）
     *
     * @param filename 原始文件名
     * @return 扩展名（带.）
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".png";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

}