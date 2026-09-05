package sidim.doma.application.bot.presentation

import dev.inmo.tgbotapi.types.rich.InputRichBlockParagraph
import dev.inmo.tgbotapi.types.rich.InputRichBlockPhoto
import dev.inmo.tgbotapi.types.rich.InputRichBlockSectionHeading
import kotlin.test.Test
import kotlin.test.assertTrue

class StartPresentationBuilderTest {
    private val builder = StartPresentationBuilder()

    @Test
    fun buildsWelcomeWithPhotoAndTitle() {
        val message = builder.build("en")
        val blocks = requireNotNull(message.blocks)

        assertTrue(blocks.any { it is InputRichBlockPhoto })
        assertTrue(
            blocks.filterIsInstance<InputRichBlockSectionHeading>()
                .any { it.text.toString().contains("Steam News Bot") }
        )
        assertTrue(
            blocks.filterIsInstance<InputRichBlockParagraph>()
                .any { it.text.toString().contains("Welcome") }
        )
    }

    @Test
    fun buildsRussianWelcome() {
        val message = builder.build("ru")
        val blocks = requireNotNull(message.blocks)

        assertTrue(
            blocks.filterIsInstance<InputRichBlockParagraph>()
                .any { it.text.toString().contains("Добро пожаловать") }
        )
    }
}
