package sidim.doma.application.news.parser

class SteamContentParser(
    private val imageUrlResolver: SteamImageUrlResolver = SteamImageUrlResolver()
) {
    fun parse(raw: String): ParsedSteamContent {
        val cleaned = removeRelatedLinks(raw).trim()
        if (cleaned.isEmpty()) {
            return ParsedSteamContent(emptyList(), null)
        }

        val imageUrls = imageUrlResolver.extractImageUrls(cleaned)
        val heroImageUrl = imageUrls.firstOrNull()
        val blocks = parseBlocks(cleaned)

        return ParsedSteamContent(blocks, heroImageUrl)
    }

    private fun removeRelatedLinks(content: String): String =
        content.replace(Regex("RELATED LINKS:.*", RegexOption.DOT_MATCHES_ALL), "")

    private fun parseBlocks(input: String): List<SteamContentNode> {
        val parser = Parser(input)
        val blocks = parser.parseTopLevel()
        return normalizeBlocks(blocks)
    }

    private fun normalizeBlocks(blocks: List<SteamContentNode>): List<SteamContentNode> {
        val result = mutableListOf<SteamContentNode>()
        var inlineBuffer = mutableListOf<SteamContentNode>()

        fun flushInline() {
            if (inlineBuffer.isEmpty()) return
            val merged = mergeAdjacentText(inlineBuffer)
            if (merged.any { it !is SteamContentNode.Text || it.value.isNotBlank() }) {
                result.add(SteamContentNode.Paragraph(merged))
            }
            inlineBuffer = mutableListOf()
        }

        for (block in blocks) {
            when (block) {
                is SteamContentNode.Paragraph,
                is SteamContentNode.Heading,
                is SteamContentNode.Quote,
                is SteamContentNode.Code,
                is SteamContentNode.Pre,
                is SteamContentNode.ListBlock,
                is SteamContentNode.Image -> {
                    flushInline()
                    result.add(block)
                }
                is SteamContentNode.LineBreak -> {
                    flushInline()
                }
                else -> inlineBuffer.add(block)
            }
        }
        flushInline()
        return result
    }

    private inner class Parser(private val input: String) {
        private var pos = 0

        fun parseTopLevel(): List<SteamContentNode> = parseUntil(endTags = emptySet())

        private fun parseUntil(endTags: Set<String>): List<SteamContentNode> {
            val nodes = mutableListOf<SteamContentNode>()

            while (pos < input.length) {
                if (tryConsumeClosingTag(endTags)) break

                when {
                    startsWith("{STEAM_CLAN_IMAGE}") -> {
                        val tokenEnd = findSteamImageTokenEnd()
                        val token = input.substring(pos, tokenEnd)
                        imageUrlResolver.resolveClanImage(token)?.let {
                            nodes.add(SteamContentNode.Image(it))
                        }
                        pos = tokenEnd
                    }
                    startsWith("[img]", ignoreCase = true) -> {
                        val end = input.indexOf("[/img]", pos, ignoreCase = true)
                        if (end == -1) {
                            appendText(nodes, "[img]")
                            pos += 5
                        } else {
                            val url = input.substring(pos + 5, end)
                            nodes.add(SteamContentNode.Image(imageUrlResolver.resolveImgTag(url)))
                            pos = end + 6
                        }
                    }
                    input[pos] == '[' -> {
                        val tag = readOpeningBBTag() ?: run {
                            appendText(nodes, "[")
                            pos++
                            continue
                        }
                        when (tag.name.lowercase()) {
                            "h1", "h2", "h3" -> nodes.add(parseHeading(tag))
                            "p" -> nodes.add(parseParagraph(tag))
                            "quote" -> nodes.add(parseQuote(tag))
                            "code" -> nodes.add(parseCode(tag))
                            "pre" -> nodes.add(parsePre(tag))
                            "list" -> nodes.add(parseList(tag))
                            "b", "strong" -> {
                                val children = parseInlineUntil(setOf("b", "strong"))
                                skipClosingTag("b")
                                skipClosingTag("strong")
                                nodes.add(SteamContentNode.Bold(children))
                            }
                            "i", "em" -> {
                                val children = parseInlineUntil(setOf("i", "em"))
                                skipClosingTag("i")
                                skipClosingTag("em")
                                nodes.add(SteamContentNode.Italic(children))
                            }
                            "u" -> {
                                val children = parseInlineUntil(setOf("u"))
                                skipClosingTag("u")
                                nodes.add(SteamContentNode.Underline(children))
                            }
                            "strike" -> {
                                val children = parseInlineUntil(setOf("strike"))
                                skipClosingTag("strike")
                                nodes.add(SteamContentNode.Strike(children))
                            }
                            "url" -> {
                                val href = tag.param ?: ""
                                val children = parseInlineUntil(setOf("url"))
                                skipClosingTag("url")
                                nodes.add(SteamContentNode.Link(href, children))
                            }
                            else -> {
                                appendText(nodes, tag.raw)
                            }
                        }
                    }
                    input[pos] == '<' -> {
                        val tag = readHtmlTag() ?: run {
                            appendText(nodes, "<")
                            pos++
                            continue
                        }
                        when (tag.name.lowercase()) {
                            "br" -> nodes.add(SteamContentNode.LineBreak)
                            "p" -> nodes.add(parseHtmlBlockTag("p", setOf("p")))
                            "strong", "b" -> {
                                val children = parseInlineUntilHtml(setOf("strong", "b"))
                                skipHtmlClosingTag("strong")
                                skipHtmlClosingTag("b")
                                nodes.add(SteamContentNode.Bold(children))
                            }
                            "em", "i" -> {
                                val children = parseInlineUntilHtml(setOf("em", "i"))
                                skipHtmlClosingTag("em")
                                skipHtmlClosingTag("i")
                                nodes.add(SteamContentNode.Italic(children))
                            }
                            "a" -> {
                                val href = tag.attributes["href"] ?: ""
                                val children = parseInlineUntilHtml(setOf("a"))
                                skipHtmlClosingTag("a")
                                nodes.add(SteamContentNode.Link(href, children))
                            }
                            "img" -> {
                                val src = tag.attributes["src"] ?: tag.raw
                                if (src.startsWith("http")) {
                                    nodes.add(SteamContentNode.Image(imageUrlResolver.resolveDirectUrl(src)))
                                }
                            }
                            else -> appendText(nodes, tag.raw)
                        }
                    }
                    else -> {
                        val next = findNextSpecial(pos)
                        appendText(nodes, input.substring(pos, next))
                        pos = next
                    }
                }
            }

            return mergeAdjacentText(nodes)
        }

        private fun parseHeading(openTag: BBTag): SteamContentNode.Heading {
            val level = openTag.name.last().digitToIntOrNull() ?: 1
            val children = parseInlineUntil(setOf(openTag.name, "/${openTag.name}"))
            skipClosingTag(openTag.name)
            return SteamContentNode.Heading(level, children)
        }

        private fun parseParagraph(openTag: BBTag): SteamContentNode.Paragraph {
            val children = parseInlineUntil(setOf("p", "/p"))
            skipClosingTag("p")
            return SteamContentNode.Paragraph(children)
        }

        private fun parseQuote(openTag: BBTag): SteamContentNode.Quote {
            val children = parseInlineUntil(setOf("quote", "/quote"))
            skipClosingTag("quote")
            return SteamContentNode.Quote(children)
        }

        private fun parseCode(openTag: BBTag): SteamContentNode.Code {
            val children = parseInlineUntil(setOf("code", "/code"))
            skipClosingTag("code")
            return SteamContentNode.Code(children)
        }

        private fun parsePre(openTag: BBTag): SteamContentNode.Pre {
            val start = pos
            val end = input.indexOf("[/pre]", pos, ignoreCase = true)
            val text = if (end == -1) input.substring(start) else input.substring(start, end)
            pos = if (end == -1) input.length else end + 6
            return SteamContentNode.Pre(text)
        }

        private fun parseList(openTag: BBTag): SteamContentNode.ListBlock {
            val end = input.indexOf("[/list]", pos, ignoreCase = true)
            val listContent = if (end == -1) input.substring(pos) else input.substring(pos, end)
            pos = if (end == -1) input.length else end + 7

            val items = listContent
                .split(Regex("""\[\*]"""))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { itemContent ->
                    Parser(itemContent).parseUntil(endTags = emptySet())
                }

            return SteamContentNode.ListBlock(items)
        }

        private fun parseHtmlBlockTag(tagName: String, endTags: Set<String>): SteamContentNode.Paragraph {
            val children = parseInlineUntilHtml(endTags)
            skipHtmlClosingTag(tagName)
            return SteamContentNode.Paragraph(children)
        }

        private fun parseInlineUntil(endTags: Set<String>): List<SteamContentNode> =
            parseUntil(endTags = endTags)

        private fun parseInlineUntilHtml(endTags: Set<String>): List<SteamContentNode> =
            parseUntil(endTags = endTags)

        private fun appendText(nodes: MutableList<SteamContentNode>, text: String) {
            if (text.isEmpty()) return
            nodes.add(SteamContentNode.Text(text))
        }

        private fun findNextSpecial(from: Int): Int {
            var i = from
            while (i < input.length) {
                when (input[i]) {
                    '[', '<' -> return i
                    '{' -> if (input.startsWith("{STEAM_CLAN_IMAGE}", i)) return i
                }
                i++
            }
            return input.length
        }

        private fun findSteamImageTokenEnd(): Int {
            var i = pos
            while (i < input.length && !input[i].isWhitespace() && input[i] != '[' && input[i] != '<') i++
            return i
        }

        private fun startsWith(prefix: String, ignoreCase: Boolean = false): Boolean =
            input.startsWith(prefix, pos, ignoreCase)

        private fun tryConsumeClosingTag(endTags: Set<String>): Boolean {
            for (tag in endTags) {
                val bbClosing = "[/$tag]"
                if (input.startsWith(bbClosing, pos, ignoreCase = true)) {
                    pos += bbClosing.length
                    return true
                }
                val htmlClosing = "</$tag>"
                if (input.startsWith(htmlClosing, pos, ignoreCase = true)) {
                    pos += htmlClosing.length
                    return true
                }
            }
            return false
        }

        private fun skipClosingTag(tag: String) {
            val closing = "[/$tag]"
            if (input.startsWith(closing, pos, ignoreCase = true)) {
                pos += closing.length
            }
        }

        private fun skipHtmlClosingTag(tag: String) {
            val closing = "</$tag>"
            if (input.startsWith(closing, pos, ignoreCase = true)) {
                pos += closing.length
            }
        }

        private fun readOpeningBBTag(): BBTag? {
            if (input[pos] != '[') return null
            val close = input.indexOf(']', pos)
            if (close == -1) return null

            val startIndex = pos
            val inner = input.substring(pos + 1, close)
            pos = close + 1

            if (inner.startsWith("/")) return null

            val eq = inner.indexOf('=')
            return if (eq == -1) {
                BBTag(inner.lowercase(), null, input.substring(startIndex, pos))
            } else {
                BBTag(inner.substring(0, eq).lowercase(), inner.substring(eq + 1), input.substring(startIndex, pos))
            }
        }

        private fun readHtmlTag(): HtmlTag? {
            if (input[pos] != '<') return null
            val close = input.indexOf('>', pos)
            if (close == -1) return null

            val raw = input.substring(pos, close + 1)
            pos = close + 1

            val inner = raw.removePrefix("<").removeSuffix(">").trim()
            if (inner.startsWith("/")) return null

            val selfClosing = inner.endsWith("/")
            val normalized = inner.removeSuffix("/").trim()
            val parts = normalized.split(Regex("\\s+"), limit = 2)
            val name = parts[0].lowercase()
            val attributes = parseHtmlAttributes(parts.getOrNull(1) ?: "")

            return HtmlTag(name, attributes, raw, selfClosing)
        }

        private fun parseHtmlAttributes(raw: String): Map<String, String> {
            val attrs = mutableMapOf<String, String>()
            Regex("""(\w+)\s*=\s*"([^"]*)"""").findAll(raw).forEach { match ->
                attrs[match.groupValues[1].lowercase()] = match.groupValues[2]
            }
            return attrs
        }
    }

    private data class BBTag(val name: String, val param: String?, val raw: String)
    private data class HtmlTag(
        val name: String,
        val attributes: Map<String, String>,
        val raw: String,
        val selfClosing: Boolean
    )

    private fun mergeAdjacentText(nodes: List<SteamContentNode>): List<SteamContentNode> {
        if (nodes.isEmpty()) return nodes
        val result = mutableListOf<SteamContentNode>()
        val textBuffer = StringBuilder()

        fun flushText() {
            if (textBuffer.isNotEmpty()) {
                result.add(SteamContentNode.Text(textBuffer.toString()))
                textBuffer.clear()
            }
        }

        for (node in nodes) {
            when (node) {
                is SteamContentNode.Text -> textBuffer.append(node.value)
                else -> {
                    flushText()
                    result.add(node)
                }
            }
        }
        flushText()
        return result
    }
}
