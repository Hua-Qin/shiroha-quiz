# Checklist

## 阶段零:基础(已完成)
- [x] HomeScreen.kt 已删除,无残留引用
- [x] QuizRepository warmThemeEnabled(SP key + 状态 + init + setter + persist)
- [x] Tokens.kt 全部颜色 getter 三态(warm > dark > 默认)
- [x] Type.kt display/headline 改 Serif + EditorialFigureStyle + EditorialKickerStyle
- [x] Theme.kt warmTheme 参数
- [x] MainActivity.kt 状态栏暖色适配
- [x] MeScreen.kt AppearancePreferenceScreen 暖色开关

## 阶段一:编辑式组件库
- [ ] `EditorialFigure(value, label, unit)` 已新增:衬线超大数字 + 小标签 + 发丝下划线
- [ ] `EditorialSection(title?, kicker?, content)` 已新增:发丝分隔线区块
- [ ] `EditorialDivider(label?)` 已新增:发丝线 + 可选居中标签
- [ ] `ShirohaHeader` 已重构:kicker 用 EditorialKickerStyle,title 用 Serif

## 阶段二:首页样板
- [ ] HomeDashboardScreen hero 区启用 IllustrationHeroCard(Shiroha 模式带浮动插画)
- [ ] 6 个指标改用 EditorialFigure 呈现(衬线大数字 + 小标签 + 发丝下划线)
- [ ] 今日学习/趋势图/错题分类/AI 建议区用 EditorialSection 包裹
- [ ] 函数签名与全部回调保留(onGoStudy/onGoExam/onOpenWrongBook 等不变)
- [ ] 全部数据读取保留(stats/todayPracticeCount/wrongBookActiveCount 等)
- [ ] 全部功能正常(继续练习/模拟考试/快捷入口/AI 建议)

## 阶段三:第一批 4 屏
- [ ] PracticeScreen:题干 Serif,选项暖色,答题逻辑不变
- [ ] WrongBookScreen:发丝分隔列表,筛选/排序/复习/AI 解析逻辑不变
- [ ] StatisticsScreen:OverviewRow 改 EditorialFigure,统计逻辑不变
- [ ] MeScreen/AppearancePreferenceScreen:设置暖色化,偏好逻辑不变

## 阶段四:第二批 6 屏
- [ ] StudyScreen/StudyCourseScreen/StudySessionScreen:三阶段视觉暖色,学习逻辑不变
- [ ] BankListScreen/BankDetailScreen/BankReviewScreen:发丝分隔,CRUD 逻辑不变

## 阶段五:第三批 5 屏
- [ ] ExamScreen:考试视觉暖色,考试逻辑不变
- [ ] RecordsScreen/RecordDetailScreen:记录视觉暖色
- [ ] FavoriteScreen:收藏视觉暖色
- [ ] ImportScreen/StandardImportFormatScreen:导入视觉暖色,导入解析逻辑不变
- [ ] QuestionSearchScreen:搜索视觉暖色

## 阶段六:第四批收尾
- [ ] AboutScreen:关于页视觉暖色
- [ ] HomeRedirectNotice:重定向提示视觉暖色

## 反 AI slop 检查
- [ ] 无紫粉渐变
- [ ] 无 emoji 图标
- [ ] 无圆角卡片 + 左 border accent 滥用
- [ ] 暖琥珀 #B45309 作为单 accent 色(有 mid-century-modern 风格出处)
- [ ] Serif display + Sans body 字型配对(有杂志气质)

## Shiroha 模式兼容
- [ ] 暖色 + Shiroha 模式同时启用时,首页 hero 插画正常
- [ ] 暖色 + Shiroha 模式同时启用时,开屏插画正常
- [ ] 暖色 + Shiroha 模式同时启用时,图标切换正常
- [ ] 暖色不影响 Shiroha 模式的任何功能

## 功能完整性(每屏都需验证)
- [ ] 首页/看板功能正常
- [ ] 练习/考试答题逻辑正常
- [ ] 错题本/收藏功能正常
- [ ] 统计图表正常
- [ ] 边学边答三阶段正常
- [ ] 题库管理 CRUD 正常
- [ ] 导入解析正常
- [ ] 搜索功能正常
- [ ] 设置页全部偏好正常

## 主题切换
- [ ] 冷蓝→暖色即时切换
- [ ] 暖色→冷蓝即时切换
- [ ] 暖色持久化(重启恢复)
- [ ] 深色 + 暖色组合时暖色优先

## 品质评审
- [ ] huashu-design 5 维度评审通过(哲学/层级/细节/功能/创新)
