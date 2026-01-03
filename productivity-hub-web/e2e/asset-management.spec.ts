/**
 * 资产管理 E2E 测试
 * TASK-TEST-05: E2E测试编写
 */

import { test, expect } from '@playwright/test'

test.describe('资产管理流程', () => {
  test.beforeEach(async ({ page }) => {
    // 假设需要登录，这里可以根据实际情况调整
    // await page.goto('/login')
    // await page.fill('input[name="username"]', 'test_user')
    // await page.fill('input[name="password"]', 'test_password')
    // await page.click('button[type="submit"]')
    // await page.waitForURL('/')
  })

  test('创建资产流程', async ({ page }) => {
    // 1. 打开资产列表页
    await page.goto('/assets')
    await page.waitForLoadState('networkidle')

    // 2. 点击创建资产按钮
    await page.click('text=创建资产')

    // 3. 填写表单
    await page.fill('input[name="name"]', 'MacBook Pro')
    await page.fill('input[name="price"]', '12999.00')
    await page.selectOption('select[name="categoryId"]', 'cat_001')
    await page.fill('input[name="purchaseDate"]', '2025-01-15')

    // 4. 提交表单
    await page.click('button[type="submit"]')

    // 5. 验证创建成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    await expect(page.locator('text=MacBook Pro')).toBeVisible()
  })

  test('编辑资产流程', async ({ page }) => {
    // 1. 打开资产列表页
    await page.goto('/assets')
    await page.waitForLoadState('networkidle')

    // 2. 点击第一个资产的编辑按钮
    const firstAsset = page.locator('.asset-card').first()
    await firstAsset.locator('button:has-text("编辑")').click()

    // 3. 修改资产名称
    await page.fill('input[name="name"]', 'MacBook Pro Updated')

    // 4. 提交表单
    await page.click('button[type="submit"]')

    // 5. 验证更新成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    await expect(page.locator('text=MacBook Pro Updated')).toBeVisible()
  })

  test('删除资产流程', async ({ page }) => {
    // 1. 打开资产列表页
    await page.goto('/assets')
    await page.waitForLoadState('networkidle')

    // 2. 点击第一个资产的删除按钮
    const firstAsset = page.locator('.asset-card').first()
    const assetName = await firstAsset.locator('.asset-name').textContent()
    await firstAsset.locator('button:has-text("删除")').click()

    // 3. 确认删除
    await page.click('.el-message-box__btns button:has-text("确定")')

    // 4. 验证删除成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    if (assetName) {
      await expect(page.locator(`text=${assetName}`)).not.toBeVisible()
    }
  })

  test('查看资产详情流程', async ({ page }) => {
    // 1. 打开资产列表页
    await page.goto('/assets')
    await page.waitForLoadState('networkidle')

    // 2. 点击第一个资产的查看按钮
    const firstAsset = page.locator('.asset-card').first()
    await firstAsset.locator('button:has-text("查看")').click()

    // 3. 验证跳转到详情页
    await expect(page).toHaveURL(/\/assets\/\w+/, { timeout: 5000 })
    await expect(page.locator('.asset-detail')).toBeVisible()
  })
})

