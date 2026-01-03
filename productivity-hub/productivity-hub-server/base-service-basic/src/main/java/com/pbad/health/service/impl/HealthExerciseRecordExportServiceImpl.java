package com.pbad.health.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;
import com.pbad.health.domain.po.HealthExerciseRecordPO;
import com.pbad.health.domain.po.HealthTrainingPlanPO;
import com.pbad.health.mapper.HealthExerciseRecordMapper;
import com.pbad.health.mapper.HealthTrainingPlanMapper;
import com.pbad.health.service.HealthExerciseRecordExportService;
import common.exception.BusinessException;
import common.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运动记录导出服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthExerciseRecordExportServiceImpl implements HealthExerciseRecordExportService {

    private final HealthExerciseRecordMapper exerciseRecordMapper;
    private final HealthTrainingPlanMapper trainingPlanMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] exportToExcel(ExerciseRecordQueryDTO queryDTO, String userId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("运动记录");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.LEFT);

            // 创建日期样式
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(dataStyle);
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "运动日期", "运动类型", "运动时长(分钟)", "消耗卡路里(千卡)",
                    "运动距离(公里)", "平均心率(次/分)", "最大心率(次/分)",
                    "训练计划", "备注", "创建时间", "更新时间"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 查询数据
            List<HealthExerciseRecordPO> records = queryRecords(queryDTO, userId);
            
            // 获取训练计划映射（用于显示计划名称）
            Map<String, String> planNameMap = getTrainingPlanNameMap(records, userId);

            // 填充数据
            int rowNum = 1;
            for (HealthExerciseRecordPO record : records) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // 运动日期
                Cell dateCell = row.createCell(colNum++);
                if (record.getExerciseDate() != null) {
                    dateCell.setCellValue(record.getExerciseDate());
                    dateCell.setCellStyle(dateStyle);
                }

                // 运动类型
                setCellValue(row.createCell(colNum++), record.getExerciseType());

                // 运动时长
                setCellValue(row.createCell(colNum++), record.getDurationMinutes());

                // 消耗卡路里
                setCellValue(row.createCell(colNum++), record.getCaloriesBurned());

                // 运动距离
                setCellValue(row.createCell(colNum++), record.getDistanceKm());

                // 平均心率
                setCellValue(row.createCell(colNum++), record.getHeartRateAvg());

                // 最大心率
                setCellValue(row.createCell(colNum++), record.getHeartRateMax());

                // 训练计划
                String planName = planNameMap.get(record.getTrainingPlanId());
                setCellValue(row.createCell(colNum++), planName);

                // 备注
                setCellValue(row.createCell(colNum++), record.getNotes());

                // 创建时间
                Cell createTimeCell = row.createCell(colNum++);
                if (record.getCreatedAt() != null) {
                    createTimeCell.setCellValue(record.getCreatedAt());
                    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
                    createTimeCell.setCellStyle(dateStyle);
                }

                // 更新时间
                Cell updateTimeCell = row.createCell(colNum++);
                if (record.getUpdatedAt() != null) {
                    updateTimeCell.setCellValue(record.getUpdatedAt());
                    updateTimeCell.setCellStyle(dateStyle);
                }

                // 设置单元格样式
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null && cell.getCellStyle() == null) {
                        cell.setCellStyle(dataStyle);
                    }
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1000, 15000));
            }

            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("导出运动记录Excel失败", e);
            throw new BusinessException("500", "导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportToCsv(ExerciseRecordQueryDTO queryDTO, String userId) {
        try {
            // 查询数据
            List<HealthExerciseRecordPO> records = queryRecords(queryDTO, userId);
            
            // 获取训练计划映射
            Map<String, String> planNameMap = getTrainingPlanNameMap(records, userId);

            // 构建CSV内容
            StringBuilder csv = new StringBuilder();
            
            // 表头
            csv.append("运动日期,运动类型,运动时长(分钟),消耗卡路里(千卡),运动距离(公里),平均心率(次/分),最大心率(次/分),训练计划,备注,创建时间,更新时间\n");

            // 数据行
            for (HealthExerciseRecordPO record : records) {
                csv.append(escapeCsvValue(DATE_FORMAT.format(record.getExerciseDate()))).append(",");
                csv.append(escapeCsvValue(record.getExerciseType())).append(",");
                csv.append(escapeCsvValue(record.getDurationMinutes())).append(",");
                csv.append(escapeCsvValue(record.getCaloriesBurned())).append(",");
                csv.append(escapeCsvValue(record.getDistanceKm())).append(",");
                csv.append(escapeCsvValue(record.getHeartRateAvg())).append(",");
                csv.append(escapeCsvValue(record.getHeartRateMax())).append(",");
                csv.append(escapeCsvValue(planNameMap.get(record.getTrainingPlanId()))).append(",");
                csv.append(escapeCsvValue(record.getNotes())).append(",");
                csv.append(escapeCsvValue(record.getCreatedAt() != null ? DATETIME_FORMAT.format(record.getCreatedAt()) : "")).append(",");
                csv.append(escapeCsvValue(record.getUpdatedAt() != null ? DATETIME_FORMAT.format(record.getUpdatedAt()) : "")).append("\n");
            }

            return csv.toString().getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("导出运动记录CSV失败", e);
            throw new BusinessException("500", "导出CSV失败: " + e.getMessage());
        }
    }

    /**
     * 查询记录
     */
    private List<HealthExerciseRecordPO> queryRecords(ExerciseRecordQueryDTO queryDTO, String userId) {
        Date startDate = null;
        Date endDate = null;
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            startDate = DateUtil.parseDate(queryDTO.getStartDate());
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            endDate = DateUtil.parseDate(queryDTO.getEndDate());
        }

        return exerciseRecordMapper.selectAll(
                userId,
                queryDTO.getExerciseType(),
                startDate,
                endDate,
                queryDTO.getTrainingPlanId(),
                queryDTO.getSortField(),
                queryDTO.getSortOrder()
        );
    }

    /**
     * 获取训练计划名称映射
     */
    private Map<String, String> getTrainingPlanNameMap(List<HealthExerciseRecordPO> records, String userId) {
        return records.stream()
                .map(HealthExerciseRecordPO::getTrainingPlanId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toMap(
                        planId -> planId,
                        planId -> {
                            HealthTrainingPlanPO plan = trainingPlanMapper.selectById(planId, userId);
                            return plan != null ? plan.getPlanName() : "";
                        },
                        (v1, v2) -> v1
                ));
    }

    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 转义CSV值
     */
    private String escapeCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}

