/**
 * 标签视图对象
 */
export interface BookmarkTag {
  id: string
  name: string
  parentId?: string
  level: number
  sortOrder: number
  urlCount: number
  children?: BookmarkTag[]
}

/**
 * 网址视图对象
 */
export interface BookmarkUrl {
  id: string
  title: string
  url: string
  iconUrl?: string
  description?: string
  tags?: BookmarkTag[]
  createdAt: string
  updatedAt: string
}

/**
 * 网址子分组（二级标签分组）
 */
export interface BookmarkSubGroup {
  tag: BookmarkTag
  urls: BookmarkUrl[]
}

/**
 * 网址分组（一级标签分组）
 */
export interface BookmarkGroup {
  parentTag: BookmarkTag
  subGroups: BookmarkSubGroup[]
  uncategorizedUrls: BookmarkUrl[]
}

/**
 * 标签创建DTO
 */
export interface BookmarkTagCreateDTO {
  name: string
  parentId?: string
  sortOrder?: number
}

/**
 * 标签更新DTO
 */
export interface BookmarkTagUpdateDTO {
  id: string
  name: string
  sortOrder?: number
}

/**
 * 标签排序DTO
 */
export interface BookmarkTagSortDTO {
  tagIds: string[]
}

/**
 * 网址创建DTO
 */
export interface BookmarkUrlCreateDTO {
  title: string
  url: string
  iconUrl?: string
  description?: string
  tagIds: string[]
}

/**
 * 网址更新DTO
 */
export interface BookmarkUrlUpdateDTO {
  id: string
  title: string
  url: string
  iconUrl?: string
  description?: string
  tagIds: string[]
}

/**
 * 网址批量更新DTO
 */
export interface BookmarkUrlBatchUpdateDTO {
  urlIds: string[]
  addTagIds?: string[]
  removeTagIds?: string[]
}

/**
 * Excel导入结果
 */
export interface BookmarkImportResult {
  total: number
  success: number
  failed: number
  skipped: number
  errors: string[]
}

/**
 * Excel 导入预览单行
 */
export interface BookmarkImportPreviewItem {
  parentTagName?: string
  childTagName?: string
  title: string
  url: string
  description?: string
  iconUrl?: string
}

/**
 * Excel 导入预览结果
 */
export interface BookmarkImportPreview {
  total: number
  items: BookmarkImportPreviewItem[]
}

