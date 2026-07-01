package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val appMeta = rememberAppMeta(context)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass: ScreenClass = screenClassFor(maxWidth)
        val scale: Float = editorialScaleFor(screenClass)
        val uiScale: Float = uiScaleFor(screenClass)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            // === Header: kicker + 琛嚎澶ф爣棰?+ 鍓枃 ===
            ShirohaHeader(
                kicker = "About",
                title = "鍏充簬 Shiroha Quiz",
                subtitle = "涓€娆句负璁ょ湡鐨勪汉鍋氱殑棰樺簱搴旂敤",
                scale = scale
            )

            // === Hero 鍖$Shiroha 妯″紡鏃跺甫娴姩鎻掔敾 ===
            IllustrationHeroCard(
                title = "Shiroha Quiz",
                subtitle = "鎶婃瘡涓€娆＄粌涔?閮藉綋浣滀竴娆＄簿杩涖€",
                imageRes = R.drawable.illus_me_settings,
                modifier = Modifier.height(ShirohaDimens.HeroCardHeight),
                imageSize = ShirohaDimens.HeroImageSize,
                scale = scale
            )

            // === 缁熻鏁版嵁:鐗堟湰鍙?/ 鏋勫缓鏃堕棿 / 棰樺簱鏁?/ 棰樼洰鎬绘暟 ===
            EditorialSection(
                kicker = "Numbers",
                title = "搴旂敤鏁版嵁",
                scale = scale
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
                ) {
                    EditorialFigure(
                        value = appMeta.versionName,
                        label = "鐗堟湰鍙",
                        scale = scale,
                        modifier = Modifier.weight(1f)
                    )
                    EditorialFigure(
                        value = appMeta.buildTime,
                        label = "鏋勫缓鏃堕棿",
                        scale = scale,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(ShirohaSpacing.Sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
                ) {
                    EditorialFigure(
                        value = QuizRepository.banks.size.toString(),
                        label = "棰樺簱鏁",
                        unit = "濂",
                        scale = scale,
                        modifier = Modifier.weight(1f)
                    )
                    EditorialFigure(
                        value = QuizRepository.banks.sumOf { it.questions.size }.toString(),
                        label = "棰樼洰鎬绘暟",
                        unit = "棰",
                        scale = scale,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // === 椤圭洰绠€浠?闀挎枃鏈$Serif 姝ｆ枃 ===
            EditorialSection(
                kicker = "Story",
                title = "瀹冩槸浠€涔",
                scale = scale
            ) {
                AboutParagraph(
                    text = "Shiroha Quiz 鏄竴涓潰鍚戞湰鍦伴搴撳鍏ャ€佺粌涔犮€佽€冭瘯銆侀敊棰樺涔犲拰瀛︿範璁板綍绠＄悊鐨勫埛棰樺伐鍏枫€傚師鐢熷畨鍗撶増鏈噸鐐逛紭鍖栫Щ鍔ㄧ鎿嶄綔浣撻獙銆侀搴撴牳瀵瑰拰鏈湴鏁版嵁绠＄悊銆",
                    uiScale = uiScale
                )
                EditorialDivider()
                AboutParagraph(
                    text = "鎴戜滑閲囩敤缂栬緫鏉傚織椋庤璁¤瑷€,鎶婃瘡涓€娆＄粌涔?褰撲綔涓€娆＄簿杩涖€傜背榛勫簳鑹查厤琛嚎澶ф暟瀛?闀挎椂闂撮槄璇绘洿鏌斿拰銆傛墍鏈夋暟鎹潎淇濆瓨鍦ㄦ湰鍦?浣犲彲浠ラ殢鏃跺鍑哄浠芥垨娓呴櫎銆",
                    uiScale = uiScale
                )
            }

            // === 璁捐鐞嗗康 ===
            EditorialSection(
                kicker = "Design",
                title = "璁捐鐞嗗康",
                scale = scale
            ) {
                AboutParagraph(
                    text = "鏆栫焊寮犲緞鍚戞笎鍙?+ 琛嚎澶ф暟瀛?+ 鍙戜笣涓嬪垝绾?钀ラ€犳潅蹇楀皝闈㈢骇鐨勬暟鎹劅;鍗＄墖杞婚噺鍖?鐣欑櫧浼樺厛,閬垮紑绱矇娓愬彉涓庨噸鎶曞奖銆係hiroha 妯″紡寮€鍚椂,閮ㄥ垎椤甸潰浼氭樉绀烘彃鐢讳笌寮€灞忓浘銆",
                    uiScale = uiScale
                )
            }

            // === 鍔熻兘鐗规€?===
            EditorialSection(
                kicker = "Features",
                title = "鏍稿績鍔熻兘",
                scale = scale
            ) {
                AboutFeatureRow(
                    icon = Icons.Rounded.Folder,
                    title = "棰樺簱瀵煎叆",
                    desc = "鏀寔 ZIP / JSON / 鏂囨湰瀵煎叆,AI 杈呭姪閲嶆瀯涓庢牳瀵广€",
                    onClick = { uriHandler.openUri("https://github.com/reiqr/shiroha-quiz") }
                )
                EditorialDivider()
                AboutFeatureRow(
                    icon = Icons.Rounded.School,
                    title = "閿欓鏈",
                    desc = "鑷姩鏀跺綍閿欓,鏅鸿兘澶嶄範涓庢帉鎻″垽瀹氥€",
                    onClick = { uriHandler.openUri("https://github.com/reiqr/shiroha-quiz") }
                )
                EditorialDivider()
                AboutFeatureRow(
                    icon = Icons.Rounded.Star,
                    title = "鏀惰棌",
                    desc = "鏀惰棌閲嶇偣棰樼洰,闅忔椂缈荤湅涓庡洖椤俱€",
                    onClick = { uriHandler.openUri("https://github.com/reiqr/shiroha-quiz") }
                )
                EditorialDivider()
                AboutFeatureRow(
                    icon = Icons.Rounded.AutoStories,
                    title = "杈瑰杈圭瓟",
                    desc = "璇剧▼瀛︿範 + 鍗虫椂鍙嶉 + 妯℃嫙鑰冭瘯銆",
                    onClick = { uriHandler.openUri("https://github.com/reiqr/shiroha-quiz") }
                )
                EditorialDivider()
                AboutFeatureRow(
                    icon = Icons.Rounded.Description,
                    title = "AI 瑙ｆ瀽",
                    desc = "鍗曢 AI 鍒嗘瀽涓庢牳瀵?浜哄伐纭鍚庡啓鍏ャ€",
                    onClick = { uriHandler.openUri("https://github.com/reiqr/shiroha-quiz") }
                )
            }

            // === 寮€婧愯鍙?/ 鑷磋阿 ===
            EditorialSection(
                kicker = "Credits",
                title = "鑷磋阿",
                scale = scale
            ) {
                AboutParagraph(
                    text = "棰樺簱銆侀敊棰樻湰鍜屽涔犺褰曢粯璁や繚瀛樺湪鏈満銆傚彂甯冨寘涓嶅簲鍖呭惈鐢ㄦ埛鏈湴瀵煎叆鏁版嵁銆",
                    uiScale = uiScale
                )
                EditorialDivider()
                AboutParagraph(
                    text = "椤圭洰鍦板潃:https://github.com/reiqr/shiroha-quiz",
                    uiScale = uiScale
                )
                Spacer(Modifier.height(ShirohaSpacing.Sm))
                ActionPillButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    text = "鎵撳紑 GitHub 椤圭洰椤",
                    primary = true,
                    modifier = Modifier.height(44.dp),
                    onClick = { uriHandler.openUri("https://github.com/reiqr/shiroha-quiz") }
                )
            }

            // === 搴曢儴:杩斿洖 ===
            Spacer(Modifier.height(ShirohaSpacing.Sm))
            ActionPillButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                text = "杩斿洖璁剧疆",
                primary = false,
                modifier = Modifier.height(44.dp),
                onClick = onBack
            )
        }
    }
}

@Composable
private fun AboutParagraph(
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
private fun AboutFeatureRow(
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(ShirohaSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
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
 * 搴旂敤鍏冩暟鎹?鐗堟湰鍙?/ 鏋勫缓鏃堕棿),浠$PackageInfo 璇诲彇,缂哄け鏃朵娇鐢ㄥ厹搴曟枃妗堛€? */
private data class AppMeta(
    val versionName: String,
    val buildTime: String
)

private fun resolveAppMeta(context: Context): AppMeta {
    val fallbackVersion = "1.0.0"
    val fallbackBuild = "鈥"
    return runCatching {
        val pm = context.packageManager
        val pkgInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, 0)
        }
        val versionName = pkgInfo.versionName?.takeIf { it.isNotBlank() } ?: fallbackVersion
        val firstInstall = pkgInfo.firstInstallTime
        val lastUpdate = pkgInfo.lastUpdateTime
        val buildTime = if (lastUpdate > 0L) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lastUpdate))
        } else if (firstInstall > 0L) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(firstInstall))
        } else {
            fallbackBuild
        }
        AppMeta(versionName = versionName, buildTime = buildTime)
    }.getOrDefault(AppMeta(fallbackVersion, fallbackBuild))
}

@Composable
private fun rememberAppMeta(context: Context): AppMeta {
    return androidx.compose.runtime.remember(context.packageName) {
        resolveAppMeta(context)
    }
}

