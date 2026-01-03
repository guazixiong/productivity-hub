package com.pbad.asset.service.impl;

import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.domain.po.WishlistPO;
import com.pbad.asset.domain.vo.DataImportResultVO;
import com.pbad.asset.handler.CsvImportHandler;
import com.pbad.asset.handler.DataImportHandler;
import com.pbad.asset.handler.ExcelImportHandler;
import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.mapper.WishlistMapper;
import com.pbad.asset.service.DataManagementService;
import com.pbad.asset.validator.ValidatorChainBuilder;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据管理服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataManagementServiceImpl implements DataManagementService {

    private final ExcelImportHandler excelImportHandler;
    private final CsvImportHandler csvImportHandler;
    private final AssetMapper assetMapper;
    private final AssetCategoryMapper categoryMapper;
    private final WishlistMapper wishlistMapper;
    private final ValidatorChainBuilder validatorChainBuilder;

    @Override
    public DataImportResultVO importData(MultipartFile file, String dataType, boolean incremental) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("40071", "文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new BusinessException("40071", "文件名不能为空");
        }

        // 根据文件扩展名选择导入处理器
        DataImportHandler handler;
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            handler = excelImportHandler;
        } else if (fileName.endsWith(".csv")) {
            handler = csvImportHandler;
        } else {
            throw new BusinessException("40071", "文件格式不支持，仅支持Excel(.xlsx, .xls)和CSV(.csv)格式");
        }

        try {
            return handler.importData(file, dataType, incremental);
        } catch (Exception e) {
            log.error("导入数据失败", e);
            throw new BusinessException("40072", "文件解析失败: " + e.getMessage());
        }
    }

    @Override
    public void exportData(String dataType, String format, HttpServletResponse response) throws IOException {
        String userId = RequestUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        if ("wishlist".equalsIgnoreCase(dataType)) {
            // 导出心愿单数据
            List<WishlistPO> wishlists = wishlistMapper.selectAll(userId, null);
            if ("csv".equalsIgnoreCase(format)) {
                exportWishlistCsv(wishlists, response);
            } else {
                exportWishlistExcel(wishlists, response);
            }
        } else {
            // 导出资产数据
            List<AssetPO> assets = assetMapper.selectPage(userId, null, null, 0, Integer.MAX_VALUE);
            if ("csv".equalsIgnoreCase(format)) {
                exportAssetCsv(assets, response);
            } else {
                exportAssetExcel(assets, response);
            }
        }
    }

    /**
     * 导出资产数据Excel格式
     */
    private void exportAssetExcel(List<AssetPO> assets, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=资产数据_export.xlsx");

        // 加载所有分类信息，用于分类名称转换
        Map<String, AssetCategoryPO> categoryMap = loadCategoryMap();

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("资产数据");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头（不包含ID字段）
            Row headerRow = sheet.createRow(0);
            String[] headers = {"资产名称", "一级分类", "二级分类", "价格", "图片URL", "备注", "购买日期", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 日期格式化
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // 填充数据
            int rowNum = 1;
            for (AssetPO asset : assets) {
                Row row = sheet.createRow(rowNum++);
                int colIndex = 0;

                // 资产名称
                row.createCell(colIndex++).setCellValue(asset.getName() != null ? asset.getName() : "");

                // 一级分类和二级分类
                String[] categoryNames = getCategoryNames(asset.getCategoryId(), categoryMap);
                row.createCell(colIndex++).setCellValue(categoryNames[0]);
                row.createCell(colIndex++).setCellValue(categoryNames[1]);

                // 价格
                row.createCell(colIndex++).setCellValue(asset.getPrice() != null ? asset.getPrice().doubleValue() : 0);

                // 图片URL
                row.createCell(colIndex++).setCellValue(asset.getImage() != null ? asset.getImage() : "");

                // 备注
                row.createCell(colIndex++).setCellValue(asset.getRemark() != null ? asset.getRemark() : "");

                // 购买日期（格式化为年月日）
                String purchaseDateStr = "";
                if (asset.getPurchaseDate() != null) {
                    purchaseDateStr = dateFormat.format(asset.getPurchaseDate());
                }
                row.createCell(colIndex++).setCellValue(purchaseDateStr);

                // 状态（转义为名称）
                row.createCell(colIndex++).setCellValue(formatStatus(asset.getStatus()));
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 导出资产数据CSV格式
     */
    private void exportAssetCsv(List<AssetPO> assets, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=资产数据_export.csv");

        // 加载所有分类信息
        Map<String, AssetCategoryPO> categoryMap = loadCategoryMap();

        // 日期格式化
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try (PrintWriter writer = response.getWriter()) {
            // 写入BOM，确保Excel正确识别UTF-8编码
            writer.write('\ufeff');

            // 写入表头（不包含ID字段）
            writer.println("资产名称,一级分类,二级分类,价格,图片URL,备注,购买日期,状态");

            // 写入数据
            for (AssetPO asset : assets) {
                String[] categoryNames = getCategoryNames(asset.getCategoryId(), categoryMap);
                String purchaseDateStr = asset.getPurchaseDate() != null ? dateFormat.format(asset.getPurchaseDate()) : "";

                writer.print(escapeCsvField(asset.getName()));
                writer.print(",");
                writer.print(escapeCsvField(categoryNames[0]));
                writer.print(",");
                writer.print(escapeCsvField(categoryNames[1]));
                writer.print(",");
                writer.print(asset.getPrice() != null ? asset.getPrice() : "");
                writer.print(",");
                writer.print(escapeCsvField(asset.getImage()));
                writer.print(",");
                writer.print(escapeCsvField(asset.getRemark()));
                writer.print(",");
                writer.print(escapeCsvField(purchaseDateStr));
                writer.print(",");
                writer.println(escapeCsvField(formatStatus(asset.getStatus())));
            }
        }
    }

    /**
     * 导出心愿单数据Excel格式
     */
    private void exportWishlistExcel(List<WishlistPO> wishlists, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=心愿单数据_export.xlsx");

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("心愿单数据");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 创建表头（不包含ID字段）
            Row headerRow = sheet.createRow(0);
            String[] headers = {"心愿名称", "价格", "链接", "备注", "是否已实现", "实现时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 日期格式化
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 填充数据
            int rowNum = 1;
            for (WishlistPO wishlist : wishlists) {
                Row row = sheet.createRow(rowNum++);
                int colIndex = 0;

                row.createCell(colIndex++).setCellValue(wishlist.getName() != null ? wishlist.getName() : "");
                row.createCell(colIndex++).setCellValue(wishlist.getPrice() != null ? wishlist.getPrice().doubleValue() : 0);
                row.createCell(colIndex++).setCellValue(wishlist.getLink() != null ? wishlist.getLink() : "");
                row.createCell(colIndex++).setCellValue(wishlist.getRemark() != null ? wishlist.getRemark() : "");
                row.createCell(colIndex++).setCellValue(wishlist.getAchieved() != null && wishlist.getAchieved() ? "是" : "否");
                String achievedAtStr = wishlist.getAchievedAt() != null ? dateFormat.format(wishlist.getAchievedAt()) : "";
                row.createCell(colIndex++).setCellValue(achievedAtStr);
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 导出心愿单数据CSV格式
     */
    private void exportWishlistCsv(List<WishlistPO> wishlists, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=心愿单数据_export.csv");

        // 日期格式化
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try (PrintWriter writer = response.getWriter()) {
            // 写入BOM，确保Excel正确识别UTF-8编码
            writer.write('\ufeff');

            // 写入表头（不包含ID字段）
            writer.println("心愿名称,价格,链接,备注,是否已实现,实现时间");

            // 写入数据
            for (WishlistPO wishlist : wishlists) {
                String achievedAtStr = wishlist.getAchievedAt() != null ? dateFormat.format(wishlist.getAchievedAt()) : "";
                String achieved = wishlist.getAchieved() != null && wishlist.getAchieved() ? "是" : "否";

                writer.print(escapeCsvField(wishlist.getName()));
                writer.print(",");
                writer.print(wishlist.getPrice() != null ? wishlist.getPrice() : "");
                writer.print(",");
                writer.print(escapeCsvField(wishlist.getLink()));
                writer.print(",");
                writer.print(escapeCsvField(wishlist.getRemark()));
                writer.print(",");
                writer.print(escapeCsvField(achieved));
                writer.print(",");
                writer.println(escapeCsvField(achievedAtStr));
            }
        }
    }

    /**
     * 加载所有分类信息到Map
     */
    private Map<String, AssetCategoryPO> loadCategoryMap() {
        Map<String, AssetCategoryPO> categoryMap = new HashMap<>();
        List<AssetCategoryPO> categories = categoryMapper.selectAll();
        for (AssetCategoryPO category : categories) {
            categoryMap.put(category.getId(), category);
        }
        return categoryMap;
    }

    /**
     * 根据分类ID获取一级分类和二级分类名称
     *
     * @param categoryId  分类ID
     * @param categoryMap 分类Map
     * @return [一级分类名称, 二级分类名称]
     */
    private String[] getCategoryNames(String categoryId, Map<String, AssetCategoryPO> categoryMap) {
        String[] names = {"", ""};
        if (categoryId == null || categoryId.isEmpty()) {
            return names;
        }

        AssetCategoryPO category = categoryMap.get(categoryId);
        if (category == null) {
            return names;
        }

        if (category.getLevel() != null && category.getLevel() == 2 && category.getParentId() != null) {
            // 二级分类
            names[1] = category.getName() != null ? category.getName() : "";
            AssetCategoryPO parentCategory = categoryMap.get(category.getParentId());
            if (parentCategory != null) {
                names[0] = parentCategory.getName() != null ? parentCategory.getName() : "";
            }
        } else {
            // 一级分类
            names[0] = category.getName() != null ? category.getName() : "";
        }

        return names;
    }

    /**
     * 格式化状态为中文名称
     */
    private String formatStatus(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "IN_SERVICE":
                return "正在服役";
            case "RETIRED":
                return "已退役";
            case "SOLD":
                return "已卖出";
            default:
                return status;
        }
    }

    /**
     * 转义CSV字段（处理包含逗号、引号、换行符的情况）
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        // 如果字段包含逗号、引号或换行符，需要用引号包围，并转义内部引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }

        return field;
    }
}


