package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclRoleCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 角色幂等键校验器（仅用于创建操作）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class RoleIdempotentValidator extends AbstractValidator {

    private final StringRedisTemplate redisTemplate;

    /**
     * 幂等键缓存前缀
     */
    private static final String IDEMPOTENT_KEY_PREFIX = "acl:role:idempotent:";

    /**
     * 幂等键缓存过期时间（秒，默认24小时）
     */
    private static final long IDEMPOTENT_KEY_EXPIRE = 24 * 60 * 60;

    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            return result;
        }

        // 只对创建DTO进行幂等键校验
        if (!(dto instanceof AclRoleCreateDTO)) {
            return result;
        }

        AclRoleCreateDTO createDTO = (AclRoleCreateDTO) dto;
        String idempotentKey = createDTO.getIdempotentKey();

        if (idempotentKey == null || idempotentKey.trim().isEmpty()) {
            return result;
        }

        String cacheKey = IDEMPOTENT_KEY_PREFIX + idempotentKey;

        // 检查幂等键是否已存在
        Boolean exists = redisTemplate.hasKey(cacheKey);
        if (Boolean.TRUE.equals(exists)) {
            result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_5094));
            return result;
        }

        return result;
    }

    /**
     * 设置幂等键（由Service层调用）
     *
     * @param idempotentKey 幂等键
     */
    public void setIdempotentKey(String idempotentKey) {
        if (idempotentKey != null && !idempotentKey.trim().isEmpty()) {
            String cacheKey = IDEMPOTENT_KEY_PREFIX + idempotentKey;
            redisTemplate.opsForValue().set(cacheKey, "1", IDEMPOTENT_KEY_EXPIRE, TimeUnit.SECONDS);
        }
    }
}

