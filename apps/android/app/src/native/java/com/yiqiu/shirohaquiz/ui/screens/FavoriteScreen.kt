package com.yiqiu.shirohaquiz.ui.screens

import com.yiqiu.shirohaquiz.ui.theme.shirohaEditorialBackground

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiqiu.shirohaquiz.R
import com.yiqiu.shirohaquiz.importer.model.QuestionType
import com.yiqiu.shirohaquiz.state.DEFAULT_BANK_GROUP_NAME
import com.yiqiu.shirohaquiz.state.FavoriteQuestionEntry
import com.yiqiu.shirohaquiz.state.QuizBank
import com.yiqiu.shirohaquiz.state.QuizRepository
import com.yiqiu.shirohaquiz.ui.components.ActionPillButton
import com.yiqiu.shirohaquiz.ui.components.EditorialDivider
import com.yiqiu.shirohaquiz.ui.components.EditorialFigure
import com.yiqiu.shirohaquiz.ui.components.EditorialSection
import com.yiqiu.shirohaquiz.ui.components.EmptyStateIllustration
import com.yiqiu.shirohaquiz.ui.components.GlassCard
import com.yiqiu.shirohaquiz.ui.components.QuestionImagesBlock
import com.yiqiu.shirohaquiz.ui.components.ShirohaHeader
import com.yiqiu.shirohaquiz.ui.components.StatusChip
import com.yiqiu.shirohaquiz.ui.components.shirohaNoRippleClickable
import com.yiqiu.shirohaquiz.ui.theme.ShirohaColors
import com.yiqiu.shirohaquiz.ui.theme.ShirohaDimens
import com.yiqiu.shirohaquiz.ui.theme.ShirohaRadius
import com.yiqiu.shirohaquiz.ui.theme.ShirohaSpacing
import com.yiqiu.shirohaquiz.ui.theme.editorialScaleFor
import com.yiqiu.shirohaquiz.ui.theme.screenClassFor
import com.yiqiu.shirohaquiz.ui.theme.uiScaleFor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FAVORITE_SCOPE_ALL = "all"
private const val FAVORITE_SCOPE_BANK_PREFIX = "bank:"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FavoriteScreen(
    onBack: () -> Unit,
    onGoPractice: () -> Unit
) {
    val banks = QuizRepository.banks.toList()
    val favorites = QuizRepository.favoriteQuestions.toList()
    var selectedScopeKey by rememberSaveable { mutableStateOf(FAVORITE_SCOPE_ALL) }
    var showScopeDialog by remember { mutableStateOf(false) }
    var expandedFavoriteKey by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedBankId = selectedScopeKey
        .takeIf { it.startsWith(FAVORITE_SCOPE_BANK_PREFIX) }
        ?.removePrefix(FAVORITE_SCOPE_BANK_PREFIX)
    val selectedBank = selectedBankId?.let { id -> banks.firstOrNull { it.id == id } }
    val effectiveScopeKey = if (selectedBankId == null || selectedBank != null) {
        selectedScopeKey
    } else {
        FAVORITE_SCOPE_ALL
    }
    val visibleFavorites = selectedBank?.let { bank ->
        favorites.filter { it.bankId == bank.id }
    } ?: favorites

    val categoryCount = remember(favorites) {
        favorites.mapNotNull { it.question.category?.ifBlank { null } }.distinct().size
    }

    if (showScopeDialog) {
        FavoriteScopeDialog(
            banks = banks,
            favorites = favorites,
            selectedScopeKey = effectiveScopeKey,
            onSelect = { key ->
                selectedScopeKey = key
                showScopeDialog = false
            },
            onDismiss = { showScopeDialog = false }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .shirohaEditorialBackground()
    ) {
        val screenClass = screenClassFor(maxWidth)
        val scale = editorialScaleFor(screenClass)
        val uiScale = uiScaleFor(screenClass)

        if (favorites.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
            ) {
                ShirohaHeader(
                    kicker = "Favorites",
                    title = "鏀惰棌澶",
                    subtitle = "闆嗕腑鏌ョ湅鍜岀粌涔犱綘涓诲姩鏍囪鐨勯鐩€",
                    scale = scale
                )
                EmptyStateIllustration(
                    title = "鏀惰棌澶硅繕鏄┖鐨",
                    message = "缁冧範鏃剁偣鍑婚鐩彸涓婅鏄熸爣鍗冲彲鏀惰棌銆",
                    imageRes = R.drawable.illus_empty_state_webp,
                    action = {
                        Spacer(Modifier.height(14.dp))
                        ActionPillButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            text = "杩斿洖棣栭〉",
                            primary = true,
                            modifier = Modifier.height(44.dp),
                            onClick = onBack
                        )
                    }
                )
            }
            return@BoxWithConstraints
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ShirohaSpacing.Xl, vertical = ShirohaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
        ) {
            // === Header + 缁熻 + 绛涢€?+ 鎿嶄綔 ===
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ShirohaSpacing.Lg)
                ) {
                    ShirohaHeader(
                        kicker = "Favorites",
                        title = "鏀惰棌澶",
                        subtitle = "闆嗕腑鏌ョ湅鍜岀粌涔犱綘涓诲姩鏍囪鐨勯鐩€",
                        scale = scale
                    )

                    // 椤堕儴缁熻:EditorialFigure 脳2(鏀惰棌棰樻暟 / 鍒嗙被鏁?
                    EditorialSection(
                        kicker = "Overview",
                        title = "姒傝",
                        scale = scale
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ShirohaSpacing.Xl)
                        ) {
                            EditorialFigure(
                                modifier = Modifier.weight(1f),
                                scale = scale,
                                value = "${favorites.size}",
                                label = "鏀惰棌棰樻暟",
                                unit = "棰"
                            )
                            EditorialFigure(
                                modifier = Modifier.weight(1f),
                                scale = scale,
                                value = "$categoryCount",
                                label = "瑕嗙洊鍒嗙被",
                                unit = "绫"
                            )
                        }
                    }

                    // 绛涢€夊尯
                    EditorialSection(
                        kicker = "Filter",
                        title = "绛涢€",
                        scale = scale
                    ) {
                        Text(
                            text = if (selectedBank == null) {
                                "鏀惰棌 ${favorites.size} 棰"
                            } else {
                                "鏀惰棌 ${visibleFavorites.size} / ${favorites.size} 棰"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = (MaterialTheme.typography.titleLarge.fontSize.value * uiScale).sp
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(ShirohaSpacing.Xs))
                        Text(
                            text = "鏀惰棌鏁版嵁缁熶竴淇濆瓨锛屽彲鎸夊叏閮ㄩ搴撴垨鍗曚釜棰樺簱鏌ョ湅鍜岀粌涔犮€",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value * uiScale).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(ShirohaSpacing.Sm))
                        Text(
                            text = "鏀惰棌鑼冨洿",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = (MaterialTheme.typography.labelLarge.fontSize.value * uiScale).sp
                            ),
                            color = ShirohaColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(ShirohaSpacing.Sm))
                        FavoriteScopeSelector(
                            selectedBank = selectedBank,
                            visibleCount = visibleFavorites.size,
                            totalCount = favorites.size,
                            uiScale = uiScale,
                            onClick = { showScopeDialog = true }
                        )
                        Spacer(Modifier.height(ShirohaSpacing.Sm))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ActionPillButton(
                                icon = Icons.Rounded.PlayArrow,
                                text = if (selectedBank == null) "缁冧範鏀惰棌棰? else "缁冧範褰撳墠 ${visibleFavorites.size} 棰?,
                                primary = true,
                                enabled = visibleFavorites.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                fillWidthContent = true,
                                onClick = {
                                    if (QuizRepository.startFavoritePractice(visibleFavorites)) {
                                        onGoPractice()
                                    }
                                }
                            )
                            ActionPillButton(
                                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                text = "杩斿洖",
                                primary = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                fillWidthContent = true,
                                onClick = onBack
                            )
                        }
                    }
                }
            }

            // === 鍒楄〃 ===
            if (visibleFavorites.isEmpty()) {
                item {
                    EmptyStateIllustration(
                        title = "褰撳墠棰樺簱鏆傛棤鏀惰棌棰",
                        message = "鍙互鍒囨崲鍒板叾浠栭搴撴垨鍏ㄩ儴棰樺簱銆",
                        imageRes = R.drawable.illus_empty_state_webp
                    )
                }
            } else {
                item {
                    EditorialSection(
                        kicker = "Items",
                        title = "鏀惰棌棰",
                        scale = scale
                    ) {
                        Text(
                            text = "鐐瑰嚮棰樺崱鍙睍寮€绛旀涓庤В鏋愶紱鐐瑰嚮鏄熸爣绉婚櫎鏀惰棌銆",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (MaterialTheme.typography.bodySmall.fontSize.value * uiScale).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(
                    items = visibleFavorites,
                    key = { entry -> "${entry.bankId}#${entry.question.id}" }
                ) { entry ->
                    val entryKey = "${entry.bankId}#${entry.question.id}"
                    FavoriteQuestionPreview(
                        entry = entry,
                        expanded = expandedFavoriteKey == entryKey,
                        onToggleExpand = {
                            expandedFavoriteKey = if (expandedFavoriteKey == entryKey) null else entryKey
                        },
                        scale = scale,
                        uiScale = uiScale,
                        onPractice = {
                            if (QuizRepository.startFavoritePractice(listOf(entry))) {
                                onGoPractice()
                            }
                        }
                    )
                }
            }
        }
    }
}


 * 鏀惰棌鑼冨洿閫夋嫨鍣?Surface(Card) + Row(鏍囬 + 鍓枃 + 灞曞紑鍥炬爣)
 */
@Composable
private fun FavoriteScopeSelector(
    selectedBank: QuizBank?,
    visibleCount: Int,
    totalCount: Int,
    uiScale: Float,
    onClick: () -> Unit
) {
    val smallFontSize = (MaterialTheme.typography.bodySmall.fontSize.value * uiScale).sp
    val titleFontSize = (MaterialTheme.typography.titleSmall.fontSize.value * uiScale).sp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Md),
        color = ShirohaColors.CardWhite86,
        border = BorderStroke(ShirohaDimens.Hairline, ShirohaColors.LineStrong)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedBank?.name ?: "鍏ㄩ儴棰樺簱",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = titleFontSize),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = selectedBank?.let { bank ->
                        val groupName = bank.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME }
                        "$groupName 路 鏀惰棌 $visibleCount 棰"
                    } ?: "褰撳墠鏄剧ず鍏ㄩ儴棰樺簱鐨?$totalCount 閬撴敹钘",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = smallFontSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = "閫夋嫨鏀惰棌鑼冨洿",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun FavoriteScopeDialog(
    banks: List<QuizBank>,
    favorites: List<FavoriteQuestionEntry>,
    selectedScopeKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val favoriteCountByBank = favorites.groupingBy { it.bankId }.eachCount()
    val groupedBanks = banks
        .groupBy { it.groupName.ifBlank { DEFAULT_BANK_GROUP_NAME } }
        .entries
        .sortedBy { entry -> if (entry.key == DEFAULT_BANK_GROUP_NAME) "" else entry.key }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("閫夋嫨鏀惰棌鑼冨洿") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FavoriteScopeOption(
                    title = "鍏ㄩ儴棰樺簱",
                    desc = "鍏?${favorites.size} 閬撴敹钘",
                    selected = selectedScopeKey == FAVORITE_SCOPE_ALL,
                    onClick = { onSelect(FAVORITE_SCOPE_ALL) }
                )
                groupedBanks.forEach { entry ->
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.labelLarge,
                        color = ShirohaColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    entry.value.forEach { bank ->
                        val key = FAVORITE_SCOPE_BANK_PREFIX + bank.id
                        FavoriteScopeOption(
                            title = bank.name,
                            desc = "鏀惰棌 ${favoriteCountByBank[bank.id] ?: 0} 棰?路 鍏?${bank.questions.size} 棰",
                            selected = selectedScopeKey == key,
                            onClick = { onSelect(key) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("鍏抽棴") }
        }
    )
}

@Composable
private fun FavoriteScopeOption(
    title: String,
    desc: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shirohaNoRippleClickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ShirohaRadius.Md),
        color = if (selected) ShirohaColors.BrandPrimarySoft else Color.Transparent,
        border = BorderStroke(
            ShirohaDimens.Hairline,
            if (selected) ShirohaColors.LineSelected else ShirohaColors.LineSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "宸查€夋嫨",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteQuestionPreview(
    entry: FavoriteQuestionEntry,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    scale: Float,
    uiScale: Float,
    onPractice: () -> Unit
) {
    val titleFontSize = (MaterialTheme.typography.titleMedium.fontSize.value * uiScale).sp
    val bodyFontSize = (MaterialTheme.typography.bodyMedium.fontSize.value * uiScale).sp
    val smallFontSize = (MaterialTheme.typography.bodySmall.fontSize.value * uiScale).sp

    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shirohaNoRippleClickable(onClick = onToggleExpand),
            verticalAlignment = Alignment.Top
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(typeLabel(entry.question.type))
                StatusChip(entry.bankName)
                StatusChip("鏀惰棌 ${formatTimestamp(entry.favoritedAt)}")
            }
            IconButton(onClick = { QuizRepository.removeFavoriteQuestion(entry) }) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "鍙栨秷鏀惰棌",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = favoriteQuestionDisplayTitle(entry),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize),
            fontWeight = FontWeight.SemiBold
        )
        if (entry.question.images.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            QuestionImagesBlock(
                images = entry.question.images,
                maxPreviewHeight = 260.dp,
                showMeta = false
            )
        }
        // === 鎶樺彔 / 灞曞紑绛旀 ===
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            EditorialDivider(label = "Answer")
            Spacer(Modifier.height(10.dp))
            if (entry.question.options.isNotEmpty()) {
                entry.question.options.forEach { option ->
                    Text(
                        text = "${option.key}. ${option.text}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = bodyFontSize)
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = "姝ｇ‘绛旀锛${entry.question.answer.joinToString(" / ").ifBlank { "鏈瘑鍒瓟妗" }}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = bodyFontSize)
            )
            if (entry.question.analysis.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "瑙ｆ瀽锛${entry.question.analysis}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = smallFontSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionPillButton(
                    icon = Icons.Rounded.PlayArrow,
                    text = "缁冧範鏈",
                    primary = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    fillWidthContent = true,
                    onClick = onPractice
                )
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "鐐瑰嚮棰樺崱灞曞紑绛旀涓庤В鏋",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = smallFontSize),
                color = ShirohaColors.TextSecondary
            )
        }
    }
}

private fun favoriteQuestionDisplayTitle(entry: FavoriteQuestionEntry): String {
    val number = entry.question.number.trim()
    val stem = entry.question.question.trim()
    val safeNumber = number.takeUnless(::isSuspiciousFavoriteQuestionNumber)

    return when {
        safeNumber.isNullOrBlank() -> stem
        stem.isBlank() -> safeNumber
        else -> "$safeNumber. $stem"
    }
}

private fun isSuspiciousFavoriteQuestionNumber(number: String): Boolean {
    if (number.isBlank()) return false
    val compact = number.replace(Regex("""\s+"""), "")
    if (compact.length > 24) return true

    val numericParts = Regex("""\d+""").findAll(compact).count()
    val dashCount = compact.count { it == '-' || it == '锛? || it == '鈥? || it == '鈥? }
    return numericParts >= 4 && dashCount >= 3
}

private fun typeLabel(type: QuestionType): String = when (type) {
    QuestionType.SINGLE -> "鍗曢€夐"
    QuestionType.MULTIPLE -> "澶氶€夐"
    QuestionType.JUDGE -> "鍒ゆ柇棰"
    QuestionType.BLANK -> "濉┖棰"
    QuestionType.SHORT -> "绠€绛旈"
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
