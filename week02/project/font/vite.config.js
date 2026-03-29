// 从 Vite 中导入 defineConfig 方法
// 作用：用于定义 Vite 配置对象，提供智能提示和类型校验
import { defineConfig } from 'vite'

// 导入 Vite 官方提供的 Vue 插件
// 作用：让 Vite 支持编译和解析 .vue 单文件组件
import vue from '@vitejs/plugin-vue'

// 导入 Node.js 内置的路径处理模块 path
// 作用：用于处理文件的绝对路径、相对路径，实现路径别名的配置
import path from 'path'

// 导出 Vite 配置
// defineConfig 会包裹配置，让编辑器获得更好的类型提示
export default defineConfig({

  // 插件配置
  // 使用 vue() 插件，使项目支持 Vue3 语法和组件编译
  plugins: [vue()],

  // 开发服务器配置
  server: {
    // 前端项目启动时使用的端口号
    // 访问地址：http://localhost:3000
    port: 3000,

    // 项目启动完成后，自动在浏览器中打开页面
    open: true,

    // 代理配置（解决开发环境跨域问题）
    proxy: {
      // 当请求路径以 /api 开头时，触发代理转发
      '/api': {
        // 目标服务器地址（后端接口地址）
        target: 'http://localhost:8080',

        // 开启跨域，允许改变源（必须开启，否则跨域失败）
        changeOrigin: true,

        // 是否使用 https，开发环境关闭证书验证
        secure: false
      }
    }
  },

  // 路径解析配置
  resolve: {
    // 路径别名配置
    alias: {
      // 将符号 @ 映射为项目根目录下的 src 文件夹
      // 之后可以使用 @/xxx 代替 src/xxx
      '@': path.resolve(__dirname, './src')
    }
  },

  // CSS 配置
  css: {
    // CSS 预处理器配置
    preprocessorOptions: {
      // 针对 SCSS 预处理器的配置
      scss: {
        // 全局自动引入指定的 SCSS/CSS 文件
        // 所有组件都会自动引入这个全局样式文件
        additionalData: '@use "@/assets/styles/global.css" as *;'
      }
    }
  }
})