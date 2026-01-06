# 测试说明文档

## 测试框架配置

### 前端测试框架

- **Vitest**: 单元测试框架
- **Vue Test Utils**: Vue 组件测试工具
- **Playwright**: E2E 测试框架

### 后端测试框架

- **JUnit 5**: 单元测试框架
- **Mockito**: Mock 框架
- **Spring Boot Test**: 集成测试框架

## 运行测试

### 前端测试

```bash
# 运行所有测试
npm run test

# 运行测试并查看 UI
npm run test:ui

# 运行测试并生成覆盖率报告
npm run test:coverage

# 运行 E2E 测试
npm run test:e2e

# 运行 E2E 测试并查看 UI
npm run test:e2e:ui
```

### 后端测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=AssetServiceImplTest

# 运行测试并生成覆盖率报告
mvn test jacoco:report
```

## 测试覆盖范围

### 前端测试

#### 单元测试
- ✅ 组件测试：AssetCard
- ✅ 工具函数测试：compatibility, format, caloriesCalculator, bmiCalculator
- ✅ Store 测试：responsive, auth, config
- ⏳ 其他组件测试：待补充

#### E2E 测试
- ✅ 资产管理流程测试：创建、编辑、删除、查看
- ✅ 心愿单管理流程测试：创建、编辑、删除、筛选
- ✅ 统计分析流程测试：查看统计、图表、导出
- ✅ 性能测试：页面加载、首屏渲染、交互响应

### 后端测试

#### 单元测试
- ✅ Service 层测试：
  - AssetServiceImpl
  - AssetCategoryServiceImpl
  - CurrencyServiceImpl
  - AssetStatisticsServiceImpl
- ✅ 工具类测试：ImageUtils, FileStorageUtils等
- ✅ 设计模式实现测试：ValidatorChain, 装饰器模式等

#### 集成测试
- ✅ Controller 层测试：
  - CurrencyController
  - DataManagementController
  - AssetController

#### 性能测试
- ✅ 并发性能测试：AssetServicePerformanceTest
- ✅ 响应时间测试

## 测试用例示例

### 前端组件测试

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AssetCard from '../AssetCard.vue'

describe('AssetCard', () => {
  it('应该正确渲染资产名称', () => {
    const wrapper = mount(AssetCard, {
      props: { asset: mockAsset }
    })
    expect(wrapper.text()).toContain('MacBook Pro')
  })
})
```

### 后端 Service 测试

```java
@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {
    @Mock
    private AssetMapper assetMapper;
    
    @InjectMocks
    private AssetServiceImpl assetService;
    
    @Test
    void testCreateAsset_Success() {
        // Given
        AssetCreateDTO dto = new AssetCreateDTO();
        // When
        AssetDetailVO result = assetService.createAsset(dto);
        // Then
        assertNotNull(result);
    }
}
```

### E2E 测试

```typescript
test('创建资产流程', async ({ page }) => {
  await page.goto('/assets')
  await page.click('text=创建资产')
  await page.fill('input[name="name"]', 'MacBook Pro')
  await page.click('button[type="submit"]')
  await expect(page.locator('.el-message--success')).toBeVisible()
})
```

## 测试覆盖率目标

- **单元测试覆盖率**: > 80%
- **分支覆盖率**: > 70%
- **行覆盖率**: > 80%

## 注意事项

1. **测试环境**: 测试应在独立的环境中运行，避免影响开发环境
2. **测试数据**: 使用测试数据生成工具，确保测试数据隔离
3. **测试清理**: 每次测试后清理测试数据，避免测试之间的相互影响
4. **Mock 数据**: 使用 Mock 数据模拟外部依赖，提高测试速度和稳定性

## 已完成工作

- [x] 补充前端 Store 测试（auth, config, responsive）
- [x] 补充后端 Service 单元测试（AssetCategory, Currency, AssetStatistics）
- [x] 补充后端 Controller 集成测试（Asset, Currency, DataManagement）
- [x] 补充 E2E 测试（心愿单、统计、性能）
- [x] 实现性能测试（前后端）

## 待完成工作

- [ ] 补充前端其他组件测试
- [ ] 补充数据库操作集成测试
- [ ] 补充更多设计模式实现测试
- [ ] 提高测试覆盖率到目标值（>80%）

