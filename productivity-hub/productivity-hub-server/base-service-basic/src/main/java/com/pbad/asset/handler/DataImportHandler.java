package com.pbad.asset.handler;

import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.domain.po.WishlistPO;
import com.pbad.asset.domain.vo.DataImportResultVO;
import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.mapper.WishlistMapper;
import com.pbad.asset.validator.ValidationResult;
import com.pbad.asset.validator.ValidatorChainBuilder;
import com.pbad.generator.api.IdGeneratorApi;
import common.web.context.RequestUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象数据导入处理器（模板方法模式）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
public abstract class DataImportHandler {

    protected final AssetMapper assetMapper;
    protected final AssetCategoryMapper categoryMapper;
    protected final WishlistMapper wishlistMapper;
    protected final IdGeneratorApi idGeneratorApi;
    protected final ValidatorChainBuilder validatorChainBuilder;

    // 分类名称缓存（一级分类名称 -> 分类对象）
    private Map<String, AssetCategoryPO> firstLevelCategoryMap = null;
    // 分类名称缓存（一级分类名称 + 二级分类名称 -> 分类对象）
    private Map<String, AssetCategoryPO> secondLevelCategoryMap = null;

    public DataImportHandler(AssetMapper assetMapper, AssetCategoryMapper categoryMapper,
                             WishlistMapper wishlistMapper, IdGeneratorApi idGeneratorApi, 
                             ValidatorChainBuilder validatorChainBuilder) {
        this.assetMapper = assetMapper;
        this.categoryMapper = categoryMapper;
        this.wishlistMapper = wishlistMapper;
        this.idGeneratorApi = idGeneratorApi;
        this.validatorChainBuilder = validatorChainBuilder;
    }

    /**
     * 初始化分类缓存
     */
    private void initCategoryCache() {
        if (firstLevelCategoryMap != null) {
            return; // 已初始化
        }
        firstLevelCategoryMap = new HashMap<>();
        secondLevelCategoryMap = new HashMap<>();
        List<AssetCategoryPO> categories = categoryMapper.selectAll();
        for (AssetCategoryPO category : categories) {
            if (category.getLevel() != null && category.getLevel() == 1) {
                // 一级分类
                firstLevelCategoryMap.put(category.getName(), category);
            } else if (category.getLevel() != null && category.getLevel() == 2 && category.getParentId() != null) {
                // 二级分类，需要查找父分类名称
                AssetCategoryPO parentCategory = categoryMapper.selectById(category.getParentId());
                if (parentCategory != null) {
                    String key = parentCategory.getName() + "|" + category.getName();
                    secondLevelCategoryMap.put(key, category);
                }
            }
        }
    }

    /**
     * 模板方法：导入数据
     *
     * @param file        文件
     * @param dataType    数据类型（asset/wishlist）
     * @param incremental 是否增量导入
     * @return 导入结果
     */
    public final DataImportResultVO importData(MultipartFile file, String dataType, boolean incremental) {
        try {
            // 1. 解析文件
            List<Map<String, Object>> rawDataList = parseFile(file.getInputStream());

            if (rawDataList.isEmpty()) {
                return DataImportResultVO.fail("文件为空或没有有效数据");
            }

            // 2. 根据数据类型选择处理逻辑
            if ("wishlist".equalsIgnoreCase(dataType)) {
                return importWishlistData(rawDataList, incremental);
            } else {
                return importAssetData(rawDataList, incremental);
            }

        } catch (Exception e) {
            log.error("导入数据失败", e);
            return DataImportResultVO.fail("导入失败：" + e.getMessage());
        }
    }

    /**
     * 导入资产数据
     */
    private DataImportResultVO importAssetData(List<Map<String, Object>> rawDataList, boolean incremental) {
        // 数据校验和转换
        List<DataImportResultVO.ImportError> errors = new ArrayList<>();
        List<AssetPO> assets = new ArrayList<>();

        for (int i = 0; i < rawDataList.size(); i++) {
            Map<String, Object> rawData = rawDataList.get(i);
            try {
                // 转换数据
                AssetPO asset = convertAsset(rawData);
                if (asset == null) {
                    continue;
                }

                // 校验数据
                ValidationResult validationResult = validateAsset(asset);
                if (!validationResult.isValid()) {
                    DataImportResultVO.ImportError error = new DataImportResultVO.ImportError();
                    error.setRow(i + 2); // 行号从2开始（第1行是表头）
                    error.setMessage(String.join("; ", validationResult.getErrors()));
                    errors.add(error);
                    continue;
                }

                assets.add(asset);
            } catch (Exception e) {
                DataImportResultVO.ImportError error = new DataImportResultVO.ImportError();
                error.setRow(i + 2);
                error.setMessage("数据转换失败: " + e.getMessage());
                errors.add(error);
            }
        }

        // 数据保存
        if (assets.isEmpty()) {
            return DataImportResultVO.fail(errors);
        }

        return saveAssets(assets, incremental, errors);
    }

    /**
     * 导入心愿单数据
     */
    private DataImportResultVO importWishlistData(List<Map<String, Object>> rawDataList, boolean incremental) {
        // 数据校验和转换
        List<DataImportResultVO.ImportError> errors = new ArrayList<>();
        List<WishlistPO> wishlists = new ArrayList<>();

        for (int i = 0; i < rawDataList.size(); i++) {
            Map<String, Object> rawData = rawDataList.get(i);
            try {
                // 转换数据
                WishlistPO wishlist = convertWishlist(rawData);
                if (wishlist == null) {
                    continue;
                }

                // 校验数据
                ValidationResult validationResult = validateWishlist(wishlist);
                if (!validationResult.isValid()) {
                    DataImportResultVO.ImportError error = new DataImportResultVO.ImportError();
                    error.setRow(i + 2); // 行号从2开始（第1行是表头）
                    error.setMessage(String.join("; ", validationResult.getErrors()));
                    errors.add(error);
                    continue;
                }

                wishlists.add(wishlist);
            } catch (Exception e) {
                DataImportResultVO.ImportError error = new DataImportResultVO.ImportError();
                error.setRow(i + 2);
                error.setMessage("数据转换失败: " + e.getMessage());
                errors.add(error);
            }
        }

        // 数据保存
        if (wishlists.isEmpty()) {
            return DataImportResultVO.fail(errors);
        }

        return saveWishlists(wishlists, incremental, errors);
    }

    /**
     * 抽象方法：解析文件（由子类实现）
     *
     * @param inputStream 文件输入流
     * @return 原始数据列表（Map列表，key为字段名，value为字段值）
     */
    protected abstract List<Map<String, Object>> parseFile(InputStream inputStream) throws Exception;

    /**
     * 公共方法：转换资产数据
     *
     * @param rawData 原始数据
     * @return 资产对象
     */
    protected AssetPO convertAsset(Map<String, Object> rawData) {
        AssetPO asset = new AssetPO();
        String userId = RequestUserContext.getUserId();
        asset.setUserId(userId);

        // ID自动生成
        asset.setId(idGeneratorApi.generateId());

        // 设置基本字段
        if (rawData.containsKey("资产名称") || rawData.containsKey("name")) {
            String name = rawData.containsKey("资产名称") ? 
                String.valueOf(rawData.get("资产名称")) : String.valueOf(rawData.get("name"));
            asset.setName(name);
        }

        // 根据一级分类和二级分类名称查找分类ID
        String firstLevelName = null;
        String secondLevelName = null;
        if (rawData.containsKey("一级分类")) {
            Object firstLevelObj = rawData.get("一级分类");
            if (firstLevelObj != null && !"null".equalsIgnoreCase(String.valueOf(firstLevelObj))) {
                firstLevelName = String.valueOf(firstLevelObj);
            }
        }
        if (rawData.containsKey("二级分类")) {
            Object secondLevelObj = rawData.get("二级分类");
            if (secondLevelObj != null && !"null".equalsIgnoreCase(String.valueOf(secondLevelObj))) {
                secondLevelName = String.valueOf(secondLevelObj);
            }
        }
        
        // 如果提供了分类名称，则查找分类ID
        if (firstLevelName != null && !firstLevelName.trim().isEmpty()) {
            initCategoryCache();
            String categoryId = findCategoryId(firstLevelName, secondLevelName);
            if (categoryId != null) {
                asset.setCategoryId(categoryId);
            }
        } else if (rawData.containsKey("categoryId")) {
            // 兼容旧的categoryId字段
            Object categoryIdObj = rawData.get("categoryId");
            if (categoryIdObj != null && !"null".equalsIgnoreCase(String.valueOf(categoryIdObj))) {
                asset.setCategoryId(String.valueOf(categoryIdObj));
            }
        }

        if (rawData.containsKey("价格") || rawData.containsKey("price")) {
            Object priceObj = rawData.containsKey("价格") ? rawData.get("价格") : rawData.get("price");
            if (priceObj != null) {
                asset.setPrice(new java.math.BigDecimal(priceObj.toString()));
            }
        }
        if (rawData.containsKey("图片URL") || rawData.containsKey("image")) {
            String image = rawData.containsKey("图片URL") ? 
                String.valueOf(rawData.get("图片URL")) : String.valueOf(rawData.get("image"));
            asset.setImage(image);
        }
        if (rawData.containsKey("备注") || rawData.containsKey("remark")) {
            String remark = rawData.containsKey("备注") ? 
                String.valueOf(rawData.get("备注")) : String.valueOf(rawData.get("remark"));
            asset.setRemark(remark);
        }
        if (rawData.containsKey("购买日期") || rawData.containsKey("purchaseDate")) {
            String dateStr = rawData.containsKey("购买日期") ? 
                String.valueOf(rawData.get("购买日期")) : String.valueOf(rawData.get("purchaseDate"));
            Date purchaseDate = parseDate(dateStr);
            if (purchaseDate != null) {
                asset.setPurchaseDate(purchaseDate);
            }
        }
        if (rawData.containsKey("状态") || rawData.containsKey("status")) {
            String status = rawData.containsKey("状态") ? 
                String.valueOf(rawData.get("状态")) : String.valueOf(rawData.get("status"));
            asset.setStatus(parseStatus(status));
        }

        return asset;
    }

    /**
     * 根据一级分类和二级分类名称查找分类ID
     */
    private String findCategoryId(String firstLevelName, String secondLevelName) {
        if (firstLevelName == null || firstLevelName.trim().isEmpty()) {
            return null;
        }

        if (secondLevelName != null && !secondLevelName.trim().isEmpty()) {
            // 查找二级分类
            String key = firstLevelName.trim() + "|" + secondLevelName.trim();
            AssetCategoryPO category = secondLevelCategoryMap.get(key);
            if (category != null) {
                return category.getId();
            }
        } else {
            // 查找一级分类
            AssetCategoryPO category = firstLevelCategoryMap.get(firstLevelName.trim());
            if (category != null) {
                return category.getId();
            }
        }

        return null;
    }

    /**
     * 解析日期字符串（支持yyyy-MM-dd格式）
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "null".equalsIgnoreCase(dateStr)) {
            return null;
        }

        try {
            // 尝试多种日期格式
            String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"};
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    return sdf.parse(dateStr.trim());
                } catch (ParseException e) {
                    // 继续尝试下一个格式
                }
            }
        } catch (Exception e) {
            log.warn("日期解析失败: " + dateStr, e);
        }

        return null;
    }

    /**
     * 解析状态字符串（支持中文名称）
     */
    private String parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        String statusTrim = status.trim();
        // 支持中文状态名称
        if ("正在服役".equals(statusTrim)) {
            return "IN_SERVICE";
        } else if ("已退役".equals(statusTrim)) {
            return "RETIRED";
        } else if ("已卖出".equals(statusTrim)) {
            return "SOLD";
        }

        // 支持英文状态名称
        if ("IN_SERVICE".equals(statusTrim) || "RETIRED".equals(statusTrim) || "SOLD".equals(statusTrim)) {
            return statusTrim;
        }

        return null;
    }

    /**
     * 公共方法：转换心愿单数据
     *
     * @param rawData 原始数据
     * @return 心愿单对象
     */
    protected WishlistPO convertWishlist(Map<String, Object> rawData) {
        WishlistPO wishlist = new WishlistPO();
        String userId = RequestUserContext.getUserId();
        wishlist.setUserId(userId);

        // ID自动生成
        wishlist.setId(idGeneratorApi.generateId());

        // 设置基本字段（按照导出格式：心愿名称,价格,链接,备注,是否已实现,实现时间）
        if (rawData.containsKey("心愿名称") || rawData.containsKey("name")) {
            String name = rawData.containsKey("心愿名称") ? 
                String.valueOf(rawData.get("心愿名称")) : String.valueOf(rawData.get("name"));
            wishlist.setName(name);
        }

        if (rawData.containsKey("价格") || rawData.containsKey("price")) {
            Object priceObj = rawData.containsKey("价格") ? rawData.get("价格") : rawData.get("price");
            if (priceObj != null) {
                wishlist.setPrice(new java.math.BigDecimal(priceObj.toString()));
            }
        }

        if (rawData.containsKey("链接") || rawData.containsKey("link")) {
            String link = rawData.containsKey("链接") ? 
                String.valueOf(rawData.get("链接")) : String.valueOf(rawData.get("link"));
            if (link != null && !"null".equalsIgnoreCase(link) && !link.trim().isEmpty()) {
                wishlist.setLink(link);
            }
        }

        if (rawData.containsKey("备注") || rawData.containsKey("remark")) {
            String remark = rawData.containsKey("备注") ? 
                String.valueOf(rawData.get("备注")) : String.valueOf(rawData.get("remark"));
            if (remark != null && !"null".equalsIgnoreCase(remark) && !remark.trim().isEmpty()) {
                wishlist.setRemark(remark);
            }
        }

        if (rawData.containsKey("是否已实现") || rawData.containsKey("achieved")) {
            Object achievedObj = rawData.containsKey("是否已实现") ? 
                rawData.get("是否已实现") : rawData.get("achieved");
            if (achievedObj != null) {
                String achievedStr = String.valueOf(achievedObj);
                if ("是".equals(achievedStr) || "true".equalsIgnoreCase(achievedStr) || "1".equals(achievedStr)) {
                    wishlist.setAchieved(true);
                } else if ("否".equals(achievedStr) || "false".equalsIgnoreCase(achievedStr) || "0".equals(achievedStr)) {
                    wishlist.setAchieved(false);
                }
            }
        }

        if (rawData.containsKey("实现时间") || rawData.containsKey("achievedAt")) {
            String dateStr = rawData.containsKey("实现时间") ? 
                String.valueOf(rawData.get("实现时间")) : String.valueOf(rawData.get("achievedAt"));
            Date achievedAt = parseDateTime(dateStr);
            if (achievedAt != null) {
                wishlist.setAchievedAt(achievedAt);
            }
        }

        // 设置创建时间和更新时间
        Date now = new Date();
        wishlist.setCreatedAt(now);
        wishlist.setUpdatedAt(now);

        return wishlist;
    }

    /**
     * 公共方法：校验资产数据
     *
     * @param asset 资产对象
     * @return 校验结果
     */
    protected ValidationResult validateAsset(AssetPO asset) {
        ValidationResult result = new ValidationResult();

        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            result.addError("资产名称不能为空");
        }
        if (asset.getCategoryId() == null || asset.getCategoryId().trim().isEmpty()) {
            result.addError("资产分类不存在，请先创建分类");
        } else {
            // 验证分类是否存在
            AssetCategoryPO category = categoryMapper.selectById(asset.getCategoryId());
            if (category == null) {
                result.addError("资产分类不存在，请先创建分类");
            }
        }
        if (asset.getPrice() == null || asset.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            result.addError("资产价格必须大于0");
        }
        if (asset.getPurchaseDate() == null) {
            result.addError("购买日期不能为空");
        }

        return result;
    }

    /**
     * 公共方法：校验心愿单数据
     *
     * @param wishlist 心愿单对象
     * @return 校验结果
     */
    protected ValidationResult validateWishlist(WishlistPO wishlist) {
        ValidationResult result = new ValidationResult();

        if (wishlist.getName() == null || wishlist.getName().trim().isEmpty()) {
            result.addError("心愿名称不能为空");
        } else if (wishlist.getName().length() > 100) {
            result.addError("心愿名称长度不能超过100个字符");
        }

        if (wishlist.getPrice() == null || wishlist.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            result.addError("心愿价格必须大于0");
        }

        if (wishlist.getLink() != null && wishlist.getLink().length() > 500) {
            result.addError("心愿链接长度不能超过500个字符");
        }

        if (wishlist.getRemark() != null && wishlist.getRemark().length() > 500) {
            result.addError("备注长度不能超过500个字符");
        }

        return result;
    }

    /**
     * 公共方法：保存资产数据
     *
     * @param assets     资产列表
     * @param incremental 是否增量导入
     * @param errors     错误列表
     * @return 导入结果
     */
    protected DataImportResultVO saveAssets(List<AssetPO> assets, boolean incremental, List<DataImportResultVO.ImportError> errors) {
        int successCount = 0;
        int failCount = errors.size();

        for (AssetPO asset : assets) {
            try {
                // ID已自动生成，直接插入
                assetMapper.insertAsset(asset);
                successCount++;
            } catch (Exception e) {
                failCount++;
                DataImportResultVO.ImportError error = new DataImportResultVO.ImportError();
                error.setRow(0);
                error.setMessage("保存失败: " + e.getMessage());
                errors.add(error);
            }
        }

        DataImportResultVO result = new DataImportResultVO();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setErrors(errors);
        return result;
    }

    /**
     * 公共方法：保存心愿单数据
     *
     * @param wishlists  心愿单列表
     * @param incremental 是否增量导入
     * @param errors     错误列表
     * @return 导入结果
     */
    protected DataImportResultVO saveWishlists(List<WishlistPO> wishlists, boolean incremental, List<DataImportResultVO.ImportError> errors) {
        int successCount = 0;
        int failCount = errors.size();

        for (WishlistPO wishlist : wishlists) {
            try {
                // ID已自动生成，直接插入
                wishlistMapper.insertWishlist(wishlist);
                successCount++;
            } catch (Exception e) {
                failCount++;
                DataImportResultVO.ImportError error = new DataImportResultVO.ImportError();
                error.setRow(0);
                error.setMessage("保存失败: " + e.getMessage());
                errors.add(error);
            }
        }

        DataImportResultVO result = new DataImportResultVO();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setErrors(errors);
        return result;
    }

    /**
     * 解析日期时间字符串（支持yyyy-MM-dd HH:mm:ss格式）
     */
    private Date parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || "null".equalsIgnoreCase(dateStr)) {
            return null;
        }

        try {
            // 尝试多种日期时间格式
            String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd"};
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    return sdf.parse(dateStr.trim());
                } catch (ParseException e) {
                    // 继续尝试下一个格式
                }
            }
        } catch (Exception e) {
            log.warn("日期时间解析失败: " + dateStr, e);
        }

        return null;
    }
}

