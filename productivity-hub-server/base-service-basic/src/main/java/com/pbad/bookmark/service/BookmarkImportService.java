package com.pbad.bookmark.service;

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
     * 下载导入模板
     *
     * @return Excel文件字节数组
     */
    byte[] downloadTemplate();
}

