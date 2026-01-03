package com.pbad.bookmark.service;

import com.pbad.bookmark.domain.vo.BookmarkImportPreviewVO;
import com.pbad.bookmark.domain.vo.BookmarkImportResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * Excel导入服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface BookmarkImportService {

    /**
     * 导入Excel文件
     *
     * @param file Excel文件
     * @return 导入结果
     */
    BookmarkImportResultVO importFromExcel(MultipartFile file);

    /**
     * 预览 Excel 文件（仅解析，不落库）
     *
     * @param file Excel 文件
     * @return 预览结果
     */
    BookmarkImportPreviewVO previewFromExcel(MultipartFile file);

    /**
     * 下载导入模板
     *
     * @return Excel文件字节数组
     */
    byte[] downloadTemplate();
}

