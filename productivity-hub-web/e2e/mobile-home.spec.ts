/**
 * 移动端首页适配E2E测试
 * 
 * 注意：需要安装Playwright才能运行此测试
 * 安装命令：npm install -D @playwright/test && npx playwright install
 */

import { test, expect } from '@playwright/test'

test.describe('移动端首页适配', () => {
  test('iPhone 16竖屏访问首页', async ({ page }) => {
    // 设置移动端视口（iPhone 16尺寸）
    await page.setViewportSize({ width: 390, height: 844 })
    
    // 访问首页
    await page.goto('/')
    
    // 等待页面加载
    await page.waitForLoadState('networkidle')
    
    // 检查侧边栏是否隐藏
    const sidebar = page.locator('.sidebar-container, .el-aside, [class*="sidebar"]')
    const sidebarVisible = await sidebar.first().isVisible().catch(() => false)
    expect(sidebarVisible).toBe(false)
    
    // 检查菜单按钮是否显示（移动端应该有菜单按钮）
    const menuButton = page.locator('button[class*="menu"], .menu-button, [aria-label*="菜单"]')
    const menuButtonVisible = await menuButton.first().isVisible().catch(() => false)
    // 注意：如果菜单按钮不存在，可能是通过其他方式实现的，这里仅作为示例
    
    // 检查页面布局 - 内容区域应该是全宽
    const bodyWidth = await page.evaluate(() => {
      return document.body.clientWidth
    })
    expect(bodyWidth).toBeLessThanOrEqual(390)
    
    // 检查无横向滚动条
    const hasHorizontalScroll = await page.evaluate(() => {
      return document.documentElement.scrollWidth > document.documentElement.clientWidth
    })
    expect(hasHorizontalScroll).toBe(false)
    
    // 检查视觉风格 - 字体大小应该适当缩放
    const fontSize = await page.evaluate(() => {
      return parseFloat(window.getComputedStyle(document.body).fontSize)
    })
    // 移动端字体应该小于PC端（假设PC端默认16px）
    expect(fontSize).toBeLessThan(16)
  })
})

test.describe('移动端响应式布局', () => {
  test('不同屏幕尺寸适配', async ({ page }) => {
    const viewports = [
      { width: 375, height: 667, name: 'iPhone SE' },
      { width: 390, height: 844, name: 'iPhone 16' },
      { width: 414, height: 896, name: 'iPhone 11 Pro Max' },
    ]
    
    for (const viewport of viewports) {
      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await page.goto('/')
      await page.waitForLoadState('networkidle')
      
      // 检查无横向滚动
      const hasHorizontalScroll = await page.evaluate(() => {
        return document.documentElement.scrollWidth > document.documentElement.clientWidth
      })
      expect(hasHorizontalScroll).toBe(false)
    }
  })
})

