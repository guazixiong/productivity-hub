package com.pbad.basic.cache.controller;

import com.pbad.cache.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pbad
 * @date 2024年06月15日 10:15
 */
// Override bean name to avoid collision with auth module's UserController
@RestController("cacheUserController")
@RequestMapping("/cache")
public class CacheUserController {

    @Autowired
    private UserService userService;


    /**
     * 根据账号查询用户.
     *
     * @param account 账号
     * @return 用户
     */
    @GetMapping("/{account}")
    public Object findUserByAccount(@PathVariable String account) {
        return userService.getUserByAccount(account);
    }

}
