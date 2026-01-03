package common.annotation;

import java.lang.annotation.*;

// 自定义注解，用于标识字段的中文名称
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldName {
    String value();
}