﻿package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.ScreenClass
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import com.yiqiu.shirohaquiz.ui.theme.uiScaleFor

@Composable
fun HomeRedirectNotice(onGoStatistics: () -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass: ScreenClass = screenClassFor(maxWidth)
        val scale: Float = editorialScaleFor(screenClass)
        val uiScale: Float = uiScaleFor(screenClass)

        // 鍔ㄦ€佹枃妗?鏍规嵁褰撳墠瑙﹀彂鏉′欢缁欏嚭鎻愮ず
        val triggerTitle = resolveTriggerTitle()
        val triggerSubtitle = resolveTriggerSubtitle()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            // === Header: kicker + 琛嚎澶ф爣棰?+ 鍓枃 ===
            ShirohaHeader(
                kicker = "Notice",
                title = "棣栭〉璺宠浆",
                subtitle = "鏌愪簺鎯呭喌涓?棣栭〉鍙兘浼氳閲嶅畾鍚戝埌鍏朵粬椤甸潰",
                scale = scale
            )

            // === Hero 鍖?Shiroha 妯″紡鏃跺甫娴姩鎻掔敾 ===
            IllustrationHeroCard(
                title = triggerTitle,
                subtitle = triggerSubtitle,
                imageRes = R.drawable.illus_home_welcome,
                modifier = Modifier.height(ShirohaDimens.HeroCardHeight),
                imageSize = ShirohaDimens.HeroImageSize,
                scale = scale
            )

            // === 瑙﹀彂鎯呭舰鍒楄〃 ===
            EditorialSection(
                kicker = "Cases",
                title = "瑙﹀彂鎯呭舰",
                scale = scale
            ) {
                TriggerCaseRow(
                    icon = Icons.Rounded.Warning,
                    title = "棰樺簱涓虹┖",
                    desc = "鏈湴鏈鍏ヤ换浣曢搴撴椂,棣栭〉浼氬紩瀵艰繘鍏ュ涔犳暟鎹湅鏉裤€?,
                    active = QuizRepository.banks.isEmpty()
                )
                EditorialDivider()
                TriggerCaseRow(
                    icon = Icons.Rounded.QueryStats,
                    title = "棣栨鍚姩",
                    desc = "棣栨鍚姩鎴栧崌绾у悗,浼氫紭鍏堝睍绀哄涔犳暟鎹湅鏉裤€?,
                    active = false
                )
                EditorialDivider()
                TriggerCaseRow(
                    icon = Icons.Rounded.Warning,
                    title = "鍗囩骇鍚?,
                    desc = "搴旂敤鍗囩骇鍒版柊鐗堟湰鍚?棣栭〉鍙兘閲嶅畾鍚戜互灞曠ず鏂板姛鑳姐€?,
                    active = false
                )
            }

            // === 璺宠浆鎿嶄綔:涓绘搷浣?+ 娆℃搷浣?===
            EditorialSection(
                kicker = "Action",
                title = "璺宠浆鎿嶄綔",
                scale = scale
            ) {
                HomeNoticeParagraph(
                    text = "浣犲彲浠ラ€夋嫨绔嬪嵆杩涘叆瀛︿範鏁版嵁鐪嬫澘,鎴栫户缁暀鍦ㄥ師椤点€?,
                    uiScale = uiScale
                )
                Spacer(Modifier.height(ShirohaSpacing.Sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)
                ) {
                    ActionPillButton(
                        icon = Icons.Rounded.QueryStats,
                        text = "杩涘叆瀛︿範鏁版嵁鐪嬫澘",
                        primary = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        fillWidthContent = true,
                        onClick = onGoStatistics
                    )
                    ActionPillButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        text = "鐣欏湪鍘熼〉",
                        primary = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        fillWidthContent = true,
                        onClick = { /* 鍙栨秷璺宠浆:涓嶅仛浠讳綍璺宠浆 */ }
                    )
                }
            }

            // === 搴曢儴:鍙栨秷璺宠浆 / 鍏抽棴 ===
            Spacer(Modifier.height(ShirohaSpacing.Sm))
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                text = "鍏抽棴鎻愮ず",
                primary = false,
                modifier = Modifier.height(44.dp),
                onClick = { /* 鍏抽棴鎻愮ず:涓嶅仛浠讳綍璺宠浆 */ }
            )
        }
    }
}

@Composable
private fun HomeNoticeParagraph(
    text: String,
    uiScale: Float
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value * uiScale).sp
        ),
        color = ShirohaColors.TextSecondary
    )
}

@Composable
private fun TriggerCaseRow(
    icon: ImageVector,
    title: String,
    desc: String,
    active: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (active) MaterialTheme.colorScheme.primary else ShirohaColors.TextTertiary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(ShirohaSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (active) MaterialTheme.colorScheme.onSurface else ShirohaColors.TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = ShirohaColors.TextSecondary
            )
        }
    }
}

/**
 * 瑙﹀彂鏉′欢涓嬬殑鍔ㄦ€佹爣棰樻枃妗?鏍规嵁褰撳墠浠撳簱鐘舵€佺粰鍑轰笉鍚屾彁绀恒€?
 */
private fun resolveTriggerTitle(): String {
    return if (QuizRepository.banks.isEmpty()) {
        "棣栭〉宸插崌绾?
    } else {
        "瀛︿範鏁版嵁鐪嬫澘宸插氨缁?
    }
}

private fun resolveTriggerSubtitle(): String {
    return if (QuizRepository.banks.isEmpty()) {
        "鏈湴杩樻病鏈夐搴?鍏堝幓瀵煎叆鍐嶅洖鍒伴椤电粌涔犲惂銆?
    } else {
        "瀛︿範鏁版嵁鐪嬫澘鎻愪緵瀹屾暣鐨勫涔犳€昏涓庣粺璁″姛鑳姐€?
    }
}

