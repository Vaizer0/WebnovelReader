package my.noveldoksuha.coreui.components.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

internal fun luaHighlight(text: String, colors: SyntaxColors): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    val taken = BooleanArray(text.length)
    val spans = mutableListOf<Pair<IntRange, SpanStyle>>()

    fun add(regex: Regex, style: SpanStyle, groupIndex: Int = 0) {
        for (match in regex.findAll(text)) {
            val group = match.groups[groupIndex] ?: continue
            val range = group.range
            if (range.first > range.last) continue
            if ((range.first..range.last).any { taken[it] }) continue
            spans.add(range to style)
            for (i in range.first..range.last) taken[i] = true
        }
    }

    add(LUA_BLOCK_COMMENT_REGEX, SpanStyle(color = colors.comment))
    add(LINE_COMMENT_REGEX, SpanStyle(color = colors.comment))
    add(LUA_STRING_REGEX, SpanStyle(color = colors.string))
    add(STRING_REGEX, SpanStyle(color = colors.string))
    add(NUMBER_REGEX, SpanStyle(color = colors.number))

    val keywordPattern = Regex("""\b(${LUA_KEYWORDS.joinToString("|") { Regex.escape(it) }})\b""")
    add(keywordPattern, SpanStyle(color = colors.keyword))

    val hooksPattern = Regex("""\b(${NOVELA_LUA_HOOKS.joinToString("|") { Regex.escape(it) }})\b""")
    add(hooksPattern, SpanStyle(color = colors.type))

    val builtinPattern = Regex("""\b(${LUA_BUILTIN_FUNCTIONS.joinToString("|") { Regex.escape(it) }})\b""")
    add(builtinPattern, SpanStyle(color = colors.function))

    val modulesPattern = Regex("""\b(${LUA_STANDARD_MODULES.joinToString("|") { Regex.escape(it) }})\b""")
    add(modulesPattern, SpanStyle(color = colors.type))

    add(FUNCTION_CALL_REGEX, SpanStyle(color = colors.function), groupIndex = 1)
    add(TYPE_REGEX, SpanStyle(color = colors.type), groupIndex = 1)
    add(LUA_OPERATOR_REGEX, SpanStyle(color = colors.operator))

    spans.sortBy { it.first.first }

    val builder = AnnotatedString.Builder(text)
    for ((range, style) in spans) {
        builder.addStyle(style, range.first, range.last + 1)
    }
    return builder.toAnnotatedString()
}

private val LUA_KEYWORDS = setOf(
    "and", "break", "do", "else", "elseif", "end", "false", "for", "function", "goto",
    "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true",
    "until", "while"
)

internal val LUA_BUILTIN_FUNCTIONS = setOf(
    "assert", "collectgarbage", "dofile", "error", "getmetatable",
    "ipairs", "load", "loadfile", "next", "pairs", "pcall", "print",
    "rawequal", "rawget", "rawlen", "rawset", "select", "setmetatable",
    "tonumber", "tostring", "type", "xpcall", "require", "_G", "_VERSION",
    "http_get", "http_post", "http_get_batch",
    "get_cookies", "set_cookies", "get_preference", "set_preference",
    "aes_decrypt", "base64_decode", "base64_encode",
    "html_parse", "html_select", "html_select_first", "html_attr", "html_text", "html_remove",
    "url_encode", "url_encode_charset", "url_resolve",
    "regex_match", "regex_replace",
    "string_normalize", "string_split", "string_trim", "string_starts_with", "string_ends_with", "string_clean", "unescape_unicode",
    "json_parse", "json_stringify",
    "detect_pagination", "sleep", "log_info", "log_error", "os_time"
)

internal val NOVELA_LUA_HOOKS = setOf(
    "getCatalogList", "getCatalogSearch", "getBookTitle", "getBookCoverImageUrl",
    "getBookDescription", "getBookGenres", "getChapterList", "getChapterText",
    "getChapterListHash", "getFilterList", "getSettingsSchema", "parsePage",
    "baseUrl", "cf_options"
)

internal val LUA_STANDARD_MODULES = setOf(
    "coroutine", "debug", "io", "math", "os", "package", "string", "table", "utf8", "luajava"
)

internal val NUMBER_REGEX = Regex("\\b0[xX][0-9a-fA-F]+\\b|\\b\\d+(\\.\\d+)?[fFLl]?\\b")
internal val STRING_REGEX = Regex("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'")
internal val LUA_STRING_REGEX = Regex("\\[\\[[\\s\\S]*?\\]\\]")
internal val LINE_COMMENT_REGEX = Regex("--[^\n]*")
internal val LUA_BLOCK_COMMENT_REGEX = Regex("--\\[\\[[\\s\\S]*?\\]\\]")
internal val FUNCTION_CALL_REGEX = Regex("\\b([A-Za-z_][A-Za-z0-9_]*)\\s*(?=\\()")
internal val TYPE_REGEX = Regex("\\b([A-Z][A-Za-z0-9_]*)\\b")
internal val LUA_OPERATOR_REGEX = Regex("[+\\-*/%^#=~<>&|.:]+")
