package sidim.doma.application.news.parser

import dev.inmo.tgbotapi.types.rich.InputRichBlock
import dev.inmo.tgbotapi.types.rich.InputRichBlockBlockQuotation
import dev.inmo.tgbotapi.types.rich.InputRichBlockList
import dev.inmo.tgbotapi.types.rich.InputRichBlockListItem
import dev.inmo.tgbotapi.types.rich.InputRichBlockParagraph
import dev.inmo.tgbotapi.types.rich.InputRichBlockPreformatted
import dev.inmo.tgbotapi.types.rich.InputRichBlockSectionHeading
import dev.inmo.tgbotapi.types.rich.RichText
import dev.inmo.tgbotapi.types.rich.RichTextPlain
import dev.inmo.tgbotapi.types.rich.buildRichText

class SteamContentToRichBlocksMapper {

    fun toRichBlocks(
        blocks: List<SteamContentNode>,
        heroImageUrl: String?
    ): List<InputRichBlock> = blocks.mapNotNull { block ->
        when (block) {
            is SteamContentNode.Image -> {
                if (block.url == heroImageUrl) null else InputRichBlockParagraph(toRichText(block))
            }
            else -> toRichBlock(block)
        }
    }

    fun toRichBlock(node: SteamContentNode): InputRichBlock? = when (node) {
        is SteamContentNode.Paragraph -> InputRichBlockParagraph(toRichText(node.children))
        is SteamContentNode.Heading -> InputRichBlockSectionHeading(
            toRichText(node.children),
            node.level.coerceIn(1, 6)
        )
        is SteamContentNode.Quote -> InputRichBlockBlockQuotation(
            listOf(InputRichBlockParagraph(toRichText(node.children)))
        )
        is SteamContentNode.Code -> InputRichBlockParagraph(toRichText(listOf(node)))
        is SteamContentNode.Pre -> InputRichBlockPreformatted(RichTextPlain(node.text))
        is SteamContentNode.ListBlock -> InputRichBlockList(
            node.items.map { item ->
                InputRichBlockListItem.Unordered(
                    blocks = item.mapNotNull { child ->
                        when (child) {
                            is SteamContentNode.Paragraph -> InputRichBlockParagraph(toRichText(child.children))
                            else -> toRichBlock(child)
                        }
                    }
                )
            }
        )
        is SteamContentNode.Image -> InputRichBlockParagraph(toRichText(node))
        is SteamContentNode.Text -> {
            if (node.value.isBlank()) null else InputRichBlockParagraph(toRichText(listOf(node)))
        }
        is SteamContentNode.LineBreak -> InputRichBlockParagraph(RichTextPlain("\n"))
        else -> InputRichBlockParagraph(toRichText(listOf(node)))
    }

    fun toRichText(nodes: List<SteamContentNode>): RichText = buildRichText {
        nodes.forEach { appendNode(it) }
    }

    fun toRichText(node: SteamContentNode): RichText = toRichText(listOf(node))

    private fun dev.inmo.tgbotapi.types.rich.RichTextBuilder.appendNode(node: SteamContentNode) {
        when (node) {
            is SteamContentNode.Text -> plain(node.value)
            is SteamContentNode.Bold -> bold { node.children.forEach { appendNode(it) } }
            is SteamContentNode.Italic -> italic { node.children.forEach { appendNode(it) } }
            is SteamContentNode.Underline -> underline { node.children.forEach { appendNode(it) } }
            is SteamContentNode.Strike -> strikethrough { node.children.forEach { appendNode(it) } }
            is SteamContentNode.Link -> {
                val label = plainTextOf(node).ifBlank { node.href }
                url(label, node.href)
            }
            is SteamContentNode.Code -> code { node.children.forEach { appendNode(it) } }
            is SteamContentNode.Pre -> plain(node.text)
            is SteamContentNode.Image -> plain("")
            SteamContentNode.LineBreak -> plain("\n")
            is SteamContentNode.Paragraph -> node.children.forEach { appendNode(it) }
            is SteamContentNode.Heading -> node.children.forEach { appendNode(it) }
            is SteamContentNode.Quote -> node.children.forEach { appendNode(it) }
            is SteamContentNode.ListBlock -> {
                node.items.forEachIndexed { index, item ->
                    if (index > 0) plain("\n")
                    plain("• ")
                    item.forEach { appendNode(it) }
                }
            }
        }
    }
}
