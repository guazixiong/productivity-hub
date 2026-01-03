<template>
  <div ref="containerRef" class="virtual-list" :style="containerStyle" @scroll="handleScroll">
    <div :style="spacerStyle"></div>
    <div :style="contentStyle">
      <div
        v-for="item in visibleItems"
        :key="getItemKey(item, item.index)"
        :style="getItemStyle(item.index)"
      >
        <slot :item="item.data" :index="item.index"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 虚拟滚动列表组件
 * 用于优化长列表渲染性能
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'

interface Props<T> {
  /** 数据列表 */
  items: T[]
  /** 每项高度（像素） */
  itemHeight: number
  /** 容器高度（像素） */
  containerHeight?: number
  /** 缓冲区大小（可见区域上下各渲染多少项） */
  bufferSize?: number
  /** 获取唯一键的函数 */
  getItemKey?: (item: T, index: number) => string | number
}

const props = withDefaults(
  defineProps<Props<any>>(),
  {
    containerHeight: 400,
    bufferSize: 5,
    getItemKey: (_, index) => index,
  }
)

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

const containerStyle = computed(() => ({
  height: `${props.containerHeight}px`,
  overflow: 'auto',
}))

const totalHeight = computed(() => props.items.length * props.itemHeight)

const spacerStyle = computed(() => ({
  height: `${totalHeight.value}px`,
  position: 'relative',
}))

const contentStyle = computed(() => ({
  position: 'absolute',
  top: 0,
  left: 0,
  right: 0,
}))

const visibleStartIndex = computed(() => {
  const start = Math.floor(scrollTop.value / props.itemHeight)
  return Math.max(0, start - props.bufferSize)
})

const visibleEndIndex = computed(() => {
  const visibleCount = Math.ceil(props.containerHeight / props.itemHeight)
  const end = visibleStartIndex.value + visibleCount + props.bufferSize * 2
  return Math.min(props.items.length, end)
})

const visibleItems = computed(() => {
  return props.items.slice(visibleStartIndex.value, visibleEndIndex.value).map((item, idx) => ({
    data: item,
    index: visibleStartIndex.value + idx,
  }))
})

const getItemStyle = (index: number) => ({
  height: `${props.itemHeight}px`,
  position: 'absolute',
  top: `${index * props.itemHeight}px`,
  left: 0,
  right: 0,
})

const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  scrollTop.value = target.scrollTop
}

onMounted(() => {
  if (containerRef.value) {
    containerRef.value.addEventListener('scroll', handleScroll)
  }
})

onUnmounted(() => {
  if (containerRef.value) {
    containerRef.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped lang="scss">
.virtual-list {
  position: relative;
  width: 100%;
}
</style>

