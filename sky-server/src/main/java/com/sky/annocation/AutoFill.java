package com.sky.annocation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,这个注解加的位置
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//这里的方法我有点不太理解 TODO
public @interface AutoFill {
//    数据库操作类型UPDATE、INSERT，切面类
    OperationType value();
}
