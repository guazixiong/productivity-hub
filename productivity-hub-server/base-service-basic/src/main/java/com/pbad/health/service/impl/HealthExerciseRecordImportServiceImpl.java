package com.pbad.health.service.impl;

import com.pbad.health.domain.dto.ExerciseRecordCreateDTO;
import com.pbad.health.domain.vo.ExerciseRecordImportResultVO;
import com.pbad.health.service.HealthExerciseRecordImportService;
import com.pbad.health.service.HealthExerciseRecordService;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 运动记录导入服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthExerciseRecordImportServiceImpl implements HealthExerciseRecordImportService {

    private final HealthExerciseRecordService exerciseRecordService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // 运动类型枚举值
    private static final String[] EXERCISE_TYPES = {
            "跑步", "游泳", "骑行", "力量训练", "瑜伽", "有氧运动", "球类运动", "其他"
    };

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        String userId = RequestUserContext.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            throw new BusinessException("401", "用户未登录");
        }
        return userId;
    }

    @Override
    public ExerciseRecordImportResultVO importFromExcel(MultipartFile file) {
        String userId = getCurrentUserId();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("400", "文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("400", "文件格式不正确，仅支持.xlsx和.xls格式");
        }

        ExerciseRecordImportResultVO result = new ExerciseRecordImportResultVO();
        result.setTotal(0);
        result.setSuccess(0);
        result.setFailed(0);
        result.setSkipped(0);
        result.setErrors(new ArrayList<>());

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("400", "Excel文件格式不正确");
            }

            // 读取表头（第一行）
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("400", "Excel文件缺少表头");
            }

            // 解析表头，获取列索引
            int colIndexExerciseDate = -1;
            int colIndexExerciseType = -1;
            int colIndexDurationMinutes = -1;
            int colIndexCaloriesBurned = -1;
            int colIndexDistanceKm = -1;
            int colIndexHeartRateAvg = -1;
            int colIndexHeartRateMax = -1;
            int colIndexTrainingPlan = -1;
            int colIndexNotes = -1;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).trim();
                    switch (cellValue) {
                        case "运动日期":
                        case "日期":
                            colIndexExerciseDate = i;
                            break;
                        case "运动类型":
                        case "类型":
                            colIndexExerciseType = i;
                            break;
                        case "运动时长(分钟)":
                        case "运动时长":
                        case "时长":
                        case "时长(分钟)":
                            colIndexDurationMinutes = i;
                            break;
                        case "消耗卡路里(千卡)":
                        case "消耗卡路里":
                        case "卡路里":
                        case "卡路里(千卡)":
                            colIndexCaloriesBurned = i;
                            break;
                        case "运动距离(公里)":
                        case "运动距离":
                        case "距离":
                        case "距离(公里)":
                            colIndexDistanceKm = i;
                            break;
                        case "平均心率(次/分)":
                        case "平均心率":
                        case "心率(平均)":
                            colIndexHeartRateAvg = i;
                            break;
                        case "最大心率(次/分)":
                        case "最大心率":
                        case "心率(最大)":
                            colIndexHeartRateMax = i;
                            break;
                        case "训练计划":
                        case "计划":
                            colIndexTrainingPlan = i;
                            break;
                        case "备注":
                        case "备注信息":
                            colIndexNotes = i;
                            break;
                    }
                }
            }

            // 验证必需字段
            if (colIndexExerciseType < 0) {
                throw new BusinessException("400", "Excel文件缺少必需字段：运动类型");
            }
            if (colIndexDurationMinutes < 0) {
                throw new BusinessException("400", "Excel文件缺少必需字段：运动时长");
            }

            // 从第二行开始读取数据
            int totalRows = sheet.getLastRowNum();
            result.setTotal(totalRows);

            for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    result.setSkipped(result.getSkipped() + 1);
                    continue;
                }

                try {
                    // 读取数据
                    String exerciseDateStr = colIndexExerciseDate >= 0 ? getCellValueAsString(row.getCell(colIndexExerciseDate)) : null;
                    String exerciseTypeStr = getCellValueAsString(row.getCell(colIndexExerciseType));
                    String durationMinutesStr = getCellValueAsString(row.getCell(colIndexDurationMinutes));
                    String caloriesBurnedStr = colIndexCaloriesBurned >= 0 ? getCellValueAsString(row.getCell(colIndexCaloriesBurned)) : null;
                    String distanceKmStr = colIndexDistanceKm >= 0 ? getCellValueAsString(row.getCell(colIndexDistanceKm)) : null;
                    String heartRateAvgStr = colIndexHeartRateAvg >= 0 ? getCellValueAsString(row.getCell(colIndexHeartRateAvg)) : null;
                    String heartRateMaxStr = colIndexHeartRateMax >= 0 ? getCellValueAsString(row.getCell(colIndexHeartRateMax)) : null;
                    String trainingPlanStr = colIndexTrainingPlan >= 0 ? getCellValueAsString(row.getCell(colIndexTrainingPlan)) : null;
                    String notes = colIndexNotes >= 0 ? getCellValueAsString(row.getCell(colIndexNotes)) : null;

                    // 验证必需字段
                    if (exerciseTypeStr == null || exerciseTypeStr.trim().isEmpty()) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：运动类型不能为空", rowIndex + 1));
                        continue;
                    }

                    // 验证运动类型
                    String exerciseType = exerciseTypeStr.trim();
                    boolean isValidType = false;
                    for (String type : EXERCISE_TYPES) {
                        if (type.equals(exerciseType)) {
                            isValidType = true;
                            break;
                        }
                    }
                    if (!isValidType) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：运动类型无效，应为：%s", rowIndex + 1, String.join("、", EXERCISE_TYPES)));
                        continue;
                    }

                    // 验证运动时长
                    if (durationMinutesStr == null || durationMinutesStr.trim().isEmpty()) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：运动时长不能为空", rowIndex + 1));
                        continue;
                    }

                    Integer durationMinutes;
                    try {
                        durationMinutes = Integer.parseInt(durationMinutesStr.trim());
                        if (durationMinutes < 1 || durationMinutes > 1440) {
                            result.setFailed(result.getFailed() + 1);
                            result.getErrors().add(String.format("第%d行：运动时长必须在1-1440分钟之间", rowIndex + 1));
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：运动时长格式错误", rowIndex + 1));
                        continue;
                    }

                    // 构建创建DTO
                    ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
                    createDTO.setExerciseType(exerciseType);
                    if (StringUtils.hasText(exerciseDateStr)) {
                        createDTO.setExerciseDate(exerciseDateStr.trim());
                    }
                    createDTO.setDurationMinutes(durationMinutes);

                    // 解析可选字段
                    if (StringUtils.hasText(caloriesBurnedStr)) {
                        try {
                            Integer caloriesBurned = Integer.parseInt(caloriesBurnedStr.trim());
                            if (caloriesBurned > 0) {
                                createDTO.setCaloriesBurned(caloriesBurned);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误，不设置该字段
                        }
                    }

                    if (StringUtils.hasText(distanceKmStr)) {
                        try {
                            BigDecimal distanceKm = new BigDecimal(distanceKmStr.trim());
                            if (distanceKm.compareTo(BigDecimal.ZERO) > 0) {
                                createDTO.setDistanceKm(distanceKm);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误，不设置该字段
                        }
                    }

                    if (StringUtils.hasText(heartRateAvgStr)) {
                        try {
                            Integer heartRateAvg = Integer.parseInt(heartRateAvgStr.trim());
                            if (heartRateAvg > 0 && heartRateAvg <= 250) {
                                createDTO.setHeartRateAvg(heartRateAvg);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误，不设置该字段
                        }
                    }

                    if (StringUtils.hasText(heartRateMaxStr)) {
                        try {
                            Integer heartRateMax = Integer.parseInt(heartRateMaxStr.trim());
                            if (heartRateMax > 0 && heartRateMax <= 250) {
                                createDTO.setHeartRateMax(heartRateMax);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误，不设置该字段
                        }
                    }

                    // 训练计划ID需要通过计划名称查询，这里暂时不处理，因为需要额外的查询
                    // 如果后续需要支持，可以通过训练计划名称查询ID
                    // if (StringUtils.hasText(trainingPlanStr)) {
                    //     createDTO.setTrainingPlanId(trainingPlanId);
                    // }

                    if (StringUtils.hasText(notes)) {
                        createDTO.setNotes(notes.trim());
                    }

                    // 创建记录
                    exerciseRecordService.create(createDTO, userId);
                    result.setSuccess(result.getSuccess() + 1);

                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    result.getErrors().add(String.format("第%d行：%s", rowIndex + 1, e.getMessage()));
                    log.error("导入第{}行数据失败", rowIndex + 1, e);
                }
            }

        } catch (IOException e) {
            throw new BusinessException("500", "读取Excel文件失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public byte[] downloadTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("运动记录导入模板");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "运动日期", "运动类型", "运动时长(分钟)", "消耗卡路里(千卡)",
                    "运动距离(公里)", "平均心率(次/分)", "最大心率(次/分)", "备注"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("2025-01-15");
            exampleRow.createCell(1).setCellValue("跑步");
            exampleRow.createCell(2).setCellValue(30);
            exampleRow.createCell(3).setCellValue(300);
            exampleRow.createCell(4).setCellValue(5.5);
            exampleRow.createCell(5).setCellValue(140);
            exampleRow.createCell(6).setCellValue(160);
            exampleRow.createCell(7).setCellValue("晨跑");

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new BusinessException("500", "生成模板文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取单元格值（字符串格式）
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    if (date != null) {
                        return DATE_FORMAT.format(date);
                    }
                    return null;
                } else {
                    // 处理数字，避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}

