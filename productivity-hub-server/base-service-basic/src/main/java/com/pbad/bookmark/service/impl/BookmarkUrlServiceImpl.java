package com.pbad.bookmark.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbad.bookmark.domain.dto.BookmarkUrlBatchUpdateDTO;
import com.pbad.bookmark.domain.dto.BookmarkUrlCreateDTO;
import com.pbad.bookmark.domain.dto.BookmarkUrlUpdateDTO;
import com.pbad.bookmark.domain.po.BookmarkTagPO;
import com.pbad.bookmark.domain.po.BookmarkUrlPO;
import com.pbad.bookmark.domain.po.BookmarkUrlTagPO;
import com.pbad.bookmark.domain.vo.BookmarkGroupVO;
import com.pbad.bookmark.domain.vo.BookmarkSubGroupVO;
import com.pbad.bookmark.domain.vo.BookmarkTagVO;
import com.pbad.bookmark.domain.vo.BookmarkUrlVO;
import com.pbad.bookmark.mapper.BookmarkTagMapper;
import com.pbad.bookmark.mapper.BookmarkUrlMapper;
import com.pbad.bookmark.mapper.BookmarkUrlTagMapper;
import com.pbad.bookmark.service.BookmarkUrlService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 网址服务实现类.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkUrlServiceImpl implements BookmarkUrlService {

    private static final String BOOKMARK_URL_CACHE_KEY = "phub:bookmark:urls:all";
    private static final String BOOKMARK_GROUP_CACHE_KEY = "phub:bookmark:groups";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final BookmarkUrlMapper urlMapper;
    private final BookmarkTagMapper tagMapper;
    private final BookmarkUrlTagMapper urlTagMapper;
    private final IdGeneratorApi idGeneratorApi;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkGroupVO> getUrlGroups() {
        List<BookmarkGroupVO> cachedGroups = getCachedGroups();
        if (cachedGroups != null) {
            return cachedGroups;
        }

        // 缓存缺失时，立即回源构建并写入缓存
        List<BookmarkUrlVO> urlList = loadAllUrlVOs();
        List<BookmarkGroupVO> groups = buildUrlGroups(urlList);
        cacheBookmarkData(groups, urlList);
        return groups;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkUrlVO> getUrlsByTagId(String tagId) {
        List<BookmarkUrlVO> allUrls = getCachedUrlList();
        if (allUrls == null) {
            return new ArrayList<>();
        }
        return allUrls.stream()
                .filter(url -> url.getTags() != null &&
                        url.getTags().stream().anyMatch(tag -> tagId.equals(tag.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkUrlVO> searchUrls(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String normalized = keyword.trim().toLowerCase();
        List<BookmarkUrlVO> allUrls = getCachedUrlList();
        if (allUrls == null) {
            return new ArrayList<>();
        }
        return allUrls.stream()
                .filter(url -> {
                    String title = url.getTitle() != null ? url.getTitle().toLowerCase() : "";
                    String desc = url.getDescription() != null ? url.getDescription().toLowerCase() : "";
                    return title.contains(normalized) || desc.contains(normalized);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkUrlVO getUrlById(String id) {
        List<BookmarkUrlVO> allUrls = getCachedUrlList();
        if (allUrls != null) {
            for (BookmarkUrlVO url : allUrls) {
                if (id.equals(url.getId())) {
                    return url;
                }
            }
        }

        // 兜底直接查库，避免缓存异常导致的404
        BookmarkUrlPO url = urlMapper.selectById(id);
        if (url == null) {
            throw new BusinessException("404", "网址不存在");
        }
        return convertUrlToVO(url);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookmarkUrlVO createUrl(BookmarkUrlCreateDTO createDTO) {
        BookmarkUrlVO vo = doCreateUrl(createDTO);
        refreshBookmarkCache();
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookmarkUrlVO updateUrl(BookmarkUrlUpdateDTO updateDTO) {
        BookmarkUrlVO vo = doUpdateUrl(updateDTO);
        refreshBookmarkCache();
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUrl(String id) {
        doDeleteUrl(id);
        refreshBookmarkCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUrls(List<String> ids) {
        doBatchDeleteUrls(ids);
        refreshBookmarkCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateUrlTags(BookmarkUrlBatchUpdateDTO batchUpdateDTO) {
        doBatchUpdateUrlTags(batchUpdateDTO);
        refreshBookmarkCache();
    }

    @Override
    public void refreshBookmarkCache() {
        List<BookmarkUrlVO> urlList = loadAllUrlVOs();
        List<BookmarkGroupVO> groups = buildUrlGroups(urlList);
        cacheBookmarkData(groups, urlList);
    }

    private BookmarkUrlVO doCreateUrl(BookmarkUrlCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException("400", "参数不能为空");
        }
        if (createDTO.getTitle() == null || createDTO.getTitle().trim().isEmpty()) {
            throw new BusinessException("400", "网址标题不能为空");
        }
        if (createDTO.getUrl() == null || createDTO.getUrl().trim().isEmpty()) {
            throw new BusinessException("400", "网址URL不能为空");
        }
        if (createDTO.getTagIds() == null || createDTO.getTagIds().isEmpty()) {
            throw new BusinessException("400", "至少需要选择一个标签");
        }

        String url = createDTO.getUrl().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BusinessException("400", "URL格式不正确，必须以http://或https://开头");
        }

        for (String tagId : createDTO.getTagIds()) {
            BookmarkTagPO tag = tagMapper.selectById(tagId);
            if (tag == null) {
                throw new BusinessException("404", "标签不存在: " + tagId);
            }
        }

        BookmarkUrlPO newUrl = new BookmarkUrlPO();
        newUrl.setId(idGeneratorApi.generateId());
        newUrl.setTitle(createDTO.getTitle().trim());
        newUrl.setUrl(url);
        newUrl.setIconUrl(StringUtils.hasText(createDTO.getIconUrl()) ? createDTO.getIconUrl().trim() : null);
        newUrl.setDescription(StringUtils.hasText(createDTO.getDescription()) ? createDTO.getDescription().trim() : null);

        int insertCount = urlMapper.insertUrl(newUrl);
        if (insertCount <= 0) {
            throw new BusinessException("500", "创建网址失败");
        }

        createUrlTagRelations(newUrl.getId(), createDTO.getTagIds());

        BookmarkUrlPO insertedUrl = urlMapper.selectById(newUrl.getId());
        return convertUrlToVO(insertedUrl);
    }

    private BookmarkUrlVO doUpdateUrl(BookmarkUrlUpdateDTO updateDTO) {
        if (updateDTO == null || updateDTO.getId() == null || updateDTO.getId().trim().isEmpty()) {
            throw new BusinessException("400", "网址ID不能为空");
        }
        if (updateDTO.getTitle() == null || updateDTO.getTitle().trim().isEmpty()) {
            throw new BusinessException("400", "网址标题不能为空");
        }
        if (updateDTO.getUrl() == null || updateDTO.getUrl().trim().isEmpty()) {
            throw new BusinessException("400", "网址URL不能为空");
        }

        BookmarkUrlPO url = urlMapper.selectById(updateDTO.getId());
        if (url == null) {
            throw new BusinessException("404", "网址不存在");
        }

        String urlStr = updateDTO.getUrl().trim();
        if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
            throw new BusinessException("400", "URL格式不正确，必须以http://或https://开头");
        }

        url.setTitle(updateDTO.getTitle().trim());
        url.setUrl(urlStr);
        url.setIconUrl(StringUtils.hasText(updateDTO.getIconUrl()) ? updateDTO.getIconUrl().trim() : null);
        url.setDescription(StringUtils.hasText(updateDTO.getDescription()) ? updateDTO.getDescription().trim() : null);

        int updateCount = urlMapper.updateUrl(url);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新网址失败");
        }

        if (updateDTO.getTagIds() != null) {
            for (String tagId : updateDTO.getTagIds()) {
                BookmarkTagPO tag = tagMapper.selectById(tagId);
                if (tag == null) {
                    throw new BusinessException("404", "标签不存在: " + tagId);
                }
            }

            urlTagMapper.deleteByUrlId(updateDTO.getId());
            if (!updateDTO.getTagIds().isEmpty()) {
                createUrlTagRelations(updateDTO.getId(), updateDTO.getTagIds());
            }
        }

        BookmarkUrlPO updatedUrl = urlMapper.selectById(updateDTO.getId());
        return convertUrlToVO(updatedUrl);
    }

    private void doDeleteUrl(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException("400", "网址ID不能为空");
        }

        BookmarkUrlPO url = urlMapper.selectById(id);
        if (url == null) {
            throw new BusinessException("404", "网址不存在");
        }

        urlTagMapper.deleteByUrlId(id);

        int deleteCount = urlMapper.deleteUrl(id);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "删除网址失败");
        }
    }

    private void doBatchDeleteUrls(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("400", "网址ID列表不能为空");
        }

        for (String id : ids) {
            urlTagMapper.deleteByUrlId(id);
        }

        int deleteCount = urlMapper.batchDeleteUrls(ids);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "批量删除网址失败");
        }
    }

    private void doBatchUpdateUrlTags(BookmarkUrlBatchUpdateDTO batchUpdateDTO) {
        if (batchUpdateDTO == null || batchUpdateDTO.getUrlIds() == null || batchUpdateDTO.getUrlIds().isEmpty()) {
            throw new BusinessException("400", "网址ID列表不能为空");
        }

        if (batchUpdateDTO.getAddTagIds() != null && !batchUpdateDTO.getAddTagIds().isEmpty()) {
            for (String urlId : batchUpdateDTO.getUrlIds()) {
                for (String tagId : batchUpdateDTO.getAddTagIds()) {
                    List<String> existingTagIds = urlTagMapper.selectTagIdsByUrlId(urlId);
                    if (!existingTagIds.contains(tagId)) {
                        BookmarkTagPO tag = tagMapper.selectById(tagId);
                        if (tag == null) {
                            throw new BusinessException("404", "标签不存在: " + tagId);
                        }

                        BookmarkUrlTagPO urlTag = new BookmarkUrlTagPO();
                        urlTag.setId(idGeneratorApi.generateId());
                        urlTag.setUrlId(urlId);
                        urlTag.setTagId(tagId);
                        urlTagMapper.insertUrlTag(urlTag);
                    }
                }
            }
        }

        if (batchUpdateDTO.getRemoveTagIds() != null && !batchUpdateDTO.getRemoveTagIds().isEmpty()) {
            for (String urlId : batchUpdateDTO.getUrlIds()) {
                urlTagMapper.batchDeleteUrlTags(urlId, batchUpdateDTO.getRemoveTagIds());
            }
        }
    }

    private List<BookmarkUrlVO> getCachedUrlList() {
        List<BookmarkUrlVO> cached = getCachedUrlListInternal(true);
        if (cached != null) {
            return cached;
        }
        refreshBookmarkCache();
        return getCachedUrlListInternal(false);
    }

    @SuppressWarnings("unchecked")
    private List<BookmarkUrlVO> getCachedUrlListInternal(boolean allowRefresh) {
        try {
            Object cache = redisUtil.getValue(BOOKMARK_URL_CACHE_KEY);
            List<BookmarkUrlVO> converted = safeConvertCache(cache, new TypeReference<List<BookmarkUrlVO>>() {
            });
            if (converted != null) {
                return converted;
            }
        } catch (Exception ex) {
            log.warn("读取网址缓存失败，将尝试{}刷新", allowRefresh ? "重新" : "跳过", ex);
        }
        if (allowRefresh) {
            refreshBookmarkCache();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<BookmarkGroupVO> getCachedGroups() {
        try {
            Object cache = redisUtil.getValue(BOOKMARK_GROUP_CACHE_KEY);
            List<BookmarkGroupVO> converted = safeConvertCache(cache, new TypeReference<List<BookmarkGroupVO>>() {
            });
            if (converted != null) {
                return converted;
            }
        } catch (Exception ex) {
            log.warn("读取网址分组缓存失败，将回源构建", ex);
        }
        return null;
    }

    private void cacheBookmarkData(List<BookmarkGroupVO> groups, List<BookmarkUrlVO> urls) {
        redisUtil.defaultSetKeyNoExpiration(BOOKMARK_URL_CACHE_KEY, urls);
        redisUtil.defaultSetKeyNoExpiration(BOOKMARK_GROUP_CACHE_KEY, groups);
    }

    private List<BookmarkUrlVO> loadAllUrlVOs() {
        List<BookmarkUrlPO> allUrls = urlMapper.selectAll();
        return allUrls.stream().map(this::convertUrlToVO).collect(Collectors.toList());
    }

    private List<BookmarkGroupVO> buildUrlGroups(List<BookmarkUrlVO> allUrls) {
        List<BookmarkTagPO> parentTags = tagMapper.selectByLevel(1);
        List<BookmarkTagPO> childTags = tagMapper.selectByLevel(2);

        Map<String, List<BookmarkUrlVO>> urlsByTagId = new HashMap<>();
        for (BookmarkUrlVO url : allUrls) {
            if (url.getTags() == null) {
                continue;
            }
            for (BookmarkTagVO tag : url.getTags()) {
                urlsByTagId.computeIfAbsent(tag.getId(), k -> new ArrayList<>()).add(url);
            }
        }

        List<BookmarkGroupVO> result = new ArrayList<>();
        for (BookmarkTagPO parentTag : parentTags) {
            BookmarkGroupVO group = new BookmarkGroupVO();
            group.setParentTag(convertTagToVO(parentTag));

            List<BookmarkTagPO> subTags = childTags.stream()
                    .filter(tag -> parentTag.getId().equals(tag.getParentId()))
                    .collect(Collectors.toList());

            List<BookmarkSubGroupVO> subGroups = new ArrayList<>();
            Set<String> categorizedUrlIds = new java.util.HashSet<>();

            for (BookmarkTagPO subTag : subTags) {
                BookmarkSubGroupVO subGroup = new BookmarkSubGroupVO();
                subGroup.setTag(convertTagToVO(subTag));

                List<BookmarkUrlVO> subUrls = urlsByTagId.getOrDefault(subTag.getId(), new ArrayList<>());
                subGroup.setUrls(subUrls);
                subGroups.add(subGroup);

                subUrls.forEach(url -> categorizedUrlIds.add(url.getId()));
            }

            group.setSubGroups(subGroups);

            List<BookmarkUrlVO> uncategorizedUrls = allUrls.stream()
                    .filter(url -> urlsByTagId.getOrDefault(parentTag.getId(), new ArrayList<>()).contains(url)
                            && !categorizedUrlIds.contains(url.getId()))
                    .collect(Collectors.toList());

            group.setUncategorizedUrls(uncategorizedUrls);

            if (!subGroups.isEmpty() || !uncategorizedUrls.isEmpty()) {
                result.add(group);
            }
        }

        return result;
    }

    private void createUrlTagRelations(String urlId, List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        Set<String> distinctTagIds = tagIds.stream()
                .filter(tagId -> tagId != null && !tagId.trim().isEmpty())
                .collect(Collectors.toSet());

        if (distinctTagIds.isEmpty()) {
            return;
        }

        List<String> existingTagIds = urlTagMapper.selectTagIdsByUrlId(urlId);
        Set<String> existingTagIdSet = existingTagIds != null && !existingTagIds.isEmpty()
                ? existingTagIds.stream().collect(Collectors.toSet())
                : new java.util.HashSet<>();

        List<String> newTagIds = distinctTagIds.stream()
                .filter(tagId -> !existingTagIdSet.contains(tagId))
                .collect(Collectors.toList());

        if (newTagIds.isEmpty()) {
            return;
        }

        Set<String> processedCombinations = new java.util.HashSet<>();
        Set<String> generatedIds = new java.util.HashSet<>();
        List<BookmarkUrlTagPO> urlTags = new ArrayList<>();
        for (String tagId : newTagIds) {
            String combination = urlId + ":" + tagId;
            if (processedCombinations.contains(combination)) {
                continue;
            }
            processedCombinations.add(combination);

            String id;
            int retryCount = 0;
            do {
                id = idGeneratorApi.generateId();
                retryCount++;
                if (retryCount > 10) {
                    throw new BusinessException("500", "生成唯一ID失败，请重试");
                }
            } while (generatedIds.contains(id));
            generatedIds.add(id);

            BookmarkUrlTagPO urlTag = new BookmarkUrlTagPO();
            urlTag.setId(id);
            urlTag.setUrlId(urlId);
            urlTag.setTagId(tagId);
            urlTags.add(urlTag);
        }

        if (!urlTags.isEmpty()) {
            urlTagMapper.batchInsertUrlTags(urlTags);
        }
    }

    private <T> List<T> safeConvertCache(Object cache, TypeReference<List<T>> typeReference) {
        if (!(cache instanceof List<?>)) {
            return null;
        }
        List<?> list = (List<?>) cache;
        try {
            return OBJECT_MAPPER.convertValue(list, typeReference);
        } catch (IllegalArgumentException ex) {
            log.warn("反序列化缓存数据失败，忽略当前缓存", ex);
            return null;
        }
    }

    private BookmarkTagVO convertTagToVO(BookmarkTagPO po) {
        BookmarkTagVO vo = new BookmarkTagVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setParentId(po.getParentId());
        vo.setLevel(po.getLevel());
        vo.setSortOrder(po.getSortOrder());

        int urlCount = tagMapper.countUrlsByTagId(po.getId());
        vo.setUrlCount(urlCount);

        return vo;
    }

    private BookmarkUrlVO convertUrlToVO(BookmarkUrlPO po) {
        BookmarkUrlVO vo = new BookmarkUrlVO();
        vo.setId(po.getId());
        vo.setTitle(po.getTitle());
        vo.setUrl(po.getUrl());
        vo.setIconUrl(po.getIconUrl());
        vo.setDescription(po.getDescription());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());

        List<String> tagIds = urlTagMapper.selectTagIdsByUrlId(po.getId());
        List<BookmarkTagVO> tags = new ArrayList<>();
        for (String tagId : tagIds) {
            BookmarkTagPO tag = tagMapper.selectById(tagId);
            if (tag != null) {
                tags.add(convertTagToVO(tag));
            }
        }
        vo.setTags(tags);

        return vo;
    }
}

