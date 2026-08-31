package sidim.doma.application.news.parser

sealed interface SteamContentNode {
    data class Text(val value: String) : SteamContentNode
    data class Bold(val children: List<SteamContentNode>) : SteamContentNode
    data class Italic(val children: List<SteamContentNode>) : SteamContentNode
    data class Underline(val children: List<SteamContentNode>) : SteamContentNode
    data class Strike(val children: List<SteamContentNode>) : SteamContentNode
    data class Link(val href: String, val children: List<SteamContentNode>) : SteamContentNode
    data class Paragraph(val children: List<SteamContentNode>) : SteamContentNode
    data class Heading(val level: Int, val children: List<SteamContentNode>) : SteamContentNode
    data class Quote(val children: List<SteamContentNode>) : SteamContentNode
    data class Code(val children: List<SteamContentNode>) : SteamContentNode
    data class Pre(val text: String) : SteamContentNode
    data class ListBlock(val items: List<List<SteamContentNode>>) : SteamContentNode
    data class Image(val url: String) : SteamContentNode
    data object LineBreak : SteamContentNode
}

data class ParsedSteamContent(
    val blocks: List<SteamContentNode>,
    val heroImageUrl: String?
)

fun plainTextOf(node: SteamContentNode): String = when (node) {
    is SteamContentNode.Text -> node.value
    is SteamContentNode.Bold -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.Italic -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.Underline -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.Strike -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.Link -> node.children.joinToString("") { plainTextOf(it) }.ifBlank { node.href }
    is SteamContentNode.Code -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.Pre -> node.text
    is SteamContentNode.Paragraph -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.Heading -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.Quote -> node.children.joinToString("") { plainTextOf(it) }
    is SteamContentNode.ListBlock -> node.items.joinToString("\n") { item ->
        "• ${item.joinToString("") { plainTextOf(it) }}"
    }
    is SteamContentNode.Image -> ""
    SteamContentNode.LineBreak -> "\n"
}

fun plainTextLength(node: SteamContentNode): Int = plainTextOf(node).length

fun truncateBlocks(blocks: List<SteamContentNode>, maxLength: Int): Pair<List<SteamContentNode>, Boolean> {
    if (maxLength <= 0) return emptyList<SteamContentNode>() to blocks.isNotEmpty()

    val result = mutableListOf<SteamContentNode>()
    var length = 0
    var truncated = false

    for (block in blocks) {
        val blockLength = plainTextLength(block)
        when {
            blockLength == 0 -> continue
            length + blockLength <= maxLength -> {
                result.add(block)
                length += blockLength
            }
            else -> {
                truncated = true
                val remaining = maxLength - length
                if (remaining > 0) {
                    truncateBlockPlainText(block, remaining)?.let { result.add(it) }
                }
                return result to truncated
            }
        }
    }

    return result to truncated
}

private fun truncateBlockPlainText(block: SteamContentNode, maxChars: Int): SteamContentNode? {
    if (maxChars <= 0) return null

    val text = plainTextOf(block)
    if (text.isEmpty()) return null

    val truncatedText = if (text.length <= maxChars) {
        text
    } else {
        text.take(maxChars).trimEnd() + "…"
    }

    return when (block) {
        is SteamContentNode.Pre -> SteamContentNode.Pre(truncatedText)
        is SteamContentNode.Heading -> SteamContentNode.Heading(
            block.level,
            listOf(SteamContentNode.Text(truncatedText))
        )
        else -> SteamContentNode.Paragraph(listOf(SteamContentNode.Text(truncatedText)))
    }
}
