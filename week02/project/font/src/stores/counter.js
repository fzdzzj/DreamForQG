// 导入 Vue3 的响应式 API：ref 定义基础响应式数据，computed 计算属性
import { ref, computed } from 'vue'
// 导入 Pinia 的定义仓库方法，用于创建状态仓库
import { defineStore } from 'pinia'

/**
 * 创建 Pinia 状态仓库：counter
 * 这是一个计数器仓库，用于管理数字的状态与方法
 */
export const useCounterStore = defineStore('counter', () => {
  // 定义响应式变量：计数器数值，初始值为 0
  const count = ref(0)

  // 定义计算属性：计数器的双倍值
  // 具有缓存特性，依赖 count 变化时才会重新计算
  const doubleCount = computed(() => count.value * 2)

  // 定义修改状态的方法：让计数器 +1
  function increment() {
    count.value++
  }

  // 将需要在组件中使用的 响应式数据、计算属性、方法 暴露出去
  return { count, doubleCount, increment }
})