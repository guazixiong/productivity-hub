package com.pbad.asset.handler;

import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.mapper.WishlistMapper;
import com.pbad.asset.validator.ValidatorChainBuilder;
import com.pbad.generator.api.IdGeneratorApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导入处理器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Component
public class ExcelImportHandler extends DataImportHandler {

    public ExcelImportHandler(AssetMapper assetMapper, AssetCategoryMapper categoryMapper,
                              WishlistMapper wishlistMapper, IdGeneratorApi idGeneratorApi, 
                              ValidatorChainBuilder validatorChainBuilder) {
        super(assetMapper, categoryMapper, wishlistMapper, idGeneratorApi, validatorChainBuilder);
    }

    @Override
    protected List<Map<String, Object>> parseFile(InputStream inputStream) throws Exception {
        List<Map<String, Object>> dataList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new RuntimeException("Excel文件格式不正确");
            }

            // 读取表头（第一行）
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel文件缺少表头");
            }

            // 解析表头，获取列索引映射
            Map<Integer, String> columnMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = getCellValueAsString(cell).trim();
                    columnMap.put(i, headerName);
                }
            }

            // 读取数据行（从第二行开始）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Map<String, Object> rowData = new HashMap<>();
                boolean hasData = false;

                for (Map.Entry<Integer, String> entry : columnMap.entrySet()) {
                    int colIndex = entry.getKey();
                    String columnName = entry.getValue();
                    Cell cell = row.getCell(colIndex);

                    if (cell != null) {
                        Object cellValue = getCellValue(cell);
                        if (cellValue != null && !cellValue.toString().trim().isEmpty()) {
                            rowData.put(columnName, cellValue);
                            hasData = true;
                        }
                    }
                }

                if (hasData) {
                    dataList.add(rowData);
                }
            }
        }

        return dataList;
    }

    /**
     * 获取单元格值（字符串）
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
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
                return "";
        }
    }

    /**
     * 获取单元格值（对象）
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}

