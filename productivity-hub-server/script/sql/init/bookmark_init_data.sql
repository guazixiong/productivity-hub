-- 宝藏类网址初始化数据

-- 初始化标签数据（一级标签）
INSERT INTO `bookmark_tag`(`id`, `name`, `parent_id`, `level`, `sort_order`, `created_at`, `updated_at`) VALUES
('tag_1', '开发工具', NULL, 1, 1, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_2', '设计资源', NULL, 1, 2, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_3', '学习资源', NULL, 1, 3, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_4', '效率工具', NULL, 1, 4, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_5', 'AI工具', NULL, 1, 5, '2025-01-01 10:00', '2025-01-01 10:00');

-- 初始化标签数据（二级标签）
INSERT INTO `bookmark_tag`(`id`, `name`, `parent_id`, `level`, `sort_order`, `created_at`, `updated_at`) VALUES
-- 开发工具下的二级标签
('tag_1_1', '代码托管', 'tag_1', 2, 1, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_1_2', '在线工具', 'tag_1', 2, 2, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_1_3', '文档资源', 'tag_1', 2, 3, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_1_4', 'API文档', 'tag_1', 2, 4, '2025-01-01 10:00', '2025-01-01 10:00'),
-- 设计资源下的二级标签
('tag_2_1', 'UI设计', 'tag_2', 2, 1, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_2_2', '图标素材', 'tag_2', 2, 2, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_2_3', '配色方案', 'tag_2', 2, 3, '2025-01-01 10:00', '2025-01-01 10:00'),
-- 学习资源下的二级标签
('tag_3_1', '技术博客', 'tag_3', 2, 1, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_3_2', '在线课程', 'tag_3', 2, 2, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_3_3', '技术社区', 'tag_3', 2, 3, '2025-01-01 10:00', '2025-01-01 10:00'),
-- 效率工具下的二级标签
('tag_4_1', '笔记工具', 'tag_4', 2, 1, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_4_2', '项目管理', 'tag_4', 2, 2, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_4_3', '时间管理', 'tag_4', 2, 3, '2025-01-01 10:00', '2025-01-01 10:00'),
-- AI工具下的二级标签
('tag_5_1', 'AI聊天', 'tag_5', 2, 1, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_5_2', 'AI编程', 'tag_5', 2, 2, '2025-01-01 10:00', '2025-01-01 10:00'),
('tag_5_3', 'AI设计', 'tag_5', 2, 3, '2025-01-01 10:00', '2025-01-01 10:00');

-- 初始化网址数据
INSERT INTO `bookmark_url`(`id`, `title`, `url`, `icon_url`, `description`, `created_at`, `updated_at`) VALUES
-- 代码托管相关
('url_1', 'GitHub', 'https://github.com', 'https://github.com/favicon.ico', '全球最大的代码托管平台，支持Git版本控制', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_2', 'GitLab', 'https://gitlab.com', 'https://gitlab.com/favicon.ico', '开源代码托管和CI/CD平台', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_3', 'Gitee', 'https://gitee.com', 'https://gitee.com/favicon.ico', '中国领先的代码托管平台', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 在线工具相关
('url_4', 'CodePen', 'https://codepen.io', 'https://codepen.io/favicon.ico', '在线前端代码编辑器和社区', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_5', 'JSFiddle', 'https://jsfiddle.net', 'https://jsfiddle.net/favicon.ico', '在线JavaScript代码测试工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_6', 'JSON Formatter', 'https://jsonformatter.org', 'https://jsonformatter.org/favicon.ico', 'JSON格式化、验证和美化工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_7', 'RegExr', 'https://regexr.com', 'https://regexr.com/favicon.ico', '正则表达式学习和测试工具', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 文档资源相关
('url_8', 'MDN Web Docs', 'https://developer.mozilla.org', 'https://developer.mozilla.org/favicon.ico', 'Mozilla开发者网络，权威的Web技术文档', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_9', 'Can I Use', 'https://caniuse.com', 'https://caniuse.com/favicon.ico', '查询Web技术在各个浏览器的兼容性', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_10', 'DevDocs', 'https://devdocs.io', 'https://devdocs.io/favicon.ico', '集成了多种编程语言和框架的离线文档', '2025-01-01 10:00', '2025-01-01 10:00'),
-- API文档相关
('url_11', 'Postman', 'https://www.postman.com', 'https://www.postman.com/favicon.ico', '强大的API开发和测试工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_12', 'Swagger', 'https://swagger.io', 'https://swagger.io/favicon.ico', 'API文档生成和测试工具', '2025-01-01 10:00', '2025-01-01 10:00'),
-- UI设计相关
('url_13', 'Figma', 'https://www.figma.com', 'https://www.figma.com/favicon.ico', '在线协作UI设计工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_14', 'Dribbble', 'https://dribbble.com', 'https://dribbble.com/favicon.ico', '设计师作品展示和灵感平台', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_15', 'Behance', 'https://www.behance.net', 'https://www.behance.net/favicon.ico', 'Adobe旗下的创意作品展示平台', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 图标素材相关
('url_16', 'Font Awesome', 'https://fontawesome.com', 'https://fontawesome.com/favicon.ico', '最流行的图标字体库', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_17', 'Iconify', 'https://iconify.design', 'https://iconify.design/favicon.ico', '统一的图标框架，包含100+图标集', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_18', 'Feather Icons', 'https://feathericons.com', 'https://feathericons.com/favicon.ico', '简洁美观的开源图标库', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 配色方案相关
('url_19', 'Coolors', 'https://coolors.co', 'https://coolors.co/favicon.ico', '配色方案生成器', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_20', 'Adobe Color', 'https://color.adobe.com', 'https://color.adobe.com/favicon.ico', 'Adobe官方配色工具', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 技术博客相关
('url_21', '阮一峰的网络日志', 'https://www.ruanyifeng.com/blog', 'https://www.ruanyifeng.com/favicon.ico', '知名技术博客，内容深入浅出', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_22', '掘金', 'https://juejin.cn', 'https://juejin.cn/favicon.ico', '中文技术社区，高质量技术文章', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_23', 'InfoQ', 'https://www.infoq.cn', 'https://www.infoq.cn/favicon.ico', '面向架构师、技术团队的技术媒体', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 在线课程相关
('url_24', 'Coursera', 'https://www.coursera.org', 'https://www.coursera.org/favicon.ico', '世界顶级大学的在线课程平台', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_25', 'Udemy', 'https://www.udemy.com', 'https://www.udemy.com/favicon.ico', '在线学习平台，涵盖各种技能课程', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_26', 'FreeCodeCamp', 'https://www.freecodecamp.org', 'https://www.freecodecamp.org/favicon.ico', '免费学习编程的在线平台', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 技术社区相关
('url_27', 'Stack Overflow', 'https://stackoverflow.com', 'https://stackoverflow.com/favicon.ico', '全球最大的程序员问答社区', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_28', 'V2EX', 'https://www.v2ex.com', 'https://www.v2ex.com/favicon.ico', '创意工作者的社区', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_29', 'SegmentFault', 'https://segmentfault.com', 'https://segmentfault.com/favicon.ico', '中文技术问答社区', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 笔记工具相关
('url_30', 'Notion', 'https://www.notion.so', 'https://www.notion.so/favicon.ico', '全能型笔记和协作工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_31', 'Obsidian', 'https://obsidian.md', 'https://obsidian.md/favicon.ico', '强大的知识管理和笔记工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_32', '语雀', 'https://www.yuque.com', 'https://www.yuque.com/favicon.ico', '阿里出品的知识库和文档工具', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 项目管理相关
('url_33', 'Trello', 'https://trello.com', 'https://trello.com/favicon.ico', '看板式项目管理工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_34', 'Jira', 'https://www.atlassian.com/software/jira', 'https://www.atlassian.com/favicon.ico', '专业的项目管理和问题跟踪工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_35', 'Asana', 'https://asana.com', 'https://asana.com/favicon.ico', '团队协作和项目管理平台', '2025-01-01 10:00', '2025-01-01 10:00'),
-- 时间管理相关
('url_36', '番茄工作法', 'https://pomofocus.io', 'https://pomofocus.io/favicon.ico', '在线番茄钟时间管理工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_37', 'Toggl', 'https://toggl.com', 'https://toggl.com/favicon.ico', '时间跟踪和工作效率分析工具', '2025-01-01 10:00', '2025-01-01 10:00'),
-- AI聊天相关
('url_38', 'ChatGPT', 'https://chat.openai.com', 'https://chat.openai.com/favicon.ico', 'OpenAI的AI聊天助手', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_39', 'Claude', 'https://claude.ai', 'https://claude.ai/favicon.ico', 'Anthropic的AI助手', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_40', '文心一言', 'https://yiyan.baidu.com', 'https://yiyan.baidu.com/favicon.ico', '百度AI对话助手', '2025-01-01 10:00', '2025-01-01 10:00'),
-- AI编程相关
('url_41', 'GitHub Copilot', 'https://github.com/features/copilot', 'https://github.com/favicon.ico', 'GitHub的AI编程助手', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_42', 'Cursor', 'https://cursor.sh', 'https://cursor.sh/favicon.ico', 'AI驱动的代码编辑器', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_43', 'Codeium', 'https://codeium.com', 'https://codeium.com/favicon.ico', '免费的AI代码补全工具', '2025-01-01 10:00', '2025-01-01 10:00'),
-- AI设计相关
('url_44', 'Midjourney', 'https://www.midjourney.com', 'https://www.midjourney.com/favicon.ico', 'AI图像生成工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_45', 'DALL-E', 'https://openai.com/dall-e-2', 'https://openai.com/favicon.ico', 'OpenAI的AI图像生成工具', '2025-01-01 10:00', '2025-01-01 10:00'),
('url_46', 'Remove.bg', 'https://www.remove.bg', 'https://www.remove.bg/favicon.ico', 'AI背景移除工具', '2025-01-01 10:00', '2025-01-01 10:00');

-- 初始化标签网址关联数据
INSERT INTO `bookmark_url_tag`(`id`, `url_id`, `tag_id`, `created_at`) VALUES
-- 代码托管
('rel_1', 'url_1', 'tag_1_1', '2025-01-01 10:00'),
('rel_2', 'url_2', 'tag_1_1', '2025-01-01 10:00'),
('rel_3', 'url_3', 'tag_1_1', '2025-01-01 10:00'),
-- 在线工具
('rel_4', 'url_4', 'tag_1_2', '2025-01-01 10:00'),
('rel_5', 'url_5', 'tag_1_2', '2025-01-01 10:00'),
('rel_6', 'url_6', 'tag_1_2', '2025-01-01 10:00'),
('rel_7', 'url_7', 'tag_1_2', '2025-01-01 10:00'),
-- 文档资源
('rel_8', 'url_8', 'tag_1_3', '2025-01-01 10:00'),
('rel_9', 'url_9', 'tag_1_3', '2025-01-01 10:00'),
('rel_10', 'url_10', 'tag_1_3', '2025-01-01 10:00'),
-- API文档
('rel_11', 'url_11', 'tag_1_4', '2025-01-01 10:00'),
('rel_12', 'url_12', 'tag_1_4', '2025-01-01 10:00'),
-- UI设计
('rel_13', 'url_13', 'tag_2_1', '2025-01-01 10:00'),
('rel_14', 'url_14', 'tag_2_1', '2025-01-01 10:00'),
('rel_15', 'url_15', 'tag_2_1', '2025-01-01 10:00'),
-- 图标素材
('rel_16', 'url_16', 'tag_2_2', '2025-01-01 10:00'),
('rel_17', 'url_17', 'tag_2_2', '2025-01-01 10:00'),
('rel_18', 'url_18', 'tag_2_2', '2025-01-01 10:00'),
-- 配色方案
('rel_19', 'url_19', 'tag_2_3', '2025-01-01 10:00'),
('rel_20', 'url_20', 'tag_2_3', '2025-01-01 10:00'),
-- 技术博客
('rel_21', 'url_21', 'tag_3_1', '2025-01-01 10:00'),
('rel_22', 'url_22', 'tag_3_1', '2025-01-01 10:00'),
('rel_23', 'url_23', 'tag_3_1', '2025-01-01 10:00'),
-- 在线课程
('rel_24', 'url_24', 'tag_3_2', '2025-01-01 10:00'),
('rel_25', 'url_25', 'tag_3_2', '2025-01-01 10:00'),
('rel_26', 'url_26', 'tag_3_2', '2025-01-01 10:00'),
-- 技术社区
('rel_27', 'url_27', 'tag_3_3', '2025-01-01 10:00'),
('rel_28', 'url_28', 'tag_3_3', '2025-01-01 10:00'),
('rel_29', 'url_29', 'tag_3_3', '2025-01-01 10:00'),
-- 笔记工具
('rel_30', 'url_30', 'tag_4_1', '2025-01-01 10:00'),
('rel_31', 'url_31', 'tag_4_1', '2025-01-01 10:00'),
('rel_32', 'url_32', 'tag_4_1', '2025-01-01 10:00'),
-- 项目管理
('rel_33', 'url_33', 'tag_4_2', '2025-01-01 10:00'),
('rel_34', 'url_34', 'tag_4_2', '2025-01-01 10:00'),
('rel_35', 'url_35', 'tag_4_2', '2025-01-01 10:00'),
-- 时间管理
('rel_36', 'url_36', 'tag_4_3', '2025-01-01 10:00'),
('rel_37', 'url_37', 'tag_4_3', '2025-01-01 10:00'),
-- AI聊天
('rel_38', 'url_38', 'tag_5_1', '2025-01-01 10:00'),
('rel_39', 'url_39', 'tag_5_1', '2025-01-01 10:00'),
('rel_40', 'url_40', 'tag_5_1', '2025-01-01 10:00'),
-- AI编程
('rel_41', 'url_41', 'tag_5_2', '2025-01-01 10:00'),
('rel_42', 'url_42', 'tag_5_2', '2025-01-01 10:00'),
('rel_43', 'url_43', 'tag_5_2', '2025-01-01 10:00'),
-- AI设计
('rel_44', 'url_44', 'tag_5_3', '2025-01-01 10:00'),
('rel_45', 'url_45', 'tag_5_3', '2025-01-01 10:00'),
('rel_46', 'url_46', 'tag_5_3', '2025-01-01 10:00');

