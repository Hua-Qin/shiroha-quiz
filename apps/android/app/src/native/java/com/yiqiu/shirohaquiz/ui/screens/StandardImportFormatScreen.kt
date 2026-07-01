package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.IllustrationHeroCard
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor

@Composable
fun StandardImportFormatScreen(
    onBack: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            ShirohaHeader(
                kicker = "Format",
                title = "鏍囧噯瀵煎叆鏍煎紡",
                subtitle = "鎸夎繖涓牸寮忔暣鐞嗛搴擄紝璇嗗埆浼氭洿绋冲畾銆",
                scale = scale
            )

            // === Hero 鍖$IllustrationHeroCard Shiroha 妯″紡甯︽彃鐢?===
            EditorialSection(
                kicker = "Overview",
                title = "杩欏氨鏄綘鐨勯搴撳簲鏈夌殑鏍峰瓙",
                scale = scale
            ) {
                IllustrationHeroCard(
                    title = "鎶婇搴撴暣鐞嗘垚鏍囧噯鏍煎紡",
                    subtitle = "棰樺彿銆侀€夐」銆佺瓟妗堝拰瑙ｆ瀽娓呮櫚瀵归綈,璇嗗埆浼氶潪甯哥ǔ瀹氥€",
                    imageRes = R.drawable.illus_import_hint_webp,
                    scale = scale
                ) {
                    Spacer(Modifier.height(ShirohaSpacing.Md))
                    Text(
                        text = "鏍囧噯鏍煎紡 = 棰樺彿 路 棰樺共 路 閫夐」 路 绛旀 路 瑙ｆ瀽銆傛瘡琛屼竴涓瓧娈?灞傛鍒嗘槑銆",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShirohaColors.TextSecondary
                    )
                }
            }

            // === 瀛楁璇存槑 ===
            EditorialSection(
                kicker = "Format",
                title = "瀛楁璇存槑",
                scale = scale
            ) {
                FormatSection(
                    title = "涓€銆佸崟鏂囦欢鏍囧噯鏍煎紡",
                    body = "姣忛亾棰樺缓璁寘鍚鍙枫€侀骞层€侀€夐」銆佺瓟妗堝拰瑙ｆ瀽銆傞鍙峰彲浠ョ敤 1.銆?銆侊紙1锛?绛夊舰寮忥紝浣嗗悓涓€浠介搴撳敖閲忕粺涓€銆",
                    sample = """
1. 涓嬪垪鍝竴椤规槸鑹ソ瀛︿範涔犳儻锛"
A. 璇惧墠棰勪範
B. 闀挎湡鐔
C. 鎶勫啓绛旀
D. 涓嶅仛澶嶇洏
绛旀锛欰
瑙ｆ瀽锛氳鍓嶉涔犳湁鍔╀簬鎻愬墠浜嗚В閲嶇偣鍐呭銆"
                    """.trimIndent()
                )

                FormatSection(
                    title = "浜屻€佸閫夐鏍煎紡",
                    body = "澶氶€夐绛旀鍙互鍐欐垚 AB銆丄 B銆丄銆丅 鎴?A/B銆傚缓璁瓟妗堥泦涓啓鍦?绛旀锛欰CD"杩欎竴琛屻€",
                    sample = """
2. 鏁寸悊棰樺簱鏃讹紝鍝簺鍋氭硶鏈夊姪浜庢彁楂樿瘑鍒ǔ瀹氭€э紵
A. 淇濈暀娓呮櫚棰樺彿
B. 鎶婂閬撻鎸ゅ湪涓€琛"
C. 缁熶竴閫夐」鏍煎紡
D. 鍗曠嫭鍒楀嚭绛旀
绛旀锛欰CD
瑙ｆ瀽锛氶鍙枫€侀€夐」鍜岀瓟妗堣秺娓呮櫚,瀵煎叆瓒婄ǔ瀹氥€"
                    """.trimIndent()
                )

                FormatSection(
                    title = "涓夈€佸垽鏂鏍煎紡",
                    body = "鍒ゆ柇棰樺彲浣跨敤?姝ｇ‘/閿欒""瀵?閿?"鈭?脳?銆備笉寤鸿鎶婄瓟妗堟贩鍦ㄥ緢闀跨殑棰樺共涓€",
                    sample = """
3. 棰樺簱瀵煎叆鍓嶏紝缁熶竴缂栧彿鍜岄€夐」鏍煎紡鍙互鍑忓皯璇嗗埆閿欒銆傦紙 锛"
绛旀锛氭纭"
瑙ｆ瀽锛氱粺涓€鏍煎紡鏈夊埄浜庤В鏋愬櫒鍒ゆ柇棰樼洰杈圭晫銆"
                    """.trimIndent()
                )

                FormatSection(
                    title = "鍥涖€佸弻鏂囦欢瀵煎叆鏍煎紡",
                    body = "棰樼洰鏂囦欢鍙斁棰樺共鍜岄€夐」锛岀瓟妗堟枃浠舵寜棰樺彿鍒楀嚭绛旀銆傞鍙烽渶瑕佸拰棰樼洰鏂囦欢瀵瑰簲銆",
                    sample = """
棰樼洰鏂囦欢锛"
1. 绀轰緥棰樺共涓€鈥︹€"
A. 閫夐」涓€
B. 閫夐」浜"

2. 绀轰緥棰樺共浜屸€︹€"
A. 閫夐」涓€
B. 閫夐」浜"
C. 閫夐」涓"

绛旀鏂囦欢锛"
1. B
2. AC
3. 姝ｇ‘
                    """.trimIndent()
                )

                FormatSection(
                    title = "浜斻€佸噺灏戣瘑鍒敊璇殑寤鸿",
                    body = "灏介噺閬垮厤鎶婂涓鐩尋鍦ㄤ竴琛岋紱閫夐」鍓嶄繚鐣$A. B. C. D.锛涚瓟妗堝尯鍜岃В鏋愬尯淇濇寔娓呮櫚銆傚鏉傛暣鍗风湡棰樺彲浠ュ厛瀵煎叆锛屽啀杩涘叆鏍稿椤典慨姝ｃ€",
                    sample = null
                )

                FormatSection(
                    title = "鍏€佸鏉傛牸寮忓彲鍏堢敤 AI 娓呮礂",
                    body = "濡傛灉鏉ユ簮鏉愭枡鍖呭惈澶嶅埗閿欒銆佺瓟妗堥泦涓€佽В鏋愭贩鎺掋€佹壂鎻忔枃鏈垨鏁村嵎璇存槑锛屽缓璁厛鍙戠粰甯歌 AI / LLM 娓呮礂鎴愭爣鍑嗘牸寮忥紝鍐嶅鍏$App銆傛竻娲楀彧璐熻矗鏁寸悊鏍煎紡锛屼笉璐熻矗瑙ｉ銆",
                    sample = """
璇锋妸涓嬮潰鐨勯搴撴枃鏈暣鐞嗘垚 Shiroha Quiz 鍙ǔ瀹氬鍏ョ殑鏍囧噯鏍煎紡銆"

瑕佹眰锛"
1. 鍙仛鏍煎紡鏁寸悊锛屼笉瑕佽В棰橈紝涓嶈鏀瑰啓棰樻剰锛屼笉瑕佺紪閫犻鐩€侀€夐」銆佺瓟妗堟垨瑙ｆ瀽銆"
2. 淇濈暀鎵€鏈夐鐩紝鎸夊師濮嬮『搴忚緭鍑猴紱濡傛灉鍘熸枃鏈夊垎鍗枫€佺珷鑺傘€侀鍨嬪垎鍖猴紝璇蜂繚鐣欐爣棰樸€"
3. 姣忛亾棰樻暣鐞嗘垚鐙珛棰樺潡锛屾帹鑽愭牸寮忎负锛"
棰樺彿. 棰樺共
A. 閫夐」
B. 閫夐」
C. 閫夐」
D. 閫夐」
绛旀锛欰
瑙ｆ瀽锛氬師鏂囪В鏋"

4. 鍗曢€夐绛旀鍐欐垚锛氱瓟妗堬細A
5. 澶氶€夐绛旀鍐欐垚锛氱瓟妗堬細ABCD
6. 鍒ゆ柇棰樼瓟妗堝啓鎴愶細绛旀锛氭纭?鎴?绛旀锛氶敊璇"
7. 濉┖棰樸€佺畝绛旈绛旀淇濈暀鍘熸枃锛屼笉瑕佹媶鎴愰€夋嫨棰樼瓟妗堛€"
8. 濡傛灉鍘熸枃娌℃湁瑙ｆ瀽锛屼笉瑕佺紪瑙ｆ瀽锛屽彲浠ョ渷鐣ヨВ鏋愯锛屾垨鍐欙細瑙ｆ瀽锛"
9. 濡傛灉绛旀鏃犳硶浠庡師鏂囩‘璁わ紝鍐欙細绛旀锛氥€愬緟纭銆"
10. 鏈€缁堝彧杈撳嚭鏁寸悊鍚庣殑棰樺簱姝ｆ枃锛屼笉瑕佽緭鍑鸿鏄庛€佸垎鏋愭垨 Markdown 浠ｇ爜鍧椼€"

涓嬮潰鏄師濮嬫枃鏈細
銆愭妸闇€瑕佹竻娲楃殑棰樺簱绮樿创鍒拌繖閲屻€"
                    """.trimIndent()
                )
            }

            // === 澶嶅埗 / 杩斿洖 ActionPillButton Row ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Md)
            ) {
                ActionPillButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    text = "杩斿洖璁剧疆",
                    primary = false,
                    onClick = onBack
                )
            }
        }
    }
}

@Composable
private fun FormatSection(
    title: String,
    body: String,
    sample: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = ShirohaColors.TextPrimary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!sample.isNullOrBlank()) {
            // Markdown 椋庢牸浠ｇ爜鍧?绛夊瀛椾綋 + 鏆栬壊鑳屾櫙 + 鍦嗚 + 鍙戜笣杈规
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ShirohaRadius.Md),
                color = ShirohaColors.CardWhite78,
                border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = sample,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

