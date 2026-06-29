# 结构化学习与答题系统增强 Spec

## Why

当前「边学边答」功能将学习与答题混合在同一卡片式页面（`StudySessionScreen`），不符合"先学后练"的严格顺序化学习诉求，且缺乏 AI 解析、错题归因、学习统计图表等闭环能力。本次改造旨在建立**流程分离的文章式学习体验**，并将 AI 解析、错题增强、统计看板、个性化建议等能力整合进现有模块，形成完整的学习闭环。

## What Changes

- **学习/答题流程分离**：将原 `StudySessionScreen`（同页学习+答题）拆分为独立的「学习阅读页」与「答题页」，通过状态机强制顺序（必须完成学习才可进入答题）。学习页采用类文章阅读式 UI（非卡片式）。
- **AI 解析集成**：在答题结果、错题本中复用现有 `ShirohaAiClient.analyzeSingleQuestion`，提供"AI 解析本题"入口（正确答案解释 + 知识点拓展 + 解题思路）。
- **错题本系统增强**：扩展错题记录，存储「用户错误答案、正确答案、AI 解析、错误原因」；新增手动添加错题入口。
- **学习记录与统计系统**：新增统计看板页面，以 Compose Canvas 自绘图表展示学习时长、已学知识点、答题量、正确率趋势、错题分布；复用现有 `StudyRecord` 数据。
- **AI 增强功能**：AI 辅助模拟考试（基于错题/薄弱点智能组卷）、错题智能分析、个性化学习建议（新增 AI 提示词）。
- **全中文**：所有新增界面元素、提示、反馈均使用中文。

## Impact

- Affected code:
  - `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/screens/StudySessionScreen.kt`（重构：拆分学习/答题两阶段）
  - `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/screens/StudyScreen.kt`、`StudyCourseScreen.kt`（文章式学习入口整合）
  - `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/state/QuizRepository.kt`（错题结构扩展、统计聚合方法、AI 建议状态）
  - `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/screens/WrongBookScreen.kt`（错题增强：归因、AI 解析、手动添加）
  - `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ui/screens/HomeScreen.kt`、`ShirohaAppShell.kt`（统计看板入口与路由）
  - `apps/android/app/src/native/java/com/yiqiu/shirohaquiz/ai/ShirohaAiClient.kt`、`AiPrompts.kt`（新增个性化建议/错题分析提示词）
  - 新增统计看板页 `StatisticsScreen.kt`、文章式学习组件 `ArticleReader.kt`
- 复用能力：`ShirohaAiClient.analyzeSingleQuestion`、`StudyRecord`、收藏体系、考试随机抽题、SharedPreferences 持久化范式。

## ADDED Requirements

### Requirement: 学习与答题流程分离

系统 SHALL 提供严格顺序化的学习模式：用户必须先在独立的「学习阅读页」完成当前知识点的学习（点击"我已学完，开始练习"），才能进入对应的「答题页」。学习内容与题目禁止在同一页面同时显示。

#### Scenario: 未完成学习无法答题
- **WHEN** 用户进入某章节学习会话且尚未点击"完成学习"
- **THEN** 仅显示文章式学习内容与"完成学习"按钮，不显示任何题目

#### Scenario: 完成学习后进入答题
- **WHEN** 用户点击"我已学完，开始练习"
- **THEN** 跳转到独立的答题页，学习内容不再显示，仅展示题目与作答交互

#### Scenario: 答题中可回看知识点
- **WHEN** 用户在答题页需要回顾知识点
- **THEN** 提供"查看知识点"入口以只读方式打开学习内容（覆盖层或返回学习页），不与答题混排

### Requirement: 文章式学习界面

学习内容 SHALL 以类文章阅读式 UI 呈现（标题 + 正文段落 + 行内代码块 + 代码示例区块），而非卡片堆叠，并保持与项目现有设计 Token（字体、间距、配色）一致。

#### Scenario: 知识点阅读
- **WHEN** 用户打开某章节学习页
- **THEN** 以文章排版展示章节标题、摘要、正文（支持分段）、代码示例（等宽深色块）、要点说明，整体为连续可滚动文章流

### Requirement: 题目 AI 解析

系统 SHALL 为每道题提供 AI 驱动的详细解析（正确答案解释、相关知识点拓展、解题思路分析），复用现有 `ShirohaAiClient.analyzeSingleQuestion`。

#### Scenario: 查看 AI 解析
- **WHEN** 用户在答题结果或错题本点击"AI 解析本题"且 AI 已配置
- **THEN** 调用 AI 返回结构化解析并展示；AI 未配置时引导去设置页

### Requirement: 错题本系统增强

错题记录 SHALL 存储：题目内容、用户错误答案、正确答案、AI 解析、错误原因分析。系统 SHALL 支持手动添加错题。

#### Scenario: 自动记录错题归因
- **WHEN** 用户答错题目
- **THEN** 错题本记录该题的用户答案、正确答案，并可触发 AI 错误原因分析

#### Scenario: 手动添加错题
- **WHEN** 用户在题目详情/练习页选择"加入错题本"
- **THEN** 该题进入错题本，标记来源为"手动添加"

### Requirement: 学习记录与统计看板

系统 SHALL 提供统计看板，以图表展示学习时长、已学知识点数、答题量、正确率趋势、错题分布，数据来源于现有 `StudyRecord` 与学习进度。

#### Scenario: 查看学习统计
- **WHEN** 用户进入统计看板
- **THEN** 以折线图/柱状图/环形图展示近 N 天答题量与正确率趋势、错题分类分布、累计学习时长

### Requirement: AI 增强功能

系统 SHALL 提供：AI 辅助模拟考试（基于错题/薄弱点智能组卷）、错题智能分析、个性化学习建议。

#### Scenario: 个性化学习建议
- **WHEN** 用户在统计看板点击"获取学习建议"且 AI 已配置
- **THEN** AI 基于学习记录与错题情况生成个性化建议并展示

## MODIFIED Requirements

### Requirement: 边学边答学习会话

将原「同页学习+答题」改为「两阶段分离」：学习阅读页（文章式）→ 答题页。保留章节进度记录、错题回流、关联题库筛选逻辑。

### Requirement: 错题本数据结构

扩展错题条目，新增字段：`userAnswer`（用户错误答案）、`correctAnswer`（正确答案）、`aiAnalysis`（AI 解析缓存）、`errorReason`（错误原因）。兼容旧数据（新字段缺省空值）。
