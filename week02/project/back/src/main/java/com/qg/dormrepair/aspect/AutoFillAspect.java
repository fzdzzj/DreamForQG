package com.qg.dormrepair.aspect;

import com.qg.dormrepair.annotation.AutoFill;
import com.qg.dormrepair.constants.AutoFillConstant;
import com.qg.dormrepair.enums.DataBaseOperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 公共字段填充切面类
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Before("@annotation(com.qg.dormrepair.annotation.AutoFill)")
    public void autoFill(JoinPoint joinPoint)  {
        log.info("开始进行公共字段填充");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AutoFill autoFill=method.getAnnotation(AutoFill.class);
        DataBaseOperationType value = autoFill.value();

        Object[] args=joinPoint.getArgs();
        if(args==null||args.length==0){
            log.info("args为空");
            return;
        }
        Object entity=args[0];
        try {
            if(value==DataBaseOperationType.INSERT)
            {
                log.info("开始进行插入操作");
                Method setCreateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                setCreateTime.invoke(entity, LocalDateTime.now());
                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateTime.invoke(entity, LocalDateTime.now());
            }else{
                log.info("开始进行更新操作");
                Method setUpdateTime=entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateTime.invoke(entity, LocalDateTime.now());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
