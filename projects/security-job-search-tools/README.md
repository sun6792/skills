# 🔒 网络安全岗位求职工具 (Security Job Search Tools)

> 一套面向网络安全行业求职者的自动化招聘信息收集与整理工具

## 📋 项目简介

本项目旨在帮助网络安全行业的求职者（特别是应届生和大专学历求职者）高效地收集、整理和筛选招聘信息。通过预设的经过验证的公司数据库，自动生成格式化的招聘信息 Excel 报表。

### 主要功能

- **公司数据生成** — 自动生成包含公司名称、行业、规模、地址、薪资范围、招聘来源、培训体系等详细信息的结构化数据
- **多地区覆盖** — 覆盖长沙、广州等城市的网络安全公司
- **重点标注** — 自动标注专科岗位较多的公司，方便大专学历求职者筛选
- **Excel 导出** — 将数据导出为 CSV/Excel 格式，方便进一步处理
- **条形码批量下载** — 附带条形码批量下载工具

## 🗂️ 项目结构

```
security-job-search-tools/
├── src/                                    # Python 源代码
│   ├── job_search.py                       # 招聘信息收集（基础版）
│   ├── generate_60_companies.py            # 60家公司数据生成器 v1
│   ├── generate_60_companies_v2.py         # 60家公司数据生成器 v2（迭代优化）
│   ├── generate_60_companies_v3.py         # 60家公司数据生成器 v3（迭代优化）
│   ├── generate_60_companies_v4.py         # 60家公司数据生成器 v4（最终版）
│   ├── generate_changsha_companies.py      # 长沙地区公司专项生成器
│   ├── generate_excel.py                   # 通用Excel报表生成器
│   ├── generate_verified_companies.py      # 经过验证的公司数据生成器
│   └── generate_verified_companies_final.py # 最终验证版公司数据生成器
├── scripts/                                # 辅助脚本
│   ├── download-barcodes.ps1               # 条形码批量下载脚本
│   ├── download-lin.ps1                    # 条形码批量下载脚本 (lin)
│   └── download-simple.ps1                 # 简化版条形码下载脚本
├── requirements.txt                        # Python 依赖
├── .gitignore                              # Git 忽略规则
└── README.md                               # 项目说明
```

## 🚀 快速开始

### 环境要求

- Python 3.7+
- pandas（可选，用于增强数据处理）

### 安装依赖

```bash
pip install -r requirements.txt
```

### 运行

```bash
# 生成60家公司招聘信息（推荐使用最终版）
python src/generate_verified_companies_final.py

# 生成长沙地区公司信息
python src/generate_changsha_companies.py

# 运行基础招聘搜索
python src/job_search.py
```

## 📊 数据字段说明

每条公司记录包含以下字段：

| 字段 | 说明 |
|------|------|
| 公司名称 | 公司全称 |
| 行业 | 所属行业（网络安全/信息技术等） |
| 规模 | 公司人员规模 |
| 注册地址 | 公司所在城市和区域 |
| 应届生薪资范围 | 应届生的参考薪资 |
| 招聘来源 | 招聘信息发布平台 |
| 培训体系 | 公司培训机制描述 |
| 公司简介 | 公司简要介绍 |
| 成立年份 | 公司成立时间 |
| 专科岗位多 | 是否提供较多专科岗位 |

## 🎯 适用场景

- **应届生求职** — 快速筛选有应届生培养计划的公司
- **专科求职者** — 重点发现对大专学历友好的企业
- **网络安全行业** — 专注网安领域的招聘信息
- **多城市对比** — 比较不同城市的薪资和岗位情况

## ⚠️ 免责声明

本项目中的公司数据来源于公开渠道的整理和验证，仅供参考。求职时请以实际招聘网站的最新信息为准。建议在使用前对关键信息进行二次核实。

## 🛠️ 技术栈

- **语言**: Python 3
- **数据格式**: CSV / Excel
- **辅助工具**: PowerShell（条形码下载）

## 📝 开发历程

本项目通过 **Claude Code** AI 编程助手协作开发，经历了多个迭代版本：

1. **v1** — 初始版本，建立基础公司数据库
2. **v2** — 扩充公司数量，优化数据字段
3. **v3** — 增加多地区覆盖，完善培训体系信息
4. **v4 / Final** — 经过真实招聘网站验证的最终版本

---

*🤖 本项目由 [sun6792](https://github.com/sun6792) 与 Claude Code 协作完成*
