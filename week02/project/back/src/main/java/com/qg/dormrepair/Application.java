package com.qg.dormrepair;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Date;

/**
 * 宿舍报修管理系统 —— 项目启动入口类
 * 负责 SpringBoot 应用的启动、配置扫描、MyBatis 扫描、异步任务开启等核心配置
 */
@Slf4j
@ServletComponentScan       // 扫描Servlet、Filter、Listener
@MapperScan("com.qg.dormrepair.mapper")  // MyBatis Mapper接口扫描
@SpringBootApplication     // SpringBoot 自动配置
@EnableAsync               // 开启异步任务支持
public class Application {

    /**
     * 项目主启动方法
     */
    public static void main(String[] args) {
        log.info("宿舍报修管理系统启动中");
        log.warn(new Date().toString());
        SpringApplication.run(Application.class, args);
        log.info("宿舍报修管理系统启动完成，服务运行在 http://localhost:8080");
    }
}