package com.pbad.cache;

import com.pbad.cache.domain.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author pbad
 * @date 2024年06月15日 10:07
 */
@Service
public class UserService {

    /**
     * 根据账号获取用户信息.
     *
     * @param account 账号
     * @return 用户
     */
    @Cacheable(value = "userCache")
    public User getUserByAccount(String account) {
        // 模拟数据库访问逻辑
        System.out.println("Fetching user from database for account: " + account);
        return findUserByAccount(account);
    }

    /**
     * 模拟数据库查询(年龄随机,校验缓存).
     *
     * @param account 账号
     * @return 用户
     */
    private User findUserByAccount(String account) {
        Random random = new Random();
        // 随机年龄在 0 到 99 之间
        int age = random.nextInt(100);
        return new User("张三", age, "zhangsan", "123456");
    }
}
