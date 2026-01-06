package com.pbad.codegenerator.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.codegenerator.domain.dto.*;
import com.pbad.codegenerator.domain.po.CompanyTemplatePO;
import com.pbad.codegenerator.domain.po.DatabaseConfigPO;
import com.pbad.codegenerator.domain.vo.*;
import com.pbad.codegenerator.mapper.CompanyTemplateMapper;
import com.pbad.codegenerator.mapper.DatabaseConfigMapper;
import com.pbad.codegenerator.service.CodeGeneratorService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGeneratorServiceImpl implements CodeGeneratorService {

    private final CompanyTemplateMapper companyTemplateMapper;
    private final DatabaseConfigMapper databaseConfigMapper;
    private final IdGeneratorApi idGeneratorApi;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ========== 公司模板管理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CompanyTemplateVO> getAllCompanyTemplates(String userId) {
        List<CompanyTemplatePO> poList = companyTemplateMapper.selectByUserId(userId);
        if (poList.isEmpty()) {
            CompanyTemplatePO defaultTemplate = createDefaultCompanyTemplate(userId);
            // createDefaultCompanyTemplate already inserts the record, no need to insert again
            poList = Collections.singletonList(defaultTemplate);
        }
        return poList.stream().map(this::convertToTemplateVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyTemplateVO getCompanyTemplateById(String id, String userId) {
        CompanyTemplatePO po = companyTemplateMapper.selectByIdAndUserId(id, userId);
        if (po == null) {
            throw new BusinessException("404", "模板不存在或无权限访问");
        }
        return convertToTemplateVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompanyTemplateVO saveCompanyTemplate(CompanyTemplateDTO dto, String userId) {
        if (dto == null || !StringUtils.hasText(dto.getName()) || !StringUtils.hasText(dto.getBasePackage())) {
            throw new BusinessException("400", "模板名称和基础包名不能为空");
        }

        String now = DATE_FORMAT.format(new Date());
        CompanyTemplatePO po = new CompanyTemplatePO();
        if (StringUtils.hasText(dto.getId())) {
            // 更新
            CompanyTemplatePO existing = companyTemplateMapper.selectByIdAndUserId(dto.getId(), userId);
            if (existing == null) {
                throw new BusinessException("404", "模板不存在或无权限访问");
            }
            po.setId(dto.getId());
            po.setCreatedBy(existing.getCreatedBy()); // 保持原有的创建人
            po.setCreatedAt(existing.getCreatedAt()); // 保持原有的创建时间
        } else {
            // 创建
            po.setId(idGeneratorApi.generateId());
            po.setCreatedBy(userId);
            po.setCreatedAt(now);
        }

        po.setName(dto.getName());
        po.setDescription(dto.getDescription());
        po.setBasePackage(dto.getBasePackage());
        po.setAuthor(dto.getAuthor());
        po.setTemplatesJson(JSON.toJSONString(dto.getTemplates()));
        po.setUpdatedBy(userId);
        po.setUpdatedAt(now);

        if (StringUtils.hasText(dto.getId())) {
            // 使用带用户ID验证的更新方法
            int updated = companyTemplateMapper.updateByIdAndUserId(po);
            if (updated == 0) {
                throw new BusinessException("403", "无权限更新该模板");
            }
        } else {
            companyTemplateMapper.insert(po);
        }

        return convertToTemplateVO(companyTemplateMapper.selectById(po.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCompanyTemplate(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "模板ID不能为空");
        }
        int deleted = companyTemplateMapper.deleteByIdAndUserId(id, userId);
        if (deleted == 0) {
            throw new BusinessException("404", "模板不存在或无权限删除");
        }
    }

    // ========== 数据库配置管理 ==========

    @Override
    @Transactional(readOnly = true)
    public List<DatabaseConfigVO> getAllDatabaseConfigs(String userId) {
        List<DatabaseConfigPO> poList = databaseConfigMapper.selectByUserId(userId);
        return poList.stream().map(this::convertToConfigVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DatabaseConfigVO getDatabaseConfigById(String id, String userId) {
        DatabaseConfigPO po = databaseConfigMapper.selectByIdAndUserId(id, userId);
        if (po == null) {
            throw new BusinessException("404", "数据库配置不存在或无权限访问");
        }
        return convertToConfigVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatabaseConfigVO saveDatabaseConfig(DatabaseConfigDTO dto, String userId) {
        if (dto == null || !StringUtils.hasText(dto.getName()) || !StringUtils.hasText(dto.getHost())
                || !StringUtils.hasText(dto.getDatabase())) {
            throw new BusinessException("400", "配置名称、主机和数据库名不能为空");
        }

        String now = DATE_FORMAT.format(new Date());
        DatabaseConfigPO po = new DatabaseConfigPO();
        if (StringUtils.hasText(dto.getId())) {
            // 更新
            DatabaseConfigPO existing = databaseConfigMapper.selectByIdAndUserId(dto.getId(), userId);
            if (existing == null) {
                throw new BusinessException("404", "数据库配置不存在或无权限访问");
            }
            po.setId(dto.getId());
            po.setCreatedBy(existing.getCreatedBy()); // 保持原有的创建人
            po.setCreatedAt(existing.getCreatedAt()); // 保持原有的创建时间
        } else {
            // 创建
            po.setId(idGeneratorApi.generateId());
            po.setCreatedBy(userId);
            po.setCreatedAt(now);
        }

        po.setName(dto.getName());
        po.setType(dto.getType());
        po.setHost(dto.getHost());
        po.setPort(dto.getPort());
        po.setDatabase(dto.getDatabase());
        po.setSchema(dto.getSchema());
        po.setUsername(dto.getUsername());
        // TODO: 密码加密存储
        po.setPassword(dto.getPassword());
        po.setUpdatedBy(userId);
        po.setUpdatedAt(now);

        if (StringUtils.hasText(dto.getId())) {
            // 使用带用户ID验证的更新方法
            int updated = databaseConfigMapper.updateByIdAndUserId(po);
            if (updated == 0) {
                throw new BusinessException("403", "无权限更新该配置");
            }
        } else {
            databaseConfigMapper.insert(po);
        }

        return convertToConfigVO(databaseConfigMapper.selectById(po.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDatabaseConfig(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "配置ID不能为空");
        }
        int deleted = databaseConfigMapper.deleteByIdAndUserId(id, userId);
        if (deleted == 0) {
            throw new BusinessException("404", "数据库配置不存在或无权限删除");
        }
    }

    // ========== 表结构解析 ==========

    @Override
    @Transactional(readOnly = true)
    public List<TableInfoVO> parseTableStructure(ParseTableRequestDTO request, String userId) {
        if (request == null || !StringUtils.hasText(request.getDatabaseConfigId())) {
            throw new BusinessException("400", "数据库配置ID不能为空");
        }

        DatabaseConfigPO config = databaseConfigMapper.selectByIdAndUserId(request.getDatabaseConfigId(), userId);
        if (config == null) {
            throw new BusinessException("404", "数据库配置不存在或无权限访问");
        }

        Connection connection = null;
        try {
            // 建立数据库连接
            connection = createConnection(config);

            // 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = config.getDatabase();
            String schema = config.getSchema();
            String tableNamePattern = StringUtils.hasText(request.getTableName()) ? request.getTableName() : "%";

            // 查询表列表
            ResultSet tables = metaData.getTables(catalog, schema, tableNamePattern, new String[]{"TABLE"});
            List<TableInfoVO> tableInfoList = new ArrayList<>();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                String tableComment = tables.getString("REMARKS");

                TableInfoVO tableInfo = new TableInfoVO();
                tableInfo.setName(tableName);
                tableInfo.setComment(tableComment);
                tableInfo.setJavaClassName(convertToPascalCase(tableName));
                tableInfo.setJavaPackage("com.example.entity");

                // 查询字段信息
                List<TableColumnVO> columns = parseColumns(metaData, catalog, schema, tableName);
                tableInfo.setColumns(columns);

                tableInfoList.add(tableInfo);
            }

            return tableInfoList;
        } catch (SQLException e) {
            log.error("解析表结构失败", e);
            throw new BusinessException("500", "解析表结构失败: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }

    // ========== 代码生成 ==========

    @Override
    @Transactional(readOnly = true)
    public List<GeneratedCodeVO> generateCode(GenerateCodeRequestDTO request, String userId) {
        if (request == null || !StringUtils.hasText(request.getCompanyTemplateId())
                || request.getTableInfo() == null) {
            throw new BusinessException("400", "模板ID和表信息不能为空");
        }

        CompanyTemplatePO templatePO = companyTemplateMapper.selectByIdAndUserId(request.getCompanyTemplateId(), userId);
        if (templatePO == null) {
            throw new BusinessException("404", "模板不存在或无权限访问");
        }

        // 解析模板JSON
        List<FileTemplateDTO> fileTemplates = JSON.parseArray(templatePO.getTemplatesJson(), FileTemplateDTO.class);
        if (fileTemplates == null || fileTemplates.isEmpty()) {
            throw new BusinessException("400", "模板内容为空");
        }

        // 过滤启用的模板
        List<FileTemplateDTO> enabledTemplates = fileTemplates.stream()
                .filter(t -> t.getEnabled() != null && t.getEnabled())
                .collect(Collectors.toList());

        if (request.getSelectedTemplateIds() != null && !request.getSelectedTemplateIds().isEmpty()) {
            enabledTemplates = enabledTemplates.stream()
                    .filter(t -> request.getSelectedTemplateIds().contains(t.getId()))
                    .collect(Collectors.toList());
        }

        // 生成代码
        List<GeneratedCodeVO> generatedCodes = new ArrayList<>();
        for (FileTemplateDTO template : enabledTemplates) {
            GeneratedCodeVO code = generateCodeFromTemplate(template, templatePO, request);
            generatedCodes.add(code);
        }

        return generatedCodes;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCodeZip(GenerateCodeRequestDTO request, String userId) {
        List<GeneratedCodeVO> codes = generateCode(request, userId);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (GeneratedCodeVO code : codes) {
                String entryName = StringUtils.hasText(code.getType())
                        ? code.getType() + "/" + code.getFileName()
                        : code.getFileName();
                zos.putNextEntry(new ZipEntry(entryName));
                byte[] data = Optional.ofNullable(code.getContent())
                        .orElse("")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8);
                zos.write(data);
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("生成代码压缩包失败", e);
            throw new BusinessException("500", "生成压缩包失败: " + e.getMessage());
        }
    }

    // ========== 私有方法 ==========

    /**
     * 转换为模板VO
     */
    private CompanyTemplateVO convertToTemplateVO(CompanyTemplatePO po) {
        CompanyTemplateVO vo = new CompanyTemplateVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setDescription(po.getDescription());
        vo.setBasePackage(po.getBasePackage());
        vo.setAuthor(po.getAuthor());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());

        // 解析模板JSON
        if (StringUtils.hasText(po.getTemplatesJson())) {
            List<FileTemplateVO> templates = JSON.parseArray(po.getTemplatesJson(), FileTemplateVO.class);
            vo.setTemplates(templates);
        }

        return vo;
    }

    /**
     * 转换为配置VO
     */
    private DatabaseConfigVO convertToConfigVO(DatabaseConfigPO po) {
        DatabaseConfigVO vo = new DatabaseConfigVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setType(po.getType());
        vo.setHost(po.getHost());
        vo.setPort(po.getPort());
        vo.setDatabase(po.getDatabase());
        vo.setSchema(po.getSchema());
        vo.setUsername(po.getUsername());
        // 不返回密码
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }

    /**
     * 创建数据库连接
     */
    private Connection createConnection(DatabaseConfigPO config) throws SQLException {
        String url = buildJdbcUrl(config);
        String driverClass = getDriverClass(config.getType());

        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new BusinessException("500", "数据库驱动未找到: " + driverClass);
        }

        return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
    }

    /**
     * 构建JDBC URL
     */
    private String buildJdbcUrl(DatabaseConfigPO config) {
        String type = config.getType().toLowerCase();
        switch (type) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai",
                        config.getHost(), config.getPort(), config.getDatabase());
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s",
                        config.getHost(), config.getPort(), config.getDatabase());
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s",
                        config.getHost(), config.getPort(), config.getDatabase());
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                        config.getHost(), config.getPort(), config.getDatabase());
            case "sqlite":
                return String.format("jdbc:sqlite:%s", config.getDatabase());
            default:
                throw new BusinessException("400", "不支持的数据库类型: " + type);
        }
    }

    /**
     * 获取驱动类名
     */
    private String getDriverClass(String type) {
        String lowerType = type.toLowerCase();
        switch (lowerType) {
            case "mysql":
                return "com.mysql.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "oracle":
                return "oracle.jdbc.driver.OracleDriver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "sqlite":
                return "org.sqlite.JDBC";
            default:
                throw new BusinessException("400", "不支持的数据库类型: " + type);
        }
    }

    /**
     * 解析表字段
     */
    private List<TableColumnVO> parseColumns(DatabaseMetaData metaData, String catalog, String schema, String tableName)
            throws SQLException {
        List<TableColumnVO> columns = new ArrayList<>();
        ResultSet columnsRs = metaData.getColumns(catalog, schema, tableName, "%");

        // 获取主键信息
        ResultSet pkRs = metaData.getPrimaryKeys(catalog, schema, tableName);
        Set<String> primaryKeys = new HashSet<>();
        while (pkRs.next()) {
            primaryKeys.add(pkRs.getString("COLUMN_NAME"));
        }
        pkRs.close();

        while (columnsRs.next()) {
            TableColumnVO column = new TableColumnVO();
            column.setName(columnsRs.getString("COLUMN_NAME"));
            column.setType(columnsRs.getString("TYPE_NAME"));
            column.setLength(columnsRs.getInt("COLUMN_SIZE"));
            column.setNullable(columnsRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
            column.setPrimaryKey(primaryKeys.contains(column.getName()));
            column.setAutoIncrement("YES".equals(columnsRs.getString("IS_AUTOINCREMENT")));
            column.setDefaultValue(columnsRs.getString("COLUMN_DEF"));
            column.setComment(columnsRs.getString("REMARKS"));

            // 映射Java类型
            column.setJavaType(mapDbTypeToJava(column.getType()));
            // 转换为Java字段名（下划线转驼峰）
            column.setJavaField(convertToCamelCase(column.getName()));

            columns.add(column);
        }
        columnsRs.close();

        return columns;
    }

    /**
     * 数据库类型映射到Java类型
     */
    private String mapDbTypeToJava(String dbType) {
        if (dbType == null) {
            return "String";
        }
        String lowerType = dbType.toLowerCase();
        if (lowerType.contains("int") && !lowerType.contains("bigint")) {
            return "Integer";
        } else if (lowerType.contains("bigint")) {
            return "Long";
        } else if (lowerType.contains("smallint") || lowerType.contains("tinyint")) {
            return "Short";
        } else if (lowerType.contains("decimal") || lowerType.contains("numeric")) {
            return "BigDecimal";
        } else if (lowerType.contains("float")) {
            return "Float";
        } else if (lowerType.contains("double")) {
            return "Double";
        } else if (lowerType.contains("varchar") || lowerType.contains("char") || lowerType.contains("text")) {
            return "String";
        } else if (lowerType.contains("date") || lowerType.contains("time")) {
            return "Date";
        } else if (lowerType.contains("boolean") || lowerType.contains("bit")) {
            return "Boolean";
        }
        return "String";
    }

    /**
     * 下划线转驼峰
     */
    private String convertToCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * 转PascalCase
     */
    private String convertToPascalCase(String str) {
        String camelCase = convertToCamelCase(str);
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }

    /**
     * 从模板生成代码
     */
    private GeneratedCodeVO generateCodeFromTemplate(FileTemplateDTO template, CompanyTemplatePO companyTemplate,
                                                      GenerateCodeRequestDTO request) {
        String content = template.getContent();
        TableInfoDTO tableInfo = request.getTableInfo();
        Map<String, String> fieldMappings = Optional.ofNullable(request.getFieldMappings())
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(FieldMappingDTO::getDbField, FieldMappingDTO::getJavaField,
                        (existing, replacement) -> replacement, LinkedHashMap::new));

        String className = StringUtils.hasText(tableInfo.getJavaClassName())
                ? tableInfo.getJavaClassName() : convertToPascalCase(tableInfo.getName());
        String classNameLower = className.substring(0, 1).toLowerCase() + className.substring(1);

        // 主键信息（优先使用标记了primaryKey的字段，否则使用第一个字段）
        TableColumnDTO pkColumn = tableInfo.getColumns().stream()
                .filter(col -> Boolean.TRUE.equals(col.getPrimaryKey()))
                .findFirst()
                .orElseGet(() -> tableInfo.getColumns().isEmpty() ? null : tableInfo.getColumns().get(0));
        String pkColumnName = pkColumn != null ? pkColumn.getName() : "id";
        String pkJavaField = pkColumn != null
                ? fieldMappings.getOrDefault(pkColumn.getName(),
                pkColumn.getJavaField() != null ? pkColumn.getJavaField() : convertToCamelCase(pkColumn.getName()))
                : "id";
        String pkJavaType = pkColumn != null
                ? (pkColumn.getJavaType() != null ? pkColumn.getJavaType() : mapDbTypeToJava(pkColumn.getType()))
                : "Long";
        String pkJavaFieldCapitalized = pkJavaField.substring(0, 1).toUpperCase() + pkJavaField.substring(1);
        String pkParam = "#{"+ pkJavaField + "}";

        // 替换变量
        content = content.replace("{{basePackage}}", companyTemplate.getBasePackage());
        content = content.replace("{{className}}", className);
        content = content.replace("{{classNameLower}}", classNameLower);
        content = content.replace("{{author}}", companyTemplate.getAuthor());
        content = content.replace("{{tableName}}", tableInfo.getName());
        content = content.replace("{{pkColumn}}", pkColumnName);
        content = content.replace("{{pkJavaField}}", pkJavaField);
        content = content.replace("{{pkJavaFieldCapitalized}}", pkJavaFieldCapitalized);
        content = content.replace("{{pkJavaType}}", pkJavaType);
        content = content.replace("{{pkParam}}", pkParam);

        // 生成字段
        if (content.contains("{{fields}}")) {
            StringBuilder fields = new StringBuilder();
            for (TableColumnDTO col : tableInfo.getColumns()) {
                String javaField = fieldMappings.getOrDefault(col.getName(),
                        col.getJavaField() != null ? col.getJavaField() : convertToCamelCase(col.getName()));
                String javaType = col.getJavaType() != null ? col.getJavaType() : mapDbTypeToJava(col.getType());
                String comment = StringUtils.hasText(col.getComment()) ? "    // " + col.getComment() : "";
                fields.append("    private ").append(javaType).append(" ").append(javaField).append(";")
                        .append(comment).append("\n");
            }
            content = content.replace("{{fields}}", fields.toString());
        }

        // 生成ResultMap
        if (content.contains("{{resultMap}}")) {
            StringBuilder resultMap = new StringBuilder();
            for (TableColumnDTO col : tableInfo.getColumns()) {
                String javaField = fieldMappings.getOrDefault(col.getName(),
                        col.getJavaField() != null ? col.getJavaField() : convertToCamelCase(col.getName()));
                resultMap.append("        <result column=\"").append(col.getName())
                        .append("\" property=\"").append(javaField).append("\" />\n");
            }
            content = content.replace("{{resultMap}}", resultMap.toString());
        }

        // 生成列集合
        if (content.contains("{{allColumns}}")) {
            String allColumns = tableInfo.getColumns().stream()
                    .map(TableColumnDTO::getName)
                    .collect(Collectors.joining(", "));
            content = content.replace("{{allColumns}}", allColumns);
        }

        // 生成插入列和值
        if (content.contains("{{insertColumns}}") || content.contains("{{insertValues}}")) {
            String insertColumns = tableInfo.getColumns().stream()
                    .map(TableColumnDTO::getName)
                    .collect(Collectors.joining(",\n        "));
            String insertValues = tableInfo.getColumns().stream()
                    .map(col -> fieldMappings.getOrDefault(col.getName(),
                            col.getJavaField() != null ? col.getJavaField() : convertToCamelCase(col.getName())))
                    .map(javaField -> "#{" + javaField + "}")
                    .collect(Collectors.joining(",\n        "));
            content = content.replace("{{insertColumns}}", insertColumns);
            content = content.replace("{{insertValues}}", insertValues);
        }

        // 生成更新语句片段（跳过主键）
        if (content.contains("{{updateSets}}")) {
            String updateSets = tableInfo.getColumns().stream()
                    .filter(col -> !Objects.equals(col.getName(), pkColumnName))
                    .map(col -> {
                        String javaField = fieldMappings.getOrDefault(col.getName(),
                                col.getJavaField() != null ? col.getJavaField() : convertToCamelCase(col.getName()));
                        return "        " + col.getName() + " = #{" + javaField + "}";
                    })
                    .collect(Collectors.joining(",\n"));
            if (!StringUtils.hasText(updateSets)) {
                updateSets = "        " + pkColumnName + " = #{" + pkJavaField + "}";
            }
            content = content.replace("{{updateSets}}", updateSets);
        }

        // 生成文件名
        String fileName = template.getFileNamePattern();
        fileName = fileName.replace("{{className}}", className);
        fileName = fileName.replace("{{classNameLower}}", classNameLower);

        GeneratedCodeVO code = new GeneratedCodeVO();
        code.setFileName(fileName);
        code.setContent(content);
        code.setType(template.getType());

        return code;
    }

    /**
     * 创建默认模板（包含基础的增删改查配置）
     */
    private CompanyTemplatePO createDefaultCompanyTemplate(String userId) {
        String now = DATE_FORMAT.format(new Date());
        CompanyTemplatePO po = new CompanyTemplatePO();
        po.setId(idGeneratorApi.generateId());
        po.setName("默认模板");
        po.setDescription("Spring Boot + MyBatis 标准模板");
        po.setBasePackage("com.example");
        po.setAuthor("System");
        po.setCreatedBy(userId);
        po.setUpdatedBy(userId);
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        List<FileTemplateDTO> templates = new ArrayList<>();

        FileTemplateDTO entity = new FileTemplateDTO();
        entity.setId("entity");
        entity.setName("实体类");
        entity.setType("entity");
        entity.setFileNamePattern("{{className}}.java");
        entity.setEnabled(true);
        entity.setContent("package {{basePackage}}.entity;\n\n"
                + "import lombok.Data;\n"
                + "import java.io.Serializable;\n"
                + "import java.math.BigDecimal;\n"
                + "import java.time.LocalDate;\n"
                + "import java.time.LocalDateTime;\n"
                + "import java.util.Date;\n\n"
                + "@Data\n"
                + "public class {{className}} implements Serializable {\n"
                + "{{fields}}"
                + "}");
        templates.add(entity);

        FileTemplateDTO controller = new FileTemplateDTO();
        controller.setId("controller");
        controller.setName("Controller");
        controller.setType("controller");
        controller.setFileNamePattern("{{className}}Controller.java");
        controller.setEnabled(true);
        controller.setContent("package {{basePackage}}.controller;\n\n"
                + "import {{basePackage}}.entity.{{className}};\n"
                + "import {{basePackage}}.service.{{className}}Service;\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.web.bind.annotation.*;\n\n"
                + "import java.util.List;\n\n"
                + "@RestController\n"
                + "@RequestMapping(\"/api/{{classNameLower}}\")\n"
                + "@RequiredArgsConstructor\n"
                + "public class {{className}}Controller {\n\n"
                + "    private final {{className}}Service {{classNameLower}}Service;\n\n"
                + "    @GetMapping\n"
                + "    public List<{{className}}> list() {\n"
                + "        return {{classNameLower}}Service.listAll();\n"
                + "    }\n\n"
                + "    @GetMapping(\"/{id}\")\n"
                + "    public {{className}} detail(@PathVariable(\"id\") {{pkJavaType}} {{pkJavaField}}) {\n"
                + "        return {{classNameLower}}Service.getById({{pkJavaField}});\n"
                + "    }\n\n"
                + "    @PostMapping\n"
                + "    public int create(@RequestBody {{className}} body) {\n"
                + "        return {{classNameLower}}Service.create(body);\n"
                + "    }\n\n"
                + "    @PutMapping(\"/{id}\")\n"
                + "    public int update(@PathVariable(\"id\") {{pkJavaType}} {{pkJavaField}}, @RequestBody {{className}} body) {\n"
                + "        body.set{{pkJavaFieldCapitalized}}({{pkJavaField}});\n"
                + "        return {{classNameLower}}Service.update(body);\n"
                + "    }\n\n"
                + "    @DeleteMapping(\"/{id}\")\n"
                + "    public int delete(@PathVariable(\"id\") {{pkJavaType}} {{pkJavaField}}) {\n"
                + "        return {{classNameLower}}Service.delete({{pkJavaField}});\n"
                + "    }\n"
                + "}");
        templates.add(controller);

        FileTemplateDTO service = new FileTemplateDTO();
        service.setId("service");
        service.setName("Service接口");
        service.setType("service");
        service.setFileNamePattern("{{className}}Service.java");
        service.setEnabled(true);
        service.setContent("package {{basePackage}}.service;\n\n"
                + "import {{basePackage}}.entity.{{className}};\n"
                + "import java.util.List;\n\n"
                + "public interface {{className}}Service {\n"
                + "    List<{{className}}> listAll();\n\n"
                + "    {{className}} getById({{pkJavaType}} {{pkJavaField}});\n\n"
                + "    int create({{className}} data);\n\n"
                + "    int update({{className}} data);\n\n"
                + "    int delete({{pkJavaType}} {{pkJavaField}});\n"
                + "}");
        templates.add(service);

        FileTemplateDTO serviceImpl = new FileTemplateDTO();
        serviceImpl.setId("serviceImpl");
        serviceImpl.setName("Service实现");
        serviceImpl.setType("serviceImpl");
        serviceImpl.setFileNamePattern("{{className}}ServiceImpl.java");
        serviceImpl.setEnabled(true);
        serviceImpl.setContent("package {{basePackage}}.service.impl;\n\n"
                + "import {{basePackage}}.entity.{{className}};\n"
                + "import {{basePackage}}.service.{{className}}Service;\n"
                + "import {{basePackage}}.mapper.{{className}}Mapper;\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.stereotype.Service;\n\n"
                + "@Service\n"
                + "@RequiredArgsConstructor\n"
                + "public class {{className}}ServiceImpl implements {{className}}Service {\n\n"
                + "    private final {{className}}Mapper {{classNameLower}}Mapper;\n\n"
                + "    @Override\n"
                + "    public java.util.List<{{className}}> listAll() {\n"
                + "        return {{classNameLower}}Mapper.selectAll();\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    public {{className}} getById({{pkJavaType}} {{pkJavaField}}) {\n"
                + "        return {{classNameLower}}Mapper.selectById({{pkJavaField}});\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    public int create({{className}} data) {\n"
                + "        return {{classNameLower}}Mapper.insert(data);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    public int update({{className}} data) {\n"
                + "        return {{classNameLower}}Mapper.update(data);\n"
                + "    }\n\n"
                + "    @Override\n"
                + "    public int delete({{pkJavaType}} {{pkJavaField}}) {\n"
                + "        return {{classNameLower}}Mapper.delete({{pkJavaField}});\n"
                + "    }\n"
                + "}");
        templates.add(serviceImpl);

        FileTemplateDTO mapper = new FileTemplateDTO();
        mapper.setId("mapper");
        mapper.setName("Mapper接口");
        mapper.setType("mapper");
        mapper.setFileNamePattern("{{className}}Mapper.java");
        mapper.setEnabled(true);
        mapper.setContent("package {{basePackage}}.mapper;\n\n"
                + "import {{basePackage}}.entity.{{className}};\n"
                + "import org.apache.ibatis.annotations.Mapper;\n"
                + "import org.apache.ibatis.annotations.Param;\n"
                + "import java.util.List;\n\n"
                + "@Mapper\n"
                + "public interface {{className}}Mapper {\n"
                + "    List<{{className}}> selectAll();\n\n"
                + "    {{className}} selectById(@Param(\"id\") {{pkJavaType}} {{pkJavaField}});\n\n"
                + "    int insert({{className}} entity);\n\n"
                + "    int update({{className}} entity);\n\n"
                + "    int delete(@Param(\"id\") {{pkJavaType}} {{pkJavaField}});\n"
                + "}");
        templates.add(mapper);

        FileTemplateDTO mapperXml = new FileTemplateDTO();
        mapperXml.setId("mapperXml");
        mapperXml.setName("Mapper XML");
        mapperXml.setType("mapperXml");
        mapperXml.setFileNamePattern("{{className}}Mapper.xml");
        mapperXml.setEnabled(true);
        mapperXml.setContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n"
                + "<mapper namespace=\"{{basePackage}}.mapper.{{className}}Mapper\">\n"
                + "    <resultMap id=\"BaseResultMap\" type=\"{{basePackage}}.entity.{{className}}\">\n"
                + "{{resultMap}}"
                + "    </resultMap>\n\n"
                + "    <sql id=\"Base_Column_List\">\n"
                + "        {{allColumns}}\n"
                + "    </sql>\n\n"
                + "    <select id=\"selectAll\" resultMap=\"BaseResultMap\">\n"
                + "        select\n"
                + "        <include refid=\"Base_Column_List\"/>\n"
                + "        from {{tableName}}\n"
                + "    </select>\n\n"
                + "    <select id=\"selectById\" parameterType=\"{{pkJavaType}}\" resultMap=\"BaseResultMap\">\n"
                + "        select\n"
                + "        <include refid=\"Base_Column_List\"/>\n"
                + "        from {{tableName}}\n"
                + "        where {{pkColumn}} = {{pkParam}}\n"
                + "    </select>\n\n"
                + "    <insert id=\"insert\" parameterType=\"{{basePackage}}.entity.{{className}}\">\n"
                + "        insert into {{tableName}}\n"
                + "        (\n"
                + "        {{insertColumns}}\n"
                + "        )\n"
                + "        values\n"
                + "        (\n"
                + "        {{insertValues}}\n"
                + "        )\n"
                + "    </insert>\n\n"
                + "    <update id=\"update\" parameterType=\"{{basePackage}}.entity.{{className}}\">\n"
                + "        update {{tableName}}\n"
                + "        set\n"
                + "{{updateSets}}"
                + "        where {{pkColumn}} = {{pkParam}}\n"
                + "    </update>\n\n"
                + "    <delete id=\"delete\" parameterType=\"{{pkJavaType}}\">\n"
                + "        delete from {{tableName}}\n"
                + "        where {{pkColumn}} = {{pkParam}}\n"
                + "    </delete>\n"
                + "</mapper>");
        templates.add(mapperXml);

        po.setTemplatesJson(JSON.toJSONString(templates));
        companyTemplateMapper.insert(po);
        return po;
    }
}

