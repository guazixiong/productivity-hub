package common.web.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录用户上下文数据.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestUser {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;
}

