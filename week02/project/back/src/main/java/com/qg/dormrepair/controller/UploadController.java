package com.qg.dormrepair.controller;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.util.AliyunOSSOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "文件上传接口", description = "图片/文件上传至阿里云OSS，返回URL地址")
@CrossOrigin(origins = "*")
public class UploadController {
    private final AliyunOSSOperator aliyunOSSOperator;

    // 从配置文件读取允许的文件类型
    @Value("${aliyun.oss.allowed-types}")
    private String allowedTypesStr;
    private List<String> ALLOWED_TYPES;
    // 从配置文件读取允许的文件大小
    @Value("${aliyun.oss.max-file-size}")
    private DataSize maxFileSize;

    @PostMapping("/upload")
    @OperationLog("文件上传")
    @Operation(summary = "文件上传", description = "支持图片格式，上传成功返回OSS访问地址")
    public Result<String> upload(
            @Parameter(description = "上传文件", required = true)
            MultipartFile file) throws Exception {
        log.info("文件上传请求，文件名：{}, 大小：{}KB",
                file.getOriginalFilename(), file.getSize() / 1024);
        ALLOWED_TYPES = Arrays.asList(allowedTypesStr.split(","));
        if (file.isEmpty()) {
            log.warn("上传文件不能为空");
            return Result.error(400, "上传文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            log.warn("不支持的文件类型：{}", contentType);
            return Result.error(400, "不支持的文件类型" + contentType);
        }
        if (file.getSize() > maxFileSize.toBytes()) {
            log.warn("文件大小超限");
            return Result.error(413, "文件大小不能超过 " + maxFileSize.toMegabytes() + "MB");
        }
        String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
        return Result.success(url);
    }
}