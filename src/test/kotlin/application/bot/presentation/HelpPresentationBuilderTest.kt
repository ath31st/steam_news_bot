package sidim.doma.application.bot.presentation

import dev.inmo.tgbotapi.types.rich.InputRichBlockList
import dev.inmo.tgbotapi.types.rich.InputRichBlockParagraph
import dev.inmo.tgbotapi.types.rich.InputRichBlockSectionHeading
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HelpPresentationBuilderTest {
    private val builder = HelpPresentationBuilder()

    @Test
    fun buildsSectionedHelpMessage() {
        val message = builder.build("en")
        val blocks = requireNotNull(message.blocks)
        val headings = blocks.filterIsInstance<InputRichBlockSectionHeading>()
            .map { it.text.toString() }

        assertTrue(headings.any { it.contains("Help") })
        assertTrue(headings.any { it.contains("How the bot works") })
        assertTrue(headings.any { it.contains("Commands") })
        assertTrue(headings.any { it.contains("Steam ID") })
        assertTrue(headings.any { it.contains("Wishlist") })
        assertTrue(headings.any { it.contains("Blacklist") })
        assertTrue(blocks.any { it is InputRichBlockList })
        assertTrue(
            blocks.filterIsInstance<InputRichBlockParagraph>()
                .any { it.text.toString().contains("feedback_genie_bot") }
        )
    }

    @Test
    fun doesNotUseLegacyMonolithicHelpText() {
        val message = builder.build("en")
        val allText = message.blocks.orEmpty().joinToString(" ") { it.toString() }

        assertFalse(allText.contains("1. News about your games is updated every half hour"))
    }
}
