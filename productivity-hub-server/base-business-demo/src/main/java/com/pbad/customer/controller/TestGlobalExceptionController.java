package com.pbad.customer.controller;

import com.pbad.customer.exception.ExceptionMsgEnum;
import common.core.domain.Response;
import common.exception.BusinessException;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试全局异常controller接口.
 *
 * @author: pangdi
 * @date: 2023/8/29 10:46
 * @version: 1.0
 */
@Api(tags = "测试全局异常controller接口")
@RequestMapping(path = "/test/exception/")
@RestController
public class TestGlobalExceptionController {

    /**
     * 测试空指针异常.
     */
    @GetMapping("/test01")
    public Response<Void> test01() {
        String a = null;
        System.out.println(a + 1);
        return Response.ok();
    }

    /**
     * 测试自定义业务异常.
     */
    @GetMapping("/test02")
    public Response<Void> test02() {
        throw new BusinessException(
                ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorCode(),
                ExceptionMsgEnum.CUSTOMER_ID_IS_NULL.getErrorMessage());
    }

    /**
     * 测试类型转换异常.
     */
    @GetMapping("/test03")
    public Response<Void> test03() {
        Integer.parseInt("asdf");
        return Response.ok();
    }

}
