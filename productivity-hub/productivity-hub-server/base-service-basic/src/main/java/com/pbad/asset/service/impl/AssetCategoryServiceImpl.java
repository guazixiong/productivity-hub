package com.pbad.asset.service.impl;

import com.pbad.asset.domain.dto.AssetCategoryCreateDTO;
import com.pbad.asset.domain.dto.AssetCategoryUpdateDTO;
import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.domain.vo.AssetCategoryVO;
import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.service.AssetCategoryService;
import com.pbad.auth.util.UserRoleUtil;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import common.util.RedisUtil;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资产分类服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCategoryServiceImpl implements AssetCategoryService {

    private static final String CACHE_KEY = "asset:category:list";

    private final AssetCategoryMapper categoryMapper;
    private final IdGeneratorApi idGeneratorApi;
    private final UserRoleUtil userRoleUtil;
    private final RedisUtil redisUtil;

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "用户未登录");
        }
        return userId;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetCategoryVO> getAllCategories() {
        List<AssetCategoryPO> allCategories = null;
        
        // 先尝试从缓存获取
        try {
            Object cachedData = redisUtil.getValue(CACHE_KEY);
            if (cachedData != null) {
                // 缓存存在，使用缓存数据
                @SuppressWarnings("unchecked")
                List<AssetCategoryPO> cachedCategories = (List<AssetCategoryPO>) cachedData;
                if (cachedCategories != null && !cachedCategories.isEmpty()) {
                    allCategories = cachedCategories;
                    log.debug("从Redis缓存获取资产分类数据，共{}条", allCategories.size());
                }
            }
        } catch (Exception e) {
            log.warn("从Redis缓存获取资产分类数据失败，将查询数据库", e);
        }
        
        // 缓存不存在或获取失败，查询数据库并更新缓存
        if (allCategories == null) {
            allCategories = categoryMapper.selectAll();
            if (allCategories != null && !allCategories.isEmpty()) {
                // 更新缓存
                try {
                    redisUtil.defaultSetKeyNoExpiration(CACHE_KEY, allCategories);
                    log.debug("资产分类数据已更新到Redis缓存，共{}条", allCategories.size());
                } catch (Exception e) {
                    log.warn("更新资产分类数据到Redis缓存失败", e);
                }
            } else {
                // 如果数据库查询结果为空，初始化为空列表
                allCategories = new ArrayList<>();
            }
        }
        
        // 如果分类列表为空，直接返回空列表
        if (allCategories.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 批量查询所有分类的资产数量（优化性能，避免N+1查询）
        Map<String, Integer> assetCountMap = new HashMap<>();
        try {
            List<Map<String, Object>> countResults = categoryMapper.countAssetsByCategoryIds();
            if (countResults != null && !countResults.isEmpty()) {
                for (Map<String, Object> result : countResults) {
                    Object categoryIdObj = result.get("categoryId");
                    Object countObj = result.get("assetCount");
                    
                    // 处理 categoryId：可能是 String 或其他类型
                    String categoryId = null;
                    if (categoryIdObj != null) {
                        categoryId = String.valueOf(categoryIdObj).trim();
                        if (categoryId.isEmpty() || "null".equalsIgnoreCase(categoryId)) {
                            categoryId = null;
                        }
                    }
                    
                    // 处理资产数量：确保正确转换为 int
                    int count = 0;
                    if (countObj != null) {
                        if (countObj instanceof Number) {
                            count = ((Number) countObj).intValue();
                        } else {
                            try {
                                count = Integer.parseInt(String.valueOf(countObj));
                            } catch (NumberFormatException e) {
                                log.warn("资产数量格式错误，categoryId={}, countObj={}", categoryId, countObj);
                                count = 0;
                            }
                        }
                    }
                    
                    // 只添加有效的分类ID和资产数量
                    if (categoryId != null && !categoryId.isEmpty()) {
                        assetCountMap.put(categoryId, count);
                        log.debug("分类资产数量统计：categoryId={}, count={}", categoryId, count);
                    }
                }
                log.debug("批量查询资产数量完成，共统计{}个分类", assetCountMap.size());
            } else {
                log.debug("批量查询资产数量结果为空，所有分类资产数量为0");
            }
        } catch (Exception e) {
            log.error("批量查询资产数量失败，将使用默认值0", e);
        }
        
        // 分离大分类和小分类
        List<AssetCategoryPO> parentCategories = allCategories.stream()
                .filter(cat -> cat.getParentId() == null || cat.getParentId().isEmpty())
                .collect(Collectors.toList());
        
        // 按父分类ID分组小分类
        Map<String, List<AssetCategoryPO>> childrenMap = allCategories.stream()
                .filter(cat -> cat.getParentId() != null && !cat.getParentId().isEmpty())
                .collect(Collectors.groupingBy(AssetCategoryPO::getParentId));
        
        // 构建树形结构
        List<AssetCategoryVO> result = new ArrayList<>();
        for (AssetCategoryPO parent : parentCategories) {
            AssetCategoryVO parentVO = convertToVO(parent, assetCountMap);
            
            // 添加子分类
            List<AssetCategoryPO> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
            List<AssetCategoryVO> childrenVO = children.stream()
                    .map(child -> convertToVO(child, assetCountMap))
                    .collect(Collectors.toList());
            parentVO.setChildren(childrenVO);
            
            // 对于一级分类，需要累加它本身及其所有子分类的资产数量
            int parentAssetCount = assetCountMap.getOrDefault(parent.getId(), 0);
            int childrenAssetCount = childrenVO.stream()
                    .mapToInt(AssetCategoryVO::getAssetCount)
                    .sum();
            int totalAssetCount = parentAssetCount + childrenAssetCount;
            parentVO.setAssetCount(totalAssetCount);
            
            result.add(parentVO);
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AssetCategoryVO getCategoryById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "分类ID不能为空");
        }
        AssetCategoryPO category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("404", "分类不存在");
        }
        // 单个查询时，仍然使用单独查询资产数量
        Map<String, Integer> assetCountMap = new HashMap<>();
        int assetCount = categoryMapper.countAssetsByCategoryId(category.getId());
        assetCountMap.put(category.getId(), assetCount);
        return convertToVO(category, assetCountMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetCategoryVO createCategory(AssetCategoryCreateDTO createDTO) {
        // 参数校验
        if (createDTO == null || !StringUtils.hasText(createDTO.getName())) {
            throw new BusinessException("400", "分类名称不能为空");
        }

        String parentId = createDTO.getParentId();
        Integer level;
        
        // 判断是大分类还是小分类
        if (StringUtils.hasText(parentId)) {
            // 小分类：验证父分类是否存在
            AssetCategoryPO parentCategory = categoryMapper.selectById(parentId);
            if (parentCategory == null) {
                throw new BusinessException("400", "父分类不存在");
            }
            if (parentCategory.getLevel() != null && parentCategory.getLevel() >= 2) {
                throw new BusinessException("400", "只能创建二级分类，不能创建三级及以上分类");
            }
            level = 2;
        } else {
            // 大分类
            level = 1;
        }

        // 检查同名分类是否已存在（同一父分类下不能重名）
        AssetCategoryPO existingCategory = categoryMapper.selectByNameAndParentId(
                createDTO.getName().trim(), parentId);
        if (existingCategory != null) {
            throw new BusinessException("400", "该分类名称已存在");
        }

        // 创建分类
        AssetCategoryPO newCategory = new AssetCategoryPO();
        newCategory.setId(idGeneratorApi.generateId());
        newCategory.setName(createDTO.getName().trim());
        newCategory.setIcon(createDTO.getIcon());
        newCategory.setParentId(parentId);
        newCategory.setLevel(level);
        newCategory.setIsDefault(false);
        newCategory.setSortOrder(0);
        Date now = new Date();
        newCategory.setCreatedAt(now);
        newCategory.setUpdatedAt(now);

        int insertCount = categoryMapper.insertCategory(newCategory);
        if (insertCount <= 0) {
            throw new BusinessException("500", "创建分类失败");
        }

        AssetCategoryPO insertedCategory = categoryMapper.selectById(newCategory.getId());
        
        // 更新缓存：重新加载所有分类数据
        refreshCache();
        
        // 单个查询时，仍然使用单独查询资产数量
        Map<String, Integer> assetCountMap = new HashMap<>();
        int assetCount = categoryMapper.countAssetsByCategoryId(insertedCategory.getId());
        assetCountMap.put(insertedCategory.getId(), assetCount);
        return convertToVO(insertedCategory, assetCountMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetCategoryVO updateCategory(AssetCategoryUpdateDTO updateDTO) {
        // 参数校验
        if (updateDTO == null || !StringUtils.hasText(updateDTO.getId())) {
            throw new BusinessException("400", "分类ID不能为空");
        }
        if (!StringUtils.hasText(updateDTO.getName())) {
            throw new BusinessException("400", "分类名称不能为空");
        }

        // 查询分类
        AssetCategoryPO category = categoryMapper.selectById(updateDTO.getId());
        if (category == null) {
            throw new BusinessException("404", "分类不存在");
        }

        // 检查是否为默认分类，非管理员不能修改默认分类
        if (Boolean.TRUE.equals(category.getIsDefault())) {
            String userId = getCurrentUserId();
            boolean isAdmin = userRoleUtil.isAdmin(userId);
            log.debug("尝试修改默认分类，userId={}, isAdmin={}", userId, isAdmin);
            if (!isAdmin) {
                log.warn("非管理员用户尝试修改默认分类，userId={}", userId);
            throw new BusinessException("400", "默认分类不能修改");
        }
        }

        // 检查同名分类是否已存在（排除自己，同一父分类下不能重名）
        String newParentId = updateDTO.getParentId();
        AssetCategoryPO existingCategory = categoryMapper.selectByNameAndParentId(
                updateDTO.getName().trim(), newParentId);
        if (existingCategory != null && !existingCategory.getId().equals(updateDTO.getId())) {
            throw new BusinessException("400", "该分类名称已存在");
        }

        // 如果修改了父分类，需要验证
        if (StringUtils.hasText(newParentId)) {
            // 不能将自己设置为自己的父分类
            if (newParentId.equals(updateDTO.getId())) {
                throw new BusinessException("400", "不能将自己设置为自己的父分类");
            }
            // 验证父分类是否存在
            AssetCategoryPO parentCategory = categoryMapper.selectById(newParentId);
            if (parentCategory == null) {
                throw new BusinessException("400", "父分类不存在");
            }
            if (parentCategory.getLevel() != null && parentCategory.getLevel() >= 2) {
                throw new BusinessException("400", "只能创建二级分类，不能创建三级及以上分类");
            }
            category.setParentId(newParentId);
            category.setLevel(2);
        } else {
            // 设置为大分类
            category.setParentId(null);
            category.setLevel(1);
        }

        // 更新分类
        category.setName(updateDTO.getName().trim());
        if (updateDTO.getIcon() != null) {
            category.setIcon(updateDTO.getIcon());
        }
        category.setUpdatedAt(new Date());

        int updateCount = categoryMapper.updateCategory(category);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新分类失败");
        }

        AssetCategoryPO updatedCategory = categoryMapper.selectById(updateDTO.getId());
        
        // 更新缓存：重新加载所有分类数据
        refreshCache();
        
        // 单个查询时，仍然使用单独查询资产数量
        Map<String, Integer> assetCountMap = new HashMap<>();
        int assetCount = categoryMapper.countAssetsByCategoryId(updatedCategory.getId());
        assetCountMap.put(updatedCategory.getId(), assetCount);
        return convertToVO(updatedCategory, assetCountMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(String id) {
        // 参数校验
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "分类ID不能为空");
        }

        // 查询分类
        AssetCategoryPO category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("404", "分类不存在");
        }

        // 获取当前用户ID
        String userId = getCurrentUserId();

        // 检查是否为默认分类，非管理员不能删除默认分类
        if (Boolean.TRUE.equals(category.getIsDefault())) {
            if (!userRoleUtil.isAdmin(userId)) {
            throw new BusinessException("400", "默认分类不能删除");
            }
        }

        // 检查是否有资产关联（包括子分类的资产）
        int totalAssetCount = categoryMapper.countTotalAssetsByCategoryId(id);
        if (totalAssetCount > 0) {
            // 如果是大分类，检查是否有子分类（用于提示信息）
            boolean hasChildren = false;
            if (category.getLevel() != null && category.getLevel() == 1) {
                List<AssetCategoryPO> children = categoryMapper.selectByParentId(id);
                hasChildren = children != null && !children.isEmpty();
            }
            
            if (hasChildren) {
                throw new BusinessException("400", 
                    String.format("该分类及其子分类下共存在 %d 个资产，无法删除。请先删除或转移这些资产后再删除分类。", totalAssetCount));
            } else {
                throw new BusinessException("400", 
                    String.format("该分类下存在 %d 个资产，无法删除。请先删除或转移这些资产后再删除分类。", totalAssetCount));
            }
        }

        // 如果是大分类，检查是否有子分类（在确认没有资产后）
        if (category.getLevel() != null && category.getLevel() == 1) {
            List<AssetCategoryPO> children = categoryMapper.selectByParentId(id);
            if (children != null && !children.isEmpty()) {
                throw new BusinessException("400", "该分类下存在子分类，无法删除。请先删除所有子分类后再删除该分类。");
            }
        }

        // 删除分类
        int deleteCount = categoryMapper.deleteCategory(id);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "删除分类失败");
        }
        
        // 更新缓存：重新加载所有分类数据
        refreshCache();
    }

    /**
     * 转换为VO（使用批量查询的资产数量Map）
     */
    private AssetCategoryVO convertToVO(AssetCategoryPO po, Map<String, Integer> assetCountMap) {
        AssetCategoryVO vo = new AssetCategoryVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setIcon(po.getIcon());
        vo.setParentId(po.getParentId());
        vo.setLevel(po.getLevel());
        vo.setIsDefault(po.getIsDefault());
        vo.setSortOrder(po.getSortOrder());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());

        // 从批量查询结果中获取资产数量，如果没有则默认为0
        int assetCount = assetCountMap.getOrDefault(po.getId(), 0);
        vo.setAssetCount(assetCount);
        
        // 调试日志：如果资产数量不为0，记录日志
        if (assetCount > 0) {
            log.debug("分类资产数量：categoryId={}, name={}, level={}, assetCount={}", 
                po.getId(), po.getName(), po.getLevel(), assetCount);
        }

        return vo;
    }

    /**
     * 刷新缓存：重新从数据库加载所有分类数据并更新缓存
     */
    private void refreshCache() {
        try {
            List<AssetCategoryPO> allCategories = categoryMapper.selectAll();
            if (allCategories != null) {
                redisUtil.defaultSetKeyNoExpiration(CACHE_KEY, allCategories);
                log.debug("资产分类缓存已刷新，共{}条", allCategories.size());
            } else {
                // 如果数据库中没有数据，删除缓存
                redisUtil.delete(CACHE_KEY);
                log.debug("资产分类缓存已删除（数据库无数据）");
            }
        } catch (Exception e) {
            log.warn("刷新资产分类缓存失败", e);
            // 不抛出异常，避免影响主业务流程
        }
    }
}

