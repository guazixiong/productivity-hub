package com.pbad.bookmark.service.impl;

import com.pbad.bookmark.domain.dto.BookmarkUrlCreateDTO;
import com.pbad.bookmark.domain.po.BookmarkTagPO;
import com.pbad.bookmark.domain.vo.BookmarkImportPreviewItemVO;
import com.pbad.bookmark.domain.vo.BookmarkImportPreviewVO;
import com.pbad.bookmark.domain.vo.BookmarkImportResultVO;
import com.pbad.bookmark.mapper.BookmarkTagMapper;
import com.pbad.bookmark.service.BookmarkImportService;
import com.pbad.bookmark.service.BookmarkUrlService;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkImportServiceImpl implements BookmarkImportService {

    private final BookmarkUrlService urlService;
    private final BookmarkTagMapper tagMapper;
    private final com.pbad.bookmark.service.BookmarkTagService tagService;

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
    public BookmarkImportPreviewVO previewFromExcel(MultipartFile file) {
        // 仅做解析校验，不落库
        if (file == null || file.isEmpty()) {
            throw new BusinessException("400", "文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("400", "文件格式不正确，仅支持.xlsx和.xls格式");
        }

        BookmarkImportPreviewVO previewVO = new BookmarkImportPreviewVO();
        previewVO.setTotal(0);
        previewVO.setItems(new ArrayList<>());

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
            int colIndexParentTag = -1;
            int colIndexChildTag = -1;
            int colIndexTitle = -1;
            int colIndexUrl = -1;
            int colIndexDescription = -1;
            int colIndexIconUrl = -1;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell);
                    if (cellValue != null) {
                        cellValue = cellValue.trim();
                    } else {
                        continue;
                    }
                    switch (cellValue) {
                        case "一级标签":
                        case "父标签":
                            colIndexParentTag = i;
                            break;
                        case "二级标签":
                        case "子标签":
                            colIndexChildTag = i;
                            break;
                        case "标题":
                        case "网址标题":
                            colIndexTitle = i;
                            break;
                        case "URL":
                        case "网址":
                        case "链接":
                            colIndexUrl = i;
                            break;
                        case "描述":
                        case "网址描述":
                            colIndexDescription = i;
                            break;
                        case "图标URL":
                        case "图标":
                            colIndexIconUrl = i;
                            break;
                        default:
                            break;
                    }
                }
            }

            // 验证必需字段
            if (colIndexTitle < 0 || colIndexUrl < 0) {
                throw new BusinessException("400", "Excel文件缺少必需字段：标题和URL");
            }

            // 从第二行开始读取数据
            int totalRows = sheet.getLastRowNum();
            previewVO.setTotal(totalRows);

            List<BookmarkImportPreviewItemVO> items = new ArrayList<>();

            for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String title = getCellValueAsString(row.getCell(colIndexTitle));
                String url = getCellValueAsString(row.getCell(colIndexUrl));

                // 跳过缺少必填项的行
                if (title == null || title.trim().isEmpty()) {
                    continue;
                }
                if (url == null || url.trim().isEmpty()) {
                    continue;
                }

                BookmarkImportPreviewItemVO item = new BookmarkImportPreviewItemVO();
                item.setParentTagName(colIndexParentTag >= 0 ? getCellValueAsString(row.getCell(colIndexParentTag)) : null);
                item.setChildTagName(colIndexChildTag >= 0 ? getCellValueAsString(row.getCell(colIndexChildTag)) : null);
                item.setTitle(title);
                item.setUrl(url);
                item.setDescription(colIndexDescription >= 0 ? getCellValueAsString(row.getCell(colIndexDescription)) : null);
                item.setIconUrl(colIndexIconUrl >= 0 ? getCellValueAsString(row.getCell(colIndexIconUrl)) : null);

                items.add(item);
            }

            previewVO.setItems(items);

        } catch (IOException e) {
            throw new BusinessException("500", "读取Excel文件失败: " + e.getMessage());
        }

        return previewVO;
    }

    @Override
    public BookmarkImportResultVO importFromExcel(MultipartFile file) {
        String userId = getCurrentUserId();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("400", "文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("400", "文件格式不正确，仅支持.xlsx和.xls格式");
        }

        BookmarkImportResultVO result = new BookmarkImportResultVO();
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
            int colIndexParentTag = -1;
            int colIndexChildTag = -1;
            int colIndexTitle = -1;
            int colIndexUrl = -1;
            int colIndexDescription = -1;
            int colIndexIconUrl = -1;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell).trim();
                    switch (cellValue) {
                        case "一级标签":
                        case "父标签":
                            colIndexParentTag = i;
                            break;
                        case "二级标签":
                        case "子标签":
                            colIndexChildTag = i;
                            break;
                        case "标题":
                        case "网址标题":
                            colIndexTitle = i;
                            break;
                        case "URL":
                        case "网址":
                        case "链接":
                            colIndexUrl = i;
                            break;
                        case "描述":
                        case "网址描述":
                            colIndexDescription = i;
                            break;
                        case "图标URL":
                        case "图标":
                            colIndexIconUrl = i;
                            break;
                    }
                }
            }

            // 验证必需字段
            if (colIndexTitle < 0 || colIndexUrl < 0) {
                throw new BusinessException("400", "Excel文件缺少必需字段：标题和URL");
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
                    String parentTagName = colIndexParentTag >= 0 ? getCellValueAsString(row.getCell(colIndexParentTag)) : null;
                    String childTagName = colIndexChildTag >= 0 ? getCellValueAsString(row.getCell(colIndexChildTag)) : null;
                    String title = getCellValueAsString(row.getCell(colIndexTitle));
                    String url = getCellValueAsString(row.getCell(colIndexUrl));
                    String description = colIndexDescription >= 0 ? getCellValueAsString(row.getCell(colIndexDescription)) : null;
                    String iconUrl = colIndexIconUrl >= 0 ? getCellValueAsString(row.getCell(colIndexIconUrl)) : null;

                    // 验证必需字段
                    if (title == null || title.trim().isEmpty()) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：标题不能为空", rowIndex + 1));
                        continue;
                    }
                    if (url == null || url.trim().isEmpty()) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：URL不能为空", rowIndex + 1));
                        continue;
                    }

                    // 处理标签
                    List<String> tagIds = new ArrayList<>();
                    if (parentTagName != null && !parentTagName.trim().isEmpty()) {
                        // 查找或创建一级标签
                        BookmarkTagPO parentTag = findOrCreateParentTag(parentTagName.trim(), userId);
                        tagIds.add(parentTag.getId());

                        // 如果有二级标签，查找或创建二级标签
                        if (childTagName != null && !childTagName.trim().isEmpty()) {
                            BookmarkTagPO childTag = findOrCreateChildTag(childTagName.trim(), parentTag.getId(), userId);
                            tagIds.add(childTag.getId());
                        }
                    }

                    if (tagIds.isEmpty()) {
                        result.setFailed(result.getFailed() + 1);
                        result.getErrors().add(String.format("第%d行：至少需要指定一个标签", rowIndex + 1));
                        continue;
                    }

                    // 创建网址
                    BookmarkUrlCreateDTO createDTO = new BookmarkUrlCreateDTO();
                    createDTO.setTitle(title.trim());
                    createDTO.setUrl(url.trim());
                    createDTO.setDescription(StringUtils.hasText(description) ? description.trim() : null);
                    createDTO.setIconUrl(StringUtils.hasText(iconUrl) ? iconUrl.trim() : null);
                    createDTO.setTagIds(tagIds);

                    urlService.createUrl(createDTO);
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
            Sheet sheet = workbook.createSheet("网址导入模板");

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
            String[] headers = {"一级标签", "二级标签", "标题", "URL", "描述", "图标URL"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 创建示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("AI");
            exampleRow.createCell(1).setCellValue("常用工具");
            exampleRow.createCell(2).setCellValue("ChatGPT");
            exampleRow.createCell(3).setCellValue("https://chat.openai.com");
            exampleRow.createCell(4).setCellValue("OpenAI的AI对话工具");
            exampleRow.createCell(5).setCellValue("https://chat.openai.com/favicon.ico");

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
     * 查找或创建一级标签
     */
    private BookmarkTagPO findOrCreateParentTag(String tagName, String userId) {
        // 查找是否存在
        BookmarkTagPO tag = tagMapper.selectByNameAndParentId(tagName, null, userId);
        if (tag != null && tag.getLevel() == 1) {
            return tag;
        }

        // 不存在则创建
        com.pbad.bookmark.domain.dto.BookmarkTagCreateDTO createDTO = new com.pbad.bookmark.domain.dto.BookmarkTagCreateDTO();
        createDTO.setName(tagName);
        createDTO.setParentId(null);
        createDTO.setSortOrder(0);

        com.pbad.bookmark.domain.vo.BookmarkTagVO tagVO = tagService.createTag(createDTO);

        BookmarkTagPO newTag = new BookmarkTagPO();
        newTag.setId(tagVO.getId());
        newTag.setName(tagVO.getName());
        newTag.setLevel(1);
        return newTag;
    }

    /**
     * 查找或创建二级标签
     */
    private BookmarkTagPO findOrCreateChildTag(String tagName, String parentId, String userId) {
        // 查找是否存在
        BookmarkTagPO tag = tagMapper.selectByNameAndParentId(tagName, parentId, userId);
        if (tag != null && tag.getLevel() == 2) {
            return tag;
        }

        // 不存在则创建
        com.pbad.bookmark.domain.dto.BookmarkTagCreateDTO createDTO = new com.pbad.bookmark.domain.dto.BookmarkTagCreateDTO();
        createDTO.setName(tagName);
        createDTO.setParentId(parentId);
        createDTO.setSortOrder(0);

        com.pbad.bookmark.domain.vo.BookmarkTagVO tagVO = tagService.createTag(createDTO);

        BookmarkTagPO newTag = new BookmarkTagPO();
        newTag.setId(tagVO.getId());
        newTag.setName(tagVO.getName());
        newTag.setLevel(2);
        return newTag;
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
                return null;
        }
    }
}

