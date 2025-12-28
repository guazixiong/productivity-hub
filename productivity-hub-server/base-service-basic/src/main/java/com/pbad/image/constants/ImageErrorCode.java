package com.pbad.image.constants;

/**
 * 图片管理错误码常量.
 * 关联需求：REQ-IMG-001~REQ-IMG-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public class ImageErrorCode {

    /**
     * 不支持的文件类型
     */
    public static final String UNSUPPORTED_FILE_TYPE = "4001";

    /**
     * 文件大小超过限制
     */
    public static final String FILE_SIZE_EXCEEDED = "4002";

    /**
     * 图片文件损坏或无法读取
     */
    public static final String IMAGE_CORRUPTED = "4003";

    /**
     * 参数不合法
     */
    public static final String INVALID_PARAMETER = "4004";

    /**
     * 图片已被删除，无法操作
     */
    public static final String IMAGE_DELETED = "4005";

    /**
     * 批量操作数量超过限制
     */
    public static final String BATCH_LIMIT_EXCEEDED = "4006";

    /**
     * 分享链接已撤销
     */
    public static final String SHARE_REVOKED = "4007";

    /**
     * 图片状态不是DELETED，无法恢复
     */
    public static final String INVALID_STATUS_FOR_RESTORE = "4008";

    /**
     * 无权限访问/操作该图片
     */
    public static final String NO_PERMISSION = "4031";

    /**
     * 图片不存在或已被删除
     */
    public static final String IMAGE_NOT_FOUND = "4041";

    /**
     * 分享链接不存在或已过期
     */
    public static final String SHARE_NOT_FOUND_OR_EXPIRED = "4042";

    /**
     * 存储空间不足
     */
    public static final String STORAGE_INSUFFICIENT = "5001";

    /**
     * 目录创建失败
     */
    public static final String DIRECTORY_CREATE_FAILED = "5002";

    /**
     * 图片处理失败
     */
    public static final String IMAGE_PROCESSING_FAILED = "5003";

    /**
     * 部分图片上传失败（部分成功场景）
     */
    public static final String PARTIAL_UPLOAD_FAILED = "2001";
}

