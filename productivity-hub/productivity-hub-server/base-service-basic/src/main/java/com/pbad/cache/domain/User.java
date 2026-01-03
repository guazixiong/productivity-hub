package com.pbad.cache.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 用户类
 * @author pbad
 * @date 2024年06月15日 10:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /**
     * 用户名
     */
    private String username;

    /**
     * 年龄
     */
    private int age;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age && Objects.equals(username, user.account) && Objects.equals(username, user.username) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, age, account, password);
    }
}
