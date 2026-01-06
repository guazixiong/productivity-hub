package com.pbad.asset.handler;

import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.mapper.WishlistMapper;
import com.pbad.asset.validator.ValidatorChainBuilder;
import com.pbad.generator.api.IdGeneratorApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV导入处理器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Component
public class CsvImportHandler extends DataImportHandler {

    public CsvImportHandler(AssetMapper assetMapper, AssetCategoryMapper categoryMapper,
                            WishlistMapper wishlistMapper, IdGeneratorApi idGeneratorApi, 
                            ValidatorChainBuilder validatorChainBuilder) {
        super(assetMapper, categoryMapper, wishlistMapper, idGeneratorApi, validatorChainBuilder);
    }

    @Override
    protected List<Map<String, Object>> parseFile(InputStream inputStream) throws Exception {
        List<Map<String, Object>> dataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // 读取表头（第一行）
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new RuntimeException("CSV文件缺少表头");
            }

            // 解析表头
            String[] headers = parseCsvLine(headerLine);
            if (headers.length == 0) {
                throw new RuntimeException("CSV文件表头为空");
            }

            // 读取数据行
            String line;
            int rowIndex = 1;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = parseCsvLine(line);
                if (values.length == 0) {
                    continue;
                }

                Map<String, Object> rowData = new HashMap<>();
                boolean hasData = false;

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String headerName = headers[i].trim();
                    String value = values[i].trim();

                    if (!value.isEmpty()) {
                        rowData.put(headerName, value);
                        hasData = true;
                    }
                }

                if (hasData) {
                    dataList.add(rowData);
                }

                rowIndex++;
            }
        }

        return dataList;
    }

    /**
     * 解析CSV行（简单实现，支持引号包围的字段）
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 转义的双引号
                    currentField.append('"');
                    i++;
                } else {
                    // 切换引号状态
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 字段分隔符
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // 添加最后一个字段
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }
}

