package sidim.doma.plugin

import java.util.*

object Localization {
    private const val DEFAULT_LOCALE = "en"
    private const val RESOURCE_BUNDLE_BASE_NAME = "messages"

    private fun getMessage(key: String, locale: String, vararg args: Any): String {
        val effectiveLocale = if (locale == "ru") "ru" else DEFAULT_LOCALE
        val localeObj = Locale.forLanguageTag(effectiveLocale)
        val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, localeObj)
        val message = bundle.getString(key)
        return if (args.isNotEmpty()) message.format(*args) else message
    }

    fun getButton(key: String, locale: String): String {
        return getMessage("button.$key", locale)
    }

    fun getText(key: String, locale: String, vararg args: Any): String {
        return getMessage("message.$key", locale, *args)
    }
}