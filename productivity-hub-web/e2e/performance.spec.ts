/**
 * 前端性能测试
 * TASK-TEST-06: 性能测试执行
 */

import { test, expect } from '@playwright/test'

test.describe('前端性能测试', () => {
  test('页面加载性能测试', async ({ page }) => {
    // 测量页面加载时间
    const startTime = Date.now()
    
    await page.goto('/assets')
    await page.waitForLoadState('networkidle')
    
    const loadTime = Date.now() - startTime
    
    // 验证页面加载时间小于3秒
    expect(loadTime).toBeLessThan(3000)
    console.log(`页面加载时间: ${loadTime}ms`)
  })

  test('首屏渲染性能测试', async ({ page }) => {
    await page.goto('/assets')
    
    // 等待首屏内容渲染
    await page.waitForSelector('.asset-list', { timeout: 5000 })
    
    // 测量首屏渲染时间
    const renderTime = await page.evaluate(() => {
      const perfData = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
      return perfData.domContentLoadedEventEnd - perfData.fetchStart
    })
    
    // 验证首屏渲染时间小于2秒
    expect(renderTime).toBeLessThan(2000)
    console.log(`首屏渲染时间: ${renderTime}ms`)
  })

  test('交互响应性能测试', async ({ page }) => {
    await page.goto('/assets')
    await page.waitForLoadState('networkidle')
    
    // 测量点击按钮的响应时间
    const startTime = Date.now()
    await page.click('text=创建资产')
    await page.waitForSelector('input[name="name"]', { timeout: 2000 })
    const responseTime = Date.now() - startTime
    
    // 验证交互响应时间小于500ms
    expect(responseTime).toBeLessThan(500)
    console.log(`交互响应时间: ${responseTime}ms`)
  })

  test('列表滚动性能测试', async ({ page }) => {
    await page.goto('/assets')
    await page.waitForLoadState('networkidle')
    
    // 测量滚动性能
    const startTime = Date.now()
    await page.evaluate(() => {
      window.scrollTo(0, document.body.scrollHeight)
    })
    await page.waitForTimeout(100) // 等待滚动完成
    const scrollTime = Date.now() - startTime
    
    // 验证滚动流畅（时间应该很短）
    expect(scrollTime).toBeLessThan(200)
    console.log(`滚动响应时间: ${scrollTime}ms`)
  })

  test('API请求性能测试', async ({ page }) => {
    await page.goto('/assets')
    
    // 监听网络请求
    const responsePromise = page.waitForResponse(
      response => response.url().includes('/api/asset/assets') && response.status() === 200
    )
    
    const startTime = Date.now()
    await responsePromise
    const apiTime = Date.now() - startTime
    
    // 验证API响应时间小于1秒
    expect(apiTime).toBeLessThan(1000)
    console.log(`API响应时间: ${apiTime}ms`)
  })
})

