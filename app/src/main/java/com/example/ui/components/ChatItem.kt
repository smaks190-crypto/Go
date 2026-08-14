package com.example.ui.components

import com.example.data.db.NotificationEntity

data class ExtractedOp(
    val type: String,
    val category: String,
    val subcategory: String,
    val amount: Double
)

data class NotifParsedInfo(
    val ops: List<ExtractedOp>,
    val userPhrase: String,
    val comment: String
)

/**
 * Parses operation data and comments from a NotificationEntity description string.
 */
fun extractOpsAndComment(notification: NotificationEntity): NotifParsedInfo {
    val ops = mutableListOf<ExtractedOp>()
    var userPhrase = ""
    var comment = ""
    try {
        if (notification.description.startsWith("||")) {
            val parts = notification.description.split("||")
            if (parts.size >= 3) {
                if (parts[1] == "MULTI") {
                    val opsRaw = parts[2].split(";")
                    for (opRaw in opsRaw) {
                        val opParts = opRaw.split("|")
                        if (opParts.size >= 4) {
                            ops.add(
                                ExtractedOp(
                                    type = opParts[0],
                                    category = opParts[1],
                                    subcategory = opParts[2],
                                    amount = opParts[3].toDoubleOrNull() ?: 0.0
                                )
                            )
                        }
                    }
                    if (parts.size >= 5) {
                        userPhrase = parts[3]
                        comment = parts[4]
                    } else if (parts.size >= 4) {
                        comment = parts[3]
                    }
                } else {
                    val txParts = parts[1].split("|")
                    if (txParts.size >= 4) {
                        ops.add(
                            ExtractedOp(
                                type = txParts[0],
                                category = txParts[1],
                                subcategory = txParts[2],
                                amount = txParts[3].toDoubleOrNull() ?: 0.0
                            )
                        )
                        if (txParts.size >= 5) {
                            userPhrase = txParts[4]
                        }
                    }
                    comment = parts[2]
                }
            }
        } else {
            ops.add(ExtractedOp("expense", "Прочее", notification.title, 0.0))
            comment = notification.description
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return NotifParsedInfo(ops, userPhrase, comment)
}

/**
 * Splits audit / AI response text into readable conversational display blocks.
 * Resilient against various markdown, bold, numbered, and natural language header formats.
 */
fun splitIntoSections(auditText: String): List<String> {
    if (auditText.isBlank() || auditText == "ERROR_NO_CONNECTION") return emptyList()

    val trimmed = auditText.trim()

    // 1. Check if separated by horizontal rules (--- or *** or ___)
    val hrRegex = Regex("""(?m)^(?:\s*[-*_]\s*){3,}${'$'}""")
    if (hrRegex.containsMatchIn(trimmed)) {
        val hrBlocks = trimmed.split(hrRegex)
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "ERROR_NO_CONNECTION" }
        if (hrBlocks.size > 1) return hrBlocks
    }

    // 2. Comprehensive header matching: Markdown #, bold titles **Title**, numbered headers 1., and financial keywords
    val headerPattern = "(?m)^(?=" +
        "#{1,6}\\s+|" +
        "\\*\\*(?:Главный [Вв]ердикт|Вердикт|Цифры|Динамика|Прожарка|Ачивки|Достижения|Выводы|Рекомендации|Советы|План|Анализ|Категории|Итоги|Резюме|Инсайты|Оценка|Структура)[^*]*\\*\\*|" +
        "(?i)(?:Главный Вердикт|Вердикт|Цифры и Динамика|Прожарка трат|Прожарка|Ачивки периода|Ачивки|Выводы и Советы|Выводы|Ключевые инсайты|Анализ категорий|Рекомендации Давида|Финансовый вердикт|Итоги периода):?|" +
        "\\d+\\.\\s+(?:[А-ЯЁA-Z]|\\*\\*)" +
        ")"

    val headerRegex = Regex(headerPattern)
    val rawBlocks = trimmed.split(headerRegex)
        .map { it.trim() }
        .filter { it.isNotBlank() && it != "ERROR_NO_CONNECTION" }

    if (rawBlocks.size > 1) {
        return rawBlocks
    }

    // 3. Fallback: If no explicit headers found but text is long, split by double newlines into logical cards
    if (trimmed.length > 280 && trimmed.contains("\n\n")) {
        val paragraphBlocks = trimmed.split(Regex("""\n{2,}"""))
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "ERROR_NO_CONNECTION" }
        if (paragraphBlocks.size > 1) {
            return paragraphBlocks
        }
    }

    return listOf(trimmed)
}

enum class ChatFeedFilter(val label: String, val iconEmoji: String) {
    ALL("Все", "💬"),
    DIALOGUE("Диалог и Аудит", "💡"),
    OPERATIONS("Операции", "📋")
}

enum class ChatCategory {
    DIALOGUE,
    OPERATION,
    SYSTEM
}

sealed class ChatItem {
    abstract val timestamp: Long
    abstract val id: String
    open val isFromUser: Boolean get() = false
    open val isRead: Boolean get() = true
    open val category: ChatCategory get() = ChatCategory.DIALOGUE
}

data class ChatWelcomeItem(
    override val timestamp: Long = System.currentTimeMillis()
) : ChatItem() {
    override val id: String = "welcome_$timestamp"
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatChangelogItem(
    override val timestamp: Long = System.currentTimeMillis()
) : ChatItem() {
    override val id: String = "changelog_$timestamp"
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatAuditOfferItem(
    override val timestamp: Long = 1000L
) : ChatItem() {
    override val id: String = "offer_$timestamp"
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatUnreadSeparatorItem(
    override val timestamp: Long
) : ChatItem() {
    override val id: String = "unread_sep_$timestamp"
    override val category: ChatCategory = ChatCategory.SYSTEM
}

data class ChatNotificationUserItem(
    val notification: NotificationEntity
) : ChatItem() {
    override val timestamp: Long = notification.timestamp
    override val id: String = "notif_user_${notification.id}"
    override val isFromUser: Boolean get() = true
    override val isRead: Boolean get() = true
    override val category: ChatCategory = ChatCategory.OPERATION
}

data class ChatNotificationDavidItem(
    val notification: NotificationEntity
) : ChatItem() {
    override val timestamp: Long = notification.timestamp + 10L
    override val id: String = "notif_david_${notification.id}"
    override val isFromUser: Boolean get() = false
    override val isRead: Boolean get() = notification.isRead
    override val category: ChatCategory = ChatCategory.OPERATION
}

data class ChatAuditRequestItem(
    override val timestamp: Long,
    val text: String = "",
    val fileName: String? = null,
    val hasError: Boolean = false
) : ChatItem() {
    override val id: String = "req_$timestamp"
    override val isFromUser: Boolean get() = true
    override val isRead: Boolean get() = true
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatAuditSystemItem(
    override val timestamp: Long
) : ChatItem() {
    override val id: String = "sys_$timestamp"
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatAuditBlockItem(
    override val timestamp: Long,
    val text: String,
    val isFirst: Boolean
) : ChatItem() {
    override val id: String = "block_${timestamp}_${text.hashCode()}"
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatAuditRetryItem(
    override val timestamp: Long
) : ChatItem() {
    override val id: String = "retry_$timestamp"
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatTypingItem(
    override val timestamp: Long,
    val type: String = "audit"
) : ChatItem() {
    override val id: String = "typing_${type}_$timestamp"
    override val category: ChatCategory = ChatCategory.DIALOGUE
}

data class ChatConnectingItem(
    override val timestamp: Long,
    val isRestored: Boolean = false
) : ChatItem() {
    override val id: String = "connecting_$timestamp"
    override val category: ChatCategory = ChatCategory.SYSTEM
}
