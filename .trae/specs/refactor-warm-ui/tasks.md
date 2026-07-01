# Tasks

## 阶段零:已完成的基础(勿重复)

- [x] Task 0a: 删除已弃用的 HomeScreen.kt(已删除,无残留引用)
- [x] Task 0b: QuizRepository 新增 warmThemeEnabled 偏好(SP key + 状态 + init + setter + persist)
- [x] Task 0c: Tokens.kt 全部颜色 getter 改三态(warmThemeEnabled > isDarkMode > 默认)
- [x] Task 0d: Type.kt display/headline 改 FontFamily.Serif + 新增 EditorialFigureStyle / EditorialKickerStyle
- [x] Task 0e: Theme.kt 新增 warmTheme 参数
- [x] Task 0f: MainActivity.kt 状态栏暖色适配
- [x] Task 0g: MeScreen.kt AppearancePreferenceScreen 新增暖色开关

## 阶段一:编辑式组件库

- [ ] Task 1: 在 ShirohaComponents.kt 新增编辑式组件
  - `EditorialFigure(value: String, label: String, unit: String)`:衬线超大数字(EditorialFigureStyle)+ 小标签 + 发丝下划线
  - `EditorialSection(title: String?, kicker: String?, content)`:发丝分隔线区块(顶部发丝线 + 可选 kicker/title + 内容),替代部分 GlassCard
  - `EditorialDivider(label: String? = null)`:发丝分隔线,可选居中标签
  - 重构 `ShirohaHeader`:kicker 改用 EditorialKickerStyle,title 改 Serif

## 阶段二:首页样板屏(优先)

- [ ] Task 2: 全量重写 HomeDashboardScreen.kt 为暖色编辑杂志风
  - **hero 区**:启用 `IllustrationHeroCard`(Shiroha 模式时带浮动插画 `illus_home_welcome`),暖色 hero 标题用 Serif
  - **编辑式数据区**:6 个指标改为 `EditorialFigure` 呈现(2 列网格,衬线大数字 + 小标签 + 发丝下划线),替代 MetricCell
  - **今日学习区**:`EditorialSection` 包裹,大数字今日练题数 + 待复习数 + ActionPillButton(继续练习/模拟考试)
  - **趋势图区**:`EditorialSection` 包裹 DailyTrendChart,标题用 Serif
  - **错题分类区**:`EditorialSection` 包裹 CategoryBarChart
  - **快捷入口区**:2x2 `DashboardShortcutCard` 保留(错题本/收藏/边学边答/学习记录),视觉改暖色
  - **AI 建议区**:`EditorialSection` 包裹,保留 AI 获取/重新生成逻辑
  - **保留全部函数签名与回调**(onGoStudy/onGoExam/onOpenWrongBook 等不变)
  - **保留全部数据读取**(stats/todayPracticeCount/wrongBookActiveCount 等)

## 阶段三:第一批剩余 4 屏

- [ ] Task 3: 重构 PracticeScreen.kt(练习页)
  - 题干区用 Serif 大标题,选项卡视觉暖色化,答题进度用编辑式数字
  - 保留全部答题引擎逻辑(选题/判分/下一题/批量模式等)

- [ ] Task 4: 重构 WrongBookScreen.kt(错题本)
  - 错题列表用发丝分隔 > 重卡片,筛选区 EditorialSection 包裹
  - 保留筛选/排序/复习/AI 解析逻辑

- [ ] Task 5: 重构 StatisticsScreen.kt(统计页)
  - OverviewRow 改 EditorialFigure,趋势图与分类图用 EditorialSection 包裹
  - 保留全部统计计算与 AI 建议逻辑

- [ ] Task 6: 重构 MeScreen.kt 与 AppearancePreferenceScreen(设置/外观)
  - 设置入口列表暖色化,偏好子页用 EditorialSection
  - 保留全部偏好开关逻辑

## 阶段四:第二批 6 屏(学习与题库)

- [ ] Task 7: 重构 StudyScreen / StudyCourseScreen / StudySessionScreen(边学边答三屏)
  - StudySession 三阶段(LEARN/QUIZ/DONE)视觉暖色化,ArticleReader 用 Serif
  - 保留三阶段状态机与全部学习逻辑

- [ ] Task 8: 重构 BankListScreen / BankDetailScreen / BankReviewScreen(题库三屏)
  - 题库列表用发丝分隔,详情页用 EditorialSection
  - 保留题库 CRUD 逻辑

## 阶段五:第三批 5 屏(答题与记录)

- [ ] Task 9: 重构 ExamScreen.kt(考试)
- [ ] Task 10: 重构 RecordsScreen / RecordDetailScreen(记录)
- [ ] Task 11: 重构 FavoriteScreen.kt(收藏)
- [ ] Task 12: 重构 ImportScreen / StandardImportFormatScreen(导入)
- [ ] Task 13: 重构 QuestionSearchScreen.kt(搜索)

## 阶段六:第四批收尾

- [ ] Task 14: 重构 AboutScreen.kt(关于)
- [ ] Task 15: 重构 HomeRedirectNotice.kt(首页重定向提示)

## 阶段七:验证

- [ ] Task 16: 全量功能完整性验证
  - 21 屏逐一验证功能未受影响
  - 暖色/冷蓝切换正常,Shiroha 模式兼容

- [ ] Task 17: huashu-design 5 维度专家评审
  - 哲学一致性 / 视觉层级 / 细节执行 / 功能性 / 创新性
  - 产出 Keep/Fix/Quick Wins 清单

# Task Dependencies
- Task 1(组件库)是 Task 2-15 的基础
- Task 2(首页样板)依赖 Task 1,完成后作为其余屏的参照
- Task 3-6(第一批剩余)依赖 Task 1,可与 Task 2 后并行
- Task 7-15 依赖 Task 1,各屏之间相互独立可并行
- Task 16-17 依赖全部前置完成
