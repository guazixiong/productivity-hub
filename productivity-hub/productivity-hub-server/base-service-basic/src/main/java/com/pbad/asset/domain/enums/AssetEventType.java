package com.pbad.asset.domain.enums;

/**
 * 资产事件类型枚举.
 *
 * 对应设计文档中的资产事件类型定义：
 * - CREATED：资产创建
 * - UPDATED：资产更新
 * - DELETED：资产删除
 * - RETIRED：资产退役
 * - SOLD：资产卖出
 */
public enum AssetEventType {

    /** 资产创建 */
    CREATED,

    /** 资产更新 */
    UPDATED,

    /** 资产删除 */
    DELETED,

    /** 资产退役 */
    RETIRED,

    /** 资产卖出 */
    SOLD
}


