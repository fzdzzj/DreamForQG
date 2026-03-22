// Application.java
package com.qg.dormrepair;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import java.util.Date;

@Slf4j
@ServletComponentScan
@MapperScan("com.qg.dormrepair.mapper")
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        log.info("宿舍保修管理系统启动中");
        log.warn(new Date().toString());
        SpringApplication.run(Application.class, args);
        log.info("宿舍报修管理系统启动完成，服务运行在 http://localhost:8080");
    }
}
