package com.pbad.asset.service.impl;

import com.pbad.asset.domain.dto.WishlistCreateDTO;
import com.pbad.asset.domain.dto.WishlistUpdateDTO;
import com.pbad.asset.domain.po.WishlistPO;
import com.pbad.asset.domain.vo.WishlistVO;
import com.pbad.asset.mapper.WishlistMapper;
import com.pbad.asset.service.WishlistService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 心愿单服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistMapper wishlistMapper;
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

    /**
     * 解析日期时间字符串
     */
    private Date parseDateTime(String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) {
            return null;
        }
        try {
            // 尝试多种日期时间格式
            String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss", "yyyy-MM-dd", "yyyy/MM/dd"};
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    return sdf.parse(dateTimeStr.trim());
                } catch (ParseException e) {
                    // 继续尝试下一个格式
                }
            }
            throw new BusinessException("400", "日期时间格式错误，应为 yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd");
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("400", "日期时间解析失败: " + dateTimeStr);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistVO> getWishlistList(Boolean achieved) {
        String userId = getCurrentUserId();
        List<WishlistPO> wishlists = wishlistMapper.selectAll(userId, achieved);
        return wishlists.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistVO getWishlistById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "心愿单ID不能为空");
        }
        String userId = getCurrentUserId();
        WishlistPO wishlist = wishlistMapper.selectById(id, userId);
        if (wishlist == null) {
            throw new BusinessException("404", "心愿单不存在");
        }
        return convertToVO(wishlist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WishlistVO createWishlist(WishlistCreateDTO createDTO) {
        // 参数校验
        if (createDTO == null || !StringUtils.hasText(createDTO.getName())) {
            throw new BusinessException("400", "心愿名称不能为空");
        }
        if (createDTO.getPrice() == null || createDTO.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("400", "心愿价格必须大于0");
        }

        String userId = getCurrentUserId();

        // 创建心愿单
        WishlistPO newWishlist = new WishlistPO();
        newWishlist.setId(idGeneratorApi.generateId());
        newWishlist.setUserId(userId);
        newWishlist.setName(createDTO.getName().trim());
        newWishlist.setPrice(createDTO.getPrice());
        newWishlist.setLink(createDTO.getLink());
        newWishlist.setRemark(createDTO.getRemark());
        newWishlist.setAchieved(false);
        Date now = new Date();
        newWishlist.setCreatedAt(now);
        newWishlist.setUpdatedAt(now);

        int insertCount = wishlistMapper.insertWishlist(newWishlist);
        if (insertCount <= 0) {
            throw new BusinessException("500", "创建心愿单失败");
        }

        WishlistPO insertedWishlist = wishlistMapper.selectById(newWishlist.getId(), userId);
        return convertToVO(insertedWishlist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WishlistVO updateWishlist(WishlistUpdateDTO updateDTO) {
        // 参数校验
        if (updateDTO == null || !StringUtils.hasText(updateDTO.getId())) {
            throw new BusinessException("400", "心愿单ID不能为空");
        }
        if (!StringUtils.hasText(updateDTO.getName())) {
            throw new BusinessException("400", "心愿名称不能为空");
        }
        if (updateDTO.getPrice() == null || updateDTO.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("400", "心愿价格必须大于0");
        }

        String userId = getCurrentUserId();

        // 查询心愿单
        WishlistPO wishlist = wishlistMapper.selectById(updateDTO.getId(), userId);
        if (wishlist == null) {
            throw new BusinessException("404", "心愿单不存在");
        }

        // 更新心愿单
        wishlist.setName(updateDTO.getName().trim());
        wishlist.setPrice(updateDTO.getPrice());
        if (updateDTO.getLink() != null) {
            wishlist.setLink(updateDTO.getLink());
        }
        if (updateDTO.getRemark() != null) {
            wishlist.setRemark(updateDTO.getRemark());
        }
        if (updateDTO.getAchieved() != null) {
            wishlist.setAchieved(updateDTO.getAchieved());
            // 如果 achieved 为 false，achievedAt 必须为 null
            if (!updateDTO.getAchieved()) {
                wishlist.setAchievedAt(null);
            } else {
                // 如果 achieved 为 true，优先使用前端传入的 achievedAt，否则使用当前时间
                if (StringUtils.hasText(updateDTO.getAchievedAt())) {
                    wishlist.setAchievedAt(parseDateTime(updateDTO.getAchievedAt()));
                } else {
                    wishlist.setAchievedAt(new Date());
                }
            }
        } else {
            // 如果没有传入 achieved，但传入了 achievedAt，直接使用前端传入的值
            if (StringUtils.hasText(updateDTO.getAchievedAt())) {
                wishlist.setAchievedAt(parseDateTime(updateDTO.getAchievedAt()));
            }
        }
        wishlist.setUpdatedAt(new Date());

        int updateCount = wishlistMapper.updateWishlist(wishlist);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新心愿单失败");
        }

        WishlistPO updatedWishlist = wishlistMapper.selectById(updateDTO.getId(), userId);
        return convertToVO(updatedWishlist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWishlist(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "心愿单ID不能为空");
        }

        String userId = getCurrentUserId();

        // 查询心愿单
        WishlistPO wishlist = wishlistMapper.selectById(id, userId);
        if (wishlist == null) {
            throw new BusinessException("404", "心愿单不存在");
        }

        // 删除心愿单
        int deleteCount = wishlistMapper.deleteWishlist(id, userId);
        if (deleteCount <= 0) {
            throw new BusinessException("500", "删除心愿单失败");
        }
    }

    /**
     * 转换为VO
     */
    private WishlistVO convertToVO(WishlistPO po) {
        WishlistVO vo = new WishlistVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setPrice(po.getPrice());
        vo.setLink(po.getLink());
        vo.setRemark(po.getRemark());
        vo.setAchieved(po.getAchieved());
        vo.setAchievedAt(po.getAchievedAt());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }
}

