package com.qg.dormrepair.annotation;

import com.qg.dormrepair.enums.DataBaseOperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    DataBaseOperationType value();
}
