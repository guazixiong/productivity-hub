package common.web.context;

import org.springframework.util.StringUtils;

/**
 * 请求线程内的登录用户上下文.
 */
public final class RequestUserContext {

    private static final ThreadLocal<RequestUser> USER_HOLDER = new ThreadLocal<>();

    private RequestUserContext() {
    }

    public static void set(RequestUser user) {
        USER_HOLDER.set(user);
    }

    public static RequestUser get() {
        return USER_HOLDER.get();
    }

    public static String getUserId() {
        RequestUser user = USER_HOLDER.get();
        return user != null && StringUtils.hasText(user.getUserId()) ? user.getUserId() : null;
    }

    public static String getUsername() {
        RequestUser user = USER_HOLDER.get();
        return user != null && StringUtils.hasText(user.getUsername()) ? user.getUsername() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}

