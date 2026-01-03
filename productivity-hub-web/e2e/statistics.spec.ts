/**
 * 统计分析 E2E 测试
 * TASK-TEST-05: E2E测试编写
 */

import { test, expect } from '@playwright/test'

test.describe('统计分析流程', () => {
  test.beforeEach(async ({ page }) => {
    // 假设需要登录，这里可以根据实际情况调整
    // await page.goto('/login')
    // await page.fill('input[name="username"]', 'test_user')
    // await page.fill('input[name="password"]', 'test_password')
    // await page.click('button[type="submit"]')
    // await page.waitForURL('/')
  })

  test('查看资产统计总览', async ({ page }) => {
    // 1. 打开统计页面
    await page.goto('/assets/statistics')
    await page.waitForLoadState('networkidle')

    // 2. 验证统计卡片显示
    await expect(page.locator('.statistics-card')).toBeVisible()
    await expect(page.locator('text=总资产数')).toBeVisible()
    await expect(page.locator('text=总价值')).toBeVisible()
    await expect(page.locator('text=在用资产')).toBeVisible()
  })

  test('查看资产分布图表', async ({ page }) => {
    // 1. 打开统计页面
    await page.goto('/assets/statistics')
    await page.waitForLoadState('networkidle')

    // 2. 点击"资产分布"标签
    await page.click('text=资产分布')

    // 3. 验证图表显示
    await expect(page.locator('.chart-container')).toBeVisible({ timeout: 5000 })
  })

  test('查看分类统计图表', async ({ page }) => {
    // 1. 打开统计页面
    await page.goto('/assets/statistics')
    await page.waitForLoadState('networkidle')

    // 2. 点击"分类统计"标签
    await page.click('text=分类统计')

    // 3. 验证图表显示
    await expect(page.locator('.chart-container')).toBeVisible({ timeout: 5000 })
  })

  test('切换时间范围', async ({ page }) => {
    // 1. 打开统计页面
    await page.goto('/assets/statistics')
    await page.waitForLoadState('networkidle')

    // 2. 点击时间范围选择器
    await page.click('.time-range-selector')

    // 3. 选择"最近30天"
    await page.click('text=最近30天')

    // 4. 验证图表更新
    await expect(page.locator('.chart-container')).toBeVisible({ timeout: 5000 })
  })

  test('导出统计数据', async ({ page }) => {
    // 1. 打开统计页面
    await page.goto('/assets/statistics')
    await page.waitForLoadState('networkidle')

    // 2. 点击导出按钮
    const downloadPromise = page.waitForEvent('download')
    await page.click('button:has-text("导出")')

    // 3. 验证下载开始
    const download = await downloadPromise
    expect(download.suggestedFilename()).toContain('.xlsx')
  })
})

