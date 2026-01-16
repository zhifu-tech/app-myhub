// 构建文件动态生成系统脚本插件
// 此文件包含构建文件解析、提取和生成的逻辑

/**
 * 构建文件解析器
 * 负责解析 build.gradle.kts 文件，提取平台相关的配置
 */
class BuildFileParser(private val buildFileContent: String) {
    /**
     * 解析 plugins 块
     */
    fun parsePlugins(): List<String> {
        val pluginsRegex = Regex("""plugins\s*\{([^}]+)\}""", RegexOption.DOT_MATCHES_ALL)
        val match = pluginsRegex.find(buildFileContent) ?: return emptyList()
        val pluginsBlock = match.groupValues[1]

        val pluginRegex = Regex("""alias\(libs\.plugins\.([^)]+)\)""")
        return pluginRegex.findAll(pluginsBlock).map { it.groupValues[1] }.toList()
    }

    /**
     * 解析完整的 plugins 块内容（包括所有插件声明）
     */
    fun parsePluginsBlock(): String? {
        val pluginsRegex = Regex("""plugins\s*\{([^}]+)\}""", RegexOption.DOT_MATCHES_ALL)
        val match = pluginsRegex.find(buildFileContent) ?: return null
        return match.groupValues[1]
    }

    /**
     * 从 plugins 块中提取特定格式的插件声明（如 kotlin("native.cocoapods")）
     * 包括前面的注释行（如果有）
     */
    fun extractDirectPluginDeclarations(pluginsBlock: String, pattern: Regex): List<String> {
        val result = mutableListOf<String>()
        pattern.findAll(pluginsBlock).forEach { match ->
            val matchStart = match.range.first
            val matchEnd = match.range.last
            
            // 向前查找行开始，并尝试包含前面的注释行
            var lineStart = matchStart
            var i = matchStart - 1
            while (i >= 0) {
                val char = pluginsBlock[i]
                if (char == '\n') {
                    // 检查这一行是否是注释或空行
                    val prevLineStart = i + 1
                    val prevLineEnd = lineStart
                    val prevLine = pluginsBlock.substring(prevLineStart, prevLineEnd).trim()
                    if (prevLine.isEmpty() || prevLine.startsWith("//")) {
                        lineStart = prevLineStart
                        i--
                    } else {
                        break
                    }
                } else {
                    i--
                }
            }
            
            // 向后查找行结束
            var lineEnd = matchEnd
            i = matchEnd
            while (i < pluginsBlock.length && pluginsBlock[i] != '\n') {
                i++
            }
            if (i < pluginsBlock.length) {
                lineEnd = i
            }
            
            val declaration = pluginsBlock.substring(lineStart, lineEnd)
            if (declaration.trim().isNotBlank()) {
                result.add(declaration)
            }
        }
        return result
    }

    /**
     * 提取大括号块内容（处理嵌套）
     */
    private fun extractBlock(content: String, startIndex: Int): Pair<String, Int>? {
        if (startIndex >= content.length || content[startIndex] != '{') return null

        var depth = 0
        var i = startIndex
        val start = i

        while (i < content.length) {
            when (content[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return Pair(content.substring(start + 1, i), i + 1)
                    }
                }
            }
            i++
        }
        return null
    }

    /**
     * 解析 kotlin 配置块
     */
    fun parseKotlinBlock(): String? {
        // 查找 "kotlin" 关键字，确保是独立的单词（不是其他字符串的一部分）
        val kotlinPattern = Regex("""\bkotlin\s*\{""")
        val match = kotlinPattern.find(buildFileContent) ?: return null
        val kotlinIndex = match.range.first
        val braceIndex = match.range.last

        return extractBlock(buildFileContent, braceIndex)?.first
    }

    /**
     * 解析 android 配置块（只在 kotlin 块内查找）
     */
    fun parseAndroidBlock(): String? {
        val kotlinBlock = parseKotlinBlock() ?: return null

        // 在 kotlin 块内查找 android
        val androidIndex = kotlinBlock.indexOf("android")
        if (androidIndex == -1) return null

        val braceIndex = kotlinBlock.indexOf('{', androidIndex)
        if (braceIndex == -1) return null

        return extractBlock(kotlinBlock, braceIndex)?.first
    }

    /**
     * 解析 iosTargets().forEach { ... } 块（只在 kotlin 块内查找）
     */
    fun parseIosTargetsBlock(): String? {
        val kotlinBlock = parseKotlinBlock() ?: return null

        // 在 kotlin 块内查找 iosTargets().forEach
        val iosTargetsIndex = kotlinBlock.indexOf("iosTargets().forEach")
        if (iosTargetsIndex == -1) return null

        // 找到 lambda 表达式的开始位置（{）
        val lambdaStartIndex = kotlinBlock.indexOf('{', iosTargetsIndex)
        if (lambdaStartIndex == -1) return null

        // 提取完整的 lambda 表达式块
        return extractBlock(kotlinBlock, lambdaStartIndex)?.let { (content, endIndex) ->
            // 返回完整的 iosTargets().forEach { ... } 块
            val startIndex = iosTargetsIndex
            val end = lambdaStartIndex + content.length + 2 // +2 for { and }
            kotlinBlock.substring(startIndex, end)
        }
    }

    /**
     * 解析 sourceSets 块（只在 kotlin 块内查找）
     */
    fun parseSourceSets(): String? {
        val kotlinBlock = parseKotlinBlock() ?: return null

        // 在 kotlin 块内查找 sourceSets
        val sourceSetsIndex = kotlinBlock.indexOf("sourceSets")
        if (sourceSetsIndex == -1) return null

        val braceIndex = kotlinBlock.indexOf('{', sourceSetsIndex)
        if (braceIndex == -1) return null

        return extractBlock(kotlinBlock, braceIndex)?.first
    }

    /**
     * 解析 compose.resources 配置块（顶级配置块）
     */
    fun parseComposeResourcesBlock(): String? {
        // 查找 compose.resources 或 composeResources
        val composeResourcesIndex = buildFileContent.indexOf("compose.resources")
        if (composeResourcesIndex == -1) {
            val composeResourcesAltIndex = buildFileContent.indexOf("composeResources")
            if (composeResourcesAltIndex == -1) return null

            val braceIndex = buildFileContent.indexOf('{', composeResourcesAltIndex)
            if (braceIndex == -1) return null

            return extractBlock(buildFileContent, braceIndex)?.let { (content, _) ->
                // 返回完整的 composeResources { ... } 块
                val startIndex = composeResourcesAltIndex
                val endIndex = braceIndex + content.length + 2 // +2 for { and }
                buildFileContent.substring(startIndex, endIndex)
            }
        }

        val braceIndex = buildFileContent.indexOf('{', composeResourcesIndex)
        if (braceIndex == -1) return null

        return extractBlock(buildFileContent, braceIndex)?.let { (content, _) ->
            // 返回完整的 compose.resources { ... } 块
            val startIndex = composeResourcesIndex
            val endIndex = braceIndex + content.length + 2 // +2 for { and }
            buildFileContent.substring(startIndex, endIndex)
        }
    }

    /**
     * 解析 sqldelight 配置块（顶级配置块）
     */
    fun parseSqlDelightBlock(): String? {
        // 查找 sqldelight（确保是独立的单词，不是其他字符串的一部分）
        // 使用正则表达式或更精确的查找方式
        val sqldelightPattern = Regex("""\bsqldelight\s*\{""")
        val match = sqldelightPattern.find(buildFileContent) ?: return null
        val sqldelightIndex = match.range.first
        val braceIndex = match.range.last

        return extractBlock(buildFileContent, braceIndex)?.let { (content, endIndex) ->
            // 返回完整的 sqldelight { ... } 块
            val startIndex = sqldelightIndex
            buildFileContent.substring(startIndex, endIndex)
        }
    }

    /**
     * 解析 cocoapods 配置块（只在 kotlin 块内查找）
     */
    fun parseCocoapodsBlock(): String? {
        val kotlinBlock = parseKotlinBlock() ?: return null

        // 在 kotlin 块内查找 cocoapods
        val cocoapodsPattern = Regex("""\bcocoapods\s*\{""")
        val match = cocoapodsPattern.find(kotlinBlock) ?: return null
        var cocoapodsIndex = match.range.first
        val braceIndex = match.range.last

        // 尝试包含前面的注释行（如果有的话）
        // 向前查找，直到遇到非注释、非空行的内容
        var commentStartIndex = cocoapodsIndex
        var i = cocoapodsIndex - 1
        while (i >= 0) {
            val char = kotlinBlock[i]
            if (char == '\n') {
                // 检查这一行是否是注释或空行
                val lineStart = i + 1
                val lineEnd = cocoapodsIndex
                val line = kotlinBlock.substring(lineStart, lineEnd).trim()
                if (line.isEmpty() || line.startsWith("//")) {
                    commentStartIndex = lineStart
                    i--
                } else {
                    break
                }
            } else {
                i--
            }
        }

        return extractBlock(kotlinBlock, braceIndex)?.let { (content, endIndex) ->
            // 返回完整的 cocoapods { ... } 块（包括前面的注释）
            val startIndex = commentStartIndex
            val end = braceIndex + content.length + 2 // +2 for { and }
            kotlinBlock.substring(startIndex, end)
        }
    }

    /**
     * 解析 dependencies 配置块（顶级配置块）
     * 只匹配顶级的 dependencies，不包括 kotlin 块内的 dependencies（如 commonMain.dependencies）
     */
    fun parseDependenciesBlock(): String? {
        // 先找到 kotlin 块的范围（如果存在）
        val kotlinBlockRange = kotlinBlockRange()
        
        // 查找所有 dependencies（确保是独立的单词，不是其他字符串的一部分）
        val dependenciesPattern = Regex("""\bdependencies\s*\{""")
        val allMatches = dependenciesPattern.findAll(buildFileContent)
        
        // 找到第一个不在 kotlin 块内的 dependencies
        for (match in allMatches) {
            val dependenciesIndex = match.range.first
            
            // 如果 kotlin 块存在，检查 dependencies 是否在 kotlin 块内
            if (kotlinBlockRange != null) {
                if (dependenciesIndex < kotlinBlockRange.first || dependenciesIndex >= kotlinBlockRange.last) {
                    // 这个 dependencies 不在 kotlin 块内，是顶级的
                    val braceIndex = match.range.last
                    return extractBlock(buildFileContent, braceIndex)?.let { (content, endIndex) ->
                        // 返回完整的 dependencies { ... } 块
                        val startIndex = dependenciesIndex
                        buildFileContent.substring(startIndex, endIndex)
                    }
                }
            } else {
                // 没有 kotlin 块，直接使用第一个匹配的 dependencies
                val braceIndex = match.range.last
                return extractBlock(buildFileContent, braceIndex)?.let { (content, endIndex) ->
                    // 返回完整的 dependencies { ... } 块
                    val startIndex = dependenciesIndex
                    buildFileContent.substring(startIndex, endIndex)
                }
            }
        }
        
        return null
    }
    
    /**
     * 获取 kotlin 块的范围（起始位置和结束位置）
     */
    private fun kotlinBlockRange(): IntRange? {
        val kotlinPattern = Regex("""\bkotlin\s*\{""")
        val match = kotlinPattern.find(buildFileContent) ?: return null
        val kotlinIndex = match.range.first
        val braceIndex = match.range.last
        
        // 提取 kotlin 块，获取结束位置
        val blockResult = extractBlock(buildFileContent, braceIndex) ?: return null
        val endIndex = blockResult.second
        
        return IntRange(kotlinIndex, endIndex)
    }

    /**
     * 提取特定平台的 sourceSet
     */
    fun extractPlatformSourceSet(sourceSetsContent: String, platform: String): List<String> {
        val result = mutableListOf<String>()
        val patterns = listOf("${platform}Main", "${platform}Test")

        patterns.forEach { pattern ->
            // 使用与 extractSourceSet 相同的逻辑，提取完整的 sourceSet 块
            val extracted = extractSourceSet(sourceSetsContent, pattern)
            extracted?.let { result.add(it) }
        }

        return result
    }

    /**
     * 提取 commonMain 和 commonTest
     */
    fun extractCommonSourceSets(sourceSetsContent: String): Pair<String?, String?> {
        val commonMain = extractSourceSet(sourceSetsContent, "commonMain")
        val commonTest = extractSourceSet(sourceSetsContent, "commonTest")
        return Pair(commonMain, commonTest)
    }

    private fun extractSourceSet(content: String, name: String): String? {
        val index = content.indexOf(name)
        if (index == -1) return null

        val braceIndex = content.indexOf('{', index)
        if (braceIndex == -1) return null

        // 提取完整的 sourceSet 块（从名称开始到结束大括号）
        var depth = 0
        var i = braceIndex
        while (i < content.length) {
            when (content[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        // 返回完整的 sourceSet 块，保持原始格式
                        return content.substring(index, i + 1)
                    }
                }
            }
            i++
        }
        return null
    }
}

/**
 * 平台配置提取器
 * 负责从解析结果中提取特定平台的配置
 */
class PlatformConfigExtractor(private val parser: BuildFileParser) {

    /**
     * 提取 Android 平台配置
     */
    fun extractAndroidConfig(): PlatformBuildConfig {
        val plugins = parser.parsePlugins()
        val androidPlugins = plugins.filter {
            it == "myhub.kmp" || it == "myhub.kmp.android" ||
                !it.startsWith("myhub.kmp.") // 保留非平台插件
        }

        val androidBlock = parser.parseAndroidBlock()
        val sourceSets = parser.parseSourceSets()
        val composeResourcesBlock = parser.parseComposeResourcesBlock()
        val sqldelightBlock = parser.parseSqlDelightBlock()
        val dependenciesBlock = parser.parseDependenciesBlock()

        val sourceSetList = mutableListOf<String>()
        sourceSets?.let {
            val (commonMain, commonTest) = parser.extractCommonSourceSets(it)
            commonMain?.let { sourceSetList.add(it) }
            commonTest?.let { sourceSetList.add(it) }
            parser.extractPlatformSourceSet(it, "android").forEach { sourceSetList.add(it) }
        }

        return PlatformBuildConfig(
            platform = "android",
            plugins = androidPlugins,
            androidBlock = androidBlock,
            sourceSets = sourceSetList,
            composeResourcesBlock = composeResourcesBlock,
            sqldelightBlock = sqldelightBlock,
            dependenciesBlock = dependenciesBlock
        )
    }

    /**
     * 提取 JVM 平台配置
     */
    fun extractJvmConfig(): PlatformBuildConfig {
        val plugins = parser.parsePlugins()
        val jvmPlugins = plugins.filter {
            it == "myhub.kmp" || it == "myhub.kmp.jvm" ||
                !it.startsWith("myhub.kmp.") // 保留非平台插件
        }

        val sourceSets = parser.parseSourceSets()
        val composeResourcesBlock = parser.parseComposeResourcesBlock()
        val sqldelightBlock = parser.parseSqlDelightBlock()
        val sourceSetList = mutableListOf<String>()
        sourceSets?.let {
            val (commonMain, commonTest) = parser.extractCommonSourceSets(it)
            commonMain?.let { sourceSetList.add(it) }
            commonTest?.let { sourceSetList.add(it) }
            parser.extractPlatformSourceSet(it, "jvm").forEach { sourceSetList.add(it) }
        }

        return PlatformBuildConfig(
            platform = "jvm",
            plugins = jvmPlugins,
            androidBlock = null,
            sourceSets = sourceSetList,
            composeResourcesBlock = composeResourcesBlock,
            sqldelightBlock = sqldelightBlock,
            dependenciesBlock = null
        )
    }

    /**
     * 提取 iOS 平台配置
     */
    fun extractIosConfig(): PlatformBuildConfig {
        val plugins = parser.parsePlugins()
        val iosPlugins = plugins.filter {
            it == "myhub.kmp" || it == "myhub.kmp.ios" ||
                !it.startsWith("myhub.kmp.") // 保留非平台插件
        }

        // 提取额外的插件声明（如 kotlin("native.cocoapods")）
        val pluginsBlock = parser.parsePluginsBlock()
        val extraPluginDeclarations = mutableListOf<String>()
        pluginsBlock?.let {
            // 提取 kotlin("native.cocoapods") 插件
            val cocoapodsPluginPattern = Regex("""kotlin\s*\(\s*"native\.cocoapods"\s*\)""")
            val cocoapodsPlugins = parser.extractDirectPluginDeclarations(it, cocoapodsPluginPattern)
            extraPluginDeclarations.addAll(cocoapodsPlugins)
        }

        val sourceSets = parser.parseSourceSets()
        val composeResourcesBlock = parser.parseComposeResourcesBlock()
        val iosTargetsBlock = parser.parseIosTargetsBlock()
        val cocoapodsBlock = parser.parseCocoapodsBlock()
        val sqldelightBlock = parser.parseSqlDelightBlock()
        val sourceSetList = mutableListOf<String>()
        sourceSets?.let {
            val (commonMain, commonTest) = parser.extractCommonSourceSets(it)
            commonMain?.let { sourceSetList.add(it) }
            commonTest?.let { sourceSetList.add(it) }
            parser.extractPlatformSourceSet(it, "ios").forEach { sourceSetList.add(it) }
        }

        return PlatformBuildConfig(
            platform = "ios",
            plugins = iosPlugins,
            androidBlock = null,
            sourceSets = sourceSetList,
            composeResourcesBlock = composeResourcesBlock,
            iosTargetsBlock = iosTargetsBlock,
            cocoapodsBlock = cocoapodsBlock,
            sqldelightBlock = sqldelightBlock,
            dependenciesBlock = null,
            extraPluginDeclarations = extraPluginDeclarations
        )
    }

    /**
     * 提取 JS 平台配置
     */
    fun extractJsConfig(): PlatformBuildConfig {
        val plugins = parser.parsePlugins()
        val jsPlugins = plugins.filter {
            it == "myhub.kmp" || it == "myhub.kmp.js" ||
                !it.startsWith("myhub.kmp.") // 保留非平台插件
        }

        val sourceSets = parser.parseSourceSets()
        val composeResourcesBlock = parser.parseComposeResourcesBlock()
        val sqldelightBlock = parser.parseSqlDelightBlock()
        val sourceSetList = mutableListOf<String>()
        sourceSets?.let {
            val (commonMain, commonTest) = parser.extractCommonSourceSets(it)
            commonMain?.let { sourceSetList.add(it) }
            commonTest?.let { sourceSetList.add(it) }
            parser.extractPlatformSourceSet(it, "js").forEach { sourceSetList.add(it) }
        }

        return PlatformBuildConfig(
            platform = "js",
            plugins = jsPlugins,
            androidBlock = null,
            sourceSets = sourceSetList,
            composeResourcesBlock = composeResourcesBlock,
            sqldelightBlock = sqldelightBlock,
            dependenciesBlock = null
        )
    }

    /**
     * 提取 WASM JS 平台配置
     */
    fun extractWasmJsConfig(): PlatformBuildConfig {
        val plugins = parser.parsePlugins()
        val wasmJsPlugins = plugins.filter {
            it == "myhub.kmp" || it == "myhub.kmp.wasmJs" ||
                !it.startsWith("myhub.kmp.") // 保留非平台插件
        }

        val sourceSets = parser.parseSourceSets()
        val composeResourcesBlock = parser.parseComposeResourcesBlock()
        val sqldelightBlock = parser.parseSqlDelightBlock()
        val sourceSetList = mutableListOf<String>()
        sourceSets?.let {
            val (commonMain, commonTest) = parser.extractCommonSourceSets(it)
            commonMain?.let { sourceSetList.add(it) }
            commonTest?.let { sourceSetList.add(it) }
            parser.extractPlatformSourceSet(it, "wasmJs").forEach { sourceSetList.add(it) }
        }

        return PlatformBuildConfig(
            platform = "wasmJs",
            plugins = wasmJsPlugins,
            androidBlock = null,
            sourceSets = sourceSetList,
            composeResourcesBlock = composeResourcesBlock,
            sqldelightBlock = sqldelightBlock,
            dependenciesBlock = null
        )
    }
}

/**
 * 平台构建配置数据类
 */
data class PlatformBuildConfig(
    val platform: String,
    val plugins: List<String>,
    val androidBlock: String?,
    val sourceSets: List<String>,
    val composeResourcesBlock: String? = null,
    val iosTargetsBlock: String? = null,
    val cocoapodsBlock: String? = null,
    val sqldelightBlock: String? = null,
    val dependenciesBlock: String? = null,
    val extraPluginDeclarations: List<String> = emptyList() // 额外的插件声明，如 kotlin("native.cocoapods")
)

/**
 * 构建文件生成器
 * 负责根据平台配置生成 build.{platform}.gradle.kts 文件
 */
class BuildFileGenerator {
    fun generate(config: PlatformBuildConfig): String {
        val pluginsBlock = buildString {
            appendLine("plugins {")
            config.plugins.forEach { plugin ->
                appendLine("    alias(libs.plugins.$plugin)")
            }
            // 添加额外的插件声明（如 kotlin("native.cocoapods")）
            config.extraPluginDeclarations.forEach { pluginDeclaration ->
                // 保持原始格式，但需要调整缩进以适应 plugins 块
                val lines = pluginDeclaration.lines()
                val firstNonBlankLine = lines.firstOrNull { it.trim().isNotBlank() }
                val baseIndent = if (firstNonBlankLine != null) {
                    firstNonBlankLine.length - firstNonBlankLine.trimStart().length
                } else {
                    0
                }
                
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        val trimmed = line.trimStart()
                        val originalIndent = line.length - trimmed.length
                        // 计算相对缩进（相对于第一行的缩进）
                        val relativeIndent = originalIndent - baseIndent
                        // 在 plugins 块内，第一行应该有4个空格缩进
                        val finalIndent = 4 + maxOf(0, relativeIndent)
                        appendLine(" ".repeat(finalIndent) + trimmed)
                    } else {
                        appendLine()
                    }
                }
            }
            appendLine("}")
            appendLine()
        }

        // compose.resources 配置块（如果有）
        val composeResourcesBlock = if (config.composeResourcesBlock != null) {
            buildString {
                appendLine(config.composeResourcesBlock)
                appendLine()
            }
        } else {
            ""
        }

        // sqldelight 配置块（如果有）
        val sqldelightBlock = if (config.sqldelightBlock != null) {
            buildString {
                appendLine(config.sqldelightBlock)
                appendLine()
            }
        } else {
            ""
        }

        val kotlinBlock = buildString {
            appendLine("kotlin {")

            // Android 特定配置
            if (config.platform == "android" && config.androidBlock != null) {
                appendLine("    android {")
                // 保持原始缩进
                config.androidBlock.trim().lines().forEach { line ->
                    if (line.isNotBlank()) {
                        appendLine("        $line")
                    }
                }
                appendLine("    }")
            }

            // iOS 平台 cinterop 配置（iosTargets().forEach { ... }）
            if (config.platform == "ios" && config.iosTargetsBlock != null) {
                // iosTargetsBlock 已经包含了完整的块，在原始文件中已经有相对于 kotlin { 的缩进
                // 但需要确保第一行有正确的缩进（4个空格）
                val lines = config.iosTargetsBlock.lines()
                lines.forEachIndexed { index, line ->
                    if (line.isNotBlank()) {
                        val originalIndent = line.length - line.trimStart().length
                        // 如果第一行没有缩进，添加4个空格；否则保持原始缩进
                        val finalIndent = if (index == 0 && originalIndent == 0) {
                            4
                        } else {
                            originalIndent
                        }
                        appendLine(" ".repeat(finalIndent) + line.trimStart())
                    } else {
                        appendLine()
                    }
                }
                appendLine()
            }

            // iOS 平台 CocoaPods 配置（cocoapods { ... }）
            if (config.platform == "ios" && config.cocoapodsBlock != null) {
                // cocoapodsBlock 已经包含了完整的块，在原始文件中已经有相对于 kotlin { 的缩进
                // 但需要确保第一行有正确的缩进（4个空格）
                val lines = config.cocoapodsBlock.lines()
                lines.forEachIndexed { index, line ->
                    if (line.isNotBlank()) {
                        val originalIndent = line.length - line.trimStart().length
                        // 如果第一行没有缩进，添加4个空格；否则保持原始缩进
                        val finalIndent = if (index == 0 && originalIndent == 0) {
                            4
                        } else {
                            originalIndent
                        }
                        appendLine(" ".repeat(finalIndent) + line.trimStart())
                    } else {
                        appendLine()
                    }
                }
                appendLine()
            }

            // SourceSets
            // 即使没有显式定义 sourceSets，也要生成一个基本的 sourceSets 块
            // 这样 Gradle 才能正确识别平台并生成变体
            if (config.sourceSets.isNotEmpty()) {
                appendLine("    sourceSets {")
                config.sourceSets.forEach { sourceSet ->
                    // sourceSet 内容需要调整缩进
                    // 第一行（sourceSet 名称）应该没有缩进，其他行保持相对缩进
                    val lines = sourceSet.lines()
                    val firstNonBlankLine = lines.firstOrNull { it.isNotBlank() } ?: ""
                    val baseIndent = if (firstNonBlankLine.isNotBlank()) {
                        firstNonBlankLine.length - firstNonBlankLine.trimStart().length
                    } else {
                        0
                    }

                    lines.forEachIndexed { index, line ->
                        if (line.isNotBlank()) {
                            val lineIndent = line.length - line.trimStart().length
                            // 计算相对缩进
                            val relativeIndent = if (baseIndent == 0) {
                                // 如果第一行没有缩进，说明它是 sourceSet 名称
                                // 其他行的缩进是相对于原始文件中的 sourceSets 的
                                // 我们需要将这些缩进转换为相对于 commonMain 的缩进
                                // commonMain 在原始文件中有 8 个空格缩进（相对于 sourceSets）
                                // 所以其他行的缩进需要减去 8
                                if (index == 0) {
                                    0 // 第一行（sourceSet 名称）没有相对缩进
                                } else {
                                    maxOf(0, lineIndent - 8) // 减去 commonMain 的缩进（8）
                                }
                            } else {
                                // 如果第一行有缩进，使用正常的相对缩进计算
                                maxOf(0, lineIndent - baseIndent)
                            }
                            val finalIndent = 8 + relativeIndent // 8 是 sourceSets 内的基础缩进
                            appendLine(" ".repeat(finalIndent) + line.trimStart())
                        } else {
                            appendLine()
                        }
                    }
                }
                appendLine("    }")
            } else {
                // 如果没有显式定义 sourceSets，生成一个空的 sourceSets 块
                // 这样 Gradle 才能正确识别平台并生成变体
                appendLine("    sourceSets {")
                appendLine("    }")
            }

            appendLine("}")
        }

        // dependencies 配置块（如果有）
        val dependenciesBlock = if (config.dependenciesBlock != null) {
            buildString {
                appendLine()
                appendLine(config.dependenciesBlock)
            }
        } else {
            ""
        }

        return pluginsBlock + composeResourcesBlock + sqldelightBlock + kotlinBlock + dependenciesBlock
    }
}

/**
 * 动态生成并设置平台特定的构建文件
 */
fun generateAndSetBuildFile(
    project: org.gradle.api.initialization.ProjectDescriptor,
    platform: String,
    rootProject: org.gradle.api.initialization.ProjectDescriptor
) {
    // 根项目始终使用默认构建文件，不进行平台特定的构建文件生成
    if (project == rootProject) {
        project.buildFileName = "build.gradle.kts"
        return
    }

    // 优先级检查：项目根目录中的 build.xx.gradle.kts > build 目录中的 build.xx.gradle.kts > 执行生成
    // 1. 首先检查项目根目录中是否有 build.$platform.gradle.kts
    val projectRootBuildFile = File(project.projectDir, "build.$platform.gradle.kts")
    if (projectRootBuildFile.exists()) {
        // 项目根目录中的文件存在，优先使用
        project.buildFileName = "build.$platform.gradle.kts"
        println("ℹ️  使用项目根目录中的平台特定构建文件: build.$platform.gradle.kts (${project.name})")
        return
    }

    // 2. 检查 build 目录中的 build.$platform.gradle.kts
    val platformBuildFile = File(project.projectDir, "build/build-gradle-files-kts/build.$platform.gradle.kts")
    if (platformBuildFile.exists()) {
        // build 目录中的文件已存在，直接使用
        project.buildFileName = "build/build-gradle-files-kts/build.$platform.gradle.kts"
        println("ℹ️  使用 build 目录中的平台特定构建文件: build.$platform.gradle.kts (${project.name})")
        println("   提示: 如果遇到编译问题，可以执行 'gradlew clean' 清理后重新生成")
        return
    }

    // 文件不存在，需要生成新文件，开始解析和生成流程
    val buildGradleKts = File(project.projectDir, "build.gradle.kts")
    if (!buildGradleKts.exists()) {
        // 如果没有 build.gradle.kts，可能是中间目录，使用默认构建文件
        project.buildFileName = "build.gradle.kts"
        return
    }

    try {
        val buildFileContent = buildGradleKts.readText()
        val parser = BuildFileParser(buildFileContent)
        val extractor = PlatformConfigExtractor(parser)
        val generator = BuildFileGenerator()

        val config = when (platform) {
            "android" -> extractor.extractAndroidConfig()
            "jvm" -> extractor.extractJvmConfig()
            "ios" -> extractor.extractIosConfig()
            "js" -> extractor.extractJsConfig()
            "wasmJs" -> extractor.extractWasmJsConfig()
            else -> {
                // 对于 server 或其他平台，抛出异常
                throw org.gradle.api.GradleException("项目 ${project.name} 不支持平台 '$platform'，无法生成平台特定的构建文件")
            }
        }

        // 检查是否有平台特定的配置
        // 如果有平台插件（如 myhub.kmp.ios）或者有平台 sourceSet，都应该生成平台特定的构建文件
        val hasPlatformPlugin = when (platform) {
            "android" -> config.plugins.contains("myhub.kmp.android")
            "jvm" -> config.plugins.contains("myhub.kmp.jvm")
            "ios" -> config.plugins.contains("myhub.kmp.ios")
            "js" -> config.plugins.contains("myhub.kmp.js")
            "wasmJs" -> config.plugins.contains("myhub.kmp.wasmJs")
            else -> false
        }
        val hasPlatformSourceSet = when (platform) {
            "android" -> config.androidBlock != null || config.sourceSets.any { it.contains("android") }
            "jvm" -> config.sourceSets.any { it.contains("jvm") }
            "ios" -> config.sourceSets.any { it.contains("ios") }
            "js" -> config.sourceSets.any { it.contains("js") }
            "wasmJs" -> config.sourceSets.any { it.contains("wasmJs") }
            else -> false
        }
        val hasPlatformConfig = hasPlatformPlugin || hasPlatformSourceSet

        if (hasPlatformConfig) {
            // 生成新的构建文件
            val generatedContent = generator.generate(config)
            platformBuildFile.parentFile?.mkdirs() // 只创建父目录，不创建文件本身
            platformBuildFile.writeText(generatedContent)
            project.buildFileName = "build/build-gradle-files-kts/build.$platform.gradle.kts" // 设置正确的构建文件路径
            println("✅ 动态生成平台特定的构建文件: build.$platform.gradle.kts (${project.name})")
        } else {
            // 没有平台特定配置
            if (project == rootProject) {
                // 根项目没有平台配置时，使用默认构建文件
                project.buildFileName = "build.gradle.kts"
                println("ℹ️  根项目 ${project.name} 没有平台 '$platform' 的特定配置，使用默认构建文件: build.gradle.kts")
            } else {
                // 子项目没有平台配置时，为非KMP 模块
                project.buildFileName = "build.gradle.kts"
                println(
                    "项目 ${project.name} 的 build.gradle.kts 中没有找到平台 '$platform' 的特定配置，" +
                        "无法生成平台特定的构建文件。请检查 build.gradle.kts 是否包含该平台的配置。"
                )
            }
        }
    } catch (e: org.gradle.api.GradleException) {
        // 重新抛出 GradleException
        throw e
    } catch (e: Exception) {
        // 其他异常包装为 GradleException
        throw org.gradle.api.GradleException("项目 ${project.name} 解析构建文件失败: ${e.message}", e)
    }
}

// 将函数赋值给 extra 属性，以便在 settings.gradle.kts 中使用
extensions.extraProperties["generateAndSetBuildFile"] = ::generateAndSetBuildFile
