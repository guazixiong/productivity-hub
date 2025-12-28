package com.pbad.bookmark.service.impl;

import com.pbad.bookmark.domain.dto.BookmarkTagCreateDTO;
import com.pbad.bookmark.domain.dto.BookmarkTagSortDTO;
import com.pbad.bookmark.domain.dto.BookmarkTagUpdateDTO;
import com.pbad.bookmark.domain.po.BookmarkTagPO;
import com.pbad.bookmark.domain.vo.BookmarkTagVO;
import com.pbad.bookmark.mapper.BookmarkTagMapper;
import com.pbad.bookmark.mapper.BookmarkUrlTagMapper;
import com.pbad.bookmark.service.BookmarkTagService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class BookmarkTagServiceImpl implements BookmarkTagService {

    private final BookmarkTagMapper tagMapper;
    private final BookmarkUrlTagMapper urlTagMapper;
    private final IdGeneratorApi idGeneratorApi;

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
    public List<BookmarkTagVO> getTagTree() {
        String userId = getCurrentUserId();
        List<BookmarkTagPO> allTags = tagMapper.selectAll(userId);
        
        // 按层级分组
        Map<Integer, List<BookmarkTagPO>> tagsByLevel = allTags.stream()
                .collect(Collectors.groupingBy(BookmarkTagPO::getLevel));
        
        // 获取一级标签
        List<BookmarkTagPO> parentTags = tagsByLevel.getOrDefault(1, new ArrayList<>());
        
        // 获取二级标签
        List<BookmarkTagPO> childTags = tagsByLevel.getOrDefault(2, new ArrayList<>());
        
        // 按父标签ID分组二级标签
        Map<String, List<BookmarkTagPO>> childTagsByParent = childTags.stream()
                .collect(Collectors.groupingBy(tag -> tag.getParentId() != null ? tag.getParentId() : ""));
        
        // 构建树形结构
        List<BookmarkTagVO> result = new ArrayList<>();
        for (BookmarkTagPO parentTag : parentTags) {
            BookmarkTagVO parentVO = convertToVO(parentTag);
            
            // 添加子标签
            List<BookmarkTagPO> children = childTagsByParent.getOrDefault(parentTag.getId(), new ArrayList<>());
            List<BookmarkTagVO> childVOs = children.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            parentVO.setChildren(childVOs);
            
            result.add(parentVO);
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkTagVO> getParentTags() {
        String userId = getCurrentUserId();
        List<BookmarkTagPO> parentTags = tagMapper.selectByLevel(1, userId);
        return parentTags.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkTagVO> getChildTags(String parentId) {
        String userId = getCurrentUserId();
        List<BookmarkTagPO> childTags = tagMapper.selectByParentId(parentId, userId);
        return childTags.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookmarkTagVO createTag(BookmarkTagCreateDTO createDTO) {
        // 参数校验
        if (createDTO == null || createDTO.getName() == null || createDTO.getName().trim().isEmpty()) {
            throw new BusinessException("400", "标签名称不能为空");
        }

        // 判断层级
        Integer level = createDTO.getParentId() == null || createDTO.getParentId().trim().isEmpty() ? 1 : 2;
        
        String userId = getCurrentUserId();

        // 如果是二级标签，验证父标签是否存在
        if (level == 2) {
            BookmarkTagPO parentTag = tagMapper.selectById(createDTO.getParentId(), userId);
            if (parentTag == null) {
                throw new BusinessException("404", "父标签不存在");
            }
            if (parentTag.getLevel() != 1) {
                throw new BusinessException("400", "父标签必须是一级标签");
            }
        }

        // 检查同名标签是否已存在
        BookmarkTagPO existingTag = tagMapper.selectByNameAndParentId(
                createDTO.getName().trim(), 
                createDTO.getParentId(),
                userId
        );
        if (existingTag != null) {
            throw new BusinessException("400", "该标签已存在");
        }

        // 创建标签
        BookmarkTagPO newTag = new BookmarkTagPO();
        newTag.setId(idGeneratorApi.generateId());
        newTag.setUserId(userId);
        newTag.setName(createDTO.getName().trim());
        newTag.setParentId(level == 1 ? null : createDTO.getParentId());
        newTag.setLevel(level);
        newTag.setSortOrder(createDTO.getSortOrder() != null ? createDTO.getSortOrder() : 0);

        int insertCount = tagMapper.insertTag(newTag);
        if (insertCount <= 0) {
            throw new BusinessException("500", "创建标签失败");
        }

        BookmarkTagPO insertedTag = tagMapper.selectById(newTag.getId(), userId);
        return convertToVO(insertedTag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookmarkTagVO updateTag(BookmarkTagUpdateDTO updateDTO) {
        // 参数校验
        if (updateDTO == null || updateDTO.getId() == null || updateDTO.getId().trim().isEmpty()) {
            throw new BusinessException("400", "标签ID不能为空");
        }
        if (updateDTO.getName() == null || updateDTO.getName().trim().isEmpty()) {
            throw new BusinessException("400", "标签名称不能为空");
        }

        String userId = getCurrentUserId();

        // 查询标签
        BookmarkTagPO tag = tagMapper.selectById(updateDTO.getId(), userId);
        if (tag == null) {
            throw new BusinessException("404", "标签不存在");
        }

        // 检查同名标签是否已存在（排除自己）
        BookmarkTagPO existingTag = tagMapper.selectByNameAndParentId(
                updateDTO.getName().trim(),
                tag.getParentId(),
                userId
        );
        if (existingTag != null && !existingTag.getId().equals(updateDTO.getId())) {
            throw new BusinessException("400", "该标签名称已存在");
        }

        // 更新标签
        tag.setName(updateDTO.getName().trim());
        if (updateDTO.getSortOrder() != null) {
            tag.setSortOrder(updateDTO.getSortOrder());
        }

        int updateCount = tagMapper.updateTag(tag);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新标签失败");
        }

        BookmarkTagPO updatedTag = tagMapper.selectById(updateDTO.getId(), userId);
        return convertToVO(updatedTag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(String id) {
        // 参数校验
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException("400", "标签ID不能为空");
        }

        String userId = getCurrentUserId();

        // 查询标签
        BookmarkTagPO tag = tagMapper.selectById(id, userId);
        if (tag == null) {
            throw new BusinessException("404", "标签不存在");
        }

        // 如果是一级标签，检查是否有子标签
        if (tag.getLevel() == 1) {
            List<BookmarkTagPO> childTags = tagMapper.selectByParentId(id, userId);
            if (!childTags.isEmpty()) {
                throw new BusinessException("400", "该标签下存在子标签，无法删除");
            }
        }

        // 检查是否有网址关联
        int urlCount = tagMapper.countUrlsByTagId(id, userId);
        if (urlCount > 0) {
            throw new BusinessException("400", "该标签下存在网址，无法删除");
        }

        // 删除标签
        int deleteCount = tagMapper.deleteTag(id, userId);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "删除标签失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTagSort(BookmarkTagSortDTO sortDTO) {
        // 参数校验
        if (sortDTO == null || sortDTO.getTagIds() == null || sortDTO.getTagIds().isEmpty()) {
            throw new BusinessException("400", "标签ID列表不能为空");
        }

        String userId = getCurrentUserId();

        // 构建更新列表
        List<BookmarkTagPO> tags = new ArrayList<>();
        for (int i = 0; i < sortDTO.getTagIds().size(); i++) {
            String tagId = sortDTO.getTagIds().get(i);
            BookmarkTagPO tag = new BookmarkTagPO();
            tag.setId(tagId);
            tag.setSortOrder(i);
            tags.add(tag);
        }

        // 批量更新排序
        int updateCount = tagMapper.batchUpdateSortOrder(tags, userId);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新排序失败");
        }
    }

    /**
     * 转换为VO
     */
    private BookmarkTagVO convertToVO(BookmarkTagPO po) {
        BookmarkTagVO vo = new BookmarkTagVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setParentId(po.getParentId());
        vo.setLevel(po.getLevel());
        vo.setSortOrder(po.getSortOrder());
        
        // 统计网址数量
        int urlCount;
        if (po.getLevel() == 1) {
            // 一级标签：统计该标签及其所有子标签的网址总数（去重）
            urlCount = tagMapper.countUrlsByParentTagId(po.getId(), po.getUserId());
        } else {
            // 二级标签：直接统计关联的网址数量
            urlCount = tagMapper.countUrlsByTagId(po.getId(), po.getUserId());
        }
        vo.setUrlCount(urlCount);
        
        return vo;
    }
}

