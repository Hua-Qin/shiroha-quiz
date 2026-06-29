package com.yiqiu.shirohaquiz.state

/**
 * 知识点章节数据模型
 * 用于「边学边答」学习模式，将教程内容结构化为可浏览、可练习的章节单元
 */
data class KnowledgeSection(
    val id: String,                      // 章节唯一标识，如 "ch2_2"
    val chapterTitle: String,            // 章节大标题
    val sectionTitle: String,            // 小节标题
    val order: Int = 0,                  // 学习顺序序号
    val summary: String = "",            // 一句话核心摘要
    val content: String = "",            // 详细讲解内容（纯文本，\n 分段）
    val codeExample: String = "",        // 示例代码
    val codeExplanation: String = "",    // 代码要点说明
    val difficulty: String = "easy",     // 难度："easy" / "medium" / "hard"
    val prerequisites: List<String> = emptyList(), // 前置章节 id 列表
    val questionTag: String = ""         // 与 Question.knowledgePoints 关联的标签
)

/**
 * 课程数据模型
 * 一门课程包含若干按顺序排列的知识点章节，并关联一个题库
 */
data class KnowledgeCourse(
    val courseId: String,                // 课程唯一标识
    val courseName: String,              // 课程名称
    val description: String = "",        // 课程简介
    val sections: List<KnowledgeSection> = emptyList(), // 知识点章节列表
    val linkedBankId: String? = null,    // 关联的题库 id（导入课程包时建立）
    val importedAt: Long = 0L            // 导入时间戳
)

/**
 * 章节学习进度
 * 记录用户在某个章节的学习与练习状态
 */
data class SectionProgress(
    val studied: Boolean = false,        // 是否已阅读过知识点
    val practiced: Boolean = false,      // 是否已完成至少一次练习
    val correctCount: Int = 0,           // 最近一次练习正确数
    val totalCount: Int = 0,             // 最近一次练习总题数
    val lastStudiedAt: Long = 0L,        // 最后学习时间戳
    val bestAccuracy: Float = 0f,        // 历史最佳正确率（0.0~1.0）
    val practiceCount: Int = 0           // 已练习次数
) {
    val masteryLevel: MasteryLevel
        get() = when {
            !studied -> MasteryLevel.NOT_STARTED
            !practiced -> MasteryLevel.STUDIED
            bestAccuracy >= 0.8f -> MasteryLevel.MASTERED
            bestAccuracy >= 0.6f -> MasteryLevel.NEED_REVIEW
            else -> MasteryLevel.NEED_RELEARN
        }

    val lastAccuracy: Float
        get() = if (totalCount > 0) correctCount.toFloat() / totalCount else 0f
}

enum class MasteryLevel(val label: String) {
    NOT_STARTED("未学习"),
    STUDIED("已学习"),
    NEED_RELEARN("需重学"),
    NEED_REVIEW("需巩固"),
    MASTERED("已掌握")
}