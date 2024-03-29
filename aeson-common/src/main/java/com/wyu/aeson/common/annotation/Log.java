package com.wyu.aeson.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.wyu.aeson.common.enums.BusinessType;
import com.wyu.aeson.common.enums.OperatorType;

/**
 * 自定义操作日志记录注解
 *
 * @author zwx
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块
     */
    public String title() default "";

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    public BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作类别（0其它 1后台用户 2手机端用户）
     */
    public OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
    public boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    public boolean isSaveResponseData() default true;
}
