package common.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.annotation.FieldName;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象变更对比工具类
 * <p>
 * 用于比较两个对象的字段值变化，返回变化信息。
 * 支持忽略指定字段、只比较指定字段、判断是否有变化等功能。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 基本用法：比较两个对象
 * ChangeResult result = ObjectChangeComparator.compareObjectsToResult(beforeObj, afterObj);
 * 
 * // 忽略某些字段
 * Set&lt;String&gt; ignoreFields = new HashSet&lt;&gt;(Arrays.asList("updateTime", "version"));
 * ChangeResult result = ObjectChangeComparator.compareObjectsToResult(beforeObj, afterObj, ignoreFields);
 * 
 * // 只比较指定字段
 * Set&lt;String&gt; onlyFields = new HashSet&lt;&gt;(Arrays.asList("name", "price"));
 * ChangeResult result = ObjectChangeComparator.compareObjectsToResult(beforeObj, afterObj, null, onlyFields);
 * 
 * // 判断是否有变化
 * boolean hasChanges = ObjectChangeComparator.hasChanges(beforeObj, afterObj);
 * 
 * // 获取Map格式的变化信息
 * Map&lt;String, Map&lt;String, Object&gt;&gt; changesMap = ObjectChangeComparator.getChangesAsMap(beforeObj, afterObj);
 * </pre>
 * </p>
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
        return compareObjectsToResult(beforeObj, afterObj, null, null);
    }

    /**
     * 对比两个对象的变更，返回结果对象（支持忽略指定字段）
     *
     * @param beforeObj  变更前对象
     * @param afterObj   变更后对象
     * @param ignoreFields 需要忽略的字段名称集合
     * @param <T>        对象类型
     * @return 变更结果对象
     */
    public static <T> ChangeResult compareObjectsToResult(T beforeObj, T afterObj, Set<String> ignoreFields) {
        return compareObjectsToResult(beforeObj, afterObj, ignoreFields, null);
    }

    /**
     * 对比两个对象的变更，返回结果对象（支持忽略指定字段或只比较指定字段）
     *
     * @param beforeObj  变更前对象
     * @param afterObj   变更后对象
     * @param ignoreFields 需要忽略的字段名称集合（为null时不过滤）
     * @param onlyFields   只比较的字段名称集合（为null时比较所有字段，优先级高于ignoreFields）
     * @param <T>        对象类型
     * @return 变更结果对象
     */
    public static <T> ChangeResult compareObjectsToResult(T beforeObj, T afterObj, Set<String> ignoreFields, Set<String> onlyFields) {
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

        // 过滤字段
        List<Field> fieldsToCompare = filterFields(allFields, ignoreFields, onlyFields);

        for (Field field : fieldsToCompare) {
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
     * 判断两个对象是否有变化
     *
     * @param beforeObj 变更前对象
     * @param afterObj  变更后对象
     * @param <T>       对象类型
     * @return 是否有变化
     */
    public static <T> boolean hasChanges(T beforeObj, T afterObj) {
        ChangeResult result = compareObjectsToResult(beforeObj, afterObj);
        return !result.getChangeBeforeFieldList().isEmpty();
    }

    /**
     * 判断两个对象是否有变化（支持忽略指定字段）
     *
     * @param beforeObj  变更前对象
     * @param afterObj   变更后对象
     * @param ignoreFields 需要忽略的字段名称集合
     * @param <T>        对象类型
     * @return 是否有变化
     */
    public static <T> boolean hasChanges(T beforeObj, T afterObj, Set<String> ignoreFields) {
        ChangeResult result = compareObjectsToResult(beforeObj, afterObj, ignoreFields);
        return !result.getChangeBeforeFieldList().isEmpty();
    }

    /**
     * 获取变化字段的Map格式（字段名 -> 变化信息）
     *
     * @param beforeObj 变更前对象
     * @param afterObj  变更后对象
     * @param <T>       对象类型
     * @return Map格式的变化信息，key为字段名，value为包含变更前后值的Map
     */
    public static <T> Map<String, Map<String, Object>> getChangesAsMap(T beforeObj, T afterObj) {
        ChangeResult result = compareObjectsToResult(beforeObj, afterObj);
        Map<String, Map<String, Object>> changesMap = new LinkedHashMap<>();

        List<FieldChangeInfo> beforeList = result.getChangeBeforeFieldList();
        List<FieldChangeInfo> afterList = result.getChangeAfterFieldList();

        for (int i = 0; i < beforeList.size(); i++) {
            FieldChangeInfo beforeInfo = beforeList.get(i);
            FieldChangeInfo afterInfo = afterList.get(i);

            Map<String, Object> changeInfo = new HashMap<>();
            changeInfo.put("fieldName", beforeInfo.getField());
            changeInfo.put("displayName", beforeInfo.getName());
            changeInfo.put("beforeValue", beforeInfo.getValue());
            changeInfo.put("afterValue", afterInfo.getValue());

            changesMap.put(beforeInfo.getField(), changeInfo);
        }

        return changesMap;
    }

    /**
     * 获取所有字段（包括父类）
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && !clazz.equals(Object.class)) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 过滤字段（根据ignoreFields和onlyFields）
     */
    private static List<Field> filterFields(List<Field> allFields, Set<String> ignoreFields, Set<String> onlyFields) {
        if (onlyFields != null && !onlyFields.isEmpty()) {
            // 如果指定了onlyFields，只返回这些字段
            return allFields.stream()
                    .filter(field -> onlyFields.contains(field.getName()))
                    .collect(Collectors.toList());
        }

        if (ignoreFields != null && !ignoreFields.isEmpty()) {
            // 如果指定了ignoreFields，过滤掉这些字段
            return allFields.stream()
                    .filter(field -> !ignoreFields.contains(field.getName()))
                    .collect(Collectors.toList());
        }

        // 都不指定，返回所有字段
        return allFields;
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

        // 处理数组类型
        if (beforeValue.getClass().isArray() && afterValue.getClass().isArray()) {
            return !Arrays.deepEquals(new Object[]{beforeValue}, new Object[]{afterValue});
        }

        // 处理集合类型
        if (beforeValue instanceof Collection && afterValue instanceof Collection) {
            Collection<?> beforeColl = (Collection<?>) beforeValue;
            Collection<?> afterColl = (Collection<?>) afterValue;
            if (beforeColl.size() != afterColl.size()) {
                return true;
            }
            return !beforeColl.containsAll(afterColl) || !afterColl.containsAll(beforeColl);
        }

        // 处理Map类型
        if (beforeValue instanceof Map && afterValue instanceof Map) {
            Map<?, ?> beforeMap = (Map<?, ?>) beforeValue;
            Map<?, ?> afterMap = (Map<?, ?>) afterValue;
            if (beforeMap.size() != afterMap.size()) {
                return true;
            }
            return !beforeMap.equals(afterMap);
        }

        // 其他类型使用equals比较
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

        // 如果是布尔类型，保持原样
        if (value instanceof Boolean) {
            return value;
        }

        // 如果是数组，转换为列表格式
        if (value.getClass().isArray()) {
            if (value instanceof Object[]) {
                return Arrays.toString((Object[]) value);
            } else if (value instanceof int[]) {
                return Arrays.toString((int[]) value);
            } else if (value instanceof long[]) {
                return Arrays.toString((long[]) value);
            } else if (value instanceof double[]) {
                return Arrays.toString((double[]) value);
            } else if (value instanceof float[]) {
                return Arrays.toString((float[]) value);
            } else if (value instanceof boolean[]) {
                return Arrays.toString((boolean[]) value);
            } else if (value instanceof byte[]) {
                return Arrays.toString((byte[]) value);
            } else if (value instanceof short[]) {
                return Arrays.toString((short[]) value);
            } else if (value instanceof char[]) {
                return Arrays.toString((char[]) value);
            } else {
                return value.toString();
            }
        }

        // 如果是集合，转换为字符串
        if (value instanceof Collection) {
            return value.toString();
        }

        // 如果是Map，转换为字符串
        if (value instanceof Map) {
            return value.toString();
        }

        // 其他类型转为字符串
        return value.toString();
    }
}