package com.pbad.health.service.impl;

import com.pbad.health.domain.dto.WeightRecordQueryDTO;
import com.pbad.health.domain.po.HealthWeightRecordPO;
import com.pbad.health.mapper.HealthWeightRecordMapper;
import com.pbad.health.service.HealthWeightRecordExportService;
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

/**
 * 体重记录导出服务实现类.
 * 关联需求：REQ-HEALTH-005
 * 关联任务：TASK-035
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWeightRecordExportServiceImpl implements HealthWeightRecordExportService {

    private final HealthWeightRecordMapper weightRecordMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] exportToExcel(WeightRecordQueryDTO queryDTO, String userId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("体重记录");

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

            // 创建时间样式
            CellStyle timeStyle = workbook.createCellStyle();
            timeStyle.cloneStyleFrom(dataStyle);
            timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "记录日期", "记录时间", "体重(公斤)", "体脂率(%)", "肌肉量(公斤)",
                    "身高(厘米)", "BMI", "健康状态", "备注", "创建时间", "更新时间"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 查询数据
            List<HealthWeightRecordPO> records = queryRecords(queryDTO, userId);

            // 填充数据
            int rowNum = 1;
            for (HealthWeightRecordPO record : records) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // 记录日期
                Cell dateCell = row.createCell(colNum++);
                if (record.getRecordDate() != null) {
                    dateCell.setCellValue(record.getRecordDate());
                    dateCell.setCellStyle(dateStyle);
                }

                // 记录时间
                Cell timeCell = row.createCell(colNum++);
                if (record.getRecordTime() != null) {
                    timeCell.setCellValue(record.getRecordTime());
                    timeCell.setCellStyle(timeStyle);
                }

                // 体重
                setCellValue(row.createCell(colNum++), record.getWeightKg());

                // 体脂率
                setCellValue(row.createCell(colNum++), record.getBodyFatPercentage());

                // 肌肉量
                setCellValue(row.createCell(colNum++), record.getMuscleMassKg());

                // 身高
                setCellValue(row.createCell(colNum++), record.getHeightCm());

                // BMI
                setCellValue(row.createCell(colNum++), record.getBmi());

                // 健康状态
                setCellValue(row.createCell(colNum++), record.getHealthStatus());

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
            log.error("导出体重记录Excel失败", e);
            throw new BusinessException("500", "导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportToCsv(WeightRecordQueryDTO queryDTO, String userId) {
        try {
            // 查询数据
            List<HealthWeightRecordPO> records = queryRecords(queryDTO, userId);

            // 构建CSV内容
            StringBuilder csv = new StringBuilder();
            
            // 表头
            csv.append("记录日期,记录时间,体重(公斤),体脂率(%),肌肉量(公斤),身高(厘米),BMI,健康状态,备注,创建时间,更新时间\n");

            // 数据行
            for (HealthWeightRecordPO record : records) {
                csv.append(escapeCsvValue(record.getRecordDate() != null ? DATE_FORMAT.format(record.getRecordDate()) : "")).append(",");
                csv.append(escapeCsvValue(record.getRecordTime() != null ? TIME_FORMAT.format(record.getRecordTime()) : "")).append(",");
                csv.append(escapeCsvValue(record.getWeightKg())).append(",");
                csv.append(escapeCsvValue(record.getBodyFatPercentage())).append(",");
                csv.append(escapeCsvValue(record.getMuscleMassKg())).append(",");
                csv.append(escapeCsvValue(record.getHeightCm())).append(",");
                csv.append(escapeCsvValue(record.getBmi())).append(",");
                csv.append(escapeCsvValue(record.getHealthStatus())).append(",");
                csv.append(escapeCsvValue(record.getNotes())).append(",");
                csv.append(escapeCsvValue(record.getCreatedAt() != null ? DATETIME_FORMAT.format(record.getCreatedAt()) : "")).append(",");
                csv.append(escapeCsvValue(record.getUpdatedAt() != null ? DATETIME_FORMAT.format(record.getUpdatedAt()) : "")).append("\n");
            }

            return csv.toString().getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("导出体重记录CSV失败", e);
            throw new BusinessException("500", "导出CSV失败: " + e.getMessage());
        }
    }

    /**
     * 查询记录
     */
    private List<HealthWeightRecordPO> queryRecords(WeightRecordQueryDTO queryDTO, String userId) {
        Date startDate = null;
        Date endDate = null;
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            startDate = DateUtil.parseDate(queryDTO.getStartDate());
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            endDate = DateUtil.parseDate(queryDTO.getEndDate());
        }

        return weightRecordMapper.selectAll(
                userId,
                startDate,
                endDate,
                queryDTO.getSortField(),
                queryDTO.getSortOrder()
        );
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

