package sidim.doma.util

import java.util.*

object Localization {
    private const val DEFAULT_LOCALE = "en"
    private const val RESOURCE_BUNDLE_BASE_NAME = "messages"

    private fun getMessage(key: String, locale: String, vararg args: Any): String {
        val effectiveLocale = when (locale.lowercase()) {
            "ru" -> "ru"
            "de" -> "de"
            "fr" -> "fr"
            "uk" -> "uk"
            else -> DEFAULT_LOCALE
        }
        val localeObj = Locale.forLanguageTag(effectiveLocale)
        return try {
            val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME, localeObj)
            val message = bundle.getString(key)
            if (args.isNotEmpty()) message.format(*args) else message
        } catch (e: MissingResourceException) {
            "Error: Missing key '$key' for locale '$effectiveLocale'"
        } catch (e: Exception) {
            "Error: Failed to load message for key '$key' ($e)"
        }
    }

    fun getButton(key: String, locale: String): String {
        return getMessage(key, locale)
    }

    fun getText(key: String, locale: String, vararg args: Any): String {
        return getMessage(key, locale, *args)
    }
}