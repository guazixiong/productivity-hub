package com.pbad.httpclientTemplate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 测试用户类.
 *
 * @author: pangdi
 * @date: 2023/12/27 18:01
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TestUser {

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private int age;
}
