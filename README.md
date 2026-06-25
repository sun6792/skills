# 🧰 sun6792's Claude Code Skills & Projects

> 个人 Claude Code 技能库与 AI 协作项目集合

## 📋 关于本仓库

本仓库汇集了通过 **Claude Code** AI 编程助手协作开发的技能（Skills）和项目。所有内容均由 [sun6792](https://github.com/sun6792) 提供思路和方向，由 Claude Code 辅助实现。

---

## 🎯 Skills（技能）

### 🔒 AI安全岗位求职 (`ai-security-job-search`)

AI 安全方向求职助手，帮助搜索 AI 安全、AI Agent、大模型安全相关岗位。

- **触发场景**: AI安全求职、大模型安全工作、AI红队、Prompt安全、对抗攻击岗位
- **功能**: 职位搜索、匹配度分析、定制化求职材料生成

### 🛡️ 安全运维求职 (`security-ops-job-search`)

网络安全运维方向求职助手，帮助搜索安全运维、SOC 分析、应急响应相关岗位。

- **触发场景**: 安全运维求职、网络安全运维、SOC分析师、防火墙运维、态势感知
- **功能**: 岗位搜索、技能匹配、简历优化建议

### 💼 安全售前求职 (`security-presales-job-search`)

安全售前方向求职助手，帮助搜索安全售前、安全解决方案、安全顾问相关岗位。

- **触发场景**: 安全售前求职、安全解决方案工程师、安全咨询、等保合规售前
- **功能**: 岗位分析、匹配度评估、求职材料定制

### 🔮 四柱八字命理分析 (`bazi-skill`)

四柱八字命理分析工具，通过交互式步骤收集出生信息，排出四柱八字，参照经典命理典籍进行专业分析。

- **参考典籍**: 穷通宝典、三命通会、滴天髓、渊海子平、子平真诠
- **功能**: 八字排盘、大运流年分析、命理解读

---

## 📁 Projects（项目）

### 🔒 网络安全岗位求职工具 (`projects/security-job-search-tools`)

一套面向网络安全行业求职者的自动化招聘信息收集与整理工具。

- **语言**: Python 3
- **功能**: 公司数据生成、多地区覆盖、Excel 导出、条形码批量下载
- **适用**: 应届生求职、专科求职者、网络安全行业

### 🖥️ NN Engine 监控仪表盘 (`projects/nn-engine-dashboard`)

基于 Rust 后端 + 现代 Web 前端的网络引擎实时监控仪表盘。

- **技术栈**: Rust + HTML5/CSS3/JavaScript
- **功能**: 实时连接监控、流量统计、服务器管理、进程监控
- **状态**: 早期开发阶段

---

## 🗂️ 仓库结构

```
skills/
├── README.md                           # 本文件
├── ai-security-job-search/             # AI安全岗位求职技能
│   └── SKILL.md
├── security-ops-job-search/            # 安全运维求职技能
│   └── SKILL.md
├── security-presales-job-search/       # 安全售前求职技能
│   └── SKILL.md
├── bazi-skill/                         # 四柱八字命理分析技能
│   ├── SKILL.md
│   ├── README.md
│   ├── LICENSE
│   └── references/
└── projects/                           # 项目目录
    ├── security-job-search-tools/      # 网络安全岗位求职工具
    │   ├── README.md
    │   ├── src/                        # Python 源代码
    │   └── scripts/                    # PowerShell 辅助脚本
    └── nn-engine-dashboard/            # NN Engine 监控仪表盘
        ├── README.md
        └── index.html
```

---

## 🚀 使用方式

### 在 Claude Code 中使用技能

将本仓库克隆到 Claude Code 的 skills 目录：

```bash
# 克隆仓库
git clone https://github.com/sun6792/skills.git

# 复制需要的技能到 Claude Code skills 目录
cp -r skills/ai-security-job-search ~/.claude/skills/
```

### 运行项目

```bash
# 网络安全岗位求职工具
cd projects/security-job-search-tools
pip install -r requirements.txt
python src/generate_verified_companies_final.py

# NN Engine 监控仪表盘
cd projects/nn-engine-dashboard
# 用浏览器打开 index.html
```

---

## 📝 开发方式

所有技能和项目均通过 **Claude Code** AI 编程助手协作完成：

- 🧠 **思路与方向**: sun6792
- 🤖 **代码实现**: Claude Code (Claude Opus 4.8)
- 🔄 **迭代优化**: 多轮对话式开发

---

## 📄 License

本仓库中的内容除非另有说明，均遵循 MIT License。

---

*🤖 Generated with [Claude Code](https://claude.com/claude-code)*
