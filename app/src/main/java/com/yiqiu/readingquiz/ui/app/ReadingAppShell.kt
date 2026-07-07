package com.yiqiu.readingquiz.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.yiqiu.readingquiz.ui.screens.AiSettingsScreen
import com.yiqiu.readingquiz.ui.screens.ChapterOutlineScreen
import com.yiqiu.readingquiz.ui.screens.HomeScreen
import com.yiqiu.readingquiz.ui.screens.QuestionBankScreen
import com.yiqiu.readingquiz.ui.screens.QuestionEditorScreen
import com.yiqiu.readingquiz.ui.screens.QuizScreen
import com.yiqiu.readingquiz.ui.screens.ReadingScreen
import com.yiqiu.readingquiz.ui.screens.ScoreScreen
import com.yiqiu.readingquiz.ui.theme.CafeMotion

/**
 * 路由密封类。
 * ArticleId 用于携带当前文章 id；sectionId 用于章节锚点跳转（可选）。
 */
sealed class Route {
    data object Home : Route()
    data class Reading(val articleId: String, val initialSectionId: String? = null) : Route()
    data class Quiz(val articleId: String, val sectionId: String? = null) : Route()
    data class Score(val articleId: String) : Route()
    data object AiSettings : Route()
    data class ChapterOutline(val articleId: String) : Route()
    data class QuestionBank(val articleId: String) : Route()
    data class QuestionEditor(val articleId: String, val questionId: String) : Route()
}

@Composable
fun ReadingAppShell() {
    val stack = remember { mutableStateListOf<Route>(Route.Home) }
    val current = stack.last()
    BackHandler(enabled = stack.size > 1) {
        stack.removeAt(stack.lastIndex)
    }

    AnimatedContent(
        targetState = current,
        transitionSpec = {
            (fadeIn(tween(CafeMotion.pageEnter, easing = CafeMotion.easeOut)) +
                slideInVertically(
                    animationSpec = tween(CafeMotion.pageEnter, easing = CafeMotion.easeOut)
                ) { CafeMotion.pageSlideDp }) togetherWith
                (fadeOut(tween(CafeMotion.pageEnter, easing = CafeMotion.easeOut)) +
                    slideOutVertically(
                        animationSpec = tween(CafeMotion.pageEnter, easing = CafeMotion.easeOut)
                    ) { -CafeMotion.pageSlideDp })
        },
        label = "page-transition"
    ) { route ->
        when (route) {
            is Route.Home -> HomeScreen(
                onOpenArticle = { id -> stack.add(Route.ChapterOutline(id)) },
                onOpenAiSettings = { stack.add(Route.AiSettings) }
            )
            is Route.ChapterOutline -> ChapterOutlineScreen(
                articleId = route.articleId,
                onBack = { stack.removeAt(stack.lastIndex) },
                onSelectSection = { sectionId ->
                    // 选章节后：先把 Reading 入栈（含 initialSectionId），再把 Quiz 入栈（含 sectionId）
                    // 这样用户进阅读页时自动滚动到该章节，进答题时只答该章节题目
                    stack.add(Route.Reading(route.articleId, sectionId))
                }
            )
            is Route.Reading -> ReadingScreen(
                articleId = route.articleId,
                onBack = { stack.removeAt(stack.lastIndex) },
                onEnterQuiz = { id -> stack.add(Route.Quiz(id, route.initialSectionId)) },
                initialSectionId = route.initialSectionId,
                onOpenQuestionBank = { id -> stack.add(Route.QuestionBank(id)) }
            )
            is Route.QuestionBank -> QuestionBankScreen(
                articleId = route.articleId,
                onBack = { stack.removeAt(stack.lastIndex) },
                onEnterQuiz = { id -> stack.add(Route.Quiz(id, null)) },
                onEditQuestion = { id, qid -> stack.add(Route.QuestionEditor(id, qid)) }
            )
            is Route.QuestionEditor -> QuestionEditorScreen(
                articleId = route.articleId,
                questionId = route.questionId,
                onBack = { stack.removeAt(stack.lastIndex) }
            )
            is Route.Quiz -> QuizScreen(
                articleId = route.articleId,
                onBack = { stack.removeAt(stack.lastIndex) },
                onViewScore = { id -> stack.add(Route.Score(id)) },
                sectionId = route.sectionId
            )
            is Route.Score -> ScoreScreen(
                articleId = route.articleId,
                onBack = { stack.removeAt(stack.lastIndex) },
                onBackToArticle = { id -> stack.removeAll { it is Route.Reading && it.articleId == id } }
            )
            is Route.AiSettings -> AiSettingsScreen(
                onBack = { stack.removeAt(stack.lastIndex) }
            )
        }
    }
}