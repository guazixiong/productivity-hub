package com.pbad.acl.constants;

/**
 * ACL权限控制错误码常量.
 * 错误码分类：
 * - ACL-4xxx: 参数校验错误
 * - ACL-5xxx: 业务冲突错误
 * - ACL-6xxx: 系统错误
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public class AclErrorCode {

    // ========== ACL-4xxx: 参数校验错误 ==========

    /**
     * 缺少必填参数
     */
    public static final String ACL_4001 = "ACL-4001";

    /**
     * 参数格式错误
     */
    public static final String ACL_4002 = "ACL-4002";

    /**
     * 菜单层级超限
     */
    public static final String ACL_4003 = "ACL-4003";

    /**
     * 菜单名称长度超限
     */
    public static final String ACL_4004 = "ACL-4004";

    /**
     * 角色名称长度超限
     */
    public static final String ACL_4005 = "ACL-4005";

    // ========== ACL-5xxx: 业务冲突错误 ==========

    /**
     * 角色名已存在
     */
    public static final String ACL_5091 = "ACL-5091";

    /**
     * 菜单循环引用
     */
    public static final String ACL_5092 = "ACL-5092";

    /**
     * 菜单/角色被占用，无法删除
     */
    public static final String ACL_5093 = "ACL-5093";

    /**
     * 幂等键重复
     */
    public static final String ACL_5094 = "ACL-5094";

    /**
     * 菜单不存在
     */
    public static final String ACL_5095 = "ACL-5095";

    /**
     * 角色不存在
     */
    public static final String ACL_5096 = "ACL-5096";

    /**
     * 内置角色不可删除
     */
    public static final String ACL_5097 = "ACL-5097";

    /**
     * 内置角色类型不可修改
     */
    public static final String ACL_5098 = "ACL-5098";

    // ========== ACL-6xxx: 系统错误 ==========

    /**
     * 数据库操作异常
     */
    public static final String ACL_6001 = "ACL-6001";

    /**
     * 系统内部错误
     */
    public static final String ACL_6002 = "ACL-6002";

    /**
     * 未授权访问
     */
    public static final String ACL_6003 = "ACL-6003";

    /**
     * 获取错误消息
     */
    public static String getErrorMessage(String errorCode) {
        switch (errorCode) {
            case ACL_4001:
                return "缺少必填参数";
            case ACL_4002:
                return "参数格式错误";
            case ACL_4003:
                return "菜单层级超限";
            case ACL_4004:
                return "菜单名称长度超限";
            case ACL_4005:
                return "角色名称长度超限";
            case ACL_5091:
                return "角色名已存在";
            case ACL_5092:
                return "菜单循环引用";
            case ACL_5093:
                return "菜单/角色被占用，无法删除";
            case ACL_5094:
                return "幂等键重复";
            case ACL_5095:
                return "菜单不存在";
            case ACL_5096:
                return "角色不存在";
            case ACL_5097:
                return "内置角色不可删除";
            case ACL_5098:
                return "内置角色类型不可修改";
            case ACL_6001:
                return "数据库操作异常";
            case ACL_6002:
                return "系统内部错误";
            case ACL_6003:
                return "未授权访问";
            default:
                return "未知错误";
        }
    }
}

