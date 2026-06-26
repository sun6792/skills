# 代码修改详情

## 1. PlaywrightManager.java — 核心反爬修复

### 改动：用 `launchPersistentContext` 代替 `launch`

**之前**（使用 Playwright 自带 Chromium，被 BOSS 检测）:
```java
browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
    .setHeadless(false)
    .setArgs(List.of("--remote-debugging-port=7866")));  // CDP端口暴露！
```

**现在**（使用系统 Chrome + 持久化 Profile，不暴露 CDP）:
```java
// 持久化Profile目录（登录态在重启后保留）
java.nio.file.Path profileDir = java.nio.file.Paths.get(
    System.getProperty("user.dir"), ".chrome-profile");

// 使用 launchPersistentContext — 不暴露 CDP 端口
BrowserType.LaunchPersistentContextOptions opts = 
    new BrowserType.LaunchPersistentContextOptions()
        .setHeadless(false)
        .setSlowMo(80)
        .setExecutablePath(Paths.get("C:/Program Files/Google/Chrome/Application/chrome.exe"))
        .setLocale("zh-CN")
        .setTimezoneId("Asia/Shanghai")
        .setArgs(List.of(
            "--disable-blink-features=AutomationControlled",
            "--start-maximized",
            "--no-first-run"
        ));

context = playwright.chromium().launchPersistentContext(profileDir, opts);
```

### 改动：注入反检测脚本

新增了针对 BOSS `disable-devtool` 库的反检测钩子：

```javascript
// === 关键1：覆盖 console.table（BOSS核心检测手段） ===
console.table = function() {};
console.table.toString = function() { 
    return 'function table() { [native code] }'; 
};

// === 关键2：降低 performance.now 精度 ===
performance.now = function() {
    return Date.now() - performance.timeOrigin;
};

// === 关键3：伪装 Function.prototype.toString ===
Function.prototype.toString = function() {
    if (this === console.table) return 'function table() { [native code] }';
    if (this === performance.now) return 'function now() { [native code] }';
    return originalToString.call(this);
};
```

### 改动：防重复跳转冷却

```java
// 30秒内不会重复跳转BOSS登录页
private volatile long lastBossLoginRedirectMs = 0L;
private static final long BOSS_LOGIN_REDIRECT_COOLDOWN_MS = 30_000L;
```

---

## 2. Boss.java — 投递过程反检测

### 新增 `checkBossRedirect()` 方法

在投递过程中检测 BOSS 是否弹出滑块验证或踢回登录页：

```java
private boolean checkBossRedirect(Page page) {
    String url = page.url();
    // 检测滑块验证页
    if (url != null && url.contains("/web/user/safe/verify")) {
        // 等待用户手动完成滑块，最多等5分钟
        while (System.currentTimeMillis() - start < 5 * 60 * 1000) {
            if (!page.url().contains("/web/user/safe/verify")) {
                return false; // 验证通过，继续
            }
            sleep(3);
        }
    }
    // 检测被踢回登录页
    if (url != null && url.contains("/web/user/")) {
        return true; // 需要重新登录
    }
    return false;
}
```

### 随机延迟避免行为检测

```java
// 每个岗位卡片之间1-3秒随机延迟（模拟人类操作节奏）
if (i > 0) {
    int delay = 1000 + (int)(Math.random() * 2000);
    Thread.sleep(delay);
}
```

---

## 3. Liepin.java — SPA页面DOM刷新容错

猎聘是React单页应用，DOM频繁动态更新，Playwright的locator引用会失效。

### 导航重试
```java
for (int retry = 0; retry < 3; retry++) {
    try {
        page.navigate(searchUrl);
        break;
    } catch (Exception e) {
        if (e.getMessage().contains("Object doesn't exist")) {
            sleep(2); // 等DOM稳定后重试
        }
    }
}
```

### 用JS直接操作DOM
```java
// 不通过Playwright locator链，直接用JS滚动（避免locator失效）
page.evaluate(
    "(idx) => { var cards = document.querySelectorAll('div[class*=\"job-card-pc-container\"]');" +
    " if (cards[idx]) { cards[idx].scrollIntoView({behavior: 'instant', block: 'center'}); } }", 
    i
);
```

### 简化按钮查找
```java
// 直接用CSS伪类选择器，避免复杂的locator链
chatButton = page.locator("button:has-text('聊一聊')");
```

---

## 4. Job51.java — 投递按钮/复选框容错

### 复选框多级fallback
```java
Locator checkboxes = page.locator("div.ick");          // 主选择器
if (checkboxes.count() == 0) {
    checkboxes = page.locator("div[class*='ick']");    // fallback 1
}
if (checkboxes.count() == 0) {
    checkboxes = page.locator("input[type='checkbox']");// fallback 2
}
if (checkboxes.count() == 0) {
    // fallback 3：JS批量点击所有可能的选中元素
    page.evaluate("() => { document.querySelectorAll('div.ick, div[class*=ick]')" +
        ".forEach(el => { el.click(); }); }");
}
```

### 批量投递按钮8种选择器组合
```java
String[][] btnSelectors = {
    {"div.tabs_in button.p_but", "1"},       // 原始：第2个按钮
    {"div.tabs_in button.p_but", "0"},       // fallback：第1个按钮
    {"button:has-text('批量投递')", "0"},      // 文本匹配
    {"button:has-text('批量申请')", "0"},
    // ... 8种组合
};
```

### jobId获取兜底
```java
// 之前：只依赖JSON拦截器（异步，可能时序不对）
// 现在：拦截器失败时自动fallback到DOM刮取
if (deliveredIds.isEmpty()) {
    deliveredIds = collectJobIdsOnPage();  // 从页面DOM获取jobId
}
```

---

## 5. ZhiLian.java — 投递验证优化

### 投递按钮多级fallback
```java
Locator applyBtn = card.locator("button.collect-and-apply__btn");
if (applyBtn.count() == 0) {
    applyBtn = card.locator("button:has-text('立即投递'), a:has-text('投递'), .apply-btn");
}
if (applyBtn.count() == 0) {
    applyBtn = card.locator("button, a").filter(
        new Locator.FilterOptions().setHasText("投递|申请|立即"));
}
```

### 投递成功验证
```java
// 点击按钮后验证是否真的投递成功，再标记数据库
boolean delivered = checkDeliverySuccess();  // 检查成功提示
if (delivered) {
    zhilianService.markDeliveredByJobId(jobId);
}
```

### CAPTCHA检测优化
```java
// 每5个岗位检查一次验证码（避免误报）
if (pj.index % 5 == 0 && detectCaptcha()) {
    // 等10秒后二次确认
    sleep(10);
    if (detectCaptcha()) {
        return true; // 真的有验证码，跳过当前页
    }
}
```

### 公司名提取fallback
```java
String companyName = safeGetText(card, "div.companyinfo__name");
if (companyName.isEmpty()) {
    companyName = safeGetText(card, "a[class*='company'], div[class*='company']");
}
```

---

## 6. gradle.properties — 新增

```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

---

## 7. start.bat — 启动流程优化

新增步骤：
- 启动前先 `gradlew --stop` 停止旧Daemon
- 验证JDK版本输出
- 明确提示使用持久化Profile
