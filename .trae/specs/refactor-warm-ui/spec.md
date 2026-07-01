# 全量 UI 重构 · 暖色编辑杂志风 Spec

## Why

当前应用端 UI 存在三类深层问题,简单的"加暖色开关"无法解决:
1. **视觉缺乏作者性**:全仓锁定冷蓝调 + 全 SansSerif 字型 + GlassCard 容器堆叠,落入 AI 生成视觉的"最大公约数"(典型 AI slop),没有品牌识别度。
2. **布局缺乏编辑感**:首页开场就是 6 个密集 MetricCell 网格,没有视觉重点、没有插画 hero、没有杂志级数据呈现;信息密度均匀用力 = 没有重点。
3. **冗余遗留**:已弃用的 `HomeScreen.kt` 仍占据源码文件。

本次改造目标是**对全部 21 个 Native 屏幕做全量 UI 重构**(布局 + 视觉 + 组件),建立一套"暖色编辑杂志风"设计系统(参照 Stripe Press / monocle 杂志气质),让产品有温度、有作者性、有编辑级排版品质,同时完整保留 Shiroha 模式与全部业务功能。

## Scope(仅 Native 端,仅 UI 层)

**只改动 `apps/android/app/src/native/` 下的 Kotlin UI 代码**,不触碰:
- Web 端(`apps/web/`、`assets/web/`)
- Web flavor(`src/web/`)
- 业务逻辑(答题引擎、导入解析、AI 调用、数据持久化、状态管理逻辑)

**UI 层改动范围**:
- `ui/theme/`(Tokens、Type、Theme)— 设计系统基座
- `ui/components/`(ShirohaComponents 等通用组件)— 组件库
- `ui/screens/`(21 个屏幕)— 布局重构
- `MainActivity.kt` — 主题入口与状态栏

## 现有 UI 问题诊断

| 问题 | 位置 | 具体表现 |
|---|---|---|
| 色彩锁定冷蓝 | `Tokens.kt` `ShirohaColors` | 全仓仅冷蓝(浅/深),无暖色方案 |
| 字型单一 | `Type.kt` | 全部 SansSerif,无衬线 display,缺杂志气质 |
| 首页无视觉重点 | `HomeDashboardScreen.kt` | 开场 6 个密集 MetricCell,无 hero、无插画 |
| 容器堆叠 | `GlassCard` 滥用 | 每个区块都套 GlassCard,缺发丝分隔线编辑感 |
| 数据呈现平淡 | `MetricCell` | 图标+数字+标签三件套,无杂志级大数字呈现 |
| 已弃用文件残留 | `HomeScreen.kt` | 入口已迁移,旧文件混淆 |

## 重构方案设计

### 总体策略:先建设计系统 → 1 屏样板验证 → 批量铺开

采用"设计系统优先 + 样板屏验证 + 分批推进"的三段式,避免一次性大改引入回归。

### 设计系统:暖色编辑杂志风(Warm Editorial Magazine)

**参照风格**(来自 garden-skills `web-design-engineer` 风格配方 + huashu-design 反 slop 规则):
- **Stripe Press**:奶油纸张底色、衬线大标题、慷慨留白、编辑式数据呈现
- **monocle 杂志**:小型大写 kicker、发丝分隔线、单 accent 色、严谨网格
- **mid-century-modern**:暖琥珀 `#B45309`、暖深棕墨 `#3D2B1F`

**色彩 Token**(三态优先级:warmThemeEnabled > isDarkMode > 默认冷蓝):

| Token | 冷蓝浅(保留) | 暖色(新增) |
|---|---|---|
| `BgApp` | `#F4F6FB` | `#FBF6EC`(暖米白纸) |
| `CardDefault/Soft` | `#FFFFFF` | `#FFFCF5`(暖纸) |
| `TextPrimary` | `#101828` | `#3D2B1F`(暖深棕墨) |
| `TextSecondary` | `#667085` | `#8B7355`(暖灰棕) |
| `LineStrong` | `#D8E0EF` | `#E8D5C4`(暖沙线) |
| `BrandPrimary` | `#4F7CFF` | `#B45309`(暖琥珀,反 AI 紫) |
| `BrandPrimarySoft` | `#EAF0FF` | `#FEF3C7`(暖奶油) |

**字型系统**:
- `displaySmall` / `headlineMedium` / `headlineSmall` → **FontFamily.Serif**(衬线大标题,杂志气质)
- `titleLarge` / `titleMedium` / `body*` → FontFamily.SansSerif(正文可读性)
- 新增 `EditorialFigureStyle`:衬线粗体 56sp(杂志封面级大数字)
- 新增 `EditorialKickerStyle`:无衬线 SemiBold 12sp + 字间距 1.6sp(小型大写 kicker)

**布局哲学**(反 AI slop):
- ✅ 发丝分隔线(`LineSoft`)分隔区块 > 重卡片容器堆叠
- ✅ 大数字 + 小标签(编辑式数据呈现)> 图标+数字+标签三件套
- ✅ 慷慨留白 + 单 accent 色(暖琥珀)
- ✅ Hero 区带 Shiroha 浮动插画(启用 Shiroha 模式时)
- ❌ 紫粉渐变(反 AI slop 红线)
- ❌ Emoji 图标(反 AI slop 红线)
- ❌ 圆角卡片 + 左 border accent 滥用

### 组件库改造

| 组件 | 现状 | 重构方向 |
|---|---|---|
| `GlassCard` | 重卡片容器 | 保留,但减少使用频率;新增 `EditorialSection`(发丝分隔线区块)作为替代 |
| `ShirohaHeader` | kicker+title+subtitle 三行 | 改用 `EditorialKickerStyle` + Serif title |
| `IllustrationHeroCard` | 已存在但首页未用 | 首页 hero 区启用,带 Shiroha 浮动插画 |
| 新增 `EditorialFigure` | 无 | 衬线超大数字 + 小标签(杂志封面级数据) |
| 新增 `EditorialDivider` | 无 | 发丝分隔线 + 可选居中标签 |
| `MetricCell` | 图标+数字+标签 | 重构为编辑式:大数字(衬线)+ 小标签 + 发丝下划线 |

### 21 屏重构清单(分批)

**第一批(核心高频 5 屏)**:
1. `HomeDashboardScreen`(首页/学习看板)— **样板屏,优先做**
2. `PracticeScreen`(练习)
3. `WrongBookScreen`(错题本)
4. `StatisticsScreen`(统计)
5. `MeScreen` + `AppearancePreferenceScreen`(设置/外观偏好)

**第二批(学习与题库 6 屏)**:
6. `StudyScreen` / `StudyCourseScreen` / `StudySessionScreen`(边学边答三屏)
7. `BankListScreen` / `BankDetailScreen` / `BankReviewScreen`(题库三屏)

**第三批(答题与记录 5 屏)**:
8. `ExamScreen`(考试)
9. `RecordsScreen` / `RecordDetailScreen`(记录)
10. `FavoriteScreen`(收藏)
11. `ImportScreen` / `StandardImportFormatScreen`(导入)
12. `QuestionSearchScreen`(搜索)

**第四批(收尾)**:
13. `AboutScreen`(关于)
14. `HomeRedirectNotice`(首页重定向提示)

## What Changes

### 已完成的基础(本次 spec 之前)
- 删除已弃用的 `HomeScreen.kt`
- `QuizRepository` 新增 `warmThemeEnabled` 偏好(SP 持久化 + setter)
- `Tokens.kt` 全部颜色 getter 改为三态(warm > dark > 默认)
- `Type.kt` display/headline 改 Serif + 新增 EditorialFigure/Kicker 样式
- `Theme.kt` `ShirohaQuizTheme` 新增 warmTheme 参数
- `MainActivity.kt` 状态栏暖色适配
- `MeScreen.kt` 外观偏好页新增暖色开关

### 本 spec 待做
- **组件库**:新增 `EditorialSection`/`EditorialFigure`/`EditorialDivider`;重构 `MetricCell`、`ShirohaHeader`
- **首页样板**:`HomeDashboardScreen` 全量重写(hero 插画 + 编辑式数据 + 发丝区块)
- **批量铺开**:第二批 ~ 第四批 20 屏布局重构
- **插画集成**:首页及关键页启用 `IllustrationHeroCard`(受 Shiroha 模式控制)

## Impact

- Affected specs: `enhance-structured-learning`(边学边答 UI 视觉变化,功能不变)
- Affected code:
  - `ui/theme/Tokens.kt`(已完成暖色分支)
  - `ui/theme/Type.kt`(已完成 Serif 改造)
  - `ui/theme/Theme.kt`(已完成 warmTheme 参数)
  - `ui/components/ShirohaComponents.kt`(待改 + 新增编辑式组件)
  - `ui/screens/*.kt`(21 屏待重构,首批 5 屏优先)
  - `MainActivity.kt`(已完成状态栏适配)
  - `state/QuizRepository.kt`(已完成 warmThemeEnabled)
  - 删除 `ui/screens/HomeScreen.kt`(已完成)

## ADDED Requirements

### Requirement: 暖色编辑杂志风设计系统
系统 SHALL 提供一套完整的暖色编辑杂志风设计系统,包含:暖色色彩 Token(暖琥珀 accent + 暖米白纸底 + 暖深棕墨)、Serif display + Sans body 字型、编辑式数据呈现组件(EditorialFigure)、发丝分隔线区块(EditorialSection)。

#### Scenario: 暖色启用
- **WHEN** 用户启用暖色主题
- **THEN** 全部页面呈现暖米白底 + 暖深棕字 + 暖琥珀 accent,衬线大标题 + 编辑式数据呈现

### Requirement: 编辑式组件库
系统 SHALL 提供编辑式组件:`EditorialFigure`(衬线超大数字 + 小标签)、`EditorialSection`(发丝分隔线区块)、`EditorialDivider`(发丝线 + 可选标签),用于替代重卡片容器堆叠。

#### Scenario: 数据呈现编辑化
- **WHEN** 展示统计数据(累计答题、正确率等)
- **THEN** 采用衬线超大数字 + 小标签 + 发丝下划线,而非图标+数字+标签三件套

### Requirement: 首页样板(hero + 插画)
首页 SHALL 以 hero 区开场(Shiroha 模式时带浮动插画),接编辑式数据呈现,区块间用发丝分隔线分隔。

#### Scenario: Shiroha 模式 + 暖色
- **WHEN** 同时启用暖色主题与 Shiroha 模式
- **THEN** 首页呈现暖色 hero 区 + Shiroha 浮动插画,插画与暖色背景和谐共存

## MODIFIED Requirements

### Requirement: 21 屏布局重构
全部 21 个 Native 屏幕 SHALL 采用暖色编辑杂志风布局:Serif 大标题、编辑式数据、发丝分隔、慷慨留白。功能逻辑零改动。

#### Scenario: 功能完整性
- **WHEN** 任一屏幕重构完成
- **THEN** 该屏幕全部原有功能(按钮、跳转、数据展示)正常工作,仅视觉与布局变化

## REMOVED Requirements

### Requirement: 已弃用 HomeScreen
**Reason**: 入口已迁移到 HomeDashboardScreen
**Migration**: 无(已删除,全局无引用)
