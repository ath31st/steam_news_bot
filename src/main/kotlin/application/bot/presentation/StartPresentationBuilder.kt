package sidim.doma.application.bot.presentation

import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.types.media.TelegramMediaPhoto
import dev.inmo.tgbotapi.types.rich.InputRichMessage
import dev.inmo.tgbotapi.types.rich.InputRichMessageBlocks
import sidim.doma.common.util.LocalizationUtils.getText

class StartPresentationBuilder {

    fun build(locale: String): InputRichMessage = InputRichMessageBlocks {
        photo(loadWelcomePhoto())
        h2(getText("start.title", locale))
        paragraph(getText("start.body", locale))
        divider()
        paragraph(getText("start.cta", locale))
        paragraph(getText("start.feedback", locale))
    }

    private fun loadWelcomePhoto(): TelegramMediaPhoto {
        val bytes = checkNotNull(
            javaClass.classLoader.getResourceAsStream(WELCOME_IMAGE_RESOURCE)
        ) { "Missing resource: $WELCOME_IMAGE_RESOURCE" }
            .use { it.readBytes() }
        return TelegramMediaPhoto(bytes.asMultipartFile(WELCOME_IMAGE_FILENAME))
    }

    companion object {
        const val WELCOME_IMAGE_RESOURCE = "images/steam_welcome.jpg"
        const val WELCOME_IMAGE_FILENAME = "steam_welcome.jpg"
    }
}
