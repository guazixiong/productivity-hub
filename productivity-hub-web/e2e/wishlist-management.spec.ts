/**
 * 心愿单管理 E2E 测试
 * TASK-TEST-05: E2E测试编写
 */

import { test, expect } from '@playwright/test'

test.describe('心愿单管理流程', () => {
  test.beforeEach(async ({ page }) => {
    // 假设需要登录，这里可以根据实际情况调整
    // await page.goto('/login')
    // await page.fill('input[name="username"]', 'test_user')
    // await page.fill('input[name="password"]', 'test_password')
    // await page.click('button[type="submit"]')
    // await page.waitForURL('/')
  })

  test('创建心愿单流程', async ({ page }) => {
    // 1. 打开心愿单列表页
    await page.goto('/wishlist')
    await page.waitForLoadState('networkidle')

    // 2. 点击创建心愿单按钮
    await page.click('text=创建心愿单')

    // 3. 填写表单
    await page.fill('input[name="name"]', 'iPhone 15 Pro')
    await page.fill('input[name="targetPrice"]', '8999.00')
    await page.fill('textarea[name="description"]', '想要一部新手机')

    // 4. 提交表单
    await page.click('button[type="submit"]')

    // 5. 验证创建成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    await expect(page.locator('text=iPhone 15 Pro')).toBeVisible()
  })

  test('标记心愿单为已达成流程', async ({ page }) => {
    // 1. 打开心愿单列表页
    await page.goto('/wishlist')
    await page.waitForLoadState('networkidle')

    // 2. 找到第一个未达成的心愿单
    const firstWishlist = page.locator('.wishlist-item').first()
    await firstWishlist.locator('button:has-text("标记为已达成")').click()

    // 3. 确认操作
    await page.click('.el-message-box__btns button:has-text("确定")')

    // 4. 验证标记成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
  })

  test('编辑心愿单流程', async ({ page }) => {
    // 1. 打开心愿单列表页
    await page.goto('/wishlist')
    await page.waitForLoadState('networkidle')

    // 2. 点击第一个心愿单的编辑按钮
    const firstWishlist = page.locator('.wishlist-item').first()
    await firstWishlist.locator('button:has-text("编辑")').click()

    // 3. 修改目标价格
    await page.fill('input[name="targetPrice"]', '7999.00')

    // 4. 提交表单
    await page.click('button[type="submit"]')

    // 5. 验证更新成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
  })

  test('删除心愿单流程', async ({ page }) => {
    // 1. 打开心愿单列表页
    await page.goto('/wishlist')
    await page.waitForLoadState('networkidle')

    // 2. 点击第一个心愿单的删除按钮
    const firstWishlist = page.locator('.wishlist-item').first()
    const wishlistName = await firstWishlist.locator('.wishlist-name').textContent()
    await firstWishlist.locator('button:has-text("删除")').click()

    // 3. 确认删除
    await page.click('.el-message-box__btns button:has-text("确定")')

    // 4. 验证删除成功
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 5000 })
    if (wishlistName) {
      await expect(page.locator(`text=${wishlistName}`)).not.toBeVisible()
    }
  })

  test('筛选心愿单流程', async ({ page }) => {
    // 1. 打开心愿单列表页
    await page.goto('/wishlist')
    await page.waitForLoadState('networkidle')

    // 2. 点击"已达成"筛选
    await page.click('button:has-text("已达成")')

    // 3. 验证只显示已达成的心愿单
    const wishlistItems = page.locator('.wishlist-item')
    const count = await wishlistItems.count()
    for (let i = 0; i < count; i++) {
      const item = wishlistItems.nth(i)
      await expect(item.locator('.wishlist-status')).toContainText('已达成')
    }
  })
})

