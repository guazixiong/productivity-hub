package com.pbad.health.util;

import common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * 健康模块数据校验工具类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public class HealthValidationUtil {

    // 运动类型枚举值
    private static final String[] EXERCISE_TYPES = {
            "跑步", "游泳", "骑行", "力量训练", "瑜伽", "有氧运动", "球类运动", "其他"
    };

    // 饮水类型枚举值
    private static final String[] WATER_TYPES = {
            "白开水", "矿泉水", "纯净水", "茶水", "咖啡", "果汁", "运动饮料", "其他"
    };

    // 性别枚举值
    private static final String[] GENDERS = {
            "MALE", "FEMALE", "OTHER"
    };

    // 日期格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 邮箱格式正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * 校验运动类型枚举值
     *
     * @param exerciseType 运动类型
     * @throws BusinessException 如果运动类型无效
     */
    public static void validateExerciseType(String exerciseType) {
        if (!StringUtils.hasText(exerciseType)) {
            throw new BusinessException("400", "运动类型不能为空");
        }
        boolean valid = false;
        for (String type : EXERCISE_TYPES) {
            if (type.equals(exerciseType)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException("400", "运动类型无效，可选值：跑步、游泳、骑行、力量训练、瑜伽、有氧运动、球类运动、其他");
        }
    }

    /**
     * 校验饮水类型枚举值
     *
     * @param waterType 饮水类型
     * @throws BusinessException 如果饮水类型无效
     */
    public static void validateWaterType(String waterType) {
        if (!StringUtils.hasText(waterType)) {
            throw new BusinessException("400", "饮水类型不能为空");
        }
        boolean valid = false;
        for (String type : WATER_TYPES) {
            if (type.equals(waterType)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException("400", "饮水类型无效，可选值：白开水、矿泉水、纯净水、茶水、咖啡、果汁、运动饮料、其他");
        }
    }

    /**
     * 校验性别枚举值
     *
     * @param gender 性别
     * @throws BusinessException 如果性别无效
     */
    public static void validateGender(String gender) {
        if (!StringUtils.hasText(gender)) {
            return; // 性别为可选字段
        }
        boolean valid = false;
        for (String g : GENDERS) {
            if (g.equalsIgnoreCase(gender)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException("400", "性别无效，可选值：MALE、FEMALE、OTHER");
        }
    }

    /**
     * 校验日期格式（yyyy-MM-dd）
     *
     * @param dateStr 日期字符串
     * @return 解析后的LocalDate，如果为空或格式错误则返回null
     */
    public static LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException("400", "日期格式错误，应为yyyy-MM-dd");
        }
    }

    /**
     * 校验日期格式（yyyy-MM-dd），如果无效则抛出异常
     *
     * @param dateStr 日期字符串
     * @throws BusinessException 如果日期格式无效
     */
    public static void validateDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return; // 日期为可选字段
        }
        try {
            LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException("400", "日期格式错误，应为yyyy-MM-dd");
        }
    }

    /**
     * 校验日期范围
     *
     * @param startDateStr 开始日期字符串
     * @param endDateStr   结束日期字符串
     * @throws BusinessException 如果日期范围无效
     */
    public static void validateDateRange(String startDateStr, String endDateStr) {
        if (!StringUtils.hasText(startDateStr) || !StringUtils.hasText(endDateStr)) {
            throw new BusinessException("400", "开始日期和结束日期不能为空");
        }
        LocalDate startDate = parseDate(startDateStr);
        LocalDate endDate = parseDate(endDateStr);
        if (startDate == null || endDate == null) {
            throw new BusinessException("400", "日期格式错误，应为yyyy-MM-dd");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("400", "结束日期不能早于开始日期");
        }
    }

    /**
     * 校验数值范围（Integer）
     *
     * @param value 数值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @param fieldName 字段名称（用于错误提示）
     * @throws BusinessException 如果数值超出范围
     */
    public static void validateIntegerRange(Integer value, Integer min, Integer max, String fieldName) {
        if (value == null) {
            throw new BusinessException("400", fieldName + "不能为空");
        }
        if (value < min || value > max) {
            throw new BusinessException("400", fieldName + "必须在" + min + "-" + max + "之间");
        }
    }

    /**
     * 校验数值范围（Integer，可选）
     *
     * @param value 数值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @param fieldName 字段名称（用于错误提示）
     * @throws BusinessException 如果数值超出范围
     */
    public static void validateIntegerRangeOptional(Integer value, Integer min, Integer max, String fieldName) {
        if (value == null) {
            return; // 可选字段
        }
        if (value < min || value > max) {
            throw new BusinessException("400", fieldName + "必须在" + min + "-" + max + "之间");
        }
    }

    /**
     * 校验数值范围（BigDecimal）
     *
     * @param value 数值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @param fieldName 字段名称（用于错误提示）
     * @throws BusinessException 如果数值超出范围
     */
    public static void validateBigDecimalRange(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName) {
        if (value == null) {
            throw new BusinessException("400", fieldName + "不能为空");
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BusinessException("400", fieldName + "必须在" + min + "-" + max + "之间");
        }
    }

    /**
     * 校验数值范围（BigDecimal，可选）
     *
     * @param value 数值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @param fieldName 字段名称（用于错误提示）
     * @throws BusinessException 如果数值超出范围
     */
    public static void validateBigDecimalRangeOptional(BigDecimal value, BigDecimal min, BigDecimal max, String fieldName) {
        if (value == null) {
            return; // 可选字段
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new BusinessException("400", fieldName + "必须在" + min + "-" + max + "之间");
        }
    }

    /**
     * 校验邮箱格式
     *
     * @param email 邮箱地址
     * @throws BusinessException 如果邮箱格式无效
     */
    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return; // 邮箱为可选字段
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new BusinessException("400", "邮箱格式不正确");
        }
    }

    /**
     * 校验运动时长（分钟）
     *
     * @param durationMinutes 运动时长
     * @throws BusinessException 如果运动时长无效
     */
    public static void validateDurationMinutes(Integer durationMinutes) {
        validateIntegerRange(durationMinutes, 1, 1440, "运动时长");
    }

    /**
     * 校验运动时长（分钟，可选）
     *
     * @param durationMinutes 运动时长
     * @throws BusinessException 如果运动时长无效
     */
    public static void validateDurationMinutesOptional(Integer durationMinutes) {
        validateIntegerRangeOptional(durationMinutes, 1, 1440, "运动时长");
    }

    /**
     * 校验饮水量（毫升）
     *
     * @param volumeMl 饮水量
     * @throws BusinessException 如果饮水量无效
     */
    public static void validateVolumeMl(Integer volumeMl) {
        validateIntegerRange(volumeMl, 1, 5000, "饮水量");
    }

    /**
     * 校验饮水量（毫升，可选）
     *
     * @param volumeMl 饮水量
     * @throws BusinessException 如果饮水量无效
     */
    public static void validateVolumeMlOptional(Integer volumeMl) {
        validateIntegerRangeOptional(volumeMl, 1, 5000, "饮水量");
    }

    /**
     * 校验每日目标饮水量（毫升）
     *
     * @param dailyTargetMl 每日目标饮水量
     * @throws BusinessException 如果每日目标饮水量无效
     */
    public static void validateDailyTargetMl(Integer dailyTargetMl) {
        validateIntegerRange(dailyTargetMl, 500, 10000, "每日目标饮水量");
    }

    /**
     * 校验每日目标饮水量（毫升，可选）
     *
     * @param dailyTargetMl 每日目标饮水量
     * @throws BusinessException 如果每日目标饮水量无效
     */
    public static void validateDailyTargetMlOptional(Integer dailyTargetMl) {
        validateIntegerRangeOptional(dailyTargetMl, 500, 10000, "每日目标饮水量");
    }

    /**
     * 校验体重（公斤）
     *
     * @param weightKg 体重
     * @throws BusinessException 如果体重无效
     */
    public static void validateWeightKg(BigDecimal weightKg) {
        validateBigDecimalRange(weightKg, new BigDecimal("20.00"), new BigDecimal("300.00"), "体重");
    }

    /**
     * 校验体重（公斤，可选）
     *
     * @param weightKg 体重
     * @throws BusinessException 如果体重无效
     */
    public static void validateWeightKgOptional(BigDecimal weightKg) {
        validateBigDecimalRangeOptional(weightKg, new BigDecimal("20.00"), new BigDecimal("300.00"), "体重");
    }

    /**
     * 校验目标体重（公斤）
     *
     * @param targetWeightKg 目标体重
     * @throws BusinessException 如果目标体重无效
     */
    public static void validateTargetWeightKg(BigDecimal targetWeightKg) {
        validateBigDecimalRange(targetWeightKg, new BigDecimal("20.00"), new BigDecimal("300.00"), "目标体重");
    }

    /**
     * 校验目标体重（公斤，可选）
     *
     * @param targetWeightKg 目标体重
     * @throws BusinessException 如果目标体重无效
     */
    public static void validateTargetWeightKgOptional(BigDecimal targetWeightKg) {
        validateBigDecimalRangeOptional(targetWeightKg, new BigDecimal("20.00"), new BigDecimal("300.00"), "目标体重");
    }

    /**
     * 校验体脂率（%）
     *
     * @param bodyFatPercentage 体脂率
     * @throws BusinessException 如果体脂率无效
     */
    public static void validateBodyFatPercentage(BigDecimal bodyFatPercentage) {
        validateBigDecimalRangeOptional(bodyFatPercentage, new BigDecimal("5.00"), new BigDecimal("50.00"), "体脂率");
    }

    /**
     * 校验身高（厘米）
     *
     * @param heightCm 身高
     * @throws BusinessException 如果身高无效
     */
    public static void validateHeightCm(BigDecimal heightCm) {
        validateBigDecimalRangeOptional(heightCm, new BigDecimal("50.00"), new BigDecimal("250.00"), "身高");
    }

    /**
     * 校验肌肉量（公斤）
     *
     * @param muscleMassKg 肌肉量
     * @throws BusinessException 如果肌肉量无效
     */
    public static void validateMuscleMassKg(BigDecimal muscleMassKg) {
        validateBigDecimalRangeOptional(muscleMassKg, new BigDecimal("10.00"), new BigDecimal("200.00"), "肌肉量");
    }
}

