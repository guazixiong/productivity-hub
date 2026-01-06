package com.pbad.health.service;

import com.pbad.health.domain.vo.WeightRecordImportResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 体重记录导入服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWeightRecordImportService {

    /**
     * 导入Excel文件
     *
     * @param file Excel文件
     * @return 导入结果
     */
    WeightRecordImportResultVO importFromExcel(MultipartFile file);

    /**
     * 下载导入模板
     *
     * @return Excel文件字节数组
     */
    byte[] downloadTemplate();
}

