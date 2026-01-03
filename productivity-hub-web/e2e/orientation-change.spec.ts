/**
 * 横竖屏切换适配E2E测试
 * 
 * 注意：需要安装Playwright才能运行此测试
 */

import { test, expect } from '@playwright/test'

test.describe('横竖屏切换适配', () => {
  test('竖屏切换到横屏', async ({ page }) => {
    // 设置竖屏视口
    await page.setViewportSize({ width: 390, height: 844 })
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    
    // 记录竖屏时的布局信息
    const portraitWidth = await page.evaluate(() => {
      return document.body.clientWidth
    })
    
    // 切换到横屏
    await page.setViewportSize({ width: 844, height: 390 })
    
    // 等待布局调整（CSS过渡动画时间）
    await page.waitForTimeout(400)
    
    // 记录横屏时的布局信息
    const landscapeWidth = await page.evaluate(() => {
      return document.body.clientWidth
    })
    
    // 横屏时宽度应该增加
    expect(landscapeWidth).toBeGreaterThan(portraitWidth)
    
    // 检查无横向滚动
    const hasHorizontalScroll = await page.evaluate(() => {
      return document.documentElement.scrollWidth > document.documentElement.clientWidth
    })
    expect(hasHorizontalScroll).toBe(false)
  })
  
  test('横屏切换到竖屏', async ({ page }) => {
    // 设置横屏视口
    await page.setViewportSize({ width: 844, height: 390 })
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    
    // 记录横屏时的布局信息
    const landscapeWidth = await page.evaluate(() => {
      return document.body.clientWidth
    })
    
    // 切换到竖屏
    await page.setViewportSize({ width: 390, height: 844 })
    
    // 等待布局调整
    await page.waitForTimeout(400)
    
    // 记录竖屏时的布局信息
    const portraitWidth = await page.evaluate(() => {
      return document.body.clientWidth
    })
    
    // 竖屏时宽度应该减小
    expect(portraitWidth).toBeLessThan(landscapeWidth)
    
    // 检查无横向滚动
    const hasHorizontalScroll = await page.evaluate(() => {
      return document.documentElement.scrollWidth > document.documentElement.clientWidth
    })
    expect(hasHorizontalScroll).toBe(false)
  })
  
  test('快速横竖屏切换', async ({ page }) => {
    await page.goto('/')
    await page.waitForLoadState('networkidle')
    
    // 快速切换多次
    for (let i = 0; i < 5; i++) {
      await page.setViewportSize({ width: 390, height: 844 })
      await page.waitForTimeout(100)
      await page.setViewportSize({ width: 844, height: 390 })
      await page.waitForTimeout(100)
    }
    
    // 最终检查布局是否正常
    const hasHorizontalScroll = await page.evaluate(() => {
      return document.documentElement.scrollWidth > document.documentElement.clientWidth
    })
    expect(hasHorizontalScroll).toBe(false)
  })
})

