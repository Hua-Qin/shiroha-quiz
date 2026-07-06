package com.yiqiu.readingquiz.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.yiqiu.readingquiz.ui.screens.AiSettingsScreen
import com.yiqiu.readingquiz.ui.screens.HomeScreen
import com.yiqiu.readingquiz.ui.screens.QuizScreen
import com.yiqiu.readingquiz.ui.screens.ReadingScreen
import com.yiqiu.readingquiz.ui.screens.ScoreScreen

/**
 * 路由密封类。
 * ArticleId 用于携带当前文章 id。
 */
sealed class Route {
    data object Home : Route()
    data class Reading(val articleId: String) : Route()
    data class Quiz(val articleId: String) : Route()
    data class Score(val articleId: String) : Route()
    data object AiSettings : Route()
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
            (fadeIn() + slideInVertically { it / 8 }) togetherWith
                (fadeOut() + slideOutVertically { -it / 8 })
        },
        label = "page-transition"
    ) { route ->
        when (route) {
            is Route.Home -> HomeScreen(
                onOpenArticle = { id -> stack.add(Route.Reading(id)) },
                onOpenAiSettings = { stack.add(Route.AiSettings) }
            )
            is Route.Reading -> ReadingScreen(
                articleId = route.articleId,
                onBack = { stack.removeAt(stack.lastIndex) },
                onEnterQuiz = { id -> stack.add(Route.Quiz(id)) }
            )
            is Route.Quiz -> QuizScreen(
                articleId = route.articleId,
                onBack = { stack.removeAt(stack.lastIndex) },
                onViewScore = { id -> stack.add(Route.Score(id)) }
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

private infix fun androidx.compose.animation.EnterTransition.togetherWith(
    exit: androidx.compose.animation.ExitTransition
) = androidx.compose.animation.ContentTransform(this, exit)