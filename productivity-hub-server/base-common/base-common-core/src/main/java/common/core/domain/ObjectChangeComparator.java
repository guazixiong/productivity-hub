package common.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.annotation.FieldName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对象变更对比工具类
 *
 * @author: pangdi
 * @date: 2025-11-27 18:25:59
 * @version: 1.0
 */
public class ObjectChangeComparator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    //    public static void main(String[] args) {
//        // 创建变更前对象
//        User beforeItem = new User("A123-01", 123.00, "古董花瓶", new Date());
//
//        // 创建变更后对象
//        User afterItem = new User("A123-02", 150.00, "古董花瓶", new Date());
//
//        // 对比对象变更
//        String result = ObjectChangeComparator.compareObjects(beforeItem, afterItem);
//        System.out.println(result);
//
//        // 如果需要获取结果对象进行进一步处理
//        ChangeResult changeResult = ObjectChangeComparator.compareObjectsToResult(beforeItem, afterItem);
//        System.out.println("变更字段数量: " + changeResult.getChangeBeforeFieldList().size());
//    }

    /**
     * 对比两个对象的变更
     *
     * @param beforeObj 变更前对象
     * @param afterObj  变更后对象
     * @param <T>       对象类型
     * @return JSON格式的变更结果
     */
    public static <T> String compareObjects(T beforeObj, T afterObj) {
        try {
            ChangeResult result = compareObjectsToResult(beforeObj, afterObj);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new RuntimeException("对象对比失败", e);
        }
    }

    /**
     * 对比两个对象的变更，返回结果对象
     *
     * @param beforeObj 变更前对象
     * @param afterObj  变更后对象
     * @param <T>       对象类型
     * @return 变更结果对象
     */
    public static <T> ChangeResult compareObjectsToResult(T beforeObj, T afterObj) {
        if (beforeObj == null || afterObj == null) {
            throw new IllegalArgumentException("对比对象不能为空");
        }

        if (!beforeObj.getClass().equals(afterObj.getClass())) {
            throw new IllegalArgumentException("对比对象类型必须相同");
        }

        ChangeResult result = new ChangeResult();
        Class<?> clazz = beforeObj.getClass();

        // 获取所有字段（包括父类字段）
        List<Field> allFields = getAllFields(clazz);

        for (Field field : allFields) {
            try {
                // 设置字段可访问
                field.setAccessible(true);

                // 获取字段值
                Object beforeValue = field.get(beforeObj);
                Object afterValue = field.get(afterObj);

                // 判断字段值是否发生变更
                if (isFieldChanged(beforeValue, afterValue)) {
                    String fieldName = field.getName();
                    String displayName = getFieldDisplayName(field);

                    // 添加变更前数据
                    result.getChangeBeforeFieldList().add(
                            new FieldChangeInfo(displayName, formatValue(beforeValue), fieldName)
                    );

                    // 添加变更后数据
                    result.getChangeAfterFieldList().add(
                            new FieldChangeInfo(displayName, formatValue(afterValue), fieldName)
                    );
                }
            } catch (IllegalAccessException e) {
                // 记录日志或继续处理下一个字段
                System.err.println("无法访问字段: " + field.getName());
            }
        }

        return result;
    }

    /**
     * 获取所有字段（包括父类）
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 获取字段显示名称
     */
    private static String getFieldDisplayName(Field field) {
        // 首先检查自定义注解
        FieldName fieldName = field.getAnnotation(FieldName.class);
        if (fieldName != null) {
            return fieldName.value();
        }

        // 检查JsonProperty注解
        JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
        if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
            return jsonProperty.value();
        }

        // 默认返回字段名称
        return field.getName();
    }

    /**
     * 判断字段值是否发生变更
     */
    private static boolean isFieldChanged(Object beforeValue, Object afterValue) {
        if (beforeValue == null && afterValue == null) {
            return false;
        }
        if (beforeValue == null || afterValue == null) {
            return true;
        }
        return !beforeValue.equals(afterValue);
    }

    /**
     * 格式化字段值用于显示
     */
    private static Object formatValue(Object value) {
        if (value == null) {
            return null;
        }

        // 如果是数字类型，保持原样
        if (value instanceof Number) {
            return value;
        }

        // 其他类型转为字符串
        return value.toString();
    }
}