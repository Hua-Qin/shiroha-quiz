# Tasks

- [x] Task 1: 重构学习会话为两阶段分离（学习阅读页 + 答题页）
  - [x] SubTask 1.1: 在 `StudySessionScreen.kt` 引入会话阶段状态机（LEARN / QUIZ / DONE），LEARN 阶段仅显示学习内容与"完成学习"按钮，禁止显示题目
  - [x] SubTask 1.2: QUIZ 阶段为独立答题视图，不与学习内容同页混排；提供"查看知识点"只读入口
  - [x] SubTask 1.3: 复用现有 `judgeAnswer`、`questionsForSection`、`recordSectionResult`、错题回流逻辑
  - [x] SubTask 1.4: 完成答题后进入 DONE 阶段（总结 + 重练/下一章/返回）

- [x] Task 2: 实现文章式学习阅读组件
  - [x] SubTask 2.1: 新建 `ArticleReader.kt` 组件，以连续文章流排版（标题/摘要/正文分段/行内代码/代码块/要点），非卡片堆叠
  - [x] SubTask 2.2: 复用现有 `CodeBlock.kt` 渲染代码示例，复用设计 Token（字体/间距/配色）
  - [x] SubTask 2.3: LEARN 阶段使用 `ArticleReader` 替换原 `KnowledgeCard` 卡片式展示

- [x] Task 3: 集成题目 AI 解析（复用现有 ShirohaAiClient）
  - [x] SubTask 3.1: 在答题结果区与错题本增加"AI 解析本题"按钮，调用 `ShirohaAiClient.analyzeSingleQuestion`
  - [x] SubTask 3.2: AI 未配置时引导至设置页（复用 `isAiConfigured()` 判断）
  - [x] SubTask 3.3: 解析结果结构化展示（正确答案解释 / 知识点拓展 / 解题思路）

- [x] Task 4: 增强错题本系统
  - [x] SubTask 4.1: 在 `QuizRepository` 扩展错题条目结构，新增 `userAnswer`/`correctAnswer`/`aiAnalysis`/`errorReason` 字段（兼容旧数据）
  - [x] SubTask 4.2: 答题判错时写入用户答案与正确答案；支持触发 AI 错误原因分析并缓存
  - [x] SubTask 4.3: 在 `WrongBookScreen` 增加手动添加错题入口（从题库选题加入，标记来源"手动添加"）
  - [x] SubTask 4.4: 错题详情展示用户错误答案、正确答案、AI 解析、错误原因

- [x] Task 5: 实现学习记录与统计看板
  - [x] SubTask 5.1: 新建 `StatisticsScreen.kt`，用 Compose Canvas 自绘图表（折线图：答题量/正确率趋势；柱状图：错题分类分布；环形/数字：累计时长/知识点数）
  - [x] SubTask 5.2: 在 `QuizRepository` 增加统计聚合方法（基于 `StudyRecord` 与学习进度计算趋势、分布）
  - [x] SubTask 5.3: 在 `HomeScreen` 或导航增加统计看板入口，在 `ShirohaAppShell` 注册路由

- [x] Task 6: 实现 AI 增强功能
  - [x] SubTask 6.1: 新增 `AiPrompts` 个性化学习建议提示词与错题分析提示词
  - [x] SubTask 6.2: 在 `ShirohaAiClient` 增加个性化建议方法（输入学习记录摘要 + 错题摘要 → 输出建议）
  - [x] SubTask 6.3: 统计看板提供"获取学习建议"入口；错题本提供"AI 智能分析"批量入口
  - [x] SubTask 6.4: AI 辅助模拟考试：基于错题/薄弱章节智能组卷（复用考试随机抽题，增加"错题优先"组卷策略）

- [x] Task 7: 全中文与一致性校验
  - [x] SubTask 7.1: 所有新增界面文案、按钮、提示、空状态、错误反馈均为中文
  - [x] SubTask 7.2: 确认新界面与项目设计 Token、组件风格（GlassCard/ActionPillButton/NoticeCard 等）一致

# Task Dependencies

- Task 2 依赖 Task 1（学习阶段先拆分，再用文章组件替换展示）
- Task 3 依赖 Task 1（答题结果区在 QUIZ 阶段）
- Task 4 的错题写入依赖 Task 1 的答题判错流程
- Task 5 的统计聚合部分依赖 Task 1 的学习进度数据
- Task 6 依赖 Task 3（复用 AI 解析）与 Task 5（建议入口在看板）
- Task 7 为最终校验，依赖 Task 1-6