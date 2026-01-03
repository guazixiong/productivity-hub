# E2E测试目录

本目录包含移动端适配功能的E2E测试用例。

## 安装依赖

```bash
npm install -D @playwright/test
npx playwright install
```

## 运行测试

```bash
# 运行所有测试
npx playwright test

# 运行特定测试文件
npx playwright test e2e/mobile-home.spec.ts

# 以UI模式运行
npx playwright test --ui
```

## 测试文件说明

- `mobile-home.spec.ts` - 移动端首页适配测试
- `orientation-change.spec.ts` - 横竖屏切换适配测试

## 注意事项

1. 运行测试前需要启动开发服务器（`npm run dev`）
2. 或者配置Playwright自动启动服务器（见 `playwright.config.ts`）
3. 测试用例基于实际页面结构，可能需要根据实际情况调整选择器

