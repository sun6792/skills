# Get Jobs — BOSS直聘反爬修复完全指南

> **一句话总结**：BOSS直聘从2025年起使用 [`disable-devtool`](https://github.com/theajack/disable-devtool) 库检测自动化浏览器。修复需要三步：①用系统真实Chrome + 持久化Profile、②注入反检测脚本hook `console.table`和`performance.now`、③不暴露CDP远程调试端口。

---

## 目录

1. [问题现象](#问题现象)
2. [BOSS检测机制揭秘](#boss检测机制揭秘)
3. [最终解决方案](#最终解决方案)
4. [小白操作步骤](#小白操作步骤)
5. [修改过的文件清单](#修改过的文件清单)
6. [常见问题](#常见问题)

---

## 问题现象

启动 `start.bat` 后，Playwright打开的Chrome浏览器中：
- BOSS直聘页面**首页可以正常加载**，但点击"登录"后**立刻被跳转到空白页或登录页**
- 或者登录成功后，开始投递时**页面不断刷新/闪退**
- 其他三个平台（猎聘、51job、智联）不受影响

---

## BOSS检测机制揭秘

根据 [GitHub Discussion #250](https://github.com/loks666/get_jobs/discussions/250) 和实际逆向分析，BOSS使用了以下检测手段：

### 1. `disable-devtool` 库（核心）

BOSS集成了开源反调试库 `disable-devtool`，主要通过以下方式检测：

```javascript
// BOSS的检测逻辑（简化版）
function checkDevTools() {
    let start = performance.now()
    console.table(largeObject)  // DevTools打开时执行很慢
    let end = performance.now()
    if (end - start > 50) {     // 超过50ms就认为DevTools开着
        window.location.href = 'about:blank'  // 跳转到空白页！
    }
}
```

### 2. CDP远程调试端口检测

BOSS能检测到Chrome是否以 `--remote-debugging-port` 参数启动。这是Playwright `connectOverCDP()` 方案的致命缺陷。

### 3. WebDriver属性检测

BOSS检查27个WebDriver注入属性，包括：
- `navigator.webdriver`
- `window.cdc_adoQpoasnfa76pfcZLmcfl_Array`（ChromeDriver标志变量）
- `__selenium_evaluate`、`__fxdriver_evaluate`等

### 4. 其他检测维度

| 检测点 | 方法 | BOSS上报码 |
|--------|------|-----------|
| WebDriver属性（27个） | 遍历 `navigator[x]` | `550009` |
| ChromeDriver标志 | `window.cdc_*` 变量 | `550003` |
| Linux + WebDriver组合 | UA + 属性集 base64 | `550005` |
| 鼠标轨迹 | 贝塞尔曲线拟合 | 企业端 |
| 键盘事件 | Key up/down + IME | 企业端 |
| 屏幕刷新率 | requestAnimationFrame 频率 | 企业端 |

---

## 最终解决方案

### 核心原理

```
┌──────────────────────────────────────────────────────────┐
│ 之前（失败）：Playwright自带Chromium → 秒被检测           │
│ 之前（失败）：CDP connectOverCDP → --remote-debugging-port│
│ 现在（成功）：launchPersistentContext + 系统Chrome        │
│              + 反检测脚本 + 持久化Profile                  │
└──────────────────────────────────────────────────────────┘
```

### 三个关键改动

**1. PlaywrightManager.java — 用 `launchPersistentContext` 代替 `launch`**

```java
// 错误做法（会被检测）
browser = playwright.chromium().launch(...)
context = browser.newContext(...)

// 正确做法（不会被检测）  
context = playwright.chromium().launchPersistentContext(
    profileDir,  // 持久化Profile目录
    new BrowserType.LaunchPersistentContextOptions()
        .setExecutablePath("C:/Program Files/Google/Chrome/Application/chrome.exe")
        .setLocale("zh-CN")
        .setTimezoneId("Asia/Shanghai")
);
```

**2. 反检测脚本 — hook `console.table` 和 `performance.now`**

```javascript
// 关键hook 1：覆盖console.table（BOSS用它检测DevTools）
console.table = function() {};
console.table.toString = function() { 
    return 'function table() { [native code] }'; 
};

// 关键hook 2：降低performance.now精度（BOSS检测微秒级时间差）
performance.now = function() {
    return Date.now() - performance.timeOrigin;
};
```

**3. 不暴露CDP调试端口**

Chrome启动参数中**绝对不能**包含 `--remote-debugging-port`。

---

## 小白操作步骤

### 第一步：确认你的环境

你需要：
- ✅ Windows 10/11 系统
- ✅ JDK 17 安装在 `C:\Program Files\Java\jdk-17`
- ✅ Google Chrome 浏览器已安装

### 第二步：关闭所有Chrome窗口

**重要！** 在启动之前，确保没有任何Chrome浏览器在运行。

### 第三步：运行启动脚本

双击项目目录下的 `start.bat`，等待约30-60秒：
- 会自动弹出一个Chrome浏览器窗口（这是程序控制的浏览器）
- 浏览器会自动打开4个标签页（BOSS直聘、猎聘、51job、智联招聘）
- 同时会自动打开管理页面 `http://localhost:6866`

### 第四步：登录各平台

**BOSS直聘**：
1. 切换到BOSS标签页
2. 点击"登录/注册" → 用微信/APP扫码登录
3. 登录成功后，登录态会自动保存在 `.chrome-profile` 文件夹
4. **下次启动不需要重新登录！**

**其他平台**（猎聘、51job、智联）同理。

### 第五步：配置并投递

1. 在管理页面 `http://localhost:6866` 中
2. 进入各平台配置页，设置关键词、城市、薪资等
3. 点击"保存配置"
4. 点击"开始投递"

### 第六步：停止服务

- 直接关闭命令行窗口，或运行 `stop.bat`

---

## 修改过的文件清单

| 文件 | 修改内容 |
|------|---------|
| `src/main/java/com/getjobs/worker/manager/PlaywrightManager.java` | **核心修复**：改用 `launchPersistentContext` + 系统Chrome + 反检测脚本 |
| `src/main/java/com/getjobs/worker/boss/Boss.java` | 新增 `checkBossRedirect()` 滑块/登录检测 + 卡片间随机延迟 |
| `src/main/java/com/getjobs/worker/liepin/Liepin.java` | SPA DOM刷新容错：导航/计数器重试 + JS直接操作DOM |
| `src/main/java/com/getjobs/worker/job51/Job51.java` | 复选框/批量按钮多级fallback + `collectJobIdsOnPage` 兜底 |
| `src/main/java/com/getjobs/worker/zhilian/ZhiLian.java` | 投递按钮fallback + 成功验证 + CAPTCHA检测优化 |
| `src/main/java/com/getjobs/application/config/StartupRunner.java` | Playwright初始化调用 |
| `start.bat` | 启动脚本优化 |
| `gradle.properties` | 新增，锁定JDK 17 |

---

## 常见问题

### Q: 启动后Chrome打开了但BOSS页面一片空白？
A: 等几秒让页面加载。如果持续空白，关闭Chrome和命令行，重新运行`start.bat`。

### Q: BOSS登录后又被踢出去了？
A: 确保是用项目自己打开的Chrome（不要用你自己平时用的Chrome）。`.chrome-profile`文件夹里的登录态是独立的。

### Q: 投递到一半停下了？
A: 可能是遇到滑块验证。在Chrome窗口里手动拖一下滑块，程序会自动检测并继续。

### Q: 想完全重置？
A: 删除项目目录下的 `.chrome-profile` 文件夹，重新运行 `start.bat`。

### Q: 投递成功率低？
A: 检查以下几点：
- 是否已登录目标平台
- 关键词是否合理（不要太宽泛）
- 查看 `target/logs/get-jobs.log` 日志了解具体错误

---

## 技术参考

- [GitHub Discussion #250 — BOSS防检测讨论](https://github.com/loks666/get_jobs/discussions/250)
- [disable-devtool 反调试库](https://github.com/theajack/disable-devtool)
- [AntiDebug_Breaker 插件](https://github.com/0xsdeo/AntiDebug_Breaker)
- [boss-helper 插件](https://github.com/Ocyss/boss-helper)
- [Auto-JobHunter 项目](https://github.com/jolie-z/Auto-JobHunter)

---

> **免责声明**：本指南仅供学习和研究使用。使用自动化工具投递简历请遵守各平台的使用条款，合理控制投递频率。
