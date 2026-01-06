package com.pbad.health.service.impl;

import com.pbad.health.domain.dto.WeightRecordCreateDTO;
import com.pbad.health.domain.vo.WeightRecordImportResultVO;
import com.pbad.health.service.HealthWeightRecordImportService;
import com.pbad.health.service.HealthWeightRecordService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 体重记录导入服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWeightRecordImportServiceImpl implements HealthWeightRecordImportService {

    private final HealthWeightRecordService weightRecordService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

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
    public WeightRecordImportResultVO importFromExcel(MultipartFile file) {
        String userId = getCurrentUserId();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("400", "文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("400", "文件格式不正确，仅支持.xlsx和.xls格式");
        }

        WeightRecordImportResultVO result = new WeightRecordImportResultVO();
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
            int colIndexRecordDate = -1;
            int colIndexRecordTime = -1;
            int colIndexWeightKg = -1;
            int colIndexBodyFatPercentage = -1;
            int colIndexMuscleMassKg = -1;
            int colIndexHeightCm = -1;
            int colIndexNotes = -1;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).trim();
                    switch (cellValue) {
                        case "记录日期":
                        case "日期":
                            colIndexRecordDate = i;
                            break;
                        case "记录时间":
                        case "时间":
                            colIndexRecordTime = i;
                            break;
                        case "体重(公斤)":
                        case "体重":
                        case "体重(kg)":
                            colIndexWeightKg = i;
                            break;
                        case "体脂率(%)":
                        case "体脂率":
                            colIndexBodyFatPercentage = i;
                            break;
                        case "肌肉量(公斤)":
                        case "肌肉量":
                        case "肌肉量(kg)":
                            colIndexMuscleMassKg = i;
                            break;
                        case "身高(厘米)":
                        case "身高":
                        case "身高(cm)":
                            colIndexHeightCm = i;
                            break;
                        case "备注":
                        case "备注信息":
                            colIndexNotes = i;
                            break;
                    }
                }
            }

            // 验证必需字段
            if (colIndexWeightKg < 0) {
                throw new BusinessException("400", "Excel文件缺少必需字段：体重");
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
                    String recordDateStr = colIndexRecordDate >= 0 ? getCellValueAsString(row.getCell(colIndexRecordDate)) : null;
                    String recordTimeStr = colIndexRecordTime >= 0 ? getCellValueAsString(row.getCell(colIndexRecordTime)) : null;
                    String weightKgStr = getCellValueAsString(row.getCell(colIndexWeightKg));
                    String bodyFatPercentageStr = colIndexBodyFatPercentage >= 0 ? getCellValueAsString(row.getCell(colIndexBodyFatPercentage)) : null;
                    String muscleMassKgStr = colIndexMuscleMassKg >= 0 ? getCellValueAsString(row.getCell(colIndexMuscleMassKg)) : null;
                    String heightCmStr = colIndexHeightCm >= 0 ? getCellValueAsString(row.getCell(colIndexHeightCm)) : null;
                    String notes = colIndexNotes >= 0 ? getCellValueAsString(row.getCell(colIndexNotes)) : null;

                    // 验证必需字段
                    if (weightKgStr == null || weightKgStr.trim().isEmpty()) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：体重不能为空", rowIndex + 1));
                        continue;
                    }

                    // 解析体重
                    BigDecimal weightKg;
                    try {
                        weightKg = new BigDecimal(weightKgStr.trim());
                        if (weightKg.compareTo(new BigDecimal("20")) < 0 || weightKg.compareTo(new BigDecimal("300")) > 0) {
                            result.setFailed(result.getFailed() + 1);
                            result.getErrors().add(String.format("第%d行：体重必须在20.00-300.00公斤之间", rowIndex + 1));
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：体重格式错误", rowIndex + 1));
                        continue;
                    }

                    // 构建创建DTO
                    WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
                    if (StringUtils.hasText(recordDateStr)) {
                        createDTO.setRecordDate(recordDateStr.trim());
                    }
                    if (StringUtils.hasText(recordTimeStr)) {
                        createDTO.setRecordTime(recordTimeStr.trim());
                    }
                    createDTO.setWeightKg(weightKg);

                    // 解析可选字段
                    if (StringUtils.hasText(bodyFatPercentageStr)) {
                        try {
                            BigDecimal bodyFatPercentage = new BigDecimal(bodyFatPercentageStr.trim());
                            if (bodyFatPercentage.compareTo(new BigDecimal("5")) >= 0 && 
                                bodyFatPercentage.compareTo(new BigDecimal("50")) <= 0) {
                                createDTO.setBodyFatPercentage(bodyFatPercentage);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误，不设置该字段
                        }
                    }

                    if (StringUtils.hasText(muscleMassKgStr)) {
                        try {
                            BigDecimal muscleMassKg = new BigDecimal(muscleMassKgStr.trim());
                            if (muscleMassKg.compareTo(BigDecimal.ZERO) > 0) {
                                createDTO.setMuscleMassKg(muscleMassKg);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误，不设置该字段
                        }
                    }

                    if (StringUtils.hasText(heightCmStr)) {
                        try {
                            BigDecimal heightCm = new BigDecimal(heightCmStr.trim());
                            if (heightCm.compareTo(new BigDecimal("100")) >= 0 && 
                                heightCm.compareTo(new BigDecimal("250")) <= 0) {
                                createDTO.setHeightCm(heightCm);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略格式错误，不设置该字段
                        }
                    }

                    if (StringUtils.hasText(notes)) {
                        createDTO.setNotes(notes.trim());
                    }

                    // 创建记录
                    weightRecordService.create(createDTO, userId);
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
            Sheet sheet = workbook.createSheet("体重记录导入模板");

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
            String[] headers = {"记录日期", "记录时间", "体重(公斤)", "体脂率(%)", "肌肉量(公斤)", "身高(厘米)", "备注"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("2025-01-15");
            exampleRow.createCell(1).setCellValue("08:00");
            exampleRow.createCell(2).setCellValue(70.5);
            exampleRow.createCell(3).setCellValue(15.0);
            exampleRow.createCell(4).setCellValue(55.0);
            exampleRow.createCell(5).setCellValue(175.0);
            exampleRow.createCell(6).setCellValue("晨起测量");

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
                        // 判断是日期还是时间
                        String timeStr = TIME_FORMAT.format(date);
                        if (timeStr.equals("00:00")) {
                            // 是日期
                            return DATE_FORMAT.format(date);
                        } else {
                            // 是时间
                            return TIME_FORMAT.format(date);
                        }
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

