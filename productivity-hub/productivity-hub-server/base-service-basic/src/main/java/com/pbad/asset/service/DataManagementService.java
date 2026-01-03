package com.pbad.asset.service;

import com.pbad.asset.domain.vo.DataImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 数据管理服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface DataManagementService {

    /**
     * 导入数据
     *
     * @param file        文件
     * @param dataType    数据类型（asset/wishlist）
     * @param incremental 是否增量导入
     * @return 导入结果
     */
    DataImportResultVO importData(MultipartFile file, String dataType, boolean incremental);

    /**
     * 导出数据
     *
     * @param dataType 数据类型（asset/wishlist）
     * @param format   导出格式（excel/csv）
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportData(String dataType, String format, HttpServletResponse response) throws IOException;
}

