package tech.zhifu.app.myhub.feature.capture.review

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

data class CodeHighlightColors(
    val text: Color,
    val string: Color,
    val number: Color,
    val keyword: Color,
    val comment: Color,
    val symbol: Color
)

private data class HighlightToken(val start: Int, val end: Int, val style: SpanStyle, val priority: Int)

fun highlightCode(code: String, language: String?, colors: CodeHighlightColors): AnnotatedString {
    if (code.isBlank()) return AnnotatedString(code)
    return when (language?.lowercase()) {
        "json" -> highlightJson(code, colors)
        "kotlin" -> highlightCStyle(code, colors, KotlinKeywords)
        "java" -> highlightCStyle(code, colors, JavaKeywords)
        "javascript", "typescript", "js", "ts" -> highlightCStyle(code, colors, JsKeywords)
        "python", "py" -> highlightPython(code, colors)
        else -> AnnotatedString(code)
    }
}

private fun highlightJson(code: String, colors: CodeHighlightColors): AnnotatedString {
    val tokens = mutableListOf<HighlightToken>()
    addTokens(tokens, code, Regex("\"(?:\\\\.|[^\"\\\\])*\""), SpanStyle(color = colors.string), priority = 3)
    addTokens(tokens, code, Regex("-?\\d+(?:\\.\\d+)?"), SpanStyle(color = colors.number), priority = 2)
    addTokens(tokens, code, Regex("\\b(true|false|null)\\b"), SpanStyle(color = colors.keyword), priority = 2)
    addTokens(tokens, code, Regex("[{}\\[\\]:,]"), SpanStyle(color = colors.symbol), priority = 1)
    return buildAnnotated(code, tokens)
}

private fun highlightCStyle(
    code: String,
    colors: CodeHighlightColors,
    keywords: Set<String>
): AnnotatedString {
    val tokens = mutableListOf<HighlightToken>()
    addTokens(tokens, code, Regex("//.*"), SpanStyle(color = colors.comment), priority = 4)
    addTokens(tokens, code, Regex("/\\*[\\s\\S]*?\\*/"), SpanStyle(color = colors.comment), priority = 4)
    addTokens(tokens, code, Regex("\"(?:\\\\.|[^\"\\\\])*\""), SpanStyle(color = colors.string), priority = 3)
    addTokens(tokens, code, Regex("'(?:\\\\.|[^'\\\\])*'"), SpanStyle(color = colors.string), priority = 3)
    addTokens(tokens, code, Regex("\\b\\d+(?:\\.\\d+)?\\b"), SpanStyle(color = colors.number), priority = 2)
    addTokens(tokens, code, keywordRegex(keywords), SpanStyle(color = colors.keyword), priority = 2)
    addTokens(tokens, code, Regex("[{}\\[\\]();.,]"), SpanStyle(color = colors.symbol), priority = 1)
    return buildAnnotated(code, tokens)
}

private fun highlightPython(code: String, colors: CodeHighlightColors): AnnotatedString {
    val tokens = mutableListOf<HighlightToken>()
    addTokens(tokens, code, Regex("#.*"), SpanStyle(color = colors.comment), priority = 4)
    addTokens(tokens, code, Regex("\"\"\"[\\s\\S]*?\"\"\""), SpanStyle(color = colors.string), priority = 3)
    addTokens(tokens, code, Regex("'''[\\s\\S]*?'''"), SpanStyle(color = colors.string), priority = 3)
    addTokens(tokens, code, Regex("\"(?:\\\\.|[^\"\\\\])*\""), SpanStyle(color = colors.string), priority = 3)
    addTokens(tokens, code, Regex("'(?:\\\\.|[^'\\\\])*'"), SpanStyle(color = colors.string), priority = 3)
    addTokens(tokens, code, Regex("\\b\\d+(?:\\.\\d+)?\\b"), SpanStyle(color = colors.number), priority = 2)
    addTokens(tokens, code, keywordRegex(PythonKeywords), SpanStyle(color = colors.keyword), priority = 2)
    addTokens(tokens, code, Regex("[{}\\[\\]();.,]"), SpanStyle(color = colors.symbol), priority = 1)
    return buildAnnotated(code, tokens)
}

private fun addTokens(
    tokens: MutableList<HighlightToken>,
    code: String,
    regex: Regex,
    style: SpanStyle,
    priority: Int
) {
    regex.findAll(code).forEach { match ->
        tokens.add(HighlightToken(match.range.first, match.range.last + 1, style, priority))
    }
}

private fun buildAnnotated(code: String, tokens: List<HighlightToken>): AnnotatedString {
    val sorted = tokens.sortedWith(compareBy<HighlightToken> { it.priority }.thenBy { it.start })
    return buildAnnotatedString {
        append(code)
        for (token in sorted) {
            if (token.start < token.end && token.end <= code.length) {
                addStyle(token.style, token.start, token.end)
            }
        }
    }
}

private fun keywordRegex(keywords: Set<String>): Regex {
    val pattern = keywords.joinToString("|") { Regex.escape(it) }
    return Regex("\\b($pattern)\\b")
}

private val KotlinKeywords = setOf(
    "package", "import", "class", "interface", "object", "fun", "val", "var", "if", "else", "when",
    "for", "while", "return", "true", "false", "null", "try", "catch", "finally", "throw", "new",
    "in", "is", "as", "this", "super"
)

private val JavaKeywords = setOf(
    "package", "import", "class", "interface", "extends", "implements", "public", "private", "protected",
    "static", "final", "void", "int", "long", "double", "float", "boolean", "char", "new", "if", "else",
    "switch", "case", "for", "while", "do", "return", "try", "catch", "finally", "throw", "true", "false", "null"
)

private val JsKeywords = setOf(
    "const", "let", "var", "function", "class", "extends", "new", "return", "if", "else", "switch",
    "case", "for", "while", "do", "break", "continue", "try", "catch", "finally", "throw", "true", "false",
    "null", "undefined", "import", "export", "from", "async", "await"
)

private val PythonKeywords = setOf(
    "def", "class", "return", "if", "elif", "else", "for", "while", "break", "continue", "try", "except",
    "finally", "raise", "True", "False", "None", "import", "from", "as", "with", "lambda"
)
